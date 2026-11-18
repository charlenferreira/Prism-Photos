package com.charlenferreira.prism.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

enum class PrismTheme(
    val displayName: String,
    val subtitle: String,
    val swatchPrimary: Color,
    val swatchSecondary: Color
) {
    BEIGE(
        displayName = "Bege",
        subtitle = "Areia & creme suave",
        swatchPrimary = Color(0xFFF5ECE0),
        swatchSecondary = Color(0xFFC7A27C)
    ),
    WHITE(
        displayName = "Branco",
        subtitle = "Branco puro iOS",
        swatchPrimary = Color(0xFFFFFFFF),
        swatchSecondary = Color(0xFFD8E4F8)
    ),
    AMOLED_BLACK(
        displayName = "Preto",
        subtitle = "#000000 AMOLED",
        swatchPrimary = Color(0xFF000000),
        swatchSecondary = Color(0xFF24262C)
    ),
    LIGHT_GRAY(
        displayName = "Cinza Claro",
        subtitle = "Papel & alumínio suave",
        swatchPrimary = Color(0xFFEBECEF),
        swatchSecondary = Color(0xFFBFC4CE)
    )
}

@Immutable
data class PrismExtendedColors(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val ambientOrbPrimary: Color,
    val ambientOrbSecondary: Color,
    val glassFillTop: Color,
    val glassFillBottom: Color,
    val glassBorderStart: Color,
    val glassBorderMid: Color,
    val glassBorderEnd: Color,
    val rawBadgeContainer: Color,
    val rawBadgeContent: Color,
    val isDarkSurface: Boolean
)

fun getColorScheme(theme: PrismTheme, @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false): ColorScheme {
    return when (theme) {
        PrismTheme.BEIGE -> lightColorScheme(
            primary = Color(0xFF8C6444),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFF0DFCA),
            onPrimaryContainer = Color(0xFF331F10),
            secondary = Color(0xFFA88263),
            onSecondary = Color(0xFFFFFFFF),
            background = Color(0xFFF5ECE0),
            onBackground = Color(0xFF261B12),
            surface = Color(0xFFFAF3EA),
            onSurface = Color(0xFF261B12),
            surfaceVariant = Color(0xFFEBE0D0),
            onSurfaceVariant = Color(0xFF665242),
            outline = Color(0xFFD2BFA8)
        )

        PrismTheme.WHITE -> lightColorScheme(
            primary = Color(0xFF007AFF),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE3F0FF),
            onPrimaryContainer = Color(0xFF002652),
            secondary = Color(0xFF5878A8),
            onSecondary = Color(0xFFFFFFFF),
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF111318),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF111318),
            surfaceVariant = Color(0xFFF2F4F8),
            onSurfaceVariant = Color(0xFF5A6270),
            outline = Color(0xFFD7DCE6)
        )

        PrismTheme.AMOLED_BLACK -> darkColorScheme(
            primary = Color(0xFF0A84FF),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF181B22),
            onPrimaryContainer = Color(0xFFE8F1FF),
            secondary = Color(0xFF9BA8C2),
            onSecondary = Color(0xFF0A0C10),
            background = Color(0xFF000000),
            onBackground = Color(0xFFF7F8FA),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFF7F8FA),
            surfaceVariant = Color(0xFF121318),
            onSurfaceVariant = Color(0xFFA4AAB8),
            outline = Color(0xFF2A2C34)
        )

        PrismTheme.LIGHT_GRAY -> lightColorScheme(
            primary = Color(0xFF3A6AE0),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFDFE6F5),
            onPrimaryContainer = Color(0xFF14244A),
            secondary = Color(0xFF6B7486),
            onSecondary = Color(0xFFFFFFFF),
            background = Color(0xFFEBECEF),
            onBackground = Color(0xFF181B20),
            surface = Color(0xFFF3F4F6),
            onSurface = Color(0xFF181B20),
            surfaceVariant = Color(0xFFDFE2E8),
            onSurfaceVariant = Color(0xFF545B68),
            outline = Color(0xFFC5CAD4)
        )
    }
}

fun getExtendedColors(theme: PrismTheme, @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false): PrismExtendedColors {
    return when (theme) {
        PrismTheme.BEIGE -> PrismExtendedColors(
            backgroundTop = Color(0xFFF7EFE4),
            backgroundBottom = Color(0xFFEDE0CE),
            ambientOrbPrimary = Color.Transparent,
            ambientOrbSecondary = Color.Transparent,
            glassFillTop = Color(0xCCFFFBF5),
            glassFillBottom = Color(0xB3F5E9D9),
            glassBorderStart = Color(0xE6FFFFFF),
            glassBorderMid = Color(0x55E0C9AE),
            glassBorderEnd = Color(0x80C7AA8A),
            rawBadgeContainer = Color(0xE68C6444),
            rawBadgeContent = Color(0xFFFFFFFF),
            isDarkSurface = false
        )

        PrismTheme.WHITE -> PrismExtendedColors(
            backgroundTop = Color(0xFFFFFFFF),
            backgroundBottom = Color(0xFFF7F8FA),
            ambientOrbPrimary = Color.Transparent,
            ambientOrbSecondary = Color.Transparent,
            glassFillTop = Color(0xCCFFFFFF),
            glassFillBottom = Color(0xB3F2F5FA),
            glassBorderStart = Color(0xF0FFFFFF),
            glassBorderMid = Color(0x5CD0DAE8),
            glassBorderEnd = Color(0x85B5C2D6),
            rawBadgeContainer = Color(0xE6007AFF),
            rawBadgeContent = Color(0xFFFFFFFF),
            isDarkSurface = false
        )

        PrismTheme.AMOLED_BLACK -> PrismExtendedColors(
            backgroundTop = Color(0xFF000000),
            backgroundBottom = Color(0xFF000000),
            ambientOrbPrimary = Color.Transparent,
            ambientOrbSecondary = Color.Transparent,
            glassFillTop = Color(0x991C1E24),
            glassFillBottom = Color(0x880E1014),
            glassBorderStart = Color(0x4DFFFFFF),
            glassBorderMid = Color(0x1EFFFFFF),
            glassBorderEnd = Color(0x33A6B0C8),
            rawBadgeContainer = Color(0xE60A84FF),
            rawBadgeContent = Color(0xFFFFFFFF),
            isDarkSurface = true
        )

        PrismTheme.LIGHT_GRAY -> PrismExtendedColors(
            backgroundTop = Color(0xFFEBECEF),
            backgroundBottom = Color(0xFFE2E4E9),
            ambientOrbPrimary = Color.Transparent,
            ambientOrbSecondary = Color.Transparent,
            glassFillTop = Color(0xCCF7F8FA),
            glassFillBottom = Color(0xB3E8EAEF),
            glassBorderStart = Color(0xEBFFFFFF),
            glassBorderMid = Color(0x5CC6CBD6),
            glassBorderEnd = Color(0x85ADB4C2),
            rawBadgeContainer = Color(0xE63A6AE0),
            rawBadgeContent = Color(0xFFFFFFFF),
            isDarkSurface = false
        )
    }
}
