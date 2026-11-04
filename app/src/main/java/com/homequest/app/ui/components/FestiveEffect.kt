package com.homequest.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.Dp
import com.homequest.app.domain.FestivalPack
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

enum class EffectStyle(val count: Int) { FIREWORKS(6), POWDER(9), SNOW(50), KITES(6), CONFETTI(60), PETALS(34), LIGHTS(14) }

data class FestiveLook(val style: EffectStyle, val colors: List<Color>)

fun FestivalPack.look(): FestiveLook = when (this) {
    FestivalPack.DIWALI -> FestiveLook(EffectStyle.FIREWORKS, listOf(Color(0xFFFFC107), Color(0xFFFF7043), Color(0xFFFFF59D), Color(0xFFF06292)))
    FestivalPack.NEW_YEAR -> FestiveLook(EffectStyle.FIREWORKS, listOf(Color(0xFFF5C451), Color(0xFFE5E7EB), Color(0xFFC084FC), Color(0xFFF472B6)))
    FestivalPack.HOLI -> FestiveLook(EffectStyle.POWDER, listOf(Color(0xFFEC4899), Color(0xFFFACC15), Color(0xFF22C55E), Color(0xFF3B82F6), Color(0xFFF97316), Color(0xFFA855F7)))
    FestivalPack.CHRISTMAS -> FestiveLook(EffectStyle.SNOW, listOf(Color.White, Color(0xFFE0F2FE)))
    FestivalPack.UTTARAYAN -> FestiveLook(EffectStyle.KITES, listOf(Color(0xFFEF4444), Color(0xFFFACC15), Color(0xFF22C55E), Color(0xFF3B82F6), Color(0xFFEC4899)))
    FestivalPack.TIRANGA -> FestiveLook(EffectStyle.CONFETTI, listOf(Color(0xFFFF9933), Color.White, Color(0xFF138808), Color(0xFF1E3A8A)))
    FestivalPack.RAKHI -> FestiveLook(EffectStyle.PETALS, listOf(Color(0xFFF9A8D4), Color(0xFFFBCFE8), Color(0xFFFCD34D)))
    FestivalPack.GANESH -> FestiveLook(EffectStyle.PETALS, listOf(Color(0xFFF97316), Color(0xFFFBBF24), Color(0xFFDC2626)))
    FestivalPack.NAVRATRI -> FestiveLook(EffectStyle.LIGHTS, listOf(Color(0xFFDC2626), Color(0xFFF59E0B), Color(0xFF16A34A), Color(0xFF3B82F6), Color(0xFFEC4899)))
}

private class Mote(val x: Float, val y: Float, val phase: Float, val speed: Int, val size: Float, val color: Int)

private const val TAU = 2f * PI.toFloat()

private fun frac(v: Float) = v - floor(v)

@Composable
fun FestiveEffect(look: FestiveLook, modifier: Modifier = Modifier) {
    val clock by rememberInfiniteTransition(label = "festive").animateFloat(0f, 1f, infiniteRepeatable(tween(12_000, easing = LinearEasing)), label = "clock")
    val motes = remember(look) {
        val r = Random(look.style.ordinal * 31 + look.colors.size)
        List(look.style.count) { Mote(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1 + r.nextInt(2), 0.5f + r.nextFloat() * 0.5f, r.nextInt(look.colors.size)) }
    }
    Canvas(modifier.fillMaxSize()) {
        when (look.style) {
            EffectStyle.FIREWORKS -> fireworks(motes, look.colors, clock)
            EffectStyle.POWDER -> powder(motes, look.colors, clock)
            EffectStyle.SNOW -> falling(motes, look.colors, clock, sway = 14f) { m, c, at -> drawCircle(c.copy(alpha = 0.55f + 0.4f * m.size), (1.4f + 2.4f * m.size) * density, at) }
            EffectStyle.CONFETTI -> falling(motes, look.colors, clock, sway = 22f) { m, c, at ->
                rotate(clock * 720f * m.speed + m.phase * 360f, at) { drawRect(c, Offset(at.x - 4f * density, at.y - 2.5f * density), Size(8f * density * m.size + 3f * density, 5f * density)) }
            }
            EffectStyle.PETALS -> falling(motes, look.colors, clock, sway = 26f) { m, c, at ->
                rotate(sin((clock * m.speed + m.phase) * TAU) * 70f, at) { drawOval(c.copy(alpha = 0.9f), Offset(at.x - 5f * density, at.y - 2.5f * density), Size(10f * density * m.size + 2f * density, 5f * density)) }
            }
            EffectStyle.KITES -> kites(motes, look.colors, clock)
            EffectStyle.LIGHTS -> lights(motes, look.colors, clock)
        }
    }
}

private fun DrawScope.falling(motes: List<Mote>, colors: List<Color>, t: Float, sway: Float, draw: DrawScope.(Mote, Color, Offset) -> Unit) {
    motes.forEach { m ->
        val q = frac(t * m.speed * 2 + m.phase)
        val y = -12f * density + q * (size.height + 24f * density)
        val x = m.x * size.width + sin((q * 2 + m.phase) * TAU) * sway * density * m.size
        draw(m, colors[m.color], Offset(x, y))
    }
}

private fun DrawScope.fireworks(motes: List<Mote>, colors: List<Color>, t: Float) {
    motes.forEach { m ->
        val q = frac(t * m.speed * 3 + m.phase)
        val center = Offset((0.12f + 0.76f * m.x) * size.width, (0.12f + 0.45f * m.y) * size.height)
        val color = colors[m.color]
        if (q < 0.22f) {
            val p = 1f - (1f - q / 0.22f).pow(2)
            val y = size.height - (size.height - center.y) * p
            drawCircle(color, 2.2f * density, Offset(center.x, y))
            drawLine(color.copy(alpha = 0.35f), Offset(center.x, y), Offset(center.x, y + 18f * density), 1.5f * density)
            return@forEach
        }
        val e = (q - 0.22f) / 0.78f
        val radius = (36f + 44f * m.size) * density * (1f - (1f - e).pow(3))
        val alpha = (1f - e).pow(1.5f)
        val drop = e * e * 26f * density
        repeat(16) { i ->
            val a = i / 16f * TAU + m.phase
            val tip = Offset(center.x + cos(a) * radius, center.y + sin(a) * radius + drop)
            val tail = Offset(center.x + cos(a) * radius * 0.72f, center.y + sin(a) * radius * 0.72f + drop * 0.7f)
            drawLine(color.copy(alpha = alpha * 0.45f), tail, tip, 1.4f * density)
            drawCircle(colors[(m.color + i) % colors.size].copy(alpha = alpha), 2.4f * density * (1f - e * 0.5f), tip)
        }
        drawCircle(Brush.radialGradient(listOf(color.copy(alpha = alpha * 0.35f), Color.Transparent), center, radius.coerceAtLeast(1f)), radius.coerceAtLeast(1f), center)
    }
}

private fun DrawScope.powder(motes: List<Mote>, colors: List<Color>, t: Float) {
    motes.forEach { m ->
        val a = (t * m.speed + m.phase) * TAU
        val center = Offset(size.width * (m.x + 0.08f * sin(a)), size.height * (m.y + 0.07f * cos(a)))
        val radius = size.width * (0.16f + 0.14f * m.size)
        drawCircle(Brush.radialGradient(listOf(colors[m.color].copy(alpha = 0.42f), Color.Transparent), center, radius), radius, center)
        val q = frac(t * 4 * m.speed + m.phase)
        val splash = Offset(size.width * frac(m.x + 0.37f), size.height * frac(m.y + 0.61f))
        repeat(7) { i ->
            val angle = i / 7f * TAU + m.phase * TAU
            val r = q * (18f + 22f * m.size) * density
            drawCircle(colors[(m.color + i) % colors.size].copy(alpha = (1f - q) * 0.8f), (2.8f - q * 1.6f) * density, Offset(splash.x + cos(angle) * r, splash.y + sin(angle) * r))
        }
    }
}

private fun DrawScope.kites(motes: List<Mote>, colors: List<Color>, t: Float) {
    motes.forEach { m ->
        val q = frac(t * m.speed + m.phase)
        val x = -0.15f * size.width + q * 1.3f * size.width
        val y = size.height * (0.12f + 0.62f * m.y) + sin((q * 2 + m.phase) * TAU) * size.height * 0.06f
        val s = (12f + 10f * m.size) * density
        val color = colors[m.color]
        rotate(sin((q * 3 + m.phase) * TAU) * 14f, Offset(x, y)) {
            val diamond = Path().apply {
                moveTo(x, y - s); lineTo(x + s * 0.75f, y); lineTo(x, y + s); lineTo(x - s * 0.75f, y); close()
            }
            drawPath(diamond, color)
            drawLine(Color.White.copy(alpha = 0.6f), Offset(x, y - s), Offset(x, y + s), 0.8f * density)
            drawLine(Color.White.copy(alpha = 0.6f), Offset(x - s * 0.75f, y), Offset(x + s * 0.75f, y), 0.8f * density)
            val tail = Path().apply {
                moveTo(x, y + s)
                quadraticTo(x - s * 0.5f, y + s * 1.6f, x + s * 0.2f, y + s * 2.2f)
            }
            drawPath(tail, color.copy(alpha = 0.8f), style = Stroke(1.2f * density))
        }
        drawLine(Color.White.copy(alpha = 0.25f), Offset(x, y + s), Offset(x - size.width * 0.3f, size.height), 0.7f * density)
    }
}

private fun DrawScope.lights(motes: List<Mote>, colors: List<Color>, t: Float) {
    listOf(0.04f to 0.12f, 0.0f to 0.2f).forEachIndexed { strand, (top, sag) ->
        val bulbs = 13
        repeat(bulbs) { i ->
            val xn = (i + 0.5f * strand) / (bulbs - 1)
            if (xn > 1f) return@repeat
            val at = Offset(xn * size.width, size.height * (top + sag * 4f * xn * (1f - xn)))
            val twinkle = 0.45f + 0.55f * (0.5f + 0.5f * sin((t * 8 + i / 4f + strand * 0.5f) * TAU))
            val c = colors[(i + strand) % colors.size]
            drawCircle(Brush.radialGradient(listOf(c.copy(alpha = 0.55f * twinkle), Color.Transparent), at, 12f * density), 12f * density, at)
            drawCircle(c.copy(alpha = twinkle), 3.4f * density, at)
        }
    }
    motes.forEach { m ->
        val q = frac(t * m.speed * 2 + m.phase)
        val at = Offset(m.x * size.width + sin((q + m.phase) * TAU) * 10f * density, size.height * (1f - q))
        val c = colors[m.color]
        val r = (2f + 3f * m.size) * density * (1f - q * 0.6f)
        drawLine(c.copy(alpha = 1f - q), Offset(at.x - r, at.y), Offset(at.x + r, at.y), 1.2f * density)
        drawLine(c.copy(alpha = 1f - q), Offset(at.x, at.y - r), Offset(at.x, at.y + r), 1.2f * density)
    }
}

@Composable
fun StreakFlame(size: Dp, lit: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "flame")
    val flicker by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "flicker")
    val sway by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(1300), RepeatMode.Reverse), label = "sway")
    Canvas(modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        fun flame(scale: Float, lean: Float): Path {
            fun p(x: Float, y: Float) = Offset(w * (0.5f + (x - 0.5f) * scale), h * (0.94f + (y - 0.94f) * scale))
            fun Path.curve(c1: Offset, c2: Offset, end: Offset) = cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
            val tip = p(0.5f + lean, 0.06f)
            return Path().apply {
                moveTo(tip.x, tip.y)
                curve(p(0.62f, 0.3f), p(0.88f, 0.48f), p(0.86f, 0.66f))
                curve(p(0.84f, 0.86f), p(0.66f, 0.95f), p(0.5f, 0.94f))
                curve(p(0.34f, 0.95f), p(0.16f, 0.86f), p(0.14f, 0.66f))
                curve(p(0.12f, 0.46f), p(0.38f, 0.32f), tip)
                close()
            }
        }
        val outer = if (lit) listOf(Color(0xFFFFB300), Color(0xFFFF6D00), Color(0xFFE53935)) else listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E), Color(0xFF757575))
        val inner = if (lit) listOf(Color(0xFFFFFDE7), Color(0xFFFFEE58)) else listOf(Color(0xFFF5F5F5), Color(0xFFE0E0E0))
        if (lit) drawCircle(Brush.radialGradient(listOf(Color(0xFFFF9100).copy(alpha = 0.35f + 0.15f * flicker), Color.Transparent), Offset(w / 2, h * 0.62f), w * 0.6f), w * 0.6f, Offset(w / 2, h * 0.62f))
        drawPath(flame(0.96f + 0.04f * flicker, 0.05f * sway), Brush.verticalGradient(outer, startY = 0f, endY = h))
        drawPath(flame(0.56f + 0.05f * flicker, -0.04f * sway), Brush.verticalGradient(inner, startY = h * 0.35f, endY = h))
    }
}
