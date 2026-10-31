package com.homequest.app.ui.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.domain.Category
import com.homequest.app.util.Dates
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ExpensesState(
    val symbol: String = "₹",
    val query: String = "",
    val categoryId: String? = null,
    val categories: List<Category> = emptyList(),
    val sections: List<Pair<Long, List<ExpenseWithPayer>>> = emptyList(),
    val monthTotal: Double = 0.0,
    val lastMonthTotal: Double = 0.0,
    val monthCount: Int = 0
)

class ExpensesViewModel(container: AppContainer) : ViewModel() {
    private val query = MutableStateFlow("")
    private val categoryId = MutableStateFlow<String?>(null)

    val state: StateFlow<ExpensesState> = combine(
        container.expenseRepository.all,
        container.categoryRepository.categories,
        container.settingsRepository.settings,
        combine(query, categoryId) { q, c -> q to c },
        Dates.days()
    ) { all, categories, settings, (q, cat), today ->
        val filtered = all.filter { e ->
            (cat == null || e.expense.category == cat) &&
                (q.isBlank() || e.expense.title.contains(q, ignoreCase = true) || e.expense.note.contains(q, ignoreCase = true))
        }
        val thisMonth = Dates.monthRange(YearMonth.from(today))
        val lastMonth = Dates.monthRange(YearMonth.from(today).minusMonths(1))
        ExpensesState(
            symbol = settings.currencySymbol,
            query = q,
            categoryId = cat,
            categories = categories,
            sections = filtered.groupBy { it.expense.epochDay }.toList().sortedByDescending { it.first },
            monthTotal = all.filter { it.expense.epochDay in thisMonth }.sumOf { it.expense.amount },
            lastMonthTotal = all.filter { it.expense.epochDay in lastMonth }.sumOf { it.expense.amount },
            monthCount = all.count { it.expense.epochDay in thisMonth }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpensesState())

    fun setQuery(value: String) { query.value = value }
    fun setCategory(id: String) { categoryId.value = if (categoryId.value == id) null else id }
}
