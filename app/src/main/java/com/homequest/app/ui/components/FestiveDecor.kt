package com.homequest.app.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homequest.app.R
import com.homequest.app.domain.FestivalPack
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class Decor { DIYAS, TORAN, MARIGOLD, KITES, BUNTING, FAIRY_LIGHTS, STARS, GULAL }

private fun FestivalPack.decor(): Decor = when (this) {
    FestivalPack.DIWALI -> Decor.DIYAS
    FestivalPack.NEW_YEAR -> Decor.STARS
    FestivalPack.HOLI -> Decor.GULAL
    FestivalPack.CHRISTMAS -> Decor.FAIRY_LIGHTS
    FestivalPack.UTTARAYAN -> Decor.KITES
    FestivalPack.TIRANGA -> Decor.BUNTING
    FestivalPack.RAKHI, FestivalPack.NAVRATRI -> Decor.TORAN
    FestivalPack.GANESH -> Decor.MARIGOLD
}

@Composable
fun FestivalEmblem(pack: FestivalPack, size: Dp, modifier: Modifier = Modifier) {
    if (pack == FestivalPack.GANESH) Image(painterResource(R.drawable.ic_ganesh), contentDescription = null, modifier = modifier.size(size))
    else Text(pack.emoji, fontSize = (size.value * 0.82f).sp, modifier = modifier)
}

private class Hanger(val x: Float, val length: Float, val phase: Float, val color: Int, val speed: Float)

private val TRICOLOUR = listOf(Color(0xFFFF9933), Color.White, Color(0xFF138808))
private val BULBS = listOf(Color(0xFFEF4444), Color(0xFF22C55E), Color(0xFFFACC15), Color(0xFF3B82F6))
private val GOLD = Color(0xFFF5B301)
private val ROPE = Color(0xFF7C2D12)
private val LEAF = Color(0xFF16A34A)

@Composable
fun FestiveBackdrop(pack: FestivalPack, modifier: Modifier = Modifier, inset: Dp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()) {
    val decor = pack.decor()
    val colors = pack.look().colors
    val scheme = MaterialTheme.colorScheme
    val seconds by produceState(0f) {
        val start = withInfiniteAnimationFrameNanos { it }
        while (true) withInfiniteAnimationFrameNanos { value = (it - start) / 1_000_000_000f }
    }
    val hangers = remember(pack) {
        val r = Random(decor.ordinal * 17 + 5)
        List(9) { Hanger((it + 0.5f) / 9 + (r.nextFloat() - 0.5f) * 0.04f, r.nextFloat(), r.nextFloat() * TAU, r.nextInt(colors.size), 0.5f + r.nextFloat() * 0.5f) }
    }
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(scheme.primary.copy(alpha = 0.24f), scheme.tertiary.copy(alpha = 0.1f), Color.Transparent)))
        val top = inset.toPx()
        val t = seconds
        when (decor) {
            Decor.DIYAS -> {
                garland(top, colors, t, GOLD)
                hangers.forEachIndexed { i, h -> hanging(top, h, t, GOLD) { at -> if (i % 2 == 0 && i in 2..6) kandil(at, 14.dp.toPx(), t + h.phase) else bead(at, 5.dp.toPx(), colors[h.color], 1f) } }
            }
            Decor.STARS -> {
                garland(top, listOf(Color(0xFFE5E7EB)), t, Color(0xFFE5E7EB))
                hangers.forEach { h -> hanging(top, h, t, Color(0xFFE5E7EB)) { at -> star(at, (7f + 4f * h.length).dp.toPx(), colors[h.color].copy(alpha = 0.6f + 0.4f * sin(t * 2.4f + h.phase))) } }
            }
            Decor.TORAN -> toran(top, colors, t, leaves = false)
            Decor.MARIGOLD -> toran(top, colors, t, leaves = true)
            Decor.KITES -> hangers.take(5).forEach { kite(top, it, colors, t) }
            Decor.BUNTING -> repeat(2) { row -> bunting(top + row * 16.dp.toPx(), row, t) }
            Decor.FAIRY_LIGHTS -> {
                garland(top, BULBS, t, GOLD)
                garland(top + 14.dp.toPx(), BULBS.reversed(), t + 1f, GOLD)
                snow(hangers, t)
            }
            Decor.GULAL -> hangers.take(7).forEach { h ->
                val center = Offset((h.x + 0.05f * sin(t * 0.4f + h.phase)) * size.width, top + (h.length * 70f).dp.toPx() + 6.dp.toPx() * cos(t * 0.5f + h.phase))
                val radius = size.width * 0.26f
                drawCircle(Brush.radialGradient(listOf(colors[h.color].copy(alpha = 0.45f), Color.Transparent), center, radius), radius, center)
            }
        }
    }
}

private fun sag(xn: Float, top: Float, depth: Float) = top + depth * 4f * xn * (1f - xn)

private fun DrawScope.rope(top: Float, depth: Float, color: Color, width: Float) {
    val path = Path().apply {
        moveTo(0f, top)
        for (i in 1..40) { val xn = i / 40f; lineTo(xn * size.width, sag(xn, top, depth)) }
    }
    drawPath(path, color, style = Stroke(width))
}

private fun DrawScope.garland(top: Float, colors: List<Color>, t: Float, wire: Color) {
    val y = top + 2.dp.toPx()
    val depth = 10.dp.toPx()
    rope(y, depth, wire.copy(alpha = 0.6f), 1.2.dp.toPx())
    val count = 15
    repeat(count) { i ->
        val xn = (i + 0.5f) / count
        bead(Offset(xn * size.width, sag(xn, y, depth) + 3.dp.toPx()), 4.dp.toPx(), colors[i % colors.size], 0.5f + 0.5f * sin(t * 4f + i * 1.7f))
    }
}

private fun DrawScope.hanging(top: Float, h: Hanger, t: Float, wire: Color, end: DrawScope.(Offset) -> Unit) {
    val angle = sin(t * 1.3f + h.phase) * 0.06f
    val length = (12f + 18f * h.length).dp.toPx()
    val from = Offset(h.x * size.width, top)
    val to = Offset(from.x + sin(angle) * length, top + length)
    drawLine(wire.copy(alpha = 0.7f), from, to, 1.dp.toPx())
    end(to)
}

private fun DrawScope.bead(at: Offset, r: Float, color: Color, glow: Float) {
    val alpha = glow.coerceIn(0.15f, 1f)
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.8f * alpha), Color.Transparent), at, r * 2.6f), r * 2.6f, at)
    val diamond = Path().apply { moveTo(at.x, at.y - r); lineTo(at.x + r * 0.8f, at.y); lineTo(at.x, at.y + r); lineTo(at.x - r * 0.8f, at.y); close() }
    drawPath(diamond, color.copy(alpha = alpha))
}

private fun DrawScope.kandil(at: Offset, s: Float, t: Float) = translate(at.x, at.y) {
    val lamp = Offset(0f, s * 0.7f)
    drawCircle(Brush.radialGradient(listOf(Color(0xAAFFB74D), Color.Transparent), lamp, s * 1.8f), s * 1.8f, lamp)
    val body = Path().apply {
        moveTo(0f, 0f); lineTo(s * 0.7f, s * 0.45f); lineTo(s * 0.55f, s * 1.15f); lineTo(0f, s * 1.45f); lineTo(-s * 0.55f, s * 1.15f); lineTo(-s * 0.7f, s * 0.45f); close()
    }
    drawPath(body, Brush.verticalGradient(listOf(Color(0xFFFDE68A), Color(0xFFF97316), Color(0xFFB91C1C)), 0f, s * 1.5f))
    drawPath(body, Color(0xFFFDE68A), style = Stroke(1.dp.toPx()))
    drawOval(Color(0xFFFFF7D6).copy(alpha = 0.6f + 0.4f * sin(t * 5f)), Offset(-s * 0.18f, s * 0.45f), Size(s * 0.36f, s * 0.6f))
    for (i in -2..2) drawLine(Color(0xFFFACC15), Offset(i * s * 0.12f, s * 1.45f), Offset(i * s * 0.17f, s * 2.05f + sin(t * 3f + i) * s * 0.1f), 1.dp.toPx())
}

private fun DrawScope.star(at: Offset, r: Float, color: Color) {
    val path = Path().apply {
        repeat(10) { i ->
            val a = -TAU / 4 + i * TAU / 10
            val radius = if (i % 2 == 0) r else r * 0.45f
            val p = Offset(at.x + cos(a) * radius, at.y + r + sin(a) * radius)
            if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
        }
        close()
    }
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = color.alpha * 0.5f), Color.Transparent), Offset(at.x, at.y + r), r * 2.2f), r * 2.2f, Offset(at.x, at.y + r))
    drawPath(path, color)
}

private fun DrawScope.toran(top: Float, colors: List<Color>, t: Float, leaves: Boolean) {
    val y = top + 1.dp.toPx()
    val depth = 8.dp.toPx()
    rope(y, depth, ROPE, 1.4.dp.toPx())
    val count = 16
    val flower = 3.5.dp.toPx()
    repeat(count) { i ->
        val xn = (i + 0.5f) / count
        val x = xn * size.width
        val base = sag(xn, y, depth)
        val sway = sin(t * 1.6f + i) * 2.dp.toPx()
        repeat(3) { k -> drawCircle(colors[(i + k) % colors.size], flower, Offset(x + sway * k / 2f, base + flower * 1.6f + k * flower * 2.2f)) }
        val tip = base + flower * 8.4f
        val drop = Path().apply { moveTo(x - flower * 1.4f + sway, tip); lineTo(x + flower * 1.4f + sway, tip); lineTo(x + sway * 1.5f, tip + flower * 2.8f); close() }
        drawPath(drop, colors[i % colors.size])
        if (leaves) {
            val leaf = Offset((i + 1f) / count * size.width, sag((i + 1f) / count, y, depth) + flower)
            rotate(35f, leaf) { drawOval(LEAF, Offset(leaf.x - flower * 2.4f, leaf.y - flower), Size(flower * 4.8f, flower * 2f)) }
        }
    }
}

private fun DrawScope.kite(top: Float, h: Hanger, colors: List<Color>, t: Float) {
    val q = frac(t * 0.04f * h.speed + h.phase / TAU)
    val x = -0.1f * size.width + q * 1.2f * size.width
    val y = top + (10f + 46f * h.length).dp.toPx() + sin(t * 1.2f + h.phase) * 4.dp.toPx()
    val s = 11.dp.toPx()
    val color = colors[h.color]
    rotate(sin(t * 2f + h.phase) * 14f, Offset(x, y)) {
        drawPath(Path().apply { moveTo(x, y - s); lineTo(x + s * 0.75f, y); lineTo(x, y + s); lineTo(x - s * 0.75f, y); close() }, color)
        drawLine(Color.White.copy(alpha = 0.65f), Offset(x, y - s), Offset(x, y + s), 0.8.dp.toPx())
        drawLine(Color.White.copy(alpha = 0.65f), Offset(x - s * 0.75f, y), Offset(x + s * 0.75f, y), 0.8.dp.toPx())
        drawPath(Path().apply { moveTo(x, y + s); quadraticTo(x - s * 0.5f, y + s * 1.6f, x + s * 0.3f, y + s * 2.4f) }, color, style = Stroke(1.dp.toPx()))
    }
    drawLine(Color.White.copy(alpha = 0.3f), Offset(x, y + s), Offset(x - size.width * 0.3f, size.height * 0.7f), 0.7.dp.toPx())
}

private fun DrawScope.bunting(top: Float, row: Int, t: Float) {
    val depth = 10.dp.toPx()
    rope(top, depth, Color.White.copy(alpha = 0.7f), 1.dp.toPx())
    val count = 14
    val half = 7.dp.toPx()
    repeat(count) { i ->
        val xn = (i + 0.5f + row * 0.5f) / count
        if (xn > 1f) return@repeat
        val x = xn * size.width
        val y = sag(xn, top, depth)
        val sway = sin(t * 2f + i) * 2.dp.toPx()
        val flag = Path().apply { moveTo(x - half, y); lineTo(x + half, y); lineTo(x + sway, y + half * 2.6f); close() }
        drawPath(flag, TRICOLOUR[(i + row) % TRICOLOUR.size])
        drawPath(flag, Color.Black.copy(alpha = 0.08f), style = Stroke(0.6.dp.toPx()))
    }
}

private fun DrawScope.snow(hangers: List<Hanger>, t: Float) {
    repeat(4) { layer ->
        hangers.forEachIndexed { i, h ->
            val q = frac(t * 0.06f * h.speed + h.length + layer * 0.27f)
            val x = frac(h.x + layer * 0.31f) * size.width + sin(t + h.phase + i) * 6.dp.toPx()
            drawCircle(Color.White.copy(alpha = 0.55f + 0.4f * h.speed - 0.2f), (1.6f + 1.6f * h.speed).dp.toPx(), Offset(x, q * size.height * 0.9f))
        }
    }
}
