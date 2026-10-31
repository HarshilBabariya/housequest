package com.homequest.app.ui.screens.rewards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.domain.Badge
import com.homequest.app.domain.RewardItem
import com.homequest.app.domain.RewardKind
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AnimatedCount
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.theme.Palettes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(onOpenAvatar: (Long) -> Unit) {
    val vm = appViewModel { RewardsViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.lastMessage) {
        state.lastMessage?.let { snackbar.showSnackbar(it); vm.clearMessage() }
    }

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
                SectionHeader("Badges · ${state.earnedBadgeIds.size}/${state.badges.size}")
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.badges, key = { it.id }) { BadgeTile(it, it.id in state.earnedBadgeIds) }
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
private fun BadgeTile(badge: Badge, earned: Boolean) {
    val scale by animateFloatAsState(if (earned) 1f else 0.92f, label = "badge")
    Column(
        Modifier
            .width(108.dp)
            .scale(scale)
            .alpha(if (earned) 1f else 0.45f)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, RoundedCornerShape(18.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(52.dp).background(if (earned) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) {
            Text(if (earned) badge.emoji else "🔒", fontSize = 26.sp)
        }
        Text(badge.name, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp), maxLines = 1)
        Text(badge.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, minLines = 2)
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
