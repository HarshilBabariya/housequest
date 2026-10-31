package com.homequest.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.homequest.app.domain.Accessories
import com.homequest.app.domain.AvatarConfig
import com.homequest.app.domain.HairColors
import com.homequest.app.domain.HairStyles
import com.homequest.app.domain.Outfits
import com.homequest.app.domain.SkinTones

@Composable
fun AvatarView(config: AvatarConfig, size: Dp = 96.dp, animated: Boolean = false, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "avatar")
    val bob by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "bob")
    val offset = if (animated) bob else 0f
    Canvas(modifier.size(size)) {
        val u = this.size.width / 100f
        translate(top = offset * 3f * u) {
            drawAvatar(config, u)
        }
    }
}

private fun DrawScope.drawAvatar(config: AvatarConfig, u: Float) {
    val skin = SkinTones.getOrElse(config.skinTone) { SkinTones.first() }
    val hair = HairColors.getOrElse(config.hairColor) { HairColors.first() }
    val outfit = outfitColors(config.outfitId)

    drawOutfit(config.outfitId, outfit, u)
    drawCircle(skin, 8f * u, Offset(32f * u, 74f * u))
    drawCircle(skin, 8f * u, Offset(68f * u, 74f * u))
    drawRoundRect(skin, Offset(42f * u, 52f * u), Size(16f * u, 12f * u), CornerRadius(4f * u))
    drawCircle(skin, 25f * u, Offset(50f * u, 36f * u))
    drawHairBack(config.hairId, hair, u)
    drawCircle(Color(0xFF2B2B2B), 2.6f * u, Offset(41f * u, 36f * u))
    drawCircle(Color(0xFF2B2B2B), 2.6f * u, Offset(59f * u, 36f * u))
    drawCircle(Color.White, 0.9f * u, Offset(42f * u, 35f * u))
    drawCircle(Color.White, 0.9f * u, Offset(60f * u, 35f * u))
    drawCircle(Color(0xFFF08080).copy(alpha = 0.5f), 3f * u, Offset(36f * u, 44f * u))
    drawCircle(Color(0xFFF08080).copy(alpha = 0.5f), 3f * u, Offset(64f * u, 44f * u))
    val smile = Path().apply {
        moveTo(43f * u, 46f * u)
        quadraticTo(50f * u, 53f * u, 57f * u, 46f * u)
    }
    drawPath(smile, Color(0xFF8B3A3A), style = Stroke(1.8f * u))
    drawHairFront(config.hairId, hair, u)
    drawAccessory(config.accessoryId, u)
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
            val lapel = Path().apply {
                moveTo(42f * u, 60f * u); lineTo(50f * u, 80f * u); lineTo(58f * u, 60f * u); close()
            }
            drawPath(lapel, colors.accent)
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
            val emblem = Path().apply {
                moveTo(50f * u, 66f * u); lineTo(58f * u, 72f * u); lineTo(55f * u, 82f * u)
                lineTo(45f * u, 82f * u); lineTo(42f * u, 72f * u); close()
            }
            drawPath(emblem, colors.accent)
            drawRect(Color(0xFF1565C0), Offset(30f * u, 92f * u), Size(40f * u, 8f * u))
        }
        else -> drawRect(colors.accent, Offset(30f * u, 60f * u), Size(6f * u, 10f * u))
    }
}

private fun DrawScope.drawHairBack(id: String, color: Color, u: Float) {
    when (id) {
        HairStyles.LONG -> drawRoundRect(color, Offset(24f * u, 22f * u), Size(52f * u, 50f * u), CornerRadius(20f * u))
        HairStyles.CURLY -> {
            listOf(22f to 30f, 78f to 30f, 20f to 45f, 80f to 45f, 26f to 58f, 74f to 58f).forEach { (x, y) ->
                drawCircle(color, 8f * u, Offset(x * u, y * u))
            }
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
            listOf(30f to 16f, 42f to 10f, 58f to 10f, 70f to 16f).forEach { (x, y) ->
                drawCircle(color, 7f * u, Offset(x * u, y * u))
            }
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
            val crown = Path().apply {
                moveTo(32f * u, 20f * u); lineTo(32f * u, 4f * u); lineTo(41f * u, 12f * u); lineTo(50f * u, 2f * u)
                lineTo(59f * u, 12f * u); lineTo(68f * u, 4f * u); lineTo(68f * u, 20f * u); close()
            }
            drawPath(crown, Color(0xFFFFC107))
            drawCircle(Color(0xFFE53935), 2.5f * u, Offset(50f * u, 14f * u))
        }
    }
}
