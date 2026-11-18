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
        subtitle = "Areia quente & creme suave",
        swatchPrimary = Color(0xFFF4EAE0),
        swatchSecondary = Color(0xFFC5A888)
    ),
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
    ),
    LIGHT_GRAY(
        displayName = "Cinza Claro",
        subtitle = "Papel fosco & alumínio suave",
        swatchPrimary = Color(0xFFECEEF2),
        swatchSecondary = Color(0xFFB8BEC9)
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

fun getColorScheme(theme: PrismTheme, darkTheme: Boolean): ColorScheme {
    return when (theme) {
        PrismTheme.BEIGE -> lightColorScheme(
            primary = Color(0xFF7D5A38),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEBD9C5),
            onPrimaryContainer = Color(0xFF2E1E0E),
            secondary = Color(0xFF6E5D4F),
            onSecondary = Color(0xFFFFFFFF),
            background = Color(0xFFF6EFE7),
            onBackground = Color(0xFF211A14),
            surface = Color(0xFFF6EFE7),
            onSurface = Color(0xFF211A14),
            surfaceVariant = Color(0xFFE9DEC9),
            onSurfaceVariant = Color(0xFF52463B),
            outline = Color(0xFF8A7B6D)
        )
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
        PrismTheme.LIGHT_GRAY -> lightColorScheme(
            primary = Color(0xFF2C3E50),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD9E1E8),
            onPrimaryContainer = Color(0xFF141D26),
            secondary = Color(0xFF5A6572),
            onSecondary = Color(0xFFFFFFFF),
            background = Color(0xFFECEEF2),
            onBackground = Color(0xFF181A1F),
            surface = Color(0xFFECEEF2),
            onSurface = Color(0xFF181A1F),
            surfaceVariant = Color(0xFFDFE3EA),
            onSurfaceVariant = Color(0xFF4A505A),
            outline = Color(0xFF98A0AC)
        )
    }
}

fun getExtendedColors(theme: PrismTheme, darkTheme: Boolean): PrismExtendedColors {
    return when (theme) {
        PrismTheme.BEIGE -> PrismExtendedColors(
            backgroundTop = Color(0xFFF6EFE7),
            backgroundBottom = Color(0xFFEDE2D4),
            ambientOrbPrimary = Color(0xFFDCC3A6).copy(alpha = 0.28f),
            ambientOrbSecondary = Color(0xFFE6D2BC).copy(alpha = 0.24f),
            glassFillTop = Color(0xFFFDF9F4).copy(alpha = 0.86f),
            glassFillBottom = Color(0xFFF2E7DA).copy(alpha = 0.76f),
            glassBorderStart = Color.White.copy(alpha = 0.90f),
            glassBorderMid = Color(0xFFC5A888).copy(alpha = 0.24f),
            glassBorderEnd = Color(0xFF9C8269).copy(alpha = 0.34f),
            rawBadgeContainer = Color(0xFF7D5A38).copy(alpha = 0.92f),
            rawBadgeContent = Color.White,
            isDarkSurface = false
        )
        PrismTheme.WHITE -> PrismExtendedColors(
            backgroundTop = Color(0xFFFFFFFF),
            backgroundBottom = Color(0xFFF5F6F8),
            ambientOrbPrimary = Color(0xFFD6E6FF).copy(alpha = 0.26f),
            ambientOrbSecondary = Color(0xFFE8ECF2).copy(alpha = 0.24f),
            glassFillTop = Color(0xFFFFFFFF).copy(alpha = 0.88f),
            glassFillBottom = Color(0xFFF4F5F7).copy(alpha = 0.78f),
            glassBorderStart = Color.White.copy(alpha = 0.96f),
            glassBorderMid = Color(0xFF007AFF).copy(alpha = 0.14f),
            glassBorderEnd = Color(0xFFC7C7CC).copy(alpha = 0.42f),
            rawBadgeContainer = Color(0xFF1C1C1E).copy(alpha = 0.84f),
            rawBadgeContent = Color.White,
            isDarkSurface = false
        )
        PrismTheme.AMOLED_BLACK -> PrismExtendedColors(
            backgroundTop = Color(0xFF000000),
            backgroundBottom = Color(0xFF000000),
            ambientOrbPrimary = Color.Transparent,
            ambientOrbSecondary = Color.Transparent,
            glassFillTop = Color(0xFF1C1C1E).copy(alpha = 0.82f),
            glassFillBottom = Color(0xFF121214).copy(alpha = 0.74f),
            glassBorderStart = Color.White.copy(alpha = 0.26f),
            glassBorderMid = Color.White.copy(alpha = 0.10f),
            glassBorderEnd = Color.White.copy(alpha = 0.18f),
            rawBadgeContainer = Color(0xFF2C2C2E).copy(alpha = 0.90f),
            rawBadgeContent = Color.White,
            isDarkSurface = true
        )
        PrismTheme.LIGHT_GRAY -> PrismExtendedColors(
            backgroundTop = Color(0xFFECEEF2),
            backgroundBottom = Color(0xFFE2E5EC),
            ambientOrbPrimary = Color(0xFFD0D6E2).copy(alpha = 0.28f),
            ambientOrbSecondary = Color(0xFFDAE0EA).copy(alpha = 0.24f),
            glassFillTop = Color(0xFFF7F8FA).copy(alpha = 0.86f),
            glassFillBottom = Color(0xFFE6E9F0).copy(alpha = 0.76f),
            glassBorderStart = Color.White.copy(alpha = 0.92f),
            glassBorderMid = Color(0xFF5A6572).copy(alpha = 0.16f),
            glassBorderEnd = Color(0xFF98A0AC).copy(alpha = 0.38f),
            rawBadgeContainer = Color(0xFF2C3E50).copy(alpha = 0.90f),
            rawBadgeContent = Color.White,
            isDarkSurface = false
        )
    }
}
