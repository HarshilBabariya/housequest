package com.homequest.app.ui.screens.rewards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homequest.app.R
import com.homequest.app.domain.BadgeStatus
import com.homequest.app.util.Dates
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.delay

@Composable
fun BadgeTile(status: BadgeStatus, index: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val badge = status.badge
    val appear = remember(badge.id) { Animatable(0f) }
    LaunchedEffect(badge.id) { delay(60L * index.coerceAtMost(8)); appear.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(1f, 1.07f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "scale")
    Column(
        modifier
            .scale(0.7f + 0.3f * appear.value)
            .alpha(appear.value)
            .clip(RoundedCornerShape(18.dp))
            .background(scheme.surfaceContainerLowest)
            .border(1.dp, if (status.earned) scheme.primary.copy(alpha = 0.35f) else scheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(54.dp)
                    .scale(if (status.earned) pulse else 1f)
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
        Text(stringResource(badge.nameRes), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (status.earned) scheme.onSurface else scheme.onSurfaceVariant)
        LinearProgressIndicator(
            progress = { status.fraction },
            modifier = Modifier.padding(top = 6.dp, start = 10.dp, end = 10.dp).fillMaxWidth().height(4.dp),
            color = if (status.earned) scheme.secondary else scheme.primary,
            trackColor = scheme.surfaceContainerHigh
        )
        Text(
            if (status.earned) stringResource(R.string.plus_pts, badge.bonusPoints) else "${status.progress.coerceAtMost(badge.target)}/${badge.target}",
            style = MaterialTheme.typography.labelSmall,
            color = if (status.earned) scheme.primary else scheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeDetailSheet(status: BadgeStatus, onDismiss: () -> Unit) {
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
            Text(stringResource(badge.nameRes), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 14.dp))
            Text(stringResource(badge.descriptionRes), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            Text(
                if (status.earned) stringResource(R.string.earned_on, Dates.longLabel(Instant.ofEpochMilli(status.earnedAt!!).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()), badge.bonusPoints)
                else stringResource(R.string.reward_points, badge.bonusPoints),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
            if (!status.earned) {
                LinearProgressIndicator(progress = { status.fraction }, modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 2.dp), color = scheme.primary, trackColor = scheme.surfaceContainerHigh)
                Text(stringResource(R.string.progress_of, status.progress.coerceAtMost(badge.target), badge.target, stringResource(badge.unitRes)), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
            }
            Text(stringResource(R.string.how_to_earn), style = MaterialTheme.typography.titleSmall, color = scheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 6.dp))
            badge.howTo.forEachIndexed { i, stepRes ->
                AnimatedVisibility(visible = i < revealed, enter = fadeIn() + slideInHorizontally { it / 4 }) {
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(28.dp).background(if (status.earned) scheme.primaryContainer else scheme.surfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) {
                            Text(if (status.earned) "✓" else "${i + 1}", style = MaterialTheme.typography.labelLarge, color = if (status.earned) scheme.onPrimaryContainer else scheme.onSurfaceVariant)
                        }
                        Text(stringResource(stepRes), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        }
    }
}
