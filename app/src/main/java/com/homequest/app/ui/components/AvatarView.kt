package com.homequest.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.homequest.app.domain.Accessories
import com.homequest.app.domain.AvatarConfig
import com.homequest.app.domain.HairColors
import com.homequest.app.domain.HairStyles
import com.homequest.app.domain.Mood
import com.homequest.app.domain.Outfits
import com.homequest.app.domain.SkinTones
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun AvatarView(
    config: AvatarConfig,
    size: Dp = 96.dp,
    animated: Boolean = false,
    mood: Mood = Mood.HAPPY,
    waveTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "avatar")
    val breath by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1700), RepeatMode.Reverse), label = "breath")
    val sway by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(2800), RepeatMode.Reverse), label = "sway")
    val jitter by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(90), RepeatMode.Reverse), label = "jitter")

    val blink = remember { Animatable(1f) }
    val wave = remember { Animatable(0f) }
    LaunchedEffect(animated) {
        while (animated) {
            delay(2400L + Random.nextLong(2600))
            blink.animateTo(0.06f, tween(70))
            blink.animateTo(1f, tween(100))
        }
    }
    LaunchedEffect(waveTrigger, animated) {
        if (animated && waveTrigger > 0) {
            wave.snapTo(0f)
            wave.animateTo(1f, tween(260))
            repeat(3) {
                wave.animateTo(0.78f, tween(160))
                wave.animateTo(1f, tween(160))
            }
            wave.animateTo(0f, tween(300))
        }
    }

    val bob = if (animated) breath else 0f
    val tilt = if (animated) (if (mood == Mood.ANGRY) jitter * 1.3f else sway * 2.2f) else 0f
    val shiver = if (animated && mood == Mood.WORRIED) jitter * 0.6f else 0f

    Canvas(modifier.size(size)) {
        val u = this.size.width / 100f
        translate(left = shiver * u, top = bob * 2f * u) {
            rotate(tilt, pivot = Offset(50f * u, 100f * u)) {
                drawAvatar(config, mood, u, blink.value, wave.value, breath)
            }
        }
    }
}

private class Pen(private val u: Float) {
    val path = Path()
    fun m(x: Float, y: Float) = apply { path.moveTo(x * u, y * u) }
    fun l(x: Float, y: Float) = apply { path.lineTo(x * u, y * u) }
    fun q(cx: Float, cy: Float, x: Float, y: Float) = apply { path.quadraticTo(cx * u, cy * u, x * u, y * u) }
    fun c(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) = apply { path.cubicTo(x1 * u, y1 * u, x2 * u, y2 * u, x * u, y * u) }
    fun z() = apply { path.close() }
}

private fun pen(u: Float, build: Pen.() -> Unit): Path = Pen(u).apply(build).path

private val Outline = Color(0x8C281914)
private val Ink = Color(0xFF1F2937)
private val Lip = Color(0xFF7F1D1D)

private class Palette(val skin: Color, val skinShade: Color, val hair: Color, val hairDark: Color, val hairLight: Color, val cloth: Color, val accent: Color)

private fun DrawScope.drawAvatar(config: AvatarConfig, mood: Mood, u: Float, eyeOpen: Float, wave: Float, breath: Float) {
    val skin = SkinTones.getOrElse(config.skinTone) { SkinTones.first() }
    val hair = HairColors.getOrElse(config.hairColor) { HairColors.first() }
    val (cloth, accent) = outfitColors(config.outfitId)
    val p = Palette(skin, skin.darken(0.16f), hair, hair.darken(0.28f), hair.lighten(0.3f), cloth, accent)
    val outline = Stroke(1f * u)

    drawHairBack(config.hairId, p, u, outline)
    drawBody(config.outfitId, p, u, outline)
    if (wave > 0f) {
        rotate(-115f * wave, pivot = Offset(74f * u, 70f * u)) {
            drawLine(p.cloth, Offset(74f * u, 70f * u), Offset(78f * u, 92f * u), 11f * u, StrokeCap.Round)
            drawCircle(p.skin, 6.5f * u, Offset(79f * u, 94f * u))
        }
    }
    listOf(25.5f, 74.5f).forEach { x ->
        drawOval(p.skin, Offset((x - 4.8f) * u, 37.2f * u), Size(9.6f * u, 11.6f * u))
        drawOval(Outline, Offset((x - 4.8f) * u, 37.2f * u), Size(9.6f * u, 11.6f * u), style = outline)
        drawOval(p.skinShade, Offset((x - 2.2f) * u, 40f * u), Size(4.4f * u, 6f * u))
    }
    drawFace(mood, p, u, eyeOpen, breath, config.hairColor)
    drawHairFront(config.hairId, p, u, outline)
    drawAccessory(config.accessoryId, u)
}

private fun outfitColors(id: String): Pair<Color, Color> = when (id) {
    Outfits.HOODIE -> Color(0xFF64748B) to Color(0xFF334155)
    Outfits.BLAZER -> Color(0xFF1E293B) to Color(0xFFF8FAFC)
    Outfits.KURTA -> Color(0xFFF97316) to Color(0xFFFDE68A)
    Outfits.SPACESUIT -> Color(0xFFE5E7EB) to Color(0xFFF97316)
    Outfits.HERO -> Color(0xFFDC2626) to Color(0xFFFACC15)
    Outfits.DENIM -> Color(0xFF3B5BA9) to Color(0xFF8FB3E3)
    Outfits.SHIRT -> Color(0xFFFFFFFF) to Color(0xFF111827)
    Outfits.SWEATER -> Color(0xFF9F1239) to Color(0xFFFBCFE8)
    Outfits.DRESS -> Color(0xFF10B981) to Color(0xFFFDE68A)
    Outfits.SAREE -> Color(0xFFC026D3) to Color(0xFFFBBF24)
    Outfits.SHERWANI -> Color(0xFF1F2937) to Color(0xFFD4AF37)
    else -> Color(0xFF3B82F6) to Color(0xFF1D4ED8)
}

private fun DrawScope.drawBody(id: String, p: Palette, u: Float, outline: Stroke) {
    val torso = pen(u) { m(19f, 100f).l(19f, 86f).c(19f, 73f, 31f, 68f, 40f, 66f).l(60f, 66f).c(69f, 68f, 81f, 73f, 81f, 86f).l(81f, 100f).z() }
    val base = if (id == Outfits.SAREE) Color(0xFF7E22CE) else p.cloth
    drawPath(torso, base)
    drawPath(pen(u) { m(19f, 100f).l(19f, 86f).c(19f, 76f, 26f, 71f, 34f, 68f).l(34f, 100f).z() }, base.darken(0.12f))
    drawPath(torso, if (id == Outfits.SHIRT) Color(0xFFE5E7EB) else Outline, style = outline)
    drawPath(pen(u) { m(43f, 55f).l(57f, 55f).l(57f, 68f).q(50f, 73f, 43f, 68f).z() }, p.skinShade)
    when (id) {
        Outfits.HOODIE -> {
            drawPath(pen(u) { m(36f, 66f).q(50f, 81f, 64f, 66f).l(62f, 73f).q(50f, 86f, 38f, 73f).z() }, p.accent)
            drawLine(Color(0xFFE5E7EB), Offset(45f * u, 73f * u), Offset(44f * u, 90f * u), 1.8f * u, StrokeCap.Round)
            drawLine(Color(0xFFE5E7EB), Offset(55f * u, 73f * u), Offset(56f * u, 90f * u), 1.8f * u, StrokeCap.Round)
            drawRoundRect(p.accent, Offset(34f * u, 88f * u), Size(32f * u, 12f * u), CornerRadius(4f * u))
        }
        Outfits.BLAZER -> {
            drawPath(pen(u) { m(40f, 66f).l(50f, 84f).l(60f, 66f).z() }, p.accent)
            val lapel = p.cloth.darken(0.35f)
            drawPath(pen(u) { m(40f, 66f).l(36f, 82f).l(50f, 100f).l(50f, 84f).z() }, lapel)
            drawPath(pen(u) { m(60f, 66f).l(64f, 82f).l(50f, 100f).l(50f, 84f).z() }, lapel)
            drawPath(pen(u) { m(48f, 68f).l(52f, 68f).l(53f, 86f).l(50f, 92f).l(47f, 86f).z() }, Color(0xFFB91C1C))
            drawCircle(Color(0xFFD4AF37), 1.2f * u, Offset(50f * u, 94f * u))
        }
        Outfits.KURTA -> {
            drawPath(pen(u) { m(42f, 66f).l(58f, 66f).l(58f, 70f).q(50f, 75f, 42f, 70f).z() }, p.accent)
            drawRect(p.accent, Offset(48.5f * u, 70f * u), Size(3f * u, 24f * u))
            listOf(76f, 82f, 88f).forEach { y -> drawCircle(p.cloth.darken(0.4f), 1.2f * u, Offset(50f * u, y * u)) }
            drawLine(p.accent.copy(alpha = 0.6f), Offset(19f * u, 92f * u), Offset(81f * u, 92f * u), 1.2f * u)
        }
        Outfits.SPACESUIT -> {
            drawRect(p.accent, Offset(19f * u, 86f * u), Size(62f * u, 4f * u))
            drawCircle(p.accent, 6f * u, Offset(50f * u, 78f * u))
            drawCircle(Color(0xFF1E293B), 3.5f * u, Offset(50f * u, 78f * u))
            drawPath(pen(u) { m(19f, 84f).q(50f, 62f, 81f, 84f) }, Color(0xFF9CA3AF), style = Stroke(3f * u))
            drawRoundRect(Color(0xFF9CA3AF), Offset(26f * u, 92f * u), Size(8f * u, 5f * u), CornerRadius(1f * u))
        }
        Outfits.HERO -> {
            drawPath(pen(u) { m(50f, 72f).l(58f, 78f).l(55f, 88f).l(45f, 88f).l(42f, 78f).z() }, p.accent)
            drawPath(pen(u) { m(19f, 100f).l(19f, 86f).q(13f, 93f, 15f, 100f).z() }, Color(0xFF991B1B))
            drawPath(pen(u) { m(81f, 100f).l(81f, 86f).q(87f, 93f, 85f, 100f).z() }, Color(0xFF991B1B))
            drawPath(pen(u) { m(40f, 66f).q(50f, 72f, 60f, 66f) }, p.accent, style = Stroke(2f * u))
        }
        Outfits.DENIM -> {
            drawPath(pen(u) { m(46f, 70f).l(54f, 70f).l(54f, 100f).l(46f, 100f).z() }, Color(0xFFF3F4F6))
            drawPath(pen(u) { m(40f, 66f).l(46f, 100f).l(34f, 100f).l(30f, 72f).z() }, p.accent.copy(alpha = 0.9f))
            drawPath(pen(u) { m(60f, 66f).l(54f, 100f).l(66f, 100f).l(70f, 72f).z() }, p.accent.copy(alpha = 0.9f))
            drawPath(pen(u) { m(40f, 66f).l(36f, 76f).l(30f, 72f).z() }, p.cloth.darken(0.2f))
            drawPath(pen(u) { m(60f, 66f).l(64f, 76f).l(70f, 72f).z() }, p.cloth.darken(0.2f))
            listOf(78f, 86f, 94f).forEach { y -> drawCircle(Color(0xFFD1D5DB), 1.2f * u, Offset(43f * u, y * u)) }
        }
        Outfits.SHIRT -> {
            drawPath(pen(u) { m(40f, 66f).l(50f, 76f).l(60f, 66f).l(62f, 70f).l(50f, 82f).l(38f, 70f).z() }, Color(0xFFE5E7EB))
            drawLine(Color(0xFFE5E7EB), Offset(50f * u, 76f * u), Offset(50f * u, 100f * u), 1.5f * u)
            listOf(84f, 92f).forEach { y -> drawCircle(p.accent, 1.2f * u, Offset(50f * u, y * u)) }
            drawRoundRect(Color(0xFFE5E7EB), Offset(60f * u, 80f * u), Size(10f * u, 9f * u), CornerRadius(1f * u), style = Stroke(1f * u))
        }
        Outfits.SWEATER -> {
            drawPath(pen(u) { m(38f, 66f).q(50f, 78f, 62f, 66f).l(62f, 70f).q(50f, 83f, 38f, 70f).z() }, p.accent)
            drawRect(p.cloth.darken(0.2f), Offset(19f * u, 94f * u), Size(62f * u, 6f * u))
            listOf(80f, 88f).forEach { y ->
                drawPath(pen(u) { m(24f, y).q(30f, y - 4f, 36f, y).q(42f, y + 4f, 48f, y).q(54f, y - 4f, 60f, y).q(66f, y + 4f, 72f, y) }, p.cloth.darken(0.25f), style = Stroke(1.4f * u))
            }
        }
        Outfits.DRESS -> {
            drawPath(pen(u) { m(36f, 66f).q(50f, 72f, 64f, 66f).l(66f, 74f).q(50f, 80f, 34f, 74f).z() }, p.accent)
            listOf(30f, 42f, 54f, 66f).forEach { x -> drawCircle(p.accent.copy(alpha = 0.8f), 2f * u, Offset(x * u, 90f * u)) }
        }
        Outfits.SAREE -> {
            drawPath(pen(u) { m(62f, 66f).c(78f, 70f, 82f, 86f, 68f, 100f).l(54f, 100f).c(64f, 88f, 60f, 76f, 52f, 70f).z() }, p.cloth)
            drawPath(pen(u) { m(62f, 66f).c(78f, 70f, 82f, 86f, 68f, 100f) }, p.accent, style = Stroke(2.5f * u))
            drawPath(pen(u) { m(40f, 66f).q(50f, 74f, 60f, 66f) }, p.accent, style = Stroke(2f * u))
            listOf(66f to 78f, 70f to 88f, 62f to 94f).forEach { (x, y) -> drawCircle(p.accent, 1.3f * u, Offset(x * u, y * u)) }
        }
        Outfits.SHERWANI -> {
            drawPath(pen(u) { m(42f, 66f).l(58f, 66f).l(58f, 70f).q(50f, 74f, 42f, 70f).z() }, p.accent)
            drawLine(p.accent, Offset(50f * u, 70f * u), Offset(50f * u, 100f * u), 1.5f * u)
            listOf(76f, 83f, 90f, 97f).forEach { y -> drawCircle(p.accent, 1.4f * u, Offset(50f * u, y * u)) }
            drawPath(pen(u) { m(28f, 76f).q(32f, 73f, 36f, 76f) }, p.accent, style = Stroke(1.2f * u))
            drawPath(pen(u) { m(64f, 76f).q(68f, 73f, 72f, 76f) }, p.accent, style = Stroke(1.2f * u))
        }
        else -> drawPath(pen(u) { m(41f, 66f).q(50f, 75f, 59f, 66f) }, p.accent, style = Stroke(2.4f * u))
    }
}

private fun DrawScope.drawFace(mood: Mood, p: Palette, u: Float, eyeOpen: Float, breath: Float, hairColorIndex: Int) {
    val face = pen(u) {
        m(26f, 38f).c(26f, 12f, 74f, 12f, 74f, 38f).c(74f, 52f, 67f, 62f, 57f, 64.5f).q(50f, 66f, 43f, 64.5f).c(33f, 62f, 26f, 52f, 26f, 38f).z()
    }
    drawPath(face, p.skin)
    drawPath(face, Outline, style = Stroke(1.1f * u))

    val blush = when (mood) { Mood.ANGRY -> 0.8f; Mood.HAPPY -> 0.5f; else -> 0.3f }
    drawOval(Color(0xFFF28B82).copy(alpha = blush), Offset(31f * u, 47f * u), Size(10f * u, 6f * u))
    drawOval(Color(0xFFF28B82).copy(alpha = blush), Offset(59f * u, 47f * u), Size(10f * u, 6f * u))

    val iris = if (hairColorIndex <= 2) p.hair.darken(0.1f) else Color(0xFF3F5F7A)
    listOf(40.5f to -1f, 59.5f to 1f).forEach { (x, d) ->
        scale(scaleX = 1f, scaleY = eyeOpen, pivot = Offset(x * u, 41.5f * u)) {
            drawPath(pen(u) { m(x - 7f, 41.5f).q(x, 33.5f, x + 7f, 41.5f).q(x, 48.5f, x - 7f, 41.5f).z() }, Color.White)
            drawCircle(iris, 4.3f * u, Offset((x + 0.6f * d) * u, 42f * u))
            drawCircle(Ink, 2.4f * u, Offset((x + 0.6f * d) * u, 42f * u))
            drawCircle(Color.White, 1.4f * u, Offset((x - 1.4f) * u, 40.2f * u))
            drawCircle(Color.White, 0.7f * u, Offset((x + 1.8f) * u, 43.8f * u))
            drawPath(pen(u) { m(x - 7f, 41.5f).q(x, 48.5f, x + 7f, 41.5f) }, Outline, style = Stroke(0.9f * u))
        }
        drawPath(pen(u) { m(x - 7.2f, 41.5f).q(x, 32.8f, x + 7.2f, 41.5f) }, Ink, style = Stroke(2f * u, cap = StrokeCap.Round))
        drawLine(Ink, Offset((x + 6.8f * d) * u, 40f * u), Offset((x + 8.3f * d) * u, 38.8f * u), 1.3f * u, StrokeCap.Round)
    }

    when (mood) {
        Mood.ANGRY -> {
            drawPath(pen(u) { m(32f, 29.5f).q(40f, 30f, 48f, 34f).q(40f, 34.5f, 32f, 32.5f).z() }, p.hairDark)
            drawPath(pen(u) { m(68f, 29.5f).q(60f, 30f, 52f, 34f).q(60f, 34.5f, 68f, 32.5f).z() }, p.hairDark)
        }
        Mood.WORRIED -> {
            drawPath(pen(u) { m(32f, 32f).q(40f, 25f, 48f, 29f).q(40f, 29f, 32f, 35f).z() }, p.hairDark)
            drawPath(pen(u) { m(68f, 32f).q(60f, 25f, 52f, 29f).q(60f, 29f, 68f, 35f).z() }, p.hairDark)
        }
        else -> {
            drawPath(pen(u) { m(32.5f, 30.5f).q(40f, 25.5f, 48f, 28.5f).q(40f, 29f, 32.5f, 33.5f).z() }, p.hairDark)
            drawPath(pen(u) { m(67.5f, 30.5f).q(60f, 25.5f, 52f, 28.5f).q(60f, 29f, 67.5f, 33.5f).z() }, p.hairDark)
        }
    }

    drawPath(pen(u) { m(47.5f, 50f).q(50f, 52.5f, 52.5f, 50f) }, p.skin.darken(0.3f), style = Stroke(1.6f * u, cap = StrokeCap.Round))

    val mouth = Stroke(2.4f * u, cap = StrokeCap.Round)
    when (mood) {
        Mood.HAPPY -> {
            val smile = pen(u) { m(40.5f, 54f).q(50f, 64.5f, 59.5f, 54f).q(50f, 57f, 40.5f, 54f).z() }
            drawPath(smile, Lip)
            drawPath(smile, Outline, style = Stroke(0.9f * u))
            drawPath(pen(u) { m(43f, 55.2f).q(50f, 59.5f, 57f, 55.2f).q(50f, 57.2f, 43f, 55.2f).z() }, Color.White)
        }
        Mood.NEUTRAL -> drawPath(pen(u) { m(43.5f, 56.5f).q(50f, 57.5f, 56.5f, 56.5f) }, Lip, style = mouth)
        Mood.WORRIED -> {
            drawPath(pen(u) { m(43f, 57.5f).q(46.5f, 54.5f, 50f, 57.5f).q(53.5f, 60.5f, 57f, 57.5f) }, Lip, style = mouth)
            val dy = breath * 3f
            drawPath(pen(u) { m(77f, 20f + dy).q(81f, 27f + dy, 77f, 29f + dy).q(73f, 27f + dy, 77f, 20f + dy).z() }, Color(0xFF60A5FA))
        }
        Mood.ANGRY -> drawPath(pen(u) { m(43f, 59f).q(50f, 52.5f, 57f, 59f) }, Lip, style = mouth)
    }
}

private fun DrawScope.drawHairBack(id: String, p: Palette, u: Float, outline: Stroke) {
    fun fill(path: Path) { drawPath(path, p.hair); drawPath(path, Outline, style = outline) }
    when (id) {
        HairStyles.LONG -> {
            fill(pen(u) { m(23f, 42f).c(21f, 8f, 79f, 8f, 77f, 42f).l(79f, 84f).q(50f, 92f, 21f, 84f).z() })
            drawPath(pen(u) { m(30f, 60f).q(31f, 78f, 27f, 84f) }, p.hairDark.copy(alpha = 0.5f), style = Stroke(2f * u))
            drawPath(pen(u) { m(70f, 60f).q(69f, 78f, 73f, 84f) }, p.hairDark.copy(alpha = 0.5f), style = Stroke(2f * u))
        }
        HairStyles.WAVY -> fill(pen(u) {
            m(23f, 42f).c(21f, 8f, 79f, 8f, 77f, 42f).c(80f, 56f, 70f, 62f, 76f, 72f).c(70f, 80f, 80f, 86f, 74f, 90f)
            q(50f, 96f, 26f, 90f).c(20f, 86f, 30f, 80f, 24f, 72f).c(30f, 62f, 20f, 56f, 23f, 42f).z()
        })
        HairStyles.PONYTAIL -> {
            fill(pen(u) { m(68f, 30f).c(84f, 30f, 92f, 46f, 86f, 66f).c(84f, 74f, 90f, 80f, 84f, 88f).c(78f, 80f, 82f, 74f, 80f, 66f).c(82f, 50f, 74f, 40f, 66f, 40f).z() })
            drawPath(pen(u) { m(80f, 66f).q(82f, 76f, 84f, 88f) }, p.hairDark.copy(alpha = 0.6f), style = Stroke(1.5f * u))
        }
        HairStyles.BOB -> fill(pen(u) { m(23f, 40f).c(21f, 8f, 79f, 8f, 77f, 40f).l(77f, 66f).q(70f, 72f, 62f, 70f).l(38f, 70f).q(30f, 72f, 23f, 66f).z() })
        HairStyles.CURLY -> listOf(
            Triple(26f, 24f, 8f), Triple(74f, 24f, 8f), Triple(23f, 38f, 8f), Triple(77f, 38f, 8f),
            Triple(26f, 52f, 7f), Triple(74f, 52f, 7f), Triple(34f, 13f, 8f), Triple(66f, 13f, 8f), Triple(50f, 9f, 9f)
        ).forEach { (x, y, r) ->
            drawCircle(p.hair, r * u, Offset(x * u, y * u))
            drawCircle(Outline, r * u, Offset(x * u, y * u), style = outline)
        }
        HairStyles.BRAIDS -> {
            listOf(30f, 70f).forEach { sx ->
                val braid = pen(u) { m(sx, 50f).q(sx + 4f, 60f, sx, 70f).q(sx - 4f, 80f, sx, 90f) }
                drawPath(braid, p.hair, style = Stroke(7f * u, cap = StrokeCap.Round))
                drawPath(braid, p.hairDark, style = Stroke(1.6f * u, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f * u, 4f * u))))
            }
            fill(pen(u) { m(23f, 42f).c(21f, 8f, 79f, 8f, 77f, 42f).l(77f, 52f).l(23f, 52f).z() })
        }
    }
}

private fun DrawScope.drawHairFront(id: String, p: Palette, u: Float, outline: Stroke) {
    fun fill(path: Path) { drawPath(path, p.hair); drawPath(path, Outline, style = outline) }
    val shine = Stroke(2.2f * u, cap = StrokeCap.Round)
    val cap = pen(u) { m(26f, 38f).c(25f, 10f, 75f, 10f, 74f, 38f).c(70f, 28f, 60f, 24f, 50f, 25f).c(40f, 24f, 30f, 28f, 26f, 38f).z() }
    val capShine = pen(u) { m(36f, 24f).q(44f, 18f, 54f, 19f) }
    when (id) {
        HairStyles.SHORT -> {
            fill(pen(u) { m(26f, 36f).c(26f, 10f, 46f, 6f, 58f, 9f).c(73f, 11f, 75f, 24f, 74f, 36f).c(70f, 28f, 62f, 24f, 54f, 25f).c(50f, 23f, 46f, 22f, 40f, 27f).c(33f, 30f, 30f, 32f, 26f, 36f).z() })
            drawPath(pen(u) { m(30f, 28f).q(40f, 16f, 54f, 15f) }, p.hairLight.copy(alpha = 0.55f), style = Stroke(2.4f * u, cap = StrokeCap.Round))
            drawPath(pen(u) { m(44f, 24f).q(52f, 21f, 60f, 24f) }, p.hairDark.copy(alpha = 0.5f), style = Stroke(1.4f * u, cap = StrokeCap.Round))
        }
        HairStyles.BUZZ -> {
            val buzz = pen(u) { m(26f, 34f).c(26f, 14f, 74f, 14f, 74f, 34f).c(66f, 27f, 58f, 25f, 50f, 25.5f).c(42f, 25f, 34f, 27f, 26f, 34f).z() }
            drawPath(buzz, p.hair.copy(alpha = 0.85f))
            drawPath(buzz, Outline, style = outline)
        }
        HairStyles.LONG, HairStyles.WAVY, HairStyles.PONYTAIL -> {
            fill(cap)
            drawPath(capShine, p.hairLight.copy(alpha = 0.55f), style = shine)
        }
        HairStyles.BOB -> {
            fill(pen(u) { m(26f, 38f).c(25f, 10f, 75f, 10f, 74f, 38f).c(70f, 24f, 58f, 22f, 50f, 28f).c(40f, 24f, 31f, 28f, 26f, 38f).z() })
            drawPath(pen(u) { m(34f, 22f).q(42f, 15f, 52f, 16f) }, p.hairLight.copy(alpha = 0.55f), style = shine)
        }
        HairStyles.CURLY -> {
            fill(pen(u) { m(26f, 36f).c(26f, 16f, 74f, 16f, 74f, 36f).c(66f, 28f, 56f, 26f, 50f, 27f).c(44f, 26f, 34f, 28f, 26f, 36f).z() })
            listOf(36f to 22f, 50f to 19f, 64f to 22f).forEach { (x, y) -> drawCircle(p.hairLight.copy(alpha = 0.35f), 5f * u, Offset(x * u, y * u)) }
        }
        HairStyles.BUN -> {
            fill(cap)
            drawCircle(p.hair, 9f * u, Offset(50f * u, 12f * u))
            drawCircle(Outline, 9f * u, Offset(50f * u, 12f * u), style = outline)
            drawCircle(p.hairLight.copy(alpha = 0.5f), 5f * u, Offset(50f * u, 12f * u), style = Stroke(1.5f * u))
            drawPath(capShine, p.hairLight.copy(alpha = 0.55f), style = shine)
        }
        HairStyles.BRAIDS -> {
            fill(cap)
            drawPath(pen(u) { m(34f, 22f).q(50f, 14f, 66f, 22f) }, p.hairDark.copy(alpha = 0.5f), style = Stroke(1.2f * u))
            drawPath(pen(u) { m(38f, 20f).q(50f, 12f, 62f, 20f) }, p.hairLight.copy(alpha = 0.5f), style = Stroke(1.6f * u))
        }
        HairStyles.MOHAWK -> {
            fill(pen(u) {
                m(30f, 30f).q(38f, 22f, 44f, 24f).l(45f, 10f).q(47f, 2f, 50f, 1f).q(53f, 2f, 55f, 10f).l(56f, 24f)
                q(62f, 22f, 70f, 30f).q(60f, 26f, 50f, 27f).q(40f, 26f, 30f, 30f).z()
            })
            drawLine(p.hairLight.copy(alpha = 0.5f), Offset(48f * u, 6f * u), Offset(48f * u, 22f * u), 1.6f * u)
        }
    }
}

private fun DrawScope.drawAccessory(id: String, u: Float) {
    when (id) {
        Accessories.GLASSES -> {
            val st = Stroke(1.8f * u)
            drawCircle(Ink, 7.2f * u, Offset(41.5f * u, 41f * u), style = st)
            drawCircle(Ink, 7.2f * u, Offset(58.5f * u, 41f * u), style = st)
            drawLine(Ink, Offset(48.7f * u, 41f * u), Offset(51.3f * u, 41f * u), 1.8f * u)
            drawLine(Ink, Offset(34.3f * u, 40f * u), Offset(29f * u, 38f * u), 1.8f * u)
            drawLine(Ink, Offset(65.7f * u, 40f * u), Offset(71f * u, 38f * u), 1.8f * u)
        }
        Accessories.CAP -> {
            drawPath(pen(u) { m(26f, 32f).c(26f, 12f, 74f, 12f, 74f, 32f).z() }, Color(0xFF2563EB))
            drawPath(pen(u) { m(26f, 32f).q(50f, 28f, 74f, 32f).l(74f, 36f).q(50f, 32f, 26f, 36f).z() }, Color(0xFF1D4ED8))
            drawPath(pen(u) { m(60f, 33f).l(84f, 34f).q(84f, 38f, 60f, 38f).z() }, Color(0xFF1D4ED8))
            drawLine(Color(0xFF1D4ED8), Offset(50f * u, 14f * u), Offset(50f * u, 30f * u), 1.2f * u)
        }
        Accessories.HEADPHONES -> {
            drawPath(pen(u) { m(26f, 40f).c(26f, 14f, 74f, 14f, 74f, 40f) }, Color(0xFF374151), style = Stroke(3.5f * u))
            drawRoundRect(Color(0xFFF97316), Offset(21f * u, 35f * u), Size(10f * u, 16f * u), CornerRadius(4f * u))
            drawRoundRect(Color(0xFFF97316), Offset(69f * u, 35f * u), Size(10f * u, 16f * u), CornerRadius(4f * u))
        }
        Accessories.CROWN -> {
            drawPath(pen(u) { m(32f, 24f).l(32f, 8f).l(41f, 16f).l(50f, 4f).l(59f, 16f).l(68f, 8f).l(68f, 24f).z() }, Color(0xFFFBBF24))
            drawCircle(Color(0xFFEF4444), 2.5f * u, Offset(50f * u, 18f * u))
            drawCircle(Color(0xFF3B82F6), 1.5f * u, Offset(40f * u, 20f * u))
            drawCircle(Color(0xFF3B82F6), 1.5f * u, Offset(60f * u, 20f * u))
        }
        Accessories.EARRINGS -> {
            drawCircle(Color(0xFFFBBF24), 2f * u, Offset(25.5f * u, 50f * u))
            drawCircle(Color(0xFFFBBF24), 2f * u, Offset(74.5f * u, 50f * u))
        }
        Accessories.BINDI -> drawCircle(Color(0xFFDC2626), 1.6f * u, Offset(50f * u, 30f * u))
    }
}

private fun Color.darken(amount: Float) = Color(red * (1 - amount), green * (1 - amount), blue * (1 - amount), alpha)
private fun Color.lighten(amount: Float) = Color(red + (1 - red) * amount, green + (1 - green) * amount, blue + (1 - blue) * amount, alpha)

fun moodFor(budgetProgress: Float, hasBudget: Boolean): Mood = when {
    !hasBudget || budgetProgress < 0.6f -> Mood.HAPPY
    budgetProgress < 0.85f -> Mood.NEUTRAL
    budgetProgress <= 1f -> Mood.WORRIED
    else -> Mood.ANGRY
}

fun moodLine(mood: Mood, name: String, hour: Int): String {
    val greeting = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Still up"
    }
    return when (mood) {
        Mood.HAPPY -> "$greeting, $name! Spending looks healthy this month."
        Mood.NEUTRAL -> "$greeting, $name. We're on track — keep an eye on it."
        Mood.WORRIED -> "Hmm, $name, we're close to the budget line…"
        Mood.ANGRY -> "$name! We've gone over budget this month."
    }
}
