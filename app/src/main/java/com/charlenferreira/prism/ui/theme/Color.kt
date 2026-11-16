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
    LATTE(
        displayName = "Bege Latte",
        subtitle = "Creme pastel suave",
        swatchPrimary = Color(0xFFF2E5D5),
        swatchSecondary = Color(0xFFC59B76)
    ),
    CLEAN_WHITE(
        displayName = "Branco Clean",
        subtitle = "Branco cristalino",
        swatchPrimary = Color(0xFFFFFFFF),
        swatchSecondary = Color(0xFFC7D4EE)
    ),
    AMOLED_BLACK(
        displayName = "Preto AMOLED",
        subtitle = "#000000 absoluto",
        swatchPrimary = Color(0xFF000000),
        swatchSecondary = Color(0xFF1A1A1E)
    ),
    DEEP_TECH_BLUE(
        displayName = "Azul Deep Tech",
        subtitle = "Azul céu pastel",
        swatchPrimary = Color(0xFFA9D2FF),
        swatchSecondary = Color(0xFF5D9CE6)
    ),
    CRIMSON_RED(
        displayName = "Vermelho Crimson",
        subtitle = "Rosa rubi pastel",
        swatchPrimary = Color(0xFFFFB5C4),
        swatchSecondary = Color(0xFFE36480)
    ),
    EMERALD_GREEN(
        displayName = "Verde Esmeralda",
        subtitle = "Esmeralda vivo",
        swatchPrimary = Color(0xFF34F09C),
        swatchSecondary = Color(0xFF00B86B)
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
    val glassSpecularTop: Color,
    val glassBorderStart: Color,
    val glassBorderMid: Color,
    val glassBorderEnd: Color,
    val rawBadgeContainer: Color,
    val rawBadgeContent: Color,
    val isDarkSurface: Boolean
)

fun isDarkThemeFor(theme: PrismTheme, systemDark: Boolean): Boolean {
    return when (theme) {
        PrismTheme.LATTE, PrismTheme.CLEAN_WHITE -> false
        PrismTheme.AMOLED_BLACK -> true
        PrismTheme.DEEP_TECH_BLUE,
        PrismTheme.CRIMSON_RED,
        PrismTheme.EMERALD_GREEN -> systemDark
    }
}

fun getColorScheme(theme: PrismTheme, darkTheme: Boolean): ColorScheme {
    val effectiveDark = isDarkThemeFor(theme, darkTheme)
    return when (theme) {
        PrismTheme.LATTE -> lightColorScheme(
            primary = Color(0xFFA67B5B),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFEDDDC9),
            onPrimaryContainer = Color(0xFF3B2717),
            secondary = Color(0xFFC19A78),
            onSecondary = Color(0xFFFFFFFF),
            background = Color(0xFFF6EDE2),
            onBackground = Color(0xFF34271D),
            surface = Color(0xFFFAF3EA),
            onSurface = Color(0xFF34271D),
            surfaceVariant = Color(0xFFEBDCCF),
            onSurfaceVariant = Color(0xFF6B5849),
            outline = Color(0xFFCEBBA8)
        )

        PrismTheme.CLEAN_WHITE -> lightColorScheme(
            primary = Color(0xFF657EE4),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE8EEFF),
            onPrimaryContainer = Color(0xFF1F2B56),
            secondary = Color(0xFF8FA3D8),
            onSecondary = Color(0xFFFFFFFF),
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1B2230),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1B2230),
            surfaceVariant = Color(0xFFF1F4F9),
            onSurfaceVariant = Color(0xFF606C82),
            outline = Color(0xFFD9E1EE)
        )

        PrismTheme.AMOLED_BLACK -> darkColorScheme(
            primary = Color(0xFFD6E0FF),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0xFF1C1D22),
            onPrimaryContainer = Color(0xFFF4F6FB),
            secondary = Color(0xFFA8B3CF),
            onSecondary = Color(0xFF000000),
            background = Color(0xFF000000),
            onBackground = Color(0xFFF5F5F7),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFF5F5F7),
            surfaceVariant = Color(0xFF121215),
            onSurfaceVariant = Color(0xFFA1A1AA),
            outline = Color(0xFF27272C)
        )

        PrismTheme.DEEP_TECH_BLUE -> if (effectiveDark) {
            darkColorScheme(
                primary = Color(0xFF8CC8FF),
                onPrimary = Color(0xFF062038),
                primaryContainer = Color(0xFF163554),
                onPrimaryContainer = Color(0xFFDCEEFF),
                secondary = Color(0xFFA8B8FF),
                onSecondary = Color(0xFF10193C),
                background = Color(0xFF0B1624),
                onBackground = Color(0xFFEBF4FF),
                surface = Color(0xFF112033),
                onSurface = Color(0xFFEBF4FF),
                surfaceVariant = Color(0xFF1A2F4A),
                onSurfaceVariant = Color(0xFFA9C3DF),
                outline = Color(0xFF314F73)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF5698E2),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFDCEEFF),
                onPrimaryContainer = Color(0xFF0D2E52),
                secondary = Color(0xFF84A9F5),
                onSecondary = Color(0xFFFFFFFF),
                background = Color(0xFFEFF6FF),
                onBackground = Color(0xFF162B45),
                surface = Color(0xFFF7FAFF),
                onSurface = Color(0xFF162B45),
                surfaceVariant = Color(0xFFE0EEFD),
                onSurfaceVariant = Color(0xFF4B6788),
                outline = Color(0xFFB7D2F0)
            )
        }

        PrismTheme.CRIMSON_RED -> if (effectiveDark) {
            darkColorScheme(
                primary = Color(0xFFFF96AA),
                onPrimary = Color(0xFF3A0914),
                primaryContainer = Color(0xFF4D1826),
                onPrimaryContainer = Color(0xFFFFE0E7),
                secondary = Color(0xFFFFB2A6),
                onSecondary = Color(0xFF3B120C),
                background = Color(0xFF1B0B10),
                onBackground = Color(0xFFFFF0F3),
                surface = Color(0xFF251017),
                onSurface = Color(0xFFFFF0F3),
                surfaceVariant = Color(0xFF361923),
                onSurfaceVariant = Color(0xFFE2B5C0),
                outline = Color(0xFF633240)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFDF5C79),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFFE1E8),
                onPrimaryContainer = Color(0xFF4A0E1C),
                secondary = Color(0xFFF28B9E),
                onSecondary = Color(0xFFFFFFFF),
                background = Color(0xFFFFF2F4),
                onBackground = Color(0xFF381620),
                surface = Color(0xFFFFF9FA),
                onSurface = Color(0xFF381620),
                surfaceVariant = Color(0xFFFCE2E8),
                onSurfaceVariant = Color(0xFF7B4856),
                outline = Color(0xFFF0BCC7)
            )
        }

        PrismTheme.EMERALD_GREEN -> if (effectiveDark) {
            darkColorScheme(
                primary = Color(0xFF1AE68C),
                onPrimary = Color(0xFF002615),
                primaryContainer = Color(0xFF0A4A30),
                onPrimaryContainer = Color(0xFFC8FFDF),
                secondary = Color(0xFF5AF2B0),
                onSecondary = Color(0xFF002414),
                background = Color(0xFF041A11),
                onBackground = Color(0xFFE7FFF3),
                surface = Color(0xFF09261A),
                onSurface = Color(0xFFE7FFF3),
                surfaceVariant = Color(0xFF103827),
                onSurfaceVariant = Color(0xFFA1DEC0),
                outline = Color(0xFF226146)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF00BE6B),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFC9FCE2),
                onPrimaryContainer = Color(0xFF00331C),
                secondary = Color(0xFF2EE08E),
                onSecondary = Color(0xFF002916),
                background = Color(0xFFEAFFF4),
                onBackground = Color(0xFF072B1C),
                surface = Color(0xFFF4FFF9),
                onSurface = Color(0xFF072B1C),
                surfaceVariant = Color(0xFFD5F9E7),
                onSurfaceVariant = Color(0xFF2B694D),
                outline = Color(0xFF9BE4C2)
            )
        }
    }
}

fun getExtendedColors(theme: PrismTheme, darkTheme: Boolean): PrismExtendedColors {
    val effectiveDark = isDarkThemeFor(theme, darkTheme)
    return when (theme) {
        PrismTheme.LATTE -> PrismExtendedColors(
            backgroundTop = Color(0xFFF9F3EB),
            backgroundBottom = Color(0xFFEEE1D0),
            ambientOrbPrimary = Color(0x36E8C8A4),
            ambientOrbSecondary = Color(0x2CD9B38C),
            glassFillTop = Color(0xCCFFFDF9),
            glassFillBottom = Color(0x96F7ECE0),
            glassSpecularTop = Color(0x66FFFFFF),
            glassBorderStart = Color(0xF5FFFFFF),
            glassBorderMid = Color(0x60E4D2BE),
            glassBorderEnd = Color(0x99CFAF8F),
            rawBadgeContainer = Color(0xE8A67B5B),
            rawBadgeContent = Color(0xFFFFFFFF),
            isDarkSurface = false
        )

        PrismTheme.CLEAN_WHITE -> PrismExtendedColors(
            backgroundTop = Color(0xFFFFFFFF),
            backgroundBottom = Color(0xFFF3F6FB),
            ambientOrbPrimary = Color(0x2CD5E4FF),
            ambientOrbSecondary = Color(0x24E2DCFF),
            glassFillTop = Color(0xD8FFFFFF),
            glassFillBottom = Color(0xA6F7F9FD),
            glassSpecularTop = Color(0x70FFFFFF),
            glassBorderStart = Color(0xFFFFFFFF),
            glassBorderMid = Color(0x5CE0E7F3),
            glassBorderEnd = Color(0x88C2CEE4),
            rawBadgeContainer = Color(0xEA657EE4),
            rawBadgeContent = Color(0xFFFFFFFF),
            isDarkSurface = false
        )

        PrismTheme.AMOLED_BLACK -> PrismExtendedColors(
            backgroundTop = Color(0xFF000000),
            backgroundBottom = Color(0xFF000000),
            ambientOrbPrimary = Color.Transparent,
            ambientOrbSecondary = Color.Transparent,
            glassFillTop = Color(0xE6111114),
            glassFillBottom = Color(0xD608080A),
            glassSpecularTop = Color(0x16FFFFFF),
            glassBorderStart = Color(0x52FFFFFF),
            glassBorderMid = Color(0x1CFFFFFF),
            glassBorderEnd = Color(0x32FFFFFF),
            rawBadgeContainer = Color(0xE8D6E0FF),
            rawBadgeContent = Color(0xFF000000),
            isDarkSurface = true
        )

        PrismTheme.DEEP_TECH_BLUE -> if (effectiveDark) {
            PrismExtendedColors(
                backgroundTop = Color(0xFF0B1726),
                backgroundBottom = Color(0xFF060E18),
                ambientOrbPrimary = Color(0x3669B6FF),
                ambientOrbSecondary = Color(0x2899ADFF),
                glassFillTop = Color(0x66162B45),
                glassFillBottom = Color(0x480E1C2F),
                glassSpecularTop = Color(0x26FFFFFF),
                glassBorderStart = Color(0x7ABCE0FF),
                glassBorderMid = Color(0x2E74B3F5),
                glassBorderEnd = Color(0x4D94B8FF),
                rawBadgeContainer = Color(0xEB8CC8FF),
                rawBadgeContent = Color(0xFF051C30),
                isDarkSurface = true
            )
        } else {
            PrismExtendedColors(
                backgroundTop = Color(0xFFF3F8FF),
                backgroundBottom = Color(0xFFE0EEFD),
                ambientOrbPrimary = Color(0x389ECAFF),
                ambientOrbSecondary = Color(0x2EB8C8FF),
                glassFillTop = Color(0xD4FFFFFF),
                glassFillBottom = Color(0x9EE9F3FF),
                glassSpecularTop = Color(0x66FFFFFF),
                glassBorderStart = Color(0xF8FFFFFF),
                glassBorderMid = Color(0x5CADD1FA),
                glassBorderEnd = Color(0x8884B8F5),
                rawBadgeContainer = Color(0xEB5698E2),
                rawBadgeContent = Color(0xFFFFFFFF),
                isDarkSurface = false
            )
        }

        PrismTheme.CRIMSON_RED -> if (effectiveDark) {
            PrismExtendedColors(
                backgroundTop = Color(0xFF1F0B11),
                backgroundBottom = Color(0xFF120509),
                ambientOrbPrimary = Color(0x34FF849D),
                ambientOrbSecondary = Color(0x28FFA8B8),
                glassFillTop = Color(0x68351520),
                glassFillBottom = Color(0x48220C14),
                glassSpecularTop = Color(0x24FFFFFF),
                glassBorderStart = Color(0x78FFD4DE),
                glassBorderMid = Color(0x2EFF8EA6),
                glassBorderEnd = Color(0x4DFFAEBF),
                rawBadgeContainer = Color(0xEBFF96AA),
                rawBadgeContent = Color(0xFF330610),
                isDarkSurface = true
            )
        } else {
            PrismExtendedColors(
                backgroundTop = Color(0xFFFFF5F7),
                backgroundBottom = Color(0xFFFEE5EA),
                ambientOrbPrimary = Color(0x36FFB5C4),
                ambientOrbSecondary = Color(0x2AFFC9D4),
                glassFillTop = Color(0xD4FFFFFF),
                glassFillBottom = Color(0x9EFFEBF0),
                glassSpecularTop = Color(0x66FFFFFF),
                glassBorderStart = Color(0xF8FFFFFF),
                glassBorderMid = Color(0x5CF7B6C4),
                glassBorderEnd = Color(0x88EC8A9F),
                rawBadgeContainer = Color(0xEBDF5C79),
                rawBadgeContent = Color(0xFFFFFFFF),
                isDarkSurface = false
            )
        }

        PrismTheme.EMERALD_GREEN -> if (effectiveDark) {
            PrismExtendedColors(
                backgroundTop = Color(0xFF051D13),
                backgroundBottom = Color(0xFF02110B),
                ambientOrbPrimary = Color(0x381AE68C),
                ambientOrbSecondary = Color(0x285AF2B0),
                glassFillTop = Color(0x660E3524),
                glassFillBottom = Color(0x48072015),
                glassSpecularTop = Color(0x26FFFFFF),
                glassBorderStart = Color(0x78BAFFE0),
                glassBorderMid = Color(0x2E2AE695),
                glassBorderEnd = Color(0x4D5AF2B0),
                rawBadgeContainer = Color(0xEB1AE68C),
                rawBadgeContent = Color(0xFF002414),
                isDarkSurface = true
            )
        } else {
            PrismExtendedColors(
                backgroundTop = Color(0xFFEEFFF6),
                backgroundBottom = Color(0xFFD6FBE8),
                ambientOrbPrimary = Color(0x3B4CF5A7),
                ambientOrbSecondary = Color(0x2E82F7C3),
                glassFillTop = Color(0xD4FFFFFF),
                glassFillBottom = Color(0x9EE5FDF0),
                glassSpecularTop = Color(0x66FFFFFF),
                glassBorderStart = Color(0xF8FFFFFF),
                glassBorderMid = Color(0x5C8AEBC0),
                glassBorderEnd = Color(0x882DD488),
                rawBadgeContainer = Color(0xEB00BE6B),
                rawBadgeContent = Color(0xFFFFFFFF),
                isDarkSurface = false
            )
        }
    }
}
