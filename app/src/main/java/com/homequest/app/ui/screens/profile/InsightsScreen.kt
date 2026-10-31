package com.homequest.app.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.R
import com.homequest.app.data.repository.byId
import com.homequest.app.data.repository.resolve
import com.homequest.app.domain.Category
import com.homequest.app.domain.Features
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.CategoryIcon
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.components.StackedMonthChart
import com.homequest.app.ui.components.label
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class InsightsState(
    val unlocked: Boolean = false,
    val symbol: String = "₹",
    val months: List<String> = emptyList(),
    val series: Map<Category, List<Double>> = emptyMap(),
    val monthTotals: List<Double> = emptyList()
) {
    val average: Double get() = monthTotals.filter { it > 0 }.let { if (it.isEmpty()) 0.0 else it.average() }
    val topCategory: Category? get() = series.maxByOrNull { it.value.sum() }?.key
}

class InsightsViewModel(container: AppContainer) : ViewModel() {
    private val monthKeys = (5 downTo 0).map { YearMonth.now().minusMonths(it.toLong()) }

    val state: StateFlow<InsightsState> = combine(
        container.gamificationRepository.unlockedIds,
        container.settingsRepository.settings,
        container.expenseRepository.monthCategoryTotals(monthKeys.first().atDay(1).toEpochDay()),
        container.categoryRepository.categories
    ) { unlocked, settings, rows, categories ->
        val keys = monthKeys.map { Dates.monthKey(it.atDay(1)) }
        val map = categories.byId()
        val series = rows.groupBy { map.resolve(it.category) }.mapValues { (_, list) ->
            keys.map { k -> list.filter { it.month == k }.sumOf { it.total } }
        }
        InsightsState(
            unlocked = Features.INSIGHTS in unlocked,
            symbol = settings.currencySymbol,
            months = monthKeys.map { it.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault())) },
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
                title = { Text(stringResource(R.string.insights)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        }
    ) { padding ->
        if (!state.unlocked) {
            EmptyState("🔒", stringResource(R.string.insights_locked_title), stringResource(R.string.insights_locked_body), Modifier.padding(padding))
            return@Scaffold
        }
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            SectionHeader(stringResource(R.string.six_months_by_category))
            HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                StackedMonthChart(state.months, state.series)
            }
            SectionHeader(stringResource(R.string.highlights))
            HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                Text(stringResource(R.string.average_month, state.average.money(state.symbol)), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                state.topCategory?.let { Text(stringResource(R.string.biggest_category, it.label()), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                val last = state.monthTotals.lastOrNull() ?: 0.0
                val prev = state.monthTotals.dropLast(1).lastOrNull() ?: 0.0
                if (prev > 0) {
                    val change = (last - prev) / prev * 100
                    Text(
                        if (change <= 0) stringResource(R.string.down_vs_last, "%.0f".format(-change)) else stringResource(R.string.up_vs_last, "%.0f".format(change)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (change <= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
            SectionHeader(stringResource(R.string.legend))
            state.series.keys.forEach { c ->
                Row(Modifier.padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryIcon(c, size = 28)
                    Text(c.label(), Modifier.padding(start = 10.dp), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
