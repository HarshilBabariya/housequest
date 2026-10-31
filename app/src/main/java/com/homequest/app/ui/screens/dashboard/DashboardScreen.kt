package com.homequest.app.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.data.repository.avatar
import com.homequest.app.domain.Mood
import com.homequest.app.notifications.nextOccurrence
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AnimatedCount
import com.homequest.app.ui.components.AnimatedMoney
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
import com.homequest.app.ui.components.moodLine
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Composable
fun DashboardScreen(
    onAddExpense: () -> Unit,
    onOpenExpense: (Long) -> Unit,
    onOpenExpenses: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenGroups: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val vm = appViewModel { DashboardViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val symbol = state.settings.currencySymbol
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddExpense, icon = { Icon(Icons.Filled.Add, contentDescription = null) }, text = { Text("Add expense") })
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 96.dp)) {
            item { HeroCard(state, onOpenProfile) }

            item {
                val change = state.monthChangePercent
                Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("Today", state.todayTotal.money(symbol), Modifier.weight(1f), hint = Dates.shortLabel(Dates.today().toEpochDay()), emoji = "☀️")
                    StatTile(
                        "vs last month",
                        change?.let { "${if (it >= 0) "+" else ""}${"%.0f".format(it)}%" } ?: "—",
                        Modifier.weight(1f),
                        hint = when {
                            change == null -> "No data for last month"
                            change <= 0 -> "Spending less"
                            else -> "Spending more"
                        },
                        emoji = if (change != null && change > 0) "📈" else "📉",
                        accent = if (change != null && change > 0) scheme.error else scheme.primary
                    )
                }
                Row(Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        "Group balance",
                        when {
                            state.groupNet > 0.005 -> state.groupNet.money(symbol)
                            state.groupNet < -0.005 -> (-state.groupNet).money(symbol)
                            else -> "Settled"
                        },
                        Modifier.weight(1f),
                        hint = when {
                            state.groupNet > 0.005 -> "Owed to you"
                            state.groupNet < -0.005 -> "You owe"
                            else -> "Nothing pending"
                        },
                        emoji = "🤝",
                        accent = if (state.groupNet < -0.005) scheme.error else scheme.tertiary,
                        onClick = onOpenGroups
                    )
                    val top = state.categoryTotals.firstOrNull()
                    StatTile(
                        "Top category",
                        top?.category?.label ?: "—",
                        Modifier.weight(1f),
                        hint = top?.let { "${it.total.money(symbol)} this month" } ?: "Nothing logged yet",
                        emoji = "🏷️",
                        accent = top?.category?.color ?: scheme.primary,
                        onClick = onOpenExpenses
                    )
                }
            }

            item {
                SectionHeader("Monthly budget", action = "Manage", onAction = onOpenBudgets)
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val over = state.budgetProgress > 1f
                        ProgressRing(progress = state.budgetProgress, color = if (over) scheme.error else scheme.primary) {
                            Text("${(state.budgetProgress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium)
                        }
                        Column(Modifier.padding(start = 20.dp)) {
                            Text("Spent in ${Dates.monthLabel(YearMonth.now())}", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                            AnimatedMoney(state.monthTotal, symbol, MaterialTheme.typography.headlineMedium)
                            Text(
                                if (state.settings.monthlyBudget > 0) {
                                    val left = state.settings.monthlyBudget - state.monthTotal
                                    if (left >= 0) "${left.money(symbol)} left of ${state.settings.monthlyBudget.money(symbol)}" else "${(-left).money(symbol)} over budget"
                                } else "Set a budget to earn the Budget Boss badge",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (over) scheme.error else scheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader("Last 7 days")
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) { BarChart(state.weekValues, state.weekLabels) }
            }

            item {
                SectionHeader("By category", action = if (state.insightsUnlocked) "Insights" else null, onAction = onOpenInsights)
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    if (state.categoryTotals.isEmpty()) {
                        EmptyState("🧾", "Nothing logged yet", "Add your first expense to start earning points.")
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DonutChart(state.categoryTotals, centerLabel = state.monthTotal.money(symbol))
                            Column(Modifier.padding(start = 16.dp)) {
                                state.categoryTotals.take(5).forEach { t ->
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                                        Box(Modifier.size(10.dp).background(t.category.color, CircleShape))
                                        Text(t.category.label, Modifier.padding(start = 8.dp).weight(1f), style = MaterialTheme.typography.bodySmall)
                                        Text(t.total.money(symbol), style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (state.payers.isNotEmpty()) {
                item {
                    SectionHeader("Who paid this month")
                    HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                        val max = state.payers.first().total
                        state.payers.forEach { p ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                AvatarView(p.member.avatar(), size = 34.dp)
                                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                    Text(p.member.name, style = MaterialTheme.typography.bodyMedium)
                                    LinearProgressIndicator(progress = { (p.total / max).toFloat() }, modifier = Modifier.fillMaxWidth().height(6.dp).padding(top = 2.dp), color = scheme.primary, trackColor = scheme.surfaceContainerHigh)
                                }
                                Text(p.total.money(symbol), style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader("Upcoming bills", action = "All", onAction = onOpenReminders)
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    if (state.upcomingBills.isEmpty()) {
                        Text("No bills scheduled. Add rent, EMIs or subscriptions under Reminders & bills.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                    } else {
                        state.upcomingBills.forEach { r ->
                            val next = r.nextOccurrence()
                            val days = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(), next.toLocalDate())
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                                if (r.category != null) CategoryIcon(r.category, 36) else Box(Modifier.size(36.dp).background(scheme.primaryContainer, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Text("⏰") }
                                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                    Text(r.title, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        when (days) { 0L -> "Due today"; 1L -> "Due tomorrow"; else -> "In $days days · ${Dates.shortLabel(next.toLocalDate().toEpochDay())}" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (days <= 1) scheme.error else scheme.onSurfaceVariant
                                    )
                                }
                                Text((r.amount ?: 0.0).money(symbol), style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Recent", action = "See all", onAction = onOpenExpenses) }
            items(state.recent, key = { it.expense.id }) { ExpenseRow(it, symbol, onClick = { onOpenExpense(it.expense.id) }) }
        }
    }
}

@Composable
private fun HeroCard(state: DashboardState, onOpenProfile: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val mood = state.mood
    var wave by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.member?.id) { if (state.member != null) { delay(400); wave++ } }
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(scheme.primary, scheme.secondary)))
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Level ${state.level.level}", style = MaterialTheme.typography.labelLarge, color = scheme.onPrimary.copy(alpha = 0.85f))
                    Text(state.level.title, style = MaterialTheme.typography.headlineMedium, color = scheme.onPrimary)
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { state.level.progress(state.profile.lifetimePoints) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = scheme.onPrimary,
                        trackColor = scheme.onPrimary.copy(alpha = 0.3f)
                    )
                    Text("${state.profile.lifetimePoints} / ${state.level.nextLevelAt} XP", style = MaterialTheme.typography.labelSmall, color = scheme.onPrimary.copy(alpha = 0.85f), modifier = Modifier.padding(top = 4.dp))
                    Spacer(Modifier.height(12.dp))
                    Row {
                        StatPill("⭐") { AnimatedCount(state.profile.points, MaterialTheme.typography.titleMedium, scheme.onPrimary) }
                        Spacer(Modifier.width(10.dp))
                        StatPill("🔥") { Text("${state.profile.streakDays}d", style = MaterialTheme.typography.titleMedium, color = scheme.onPrimary) }
                    }
                }
                Box(
                    Modifier
                        .padding(start = 12.dp)
                        .background(scheme.onPrimary.copy(alpha = 0.18f), CircleShape)
                        .padding(6.dp)
                        .clickable(onClick = onOpenProfile),
                    contentAlignment = Alignment.Center
                ) {
                    state.member?.let { AvatarView(it.avatar(), size = 116.dp, animated = true, mood = mood, waveTrigger = wave) }
                }
            }
            AnimatedVisibility(visible = state.member != null, enter = fadeIn() + slideInVertically { it / 2 }) {
                Row(
                    Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .background(scheme.surfaceContainerLowest, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        when (mood) { Mood.ANGRY -> "😤"; Mood.WORRIED -> "😟"; Mood.NEUTRAL -> "🙂"; Mood.HAPPY -> "😄" },
                        fontSize = 22.sp,
                        modifier = Modifier.padding(end = 10.dp)
                    )
                    Text(moodLine(mood, state.member?.name.orEmpty(), LocalTime.now().hour), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurface, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatPill(emoji: String, value: @Composable () -> Unit) {
    Row(
        Modifier.background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        value()
    }
}
