package com.homequest.app.data.repository

import com.homequest.app.data.local.ExpenseDao
import com.homequest.app.data.local.ExpenseEntity
import com.homequest.app.data.local.GroupDao
import com.homequest.app.data.local.GroupEntity
import com.homequest.app.data.local.MemberDao
import com.homequest.app.data.local.MemberEntity
import com.homequest.app.data.local.ReminderDao
import com.homequest.app.data.local.ReminderEntity
import com.homequest.app.domain.Accessories
import com.homequest.app.domain.BuiltInCategories
import com.homequest.app.domain.HairStyles
import com.homequest.app.domain.Outfits
import com.homequest.app.domain.PaymentMode
import com.homequest.app.domain.RepeatRule
import com.homequest.app.domain.SplitCalculator
import com.homequest.app.domain.SplitType
import com.homequest.app.util.Dates
import java.time.ZoneId
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SampleDataRepository(
    private val memberDao: MemberDao,
    private val expenseDao: ExpenseDao,
    private val groupDao: GroupDao,
    private val reminderDao: ReminderDao,
    private val reminders: ReminderRepository
) {
    val loaded: Flow<Boolean> = expenseDao.observeSampleCount().map { it > 0 }

    private class Item(val title: String, val category: String, val min: Int, val max: Int, val mode: PaymentMode)

    private val items = listOf(
        Item("Vegetables and fruits", BuiltInCategories.GROCERIES, 120, 480, PaymentMode.UPI),
        Item("Milk and bread", BuiltInCategories.GROCERIES, 60, 140, PaymentMode.CASH),
        Item("Monthly grocery run", BuiltInCategories.GROCERIES, 1800, 3200, PaymentMode.CARD),
        Item("Electricity bill", BuiltInCategories.UTILITIES, 900, 1600, PaymentMode.BANK),
        Item("Mobile recharge", BuiltInCategories.UTILITIES, 239, 719, PaymentMode.UPI),
        Item("Wifi", BuiltInCategories.UTILITIES, 599, 999, PaymentMode.BANK),
        Item("Auto to office", BuiltInCategories.TRANSPORT, 40, 160, PaymentMode.CASH),
        Item("Petrol", BuiltInCategories.TRANSPORT, 500, 1500, PaymentMode.UPI),
        Item("Pani puri", BuiltInCategories.DINING, 40, 120, PaymentMode.CASH),
        Item("Dinner with friends", BuiltInCategories.DINING, 600, 1800, PaymentMode.CARD),
        Item("Coffee", BuiltInCategories.DINING, 120, 320, PaymentMode.UPI),
        Item("Medicines", BuiltInCategories.HEALTH, 150, 900, PaymentMode.UPI),
        Item("Online course", BuiltInCategories.EDUCATION, 499, 1999, PaymentMode.CARD),
        Item("Movie night", BuiltInCategories.ENTERTAINMENT, 300, 900, PaymentMode.UPI),
        Item("Streaming subscription", BuiltInCategories.ENTERTAINMENT, 149, 649, PaymentMode.CARD),
        Item("Shoes", BuiltInCategories.SHOPPING, 1200, 3500, PaymentMode.CARD),
        Item("Gift for mom", BuiltInCategories.SHOPPING, 800, 2500, PaymentMode.UPI),
        Item("House help", BuiltInCategories.OTHER, 1500, 3000, PaymentMode.CASH)
    )

    suspend fun load(ownerId: Long) {
        if (memberDao.sampleCount() > 0) return
        val random = Random(42)
        val priya = memberDao.insert(sampleMember("Priya", "partner", "9876543210", 1, HairStyles.LONG, 0, Outfits.DRESS, Accessories.EARRINGS))
        val arjun = memberDao.insert(sampleMember("Arjun", "roommate", "9123456780", 3, HairStyles.CURLY, 1, Outfits.HOODIE, Accessories.GLASSES))
        val groupId = groupDao.createWithMembers(GroupEntity(name = "Flat 4B", emoji = "🏠", isSample = true), listOf(ownerId, priya, arjun))
        val members = listOf(ownerId, priya, arjun)
        val today = Dates.today()
        repeat(45) { daysAgo ->
            val day = today.minusDays(daysAgo.toLong())
            repeat(random.nextInt(0, 3)) {
                val item = items.random(random)
                val shared = item.max >= 500 && random.nextInt(3) == 0
                val amount = random.nextInt(item.min, item.max + 1).toDouble()
                val expense = ExpenseEntity(
                    title = item.title,
                    amount = amount,
                    category = item.category,
                    paymentMode = item.mode,
                    epochDay = day.toEpochDay(),
                    paidByMemberId = members.random(random),
                    groupId = if (shared) groupId else null,
                    splitType = if (shared) SplitType.EQUAL else SplitType.NONE,
                    isSample = true,
                    createdAt = day.atTime(8 + random.nextInt(13), random.nextInt(60)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                expenseDao.upsertWithSplits(expense, if (shared) SplitCalculator.equalShares(amount, members) else emptyMap())
            }
        }
        val rent = ExpenseEntity(
            title = "Rent", amount = 15000.0, category = BuiltInCategories.RENT, paymentMode = PaymentMode.BANK,
            epochDay = today.withDayOfMonth(1).toEpochDay(), paidByMemberId = ownerId, groupId = groupId, splitType = SplitType.EQUAL, isSample = true
        )
        expenseDao.upsertWithSplits(rent, SplitCalculator.equalShares(rent.amount, members))
        listOf(
            ReminderEntity(title = "Rent", hour = 9, minute = 0, repeat = RepeatRule.MONTHLY, dayOfMonth = 1, amount = 15000.0, category = BuiltInCategories.RENT, isSample = true),
            ReminderEntity(title = "Electricity bill", hour = 10, minute = 0, repeat = RepeatRule.MONTHLY, dayOfMonth = 12, amount = 1400.0, category = BuiltInCategories.UTILITIES, isSample = true),
            ReminderEntity(title = "Streaming subscription", hour = 18, minute = 30, repeat = RepeatRule.MONTHLY, dayOfMonth = 20, amount = 649.0, category = BuiltInCategories.ENTERTAINMENT, isSample = true)
        ).forEach { reminders.save(it) }
    }

    suspend fun clear() {
        reminderDao.samples().forEach { reminders.delete(it) }
        expenseDao.deleteSamples()
        groupDao.deleteSamples()
        memberDao.deleteSamples()
    }

    private fun sampleMember(name: String, relation: String, phone: String, skin: Int, hair: String, hairColor: Int, outfit: String, accessory: String) =
        MemberEntity(
            name = name, relation = relation, phone = phone, skinTone = skin, hairId = hair, hairColor = hairColor,
            outfitId = outfit, accessoryId = accessory, isSample = true
        )
}
