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
private val JEWEL = listOf(Color(0xFF059669), Color(0xFF2563EB), Color(0xFFE11D48), Color(0xFFD97706))
private val NEON = listOf(Color(0xFF7C3AED), Color(0xFF0891B2), Color(0xFFDB2777), Color(0xFF65A30D))
private val AMBER = listOf(Color(0xFFB45309), Color(0xFFCA8A04), Color(0xFF9A3412), Color(0xFFEA580C))

const val DECOR_VARIANTS = 3

private enum class Shape { DIAMOND, ROUND, STAR }

private fun FestivalPack.decorColors(variant: Int): List<Color> = when (variant) {
    0 -> if (this == FestivalPack.CHRISTMAS) BULBS else look().colors
    1 -> if (this == FestivalPack.CHRISTMAS || this == FestivalPack.NAVRATRI) NEON else JEWEL
    else -> if (this == FestivalPack.CHRISTMAS || this == FestivalPack.NAVRATRI) AMBER else NEON
}
private val GOLD = Color(0xFFF5B301)
private val KANDIL = Color(0xFFF97316)
private val ROPE = Color(0xFF7C2D12)
private val LEAF = Color(0xFF16A34A)

@Composable
fun FestiveBackdrop(pack: FestivalPack, variant: Int, modifier: Modifier = Modifier) {
    val decor = pack.decor()
    val colors = pack.decorColors(variant)
    val shape = Shape.entries[variant % Shape.entries.size]
    val soft = Shape.entries[(variant + 1) % Shape.entries.size]
    val scheme = MaterialTheme.colorScheme
    val inset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val seconds by produceState(0f) {
        val start = withInfiniteAnimationFrameNanos { it }
        while (true) withInfiniteAnimationFrameNanos { value = (it - start) / 1_000_000_000f }
    }
    val hangers = remember(pack) {
        val r = Random(decor.ordinal * 17 + 5)
        List(9) { Hanger((it + 0.5f) / 9 + (r.nextFloat() - 0.5f) * 0.04f, r.nextFloat(), r.nextFloat() * TAU, r.nextInt(12), 0.5f + r.nextFloat() * 0.5f) }
    }
    Canvas(modifier) {
        drawRect(Brush.verticalGradient(listOf(scheme.primary.copy(alpha = 0.24f), scheme.tertiary.copy(alpha = 0.1f), Color.Transparent)))
        val top = inset.toPx()
        val t = seconds
        when (decor) {
            Decor.DIYAS -> {
                garland(top, colors, t, GOLD, shape)
                hangers.forEachIndexed { i, h -> hanging(top, h, t, GOLD) { at -> if (i % 2 == 0 && i in 2..6) lantern(at, 14.dp.toPx(), t + h.phase, shape, if (variant == 0) KANDIL else colors[h.color % colors.size]) else bead(at, 5.dp.toPx(), colors[h.color % colors.size], 1f, shape) } }
            }
            Decor.STARS -> {
                garland(top, listOf(Color(0xFFE5E7EB)), t, Color(0xFFE5E7EB), shape)
                hangers.forEach { h -> hanging(top, h, t, Color(0xFFE5E7EB)) { at -> bead(at, (6f + 3f * h.length).dp.toPx(), colors[h.color % colors.size], 0.6f + 0.4f * sin(t * 2.4f + h.phase), Shape.entries[(variant + 2) % Shape.entries.size]) } }
            }
            Decor.TORAN -> toran(top, colors, t, leaves = false, soft)
            Decor.MARIGOLD -> toran(top, colors, t, leaves = true, soft)
            Decor.KITES -> hangers.take(5).forEach { kite(top, it, colors, t, shape) }
            Decor.BUNTING -> repeat(2) { row -> bunting(top + row * 16.dp.toPx(), row, t, shape, if (variant == 0) TRICOLOUR else colors) }
            Decor.FAIRY_LIGHTS -> {
                garland(top, colors, t, GOLD, shape)
                garland(top + 14.dp.toPx(), colors.reversed(), t + 1f, GOLD, shape)
                snow(hangers, t, soft)
            }
            Decor.GULAL -> hangers.take(7).forEach { h ->
                val color = colors[h.color % colors.size]
                val center = Offset((h.x + 0.05f * sin(t * 0.4f + h.phase)) * size.width, top + (h.length * 70f).dp.toPx() + 6.dp.toPx() * cos(t * 0.5f + h.phase))
                val radius = size.width * 0.26f
                drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.45f), Color.Transparent), center, radius), radius, center)
                if (variant > 0) repeat(3) { k ->
                    val a = h.phase + k * TAU / 3 + t * 0.3f
                    piece(Offset(center.x + cos(a) * 22.dp.toPx(), center.y + sin(a) * 14.dp.toPx()), 5.dp.toPx(), color, shape)
                }
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

private fun DrawScope.garland(top: Float, colors: List<Color>, t: Float, wire: Color, shape: Shape) {
    val y = top + 2.dp.toPx()
    val depth = 10.dp.toPx()
    rope(y, depth, wire.copy(alpha = 0.6f), 1.2.dp.toPx())
    val count = 15
    repeat(count) { i ->
        val xn = (i + 0.5f) / count
        bead(Offset(xn * size.width, sag(xn, y, depth) + 3.dp.toPx()), 4.dp.toPx(), colors[i % colors.size], 0.5f + 0.5f * sin(t * 4f + i * 1.7f), shape)
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

private fun DrawScope.bead(at: Offset, r: Float, color: Color, glow: Float, shape: Shape) {
    val alpha = glow.coerceIn(0.15f, 1f)
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.8f * alpha), Color.Transparent), at, r * 2.6f), r * 2.6f, at)
    piece(at, r, color.copy(alpha = alpha), shape)
}

private fun DrawScope.piece(at: Offset, r: Float, color: Color, shape: Shape) = when (shape) {
    Shape.ROUND -> drawCircle(color, r * 0.9f, at)
    Shape.DIAMOND -> drawPath(Path().apply { moveTo(at.x, at.y - r); lineTo(at.x + r * 0.8f, at.y); lineTo(at.x, at.y + r); lineTo(at.x - r * 0.8f, at.y); close() }, color)
    Shape.STAR -> drawPath(starPath(at, r * 1.15f), color)
}

private fun starPath(center: Offset, r: Float) = Path().apply {
    repeat(10) { i ->
        val a = -TAU / 4 + i * TAU / 10
        val radius = if (i % 2 == 0) r else r * 0.45f
        val p = Offset(center.x + cos(a) * radius, center.y + sin(a) * radius)
        if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
    }
    close()
}

private fun DrawScope.lantern(at: Offset, s: Float, t: Float, shape: Shape, color: Color) = translate(at.x, at.y) {
    val lamp = Offset(0f, s * 0.7f)
    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.65f), Color.Transparent), lamp, s * 1.8f), s * 1.8f, lamp)
    val flicker = 0.6f + 0.4f * sin(t * 5f)
    when (shape) {
        Shape.DIAMOND -> {
            val body = Path().apply {
                moveTo(0f, 0f); lineTo(s * 0.7f, s * 0.45f); lineTo(s * 0.55f, s * 1.15f); lineTo(0f, s * 1.45f); lineTo(-s * 0.55f, s * 1.15f); lineTo(-s * 0.7f, s * 0.45f); close()
            }
            drawPath(body, Brush.verticalGradient(listOf(Color(0xFFFDE68A), color, Color(0xFFB91C1C)), 0f, s * 1.5f))
            drawPath(body, Color(0xFFFDE68A), style = Stroke(1.dp.toPx()))
            drawOval(Color(0xFFFFF7D6).copy(alpha = flicker), Offset(-s * 0.18f, s * 0.45f), Size(s * 0.36f, s * 0.6f))
        }
        Shape.ROUND -> {
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFF7D6), color, color.copy(alpha = 0.85f)), Offset(-s * 0.2f, s * 0.5f), s * 0.9f), s * 0.75f, lamp)
            for (i in -1..1) drawLine(Color.White.copy(alpha = 0.35f), Offset(i * s * 0.35f, s * 0.05f), Offset(i * s * 0.35f, s * 1.35f), 1.dp.toPx())
            drawCircle(Color(0xFFFFF7D6).copy(alpha = flicker), s * 0.2f, lamp)
        }
        Shape.STAR -> {
            drawPath(starPath(lamp, s * 0.95f), Brush.radialGradient(listOf(Color(0xFFFFF7D6), color), lamp, s))
            drawPath(starPath(lamp, s * 0.95f), Color(0xFFFDE68A), style = Stroke(1.dp.toPx()))
            drawCircle(Color.White.copy(alpha = flicker), s * 0.15f, lamp)
        }
    }
    for (i in -2..2) drawLine(Color(0xFFFACC15), Offset(i * s * 0.12f, s * 1.45f), Offset(i * s * 0.17f, s * 2.05f + sin(t * 3f + i) * s * 0.1f), 1.dp.toPx())
}

private fun DrawScope.toran(top: Float, colors: List<Color>, t: Float, leaves: Boolean, petal: Shape) {
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
        repeat(3) { k -> piece(Offset(x + sway * k / 2f, base + flower * 1.6f + k * flower * 2.2f), flower * 1.1f, colors[(i + k) % colors.size], petal) }
        val tip = base + flower * 8.4f
        val drop = Path().apply { moveTo(x - flower * 1.4f + sway, tip); lineTo(x + flower * 1.4f + sway, tip); lineTo(x + sway * 1.5f, tip + flower * 2.8f); close() }
        drawPath(drop, colors[i % colors.size])
        if (leaves) {
            val leaf = Offset((i + 1f) / count * size.width, sag((i + 1f) / count, y, depth) + flower)
            rotate(35f, leaf) { drawOval(LEAF, Offset(leaf.x - flower * 2.4f, leaf.y - flower), Size(flower * 4.8f, flower * 2f)) }
        }
    }
}

private fun DrawScope.kite(top: Float, h: Hanger, colors: List<Color>, t: Float, bow: Shape) {
    val q = frac(t * 0.04f * h.speed + h.phase / TAU)
    val x = -0.1f * size.width + q * 1.2f * size.width
    val y = top + (10f + 46f * h.length).dp.toPx() + sin(t * 1.2f + h.phase) * 4.dp.toPx()
    val s = 11.dp.toPx()
    val color = colors[h.color % colors.size]
    rotate(sin(t * 2f + h.phase) * 14f, Offset(x, y)) {
        piece(Offset(x, y), s, color, bow)
        drawLine(Color.White.copy(alpha = 0.65f), Offset(x, y - s), Offset(x, y + s), 0.8.dp.toPx())
        drawLine(Color.White.copy(alpha = 0.65f), Offset(x - s * 0.75f, y), Offset(x + s * 0.75f, y), 0.8.dp.toPx())
        drawPath(Path().apply { moveTo(x, y + s); quadraticTo(x - s * 0.5f, y + s * 1.6f, x + s * 0.3f, y + s * 2.4f) }, color, style = Stroke(1.dp.toPx()))
        repeat(3) { k -> piece(Offset(x - s * 0.25f + k * s * 0.25f, y + s * (1.4f + k * 0.45f)), s * 0.3f, colors[(h.color + k + 1) % colors.size], bow) }
    }
    drawLine(Color.White.copy(alpha = 0.3f), Offset(x, y + s), Offset(x - size.width * 0.3f, size.height * 0.7f), 0.7.dp.toPx())
}

private fun DrawScope.bunting(top: Float, row: Int, t: Float, flag: Shape, colors: List<Color>) {
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
        val path = Path().apply {
            moveTo(x - half, y); lineTo(x + half, y)
            when (flag) {
                Shape.DIAMOND -> lineTo(x + sway, y + half * 2.6f)
                Shape.ROUND -> { lineTo(x + half + sway, y + half * 2.2f); lineTo(x - half + sway, y + half * 2.2f) }
                Shape.STAR -> { lineTo(x + half + sway, y + half * 2.6f); lineTo(x + sway, y + half * 1.7f); lineTo(x - half + sway, y + half * 2.6f) }
            }
            close()
        }
        drawPath(path, colors[(i + row) % colors.size])
        drawPath(path, Color.Black.copy(alpha = 0.08f), style = Stroke(0.6.dp.toPx()))
    }
}

private fun DrawScope.snow(hangers: List<Hanger>, t: Float, flake: Shape) {
    repeat(4) { layer ->
        hangers.forEachIndexed { i, h ->
            val q = frac(t * 0.06f * h.speed + h.length + layer * 0.27f)
            val x = frac(h.x + layer * 0.31f) * size.width + sin(t + h.phase + i) * 6.dp.toPx()
            piece(Offset(x, q * size.height * 0.9f), (1.8f + 1.8f * h.speed).dp.toPx(), Color.White.copy(alpha = 0.55f + 0.4f * h.speed - 0.2f), flake)
        }
    }
}
