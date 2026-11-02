package com.homequest.app.data.repository

import com.homequest.app.data.local.ChitEntity
import com.homequest.app.data.local.GoldEntity
import com.homequest.app.data.local.HouseholdDao
import com.homequest.app.data.local.ServiceBillEntity
import com.homequest.app.data.local.ServiceDayEntity
import com.homequest.app.data.local.ServiceEntity
import com.homequest.app.data.local.ShopEntity
import com.homequest.app.data.local.ShopEntryEntity
import com.homequest.app.domain.BuiltInCategories
import com.homequest.app.domain.PaymentMode
import com.homequest.app.domain.ServiceBilling
import com.homequest.app.domain.ServiceMonth
import com.homequest.app.util.Dates
import com.homequest.app.util.titleCased
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

class HouseholdRepository(
    private val dao: HouseholdDao,
    private val expenses: ExpenseRepository,
    private val members: MemberRepository
) {
    val shops: Flow<List<ShopEntity>> = dao.observeShops()
    val shopEntries: Flow<List<ShopEntryEntity>> = dao.observeShopEntries()
    val services: Flow<List<ServiceEntity>> = dao.observeServices()
    val serviceDays: Flow<List<ServiceDayEntity>> = dao.observeServiceDays()
    val serviceBills: Flow<List<ServiceBillEntity>> = dao.observeServiceBills()
    val gold: Flow<List<GoldEntity>> = dao.observeGold()
    val chits: Flow<List<ChitEntity>> = dao.observeChits()

    suspend fun addShop(name: String, phone: String) = dao.insertShop(ShopEntity(name = name.titleCased(), phone = phone.trim()))

    suspend fun deleteShop(shop: ShopEntity) = dao.deleteShop(shop)

    suspend fun addPurchase(shop: ShopEntity, amount: Double, note: String) =
        dao.insertShopEntry(ShopEntryEntity(shopId = shop.id, amount = amount, payment = false, note = note.titleCased(), epochDay = Dates.today().toEpochDay()))

    suspend fun payShop(shop: ShopEntity, amount: Double, mode: PaymentMode, title: String) {
        dao.insertShopEntry(ShopEntryEntity(shopId = shop.id, amount = amount, payment = true, epochDay = Dates.today().toEpochDay()))
        logExpense(title, amount, BuiltInCategories.GROCERIES, mode)
    }

    suspend fun deleteShopEntry(entry: ShopEntryEntity) = dao.deleteShopEntry(entry)

    suspend fun addService(name: String, emoji: String, billing: ServiceBilling, rate: Double, defaultQty: Double, leaveDeduction: Double, categoryId: String) =
        dao.insertService(
            ServiceEntity(
                name = name.titleCased(), emoji = emoji, billing = billing, rate = rate, defaultQty = defaultQty,
                leaveDeduction = leaveDeduction, category = categoryId, startEpochDay = YearMonth.from(Dates.today()).atDay(1).toEpochDay()
            )
        )

    suspend fun updateService(service: ServiceEntity) = dao.updateService(service.copy(name = service.name.titleCased()))

    suspend fun deleteService(service: ServiceEntity) = dao.deleteService(service)

    suspend fun setServiceDay(service: ServiceEntity, day: Long, qty: Double) =
        if (qty == service.defaultQty) dao.clearServiceDay(service.id, day) else dao.upsertServiceDay(ServiceDayEntity(service.id, day, qty))

    suspend fun payServiceMonth(service: ServiceEntity, month: YearMonth, bill: ServiceMonth, mode: PaymentMode, title: String) {
        dao.insertServiceBill(ServiceBillEntity(service.id, Dates.monthKey(month.atDay(1)), bill.amount))
        logExpense(title, bill.amount, service.category, mode)
    }

    suspend fun addGold(label: String, grams: Double, karat: Int, pricePaid: Double) =
        dao.insertGold(GoldEntity(label = label.titleCased(), grams = grams, karat = karat, pricePaid = pricePaid, epochDay = Dates.today().toEpochDay()))

    suspend fun deleteGold(item: GoldEntity) = dao.deleteGold(item)

    suspend fun addChit(name: String, monthlyAmount: Double, months: Int, startMonth: YearMonth) =
        dao.insertChit(ChitEntity(name = name.titleCased(), monthlyAmount = monthlyAmount, months = months, startMonth = startMonth.toString()))

    suspend fun payChitInstalment(chit: ChitEntity) {
        if (chit.paidMonths < chit.months) dao.updateChit(chit.copy(paidMonths = chit.paidMonths + 1))
    }

    suspend fun recordChitPrize(chit: ChitEntity, amount: Double) = dao.updateChit(chit.copy(prizeAmount = amount))

    suspend fun deleteChit(chit: ChitEntity) = dao.deleteChit(chit)

    private suspend fun logExpense(title: String, amount: Double, categoryId: String, mode: PaymentMode) {
        if (amount <= 0) return
        expenses.save(ExpenseDraft(title = title, amount = amount, categoryId = categoryId, paymentMode = mode, date = Dates.today(), paidByMemberId = members.activeMemberId()))
    }
}

fun ServiceEntity.qtyOn(day: Long, overrides: Map<Long, Double>): Double = if (day < startEpochDay) 0.0 else overrides[day] ?: defaultQty

fun ServiceEntity.monthBill(month: YearMonth, overrides: Map<Long, Double>, today: LocalDate): ServiceMonth {
    val last = minOf(month.atEndOfMonth().toEpochDay(), today.toEpochDay())
    val days = (month.atDay(1).toEpochDay()..last).filter { it >= startEpochDay }
    val units = days.sumOf { qtyOn(it, overrides) }
    val leaves = days.count { qtyOn(it, overrides) == 0.0 }
    val amount = when (billing) {
        ServiceBilling.PER_UNIT -> units * rate
        ServiceBilling.MONTHLY -> if (days.isEmpty()) 0.0 else (rate - leaves * leaveDeduction).coerceAtLeast(0.0)
    }
    return ServiceMonth(units, leaves, amount)
}

fun List<ShopEntryEntity>.khataBalance(): Double = sumOf { if (it.payment) -it.amount else it.amount }

fun GoldEntity.value(rate24k: Double): Double = grams * rate24k * karat / 24.0

val ChitEntity.paidAmount: Double get() = monthlyAmount * paidMonths
val ChitEntity.completed: Boolean get() = paidMonths >= months
fun ChitEntity.nextDueMonth(): YearMonth = YearMonth.parse(startMonth).plusMonths(paidMonths.toLong())
