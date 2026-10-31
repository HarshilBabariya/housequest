package com.homequest.app.ui.screens.profile

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.domain.ExpenseCategory
import com.homequest.app.domain.Features
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.CategoryIcon
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.components.StackedMonthChart
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class InsightsState(
    val unlocked: Boolean = false,
    val symbol: String = "₹",
    val months: List<String> = emptyList(),
    val series: Map<ExpenseCategory, List<Double>> = emptyMap(),
    val monthTotals: List<Double> = emptyList()
) {
    val average: Double get() = monthTotals.filter { it > 0 }.let { if (it.isEmpty()) 0.0 else it.average() }
    val topCategory: ExpenseCategory? get() = series.maxByOrNull { it.value.sum() }?.key
}

class InsightsViewModel(container: AppContainer) : ViewModel() {
    private val monthKeys = (5 downTo 0).map { YearMonth.now().minusMonths(it.toLong()) }
    private val labelFormat = DateTimeFormatter.ofPattern("MMM")

    val state: StateFlow<InsightsState> = combine(
        container.gamificationRepository.unlockedIds,
        container.settingsRepository.settings,
        container.expenseRepository.monthCategoryTotals(monthKeys.first().atDay(1).toEpochDay())
    ) { unlocked, settings, rows ->
        val keys = monthKeys.map { Dates.monthKey(it.atDay(1)) }
        val series = rows.groupBy { it.category }.mapValues { (_, list) ->
            keys.map { k -> list.firstOrNull { it.month == k }?.total ?: 0.0 }
        }
        InsightsState(
            unlocked = Features.INSIGHTS in unlocked,
            symbol = settings.currencySymbol,
            months = monthKeys.map { it.format(labelFormat) },
            series = series,
            monthTotals = keys.indices.map { i -> series.values.sumOf { it[i] } }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsState())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(onBack: () -> Unit) {
    val vm = appViewModel { InsightsViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        if (!state.unlocked) {
            EmptyState("🔒", "Advanced insights is locked", "Unlock it in the Rewards shop for 300 points to see 6-month category trends.", Modifier.padding(padding))
            return@Scaffold
        }
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            SectionHeader("Last 6 months by category")
            Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                StackedMonthChart(state.months, state.series, Modifier.padding(16.dp))
            }
            SectionHeader("Highlights")
            Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Average month: ${state.average.money(state.symbol)}", style = MaterialTheme.typography.titleMedium)
                    state.topCategory?.let { Text("Biggest category: ${it.label}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp)) }
                    val last = state.monthTotals.lastOrNull() ?: 0.0
                    val prev = state.monthTotals.dropLast(1).lastOrNull() ?: 0.0
                    if (prev > 0) {
                        val change = (last - prev) / prev * 100
                        Text(
                            if (change <= 0) "Down ${"%.0f".format(-change)}% vs last month 🎉" else "Up ${"%.0f".format(change)}% vs last month",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (change <= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
            SectionHeader("Legend")
            state.series.keys.forEach { c ->
                Row(Modifier.padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryIcon(c, size = 28)
                    Text(c.label, Modifier.padding(start = 10.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
