package com.homequest.app.notifications

import com.homequest.app.data.local.BudgetDao
import com.homequest.app.data.local.ExpenseDao
import com.homequest.app.data.repository.GamificationRepository
import com.homequest.app.data.repository.SettingsRepository
import com.homequest.app.domain.ExpenseCategory
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import java.time.YearMonth

class BudgetMonitor(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val settings: SettingsRepository,
    private val gamification: GamificationRepository,
    private val notifications: NotificationHelper
) {
    suspend fun afterExpenseAdded(category: ExpenseCategory, amount: Double) {
        val s = settings.current()
        if (!s.budgetAlertsEnabled) return
        val range = Dates.monthRange(YearMonth.now())
        if (s.monthlyBudget > 0) {
            val total = expenseDao.total(range.first, range.last)
            notifyIfCrossed("Monthly budget", total - amount, total, s.monthlyBudget, s.currencySymbol)
        }
        budgetDao.forCategory(category.name)?.let { budget ->
            val total = expenseDao.totalForCategory(category.name, range.first, range.last)
            notifyIfCrossed("${category.label} budget", total - amount, total, budget.monthlyLimit, s.currencySymbol)
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

    private fun notifyIfCrossed(label: String, before: Double, after: Double, limit: Double, symbol: String) {
        val thresholds = listOf(0.8 to "80%", 1.0 to "100%")
        for ((fraction, text) in thresholds) {
            val line = limit * fraction
            if (before < line && after >= line) {
                val title = if (fraction >= 1.0) "$label exceeded" else "$label at $text"
                notifications.showBudgetAlert(title, "Spent ${after.money(symbol)} of ${limit.money(symbol)} this month.")
            }
        }
    }
}
