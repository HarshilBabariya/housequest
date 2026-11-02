package com.homequest.app.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.R
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.data.repository.byId
import com.homequest.app.data.repository.resolve
import com.homequest.app.domain.Category
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.ExpenseRow
import com.homequest.app.ui.components.HqCard
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class CalendarState(
    val month: YearMonth = YearMonth.now(),
    val today: LocalDate = Dates.today(),
    val selected: LocalDate? = null,
    val symbol: String = "₹",
    val dailyBudget: Double? = null,
    val totals: Map<Long, Double> = emptyMap(),
    val dayExpenses: List<ExpenseWithPayer> = emptyList(),
    val categories: Map<String, Category> = emptyMap()
) {
    val monthTotal: Double get() = totals.values.sum()
}

class CalendarViewModel(container: AppContainer) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val selected = MutableStateFlow<LocalDate?>(null)

    val state: StateFlow<CalendarState> = combine(
        container.expenseRepository.all,
        container.categoryRepository.categories,
        container.settingsRepository.settings,
        combine(month, selected) { m, d -> m to d },
        Dates.days()
    ) { all, categories, settings, (m, day), today ->
        val range = Dates.monthRange(m)
        CalendarState(
            month = m,
            today = today,
            selected = day,
            symbol = settings.currencySymbol,
            dailyBudget = settings.monthlyBudget.takeIf { it > 0 }?.let { it / m.lengthOfMonth() },
            totals = all.filter { it.expense.epochDay in range }.groupBy { it.expense.epochDay }.mapValues { (_, list) -> list.sumOf { it.expense.amount } },
            dayExpenses = day?.let { d -> all.filter { it.expense.epochDay == d.toEpochDay() } }.orEmpty(),
            categories = categories.byId()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarState())

    fun showMonth(value: YearMonth) {
        month.value = value
        selected.value = null
    }

    fun select(day: LocalDate) { selected.value = if (selected.value == day) null else day }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(onBack: () -> Unit, onOpenExpense: (Long) -> Unit) {
    val vm = appViewModel { CalendarViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    var pickingMonth by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } },
                actions = {
                    Box {
                        TextButton(onClick = { pickingMonth = true }) {
                            Text(Dates.monthLabel(state.month), maxLines = 1)
                            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = pickingMonth, onDismissRequest = { pickingMonth = false }) {
                            (0L..11L).map { YearMonth.from(state.today).minusMonths(it) }.forEach { m ->
                                DropdownMenuItem(text = { Text(Dates.monthLabel(m)) }, onClick = { vm.showMonth(m); pickingMonth = false })
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    IconButton(onClick = { vm.showMonth(state.month.minusMonths(1)) }) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_period))
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(Dates.monthLabel(state.month), style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Text(state.monthTotal.money(state.symbol), style = MaterialTheme.typography.labelLarge, color = scheme.primary, maxLines = 1)
                    }
                    IconButton(onClick = { vm.showMonth(state.month.plusMonths(1)) }, enabled = state.month < YearMonth.from(state.today)) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = stringResource(R.string.next_period))
                    }
                }
            }
            item { MonthGrid(state, onSelect = vm::select) }
            item {
                HqCard(Modifier.fillMaxWidth().padding(top = 14.dp)) {
                    Text(
                        state.dailyBudget?.let { stringResource(R.string.avg_daily_budget, it.money(state.symbol)) } ?: stringResource(R.string.calendar_no_budget),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant
                    )
                    if (state.dailyBudget != null) {
                        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            LegendDot(OverColor, stringResource(R.string.over_budget))
                            LegendDot(WithinColor, stringResource(R.string.within_budget))
                        }
                    }
                }
            }
            state.selected?.let { day ->
                item {
                    Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(Dates.longLabel(day.toEpochDay()), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text((state.totals[day.toEpochDay()] ?: 0.0).money(state.symbol), style = MaterialTheme.typography.titleMedium, color = scheme.primary, maxLines = 1)
                    }
                }
                if (state.dayExpenses.isEmpty()) {
                    item { EmptyState("🌤️", stringResource(R.string.calendar_quiet_day), stringResource(R.string.first_expense_hint)) }
                }
                items(state.dayExpenses, key = { "cal_${it.expense.id}" }) {
                    ExpenseRow(it, state.categories.resolve(it.expense.category), state.symbol, onClick = { onOpenExpense(it.expense.id) }, showDate = false)
                }
            }
        }
    }
}

private val OverColor = Color(0xFFE5484D)
private val WithinColor = Color(0xFF30A46C)

@Composable
private fun MonthGrid(state: CalendarState, onSelect: (LocalDate) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val firstDay = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val days = (0L..6L).map { firstDay.plus(it) }
    val leading = Math.floorMod(state.month.atDay(1).dayOfWeek.value - firstDay.value, 7)
    val cells: List<LocalDate?> = List(leading) { null } + (1..state.month.lengthOfMonth()).map { state.month.atDay(it) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            days.forEach { d ->
                Text(
                    d.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (d == DayOfWeek.SUNDAY) scheme.error else scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
        cells.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0 until 7).forEach { i ->
                    val date = week.getOrNull(i)
                    if (date == null) Spacer(Modifier.weight(1f)) else DayCell(date, state, Modifier.weight(1f), onSelect)
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, state: CalendarState, modifier: Modifier, onSelect: (LocalDate) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val total = state.totals[date.toEpochDay()]
    val budget = state.dailyBudget
    val tint = when {
        total == null -> null
        budget == null -> scheme.primary
        total > budget -> OverColor
        else -> WithinColor
    }
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier
            .aspectRatio(0.72f)
            .clip(shape)
            .background(tint?.copy(alpha = 0.14f) ?: scheme.surfaceContainerHigh.copy(alpha = 0.6f))
            .border(
                if (date == state.selected) 2.dp else if (date == state.today) 1.5.dp else 0.dp,
                if (date == state.selected) scheme.onSurface else if (date == state.today) scheme.primary else Color.Transparent,
                shape
            )
            .clickable(enabled = !date.isAfter(state.today)) { onSelect(date) }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (date == state.today) FontWeight.Bold else FontWeight.Medium,
            color = if (date.isAfter(state.today)) scheme.onSurfaceVariant.copy(alpha = 0.5f) else scheme.onSurface
        )
        if (total != null && tint != null) {
            Text("%,.0f".format(total), style = MaterialTheme.typography.labelSmall, color = tint, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(color.copy(alpha = 0.35f), CircleShape))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 6.dp))
    }
}
