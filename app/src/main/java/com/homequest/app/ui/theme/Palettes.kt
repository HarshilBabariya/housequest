package com.homequest.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.homequest.app.domain.ThemeIds

data class Palette(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val lightBackground: Color,
    val lightSurface: Color,
    val darkBackground: Color,
    val darkSurface: Color
) {
    fun scheme(dark: Boolean): ColorScheme = if (dark) {
        darkColorScheme(
            primary = primary.lighten(0.15f),
            onPrimary = Color(0xFF1A1A1A),
            primaryContainer = primary.darken(0.45f),
            onPrimaryContainer = primary.lighten(0.75f),
            secondary = secondary,
            onSecondary = Color(0xFF1A1A1A),
            secondaryContainer = primary.darken(0.55f),
            onSecondaryContainer = primary.lighten(0.8f),
            tertiary = tertiary,
            tertiaryContainer = tertiary.darken(0.5f),
            onTertiaryContainer = tertiary.lighten(0.75f),
            background = darkBackground,
            onBackground = Color(0xFFF2EDE9),
            surface = darkBackground,
            onSurface = Color(0xFFF2EDE9),
            surfaceVariant = darkSurface.lighten(0.06f),
            onSurfaceVariant = Color(0xFFBDB4AE),
            surfaceContainerLowest = darkBackground.darken(0.2f),
            surfaceContainerLow = darkSurface.darken(0.1f),
            surfaceContainer = darkSurface,
            surfaceContainerHigh = darkSurface.lighten(0.05f),
            surfaceContainerHighest = darkSurface.lighten(0.1f),
            outline = Color(0xFF7A716B),
            outlineVariant = darkSurface.lighten(0.14f)
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primary.lighten(0.86f),
            onPrimaryContainer = primary.darken(0.55f),
            secondary = secondary,
            onSecondary = Color(0xFF1A1A1A),
            secondaryContainer = primary.lighten(0.86f),
            onSecondaryContainer = primary.darken(0.55f),
            tertiary = tertiary,
            tertiaryContainer = tertiary.lighten(0.8f),
            onTertiaryContainer = tertiary.darken(0.55f),
            background = lightBackground,
            onBackground = Color(0xFF1E1B18),
            surface = lightBackground,
            onSurface = Color(0xFF1E1B18),
            surfaceVariant = lightBackground.darken(0.04f),
            onSurfaceVariant = Color(0xFF615A55),
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = lightSurface,
            surfaceContainer = lightSurface,
            surfaceContainerHigh = lightBackground.darken(0.025f),
            surfaceContainerHighest = lightBackground.darken(0.05f),
            outline = Color(0xFF8A817B),
            outlineVariant = lightBackground.darken(0.1f)
        )
    }
}

private fun Color.darken(amount: Float) = Color(red * (1 - amount), green * (1 - amount), blue * (1 - amount), alpha)
private fun Color.lighten(amount: Float) = Color(red + (1 - red) * amount, green + (1 - green) * amount, blue + (1 - blue) * amount, alpha)

object Palettes {
    private val byId: Map<String, Palette> = mapOf(
        ThemeIds.SUNRISE to Palette(
            primary = Color(0xFFF2673A), secondary = Color(0xFFF7B32B), tertiary = Color(0xFF2EC4B6),
            lightBackground = Color(0xFFFBF7F3), lightSurface = Color(0xFFFFFFFF),
            darkBackground = Color(0xFF15110F), darkSurface = Color(0xFF211B18)
        ),
        ThemeIds.OCEAN to Palette(
            primary = Color(0xFF2563EB), secondary = Color(0xFF0EA5E9), tertiary = Color(0xFF14B8A6),
            lightBackground = Color(0xFFF4F7FB), lightSurface = Color(0xFFFFFFFF),
            darkBackground = Color(0xFF0B1220), darkSurface = Color(0xFF131C2E)
        ),
        ThemeIds.FOREST to Palette(
            primary = Color(0xFF15803D), secondary = Color(0xFF4ADE80), tertiary = Color(0xFFA3E635),
            lightBackground = Color(0xFFF3F8F4), lightSurface = Color(0xFFFFFFFF),
            darkBackground = Color(0xFF0C1510), darkSurface = Color(0xFF152219)
        ),
        ThemeIds.MIDNIGHT to Palette(
            primary = Color(0xFF7C3AED), secondary = Color(0xFF06B6D4), tertiary = Color(0xFFF472B6),
            lightBackground = Color(0xFFF6F3FD), lightSurface = Color(0xFFFFFFFF),
            darkBackground = Color(0xFF0E0B1A), darkSurface = Color(0xFF181330)
        ),
        ThemeIds.CANDY to Palette(
            primary = Color(0xFFEC4899), secondary = Color(0xFFA78BFA), tertiary = Color(0xFFFBBF24),
            lightBackground = Color(0xFFFDF4F8), lightSurface = Color(0xFFFFFFFF),
            darkBackground = Color(0xFF1C1016), darkSurface = Color(0xFF2A1823)
        ),
        ThemeIds.ROYAL to Palette(
            primary = Color(0xFF6D28D9), secondary = Color(0xFFF59E0B), tertiary = Color(0xFF14B8A6),
            lightBackground = Color(0xFFF7F3FB), lightSurface = Color(0xFFFFFFFF),
            darkBackground = Color(0xFF130B1C), darkSurface = Color(0xFF21142C)
        )
    )

    fun forId(id: String): Palette = byId[id] ?: byId.getValue(ThemeIds.SUNRISE)
}
