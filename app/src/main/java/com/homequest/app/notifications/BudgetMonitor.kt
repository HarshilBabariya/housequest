package com.homequest.app.notifications

import android.content.Context
import com.homequest.app.R
import com.homequest.app.data.local.BudgetDao
import com.homequest.app.data.local.CategoryDao
import com.homequest.app.data.local.ExpenseDao
import com.homequest.app.data.repository.GamificationRepository
import com.homequest.app.data.repository.SettingsRepository
import com.homequest.app.domain.BuiltInCategories
import com.homequest.app.util.Dates
import com.homequest.app.util.LocaleHelper
import com.homequest.app.util.money
import java.time.YearMonth

class BudgetMonitor(
    private val context: Context,
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao,
    private val settings: SettingsRepository,
    private val gamification: GamificationRepository,
    private val notifications: NotificationHelper
) {
    suspend fun afterExpenseAdded(categoryId: String, amount: Double) {
        val s = settings.current()
        if (!s.budgetAlertsEnabled) return
        val res = LocaleHelper.localized(context, s.language).resources
        val range = Dates.monthRange(YearMonth.now())
        if (s.monthlyBudget > 0) {
            val total = expenseDao.total(range.first, range.last)
            notifyIfCrossed(s.language, res.getString(R.string.monthly_budget), total - amount, total, s.monthlyBudget, s.currencySymbol)
        }
        budgetDao.forCategory(categoryId)?.let { budget ->
            val total = expenseDao.totalForCategory(categoryId, range.first, range.last)
            val entity = categoryDao.all().firstOrNull { it.id == categoryId }
            val label = BuiltInCategories.nameRes(categoryId)?.let { res.getString(it) } ?: entity?.name ?: categoryId
            notifyIfCrossed(s.language, label, total - amount, total, budget.monthlyLimit, s.currencySymbol)
        }
    }

    suspend fun closePreviousMonthIfNeeded() {
        val s = settings.current()
        val previous = YearMonth.now().minusMonths(1)
        val key = Dates.monthKey(previous.atDay(1))
        if (s.monthlyBudget <= 0) {
            gamification.markBudgetMonthChecked(key)
            return
        }
        val range = Dates.monthRange(previous)
        val spent = expenseDao.total(range.first, range.last)
        if (spent in 0.01..s.monthlyBudget) gamification.recordMonthUnderBudget(key) else gamification.markBudgetMonthChecked(key)
    }

    private fun notifyIfCrossed(language: String, label: String, before: Double, after: Double, limit: Double, symbol: String) {
        listOf(0.8, 1.0).forEach { fraction ->
            val line = limit * fraction
            if (before < line && after >= line) {
                notifications.showBudgetAlert(language, label, after.money(symbol), limit.money(symbol), exceeded = fraction >= 1.0)
            }
        }
    }
}
