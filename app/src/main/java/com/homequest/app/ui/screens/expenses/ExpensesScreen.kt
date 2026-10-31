package com.homequest.app.ui.screens.expenses

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.domain.ExpenseCategory
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.CategoryChip
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.ExpenseRow
import com.homequest.app.ui.components.HqTextField
import com.homequest.app.ui.components.StatTile
import com.homequest.app.util.Dates
import com.homequest.app.util.money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(onAddExpense: () -> Unit, onOpenExpense: (Long) -> Unit) {
    val vm = appViewModel { ExpensesViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { TopAppBar(title = { Text("Expenses") }) },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onAddExpense, icon = { Icon(Icons.Filled.Add, contentDescription = null) }, text = { Text("Add") }) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 96.dp)) {
            item {
                val change = if (state.lastMonthTotal > 0) (state.monthTotal - state.lastMonthTotal) / state.lastMonthTotal * 100 else null
                Row(Modifier.padding(horizontal = 20.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("This month", state.monthTotal.money(state.symbol), Modifier.weight(1f), hint = "${state.monthCount} expenses", emoji = "🗓️")
                    StatTile(
                        "vs last month",
                        change?.let { "${if (it >= 0) "+" else ""}${"%.0f".format(it)}%" } ?: "—",
                        Modifier.weight(1f),
                        hint = if (change == null) "No data yet" else if (change <= 0) "Spending less" else "Spending more",
                        emoji = if (change == null || change <= 0) "📉" else "📈",
                        accent = if (change != null && change > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                HqTextField(
                    state.query, vm::setQuery, "Search",
                    placeholder = "Title or note",
                    leading = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ExpenseCategory.entries) { c ->
                        CategoryChip(c, selected = state.category == c) { vm.setCategory(c) }
                    }
                }
            }
            if (state.sections.isEmpty()) {
                item { EmptyState("🔍", "No expenses here", "Try a different filter, or add a new expense.") }
            }
            state.sections.forEach { (day, rows) ->
                item(key = "day_$day") {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(Dates.relativeLabel(day), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(rows.sumOf { it.expense.amount }.money(state.symbol), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(rows, key = { it.expense.id }) { ExpenseRow(it, state.symbol, onClick = { onOpenExpense(it.expense.id) }, showDate = false) }
            }
        }
    }
}
