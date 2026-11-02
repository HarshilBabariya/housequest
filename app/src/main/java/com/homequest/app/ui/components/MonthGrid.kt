package com.homequest.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun MonthGrid(month: YearMonth, cell: @Composable (LocalDate, Modifier) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val firstDay = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    val days = (0L..6L).map { firstDay.plus(it) }
    val leading = Math.floorMod(month.atDay(1).dayOfWeek.value - firstDay.value, 7)
    val cells: List<LocalDate?> = List(leading) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            days.forEach { d ->
                Text(
                    d.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (d == DayOfWeek.SUNDAY) scheme.error else scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
        cells.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0 until 7).forEach { i ->
                    val date = week.getOrNull(i)
                    if (date == null) Spacer(Modifier.weight(1f)) else cell(date, Modifier.weight(1f))
                }
            }
        }
    }
}
