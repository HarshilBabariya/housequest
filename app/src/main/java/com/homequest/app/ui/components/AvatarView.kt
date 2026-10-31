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
    val breath by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "breath")
    val sway by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(2600), RepeatMode.Reverse), label = "sway")
    val jitter by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(90), RepeatMode.Reverse), label = "jitter")

    val blink = remember { Animatable(1f) }
    val wave = remember { Animatable(0f) }
    LaunchedEffect(animated) {
        while (animated) {
            delay(2200L + Random.nextLong(2200))
            blink.animateTo(0.08f, tween(70))
            blink.animateTo(1f, tween(90))
        }
    }
    LaunchedEffect(waveTrigger, animated) {
        if (animated && waveTrigger > 0) {
            wave.snapTo(0f)
            repeat(3) {
                wave.animateTo(1f, tween(220))
                wave.animateTo(0.3f, tween(220))
            }
            wave.animateTo(0f, tween(260))
        }
    }

    val bob = if (animated) breath else 0f
    val tilt = if (animated) sway * (if (mood == Mood.ANGRY) 0f else 2.5f) + (if (mood == Mood.ANGRY) jitter * 1.2f else 0f) else 0f
    val shiver = if (animated && mood == Mood.WORRIED) jitter * 0.6f else 0f

    Canvas(modifier.size(size)) {
        val u = this.size.width / 100f
        translate(left = shiver * u, top = bob * 2.5f * u) {
            rotate(tilt, pivot = Offset(50f * u, 100f * u)) {
                drawAvatar(config, mood, u, blink.value, wave.value, breath)
            }
        }
    }
}

private fun DrawScope.drawAvatar(config: AvatarConfig, mood: Mood, u: Float, eyeOpen: Float, wave: Float, breath: Float) {
    val skin = SkinTones.getOrElse(config.skinTone) { SkinTones.first() }
    val hair = HairColors.getOrElse(config.hairColor) { HairColors.first() }
    val colors = outfitColors(config.outfitId)

    drawOutfit(config.outfitId, colors, u)
    drawCircle(skin, 8f * u, Offset(32f * u, 74f * u))
    if (wave > 0f) {
        rotate(-110f * wave, pivot = Offset(66f * u, 64f * u)) {
            drawLine(colors.main, Offset(66f * u, 64f * u), Offset(68f * u, 74f * u), 14f * u, StrokeCap.Round)
            drawCircle(skin, 8f * u, Offset(68f * u, 78f * u))
        }
    } else {
        drawCircle(skin, 8f * u, Offset(68f * u, 74f * u))
    }
    drawRoundRect(skin, Offset(42f * u, 52f * u), Size(16f * u, 12f * u), CornerRadius(4f * u))
    drawCircle(skin, 25f * u, Offset(50f * u, 36f * u))
    drawHairBack(config.hairId, hair, u)
    drawFace(mood, u, eyeOpen, breath)
    drawHairFront(config.hairId, hair, u)
    drawAccessory(config.accessoryId, u)
}

private fun DrawScope.drawFace(mood: Mood, u: Float, eyeOpen: Float, breath: Float) {
    val ink = Color(0xFF2B2B2B)
    listOf(41f, 59f).forEach { x ->
        scale(scaleX = 1f, scaleY = eyeOpen, pivot = Offset(x * u, 36f * u)) {
            drawCircle(ink, 2.8f * u, Offset(x * u, 36f * u))
            drawCircle(Color.White, 1f * u, Offset((x + 1f) * u, 35f * u))
        }
    }
    val blushAlpha = when (mood) { Mood.ANGRY -> 0.75f; Mood.HAPPY -> 0.5f; else -> 0.3f }
    drawCircle(Color(0xFFF08080).copy(alpha = blushAlpha), 3.2f * u, Offset(36f * u, 44f * u))
    drawCircle(Color(0xFFF08080).copy(alpha = blushAlpha), 3.2f * u, Offset(64f * u, 44f * u))

    val browStroke = Stroke(1.8f * u, cap = StrokeCap.Round)
    when (mood) {
        Mood.ANGRY -> {
            drawLine(ink, Offset(35f * u, 28f * u), Offset(45f * u, 32f * u), 1.9f * u, StrokeCap.Round)
            drawLine(ink, Offset(65f * u, 28f * u), Offset(55f * u, 32f * u), 1.9f * u, StrokeCap.Round)
            drawPath(Path().apply { moveTo(43f * u, 50f * u); quadraticTo(50f * u, 44f * u, 57f * u, 50f * u) }, Color(0xFF8B3A3A), style = browStroke)
            drawPath(Path().apply {
                moveTo(76f * u, 20f * u); lineTo(80f * u, 14f * u); lineTo(77f * u, 14f * u); lineTo(79f * u, 9f * u)
            }, Color(0xFFE53935), style = Stroke(1.6f * u, cap = StrokeCap.Round))
        }
        Mood.WORRIED -> {
            drawPath(Path().apply { moveTo(35f * u, 31f * u); quadraticTo(40f * u, 27f * u, 45f * u, 30f * u) }, ink, style = browStroke)
            drawPath(Path().apply { moveTo(65f * u, 31f * u); quadraticTo(60f * u, 27f * u, 55f * u, 30f * u) }, ink, style = browStroke)
            drawPath(Path().apply {
                moveTo(43f * u, 48f * u); quadraticTo(46.5f * u, 45f * u, 50f * u, 48f * u); quadraticTo(53.5f * u, 51f * u, 57f * u, 48f * u)
            }, Color(0xFF8B3A3A), style = browStroke)
            val drop = 14f * u + breath * 4f * u
            drawPath(Path().apply {
                moveTo(72f * u, drop); quadraticTo(76f * u, drop + 6f * u, 72f * u, drop + 8f * u); quadraticTo(68f * u, drop + 6f * u, 72f * u, drop)
            }, Color(0xFF4FC3F7))
        }
        Mood.NEUTRAL -> {
            drawLine(ink, Offset(36f * u, 29f * u), Offset(45f * u, 29f * u), 1.6f * u, StrokeCap.Round)
            drawLine(ink, Offset(55f * u, 29f * u), Offset(64f * u, 29f * u), 1.6f * u, StrokeCap.Round)
            drawLine(Color(0xFF8B3A3A), Offset(44f * u, 48f * u), Offset(56f * u, 48f * u), 1.8f * u, StrokeCap.Round)
        }
        Mood.HAPPY -> {
            drawPath(Path().apply { moveTo(43f * u, 46f * u); quadraticTo(50f * u, 54f * u, 57f * u, 46f * u) }, Color(0xFF8B3A3A), style = browStroke)
            drawPath(Path().apply { moveTo(45f * u, 47.5f * u); quadraticTo(50f * u, 51f * u, 55f * u, 47.5f * u); close() }, Color.White)
        }
    }
}

private data class OutfitColors(val main: Color, val accent: Color)

private fun outfitColors(id: String) = when (id) {
    Outfits.HOODIE -> OutfitColors(Color(0xFF546E7A), Color(0xFF37474F))
    Outfits.BLAZER -> OutfitColors(Color(0xFF1E2A44), Color(0xFFFFFFFF))
    Outfits.KURTA -> OutfitColors(Color(0xFFE65100), Color(0xFFFFD54F))
    Outfits.SPACESUIT -> OutfitColors(Color(0xFFECEFF1), Color(0xFFFF7043))
    Outfits.HERO -> OutfitColors(Color(0xFFD32F2F), Color(0xFFFFEB3B))
    else -> OutfitColors(Color(0xFF42A5F5), Color(0xFF1E88E5))
}

private fun DrawScope.drawOutfit(id: String, colors: OutfitColors, u: Float) {
    val body = Path().apply {
        moveTo(30f * u, 100f * u)
        lineTo(30f * u, 72f * u)
        quadraticTo(30f * u, 60f * u, 42f * u, 60f * u)
        lineTo(58f * u, 60f * u)
        quadraticTo(70f * u, 60f * u, 70f * u, 72f * u)
        lineTo(70f * u, 100f * u)
        close()
    }
    drawPath(body, colors.main)
    when (id) {
        Outfits.HOODIE -> {
            drawRoundRect(colors.accent, Offset(44f * u, 60f * u), Size(12f * u, 8f * u), CornerRadius(3f * u))
            drawRoundRect(colors.accent, Offset(38f * u, 84f * u), Size(24f * u, 12f * u), CornerRadius(3f * u))
        }
        Outfits.BLAZER -> {
            drawPath(Path().apply { moveTo(42f * u, 60f * u); lineTo(50f * u, 80f * u); lineTo(58f * u, 60f * u); close() }, colors.accent)
            drawRect(Color(0xFFC62828), Offset(48.5f * u, 64f * u), Size(3f * u, 14f * u))
        }
        Outfits.KURTA -> {
            drawRect(colors.accent, Offset(48f * u, 60f * u), Size(4f * u, 22f * u))
            drawRect(colors.accent, Offset(30f * u, 94f * u), Size(40f * u, 3f * u))
        }
        Outfits.SPACESUIT -> {
            drawCircle(colors.accent, 7f * u, Offset(50f * u, 76f * u))
            drawCircle(Color(0xFF263238), 4f * u, Offset(50f * u, 76f * u))
            drawRect(colors.accent, Offset(30f * u, 88f * u), Size(40f * u, 4f * u))
        }
        Outfits.HERO -> {
            drawPath(Path().apply {
                moveTo(50f * u, 66f * u); lineTo(58f * u, 72f * u); lineTo(55f * u, 82f * u); lineTo(45f * u, 82f * u); lineTo(42f * u, 72f * u); close()
            }, colors.accent)
            drawRect(Color(0xFF1565C0), Offset(30f * u, 92f * u), Size(40f * u, 8f * u))
        }
        else -> drawRect(colors.accent, Offset(30f * u, 60f * u), Size(6f * u, 10f * u))
    }
}

private fun DrawScope.drawHairBack(id: String, color: Color, u: Float) {
    when (id) {
        HairStyles.LONG -> drawRoundRect(color, Offset(24f * u, 22f * u), Size(52f * u, 50f * u), CornerRadius(20f * u))
        HairStyles.CURLY -> listOf(22f to 30f, 78f to 30f, 20f to 45f, 80f to 45f, 26f to 58f, 74f to 58f).forEach { (x, y) ->
            drawCircle(color, 8f * u, Offset(x * u, y * u))
        }
    }
}

private fun DrawScope.drawHairFront(id: String, color: Color, u: Float) {
    val cap = Path().apply {
        moveTo(25f * u, 36f * u)
        quadraticTo(25f * u, 8f * u, 50f * u, 9f * u)
        quadraticTo(75f * u, 8f * u, 75f * u, 36f * u)
        quadraticTo(70f * u, 24f * u, 58f * u, 26f * u)
        quadraticTo(46f * u, 20f * u, 34f * u, 30f * u)
        quadraticTo(28f * u, 32f * u, 25f * u, 36f * u)
        close()
    }
    when (id) {
        HairStyles.MOHAWK -> {
            drawRoundRect(color, Offset(44f * u, 2f * u), Size(12f * u, 24f * u), CornerRadius(6f * u))
            drawRoundRect(color, Offset(28f * u, 18f * u), Size(44f * u, 10f * u), CornerRadius(5f * u))
        }
        HairStyles.BUN -> {
            drawPath(cap, color)
            drawCircle(color, 9f * u, Offset(50f * u, 8f * u))
            drawCircle(Color(0xFFE91E63), 3f * u, Offset(57f * u, 10f * u))
        }
        HairStyles.CURLY -> {
            drawPath(cap, color)
            listOf(30f to 16f, 42f to 10f, 58f to 10f, 70f to 16f).forEach { (x, y) -> drawCircle(color, 7f * u, Offset(x * u, y * u)) }
        }
        else -> drawPath(cap, color)
    }
}

private fun DrawScope.drawAccessory(id: String, u: Float) {
    when (id) {
        Accessories.GLASSES -> {
            val stroke = Stroke(1.8f * u)
            drawCircle(Color(0xFF263238), 6f * u, Offset(41f * u, 36f * u), style = stroke)
            drawCircle(Color(0xFF263238), 6f * u, Offset(59f * u, 36f * u), style = stroke)
            drawLine(Color(0xFF263238), Offset(47f * u, 36f * u), Offset(53f * u, 36f * u), 1.8f * u)
        }
        Accessories.CAP -> {
            drawRoundRect(Color(0xFF1E88E5), Offset(24f * u, 12f * u), Size(52f * u, 16f * u), CornerRadius(8f * u))
            drawRoundRect(Color(0xFF1565C0), Offset(50f * u, 22f * u), Size(32f * u, 6f * u), CornerRadius(3f * u))
        }
        Accessories.HEADPHONES -> {
            drawArc(Color(0xFF37474F), 180f, 180f, false, Offset(24f * u, 12f * u), Size(52f * u, 48f * u), style = Stroke(3f * u))
            drawRoundRect(Color(0xFFFF7043), Offset(20f * u, 30f * u), Size(10f * u, 14f * u), CornerRadius(4f * u))
            drawRoundRect(Color(0xFFFF7043), Offset(70f * u, 30f * u), Size(10f * u, 14f * u), CornerRadius(4f * u))
        }
        Accessories.CROWN -> {
            drawPath(Path().apply {
                moveTo(32f * u, 20f * u); lineTo(32f * u, 4f * u); lineTo(41f * u, 12f * u); lineTo(50f * u, 2f * u)
                lineTo(59f * u, 12f * u); lineTo(68f * u, 4f * u); lineTo(68f * u, 20f * u); close()
            }, Color(0xFFFFC107))
            drawCircle(Color(0xFFE53935), 2.5f * u, Offset(50f * u, 14f * u))
        }
    }
}

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
        Mood.HAPPY -> "$greeting, $name! Spending looks great today 👋"
        Mood.NEUTRAL -> "$greeting, $name. We're on track, keep an eye on it."
        Mood.WORRIED -> "Hmm $name, we're close to the budget line…"
        Mood.ANGRY -> "$name! We've blown the budget this month 😤"
    }
}
