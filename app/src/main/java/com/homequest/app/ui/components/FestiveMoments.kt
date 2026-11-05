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

enum class MomentKind { LOGGED, SWITCHED }

data class FestiveMoment(val pack: FestivalPack, val kind: MomentKind, val id: Int)

private enum class Show(val millis: Int) { ROCKET(2400), CHAKRI(3000), GULAL(2000), BURST(1800) }

private fun FestiveMoment.show(): Show = when {
    pack == FestivalPack.HOLI -> Show.GULAL
    pack == FestivalPack.DIWALI && kind == MomentKind.SWITCHED -> Show.CHAKRI
    (pack == FestivalPack.DIWALI || pack == FestivalPack.NEW_YEAR) && kind == MomentKind.LOGGED -> Show.ROCKET
    else -> Show.BURST
}

private class Spark(val angle: Float, val speed: Float, val color: Int, val phase: Float)

private const val ROCKET_RISE = 1.1f / 2.4f

@Composable
fun FestiveMomentOverlay(moment: FestiveMoment, onDone: () -> Unit) {
    val show = moment.show()
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
            Show.CHAKRI -> chakri(p, sparks)
            Show.GULAL -> gulal(p, sparks, colors)
            Show.BURST -> burst(Offset(size.width / 2, size.height * 0.3f), p, sparks, colors, confetti = true)
        }
    }
}

private fun DrawScope.rocket(p: Float, sparks: List<Spark>, colors: List<Color>) {
    val rise = (p / ROCKET_RISE).coerceAtMost(1f)
    val top = Offset(size.width / 2, size.height * 0.25f)
    val y = size.height * 0.95f - (size.height * 0.95f - top.y) * (1f - (1f - rise).pow(2))
    val x = top.x + sin(rise * 6f) * 6.dp.toPx()
    if (rise < 1f) {
        sparks.take(14).forEachIndexed { i, s ->
            val trail = Offset(x + (s.phase - 0.5f) * 10.dp.toPx(), y + (26f + i * 6f).dp.toPx())
            drawCircle(colors[s.color].copy(alpha = (1f - i / 14f) * 0.9f), (3f - i * 0.15f).dp.toPx(), trail)
        }
        val unit = 1.dp.toPx()
        drawPath(Path().apply { moveTo(x, y - 30 * unit); lineTo(x + 10 * unit, y - 14 * unit); lineTo(x - 10 * unit, y - 14 * unit); close() }, Color(0xFF16A34A))
        drawRect(Brush.verticalGradient(listOf(Color(0xFFFDBA74), Color(0xFFEA580C)), y - 14 * unit, y + 24 * unit), Offset(x - 9 * unit, y - 14 * unit), Size(18 * unit, 38 * unit))
        repeat(4) { k -> drawCircle(Color(0xFFFDE68A), 3.5f * unit, Offset(x, y - 6 * unit + k * 9 * unit)) }
        drawCircle(Brush.radialGradient(listOf(Color(0xCCFFC107), Color.Transparent), Offset(x, y + 28 * unit), 18 * unit), 18 * unit, Offset(x, y + 28 * unit))
    } else {
        burst(top, (p - ROCKET_RISE) / (1f - ROCKET_RISE), sparks, colors, confetti = false)
    }
}

private fun DrawScope.burst(center: Offset, e: Float, sparks: List<Spark>, colors: List<Color>, confetti: Boolean) {
    val reach = size.minDimension * 0.42f
    val eased = 1f - (1f - e).pow(3)
    val alpha = (1f - e).pow(1.3f)
    if (!confetti) drawCircle(Brush.radialGradient(listOf(colors[0].copy(alpha = 0.35f * alpha), Color.Transparent), center, reach), reach, center)
    sparks.forEach { s ->
        val r = reach * s.speed * eased
        val at = Offset(center.x + cos(s.angle) * r, center.y + sin(s.angle) * r + e * e * 90.dp.toPx())
        val color = colors[s.color].copy(alpha = alpha)
        if (confetti) {
            rotate(e * 720f * s.speed + s.phase * 360f, at) { drawRect(color, Offset(at.x - 4.dp.toPx(), at.y - 2.5.dp.toPx()), Size(8.dp.toPx(), 5.dp.toPx())) }
        } else {
            val tail = Offset(center.x + cos(s.angle) * r * 0.8f, center.y + sin(s.angle) * r * 0.8f + e * e * 70.dp.toPx())
            drawLine(color.copy(alpha = alpha * 0.5f), tail, at, 1.5.dp.toPx())
            drawCircle(color, (3.2f - 1.6f * e).dp.toPx(), at)
        }
    }
}

private fun DrawScope.chakri(p: Float, sparks: List<Spark>) {
    val center = Offset(size.width / 2, size.height * 0.36f)
    val seconds = p * Show.CHAKRI.millis / 1000f
    val spin = seconds * 14f
    val alpha = ((1f - p) / 0.17f).coerceIn(0f, 1f)
    val grow = (p / 0.07f).coerceAtMost(1f)
    val rim = 44.dp.toPx() * grow
    listOf(Color(0xFFDC2626), Color(0xFFFACC15), Color(0xFF2563EB), Color(0xFFFACC15), Color(0xFFDC2626)).forEachIndexed { i, c ->
        drawCircle(c.copy(alpha = alpha), (40f - i * 8f).dp.toPx() * grow, center, style = Stroke(7.dp.toPx() * grow))
    }
    drawCircle(Color(0xFF1E3A8A).copy(alpha = alpha), 8.dp.toPx() * grow, center)
    val sparkColors = listOf(Color(0xFFFFF59D), Color(0xFFFFC107), Color.White)
    sparks.forEach { s ->
        val life = frac(seconds * 2f + s.phase)
        val a = spin - life * 2.2f + s.angle
        val from = Offset(center.x + cos(a) * rim, center.y + sin(a) * rim)
        val fly = life * 120.dp.toPx() * s.speed
        val at = Offset(from.x - sin(a) * fly, from.y + cos(a) * fly)
        drawCircle(sparkColors[s.color % sparkColors.size].copy(alpha = alpha * (1f - life)), 2.4.dp.toPx(), at)
    }
}

private fun DrawScope.gulal(p: Float, sparks: List<Spark>, colors: List<Color>) {
    val origin = Offset(size.width / 2, size.height * 0.55f)
    val eased = 1f - (1f - p).pow(2)
    val alpha = (1f - p).pow(1.2f)
    colors.forEachIndexed { i, c ->
        val a = i * TAU / colors.size + 0.4f
        val center = Offset(origin.x + cos(a) * size.width * 0.32f * eased, origin.y + sin(a) * size.height * 0.22f * eased)
        val radius = size.width * (0.12f + 0.34f * eased)
        drawCircle(Brush.radialGradient(listOf(c.copy(alpha = 0.55f * alpha), Color.Transparent), center, radius), radius, center)
    }
    sparks.forEach { s ->
        val r = size.minDimension * 0.5f * s.speed * eased
        drawCircle(colors[s.color].copy(alpha = alpha), (2.8f - 1.4f * p).dp.toPx(), Offset(origin.x + cos(s.angle) * r, origin.y + sin(s.angle) * r))
    }
}
