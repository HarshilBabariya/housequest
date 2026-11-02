package com.homequest.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.homequest.app.domain.CategoryAmount
import kotlin.math.atan2
import kotlin.math.roundToInt

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
fun DonutChart(totals: List<CategoryAmount>, valueLabel: (Double) -> String, modifier: Modifier = Modifier, centerLabel: String) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(totals) { started = true }
    val reveal by animateFloatAsState(if (started) 1f else 0f, tween(1000), label = "donut")
    var selected by remember(totals) { mutableIntStateOf(-1) }
    val sum = totals.sumOf { it.total }.takeIf { it > 0 } ?: 1.0
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(
            Modifier.size(170.dp).pointerInput(totals) {
                detectTapGestures { pos ->
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val d = pos - c
                    val r = d.getDistance()
                    if (r < size.width / 2f - 44f || r > size.width / 2f + 4f) {
                        selected = -1
                        return@detectTapGestures
                    }
                    val angle = ((Math.toDegrees(atan2(d.y, d.x).toDouble()) + 90.0 + 360.0) % 360.0) / 360.0
                    var acc = 0.0
                    val hit = totals.indexOfFirst { t -> acc += t.total / sum; angle <= acc }
                    selected = if (hit == selected) -1 else hit
                }
            }
        ) {
            val inset = 18f
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(28f))
            var start = -90f
            totals.forEachIndexed { i, t ->
                val sweep = (t.total / sum * 360f * reveal).toFloat()
                val active = i == selected
                drawArc(
                    t.category.color.copy(alpha = if (selected < 0 || active) 1f else 0.35f),
                    start, sweep - 2f, false, Offset(inset, inset), arcSize,
                    style = Stroke(if (active) 40f else 28f, cap = StrokeCap.Butt)
                )
                start += sweep
            }
        }
        val pick = totals.getOrNull(selected)
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 40.dp)) {
            if (pick != null) {
                Text(pick.category.label(), style = MaterialTheme.typography.labelMedium, color = pick.category.color, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(valueLabel(pick.total), style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text("${(pick.total / sum * 100).roundToInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(centerLabel, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, maxLines = 1)
            }
        }
    }
}

@Composable
fun BarChart(values: List<Double>, labels: List<String>, valueLabel: (Double) -> String, modifier: Modifier = Modifier, highlightIndex: Int = values.lastIndex) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(values) { started = true }
    val grow by animateFloatAsState(if (started) 1f else 0f, tween(800), label = "bars")
    var selected by remember(values, highlightIndex) { mutableIntStateOf(highlightIndex) }
    val max = values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val scheme = MaterialTheme.colorScheme
    val highlight = Brush.verticalGradient(listOf(scheme.tertiary, scheme.primary))
    val muted = SolidColor(scheme.primary.copy(alpha = 0.28f))
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = scheme.primary, fontWeight = FontWeight.SemiBold)
    Column(modifier) {
        Canvas(
            Modifier.fillMaxWidth().height(140.dp).pointerInput(values.size) {
                detectTapGestures { pos -> if (values.isNotEmpty()) selected = (pos.x / size.width * values.size).toInt().coerceIn(values.indices) }
            }
        ) {
            if (values.isEmpty()) return@Canvas
            val label = values.getOrNull(selected)?.takeIf { it > 0 }?.let { measurer.measure(valueLabel(it), labelStyle) }
            val chartHeight = size.height - (label?.size?.height?.plus(4f) ?: 0f)
            val slot = size.width / values.size
            val barWidth = slot * 0.55f
            values.forEachIndexed { i, v ->
                val h = (v / max * chartHeight * grow).toFloat().coerceAtLeast(4f)
                val x = i * slot + (slot - barWidth) / 2
                drawRoundRect(if (i == selected) highlight else muted, Offset(x, size.height - h), Size(barWidth, h), CornerRadius(12f, 12f))
                if (i == selected && label != null) {
                    val tx = (x + barWidth / 2 - label.size.width / 2).coerceIn(0f, (size.width - label.size.width).coerceAtLeast(0f))
                    drawText(label, topLeft = Offset(tx, size.height - h - label.size.height - 4f))
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            labels.forEachIndexed { i, label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == selected) scheme.primary else scheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun AreaChart(
    values: List<Double>,
    axisLabels: List<String>,
    valueLabel: (Double) -> String,
    pointLabel: (Int) -> String,
    modifier: Modifier = Modifier,
    slots: Int = values.size,
    pace: Double? = null
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(values) { started = true }
    val reveal by animateFloatAsState(if (started) 1f else 0f, tween(1100), label = "area")
    var selected by remember(values.size) { mutableIntStateOf(-1) }
    val scheme = MaterialTheme.colorScheme
    val max = maxOf(values.maxOrNull() ?: 0.0, pace ?: 0.0).takeIf { it > 0 } ?: 1.0
    val measurer = rememberTextMeasurer()
    val bubbleStyle = MaterialTheme.typography.labelMedium.copy(color = scheme.onPrimary, fontWeight = FontWeight.SemiBold)
    val pick = { x: Float, width: Float -> if (values.isNotEmpty() && slots >= 2) selected = (x / (width / (slots - 1))).roundToInt().coerceIn(values.indices) }
    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(values.size, slots) { detectTapGestures { pick(it.x, size.width.toFloat()) } }
                .pointerInput(values.size, slots) { detectHorizontalDragGestures { change, _ -> pick(change.position.x, size.width.toFloat()) } }
        ) {
            if (values.isEmpty() || slots < 2) return@Canvas
            val step = size.width / (slots - 1)
            val y = { v: Double -> (size.height - v / max * size.height * 0.92).toFloat() }
            listOf(0.25f, 0.5f, 0.75f).forEach { f ->
                drawLine(scheme.outlineVariant.copy(alpha = 0.5f), Offset(0f, size.height * f), Offset(size.width, size.height * f), strokeWidth = 1f)
            }
            pace?.let {
                drawLine(
                    scheme.tertiary.copy(alpha = 0.8f),
                    Offset(0f, size.height),
                    Offset(size.width, y(it)),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                )
            }
            val points = values.mapIndexed { i, v -> Offset(i * step, y(v)) }
            val line = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.zipWithNext { a, b ->
                    val mid = (a.x + b.x) / 2
                    cubicTo(mid, a.y, mid, b.y, b.x, b.y)
                }
            }
            val fill = Path().apply {
                addPath(line)
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }
            clipRect(right = size.width * reveal) {
                drawPath(fill, Brush.verticalGradient(listOf(scheme.primary.copy(alpha = 0.38f), scheme.primary.copy(alpha = 0.02f))))
                drawPath(line, scheme.primary, style = Stroke(5f, cap = StrokeCap.Round))
                drawCircle(scheme.onPrimary, 9f, points.last())
                drawCircle(scheme.primary, 9f, points.last(), style = Stroke(5f))
            }
            points.getOrNull(selected)?.let { p ->
                drawLine(scheme.primary.copy(alpha = 0.5f), Offset(p.x, 0f), Offset(p.x, size.height), strokeWidth = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                drawCircle(scheme.primary, 10f, p)
                drawCircle(scheme.onPrimary, 5f, p)
                val text = measurer.measure("${pointLabel(selected)} · ${valueLabel(values[selected])}", bubbleStyle)
                val w = text.size.width + 24f
                val h = text.size.height + 12f
                val left = (p.x - w / 2).coerceIn(0f, (size.width - w).coerceAtLeast(0f))
                val top = if (p.y - h - 14f > 0f) p.y - h - 14f else p.y + 14f
                drawRoundRect(scheme.primary, Offset(left, top), Size(w, h), CornerRadius(h / 2, h / 2))
                drawText(text, topLeft = Offset(left + 12f, top + 6f))
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            axisLabels.forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, maxLines = 1) }
        }
    }
}

@Composable
fun ShareBar(parts: List<Pair<Color, Double>>, modifier: Modifier = Modifier) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(parts) { started = true }
    val reveal by animateFloatAsState(if (started) 1f else 0f, tween(900), label = "share")
    val sum = parts.sumOf { it.second }.takeIf { it > 0 } ?: 1.0
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    Canvas(modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(50))) {
        drawRect(track)
        var x = 0f
        parts.forEach { (color, value) ->
            val w = (value / sum * size.width * reveal).toFloat()
            drawRect(color, Offset(x, 0f), Size((w - 3f).coerceAtLeast(0f), size.height))
            x += w
        }
    }
}
