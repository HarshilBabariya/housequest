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
            primary = Color(0xFFFF6B35), secondary = Color(0xFFFFB627), tertiary = Color(0xFF2EC4B6),
            lightBackground = Color(0xFFFFF8F3), lightSurface = Color(0xFFFFFFFF),
            darkBackground = Color(0xFF1A1210), darkSurface = Color(0xFF26201C)
        ),
        ThemeIds.OCEAN to Palette(
            primary = Color(0xFF0077B6), secondary = Color(0xFF00B4D8), tertiary = Color(0xFF90E0EF),
            lightBackground = Color(0xFFF0F9FF), lightSurface = Color(0xFFFFFFFF),
            darkBackground = Color(0xFF0B1A26), darkSurface = Color(0xFF122636)
        ),
        ThemeIds.FOREST to Palette(
            primary = Color(0xFF2D6A4F), secondary = Color(0xFF52B788), tertiary = Color(0xFFD8F3DC),
            lightBackground = Color(0xFFF3FAF5), lightSurface = Color(0xFFFFFFFF),
            darkBackground = Color(0xFF0E1A14), darkSurface = Color(0xFF17261E)
        ),
        ThemeIds.MIDNIGHT to Palette(
            primary = Color(0xFF7C4DFF), secondary = Color(0xFF00E5FF), tertiary = Color(0xFFFF4081),
            lightBackground = Color(0xFFF4F1FF), lightSurface = Color(0xFFFFFFFF),
            darkBackground = Color(0xFF0D0B1A), darkSurface = Color(0xFF181430)
        ),
        ThemeIds.CANDY to Palette(
            primary = Color(0xFFFF4F9A), secondary = Color(0xFFB388FF), tertiary = Color(0xFFFFD166),
            lightBackground = Color(0xFFFFF3F8), lightSurface = Color(0xFFFFFFFF),
            darkBackground = Color(0xFF1F1119), darkSurface = Color(0xFF2E1A26)
        ),
        ThemeIds.ROYAL to Palette(
            primary = Color(0xFF6A1B9A), secondary = Color(0xFFFFC107), tertiary = Color(0xFF26A69A),
            lightBackground = Color(0xFFF8F3FB), lightSurface = Color(0xFFFFFFFF),
            darkBackground = Color(0xFF150B1C), darkSurface = Color(0xFF23142C)
        )
    )

    fun forId(id: String): Palette = byId[id] ?: byId.getValue(ThemeIds.SUNRISE)
}
