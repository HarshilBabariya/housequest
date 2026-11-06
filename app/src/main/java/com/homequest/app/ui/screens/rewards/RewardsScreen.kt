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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homequest.app.R
import com.homequest.app.data.local.PointsLedgerEntity
import com.homequest.app.domain.BadgeStatus
import com.homequest.app.domain.PointPack
import com.homequest.app.domain.PointPacks
import com.homequest.app.domain.RewardItem
import com.homequest.app.domain.RewardKind
import com.homequest.app.ui.appViewModel
import com.homequest.app.ui.components.AnimatedCount
import com.homequest.app.ui.components.ProCard
import com.homequest.app.ui.components.SectionHeader
import com.homequest.app.ui.components.reasonEmoji
import com.homequest.app.ui.components.reasonLabel
import com.homequest.app.ui.screens.expenses.OptionSheet
import com.homequest.app.ui.theme.Palettes
import com.homequest.app.util.Dates
import com.homequest.app.util.Upi
import com.homequest.app.util.UpiResult
import java.time.Instant
import java.time.ZoneId
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(openPro: Boolean, onBack: () -> Unit, onOpenAvatar: (Long) -> Unit, onOpenBadges: () -> Unit) {
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
    var showPlans by rememberSaveable { mutableStateOf(openPro) }
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
    if (showPlans) {
        OptionSheet(stringResource(R.string.pro_title), onDismiss = { showPlans = false }) {
            Text(stringResource(R.string.topup_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PointPacks.packs.forEachIndexed { i, pack ->
                    PlanRow(pack, best = i == PointPacks.packs.lastIndex) {
                        showPlans = false
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
        }
    }
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
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back)) } }
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
                    claimable = item.claimableDuring(state.activePack),
                    onRedeem = { vm.redeem(item) },
                    onClaim = { vm.claim(item) },
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
                ProCard({ showPlans = true }, Modifier.padding(horizontal = 20.dp))
            }
            if (state.ledger.isNotEmpty()) item { SectionHeader(stringResource(R.string.points_history)) }
            itemsIndexed(state.ledger, key = { _, it -> "led_${it.id}" }) { i, entry ->
                val first = i == 0
                val last = i == state.ledger.lastIndex
                val shape = RoundedCornerShape(topStart = if (first) 22.dp else 0.dp, topEnd = if (first) 22.dp else 0.dp, bottomStart = if (last) 22.dp else 0.dp, bottomEnd = if (last) 22.dp else 0.dp)
                Column(Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(shape).background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
                    LedgerRow(entry)
                    if (!last) HorizontalDivider(Modifier.padding(start = 68.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun LedgerRow(entry: PointsLedgerEntity) {
    val scheme = MaterialTheme.colorScheme
    val gain = entry.delta > 0
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(scheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Text(reasonEmoji(entry.reason), style = MaterialTheme.typography.titleMedium)
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(reasonLabel(entry.reason), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                Dates.relativeLabel(Instant.ofEpochMilli(entry.at).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay(), stringResource(R.string.today), stringResource(R.string.yesterday)),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Text(
            (if (gain) "+" else "") + "%,d".format(entry.delta),
            style = MaterialTheme.typography.labelLarge,
            color = if (gain) scheme.primary else scheme.error,
            modifier = Modifier.clip(RoundedCornerShape(50)).background((if (gain) scheme.primary else scheme.error).copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PlanRow(pack: PointPack, best: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (best) scheme.primaryContainer else scheme.surfaceContainerLowest)
            .border(if (best) 2.dp else 1.dp, if (best) scheme.primary else scheme.outlineVariant, shape)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(pack.nameRes), style = MaterialTheme.typography.titleMedium, maxLines = 1)
                if (best) {
                    Text(
                        stringResource(R.string.best_value),
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onPrimary,
                        modifier = Modifier.padding(start = 8.dp).background(scheme.primary, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
            }
            Text(stringResource(R.string.pack_points, pack.points), style = MaterialTheme.typography.bodyMedium, color = scheme.primary, maxLines = 1)
        }
        Text(stringResource(R.string.pack_price, pack.rupees), style = MaterialTheme.typography.titleLarge, maxLines = 1)
    }
}

@Composable
private fun RewardRow(modifier: Modifier, item: RewardItem, unlocked: Boolean, active: Boolean, affordable: Boolean, claimable: Boolean, onRedeem: () -> Unit, onClaim: () -> Unit, onUse: () -> Unit) {
    val border by animateColorAsState(if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, label = "border")
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(2.dp, border, RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
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
            claimable -> Button(onClick = onClaim) { Text(stringResource(R.string.claim_free)) }
            item.pack != null -> Text(
                stringResource(R.string.pack_only, stringResource(item.pack.nameRes)),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 110.dp)
            )
            else -> Button(onClick = onRedeem, enabled = affordable) { Text("⭐ ${item.cost}") }
        }
    }
}
