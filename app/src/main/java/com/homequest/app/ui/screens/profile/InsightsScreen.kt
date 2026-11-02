package com.homequest.app.ui.screens.profile

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.homequest.app.AppContainer
import com.homequest.app.R
import com.homequest.app.data.local.ExpenseWithPayer
import com.homequest.app.data.repository.avatar
import com.homequest.app.data.repository.byId
import com.homequest.app.data.repository.resolve
import com.homequest.app.domain.Category
import com.homequest.app.domain.CategoryAmount
import com.homequest.app.domain.Features
import com.homequest.app.domain.PaymentMode
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AnimatedMoney
import com.homequest.app.ui.components.AreaChart
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.BarChart
import com.homequest.app.ui.components.CategoryIcon
import com.homequest.app.ui.components.DonutChart
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.components.ShareBar
import com.homequest.app.ui.components.label
import com.homequest.app.ui.screens.dashboard.PayerShare
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class InsightPeriod(@StringRes val labelRes: Int) {
    WEEK(R.string.period_week),
    MONTH(R.string.period_month),
    YEAR(R.string.period_year)
}

data class CategoryShare(val category: Category, val total: Double, val previous: Double)

data class InsightsState(
    val unlocked: Boolean = false,
    val symbol: String = "₹",
    val period: InsightPeriod = InsightPeriod.MONTH,
    val title: String = "",
    val isCurrent: Boolean = true,
    val count: Int = 0,
    val total: Double = 0.0,
    val previousTotal: Double = 0.0,
    val dailyAverage: Double = 0.0,
    val biggest: ExpenseWithPayer? = null,
    val trend: List<Double> = emptyList(),
    val trendLabels: List<String> = emptyList(),
    val trendSlots: Int = 0,
    val trendHighlight: Int = 0,
    val categories: List<CategoryShare> = emptyList(),
    val modes: List<Pair<PaymentMode, Double>> = emptyList(),
    val payers: List<PayerShare> = emptyList()
) {
    val change: Double? get() = if (previousTotal > 0) (total - previousTotal) / previousTotal * 100 else null
}

class InsightsViewModel(container: AppContainer) : ViewModel() {
    private val selection = MutableStateFlow(InsightPeriod.MONTH to 0)

    val state: StateFlow<InsightsState> = combine(
        Dates.days(),
        selection,
        container.expenseRepository.all,
        combine(container.categoryRepository.categories, container.memberRepository.members, ::Pair),
        combine(container.gamificationRepository.unlockedIds, container.settingsRepository.settings, ::Pair)
    ) { today, (period, offset), all, (categories, members), (unlocked, settings) ->
        val range = period.range(today, offset)
        val previousRange = period.range(today, offset - 1)
        val current = all.filter { LocalDate.ofEpochDay(it.expense.epochDay) in range }
        val previous = all.filter { LocalDate.ofEpochDay(it.expense.epochDay) in previousRange }
        val isCurrent = today in range
        val map = categories.byId()
        val previousByCategory = previous.groupBy { it.expense.category }.mapValues { (_, list) -> list.sumOf { it.expense.amount } }
        val total = current.sumOf { it.expense.amount }
        val days = (if (isCurrent) today else range.endInclusive).toEpochDay() - range.start.toEpochDay() + 1
        val trend = trendOf(period, range, today, isCurrent, current)
        InsightsState(
            unlocked = Features.INSIGHTS in unlocked,
            symbol = settings.currencySymbol,
            period = period,
            title = when (period) {
                InsightPeriod.WEEK -> "${Dates.shortLabel(range.start.toEpochDay())} – ${Dates.shortLabel(range.endInclusive.toEpochDay())}"
                InsightPeriod.MONTH -> Dates.monthLabel(YearMonth.from(range.start))
                InsightPeriod.YEAR -> range.start.year.toString()
            },
            isCurrent = isCurrent,
            count = current.size,
            total = total,
            previousTotal = previous.sumOf { it.expense.amount },
            dailyAverage = current.filter { it.expense.epochDay <= today.toEpochDay() }.sumOf { it.expense.amount } / days,
            biggest = current.maxByOrNull { it.expense.amount },
            trend = trend.values,
            trendLabels = trend.labels,
            trendSlots = trend.slots,
            trendHighlight = trend.highlight,
            categories = current.groupBy { it.expense.category }
                .map { (id, list) -> CategoryShare(map.resolve(id), list.sumOf { it.expense.amount }, previousByCategory[id] ?: 0.0) }
                .filter { it.total > 0 }
                .sortedByDescending { it.total },
            modes = PaymentMode.entries
                .map { mode -> mode to current.filter { it.expense.paymentMode == mode }.sumOf { it.expense.amount } }
                .filter { it.second > 0 }
                .sortedByDescending { it.second },
            payers = current.groupBy { it.expense.paidByMemberId }
                .mapNotNull { (id, list) -> members.firstOrNull { it.id == id }?.let { PayerShare(it, list.sumOf { e -> e.expense.amount }) } }
                .filter { it.total > 0 }
                .sortedByDescending { it.total }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsState())

    fun selectPeriod(period: InsightPeriod) { selection.value = period to 0 }

    fun shift(by: Int) = selection.update { (period, offset) -> period to (offset + by).coerceAtMost(0) }

    private class Trend(val values: List<Double>, val labels: List<String>, val slots: Int, val highlight: Int)

    private fun InsightPeriod.range(today: LocalDate, offset: Int): ClosedRange<LocalDate> = when (this) {
        InsightPeriod.WEEK -> today.with(DayOfWeek.MONDAY).plusWeeks(offset.toLong()).let { it..it.plusDays(6) }
        InsightPeriod.MONTH -> YearMonth.from(today).plusMonths(offset.toLong()).let { it.atDay(1)..it.atEndOfMonth() }
        InsightPeriod.YEAR -> today.withDayOfYear(1).plusYears(offset.toLong()).let { it..it.withDayOfYear(it.lengthOfYear()) }
    }

    private fun trendOf(period: InsightPeriod, range: ClosedRange<LocalDate>, today: LocalDate, isCurrent: Boolean, current: List<ExpenseWithPayer>): Trend {
        val byDay = current.groupBy { it.expense.epochDay }.mapValues { (_, list) -> list.sumOf { it.expense.amount }.coerceAtLeast(0.0) }
        return when (period) {
            InsightPeriod.WEEK -> {
                val days = (0L..6L).map { range.start.plusDays(it) }
                val values = days.map { byDay[it.toEpochDay()] ?: 0.0 }
                val highlight = if (isCurrent) days.indexOf(today) else values.indices.maxByOrNull { values[it] } ?: 0
                Trend(values, days.map { it.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }, values.size, highlight)
            }
            InsightPeriod.MONTH -> {
                val month = YearMonth.from(range.start)
                val lastDay = if (isCurrent) today.dayOfMonth else month.lengthOfMonth()
                val values = (1..lastDay).map { byDay[month.atDay(it).toEpochDay()] ?: 0.0 }
                val labels = listOf(1, 8, 15, 22, month.lengthOfMonth()).map { Dates.shortLabel(month.atDay(it).toEpochDay()) }
                Trend(values, labels, month.lengthOfMonth(), values.lastIndex)
            }
            InsightPeriod.YEAR -> {
                val byMonth = current.groupBy { LocalDate.ofEpochDay(it.expense.epochDay).monthValue }.mapValues { (_, list) -> list.sumOf { it.expense.amount }.coerceAtLeast(0.0) }
                val values = (1..12).map { byMonth[it] ?: 0.0 }
                val highlight = if (isCurrent) today.monthValue - 1 else values.indices.maxByOrNull { values[it] } ?: 0
                Trend(values, (1..12).map { YearMonth.of(range.start.year, it).month.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }, 12, highlight)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(onBack: () -> Unit) {
    val vm = appViewModel { InsightsViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val scheme = MaterialTheme.colorScheme
    val symbol = state.symbol
    val dayTemplate = stringResource(R.string.day_n)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.insights)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        }
    ) { padding ->
        if (!state.unlocked) {
            EmptyState("🔒", stringResource(R.string.insights_locked_title), stringResource(R.string.insights_locked_body), Modifier.padding(padding))
            return@Scaffold
        }
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            SingleChoiceSegmentedButtonRow(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                InsightPeriod.entries.forEachIndexed { i, p ->
                    SegmentedButton(
                        selected = state.period == p,
                        onClick = { vm.selectPeriod(p) },
                        shape = SegmentedButtonDefaults.itemShape(i, InsightPeriod.entries.size)
                    ) { Text(stringResource(p.labelRes), maxLines = 1) }
                }
            }

            HqCard(Modifier.padding(horizontal = 20.dp, vertical = 12.dp).fillMaxWidth(), padding = 6) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { vm.shift(-1) }) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous_period)) }
                    AnimatedContent(targetState = state.title, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "period", modifier = Modifier.weight(1f)) { title ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(stringResource(R.string.n_transactions, state.count), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                    IconButton(onClick = { vm.shift(1) }, enabled = !state.isCurrent) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = stringResource(R.string.next_period)) }
                }
            }

            SpendHero(state)
            val categoryTotal = state.categories.sumOf { it.total }

            HqCard(Modifier.padding(horizontal = 20.dp, vertical = 12.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✨", style = MaterialTheme.typography.titleLarge)
                    val top = state.categories.firstOrNull()
                    Text(
                        if (top == null) stringResource(R.string.insight_quiet)
                        else stringResource(R.string.insight_top_category, top.category.label(), (top.total / categoryTotal * 100).roundToInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            if (categoryTotal <= 0) return@Column

            SectionHeader(stringResource(R.string.spending_trend))
            HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                if (state.period == InsightPeriod.MONTH) {
                    AreaChart(state.trend, state.trendLabels, valueLabel = { it.money(symbol) }, pointLabel = { day -> dayTemplate.format(day + 1) }, slots = state.trendSlots)
                } else {
                    BarChart(state.trend, state.trendLabels, valueLabel = { it.money(symbol) }, highlightIndex = state.trendHighlight)
                }
            }

            SectionHeader(stringResource(R.string.categories))
            HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    DonutChart(state.categories.map { CategoryAmount(it.category, it.total) }, valueLabel = { it.money(symbol) }, centerLabel = categoryTotal.money(symbol))
                }
                Spacer(Modifier.height(12.dp))
                state.categories.forEach { CategoryShareRow(it, categoryTotal, symbol) }
            }

            SectionHeader(stringResource(R.string.payment_modes))
            HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                val modeTotal = state.modes.sumOf { it.second }
                ShareBar(state.modes.map { (mode, amount) -> mode.color to amount })
                state.modes.forEach { (mode, amount) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                        Box(Modifier.size(10.dp).background(mode.color, CircleShape))
                        Text(stringResource(mode.labelRes), Modifier.padding(start = 10.dp).weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                        Text("${(amount / modeTotal * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant, modifier = Modifier.padding(end = 12.dp))
                        Text(amount.money(symbol), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                    }
                }
            }

            if (state.payers.size > 1) {
                SectionHeader(stringResource(R.string.who_paid))
                val payerTotal = state.payers.sumOf { it.total }
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    state.payers.forEach { p ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            AvatarView(p.member.avatar(), size = 34.dp)
                            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                Text(p.member.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                LinearProgressIndicator(progress = { (p.total / payerTotal).toFloat() }, modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 2.dp), color = scheme.primary, trackColor = scheme.surfaceContainerHigh)
                            }
                            Text(p.total.money(symbol), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpendHero(state: InsightsState) {
    val scheme = MaterialTheme.colorScheme
    Column(
        Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(scheme.primary, scheme.tertiary)), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(stringResource(R.string.spending), style = MaterialTheme.typography.labelLarge, color = scheme.onPrimary.copy(alpha = 0.85f))
        AnimatedMoney(state.total, state.symbol, MaterialTheme.typography.displaySmall, scheme.onPrimary)
        state.change?.let { change ->
            Text(
                stringResource(if (change > 0) R.string.change_up else R.string.change_down, "%.0f".format(abs(change))),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.onPrimary,
                modifier = Modifier.padding(top = 6.dp).background(scheme.onPrimary.copy(alpha = 0.18f), RoundedCornerShape(50)).padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HeroStat(stringResource(R.string.daily_average), state.dailyAverage.money(state.symbol), Modifier.weight(1f))
            HeroStat(stringResource(R.string.transactions), state.count.toString(), Modifier.weight(1f))
            HeroStat(stringResource(R.string.biggest), state.biggest?.expense?.amount?.money(state.symbol) ?: "—", Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String, modifier: Modifier) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Column(modifier.background(onPrimary.copy(alpha = 0.14f), RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = onPrimary.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, style = MaterialTheme.typography.titleSmall, color = onPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CategoryShareRow(share: CategoryShare, total: Double, symbol: String) {
    val scheme = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
        CategoryIcon(share.category, size = 36)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(share.category.label(), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            LinearProgressIndicator(
                progress = { (share.total / total).toFloat() },
                modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 3.dp),
                color = share.category.color,
                trackColor = scheme.surfaceContainerHigh
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(share.total.money(symbol), style = MaterialTheme.typography.titleSmall, maxLines = 1)
            if (share.previous > 0) {
                val change = (share.total - share.previous) / share.previous * 100
                Text(
                    "${if (change > 0) "▲" else "▼"} ${"%.0f".format(abs(change))}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (change > 0) scheme.error else scheme.primary,
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
        }
    }
}
