package com.homequest.app.ui.screens.rewards

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.R
import com.homequest.app.domain.BadgeStatus
import com.homequest.app.domain.PointPack
import com.homequest.app.domain.PointPacks
import com.homequest.app.domain.RewardItem
import com.homequest.app.domain.RewardKind
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AnimatedCount
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.components.reasonLabel
import com.homequest.app.ui.theme.Palettes
import com.homequest.app.util.Upi
import com.homequest.app.util.UpiResult
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(onBack: () -> Unit, onOpenAvatar: (Long) -> Unit, onOpenBadges: () -> Unit) {
    val vm = appViewModel { RewardsViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var selectedBadge by remember { mutableStateOf<BadgeStatus?>(null) }
    val messageText = when (val m = state.lastMessage) {
        is RewardsMessage.Redeem -> {
            val name = stringResource(m.item.nameRes)
            if (m.missing <= 0) stringResource(R.string.unlocked_msg, m.item.emoji, name) else stringResource(R.string.need_more_points, m.missing, name)
        }
        is RewardsMessage.Purchase -> stringResource(R.string.purchase_msg, m.pack.points)
        RewardsMessage.PaymentFailed -> stringResource(R.string.upi_failed)
        RewardsMessage.NoUpiApp -> stringResource(R.string.upi_no_app)
        null -> null
    }
    var paying by remember { mutableStateOf<PointPack?>(null) }
    var confirming by remember { mutableStateOf<PointPack?>(null) }
    val payLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val pack = paying ?: return@rememberLauncherForActivityResult
        paying = null
        when (Upi.result(result.data)) {
            UpiResult.SUCCESS -> vm.creditPurchase(pack)
            UpiResult.FAILED -> vm.notify(RewardsMessage.PaymentFailed)
            UpiResult.UNKNOWN -> confirming = pack
        }
    }
    val packNote = stringResource(R.string.upi_note)

    LaunchedEffect(messageText) {
        messageText?.let { snackbar.showSnackbar(it); vm.clearMessage() }
    }

    selectedBadge?.let { BadgeDetailSheet(it, onDismiss = { selectedBadge = null }) }
    confirming?.let { pack ->
        AlertDialog(
            onDismissRequest = { confirming = null },
            title = { Text(stringResource(R.string.upi_confirm_q)) },
            text = { Text(stringResource(R.string.upi_confirm_body)) },
            confirmButton = { TextButton(onClick = { vm.creditPurchase(pack); confirming = null }) { Text(stringResource(R.string.upi_paid)) } },
            dismissButton = { TextButton(onClick = { confirming = null }) { Text(stringResource(R.string.upi_not_paid)) } }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.rewards)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back)) } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = 32.dp)) {
            item {
                val scheme = MaterialTheme.colorScheme
                val shimmer by rememberInfiniteTransition(label = "hero").animateFloat(-1f, 2f, infiniteRepeatable(tween(2600, easing = LinearEasing)), label = "sweep")
                Box(
                    Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(scheme.primary, scheme.tertiary)))
                        .drawWithContent {
                            drawContent()
                            val x = size.width * shimmer
                            drawRect(Brush.linearGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.22f), Color.Transparent), start = Offset(x - 160f, 0f), end = Offset(x + 160f, size.height)))
                        }
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.points_to_spend), style = MaterialTheme.typography.labelLarge, color = scheme.onPrimary.copy(alpha = 0.9f))
                            AnimatedCount(state.profile.points, MaterialTheme.typography.displayMedium, scheme.onPrimary)
                            Text(
                                stringResource(R.string.lifetime_level, state.profile.lifetimePoints, state.level.level, stringResource(state.level.titleRes)),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onPrimary.copy(alpha = 0.9f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val bob by rememberInfiniteTransition(label = "star").animateFloat(-6f, 6f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "bob")
                        Text("⭐", fontSize = 56.sp, modifier = Modifier.offset { IntOffset(0, bob.roundToInt()) })
                    }
                }
            }
            item {
                val earnedCount = state.badges.count { it.earned }
                SectionHeader(stringResource(R.string.badges_count, earnedCount, state.badges.size), action = stringResource(R.string.see_all), onAction = onOpenBadges)
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(state.badges.take(8), key = { _, s -> s.badge.id }) { i, status -> BadgeTile(status, i, Modifier.width(118.dp)) { selectedBadge = status } }
                }
            }
            item {
                SectionHeader(stringResource(R.string.shop))
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(RewardKind.entries) { k -> FilterChip(selected = state.kind == k, onClick = { vm.selectKind(k) }, label = { Text(stringResource(k.labelRes), maxLines = 1) }) }
                }
                Spacer(Modifier.height(8.dp))
            }
            items(state.items, key = { it.id }) { item ->
                RewardRow(
                    modifier = Modifier.animateItem(),
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
            item {
                SectionHeader(stringResource(R.string.topup_title))
                HqCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Text(stringResource(R.string.topup_sub), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PointPacks.packs.forEach { pack ->
                            PackTile(pack, Modifier.weight(1f)) {
                                paying = pack
                                try {
                                    payLauncher.launch(Upi.payIntent(pack.rupees, packNote))
                                } catch (e: ActivityNotFoundException) {
                                    paying = null
                                    vm.notify(RewardsMessage.NoUpiApp)
                                }
                            }
                        }
                    }
                    Text(stringResource(R.string.upi_to, Upi.ID), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            item { SectionHeader(stringResource(R.string.points_history)) }
            items(state.ledger, key = { "led_${it.id}" }) { entry ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp)) {
                    Text(reasonLabel(entry.reason), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
private fun PackTile(pack: PointPack, modifier: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier
            .background(scheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .border(1.dp, scheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.pack_points, pack.points), style = MaterialTheme.typography.titleMedium, color = scheme.primary, maxLines = 1)
        Text(stringResource(R.string.pack_price, pack.rupees), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 2.dp), maxLines = 1)
    }
}

@Composable
private fun RewardRow(modifier: Modifier, item: RewardItem, unlocked: Boolean, active: Boolean, affordable: Boolean, onRedeem: () -> Unit, onUse: () -> Unit) {
    val border by animateColorAsState(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, label = "border")
    Row(
        modifier
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
            Text(stringResource(item.nameRes), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(stringResource(item.descriptionRes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        when {
            active -> Text(stringResource(R.string.active), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            unlocked && item.kind == RewardKind.FEATURE -> Text(stringResource(R.string.unlocked), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            unlocked -> FilledTonalButton(onClick = onUse) { Text(stringResource(if (item.kind == RewardKind.THEME) R.string.apply else R.string.wear)) }
            else -> Button(onClick = onRedeem, enabled = affordable) { Text("⭐ ${item.cost}") }
        }
    }
}
