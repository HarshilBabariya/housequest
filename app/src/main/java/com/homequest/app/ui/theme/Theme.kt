package com.homequest.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homequest.app.data.repository.DarkMode

private val AppTypography = Typography().let { base ->
    base.copy(
        displayMedium = base.displayMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, letterSpacing = (-1).sp),
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
        labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.3.sp),
        labelMedium = base.labelMedium.copy(fontWeight = FontWeight.Medium)
    )
}

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun HomeQuestTheme(
    themeId: String,
    darkMode: DarkMode,
    content: @Composable () -> Unit
) {
    val dark = when (darkMode) {
        DarkMode.SYSTEM -> isSystemInDarkTheme()
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
    }
    val palette = Palettes.forId(themeId)
    val target = palette.scheme(dark).let { if (palette.glass) it.glassy(dark) else it }
    val spec = tween<Color>(500)
    val primary by animateColorAsState(target.primary, spec, label = "primary")
    val secondary by animateColorAsState(target.secondary, spec, label = "secondary")
    val tertiary by animateColorAsState(target.tertiary, spec, label = "tertiary")
    val background by animateColorAsState(target.background, spec, label = "background")
    val surface by animateColorAsState(target.surface, spec, label = "surface")
    val primaryContainer by animateColorAsState(target.primaryContainer, spec, label = "primaryContainer")
    val secondaryContainer by animateColorAsState(target.secondaryContainer, spec, label = "secondaryContainer")
    val surfaceContainer by animateColorAsState(target.surfaceContainer, spec, label = "surfaceContainer")

    MaterialTheme(
        colorScheme = target.copy(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            background = background,
            surface = surface,
            primaryContainer = primaryContainer,
            secondaryContainer = secondaryContainer,
            surfaceContainer = surfaceContainer,
            surfaceContainerLow = surfaceContainer
        ),
        typography = AppTypography,
        shapes = AppShapes
    ) {
        if (palette.glass) GlassBackdrop(palette, dark, content) else content()
    }
}

@Composable
private fun GlassBackdrop(palette: Palette, dark: Boolean, content: @Composable () -> Unit) {
    val glow = if (dark) 0.45f else 0.38f
    Box(
        Modifier
            .fillMaxSize()
            .background(if (dark) palette.darkBackground else palette.lightBackground)
            .drawBehind {
                listOf(
                    Triple(palette.primary, Offset(0.1f, 0.1f), 0.85f),
                    Triple(palette.tertiary, Offset(0.95f, 0.45f), 0.7f),
                    Triple(palette.secondary, Offset(0.25f, 0.92f), 0.8f)
                ).forEach { (color, at, r) ->
                    val center = Offset(size.width * at.x, size.height * at.y)
                    val radius = size.width * r
                    drawCircle(Brush.radialGradient(listOf(color.copy(alpha = glow), Color.Transparent), center, radius), radius, center)
                }
            }
    ) { content() }
}
