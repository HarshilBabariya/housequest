package com.homequest.app.ui.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.domain.ExpenseCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ExpensesState(
    val symbol: String = "₹",
    val query: String = "",
    val category: ExpenseCategory? = null,
    val sections: List<Pair<Long, List<ExpenseWithPayer>>> = emptyList(),
    val total: Double = 0.0
)

class ExpensesViewModel(container: AppContainer) : ViewModel() {
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<ExpenseCategory?>(null)

    val state: StateFlow<ExpensesState> = combine(
        container.expenseRepository.all,
        container.settingsRepository.settings,
        query,
        category
    ) { all, settings, q, cat ->
        val filtered = all.filter { e ->
            (cat == null || e.expense.category == cat) &&
                (q.isBlank() || e.expense.title.contains(q, ignoreCase = true) || e.expense.note.contains(q, ignoreCase = true))
        }
        ExpensesState(
            symbol = settings.currencySymbol,
            query = q,
            category = cat,
            sections = filtered.groupBy { it.expense.epochDay }.toList().sortedByDescending { it.first },
            total = filtered.sumOf { it.expense.amount }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpensesState())

    fun setQuery(value: String) { query.value = value }
    fun setCategory(value: ExpenseCategory?) { category.value = if (category.value == value) null else value }
}
