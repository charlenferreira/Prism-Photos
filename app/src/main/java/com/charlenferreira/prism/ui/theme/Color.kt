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
    WHITE(
        displayName = "Branco",
        subtitle = "Branco puro estilo iOS",
        swatchPrimary = Color(0xFFFFFFFF),
        swatchSecondary = Color(0xFFE5E7EB)
    ),
    AMOLED_BLACK(
        displayName = "Preto",
        subtitle = "Preto puro #000000 AMOLED",
        swatchPrimary = Color(0xFF000000),
        swatchSecondary = Color(0xFF262629)
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
        PrismTheme.WHITE -> lightColorScheme(
            primary = Color(0xFF007AFF),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE1F0FF),
            onPrimaryContainer = Color(0xFF001D36),
            secondary = Color(0xFF5856D6),
            onSecondary = Color(0xFFFFFFFF),
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF111113),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF111113),
            surfaceVariant = Color(0xFFF2F2F7),
            onSurfaceVariant = Color(0xFF48484A),
            outline = Color(0xFFAEAEB2)
        )
        PrismTheme.AMOLED_BLACK -> darkColorScheme(
            primary = Color(0xFF0A84FF),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFF1C2B3D),
            onPrimaryContainer = Color(0xFFD6E7FF),
            secondary = Color(0xFF8E8E93),
            onSecondary = Color(0xFF000000),
            background = Color(0xFF000000),
            onBackground = Color(0xFFF5F5F7),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFF5F5F7),
            surfaceVariant = Color(0xFF161618),
            onSurfaceVariant = Color(0xFFA1A1A6),
            outline = Color(0xFF3A3A3C)
        )
    }
}

fun getExtendedColors(theme: PrismTheme, @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = false): PrismExtendedColors {
    return when (theme) {
        PrismTheme.WHITE -> PrismExtendedColors(
            backgroundTop = Color(0xFFFFFFFF),
            backgroundBottom = Color(0xFFF5F6F8),
            ambientOrbPrimary = Color.Transparent,
            ambientOrbSecondary = Color.Transparent,
            glassFillTop = Color(0xF0FFFFFF),
            glassFillBottom = Color(0xE6F4F5F7),
            glassBorderStart = Color(0x14000000),
            glassBorderMid = Color(0x0F000000),
            glassBorderEnd = Color(0x18000000),
            rawBadgeContainer = Color(0xD91C1C1E),
            rawBadgeContent = Color.White,
            isDarkSurface = false
        )
        PrismTheme.AMOLED_BLACK -> PrismExtendedColors(
            backgroundTop = Color(0xFF000000),
            backgroundBottom = Color(0xFF000000),
            ambientOrbPrimary = Color.Transparent,
            ambientOrbSecondary = Color.Transparent,
            glassFillTop = Color(0xEB18181B),
            glassFillBottom = Color(0xE0121214),
            glassBorderStart = Color(0x24FFFFFF),
            glassBorderMid = Color(0x14FFFFFF),
            glassBorderEnd = Color(0x1FFFFFFF),
            rawBadgeContainer = Color(0xE62C2C2E),
            rawBadgeContent = Color.White,
            isDarkSurface = true
        )
    }
}
