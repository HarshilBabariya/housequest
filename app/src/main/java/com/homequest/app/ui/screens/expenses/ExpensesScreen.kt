package com.homequest.app.ui.screens.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.R
import com.homequest.app.data.repository.byId
import com.homequest.app.data.repository.resolve
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
fun ExpensesScreen(onOpenExpense: (Long) -> Unit, onOpenCategories: () -> Unit, onOpenCalendar: () -> Unit) {
    val vm = appViewModel { ExpensesViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val categoryMap = remember(state.categories) { state.categories.byId() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_expenses)) },
                actions = {
                    IconButton(onClick = onOpenCalendar) { Icon(Icons.Rounded.CalendarMonth, contentDescription = stringResource(R.string.calendar)) }
                    IconButton(onClick = onOpenCategories) { Icon(Icons.Rounded.Tune, contentDescription = stringResource(R.string.categories)) }
                }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 40.dp)) {
            item {
                val change = if (state.lastMonthTotal > 0) (state.monthTotal - state.lastMonthTotal) / state.lastMonthTotal * 100 else null
                Row(Modifier.padding(horizontal = 20.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(stringResource(R.string.this_month), state.monthTotal.money(state.symbol), Icons.Rounded.CalendarMonth, Modifier.weight(1f), hint = stringResource(R.string.n_expenses, state.monthCount))
                    StatTile(
                        stringResource(R.string.vs_last_month),
                        change?.let { "${if (it >= 0) "+" else ""}${"%.0f".format(it)}%" } ?: "—",
                        if (change != null && change > 0) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown,
                        Modifier.weight(1f),
                        hint = stringResource(if (change == null) R.string.no_data_yet else if (change <= 0) R.string.spending_less else R.string.spending_more),
                        accent = if (change != null && change > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
                HqTextField(
                    state.query, vm::setQuery, stringResource(R.string.search),
                    placeholder = stringResource(R.string.search_hint),
                    leading = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.categories, key = { it.id }) { c ->
                        CategoryChip(c, selected = state.categoryId == c.id) { vm.setCategory(c.id) }
                    }
                }
            }
            if (state.sections.isEmpty()) {
                item { EmptyState("🔍", stringResource(R.string.no_expenses_title), stringResource(R.string.no_expenses_body)) }
            }
            state.sections.forEach { (day, rows) ->
                item(key = "day_$day") {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(Dates.relativeLabel(day, stringResource(R.string.today), stringResource(R.string.yesterday)), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(rows.sumOf { it.expense.amount }.money(state.symbol), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
                items(rows, key = { "exp_${it.expense.id}" }) { ExpenseRow(it, categoryMap.resolve(it.expense.category), state.symbol, onClick = { onOpenExpense(it.expense.id) }, showDate = false) }
            }
        }
    }
}
