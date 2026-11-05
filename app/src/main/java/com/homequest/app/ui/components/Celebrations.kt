package com.homequest.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.homequest.app.R
import com.homequest.app.data.repository.RewardEvent
import com.homequest.app.domain.AvatarAction
import com.homequest.app.domain.AvatarConfig
import com.homequest.app.domain.BadgeCatalog
import com.homequest.app.domain.Festival
import com.homequest.app.domain.FestivalDay
import com.homequest.app.domain.LedgerReasons
import com.homequest.app.domain.PointPacks
import com.homequest.app.domain.PointsRules
import com.homequest.app.domain.RewardCatalog
import com.homequest.app.util.Dates
import com.homequest.app.util.money
import java.time.temporal.ChronoUnit
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
fun AnimatedMoney(amount: Double, symbol: String, style: TextStyle, color: Color = MaterialTheme.colorScheme.onSurface) {
    val animated by animateFloatAsState(amount.toFloat(), tween(800), label = "money")
    Text(animated.toDouble().money(symbol), style = style, color = color)
}

@Composable
fun AnimatedCount(value: Int, style: TextStyle, color: Color = MaterialTheme.colorScheme.onSurface) {
    val animated by animateFloatAsState(value.toFloat(), spring(stiffness = Spring.StiffnessLow), label = "count")
    Text("%,d".format(animated.toInt()), style = style, color = color)
}

private class Particle(val x: Float, val color: Color, val speed: Float, val drift: Float, val size: Float, val spin: Float)

@Composable
fun ConfettiBurst(trigger: Int, modifier: Modifier = Modifier) {
    val palette = listOf(
        MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary,
        Color(0xFFFFD166), Color(0xFFEF476F), Color(0xFF06D6A0)
    )
    val particles = remember(trigger) {
        List(60) { Particle(Random.nextFloat(), palette.random(), 0.6f + Random.nextFloat(), Random.nextFloat() - 0.5f, 8f + Random.nextFloat() * 10f, Random.nextFloat() * 360f) }
    }
    val progress = remember(trigger) { Animatable(0f) }
    LaunchedEffect(trigger) { if (trigger > 0) progress.animateTo(1f, tween(1800)) }
    if (trigger > 0 && progress.value < 1f) {
        Canvas(modifier.fillMaxSize()) {
            particles.forEach { p ->
                val t = (progress.value * p.speed).coerceIn(0f, 1f)
                val y = -40f + t * (size.height + 80f)
                val x = p.x * size.width + sin(t * 6f + p.spin) * 40f * p.drift
                val angle = p.spin + t * 720f
                val dx = cos(Math.toRadians(angle.toDouble())).toFloat() * p.size
                val dy = sin(Math.toRadians(angle.toDouble())).toFloat() * p.size
                drawRect(p.color.copy(alpha = 1f - t * t), Offset(x - dx / 2, y - dy / 2), Size(p.size, p.size * 0.6f))
            }
        }
    }
}

@Composable
fun RewardToast(event: RewardEvent?, onDismiss: () -> Unit) {
    var visible by remember(event) { mutableStateOf(event != null) }
    LaunchedEffect(event) {
        if (event != null) {
            delay(2600)
            visible = false
            delay(300)
            onDismiss()
        }
    }
    AnimatedVisibility(
        visible = visible && event != null,
        enter = slideInVertically { -it } + fadeIn() + scaleIn(initialScale = 0.9f),
        exit = slideOutVertically { -it } + fadeOut()
    ) {
        val (emoji, title, body) = when (event) {
            is RewardEvent.PointsAwarded -> Triple("✨", stringResource(R.string.toast_points, event.delta), reasonLabel(event.reason))
            is RewardEvent.BadgeEarned -> Triple(event.badge.emoji, stringResource(R.string.toast_badge, stringResource(event.badge.nameRes)), stringResource(event.badge.descriptionRes))
            is RewardEvent.StreakExtended -> Triple("🔥", stringResource(R.string.toast_streak, event.days), stringResource(R.string.toast_streak_body))
            is RewardEvent.LevelUp, is RewardEvent.StreakMilestone, null -> Triple("", "", "")
        }
        Box(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(emoji, fontSize = 30.sp)
                Column(Modifier.padding(start = 14.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun reasonLabel(reason: String): String {
    val arg = LedgerReasons.arg(reason)
    return when (LedgerReasons.kind(reason)) {
        LedgerReasons.LOG_EXPENSE -> stringResource(R.string.reason_log_expense)
        LedgerReasons.GROUP -> stringResource(R.string.reason_group)
        LedgerReasons.SETTLE -> stringResource(R.string.reason_settle)
        LedgerReasons.MEMBER -> stringResource(R.string.reason_member)
        LedgerReasons.BUDGET -> stringResource(R.string.reason_budget, arg)
        LedgerReasons.BADGE -> stringResource(R.string.reason_badge, BadgeCatalog.find(arg)?.let { stringResource(it.nameRes) } ?: arg)
        LedgerReasons.UNLOCK -> stringResource(R.string.reason_unlock, RewardCatalog.find(arg)?.let { stringResource(it.nameRes) } ?: arg)
        LedgerReasons.PURCHASE -> stringResource(R.string.reason_purchase, PointPacks.find(arg)?.rupees ?: 0)
        LedgerReasons.FESTIVAL -> stringResource(R.string.reason_festival, Festival.find(arg)?.let { stringResource(it.nameRes) } ?: arg)
        else -> reason
    }
}

sealed interface Celebration {
    data class Streak(val days: Int) : Celebration
    data class Level(val level: Int, val titleRes: Int) : Celebration
    data class Holiday(val day: FestivalDay) : Celebration
}

@Composable
fun CelebrationHost(celebration: Celebration, avatar: AvatarConfig?, name: String, giftedKey: String, onShown: () -> Unit, onAction: () -> Unit, onDismiss: () -> Unit) {
    LaunchedEffect(celebration) { onShown() }
    val scheme = MaterialTheme.colorScheme
    when (celebration) {
        is Celebration.Streak -> {
            val lines = stringArrayResource(R.array.celebrate_streak_lines)
            CelebrationDialog(
                look = FestiveLook(EffectStyle.FIREWORKS, listOf(Color(0xFFFFB300), Color(0xFFFF6D00), Color(0xFFFFF176), Color(0xFFE53935))),
                title = stringResource(R.string.celebrate_streak_title, celebration.days),
                body = lines[celebration.days % lines.size].format(name, celebration.days),
                action = stringResource(R.string.celebrate_keep_going),
                onAction = onAction,
                onDismiss = onDismiss
            ) {
                Box(contentAlignment = Alignment.Center) {
                    StreakFlame(170.dp, lit = true)
                    Text("${celebration.days}", style = MaterialTheme.typography.displayMedium, color = Color(0xFF5D1A00), fontWeight = FontWeight.Black, modifier = Modifier.padding(top = 56.dp))
                }
            }
        }
        is Celebration.Level -> CelebrationDialog(
            look = FestiveLook(EffectStyle.CONFETTI, listOf(scheme.primary, scheme.secondary, scheme.tertiary, Color(0xFFFFD166))),
            title = stringResource(R.string.toast_level, celebration.level, stringResource(celebration.titleRes)),
            body = stringResource(R.string.toast_level_body),
            action = stringResource(R.string.celebrate_keep_going),
            onAction = onAction,
            onDismiss = onDismiss
        ) {
            Box(
                Modifier.size(150.dp).background(Brush.radialGradient(listOf(Color(0xFFFFE082), Color(0xFFFFA000))), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⭐", fontSize = 44.sp)
                    Text("${celebration.level}", style = MaterialTheme.typography.displayMedium, color = Color(0xFF4E2A00), fontWeight = FontWeight.Black)
                }
            }
        }
        is Celebration.Holiday -> {
            val festival = celebration.day.festival
            val daysToGo = ChronoUnit.DAYS.between(Dates.today(), celebration.day.main).toInt()
            CelebrationDialog(
                look = festival.pack.look(),
                title = if (daysToGo > 0) pluralStringResource(R.plurals.fest_countdown, daysToGo, stringResource(festival.nameRes), daysToGo) else stringResource(festival.titleRes, name),
                body = stringResource(festival.lineRes) + "\n\n" + stringResource(R.string.fest_gift_hint, stringResource(festival.pack.nameRes), PointsRules.FESTIVAL_GIFT),
                action = if (celebration.day.key == giftedKey) stringResource(R.string.fest_wear) else stringResource(R.string.fest_claim, PointsRules.FESTIVAL_GIFT),
                onAction = onAction,
                onDismiss = onDismiss,
                dismissLabel = stringResource(R.string.later)
            ) {
                Box(contentAlignment = Alignment.TopCenter) {
                    avatar?.let { AvatarView(RewardCatalog.dressFor(festival.pack, it), size = 150.dp, animated = true, play = AvatarPlay(AvatarAction.CHEER, 1), modifier = Modifier.padding(top = 30.dp)) }
                    FestivalEmblem(festival.pack, 52.dp)
                }
            }
        }
    }
}

@Composable
private fun CelebrationDialog(
    look: FestiveLook,
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String? = null,
    hero: @Composable () -> Unit
) {
    val entrance = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) { entrance.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
    val glow by rememberInfiniteTransition(label = "glow").animateFloat(0.85f, 1.08f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "glowScale")
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xF21A1033), Color(0xF2070512)))),
            contentAlignment = Alignment.Center
        ) {
            FestiveEffect(look)
            Column(
                Modifier.fillMaxWidth().padding(28.dp).scale(entrance.value),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(Modifier.scale(glow), contentAlignment = Alignment.Center) { hero() }
                Text(title, style = MaterialTheme.typography.headlineMedium, color = Color.White, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(body, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center)
                PrimaryButton(action, onClick = onAction, modifier = Modifier.padding(top = 10.dp))
                dismissLabel?.let { TextButton(onClick = onDismiss) { Text(it, color = Color.White.copy(alpha = 0.8f)) } }
            }
        }
    }
}
