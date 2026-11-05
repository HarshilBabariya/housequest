package com.homequest.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homequest.app.R
import com.homequest.app.domain.FestivalDay
import com.homequest.app.domain.PointsRules
import com.homequest.app.domain.StreakMilestones
import com.homequest.app.ui.components.FestiveEffect
import com.homequest.app.ui.components.HqCard
import com.homequest.app.ui.components.StreakFlame
import com.homequest.app.ui.components.look
import com.homequest.app.ui.theme.Palettes
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun FestivalBanner(day: FestivalDay, upcoming: Boolean, today: LocalDate, name: String, onCelebrate: (() -> Unit)?, modifier: Modifier = Modifier) {
    val pack = day.festival.pack
    val palette = Palettes.forId(pack.themeId)
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(shape)
            .background(Brush.linearGradient(listOf(palette.primary, palette.tertiary.copy(alpha = 0.9f), palette.secondary)))
            .then(if (onCelebrate == null) Modifier else Modifier.clickable(onClick = onCelebrate))
    ) {
        FestiveEffect(pack.look())
        Box(Modifier.matchParentSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent))))
        Row(Modifier.padding(horizontal = 18.dp).align(Alignment.CenterStart), verticalAlignment = Alignment.CenterVertically) {
            Text(pack.emoji, fontSize = 40.sp)
            Column(Modifier.padding(start = 14.dp)) {
                val days = ChronoUnit.DAYS.between(today, day.main).toInt()
                Text(
                    if (days > 0) pluralStringResource(R.plurals.fest_countdown, days, stringResource(day.festival.nameRes), days) else stringResource(day.festival.titleRes, name),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    when { upcoming -> stringResource(R.string.fest_upcoming_hint, PointsRules.FESTIVAL_GIFT); onCelebrate != null -> stringResource(R.string.fest_tap_to_celebrate); else -> stringResource(day.festival.lineRes) },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun StreakCard(streak: Int, loggedToday: Boolean, today: LocalDate, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val lastLogged = if (loggedToday) today else today.minusDays(1)
    val firstLogged = lastLogged.minusDays(streak.toLong() - 1)
    HqCard(modifier.fillMaxWidth(), onClick = onClick, padding = 14) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StreakFlame(52.dp, lit = streak > 0)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    if (streak > 0) stringResource(R.string.toast_streak, streak) else stringResource(R.string.streak_start_title),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(
                        when {
                            streak == 0 -> R.string.streak_start_body
                            loggedToday -> R.string.streak_safe
                            else -> R.string.streak_at_risk
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (streak > 0 && !loggedToday) scheme.error else scheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (streak > 0) {
                val next = StreakMilestones.next(streak)
                Text(stringResource(R.string.streak_next, next), style = MaterialTheme.typography.labelMedium, color = scheme.primary, maxLines = 2)
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            (6 downTo 0).forEach { back ->
                val day = today.minusDays(back.toLong())
                val lit = streak > 0 && !day.isBefore(firstLogged) && !day.isAfter(lastLogged)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(30.dp)
                            .background(if (lit) Brush.verticalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF6D00))) else Brush.verticalGradient(listOf(scheme.surfaceContainerHigh, scheme.surfaceContainerHigh)), CircleShape)
                            .border(1.5.dp, if (back == 0 && !lit) scheme.primary else Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text(if (lit) "🔥" else "", fontSize = 13.sp) }
                    Text(day.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
