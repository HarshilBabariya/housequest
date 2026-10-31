package com.homequest.app.ui.screens.rewards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.domain.RewardItem
import com.homequest.app.domain.RewardKind
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AnimatedCount
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.theme.Palettes
import com.homequest.app.util.Dates
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RewardsScreen(onOpenAvatar: (Long) -> Unit) {
    val vm = appViewModel { RewardsViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var selectedBadge by remember { mutableStateOf<BadgeStatus?>(null) }

    LaunchedEffect(state.lastMessage) {
        state.lastMessage?.let { snackbar.showSnackbar(it); vm.clearMessage() }
    }

    selectedBadge?.let { BadgeDetailSheet(it, onDismiss = { selectedBadge = null }) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Rewards") }) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 32.dp)) {
            item {
                val scheme = MaterialTheme.colorScheme
                Box(
                    Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(scheme.primary, scheme.tertiary)), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Points to spend", style = MaterialTheme.typography.labelLarge, color = scheme.onPrimary.copy(alpha = 0.9f))
                            AnimatedCount(state.profile.points, MaterialTheme.typography.displayMedium, scheme.onPrimary)
                            Text("Lifetime ${state.profile.lifetimePoints} XP · Level ${state.level.level} ${state.level.title}", style = MaterialTheme.typography.bodySmall, color = scheme.onPrimary.copy(alpha = 0.9f))
                        }
                        Text("⭐", fontSize = 56.sp)
                    }
                }
            }
            item {
                val earnedCount = state.badges.count { it.earned }
                SectionHeader("Badges · $earnedCount/${state.badges.size}")
                FlowRow(
                    Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    maxItemsInEachRow = 3,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.badges.forEach { status -> BadgeTile(status, Modifier.weight(1f)) { selectedBadge = status } }
                }
            }
            item {
                SectionHeader("Shop")
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(RewardKind.entries) { k -> FilterChip(selected = state.kind == k, onClick = { vm.selectKind(k) }, label = { Text(k.label) }) }
                }
                Spacer(Modifier.height(8.dp))
            }
            items(state.items, key = { it.id }) { item ->
                RewardRow(
                    item = item,
                    unlocked = item.id in state.unlocked,
                    active = item.kind == RewardKind.THEME && item.id == state.activeThemeId,
                    affordable = state.profile.points >= item.cost,
                    onRedeem = { vm.redeem(item) },
                    onUse = {
                        when (item.kind) {
                            RewardKind.THEME -> vm.applyTheme(item.id)
                            RewardKind.OUTFIT, RewardKind.HAIR, RewardKind.ACCESSORY -> onOpenAvatar(state.ownerId)
                            RewardKind.FEATURE -> Unit
                        }
                    }
                )
            }
            item { SectionHeader("Points history") }
            items(state.ledger, key = { it.id }) { entry ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Text(entry.reason, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        (if (entry.delta > 0) "+" else "") + entry.delta,
                        color = if (entry.delta > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeTile(status: BadgeStatus, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val badge = status.badge
    Column(
        modifier
            .background(scheme.surfaceContainerLowest, RoundedCornerShape(18.dp))
            .border(1.dp, if (status.earned) scheme.primary.copy(alpha = 0.35f) else scheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(54.dp)
                    .background(
                        if (status.earned) Brush.linearGradient(listOf(scheme.primary, scheme.secondary)) else Brush.linearGradient(listOf(scheme.surfaceContainerHigh, scheme.surfaceContainerHigh)),
                        CircleShape
                    )
                    .padding(3.dp)
                    .background(scheme.surfaceContainerLowest, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(badge.emoji, fontSize = 24.sp, modifier = Modifier.alpha(if (status.earned) 1f else 0.35f))
            }
            if (!status.earned) {
                Box(Modifier.align(Alignment.BottomEnd).size(20.dp).background(scheme.surfaceContainerHigh, CircleShape).border(1.dp, scheme.surfaceContainerLowest, CircleShape), contentAlignment = Alignment.Center) {
                    Text("🔒", fontSize = 10.sp)
                }
            }
        }
        Text(badge.name, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp), maxLines = 1, color = if (status.earned) scheme.onSurface else scheme.onSurfaceVariant)
        if (status.earned) {
            Text("+${badge.bonusPoints} pts", style = MaterialTheme.typography.labelSmall, color = scheme.primary)
        } else {
            LinearProgressIndicator(
                progress = { status.fraction },
                modifier = Modifier.padding(top = 6.dp, start = 10.dp, end = 10.dp).fillMaxWidth().height(4.dp),
                color = scheme.primary,
                trackColor = scheme.surfaceContainerHigh
            )
            Text("${status.progress.coerceAtMost(badge.target)}/${badge.target}", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BadgeDetailSheet(status: BadgeStatus, onDismiss: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val badge = status.badge
    val pop = remember { Animatable(0f) }
    LaunchedEffect(badge.id) { pop.snapTo(0f); pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
    val spin by rememberInfiniteTransition(label = "ring").animateFloat(0f, 360f, infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "spin")
    var revealed by remember(badge.id) { mutableStateOf(0) }
    LaunchedEffect(badge.id) { badge.howTo.indices.forEach { delay(140); revealed = it + 1 } }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(132.dp).scale(0.6f + 0.4f * pop.value), contentAlignment = Alignment.Center) {
                if (status.earned) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .rotate(spin)
                            .background(Brush.sweepGradient(listOf(scheme.primary, scheme.secondary, scheme.tertiary, scheme.primary)), CircleShape)
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(scheme.surfaceContainerHigh, CircleShape))
                }
                Box(Modifier.size(116.dp).background(scheme.surfaceContainerLowest, CircleShape), contentAlignment = Alignment.Center) {
                    Text(badge.emoji, fontSize = 56.sp, modifier = Modifier.alpha(if (status.earned) 1f else 0.45f))
                }
            }
            Text(badge.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 14.dp))
            Text(badge.description, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            Text(
                if (status.earned) "Earned on ${Dates.longLabel(Instant.ofEpochMilli(status.earnedAt!!).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay())} · +${badge.bonusPoints} points"
                else "Reward: +${badge.bonusPoints} points",
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
            if (!status.earned) {
                LinearProgressIndicator(progress = { status.fraction }, modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 2.dp), color = scheme.primary, trackColor = scheme.surfaceContainerHigh)
                Text("${status.progress.coerceAtMost(badge.target)} of ${badge.target} ${badge.unit}", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            }
            Text("How to earn it", style = MaterialTheme.typography.titleSmall, color = scheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 6.dp))
            badge.howTo.forEachIndexed { i, step ->
                AnimatedVisibility(visible = i < revealed, enter = fadeIn() + slideInHorizontally { it / 4 }) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(28.dp).background(if (status.earned) scheme.primaryContainer else scheme.surfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) {
                            Text(if (status.earned) "✓" else "${i + 1}", style = MaterialTheme.typography.labelLarge, color = if (status.earned) scheme.onPrimaryContainer else scheme.onSurfaceVariant)
                        }
                        Text(step, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RewardRow(item: RewardItem, unlocked: Boolean, active: Boolean, affordable: Boolean, onRedeem: () -> Unit, onUse: () -> Unit) {
    val border by animateColorAsState(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, label = "border")
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .border(2.dp, border, RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(18.dp))
            .clickable(enabled = unlocked && item.kind != RewardKind.FEATURE, onClick = onUse)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.kind == RewardKind.THEME) {
            val palette = Palettes.forId(item.id)
            Row(Modifier.padding(end = 12.dp)) {
                listOf(palette.primary, palette.secondary, palette.tertiary).forEach { c -> Box(Modifier.size(16.dp).background(c, CircleShape)) }
            }
        } else {
            Text(item.emoji, fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.titleSmall)
            Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        when {
            active -> Text("Active", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            unlocked && item.kind == RewardKind.FEATURE -> Text("Unlocked", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            unlocked -> FilledTonalButton(onClick = onUse) { Text(if (item.kind == RewardKind.THEME) "Apply" else "Wear") }
            else -> Button(onClick = onRedeem, enabled = affordable) { Text("⭐ ${item.cost}") }
        }
    }
}
