package com.homequest.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.homequest.app.domain.Accessories
import com.homequest.app.domain.AvatarAction
import com.homequest.app.domain.AvatarConfig
import com.homequest.app.domain.Beard
import com.homequest.app.domain.FaceShape
import com.homequest.app.domain.Gender
import com.homequest.app.domain.HairColors
import com.homequest.app.domain.HairStyles
import com.homequest.app.domain.Mood
import com.homequest.app.domain.Outfits
import com.homequest.app.domain.SkinTones
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

data class AvatarPlay(val action: AvatarAction, val nonce: Int)

private val IdleGestures = listOf(AvatarAction.WINK, AvatarAction.JUMP, AvatarAction.WAVE, AvatarAction.LAUGH, AvatarAction.DANCE)

@Composable
fun AvatarView(
    config: AvatarConfig,
    size: Dp = 96.dp,
    animated: Boolean = false,
    mood: Mood = config.mood ?: Mood.HAPPY,
    play: AvatarPlay? = null,
    idleActions: Boolean = false,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.primaryContainer
) {
    val transition = rememberInfiniteTransition(label = "avatar")
    val breath by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(if (mood == Mood.EXCITED) 900 else 1800), RepeatMode.Reverse), label = "breath")
    val sway by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(if (mood == Mood.SLEEPY) 4200 else 3200), RepeatMode.Reverse), label = "sway")

    val blink = remember { Animatable(1f) }
    val progress = remember { Animatable(0f) }
    var action by remember { mutableStateOf<AvatarAction?>(null) }
    LaunchedEffect(animated) {
        while (animated) {
            delay(2400L + Random.nextLong(2600))
            blink.animateTo(0.06f, tween(70))
            blink.animateTo(1f, tween(100))
        }
    }
    LaunchedEffect(animated, play, idleActions) {
        action = null
        progress.snapTo(0f)
        if (!animated) return@LaunchedEffect
        suspend fun perform(a: AvatarAction) {
            action = a
            progress.snapTo(0f)
            progress.animateTo(1f, tween(a.durationMs, easing = LinearEasing))
            action = null
        }
        play?.let { perform(it.action) }
        while (idleActions) {
            delay(9000L + Random.nextLong(6000))
            perform(IdleGestures.random())
        }
    }

    val bob = if (animated) breath else 0f
    val tilt = if (animated) sway * 1.6f else 0f

    Canvas(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(background.lighten(0.12f), background)))
    ) {
        val u = this.size.width / 100f
        val pose = if (animated) poseFor(action, progress.value) else Pose()
        val pivot = Offset(50f * u, 100f * u)
        translate(left = pose.dx * u, top = (bob * 1.6f + pose.dy) * u) {
            rotate(tilt + pose.rotation, pivot = pivot) {
                scale(pose.scaleX, pose.scaleY, pivot = pivot) {
                    drawAvatar(config, mood, u, blink.value, pose, breath)
                }
            }
        }
        if (pose.burst > 0f) drawBurst(action, pose.burst, u)
    }
}

private class Pose(
    val dx: Float = 0f,
    val dy: Float = 0f,
    val rotation: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rightArm: Float = 0f,
    val leftArm: Float = 0f,
    val wink: Boolean = false,
    val laugh: Boolean = false,
    val burst: Float = 0f
)

private fun ramp(t: Float) = min(1f, t / 0.15f) * min(1f, (1f - t) / 0.15f)

private fun poseFor(action: AvatarAction?, t: Float): Pose {
    val tau = 2f * PI.toFloat()
    val r = ramp(t)
    return when (action) {
        null -> Pose()
        AvatarAction.WAVE -> Pose(rightArm = r * (1f - 0.22f * abs(sin(t * 3f * tau))), rotation = 3f * sin(t * tau) * r)
        AvatarAction.JUMP -> {
            val h = abs(sin(t * tau))
            Pose(dy = -14f * h, scaleX = 1f - 0.04f * h, scaleY = 1f + 0.06f * h, rightArm = 0.35f * h, leftArm = 0.35f * h)
        }
        AvatarAction.SPIN -> Pose(dy = -6f * sin(t * PI.toFloat()), scaleX = cos(t * tau))
        AvatarAction.DANCE -> {
            val beat = sin(t * 2f * tau)
            Pose(dx = 5f * beat * r, dy = -4f * abs(sin(t * 4f * tau)) * r, rotation = 9f * beat * r, rightArm = (0.55f + 0.4f * beat) * r, leftArm = (0.55f - 0.4f * beat) * r)
        }
        AvatarAction.WINK -> Pose(rotation = 5f * sin(t * PI.toFloat()), wink = t in 0.15f..0.85f, burst = r)
        AvatarAction.CHEER -> Pose(dy = -8f * abs(sin(t * 1.5f * tau)) * r, rightArm = r, leftArm = r, burst = r)
        AvatarAction.LAUGH -> Pose(dy = -2f * abs(sin(t * 5f * tau)) * r, rotation = 3f * sin(t * 5f * tau) * r, laugh = true)
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

private val Outline = Color(0x40281914)
private val Ink = Color(0xFF1F2937)
private val Lip = Color(0xFF8B2C2C)

private class Palette(val skin: Color, val skinShade: Color, val hair: Color, val hairDark: Color, val hairLight: Color, val cloth: Color, val accent: Color)

private enum class Mouth { SMILE, GRIN, FLAT, FROWN, WAVY, OPEN, SMIRK }
private enum class Extra { NONE, HEARTS, SPARKLES, SWEAT, TEAR, QUESTION, ZZZ, STEAM }

private class Face(val browTilt: Float, val browRaise: Float, val browAsym: Float, val eyeOpen: Float, val eyeScale: Float, val mouth: Mouth, val blush: Float, val extra: Extra, val heartEyes: Boolean = false)

private fun faceFor(mood: Mood): Face = when (mood) {
    Mood.HAPPY -> Face(0f, 0f, 0f, 1f, 1f, Mouth.SMILE, 0.5f, Extra.NONE)
    Mood.LOVE -> Face(-0.2f, 0.4f, 0f, 1f, 1f, Mouth.GRIN, 0.85f, Extra.HEARTS, heartEyes = true)
    Mood.EXCITED -> Face(0f, 0.9f, 0f, 1f, 1.18f, Mouth.OPEN, 0.6f, Extra.SPARKLES)
    Mood.SMART -> Face(0.2f, 0.1f, 0.7f, 0.85f, 1f, Mouth.SMIRK, 0.3f, Extra.NONE)
    Mood.NEUTRAL -> Face(0f, 0f, 0f, 1f, 1f, Mouth.FLAT, 0.3f, Extra.NONE)
    Mood.CONFUSED -> Face(0.1f, 0.3f, -0.8f, 0.95f, 1f, Mouth.WAVY, 0.3f, Extra.QUESTION)
    Mood.SLEEPY -> Face(-0.1f, -0.1f, 0f, 0.28f, 1f, Mouth.FLAT, 0.25f, Extra.ZZZ)
    Mood.SAD -> Face(-1f, 0.3f, 0f, 0.9f, 1f, Mouth.FROWN, 0.35f, Extra.TEAR)
    Mood.WORRIED -> Face(-0.6f, 0.6f, 0f, 1f, 1.05f, Mouth.WAVY, 0.4f, Extra.SWEAT)
    Mood.ANGRY -> Face(1f, -0.2f, 0f, 0.78f, 1f, Mouth.FROWN, 0.85f, Extra.STEAM)
}

private fun DrawScope.drawAvatar(config: AvatarConfig, mood: Mood, u: Float, eyeOpen: Float, pose: Pose, breath: Float) {
    val skin = SkinTones.getOrElse(config.skinTone) { SkinTones.first() }
    val hair = HairColors.getOrElse(config.hairColor) { HairColors.first() }
    val (cloth, accent) = outfitColors(config.outfitId)
    val p = Palette(skin, skin.darken(0.16f), hair, hair.darken(0.28f), hair.lighten(0.3f), cloth, accent)
    val outline = Stroke(0.9f * u)

    val female = config.gender == Gender.FEMALE
    drawHairBack(config.hairId, p, u, outline)
    drawBody(config.outfitId, p, u, outline, female)
    drawArm(pose.rightArm, 74f, 1f, p, u)
    drawArm(pose.leftArm, 26f, -1f, p, u)
    listOf(25.5f, 74.5f).forEach { x ->
        drawOval(p.skin, Offset((x - 4.8f) * u, 37.2f * u), Size(9.6f * u, 11.6f * u))
        drawOval(Outline, Offset((x - 4.8f) * u, 37.2f * u), Size(9.6f * u, 11.6f * u), style = outline)
        drawOval(p.skinShade.copy(alpha = 0.7f), Offset((x - 2.2f) * u, 40f * u), Size(4.4f * u, 6f * u))
    }
    val beard = if (female) Beard.NONE else config.beard
    drawFace(faceFor(mood), config.faceShape, beard, p, u, eyeOpen, breath, config.hairColor, female, pose)
    drawHairFront(config.hairId, p, u, outline)
    drawAccessory(config.accessoryId, u)
}

private fun DrawScope.drawArm(raise: Float, shoulderX: Float, side: Float, p: Palette, u: Float) {
    if (raise <= 0f) return
    rotate(-115f * raise * side, pivot = Offset(shoulderX * u, 70f * u)) {
        drawLine(p.cloth, Offset(shoulderX * u, 70f * u), Offset((shoulderX + 4f * side) * u, 92f * u), 11f * u, StrokeCap.Round)
        drawCircle(p.skin, 6.5f * u, Offset((shoulderX + 5f * side) * u, 94f * u))
    }
}

private fun DrawScope.drawBurst(action: AvatarAction?, amount: Float, u: Float) {
    val confetti = listOf(Color(0xFFF43F5E), Color(0xFFFBBF24), Color(0xFF22C55E), Color(0xFF3B82F6), Color(0xFFA855F7))
    when (action) {
        AvatarAction.CHEER -> repeat(10) { i ->
            val angle = i / 10f * 2f * PI.toFloat()
            val radius = 18f + 26f * amount
            drawCircle(confetti[i % confetti.size].copy(alpha = amount), 2.2f * u, Offset((50f + cos(angle) * radius) * u, (40f + sin(angle) * radius * 0.8f) * u))
        }
        AvatarAction.WINK -> drawSparkle(72f, 30f, 5f * amount, u, Color(0xFFFBBF24).copy(alpha = amount))
        else -> Unit
    }
}

private fun DrawScope.drawSparkle(x: Float, y: Float, size: Float, u: Float, color: Color) {
    drawPath(pen(u) { m(x, y - size).q(x + size * 0.3f, y - size * 0.3f, x + size, y).q(x + size * 0.3f, y + size * 0.3f, x, y + size).q(x - size * 0.3f, y + size * 0.3f, x - size, y).q(x - size * 0.3f, y - size * 0.3f, x, y - size).z() }, color)
}

private fun outfitColors(id: String): Pair<Color, Color> = when (id) {
    Outfits.HOODIE -> Color(0xFF64748B) to Color(0xFF334155)
    Outfits.BLAZER -> Color(0xFF1E293B) to Color(0xFFF8FAFC)
    Outfits.KURTA -> Color(0xFFF97316) to Color(0xFFFDE68A)
    Outfits.SPACESUIT -> Color(0xFFE5E7EB) to Color(0xFFF97316)
    Outfits.HERO -> Color(0xFFDC2626) to Color(0xFFFACC15)
    Outfits.DENIM -> Color(0xFF3B5BA9) to Color(0xFF8FB3E3)
    Outfits.SHIRT -> Color(0xFFF8FAFC) to Color(0xFF111827)
    Outfits.SWEATER -> Color(0xFFE11D74) to Color(0xFFFBCFE8)
    Outfits.DRESS -> Color(0xFF10B981) to Color(0xFFFDE68A)
    Outfits.SAREE -> Color(0xFFC026D3) to Color(0xFFFBBF24)
    Outfits.SHERWANI -> Color(0xFF1F2937) to Color(0xFFD4AF37)
    Outfits.POLO -> Color(0xFF0EA5E9) to Color(0xFFF8FAFC)
    Outfits.JERSEY -> Color(0xFF1D4ED8) to Color(0xFFF59E0B)
    Outfits.LEATHER -> Color(0xFF292524) to Color(0xFF9CA3AF)
    Outfits.NEHRU -> Color(0xFF0F766E) to Color(0xFFFDE68A)
    Outfits.LEHENGA -> Color(0xFFBE185D) to Color(0xFFFACC15)
    Outfits.LAB_COAT -> Color(0xFFF8FAFC) to Color(0xFF0EA5E9)
    Outfits.DIWALI_SILK -> Color(0xFF7F1D1D) to Color(0xFFFBBF24)
    Outfits.HOLI -> Color(0xFFF8FAFC) to Color(0xFFEC4899)
    Outfits.GARBA -> Color(0xFFDC2626) to Color(0xFF16A34A)
    Outfits.KITE -> Color(0xFF38BDF8) to Color(0xFFEF4444)
    Outfits.RAKHI -> Color(0xFFF9A8D4) to Color(0xFFF59E0B)
    Outfits.BAPPA -> Color(0xFFF97316) to Color(0xFFB91C1C)
    Outfits.TIRANGA -> Color(0xFFF8FAFC) to Color(0xFF1E3A8A)
    Outfits.SANTA -> Color(0xFFDC2626) to Color(0xFFF8FAFC)
    Outfits.SEQUIN -> Color(0xFF312E81) to Color(0xFFFACC15)
    else -> Color(0xFF3B82F6) to Color(0xFF1D4ED8)
}

private fun DrawScope.drawBody(id: String, p: Palette, u: Float, outline: Stroke, female: Boolean) {
    val w = if (female) 20f else 15f
    val torso = pen(u) { m(w, 100f).l(w, 86f).c(w, 72f, 30f, 67f, 40f, 65f).l(60f, 65f).c(70f, 67f, 100f - w, 72f, 100f - w, 86f).l(100f - w, 100f).z() }
    val base = if (id == Outfits.SAREE) Color(0xFF7E22CE) else p.cloth
    drawPath(torso, Brush.verticalGradient(listOf(base.lighten(0.14f), base, base.darken(0.14f)), startY = 65f * u, endY = 100f * u))
    drawPath(pen(u) { m(w, 100f).l(w, 86f).c(w, 76f, w + 7f, 71f, w + 15f, 68f).l(w + 15f, 100f).z() }, base.darken(0.1f).copy(alpha = 0.5f))
    drawPath(torso, if (id == Outfits.SHIRT || id == Outfits.LAB_COAT || id == Outfits.HOLI || id == Outfits.TIRANGA) Color(0xFFD1D5DB) else Outline, style = outline)
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
            drawLine(p.accent.copy(alpha = 0.6f), Offset(w * u, 92f * u), Offset((100f - w) * u, 92f * u), 1.2f * u)
        }
        Outfits.SPACESUIT -> {
            drawRect(p.accent, Offset(w * u, 86f * u), Size((100f - 2 * w) * u, 4f * u))
            drawCircle(p.accent, 6f * u, Offset(50f * u, 78f * u))
            drawCircle(Color(0xFF1E293B), 3.5f * u, Offset(50f * u, 78f * u))
            drawPath(pen(u) { m(w, 84f).q(50f, 62f, 100f - w, 84f) }, Color(0xFF9CA3AF), style = Stroke(3f * u))
            drawRoundRect(Color(0xFF9CA3AF), Offset(26f * u, 92f * u), Size(8f * u, 5f * u), CornerRadius(1f * u))
        }
        Outfits.HERO -> {
            drawPath(pen(u) { m(50f, 72f).l(58f, 78f).l(55f, 88f).l(45f, 88f).l(42f, 78f).z() }, p.accent)
            drawPath(pen(u) { m(w, 100f).l(w, 86f).q(w - 6f, 93f, w - 4f, 100f).z() }, Color(0xFF991B1B))
            drawPath(pen(u) { m(100f - w, 100f).l(100f - w, 86f).q(106f - w, 93f, 104f - w, 100f).z() }, Color(0xFF991B1B))
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
            drawRoundRect(Color(0xFFD1D5DB), Offset(60f * u, 80f * u), Size(10f * u, 9f * u), CornerRadius(1f * u), style = Stroke(1f * u))
        }
        Outfits.SWEATER -> {
            drawPath(pen(u) { m(36f, 65f).q(50f, 70f, 64f, 65f).l(64f, 72f).q(50f, 78f, 36f, 72f).z() }, p.cloth.darken(0.22f))
            drawRect(p.cloth.darken(0.2f), Offset(w * u, 94f * u), Size((100f - 2 * w) * u, 6f * u))
            listOf(80f, 88f).forEach { y ->
                drawPath(pen(u) { m(22f, y).q(28f, y - 4f, 34f, y).q(40f, y + 4f, 46f, y).q(52f, y - 4f, 58f, y).q(64f, y + 4f, 70f, y).q(74f, y - 2f, 78f, y) }, p.cloth.darken(0.25f), style = Stroke(1.4f * u))
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
        Outfits.POLO -> {
            drawPath(pen(u) { m(40f, 66f).l(46f, 75f).l(50f, 68f).z() }, p.accent)
            drawPath(pen(u) { m(60f, 66f).l(54f, 75f).l(50f, 68f).z() }, p.accent)
            drawRect(p.cloth.darken(0.15f), Offset(48.5f * u, 68f * u), Size(3f * u, 12f * u))
            listOf(72f, 77f).forEach { y -> drawCircle(p.accent, 1f * u, Offset(50f * u, y * u)) }
        }
        Outfits.JERSEY -> {
            drawPath(pen(u) { m(40f, 66f).l(50f, 78f).l(60f, 66f) }, p.accent, style = Stroke(2.4f * u))
            drawLine(p.accent, Offset((w + 3f) * u, 80f * u), Offset((w + 9f) * u, 74f * u), 2.4f * u)
            drawLine(p.accent, Offset((97f - w) * u, 80f * u), Offset((91f - w) * u, 74f * u), 2.4f * u)
            drawPath(pen(u) { m(44f, 82f).l(56f, 82f).l(49f, 97f) }, Color.White, style = Stroke(3f * u, cap = StrokeCap.Round))
        }
        Outfits.LEATHER -> {
            drawPath(pen(u) { m(42f, 66f).l(58f, 66f).l(56f, 100f).l(44f, 100f).z() }, Color(0xFFF3F4F6))
            drawPath(pen(u) { m(40f, 66f).l(34f, 78f).l(44f, 100f).l(42f, 70f).z() }, p.cloth.lighten(0.12f))
            drawPath(pen(u) { m(60f, 66f).l(66f, 78f).l(56f, 100f).l(58f, 70f).z() }, p.cloth.lighten(0.12f))
            drawLine(p.accent, Offset(57f * u, 72f * u), Offset(55.5f * u, 100f * u), 1.2f * u)
            drawCircle(p.accent, 1.1f * u, Offset(33f * u, 88f * u))
        }
        Outfits.NEHRU -> {
            drawPath(pen(u) { m(41f, 65f).q(50f, 69f, 59f, 65f).l(59f, 69f).q(50f, 73f, 41f, 69f).z() }, p.accent)
            drawLine(p.accent, Offset(50f * u, 71f * u), Offset(50f * u, 100f * u), 1.2f * u)
            listOf(77f, 84f, 91f).forEach { y -> drawCircle(p.accent, 1.3f * u, Offset(50f * u, y * u)) }
            drawPath(pen(u) { m(60f, 80f).l(67f, 80f).l(63.5f, 76f).z() }, Color(0xFFF472B6))
        }
        Outfits.LEHENGA -> {
            drawPath(pen(u) { m(40f, 66f).q(50f, 74f, 60f, 66f) }, p.accent, style = Stroke(2f * u))
            drawPath(pen(u) { m(30f, 72f).c(45f, 80f, 60f, 86f, 72f, 100f).l(62f, 100f).c(52f, 90f, 40f, 84f, 26f, 80f).z() }, p.accent.copy(alpha = 0.75f))
            listOf(36f to 90f, 44f to 94f, 70f to 84f, 76f to 92f).forEach { (x, y) -> drawCircle(p.accent, 1.2f * u, Offset(x * u, y * u)) }
        }
        Outfits.LAB_COAT -> {
            drawPath(pen(u) { m(42f, 66f).l(50f, 82f).l(58f, 66f).z() }, p.accent)
            drawPath(pen(u) { m(40f, 66f).l(37f, 80f).l(50f, 100f) }, Color(0xFFD1D5DB), style = Stroke(1.4f * u))
            drawPath(pen(u) { m(60f, 66f).l(63f, 80f).l(50f, 100f) }, Color(0xFFD1D5DB), style = Stroke(1.4f * u))
            drawRoundRect(Color(0xFFD1D5DB), Offset(60f * u, 84f * u), Size(9f * u, 8f * u), CornerRadius(1f * u), style = Stroke(1f * u))
            drawLine(p.accent, Offset(62f * u, 81f * u), Offset(62f * u, 87f * u), 1.4f * u, StrokeCap.Round)
        }
        Outfits.DIWALI_SILK -> {
            drawPath(pen(u) { m(41f, 65f).q(50f, 70f, 59f, 65f).l(59f, 70f).q(50f, 75f, 41f, 70f).z() }, p.accent)
            drawLine(p.accent, Offset(50f * u, 72f * u), Offset(50f * u, 100f * u), 1.6f * u)
            listOf(78f, 85f, 92f).forEach { y -> drawCircle(p.accent, 1.3f * u, Offset(50f * u, y * u)) }
            clipPath(torso) {
                drawRect(p.accent.copy(alpha = 0.85f), Offset(0f, 95f * u), Size(100f * u, 5f * u))
                listOf(24f, 32f, 40f, 60f, 68f, 76f).forEach { x -> drawCircle(p.accent, 0.8f * u, Offset(x * u, 92.5f * u)) }
            }
            drawPath(pen(u) { m(60f, 85f).q(65f, 91f, 70f, 85f).z() }, Color(0xFFF59E0B))
            drawPath(pen(u) { m(65f, 84.5f).q(62.5f, 81f, 65f, 77f).q(67.5f, 81f, 65f, 84.5f).z() }, Color(0xFFFF7A1A))
            drawCircle(Color(0xFFFDE68A), 0.9f * u, Offset(65f * u, 82.5f * u))
        }
        Outfits.HOLI -> {
            drawPath(pen(u) { m(42f, 66f).l(58f, 66f).l(58f, 70f).q(50f, 75f, 42f, 70f).z() }, Color(0xFFE5E7EB))
            drawRect(Color(0xFFE5E7EB), Offset(48.5f * u, 70f * u), Size(3f * u, 22f * u))
            clipPath(torso) {
                listOf(
                    Triple(28f, 80f, Color(0xFFEC4899)), Triple(68f, 76f, Color(0xFFFACC15)), Triple(38f, 93f, Color(0xFF22C55E)),
                    Triple(72f, 92f, Color(0xFF3B82F6)), Triple(57f, 86f, Color(0xFFF97316))
                ).forEach { (x, y, c) ->
                    drawCircle(c.copy(alpha = 0.85f), 5f * u, Offset(x * u, y * u))
                    drawCircle(c.copy(alpha = 0.7f), 1.6f * u, Offset((x + 6f) * u, (y - 4f) * u))
                    drawCircle(c.copy(alpha = 0.6f), 1.1f * u, Offset((x - 5f) * u, (y + 5f) * u))
                }
            }
        }
        Outfits.GARBA -> {
            clipPath(torso) {
                drawRect(p.accent, Offset(0f, 84f * u), Size(100f * u, 7f * u))
                drawPath(pen(u) { m(10f, 84f); var x = 10f; while (x < 90f) { l(x + 3f, 81f); l(x + 6f, 84f); x += 6f } }, Color(0xFFFACC15), style = Stroke(1.2f * u))
                listOf(76f, 88f, 96f).forEachIndexed { row, y ->
                    var x = if (row % 2 == 0) 22f else 26f
                    while (x < 80f) {
                        drawCircle(Color.White, 1.5f * u, Offset(x * u, y * u))
                        drawCircle(Color(0xFFFACC15), 1.5f * u, Offset(x * u, y * u), style = Stroke(0.6f * u))
                        x += 8f
                    }
                }
            }
            drawPath(pen(u) { m(36f, 65f).q(50f, 80f, 64f, 65f) }, Color(0xFFFACC15), style = Stroke(2.4f * u))
        }
        Outfits.KITE -> {
            drawPath(pen(u) { m(41f, 66f).q(50f, 75f, 59f, 66f) }, Color.White, style = Stroke(2.4f * u))
            listOf(Triple(34f, 82f, p.accent), Triple(63f, 78f, Color(0xFFFACC15)), Triple(55f, 92f, Color(0xFF22C55E))).forEach { (x, y, c) ->
                drawPath(pen(u) { m(x, y - 5f).l(x + 4f, y).l(x, y + 5f).l(x - 4f, y).z() }, c)
                drawLine(Color.White.copy(alpha = 0.7f), Offset(x * u, (y - 5f) * u), Offset(x * u, (y + 5f) * u), 0.5f * u)
                drawPath(pen(u) { m(x, y + 5f).q(x + 3f, y + 8f, x - 1f, y + 11f) }, Color.White.copy(alpha = 0.8f), style = Stroke(0.6f * u))
            }
        }
        Outfits.RAKHI -> {
            drawPath(pen(u) { m(42f, 66f).l(58f, 66f).l(58f, 70f).q(50f, 75f, 42f, 70f).z() }, p.accent)
            drawRect(p.accent, Offset(48.5f * u, 70f * u), Size(3f * u, 22f * u))
            listOf(30f to 80f, 70f to 84f, 36f to 94f, 64f to 96f).forEach { (x, y) ->
                repeat(5) { i ->
                    val a = i * 72f / 180f * PI.toFloat()
                    drawCircle(Color.White.copy(alpha = 0.85f), 1.5f * u, Offset((x + cos(a) * 1.8f) * u, (y + sin(a) * 1.8f) * u))
                }
                drawCircle(p.accent, 1.1f * u, Offset(x * u, y * u))
            }
        }
        Outfits.BAPPA -> {
            drawPath(pen(u) { m(42f, 66f).l(58f, 66f).l(58f, 70f).q(50f, 75f, 42f, 70f).z() }, Color(0xFFFDE68A))
            clipPath(torso) {
                drawPath(pen(u) { m(22f, 70f).l(32f, 66f).l(84f, 100f).l(70f, 100f).z() }, p.accent)
                drawPath(pen(u) { m(32f, 66f).l(84f, 100f) }, Color(0xFFFACC15), style = Stroke(1.2f * u))
                drawPath(pen(u) { m(22f, 70f).l(70f, 100f) }, Color(0xFFFACC15), style = Stroke(1.2f * u))
            }
            listOf(80f, 88f).forEach { y -> drawCircle(Color(0xFFFDE68A), 1.2f * u, Offset(40f * u, y * u)) }
        }
        Outfits.TIRANGA -> {
            clipPath(torso) {
                drawRect(Color(0xFFFF9933), Offset(0f, 65f * u), Size(100f * u, 12f * u))
                drawRect(Color(0xFF138808), Offset(0f, 89f * u), Size(100f * u, 11f * u))
            }
            drawPath(pen(u) { m(41f, 65f).q(50f, 69f, 59f, 65f).l(59f, 69f).q(50f, 73f, 41f, 69f).z() }, Color(0xFFF8FAFC))
            drawCircle(p.accent, 5f * u, Offset(50f * u, 83f * u), style = Stroke(0.9f * u))
            repeat(24) { i ->
                val a = i * 15f / 180f * PI.toFloat()
                drawLine(p.accent, Offset(50f * u, 83f * u), Offset((50f + cos(a) * 5f) * u, (83f + sin(a) * 5f) * u), 0.3f * u)
            }
            drawCircle(p.accent, 0.9f * u, Offset(50f * u, 83f * u))
        }
        Outfits.SANTA -> {
            drawPath(pen(u) { m(36f, 65f).q(50f, 72f, 64f, 65f).l(64f, 70f).q(50f, 77f, 36f, 70f).z() }, p.accent)
            drawRoundRect(p.accent, Offset(46f * u, 72f * u), Size(8f * u, 28f * u), CornerRadius(3f * u))
            clipPath(torso) { drawRect(Color(0xFF111827), Offset(0f, 88f * u), Size(100f * u, 5f * u)) }
            drawRect(Color(0xFFFACC15), Offset(45.5f * u, 87.5f * u), Size(9f * u, 6f * u), style = Stroke(1.3f * u))
        }
        Outfits.SEQUIN -> {
            clipPath(torso) {
                for (y in 68..100 step 4) for (x in 14..86 step 4) {
                    val shift = if ((y / 4) % 2 == 0) 0f else 2f
                    drawCircle(p.accent.copy(alpha = 0.25f + 0.5f * ((x * 7 + y * 13) % 5) / 4f), 0.9f * u, Offset((x + shift) * u, y * u))
                }
            }
            drawPath(pen(u) { m(40f, 66f).q(37f, 84f, 50f, 97f) }, p.accent, style = Stroke(2.2f * u))
            drawPath(pen(u) { m(60f, 66f).q(63f, 84f, 50f, 97f) }, p.accent, style = Stroke(2.2f * u))
            drawPath(pen(u) { m(50f, 70f).l(44f, 67f).l(44f, 73f).z() }, Ink)
            drawPath(pen(u) { m(50f, 70f).l(56f, 67f).l(56f, 73f).z() }, Ink)
        }
        else -> drawPath(pen(u) { m(41f, 66f).q(50f, 75f, 59f, 66f) }, p.accent, style = Stroke(2.4f * u))
    }
}

private fun facePath(shape: FaceShape, u: Float): Path = pen(u) {
    m(26f, 38f).c(26f, 12f, 74f, 12f, 74f, 38f)
    when (shape) {
        FaceShape.OVAL -> c(74f, 52f, 67f, 62f, 57f, 64.5f).q(50f, 66f, 43f, 64.5f).c(33f, 62f, 26f, 52f, 26f, 38f)
        FaceShape.ROUND -> c(75f, 56f, 66f, 65f, 56f, 66f).q(50f, 67.5f, 44f, 66f).c(34f, 65f, 25f, 56f, 26f, 38f)
        FaceShape.SQUARE -> c(74f, 54f, 72f, 61f, 62f, 64f).q(50f, 67f, 38f, 64f).c(28f, 61f, 26f, 54f, 26f, 38f)
        FaceShape.HEART -> c(74f, 48f, 62f, 60f, 54f, 65f).q(50f, 67f, 46f, 65f).c(38f, 60f, 26f, 48f, 26f, 38f)
        FaceShape.LONG -> c(74f, 56f, 67f, 66f, 57f, 68f).q(50f, 69.5f, 43f, 68f).c(33f, 66f, 26f, 56f, 26f, 38f)
    }
    z()
}

private fun DrawScope.drawFace(f: Face, shape: FaceShape, beard: Beard, p: Palette, u: Float, blink: Float, breath: Float, hairColorIndex: Int, female: Boolean, pose: Pose) {
    val face = facePath(shape, u)
    drawPath(face, Brush.radialGradient(listOf(p.skin.lighten(0.1f), p.skin, p.skin.darken(0.14f)), center = Offset(43f * u, 32f * u), radius = 42f * u))
    drawPath(face, Outline, style = Stroke(0.9f * u))

    val blush = if (female) f.blush else f.blush * 0.6f
    drawOval(Color(0xFFF28B82).copy(alpha = blush), Offset(30.5f * u, 47f * u), Size(11f * u, 6.5f * u))
    drawOval(Color(0xFFF28B82).copy(alpha = blush), Offset(58.5f * u, 47f * u), Size(11f * u, 6.5f * u))
    drawBeard(beard, face, p, u)

    val eyeOpen = minOf(blink, f.eyeOpen)
    val iris = if (hairColorIndex <= 2) p.hair.darken(0.1f) else Color(0xFF3F5F7A)
    listOf(40.5f to -1f, 59.5f to 1f).forEach { (x, d) ->
        when {
            pose.laugh || (pose.wink && d < 0) ->
                drawPath(pen(u) { m(x - 6f, 43f).q(x, 36f, x + 6f, 43f) }, Ink, style = Stroke(2.2f * u, cap = StrokeCap.Round))
            f.heartEyes -> drawHeart(x, 41.5f, 7.5f, u, Color(0xFFEF4466))
            else -> {
                scale(scaleX = f.eyeScale, scaleY = eyeOpen * f.eyeScale, pivot = Offset(x * u, 41.5f * u)) {
                    drawPath(pen(u) { m(x - 7f, 41.5f).q(x, 33.5f, x + 7f, 41.5f).q(x, 48.5f, x - 7f, 41.5f).z() }, Color.White)
                    drawCircle(iris, 4.3f * u, Offset((x + 0.6f * d) * u, 42f * u))
                    drawCircle(Ink, 2.4f * u, Offset((x + 0.6f * d) * u, 42f * u))
                    drawCircle(Color.White, 1.4f * u, Offset((x - 1.4f) * u, 40.2f * u))
                    drawCircle(Color.White, 0.7f * u, Offset((x + 1.8f) * u, 43.8f * u))
                    drawPath(pen(u) { m(x - 7f, 41.5f).q(x, 48.5f, x + 7f, 41.5f) }, Outline, style = Stroke(0.9f * u))
                }
                drawPath(pen(u) { m(x - 7.2f, 41.5f).q(x, 32.8f, x + 7.2f, 41.5f) }, Ink, style = Stroke(if (female) 2f * u else 1.5f * u, cap = StrokeCap.Round))
                if (female) {
                    drawLine(Ink, Offset((x + 6.8f * d) * u, 40f * u), Offset((x + 8.3f * d) * u, 38.8f * u), 1.3f * u, StrokeCap.Round)
                    drawLine(Ink, Offset((x + 5.2f * d) * u, 36.2f * u), Offset((x + 6.2f * d) * u, 34.6f * u), 1.1f * u, StrokeCap.Round)
                }
            }
        }
    }

    drawBrows(f, p, u, female)

    drawPath(pen(u) { m(47.5f, 50f).q(50f, 52.5f, 52.5f, 50f) }, p.skin.darken(0.3f), style = Stroke(1.6f * u, cap = StrokeCap.Round))
    drawCircle(Color.White.copy(alpha = 0.35f), 1.2f * u, Offset(48.6f * u, 48.2f * u))

    drawMouth(if (pose.laugh) Mouth.OPEN else f.mouth, u)
    if (beard == Beard.MUSTACHE || beard == Beard.GOATEE || beard == Beard.FULL) {
        drawPath(pen(u) { m(41f, 53.8f).q(45f, 50.2f, 50f, 52.2f).q(55f, 50.2f, 59f, 53.8f).q(55f, 55.2f, 50f, 54f).q(45f, 55.2f, 41f, 53.8f).z() }, p.hairDark)
    }
    drawExtra(f.extra, u, breath)
}

private fun DrawScope.drawBeard(beard: Beard, face: Path, p: Palette, u: Float) {
    when (beard) {
        Beard.NONE, Beard.MUSTACHE -> Unit
        Beard.STUBBLE -> clipPath(face) {
            drawPath(pen(u) { m(24f, 44f).c(28f, 60f, 40f, 72f, 50f, 72f).c(60f, 72f, 72f, 60f, 76f, 44f).c(70f, 54f, 62f, 58f, 57f, 58f).q(50f, 62f, 43f, 58f).c(38f, 58f, 30f, 54f, 24f, 44f).z() }, p.hairDark.copy(alpha = 0.28f))
        }
        Beard.CHIN_STRAP -> clipPath(face) {
            drawPath(pen(u) { m(26f, 40f).c(27f, 58f, 38f, 68f, 50f, 69f).c(62f, 68f, 73f, 58f, 74f, 40f) }, p.hairDark, style = Stroke(3.2f * u, cap = StrokeCap.Round))
        }
        Beard.GOATEE -> drawPath(pen(u) { m(44f, 61f).q(50f, 58.5f, 56f, 61f).q(57f, 68f, 50f, 70.5f).q(43f, 68f, 44f, 61f).z() }, Brush.verticalGradient(listOf(p.hair, p.hairDark), startY = 58f * u, endY = 71f * u))
        Beard.FULL -> drawPath(
            pen(u) { m(26f, 42f).c(26f, 62f, 38f, 74f, 50f, 75f).c(62f, 74f, 74f, 62f, 74f, 42f).l(71f, 44f).c(68f, 54f, 62f, 58f, 57f, 58f).q(50f, 61.5f, 43f, 58f).c(38f, 58f, 32f, 54f, 29f, 44f).z() },
            Brush.verticalGradient(listOf(p.hair, p.hairDark), startY = 42f * u, endY = 75f * u)
        )
    }
}

private fun DrawScope.drawBrows(f: Face, p: Palette, u: Float, female: Boolean) {
    val raise = f.browRaise * 2.5f
    val thick = if (female) 3.4f else 4.4f
    listOf(-1f, 1f).forEach { side ->
        val asym = f.browAsym * 2f * side
        val tilt = f.browTilt * 3f
        val outerY = 31f - raise + asym + tilt
        val innerY = 31f - raise + asym - tilt + 1.5f
        val ox = 50f + side * 17.5f
        val ix = 50f + side * 2.5f
        val path = pen(u) {
            m(ox, outerY + 1.2f).q(50f + side * 10f, (outerY + innerY) / 2f - 3.2f, ix, innerY)
            q(50f + side * 10f, (outerY + innerY) / 2f + 0.8f, ox, outerY + thick).z()
        }
        drawPath(path, p.hairDark)
    }
}

private fun DrawScope.drawMouth(mouth: Mouth, u: Float) {
    val stroke = Stroke(2.4f * u, cap = StrokeCap.Round)
    when (mouth) {
        Mouth.SMILE -> {
            val smile = pen(u) { m(40.5f, 54f).q(50f, 64.5f, 59.5f, 54f).q(50f, 57f, 40.5f, 54f).z() }
            drawPath(smile, Lip)
            drawPath(pen(u) { m(43f, 55.2f).q(50f, 59.5f, 57f, 55.2f).q(50f, 57.2f, 43f, 55.2f).z() }, Color.White)
        }
        Mouth.GRIN -> {
            val grin = pen(u) { m(39f, 53.5f).q(50f, 67f, 61f, 53.5f).q(50f, 56.5f, 39f, 53.5f).z() }
            drawPath(grin, Lip)
            drawPath(pen(u) { m(42f, 55f).q(50f, 59f, 58f, 55f).q(50f, 57f, 42f, 55f).z() }, Color.White)
            drawPath(pen(u) { m(44f, 58.5f).q(50f, 63.5f, 56f, 58.5f).q(50f, 61.5f, 44f, 58.5f).z() }, Color(0xFFF2756F))
        }
        Mouth.OPEN -> {
            drawOval(Lip, Offset(44.5f * u, 53.5f * u), Size(11f * u, 9f * u))
            drawPath(pen(u) { m(46f, 55f).q(50f, 57.5f, 54f, 55f).q(50f, 56f, 46f, 55f).z() }, Color.White)
            drawOval(Color(0xFFF2756F), Offset(47f * u, 58.5f * u), Size(6f * u, 3f * u))
        }
        Mouth.FLAT -> drawPath(pen(u) { m(43.5f, 56.5f).q(50f, 57.5f, 56.5f, 56.5f) }, Lip, style = stroke)
        Mouth.SMIRK -> drawPath(pen(u) { m(43f, 57f).q(51f, 59.5f, 58f, 54.5f) }, Lip, style = stroke)
        Mouth.WAVY -> drawPath(pen(u) { m(43f, 57.5f).q(46.5f, 54.5f, 50f, 57.5f).q(53.5f, 60.5f, 57f, 57.5f) }, Lip, style = stroke)
        Mouth.FROWN -> drawPath(pen(u) { m(43f, 59f).q(50f, 52.5f, 57f, 59f) }, Lip, style = stroke)
    }
}

private fun DrawScope.drawExtra(extra: Extra, u: Float, breath: Float) {
    val dy = breath * 3f
    when (extra) {
        Extra.NONE -> Unit
        Extra.HEARTS -> {
            drawHeart(80f, 22f - dy, 4.5f, u, Color(0xFFEF4466))
            drawHeart(86f, 34f - dy * 0.6f, 3f, u, Color(0xFFF472B6))
            drawHeart(18f, 28f - dy * 0.8f, 3.5f, u, Color(0xFFEF4466))
        }
        Extra.SPARKLES -> listOf(Triple(80f, 20f, 4f), Triple(88f, 36f, 2.5f), Triple(16f, 24f, 3f)).forEach { (x, y, r) ->
            drawSparkle(x, y, r * (0.8f + breath * 0.4f), u, Color(0xFFFBBF24))
        }
        Extra.SWEAT -> drawPath(pen(u) { m(77f, 20f + dy).q(81f, 27f + dy, 77f, 29f + dy).q(73f, 27f + dy, 77f, 20f + dy).z() }, Color(0xFF60A5FA))
        Extra.TEAR -> {
            drawPath(pen(u) { m(36f, 48f).q(38.5f, 54f + dy, 36f, 57f + dy).q(33.5f, 54f + dy, 36f, 48f).z() }, Color(0xFF60A5FA))
            drawPath(pen(u) { m(64f, 48f).q(66.5f, 53f + dy, 64f, 56f + dy).q(61.5f, 53f + dy, 64f, 48f).z() }, Color(0xFF60A5FA))
        }
        Extra.QUESTION -> {
            val st = Stroke(2.2f * u, cap = StrokeCap.Round)
            drawPath(pen(u) { m(78f, 18f - dy).q(78f, 11f - dy, 84f, 12f - dy).q(90f, 13f - dy, 86f, 19f - dy).q(83f, 22f - dy, 84f, 26f - dy) }, Color(0xFF7C3AED), style = st)
            drawCircle(Color(0xFF7C3AED), 1.5f * u, Offset(84f * u, (31f - dy) * u))
        }
        Extra.ZZZ -> {
            val st = Stroke(1.8f * u, cap = StrokeCap.Round)
            listOf(Triple(76f, 26f, 5f), Triple(84f, 16f, 4f), Triple(90f, 8f, 3f)).forEachIndexed { i, (x, y, s) ->
                val yy = y - dy * (1f + i * 0.4f)
                drawPath(pen(u) { m(x - s, yy - s).l(x + s, yy - s).l(x - s, yy + s).l(x + s, yy + s) }, Color(0xFF6366F1).copy(alpha = 0.9f - i * 0.2f), style = st)
            }
        }
        Extra.STEAM -> listOf(20f to 1f, 80f to -1f).forEach { (x, d) ->
            drawPath(pen(u) { m(x, 30f - dy).q(x - 4f * d, 25f - dy, x, 20f - dy).q(x + 4f * d, 15f - dy, x, 10f - dy) }, Color(0xFF9CA3AF).copy(alpha = 0.8f), style = Stroke(2.4f * u, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawHeart(cx: Float, cy: Float, r: Float, u: Float, color: Color) {
    drawPath(
        pen(u) {
            m(cx, cy + r)
            c(cx - r * 1.6f, cy - r * 0.2f, cx - r * 0.9f, cy - r * 1.3f, cx, cy - r * 0.45f)
            c(cx + r * 0.9f, cy - r * 1.3f, cx + r * 1.6f, cy - r * 0.2f, cx, cy + r)
            z()
        },
        color
    )
}

private fun DrawScope.drawHairBack(id: String, p: Palette, u: Float, outline: Stroke) {
    val brush = Brush.verticalGradient(listOf(p.hair, p.hairDark), startY = 10f * u, endY = 95f * u)
    fun fill(path: Path) { drawPath(path, brush); drawPath(path, Outline, style = outline) }
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
            drawCircle(Brush.radialGradient(listOf(p.hairLight.copy(alpha = 0.5f), p.hair, p.hairDark), center = Offset((x - r * 0.3f) * u, (y - r * 0.3f) * u), radius = r * 1.4f * u), r * u, Offset(x * u, y * u))
            drawCircle(Outline, r * u, Offset(x * u, y * u), style = outline)
        }
        HairStyles.BRAIDS -> {
            listOf(30f, 70f).forEach { sx ->
                val braid = pen(u) { m(sx, 50f).q(sx + 4f, 60f, sx, 70f).q(sx - 4f, 80f, sx, 90f) }
                drawPath(braid, p.hair, style = Stroke(7f * u, cap = StrokeCap.Round))
                drawPath(braid, p.hairDark, style = Stroke(1.6f * u, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f * u, 4f * u))))
                drawCircle(Color(0xFF93C5FD), 2.2f * u, Offset(sx * u, 84f * u))
            }
            fill(pen(u) { m(23f, 42f).c(21f, 8f, 79f, 8f, 77f, 42f).l(77f, 52f).l(23f, 52f).z() })
        }
        HairStyles.AFRO -> {
            drawCircle(Brush.radialGradient(listOf(p.hairLight.copy(alpha = 0.6f), p.hair, p.hairDark), center = Offset(44f * u, 22f * u), radius = 40f * u), 34f * u, Offset(50f * u, 32f * u))
            drawCircle(Outline, 34f * u, Offset(50f * u, 32f * u), style = outline)
        }
        HairStyles.PIXIE -> fill(pen(u) { m(24f, 40f).c(22f, 20f, 78f, 20f, 76f, 40f).l(76f, 50f).q(74f, 54f, 70f, 52f).l(30f, 52f).q(26f, 54f, 24f, 50f).z() })
        HairStyles.MAN_BUN -> {
            drawCircle(Brush.radialGradient(listOf(p.hair.lighten(0.15f), p.hair, p.hairDark), center = Offset(48f * u, 6f * u), radius = 10f * u), 7.5f * u, Offset(50f * u, 9f * u))
            drawCircle(Outline, 7.5f * u, Offset(50f * u, 9f * u), style = outline)
        }
    }
}

private fun DrawScope.drawHairFront(id: String, p: Palette, u: Float, outline: Stroke) {
    val brush = Brush.verticalGradient(listOf(p.hair.lighten(0.08f), p.hair, p.hairDark.copy(alpha = 0.95f)), startY = 4f * u, endY = 40f * u)
    fun fill(path: Path) { drawPath(path, brush); drawPath(path, Outline, style = outline) }
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
            drawCircle(Brush.radialGradient(listOf(p.hair.lighten(0.15f), p.hair, p.hairDark), center = Offset(47f * u, 9f * u), radius = 12f * u), 9f * u, Offset(50f * u, 12f * u))
            drawCircle(Outline, 9f * u, Offset(50f * u, 12f * u), style = outline)
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
        HairStyles.SPIKY -> {
            fill(pen(u) {
                m(26f, 36f).l(27f, 22f).l(32f, 25f).l(35f, 11f).l(41f, 21f).l(46f, 5f).l(51f, 19f).l(56f, 6f).l(60f, 20f).l(66f, 10f).l(67f, 24f).l(73f, 20f).l(74f, 36f)
                c(70f, 28f, 60f, 24f, 50f, 25f).c(40f, 24f, 30f, 28f, 26f, 36f).z()
            })
            drawPath(pen(u) { m(36f, 22f).q(46f, 14f, 56f, 16f) }, p.hairLight.copy(alpha = 0.5f), style = shine)
        }
        HairStyles.SIDE_PART -> {
            fill(pen(u) { m(26f, 37f).c(25f, 12f, 48f, 6f, 60f, 9f).c(72f, 12f, 75f, 24f, 74f, 37f).c(72f, 30f, 68f, 26f, 62f, 24f).c(54f, 21f, 44f, 22f, 38f, 24f).c(32f, 27f, 28f, 31f, 26f, 37f).z() })
            drawPath(pen(u) { m(40f, 11f).q(41f, 17f, 39f, 23f) }, p.hairDark.copy(alpha = 0.7f), style = Stroke(1.3f * u, cap = StrokeCap.Round))
            drawPath(pen(u) { m(44f, 16f).q(54f, 11f, 64f, 15f) }, p.hairLight.copy(alpha = 0.55f), style = shine)
        }
        HairStyles.QUIFF -> {
            fill(pen(u) { m(26f, 36f).c(26f, 18f, 34f, 10f, 44f, 8f).c(50f, 0f, 66f, 2f, 70f, 12f).c(76f, 20f, 75f, 28f, 74f, 36f).c(70f, 28f, 62f, 24f, 54f, 25f).c(46f, 23f, 36f, 27f, 26f, 36f).z() })
            drawPath(pen(u) { m(46f, 9f).q(56f, 3f, 66f, 9f) }, p.hairLight.copy(alpha = 0.55f), style = shine)
        }
        HairStyles.AFRO -> {
            fill(pen(u) { m(26f, 36f).c(26f, 16f, 74f, 16f, 74f, 36f).c(66f, 28f, 56f, 26f, 50f, 27f).c(44f, 26f, 34f, 28f, 26f, 36f).z() })
            listOf(30f to 14f, 44f to 6f, 58f to 7f, 70f to 15f, 20f to 26f, 80f to 26f).forEach { (x, y) -> drawCircle(p.hairLight.copy(alpha = 0.3f), 3.5f * u, Offset(x * u, y * u)) }
        }
        HairStyles.PIXIE -> {
            fill(pen(u) { m(25f, 40f).c(22f, 10f, 78f, 10f, 75f, 40f).c(73f, 30f, 68f, 26f, 62f, 26f).c(52f, 30f, 40f, 28f, 34f, 22f).c(31f, 28f, 28f, 32f, 25f, 40f).z() })
            drawPath(pen(u) { m(40f, 18f).q(52f, 14f, 64f, 20f) }, p.hairLight.copy(alpha = 0.55f), style = shine)
        }
        HairStyles.MAN_BUN -> {
            fill(cap)
            drawPath(pen(u) { m(34f, 24f).q(42f, 16f, 50f, 15f) }, p.hairDark.copy(alpha = 0.5f), style = Stroke(1.2f * u))
            drawPath(pen(u) { m(52f, 15f).q(60f, 16f, 66f, 24f) }, p.hairDark.copy(alpha = 0.5f), style = Stroke(1.2f * u))
            drawPath(capShine, p.hairLight.copy(alpha = 0.55f), style = shine)
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
            drawCircle(Color.White.copy(alpha = 0.25f), 5.5f * u, Offset(41.5f * u, 41f * u))
            drawCircle(Color.White.copy(alpha = 0.25f), 5.5f * u, Offset(58.5f * u, 41f * u))
        }
        Accessories.CAP -> {
            drawPath(pen(u) { m(26f, 32f).c(26f, 12f, 74f, 12f, 74f, 32f).z() }, Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF2563EB)), startY = 12f * u, endY = 32f * u))
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
            drawPath(pen(u) { m(32f, 24f).l(32f, 8f).l(41f, 16f).l(50f, 4f).l(59f, 16f).l(68f, 8f).l(68f, 24f).z() }, Brush.verticalGradient(listOf(Color(0xFFFDE68A), Color(0xFFF59E0B)), startY = 4f * u, endY = 24f * u))
            drawCircle(Color(0xFFEF4444), 2.5f * u, Offset(50f * u, 18f * u))
            drawCircle(Color(0xFF3B82F6), 1.5f * u, Offset(40f * u, 20f * u))
            drawCircle(Color(0xFF3B82F6), 1.5f * u, Offset(60f * u, 20f * u))
        }
        Accessories.EARRINGS -> {
            drawCircle(Color(0xFFFBBF24), 2f * u, Offset(25.5f * u, 50f * u))
            drawCircle(Color(0xFFFBBF24), 2f * u, Offset(74.5f * u, 50f * u))
        }
        Accessories.BINDI -> drawCircle(Color(0xFFDC2626), 1.6f * u, Offset(50f * u, 30f * u))
        Accessories.SUNGLASSES -> {
            val lens = Brush.verticalGradient(listOf(Color(0xFF374151), Color(0xFF0F172A)), startY = 36f * u, endY = 47f * u)
            drawRoundRect(lens, Offset(33f * u, 36f * u), Size(15f * u, 10.5f * u), CornerRadius(4f * u))
            drawRoundRect(lens, Offset(52f * u, 36f * u), Size(15f * u, 10.5f * u), CornerRadius(4f * u))
            drawLine(Ink, Offset(48f * u, 39f * u), Offset(52f * u, 39f * u), 1.8f * u)
            drawLine(Ink, Offset(33f * u, 39f * u), Offset(28f * u, 37.5f * u), 1.8f * u)
            drawLine(Ink, Offset(67f * u, 39f * u), Offset(72f * u, 37.5f * u), 1.8f * u)
            drawLine(Color.White.copy(alpha = 0.45f), Offset(36f * u, 38.5f * u), Offset(40f * u, 38.5f * u), 1.2f * u, StrokeCap.Round)
            drawLine(Color.White.copy(alpha = 0.45f), Offset(55f * u, 38.5f * u), Offset(59f * u, 38.5f * u), 1.2f * u, StrokeCap.Round)
        }
        Accessories.BEANIE -> {
            drawPath(pen(u) { m(25f, 33f).c(25f, 6f, 75f, 6f, 75f, 33f).z() }, Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)), startY = 6f * u, endY = 33f * u))
            drawRoundRect(Color(0xFF991B1B), Offset(24f * u, 28f * u), Size(52f * u, 7f * u), CornerRadius(3f * u))
            listOf(32f, 40f, 48f, 56f, 64f).forEach { x -> drawLine(Color(0xFF7F1D1D), Offset(x * u, 29f * u), Offset(x * u, 34f * u), 1f * u) }
            drawCircle(Color.White, 5f * u, Offset(50f * u, 7f * u))
        }
        Accessories.TURBAN -> {
            drawPath(pen(u) { m(24f, 36f).c(22f, 4f, 78f, 4f, 76f, 36f).c(66f, 30f, 58f, 28f, 50f, 30f).c(42f, 28f, 34f, 30f, 24f, 36f).z() }, Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFEA580C)), startY = 4f * u, endY = 36f * u))
            listOf(14f, 20f, 26f).forEach { y -> drawPath(pen(u) { m(28f, y + 6f).q(50f, y - 4f, 72f, y + 6f) }, Color(0xFFC2410C).copy(alpha = 0.6f), style = Stroke(1.2f * u)) }
            drawCircle(Color(0xFFDC2626), 2.4f * u, Offset(50f * u, 22f * u))
            drawCircle(Color(0xFFFDE68A), 1f * u, Offset(50f * u, 22f * u))
        }
        Accessories.FLOWER -> {
            listOf(0f, 72f, 144f, 216f, 288f).forEach { deg ->
                val a = deg / 180f * PI.toFloat()
                drawCircle(Color(0xFFF472B6), 2.8f * u, Offset((70f + cos(a) * 3.2f) * u, (26f + sin(a) * 3.2f) * u))
            }
            drawCircle(Color(0xFFFBBF24), 2f * u, Offset(70f * u, 26f * u))
        }
        Accessories.BOW_TIE -> {
            drawPath(pen(u) { m(50f, 69f).l(42f, 65f).l(42f, 73f).z() }, Color(0xFFDC2626))
            drawPath(pen(u) { m(50f, 69f).l(58f, 65f).l(58f, 73f).z() }, Color(0xFFDC2626))
            drawCircle(Color(0xFF991B1B), 1.8f * u, Offset(50f * u, 69f * u))
        }
        Accessories.NOSE_RING -> drawCircle(Color(0xFFFBBF24), 1.6f * u, Offset(53.2f * u, 51.5f * u), style = Stroke(0.8f * u))
        Accessories.CHAIN -> {
            drawPath(pen(u) { m(40f, 67f).q(50f, 82f, 60f, 67f) }, Color(0xFFFBBF24), style = Stroke(1.5f * u, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f * u, 1f * u))))
            drawCircle(Color(0xFFF59E0B), 2.6f * u, Offset(50f * u, 76f * u))
        }
        Accessories.GULAL -> {
            drawOval(Color(0xFFEC4899).copy(alpha = 0.55f), Offset(29f * u, 46f * u), Size(12f * u, 8f * u))
            drawOval(Color(0xFF22C55E).copy(alpha = 0.5f), Offset(59f * u, 47f * u), Size(11f * u, 7f * u))
            drawOval(Color(0xFFFACC15).copy(alpha = 0.55f), Offset(43f * u, 26f * u), Size(10f * u, 5f * u))
            drawCircle(Color(0xFF3B82F6).copy(alpha = 0.5f), 1.6f * u, Offset(66f * u, 55f * u))
            drawCircle(Color(0xFFEC4899).copy(alpha = 0.5f), 1.2f * u, Offset(33f * u, 56f * u))
        }
        Accessories.SANTA_HAT -> {
            drawPath(pen(u) { m(25f, 31f).c(25f, 12f, 48f, 2f, 70f, 8f).q(82f, 12f, 84f, 24f).q(76f, 18f, 72f, 20f).q(76f, 24f, 75f, 31f).z() }, Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C)), startY = 4f * u, endY = 31f * u))
            drawRoundRect(Color.White, Offset(23f * u, 27f * u), Size(54f * u, 8f * u), CornerRadius(4f * u))
            drawCircle(Color.White, 4.5f * u, Offset(84f * u, 25f * u))
        }
        Accessories.PARTY_HAT -> {
            val cone = pen(u) { m(37f, 22f).l(53f, 2f).l(64f, 24f).q(50f, 19f, 37f, 22f).z() }
            drawPath(cone, Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)), start = Offset(37f * u, 22f * u), end = Offset(64f * u, 2f * u)))
            clipPath(cone) { listOf(8f, 14f, 20f).forEach { y -> drawLine(Color(0xFFFACC15), Offset(30f * u, (y + 4f) * u), Offset(70f * u, (y - 4f) * u), 1.6f * u) } }
            drawCircle(Color(0xFFFACC15), 3.2f * u, Offset(53f * u, 3f * u))
        }
    }
}

private fun Color.darken(amount: Float) = Color(red * (1 - amount), green * (1 - amount), blue * (1 - amount), alpha)
private fun Color.lighten(amount: Float) = Color(red + (1 - red) * amount, green + (1 - green) * amount, blue + (1 - blue) * amount, alpha)

fun moodFor(budgetProgress: Float, hasBudget: Boolean, streak: Int, daysSinceLastLog: Long, hour: Int, dayOfMonth: Int): Mood = when {
    hasBudget && budgetProgress > 1f -> Mood.ANGRY
    hasBudget && budgetProgress >= 0.85f -> Mood.WORRIED
    hour >= 23 || hour < 5 -> Mood.SLEEPY
    daysSinceLastLog >= 3 -> Mood.SAD
    hasBudget && budgetProgress in 0.65f..0.85f -> Mood.CONFUSED
    streak >= 7 -> Mood.LOVE
    streak >= 3 -> Mood.EXCITED
    hasBudget && dayOfMonth >= 15 && budgetProgress < 0.4f -> Mood.SMART
    daysSinceLastLog >= 1 -> Mood.NEUTRAL
    else -> Mood.HAPPY
}
