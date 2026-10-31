package com.homequest.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.homequest.app.data.local.CategoryTotal
import com.homequest.app.domain.ExpenseCategory

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 14f,
    color: Color = MaterialTheme.colorScheme.primary,
    track: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
    content: @Composable () -> Unit = {}
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val animated by animateFloatAsState(if (started) progress.coerceIn(0f, 1f) else 0f, tween(900), label = "ring")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(120.dp)) {
            val stroke = Stroke(strokeWidth, cap = StrokeCap.Round)
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            drawArc(track, -90f, 360f, false, Offset(inset, inset), arcSize, style = stroke)
            drawArc(color, -90f, 360f * animated, false, Offset(inset, inset), arcSize, style = stroke)
        }
        content()
    }
}

@Composable
fun DonutChart(totals: List<CategoryTotal>, modifier: Modifier = Modifier, centerLabel: String) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(totals) { started = true }
    val reveal by animateFloatAsState(if (started) 1f else 0f, tween(1000), label = "donut")
    val sum = totals.sumOf { it.total }.takeIf { it > 0 } ?: 1.0
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(170.dp)) {
            val stroke = Stroke(28f, cap = StrokeCap.Butt)
            val inset = 14f
            val arcSize = Size(size.width - 28f, size.height - 28f)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = stroke)
            var start = -90f
            totals.forEach { t ->
                val sweep = (t.total / sum * 360f * reveal).toFloat()
                drawArc(t.category.color, start, sweep - 2f, false, Offset(inset, inset), arcSize, style = stroke)
                start += sweep
            }
        }
        Text(centerLabel, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

@Composable
fun BarChart(values: List<Double>, labels: List<String>, modifier: Modifier = Modifier, highlightLast: Boolean = true) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(values) { started = true }
    val grow by animateFloatAsState(if (started) 1f else 0f, tween(800), label = "bars")
    val max = values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(120.dp)) {
            if (values.isEmpty()) return@Canvas
            val slot = size.width / values.size
            val barWidth = slot * 0.55f
            values.forEachIndexed { i, v ->
                val h = (v / max * size.height * grow).toFloat().coerceAtLeast(4f)
                val color = if (highlightLast && i == values.lastIndex) primary else muted
                drawRoundRect(
                    color,
                    topLeft = Offset(i * slot + (slot - barWidth) / 2, size.height - h),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            labels.forEach {
                Text(
                    it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StackedMonthChart(months: List<String>, series: Map<ExpenseCategory, List<Double>>, modifier: Modifier = Modifier) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(series) { started = true }
    val grow by animateFloatAsState(if (started) 1f else 0f, tween(900), label = "stack")
    val columnTotals = months.indices.map { i -> series.values.sumOf { it.getOrElse(i) { 0.0 } } }
    val max = columnTotals.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
            if (months.isEmpty()) return@Canvas
            val slot = size.width / months.size
            val barWidth = slot * 0.6f
            months.indices.forEach { i ->
                var y = size.height
                series.forEach { (category, values) ->
                    val v = values.getOrElse(i) { 0.0 }
                    if (v <= 0) return@forEach
                    val h = (v / max * size.height * grow).toFloat()
                    drawRect(category.color, Offset(i * slot + (slot - barWidth) / 2, y - h), Size(barWidth, h))
                    y -= h
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            months.forEach {
                Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
