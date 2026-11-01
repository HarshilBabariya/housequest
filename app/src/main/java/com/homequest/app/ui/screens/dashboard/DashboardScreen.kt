package com.homequest.app.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.R
import com.homequest.app.data.repository.avatar
import com.homequest.app.data.repository.resolve
import com.homequest.app.notifications.nextOccurrence
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AnimatedCount
import com.homequest.app.ui.components.AnimatedMoney
import com.homequest.app.ui.components.AreaChart
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.BarChart
import com.homequest.app.ui.components.CategoryIcon
import com.homequest.app.ui.components.DonutChart
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.ExpenseRow
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.ProgressRing
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.components.StatTile
import com.homequest.app.ui.components.label
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    onAddExpense: () -> Unit,
    onVoiceExpense: () -> Unit,
    onOpenExpense: (Long) -> Unit,
    onOpenExpenses: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenGroups: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenRewards: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val vm = appViewModel { DashboardViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val symbol = state.settings.currencySymbol
    val scheme = MaterialTheme.colorScheme

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
            item { Header(state, onOpenProfile, onVoiceExpense, onOpenRewards) }

            item {
                SectionHeader(stringResource(R.string.cash_flow), action = stringResource(R.string.manage), onAction = onOpenBudgets)
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val over = state.budgetProgress > 1f
                        ProgressRing(progress = state.budgetProgress, color = if (over) scheme.error else scheme.primary) {
                            Text(if (state.settings.monthlyBudget > 0) "${(state.budgetProgress * 100).toInt()}%" else "—", style = MaterialTheme.typography.titleMedium)
                        }
                        Column(Modifier.padding(start = 20.dp).weight(1f)) {
                            Text(stringResource(R.string.spent_in, Dates.monthLabel(YearMonth.from(state.today))), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            AnimatedMoney(state.monthTotal, symbol, MaterialTheme.typography.headlineMedium)
                            Text(
                                if (state.settings.monthlyBudget > 0) {
                                    val left = state.settings.monthlyBudget - state.monthTotal
                                    if (left >= 0) stringResource(R.string.left_of, left.money(symbol), state.settings.monthlyBudget.money(symbol)) else stringResource(R.string.over_budget_by, (-left).money(symbol))
                                } else stringResource(R.string.set_budget_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (over) scheme.error else scheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            item {
                val change = state.monthChangePercent
                Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(stringResource(R.string.today), state.todayTotal.money(symbol), Modifier.weight(1f), hint = Dates.shortLabel(state.today.toEpochDay()), emoji = "☀️", onClick = onAddExpense)
                    StatTile(
                        stringResource(R.string.vs_last_month),
                        change?.let { "${if (it >= 0) "+" else ""}${"%.0f".format(it)}%" } ?: "—",
                        Modifier.weight(1f),
                        hint = stringResource(if (change == null) R.string.no_data_yet else if (change <= 0) R.string.spending_less else R.string.spending_more),
                        emoji = if (change != null && change > 0) "📈" else "📉",
                        accent = if (change != null && change > 0) scheme.error else scheme.primary
                    )
                }
                Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        stringResource(R.string.group_balance),
                        when {
                            state.groupNet > 0.005 -> state.groupNet.money(symbol)
                            state.groupNet < -0.005 -> (-state.groupNet).money(symbol)
                            else -> stringResource(R.string.settled)
                        },
                        Modifier.weight(1f),
                        hint = stringResource(when { state.groupNet > 0.005 -> R.string.owed_to_you; state.groupNet < -0.005 -> R.string.you_owe; else -> R.string.nothing_pending }),
                        emoji = "🤝",
                        accent = if (state.groupNet < -0.005) scheme.error else scheme.tertiary,
                        onClick = onOpenGroups
                    )
                    val top = state.categoryTotals.firstOrNull()
                    StatTile(
                        stringResource(R.string.top_category),
                        top?.category?.label() ?: "—",
                        Modifier.weight(1f),
                        hint = top?.let { stringResource(R.string.amount_this_month, it.total.money(symbol)) } ?: stringResource(R.string.nothing_logged),
                        emoji = "🏷️",
                        accent = top?.category?.color ?: scheme.primary,
                        onClick = onOpenExpenses
                    )
                }
            }

            if (state.monthTotal > 0) {
                item {
                    val month = YearMonth.from(state.today)
                    SectionHeader(stringResource(R.string.month_so_far))
                    HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                        Text(stringResource(R.string.on_pace_for), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                        AnimatedMoney(state.monthProjection, symbol, MaterialTheme.typography.titleLarge, if (state.settings.monthlyBudget > 0 && state.monthProjection > state.settings.monthlyBudget) scheme.error else scheme.onSurface)
                        if (state.settings.monthlyBudget > 0) {
                            Text(stringResource(R.string.pace_hint), style = MaterialTheme.typography.bodySmall, color = scheme.tertiary, modifier = Modifier.padding(bottom = 8.dp))
                        }
                        AreaChart(
                            values = state.monthCumulative,
                            axisLabels = listOf(1, 8, 15, 22, month.lengthOfMonth()).map { Dates.shortLabel(month.atDay(it).toEpochDay()) },
                            slots = month.lengthOfMonth(),
                            pace = state.settings.monthlyBudget.takeIf { it > 0 },
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            item {
                SectionHeader(stringResource(R.string.last_7_days))
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    val weekTotal = state.weekValues.sum()
                    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(bottom = 12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.spent_this_week), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant, maxLines = 1)
                            AnimatedMoney(weekTotal, symbol, MaterialTheme.typography.titleLarge)
                        }
                        Text(stringResource(R.string.avg_per_day, (weekTotal / 7).money(symbol)), style = MaterialTheme.typography.labelMedium, color = scheme.primary, maxLines = 1)
                    }
                    BarChart(state.weekValues, state.weekLabels, highlightLabel = state.todayTotal.money(symbol))
                }
            }

            item {
                SectionHeader(stringResource(R.string.by_category), action = if (state.insightsUnlocked) stringResource(R.string.insights) else null, onAction = onOpenInsights)
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    if (state.categoryTotals.isEmpty()) {
                        EmptyState("🧾", stringResource(R.string.nothing_logged), stringResource(R.string.first_expense_hint))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DonutChart(state.categoryTotals, centerLabel = state.monthTotal.money(symbol))
                            Column(Modifier.padding(start = 16.dp).weight(1f)) {
                                state.categoryTotals.take(5).forEach { t ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                                        Box(Modifier.size(10.dp).background(t.category.color, CircleShape))
                                        Text(t.category.label(), Modifier.padding(start = 8.dp).weight(1f), style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(t.total.money(symbol), style = MaterialTheme.typography.labelMedium, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (state.payers.isNotEmpty()) {
                item {
                    SectionHeader(stringResource(R.string.who_paid))
                    HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                        val max = state.payers.first().total
                        state.payers.forEach { p ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                AvatarView(p.member.avatar(), size = 34.dp)
                                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                    Text(p.member.name, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    LinearProgressIndicator(progress = { (p.total / max).toFloat() }, modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 2.dp), color = scheme.primary, trackColor = scheme.surfaceContainerHigh)
                                }
                                Text(p.total.money(symbol), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(stringResource(R.string.upcoming_bills), action = stringResource(R.string.all), onAction = onOpenReminders)
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    if (state.upcomingBills.isEmpty()) {
                        Text(stringResource(R.string.no_bills_hint), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                    } else {
                        state.upcomingBills.forEach { r ->
                            val next = r.nextOccurrence()
                            val days = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), next.toLocalDate())
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                                val category = r.category?.let { state.categories.resolve(it) }
                                if (category != null) CategoryIcon(category, 36) else Box(Modifier.size(36.dp).background(scheme.primaryContainer, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text("⏰") }
                                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                    Text(r.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        when (days) { 0L -> stringResource(R.string.due_today); 1L -> stringResource(R.string.due_tomorrow); else -> stringResource(R.string.due_in_days, days, Dates.shortLabel(next.toLocalDate().toEpochDay())) },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (days <= 1) scheme.error else scheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text((r.amount ?: 0.0).money(symbol), style = MaterialTheme.typography.titleSmall, maxLines = 1)
                            }
                        }
                    }
                }
            }

            item { SectionHeader(stringResource(R.string.recent_transactions), action = stringResource(R.string.see_all), onAction = onOpenExpenses) }
            if (state.recent.isEmpty()) item { EmptyState("✍️", stringResource(R.string.nothing_logged), stringResource(R.string.first_expense_hint)) }
            items(state.recent, key = { "exp_${it.expense.id}" }) { ExpenseRow(it, state.categories.resolve(it.expense.category), symbol, onClick = { onOpenExpense(it.expense.id) }) }
        }
    }
}

@Composable
private fun Header(state: DashboardState, onOpenProfile: () -> Unit, onVoice: () -> Unit, onOpenRewards: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val mood = state.mood
    var wave by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.member?.id) { if (state.member != null) { delay(400); wave++ } }
    val hour = LocalTime.now().hour
    val greeting = stringResource(when (hour) { in 5..11 -> R.string.good_morning; in 12..16 -> R.string.good_afternoon; in 17..21 -> R.string.good_evening; else -> R.string.still_up })

    Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.border(2.dp, scheme.primary.copy(alpha = 0.35f), CircleShape).padding(3.dp).clip(CircleShape).clickable(onClick = onOpenProfile)) {
                state.member?.let { AvatarView(it.avatar(), size = 64.dp, animated = true, mood = mood, waveTrigger = wave) }
            }
            Column(Modifier.weight(1f).padding(start = 14.dp)) {
                Text(state.member?.name.orEmpty(), style = MaterialTheme.typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(
                    Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(scheme.primaryContainer)
                        .clickable(onClick = onOpenRewards)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.level_n, state.level.level), style = MaterialTheme.typography.labelMedium, color = scheme.onPrimaryContainer, maxLines = 1)
                    Text(" · ⭐ ", style = MaterialTheme.typography.labelMedium, color = scheme.onPrimaryContainer)
                    AnimatedCount(state.profile.points, MaterialTheme.typography.labelMedium, scheme.onPrimaryContainer)
                    Text(" · 🔥 ${state.profile.streakDays}", style = MaterialTheme.typography.labelMedium, color = scheme.onPrimaryContainer, maxLines = 1)
                }
            }
            IconButton(onClick = onVoice, modifier = Modifier.size(46.dp).border(1.dp, scheme.tertiary.copy(alpha = 0.5f), CircleShape).background(scheme.tertiaryContainer.copy(alpha = 0.5f), CircleShape)) {
                Icon(Icons.Filled.Mic, contentDescription = stringResource(R.string.voice_title), tint = scheme.tertiary)
            }
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { state.level.progress(state.profile.lifetimePoints) },
            modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 8.dp),
            color = scheme.primary,
            trackColor = scheme.surfaceContainerHigh
        )
        AnimatedVisibility(visible = state.member != null, enter = fadeIn() + slideInVertically { it / 2 }) {
            Row(
                Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .background(scheme.surfaceContainerLowest, RoundedCornerShape(topStart = 6.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                    .border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(topStart = 6.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(mood.emoji, fontSize = 22.sp, modifier = Modifier.padding(end = 10.dp))
                Text(
                    "$greeting ${stringResource(mood.lineRes, state.member?.name.orEmpty())}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
