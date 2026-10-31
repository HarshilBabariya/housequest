package com.homequest.app.data.repository

import com.homequest.app.data.local.BudgetDao
import com.homequest.app.data.local.BudgetEntity
import com.homequest.app.data.local.CategoryTotal
import com.homequest.app.data.local.DayTotal
import com.homequest.app.data.local.ExpenseDao
import com.homequest.app.data.local.ExpenseEntity
import com.homequest.app.data.local.ExpenseSplitEntity
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.data.local.MonthCategoryTotal
import com.homequest.app.domain.PaymentMode
import com.homequest.app.domain.SplitCalculator
import com.homequest.app.domain.SplitType
import com.homequest.app.notifications.BudgetMonitor
import com.homequest.app.util.Dates
import com.homequest.app.util.titleCased
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

data class ExpenseDraft(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val categoryId: String,
    val paymentMode: PaymentMode,
    val date: LocalDate,
    val paidByMemberId: Long,
    val groupId: Long? = null,
    val splitType: SplitType = SplitType.NONE,
    val splitMemberIds: List<Long> = emptyList(),
    val customShares: Map<Long, Double> = emptyMap(),
    val note: String = ""
)

class ExpenseRepository(
    private val dao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val gamification: GamificationRepository,
    private val budgetMonitor: BudgetMonitor
) {
    val all: Flow<List<ExpenseWithPayer>> = dao.observeAll()
    val budgets: Flow<List<BudgetEntity>> = budgetDao.observeAll()

    fun byGroup(groupId: Long): Flow<List<ExpenseWithPayer>> = dao.observeByGroup(groupId)
    fun splitsForGroup(groupId: Long): Flow<List<ExpenseSplitEntity>> = dao.observeSplitsForGroup(groupId)
    val allSplits: Flow<List<ExpenseSplitEntity>> = dao.observeAllSplits()
    fun total(range: LongRange): Flow<Double> = dao.observeTotal(range.first, range.last)
    fun categoryTotals(range: LongRange): Flow<List<CategoryTotal>> = dao.observeCategoryTotals(range.first, range.last)
    fun dayTotals(range: LongRange): Flow<List<DayTotal>> = dao.observeDayTotals(range.first, range.last)
    fun monthCategoryTotals(fromDay: Long): Flow<List<MonthCategoryTotal>> = dao.observeMonthCategoryTotals(fromDay)

    suspend fun byId(id: Long): ExpenseEntity? = dao.byId(id)
    suspend fun splitsFor(expenseId: Long): List<ExpenseSplitEntity> = dao.splitsFor(expenseId)
    suspend fun allForExport(): List<ExpenseWithPayer> = dao.allForExport()
    suspend fun loggedToday(): Boolean = dao.countOnDay(Dates.today().toEpochDay()) > 0

    suspend fun save(draft: ExpenseDraft): Long {
        val isNew = draft.id == 0L
        val entity = ExpenseEntity(
            id = draft.id,
            title = draft.title.titleCased(),
            amount = draft.amount,
            category = draft.categoryId,
            paymentMode = draft.paymentMode,
            epochDay = draft.date.toEpochDay(),
            paidByMemberId = draft.paidByMemberId,
            groupId = draft.groupId,
            splitType = if (draft.groupId == null) SplitType.NONE else draft.splitType,
            note = draft.note.titleCased()
        )
        val shares = when (entity.splitType) {
            SplitType.EQUAL -> SplitCalculator.equalShares(draft.amount, draft.splitMemberIds)
            SplitType.CUSTOM -> draft.customShares.filterValues { it > 0 }
            SplitType.NONE -> emptyMap()
        }
        val id = dao.upsertWithSplits(entity, shares)
        if (isNew) {
            gamification.recordExpenseLogged(Dates.today(), dao.count())
            budgetMonitor.afterExpenseAdded(draft.categoryId, draft.amount)
        }
        return id
    }

    suspend fun delete(expense: ExpenseEntity) = dao.delete(expense)

    suspend fun setCategoryBudget(categoryId: String, limit: Double?) {
        if (limit == null || limit <= 0) budgetDao.delete(categoryId) else budgetDao.upsert(BudgetEntity(categoryId, limit))
    }
}
