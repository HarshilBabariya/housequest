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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.homequest.app.domain.FestivalPack
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

enum class MomentKind { LOGGED, SWITCHED, TAPPED }

data class FestiveMoment(val pack: FestivalPack, val kind: MomentKind, val id: Int)

private enum class Show(val millis: Int) { ROCKET(2800), GULAL(2000), BURST(1800) }

private fun FestiveMoment.show(): Show = when {
    pack == FestivalPack.HOLI -> Show.GULAL
    kind != MomentKind.LOGGED || pack == FestivalPack.DIWALI || pack == FestivalPack.NEW_YEAR -> Show.ROCKET
    else -> Show.BURST
}

private class Spark(val angle: Float, val speed: Float, val color: Int, val phase: Float)

private const val ROCKET_RISE = 0.68f
private const val GARLAND_HEIGHT = 170f

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
            Show.GULAL -> gulal(p, sparks, colors)
            Show.BURST -> burst(p, sparks, colors)
        }
    }
}

private fun DrawScope.rocket(p: Float, sparks: List<Spark>, colors: List<Color>) {
    val unit = 1.dp.toPx()
    val x = size.width / 2
    val rise = (p / ROCKET_RISE).coerceAtMost(1f)
    val garlandTop = size.height + 20 * unit - (size.height + (GARLAND_HEIGHT + 40) * unit) * rise.pow(1.7f)
    val fadeOut = ((1f - p) / (1f - ROCKET_RISE)).coerceIn(0f, 1f)
    val beamAlpha = (rise / 0.25f).coerceAtMost(1f) * fadeOut
    val beamTop = garlandTop + GARLAND_HEIGHT * unit * 0.4f
    val half = 44 * unit
    drawRect(
        Brush.horizontalGradient(listOf(Color.Transparent, Color(0xFFFFF4C2).copy(alpha = 0.55f * beamAlpha), Color(0xFFFFF4C2).copy(alpha = 0.55f * beamAlpha), Color.Transparent), x - half, x + half),
        Offset(x - half, beamTop.coerceAtLeast(0f)),
        Size(half * 2, (size.height - beamTop.coerceAtLeast(0f)).coerceAtLeast(0f))
    )
    drawRect(
        Brush.verticalGradient(listOf(Color.Transparent, Color(0xFFFFE08A).copy(alpha = 0.25f * beamAlpha)), beamTop.coerceAtLeast(0f), size.height),
        Offset(x - half * 0.4f, beamTop.coerceAtLeast(0f)),
        Size(half * 0.8f, (size.height - beamTop.coerceAtLeast(0f)).coerceAtLeast(0f))
    )
    sparks.forEach { s ->
        val along = frac(s.phase + p * 0.35f * s.speed)
        val sy = beamTop + (size.height - beamTop) * along
        if (sy < beamTop) return@forEach
        val sx = x + (s.angle / TAU - 0.5f) * half * 1.6f
        val twinkle = 0.45f + 0.55f * sin(p * 40f + s.phase * TAU).coerceAtLeast(0f)
        val r = (2.5f + 3.5f * s.speed) * unit
        drawPath(Path().apply { moveTo(sx, sy - r); lineTo(sx + r * 0.55f, sy); lineTo(sx, sy + r); lineTo(sx - r * 0.55f, sy); close() }, colors[s.color].copy(alpha = beamAlpha * twinkle * (1f - along)))
    }
    if (rise < 1f) garland(x, garlandTop, unit, colors)
}

private fun DrawScope.garland(x: Float, top: Float, unit: Float, colors: List<Color>) {
    val gold = Color(0xFFFACC15)
    val leaf = Color(0xFF15803D)
    drawCircle(Brush.radialGradient(listOf(Color(0xAAFFF3B0), Color.Transparent), Offset(x, top + 80 * unit), 90 * unit), 90 * unit, Offset(x, top + 80 * unit))
    listOf(-16f, 0f, 16f).forEachIndexed { i, dx ->
        val lift = if (i == 1) 22f else 16f
        drawPath(Path().apply { moveTo(x, top + 24 * unit); lineTo(x + dx * unit, top + (24 - lift) * unit); lineTo(x + dx * 0.4f * unit, top + 12 * unit); close() }, leaf)
    }
    drawRect(gold, Offset(x - 18 * unit, top + 22 * unit), Size(36 * unit, 6 * unit))
    repeat(4) { k ->
        val cy = top + (44 + k * 30) * unit
        val base = colors[k % 2]
        val light = Color(0xFFFFF7D6).copy(alpha = 0.9f)
        val dark = Color(0xFF9A3412).copy(alpha = 0.5f)
        drawCircle(Brush.radialGradient(listOf(light, base, dark), Offset(x - 4 * unit, cy - 5 * unit), 18 * unit), 16 * unit, Offset(x, cy))
        repeat(10) { i ->
            val a = i * TAU / 10
            drawCircle(base.copy(alpha = 0.75f), 4.2f * unit, Offset(x + cos(a) * 11 * unit, cy + sin(a) * 11 * unit))
        }
        drawCircle(gold.copy(alpha = 0.9f), 3 * unit, Offset(x, cy))
        if (k < 3) drawRect(gold, Offset(x - 12 * unit, cy + 13 * unit), Size(24 * unit, 4 * unit))
    }
    val bottom = top + (44 + 3 * 30 + 16) * unit
    drawRect(gold, Offset(x - 16 * unit, bottom), Size(32 * unit, 5 * unit))
    drawPath(Path().apply { moveTo(x - 7 * unit, bottom + 5 * unit); lineTo(x + 7 * unit, bottom + 5 * unit); lineTo(x, bottom + 20 * unit); close() }, Color(0xFFDC2626))
}

private fun DrawScope.burst(e: Float, sparks: List<Spark>, colors: List<Color>) {
    val center = Offset(size.width / 2, size.height * 0.3f)
    val reach = size.minDimension * 0.42f
    val eased = 1f - (1f - e).pow(3)
    val alpha = (1f - e).pow(1.3f)
    sparks.forEach { s ->
        val r = reach * s.speed * eased
        val at = Offset(center.x + cos(s.angle) * r, center.y + sin(s.angle) * r + e * e * 90.dp.toPx())
        val color = colors[s.color].copy(alpha = alpha)
        rotate(e * 720f * s.speed + s.phase * 360f, at) { drawRect(color, Offset(at.x - 4.dp.toPx(), at.y - 2.5.dp.toPx()), Size(8.dp.toPx(), 5.dp.toPx())) }
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
