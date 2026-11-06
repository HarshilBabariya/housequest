package com.homequest.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.homequest.app.domain.FestivalPack
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

data class FestiveMoment(val pack: FestivalPack, val id: Int)

private enum class Show(val millis: Int) { ROCKET(2800), BALLOONS(2600), MODAKS(2600), KITE(2800), TIRANGA(2400), RAKHI(2600), DANDIYA(2600), SNOW(2800) }

private fun FestivalPack.show(): Show = when (this) {
    FestivalPack.DIWALI, FestivalPack.NEW_YEAR -> Show.ROCKET
    FestivalPack.HOLI -> Show.BALLOONS
    FestivalPack.GANESH -> Show.MODAKS
    FestivalPack.UTTARAYAN -> Show.KITE
    FestivalPack.TIRANGA -> Show.TIRANGA
    FestivalPack.RAKHI -> Show.RAKHI
    FestivalPack.NAVRATRI -> Show.DANDIYA
    FestivalPack.CHRISTMAS -> Show.SNOW
}

private class Spark(val angle: Float, val speed: Float, val color: Int, val phase: Float)

private const val ROCKET_RISE = 0.6f

@Composable
fun FestiveMomentOverlay(moment: FestiveMoment, onDone: () -> Unit) {
    val show = moment.pack.show()
    val colors = moment.pack.look().colors
    val progress = remember { Animatable(0f) }
    val sparks = remember { val r = Random(moment.id); List(72) { Spark(r.nextFloat() * TAU, 0.4f + r.nextFloat() * 0.6f, r.nextInt(colors.size), r.nextFloat()) } }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(show.millis, easing = LinearEasing))
        onDone()
    }
    Canvas(Modifier.fillMaxSize()) {
        val p = progress.value
        when (show) {
            Show.ROCKET -> rocket(p, sparks, colors)
            Show.BALLOONS -> balloons(p, sparks, colors)
            Show.MODAKS -> modaks(p, sparks)
            Show.KITE -> kite(p, sparks, colors)
            Show.TIRANGA -> tiranga(p, sparks)
            Show.RAKHI -> rakhi(p, sparks)
            Show.DANDIYA -> dandiya(p, sparks, colors)
            Show.SNOW -> snow(p, sparks)
        }
    }
}

private fun smooth(t: Float) = t.coerceIn(0f, 1f).let { it * it * (3f - 2f * it) }

private fun fadeOut(p: Float, from: Float = 0.8f) = ((1f - p) / (1f - from)).coerceIn(0f, 1f)

private fun DrawScope.rocket(p: Float, sparks: List<Spark>, colors: List<Color>) {
    val unit = 1.dp.toPx()
    val top = Offset(size.width / 2, size.height * 0.22f)
    if (p < ROCKET_RISE) {
        val rise = p / ROCKET_RISE
        val eased = rise * rise * (3f - 2f * rise)
        val start = size.height + 160 * unit
        val y = start - (start - top.y) * eased
        val x = top.x + sin(rise * 9f) * 5 * unit
        sparks.forEach { s ->
            val back = frac(s.phase + rise * 2f * s.speed)
            val at = Offset(x - 5 * unit + (s.angle / TAU - 0.5f) * (10f + 50f * back) * unit, y + (44f + 200f * back) * unit)
            drawCircle(listOf(Color(0xFFFFE08A), Color(0xFFFF9800), Color(0xFFB0B0B0))[s.color % 3].copy(alpha = (1f - back) * (if (s.color % 3 == 2) 0.35f else 0.9f)), (2.6f - 1.8f * back) * unit, at)
        }
        drawCircle(Brush.radialGradient(listOf(Color(0x55FFE08A), Color.Transparent), Offset(x, y), 70 * unit), 70 * unit, Offset(x, y))
        drawLine(Color(0xFF8B5E34), Offset(x + 6 * unit, y + 14 * unit), Offset(x + 6 * unit, y + 150 * unit), 3 * unit)
        drawLine(Color(0xFFD9B380), Offset(x + 5 * unit, y + 14 * unit), Offset(x + 5 * unit, y + 150 * unit), unit)
        drawRoundRect(Brush.horizontalGradient(listOf(Color(0xFFFB7185), Color(0xFFE11D48), Color(0xFF9F1239)), x - 9 * unit, x + 9 * unit), Offset(x - 9 * unit, y - 28 * unit), Size(18 * unit, 58 * unit), CornerRadius(2 * unit))
        drawRect(Color(0xFFFACC15), Offset(x - 9 * unit, y - 28 * unit), Size(18 * unit, 4 * unit))
        drawRect(Color(0xFFFACC15), Offset(x - 9 * unit, y + 26 * unit), Size(18 * unit, 4 * unit))
        drawRect(Color(0xFFFDE68A), Offset(x - 9 * unit, y - 10 * unit), Size(18 * unit, 20 * unit))
        drawRect(Color(0xFF15803D), Offset(x - 9 * unit, y - 3 * unit), Size(18 * unit, 6 * unit))
        drawCircle(colors[0], 3 * unit, Offset(x, y))
        drawPath(Path().apply { moveTo(x - 10 * unit, y - 28 * unit); lineTo(x, y - 58 * unit); lineTo(x + 10 * unit, y - 28 * unit); close() }, Brush.verticalGradient(listOf(Color(0xFF4ADE80), Color(0xFF15803D)), y - 58 * unit, y - 28 * unit))
        val flick = 0.7f + 0.3f * sin(p * 190f)
        drawLine(Color(0xFF6B7280), Offset(x, y + 30 * unit), Offset(x - 5 * unit, y + 40 * unit), 1.5f * unit)
        drawCircle(Brush.radialGradient(listOf(Color.White, Color(0xFFFFC107), Color.Transparent), Offset(x - 5 * unit, y + 40 * unit), 14 * flick * unit), 14 * flick * unit, Offset(x - 5 * unit, y + 40 * unit))
        repeat(8) { k ->
            val a = k * TAU / 8 + p * 40f
            val len = (8f + 6f * frac(p * 13f + k * 0.37f)) * unit * flick
            drawLine(Color(0xFFFFE08A).copy(alpha = 0.9f), Offset(x - 5 * unit, y + 40 * unit), Offset(x - 5 * unit + cos(a) * len, y + 40 * unit + sin(a) * len), unit)
        }
    } else {
        val e = (p - ROCKET_RISE) / (1f - ROCKET_RISE)
        val flash = (1f - e * 4f).coerceIn(0f, 1f)
        drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.85f * flash), Color.Transparent), top, 140 * unit), 140 * unit, top)
        fireworks(top, e, sparks, colors, size.minDimension * 0.46f)
        if (e > 0.3f) fireworks(Offset(top.x + 70 * unit, top.y + 60 * unit), (e - 0.3f) / 0.7f, sparks.take(36), colors.reversed(), size.minDimension * 0.26f)
    }
}

private fun DrawScope.fireworks(center: Offset, e: Float, sparks: List<Spark>, colors: List<Color>, reach: Float) {
    val eased = 1f - (1f - e).pow(3)
    val alpha = (1f - e).pow(1.3f)
    drawCircle(Brush.radialGradient(listOf(colors[0].copy(alpha = 0.35f * alpha), Color.Transparent), center, reach * 0.8f), reach * 0.8f, center)
    sparks.forEach { s ->
        val r = reach * s.speed * eased
        val drop = e * e * 110.dp.toPx()
        val at = Offset(center.x + cos(s.angle) * r, center.y + sin(s.angle) * r + drop)
        val tail = Offset(center.x + cos(s.angle) * r * 0.72f, center.y + sin(s.angle) * r * 0.72f + drop * 0.8f)
        val color = colors[s.color % colors.size]
        drawLine(color.copy(alpha = alpha * 0.5f), tail, at, 1.8.dp.toPx())
        drawCircle(color.copy(alpha = alpha), (3.4f - 1.8f * e).dp.toPx(), at)
        if (s.phase > 0.6f) drawCircle(Color.White.copy(alpha = alpha * frac(s.phase * 7f + e * 9f)), 1.4.dp.toPx(), at)
    }
}

private fun DrawScope.balloons(p: Float, sparks: List<Spark>, colors: List<Color>) {
    val unit = 1.dp.toPx()
    listOf(0.28f, 0.5f, 0.72f).forEachIndexed { i, fx ->
        val delay = i * 0.08f
        val rise = smooth((p - delay) / 0.5f)
        val popAt = 0.52f + delay
        val x = size.width * fx + sin(p * 7f + i) * 8 * unit
        val y = size.height + 60 * unit - (size.height + 60 * unit - size.height * 0.3f) * rise
        val color = colors[i % colors.size]
        if (p < popAt) {
            drawLine(Color.White.copy(alpha = 0.5f), Offset(x, y + 30 * unit), Offset(x + 4 * unit, y + 70 * unit), unit)
            drawOval(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.55f), color, color.copy(alpha = 0.85f)), Offset(x - 8 * unit, y - 14 * unit), 46 * unit), Offset(x - 26 * unit, y - 32 * unit), Size(52 * unit, 64 * unit))
            drawPath(Path().apply { moveTo(x - 4 * unit, y + 30 * unit); lineTo(x + 4 * unit, y + 30 * unit); lineTo(x, y + 36 * unit); close() }, color)
        } else {
            val e = ((p - popAt) / (1f - popAt)).coerceIn(0f, 1f)
            val reach = size.width * (0.12f + 0.3f * (1f - (1f - e).pow(2)))
            drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.6f * (1f - e)), Color.Transparent), Offset(x, y), reach), reach, Offset(x, y))
            sparks.drop(i * 24).take(24).forEach { s ->
                val r = reach * 1.3f * s.speed * (1f - (1f - e).pow(3))
                val at = Offset(x + cos(s.angle) * r, y + sin(s.angle) * r + e * e * 140 * unit)
                drawCircle(color.copy(alpha = (1f - e)), (4f - 2f * e) * unit, at)
            }
        }
    }
}

private fun DrawScope.modak(center: Offset, r: Float, alpha: Float) {
    val body = Brush.radialGradient(listOf(Color(0xFFFFF3C4), Color(0xFFFBBF24), Color(0xFFD97706)), Offset(center.x - r * 0.3f, center.y - r * 0.2f), r * 1.4f)
    drawPath(Path().apply {
        moveTo(center.x - r, center.y + r * 0.3f)
        quadraticTo(center.x - r * 0.6f, center.y - r * 0.6f, center.x, center.y - r * 1.3f)
        quadraticTo(center.x + r * 0.6f, center.y - r * 0.6f, center.x + r, center.y + r * 0.3f)
        quadraticTo(center.x, center.y + r * 1.1f, center.x - r, center.y + r * 0.3f)
        close()
    }, body, alpha = alpha)
    repeat(5) { k ->
        val a = -TAU / 4 + (k - 2) * 0.32f
        drawLine(Color(0xFFB45309).copy(alpha = 0.6f * alpha), Offset(center.x, center.y - r * 1.2f), Offset(center.x + cos(a + TAU / 2) * r * 0.9f, center.y - sin(a + TAU / 2) * r * 0.9f + r * 0.1f), 1.dp.toPx())
    }
}

private fun DrawScope.modaks(p: Float, sparks: List<Spark>) {
    val unit = 1.dp.toPx()
    val alpha = fadeOut(p)
    sparks.take(26).forEach { s ->
        val fall = frac(s.phase + p * 0.6f * s.speed)
        val at = Offset(size.width * (s.angle / TAU), size.height * fall)
        rotate(fall * 540f, at) { drawOval(if (s.color % 2 == 0) Color(0xFFF97316) else Color(0xFFFBBF24), Offset(at.x - 5 * unit, at.y - 3 * unit), Size(10 * unit, 6 * unit), alpha = alpha * 0.9f) }
    }
    repeat(7) { i ->
        val fx = 0.14f + i * 0.12f
        val rise = smooth((p - i * 0.05f) / 0.75f)
        val x = size.width * fx + sin(p * 6f + i) * 10 * unit
        val y = size.height + 50 * unit - (size.height + 100 * unit) * rise
        val r = (22f + (i % 3) * 5f) * unit
        rotate(sin(p * 5f + i) * 12f, Offset(x, y)) { modak(Offset(x, y), r, alpha) }
    }
}

private fun DrawScope.kite(p: Float, sparks: List<Spark>, colors: List<Color>) {
    val unit = 1.dp.toPx()
    val t = smooth(p / 0.85f)
    val x = size.width * (0.25f + 0.5f * t) + sin(p * 8f) * 14 * unit
    val y = size.height + 80 * unit - (size.height + 160 * unit) * t
    val alpha = fadeOut(p, 0.85f)
    val w = 46 * unit
    val h = 64 * unit
    drawLine(Color.White.copy(alpha = 0.45f * alpha), Offset(size.width * 0.5f, size.height + 10 * unit), Offset(x, y + h * 0.4f), unit)
    val tail = Path().apply { moveTo(x, y + h * 0.4f) }
    for (k in 1..14) {
        val f = k / 14f
        tail.lineTo(x - f * 50 * unit + sin(p * 16f + k * 0.9f) * 16 * unit * f, y + h * 0.4f + f * 170 * unit)
    }
    drawPath(tail, colors[2 % colors.size].copy(alpha = 0.8f * alpha), style = Stroke(1.5f * unit))
    repeat(5) { k ->
        val f = (k + 1) / 5.5f
        val at = Offset(x - f * 50 * unit + sin(p * 16f + (k * 2.8f + 1) * 0.9f) * 16 * unit * f, y + h * 0.4f + f * 170 * unit)
        rotate(k * 40f + p * 200f, at) { drawRect(colors[k % colors.size].copy(alpha = alpha), Offset(at.x - 6 * unit, at.y - 2.5f * unit), Size(12 * unit, 5 * unit)) }
    }
    drawPath(Path().apply { moveTo(x, y - h * 0.6f); lineTo(x + w / 2, y); lineTo(x, y + h * 0.4f); close() }, colors[0].copy(alpha = alpha))
    drawPath(Path().apply { moveTo(x, y - h * 0.6f); lineTo(x - w / 2, y); lineTo(x, y + h * 0.4f); close() }, colors[1 % colors.size].copy(alpha = alpha))
    drawLine(Color.White.copy(alpha = 0.7f * alpha), Offset(x, y - h * 0.6f), Offset(x, y + h * 0.4f), unit)
    drawLine(Color.White.copy(alpha = 0.7f * alpha), Offset(x - w / 2, y), Offset(x + w / 2, y), unit)
    sparks.take(20).forEach { s ->
        val life = frac(s.phase + p * 1.5f)
        drawCircle(Color.White.copy(alpha = (1f - life) * 0.8f * alpha), 2 * unit, Offset(x + (s.angle / TAU - 0.5f) * 80 * unit, y + life * 60 * unit))
    }
}

private fun DrawScope.tiranga(p: Float, sparks: List<Spark>) {
    val unit = 1.dp.toPx()
    val alpha = fadeOut(p, 0.75f)
    val bands = listOf(Color(0xFFFF9933), Color.White, Color(0xFF138808))
    drawRect(Brush.verticalGradient(listOf(Color(0x33FF9933), Color.Transparent, Color(0x33138808))), alpha = alpha * (1f - p))
    sparks.forEach { s ->
        val fall = frac(s.phase + p * 0.9f * s.speed)
        val at = Offset(size.width * (s.angle / TAU) + sin(p * 9f + s.phase * 6f) * 24 * unit, size.height * fall)
        rotate(fall * 720f * s.speed, at) { drawRect(bands[s.color % 3].copy(alpha = alpha), Offset(at.x - 6 * unit, at.y - 3.5f * unit), Size(12 * unit, 7 * unit)) }
    }
}

private fun DrawScope.rakhi(p: Float, sparks: List<Spark>) {
    val unit = 1.dp.toPx()
    val alpha = fadeOut(p)
    sparks.forEach { s ->
        val fall = frac(s.phase + p * 0.5f * s.speed)
        val at = Offset(size.width * (s.angle / TAU) + sin(p * 6f + s.phase * 5f) * 18 * unit, size.height * fall)
        rotate(fall * 400f, at) { drawOval(if (s.color % 2 == 0) Color(0xFFF9A8D4) else Color(0xFFFBCFE8), Offset(at.x - 6 * unit, at.y - 3.5f * unit), Size(12 * unit, 7 * unit), alpha = alpha * 0.9f) }
    }
    val t = smooth(p / 0.8f)
    val c = Offset(size.width / 2 + sin(p * 5f) * 12 * unit, size.height + 60 * unit - (size.height * 0.7f + 60 * unit) * t)
    val r = 30 * unit
    val thread = Color(0xFFDC2626)
    drawLine(thread.copy(alpha = alpha), Offset(0f, c.y + sin(p * 4f) * 20 * unit), Offset(c.x - r, c.y), 3 * unit)
    drawLine(thread.copy(alpha = alpha), Offset(c.x + r, c.y), Offset(size.width, c.y - sin(p * 4f) * 20 * unit), 3 * unit)
    repeat(12) { k ->
        val a = k * TAU / 12 + p * 2f
        drawCircle(Color(0xFFF59E0B).copy(alpha = alpha), 6 * unit, Offset(c.x + cos(a) * r, c.y + sin(a) * r))
    }
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFDE68A), Color(0xFFDC2626)), c, r * 0.85f), r * 0.85f, c, alpha = alpha)
    repeat(8) { k ->
        val a = k * TAU / 8 - p * 3f
        drawCircle(Color(0xFFFBCFE8).copy(alpha = alpha), 4 * unit, Offset(c.x + cos(a) * r * 0.5f, c.y + sin(a) * r * 0.5f))
    }
    drawCircle(Color(0xFF7C2D12).copy(alpha = alpha), 5 * unit, c)
}

private fun DrawScope.dandiya(p: Float, sparks: List<Spark>, colors: List<Color>) {
    val unit = 1.dp.toPx()
    val alpha = fadeOut(p)
    sparks.take(30).forEach { s ->
        val tw = 0.4f + 0.6f * sin(p * 25f + s.phase * TAU).coerceAtLeast(0f)
        drawCircle(colors[s.color].copy(alpha = alpha * tw), (3f + 2f * s.speed) * unit, Offset(size.width * (s.angle / TAU), size.height * (0.06f + 0.14f * s.phase)))
    }
    val t = smooth(p / 0.8f)
    val c = Offset(size.width / 2, size.height + 90 * unit - (size.height * 0.62f + 90 * unit) * t)
    val len = 150 * unit
    listOf(-1f, 1f).forEachIndexed { i, side ->
        rotate(side * (28f + 12f * sin(p * 9f)) + p * 180f * side, c) {
            drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFFACC15), Color(0xFFDC2626), Color(0xFF16A34A), Color(0xFFFACC15)), c.y - len / 2, c.y + len / 2), Offset(c.x - 5 * unit, c.y - len / 2), Size(10 * unit, len), CornerRadius(5 * unit), alpha = alpha)
            repeat(3) { k ->
                val ty = c.y - len / 2 + k * 10 * unit
                drawLine(colors[(i + k) % colors.size].copy(alpha = alpha), Offset(c.x + 5 * unit, ty), Offset(c.x + (26 + k * 8) * unit + sin(p * 14f + k) * 8 * unit, ty + (10 + k * 6) * unit), 2 * unit)
            }
        }
    }
    val clash = frac(p * 4f)
    if (clash < 0.25f) sparks.drop(30).take(16).forEach { s ->
        val r = clash * 4f * 60 * unit * s.speed
        drawCircle(Color(0xFFFFE08A).copy(alpha = alpha * (1f - clash * 4f)), 2.5f * unit, Offset(c.x + cos(s.angle) * r, c.y - len / 2 + sin(s.angle) * r))
    }
}

private fun DrawScope.snow(p: Float, sparks: List<Spark>) {
    val unit = 1.dp.toPx()
    val alpha = fadeOut(p, 0.8f)
    sparks.forEach { s ->
        val fall = frac(s.phase + p * 0.55f * s.speed)
        val at = Offset(size.width * (s.angle / TAU) + sin(p * 5f + s.phase * 6f) * 22 * unit, size.height * fall)
        drawCircle(Color.White.copy(alpha = alpha * (0.5f + 0.5f * s.speed)), (2f + 3f * s.speed) * unit, at)
    }
    listOf(0.3f, 0.7f).forEachIndexed { i, fx ->
        val top = Offset(size.width * fx, -4 * unit)
        val swing = sin(p * 10f + i * 2f) * 0.35f
        val c = Offset(top.x + sin(swing) * 70 * unit, top.y + cos(swing) * 70 * unit)
        drawLine(Color(0xFFDC2626).copy(alpha = alpha), top, c, 3 * unit)
        rotate(swing * 57f, c) {
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFDE68A), Color(0xFFD4A017), Color(0xFF92400E)), Offset(c.x - 6 * unit, c.y - 6 * unit), 30 * unit), 24 * unit, c, alpha = alpha)
            drawRect(Color(0xFF92400E).copy(alpha = alpha), Offset(c.x - 2 * unit, c.y + 16 * unit), Size(4 * unit, 10 * unit))
            drawCircle(Color(0xFF92400E).copy(alpha = alpha), 4 * unit, Offset(c.x, c.y + 26 * unit))
            drawPath(Path().apply { moveTo(c.x - 10 * unit, c.y - 26 * unit); lineTo(c.x + 10 * unit, c.y - 26 * unit); lineTo(c.x, c.y - 14 * unit); close() }, Color(0xFF16A34A).copy(alpha = alpha))
        }
    }
}
