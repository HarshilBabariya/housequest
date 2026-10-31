package com.homequest.app.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.data.repository.avatar
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AnimatedCount
import com.homequest.app.ui.components.AnimatedMoney
import com.homequest.app.domain.Mood
import com.homequest.app.ui.components.AvatarView
import com.homequest.app.ui.components.BarChart
import com.homequest.app.ui.components.DonutChart
import com.homequest.app.ui.components.EmptyState
import com.homequest.app.ui.components.ExpenseRow
import com.homequest.app.ui.components.ProgressRing
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.components.moodLine
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.YearMonth

@Composable
fun DashboardScreen(
    onAddExpense: () -> Unit,
    onOpenExpense: (Long) -> Unit,
    onOpenExpenses: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenBudgets: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val vm = appViewModel { DashboardViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val symbol = state.settings.currencySymbol

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpense,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add expense") }
            )
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 96.dp)) {
            item {
                HeroCard(state, onOpenProfile)
            }
            item {
                BudgetCard(state, symbol, onOpenBudgets)
            }
            item {
                SectionHeader("This week")
                Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    BarChart(state.weekValues, state.weekLabels, Modifier.padding(16.dp))
                }
            }
            item {
                SectionHeader(Dates.monthLabel(YearMonth.now()), action = if (state.insightsUnlocked) "Insights" else null, onAction = onOpenInsights)
                Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    if (state.categoryTotals.isEmpty()) {
                        EmptyState("🧾", "Nothing logged yet", "Add your first expense to start earning points.")
                    } else {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
    val bubbleColor by animateColorAsState(
        when (mood) {
            Mood.ANGRY -> Color(0xFFFFE5E0)
            Mood.WORRIED -> Color(0xFFFFF3D6)
            else -> scheme.onPrimary
        },
        label = "bubble"
    )
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(scheme.primary, scheme.secondary)))
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
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
                    Text(
                        "${state.profile.lifetimePoints} / ${state.level.nextLevelAt} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimary.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
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
                        .background(scheme.onPrimary.copy(alpha = 0.15f), CircleShape)
                        .padding(6.dp)
                        .clickable(onClick = onOpenProfile),
                    contentAlignment = Alignment.Center
                ) {
                    state.member?.let { AvatarView(it.avatar(), size = 110.dp, animated = true, mood = mood, waveTrigger = wave) }
                }
            }
            AnimatedVisibility(visible = state.member != null, enter = fadeIn() + slideInVertically { it / 2 }) {
                Row(
                    Modifier
                        .padding(top = 14.dp)
                        .fillMaxWidth()
                        .background(bubbleColor, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        moodLine(mood, state.member?.name.orEmpty(), LocalTime.now().hour),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2B2320),
                        modifier = Modifier.weight(1f)
                    )
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

@Composable
private fun BudgetCard(state: DashboardState, symbol: String, onOpenBudgets: () -> Unit) {
    SectionHeader("Monthly budget", action = "Manage", onAction = onOpenBudgets)
    Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val over = state.budgetProgress > 1f
            ProgressRing(
                progress = state.budgetProgress,
                color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            ) {
                Text("${(state.budgetProgress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium)
            }
            Column(Modifier.padding(start = 20.dp)) {
                Text("Spent this month", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AnimatedMoney(state.monthTotal, symbol, MaterialTheme.typography.headlineMedium)
                Text(
                    if (state.settings.monthlyBudget > 0) {
                        val left = state.settings.monthlyBudget - state.monthTotal
                        if (left >= 0) "${left.money(symbol)} left of ${state.settings.monthlyBudget.money(symbol)}" else "${(-left).money(symbol)} over budget"
                    } else "Set a budget to earn the Budget Boss badge",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (over) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
