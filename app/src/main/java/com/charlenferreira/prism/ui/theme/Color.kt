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
        subtitle = "Creme suave & aveia",
        swatchPrimary = Color(0xFFF3E5D3),
        swatchSecondary = Color(0xFFC29B78)
    ),
    CLEAN_WHITE(
        displayName = "Branco Clean",
        subtitle = "Branco puro & cristal",
        swatchPrimary = Color(0xFFFFFFFF),
        swatchSecondary = Color(0xFFD6E2F5)
    ),
    AMOLED_BLACK(
        displayName = "Preto AMOLED",
        subtitle = "#000000 absoluto",
        swatchPrimary = Color(0xFF000000),
        swatchSecondary = Color(0xFF23252B)
    ),
    DEEP_TECH_BLUE(
        displayName = "Azul Deep Tech",
        subtitle = "Céu pastel & cobalto",
        swatchPrimary = Color(0xFF7CB9FF),
        swatchSecondary = Color(0xFFBFE0FF)
    ),
    CRIMSON_RED(
        displayName = "Vermelho Crimson",
        subtitle = "Rosa coral & rubi suave",
        swatchPrimary = Color(0xFFF2849B),
        swatchSecondary = Color(0xFFFFCCD6)
    ),
    EMERALD_GREEN(
        displayName = "Verde Esmeralda",
        subtitle = "Esmeralda vivo & menta",
        swatchPrimary = Color(0xFF00D07A),
        swatchSecondary = Color(0xFF8EF5C8)
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
        // Always light warm beige pastel
        PrismTheme.LATTE -> lightColorScheme(
            primary = Color(0xFF9C7251),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFF2E2CE),
            onPrimaryContainer = Color(0xFF3A2413),
            secondary = Color(0xFFB58E6D),
            onSecondary = Color(0xFFFFFFFF),
            background = Color(0xFFF6ECE0),
            onBackground = Color(0xFF2D2016),
            surface = Color(0xFFFAF3EA),
            onSurface = Color(0xFF2D2016),
            surfaceVariant = Color(0xFFEDE0D0),
            onSurfaceVariant = Color(0xFF6B5544),
            outline = Color(0xFFD5C1AA)
        )

        // Always pure clean white pastel
        PrismTheme.CLEAN_WHITE -> lightColorScheme(
            primary = Color(0xFF6A85E6),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE7EEFF),
            onPrimaryContainer = Color(0xFF1D2948),
            secondary = Color(0xFF8CA1D6),
            onSecondary = Color(0xFFFFFFFF),
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF151923),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF151923),
            surfaceVariant = Color(0xFFF1F5FA),
            onSurfaceVariant = Color(0xFF5A657A),
            outline = Color(0xFFD9E1EE)
        )

        // Always true pure #000000 AMOLED black
        PrismTheme.AMOLED_BLACK -> darkColorScheme(
            primary = Color(0xFFE6ECFF),
            onPrimary = Color(0xFF050608),
            primaryContainer = Color(0xFF1B1D24),
            onPrimaryContainer = Color(0xFFF2F5FF),
            secondary = Color(0xFFAAB6D3),
            onSecondary = Color(0xFF0A0C10),
            background = Color(0xFF000000),
            onBackground = Color(0xFFF7F8FA),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFF7F8FA),
            surfaceVariant = Color(0xFF111216),
            onSurfaceVariant = Color(0xFFA7ACB8),
            outline = Color(0xFF282A30)
        )

        PrismTheme.DEEP_TECH_BLUE -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF8CCBFF),
                onPrimary = Color(0xFF072138),
                primaryContainer = Color(0xFF133352),
                onPrimaryContainer = Color(0xFFDDF0FF),
                secondary = Color(0xFFA9B9FF),
                onSecondary = Color(0xFF121A3D),
                background = Color(0xFF0A1626),
                onBackground = Color(0xFFEDF5FF),
                surface = Color(0xFF102036),
                onSurface = Color(0xFFEDF5FF),
                surfaceVariant = Color(0xFF182D49),
                onSurfaceVariant = Color(0xFFAEC5DF),
                outline = Color(0xFF2C486C)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF5895E8),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFDCEBFF),
                onPrimaryContainer = Color(0xFF0D2847),
                secondary = Color(0xFF84A9F5),
                onSecondary = Color(0xFFFFFFFF),
                background = Color(0xFFEEF5FF),
                onBackground = Color(0xFF10233D),
                surface = Color(0xFFF7FAFF),
                onSurface = Color(0xFF10233D),
                surfaceVariant = Color(0xFFE1EEFE),
                onSurfaceVariant = Color(0xFF476282),
                outline = Color(0xFFBBD4F2)
            )
        }

        PrismTheme.CRIMSON_RED -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFF96AA),
                onPrimary = Color(0xFF380914),
                primaryContainer = Color(0xFF521827),
                onPrimaryContainer = Color(0xFFFFE0E6),
                secondary = Color(0xFFFFB4A8),
                onSecondary = Color(0xFF3A120C),
                background = Color(0xFF1C0A0F),
                onBackground = Color(0xFFFFF1F4),
                surface = Color(0xFF271017),
                onSurface = Color(0xFFFFF1F4),
                surfaceVariant = Color(0xFF381822),
                onSurfaceVariant = Color(0xFFE5BAC4),
                outline = Color(0xFF662E3C)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFE06680),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFFDFE6),
                onPrimaryContainer = Color(0xFF470B19),
                secondary = Color(0xFFF18FA3),
                onSecondary = Color(0xFFFFFFFF),
                background = Color(0xFFFFF3F5),
                onBackground = Color(0xFF330E18),
                surface = Color(0xFFFFF9FA),
                onSurface = Color(0xFF330E18),
                surfaceVariant = Color(0xFFFCE2E8),
                onSurfaceVariant = Color(0xFF784452),
                outline = Color(0xFFF0C2CD)
            )
        }

        // Vivid luminous emerald green with pastel mint harmony
        PrismTheme.EMERALD_GREEN -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF18E28A),
                onPrimary = Color(0xFF022414),
                primaryContainer = Color(0xFF084B2E),
                onPrimaryContainer = Color(0xFFC8FFE5),
                secondary = Color(0xFF69F0AE),
                onSecondary = Color(0xFF032415),
                background = Color(0xFF041A11),
                onBackground = Color(0xFFE9FFF4),
                surface = Color(0xFF08261A),
                onSurface = Color(0xFFE9FFF4),
                surfaceVariant = Color(0xFF0E3827),
                onSurfaceVariant = Color(0xFFA7DEC5),
                outline = Color(0xFF1E6346)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF00BE6E),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFC4FCE2),
                onPrimaryContainer = Color(0xFF02301B),
                secondary = Color(0xFF2CE094),
                onSecondary = Color(0xFF032917),
                background = Color(0xFFE9FFF4),
                onBackground = Color(0xFF06291B),
                surface = Color(0xFFF5FFF9),
                onSurface = Color(0xFF06291B),
                surfaceVariant = Color(0xFFD5F9E8),
                onSurfaceVariant = Color(0xFF29664C),
                outline = Color(0xFF9BE6C4)
            )
        }
    }
}

fun getExtendedColors(theme: PrismTheme, darkTheme: Boolean): PrismExtendedColors {
    return when (theme) {
        PrismTheme.LATTE -> PrismExtendedColors(
            backgroundTop = Color(0xFFF8F0E5),
            backgroundBottom = Color(0xFFEDE0CE),
            ambientOrbPrimary = Color(0x36DFBA95),
            ambientOrbSecondary = Color(0x2AC9A17D),
            glassFillTop = Color(0xD8FFFBF5),
            glassFillBottom = Color(0xA6F7ECDE),
            glassBorderStart = Color(0xF5FFFFFF),
            glassBorderMid = Color(0x66E4CEB5),
            glassBorderEnd = Color(0x99CBAF92),
            rawBadgeContainer = Color(0xE89C7251),
            rawBadgeContent = Color(0xFFFFFFFF),
            isDarkSurface = false
        )

        PrismTheme.CLEAN_WHITE -> PrismExtendedColors(
            backgroundTop = Color(0xFFFFFFFF),
            backgroundBottom = Color(0xFFF2F5FA),
            ambientOrbPrimary = Color(0x2CBAD2FF),
            ambientOrbSecondary = Color(0x24D8E2FF),
            glassFillTop = Color(0xDEFFFFFF),
            glassFillBottom = Color(0xAEF4F7FC),
            glassBorderStart = Color(0xFFFFFFFF),
            glassBorderMid = Color(0x70D2DCEB),
            glassBorderEnd = Color(0x99B6C4DC),
            rawBadgeContainer = Color(0xE86A85E6),
            rawBadgeContent = Color(0xFFFFFFFF),
            isDarkSurface = false
        )

        PrismTheme.AMOLED_BLACK -> PrismExtendedColors(
            backgroundTop = Color(0xFF000000),
            backgroundBottom = Color(0xFF000000),
            ambientOrbPrimary = Color.Transparent,
            ambientOrbSecondary = Color.Transparent,
            glassFillTop = Color(0x36202229),
            glassFillBottom = Color(0x220E0F13),
            glassBorderStart = Color(0x52FFFFFF),
            glassBorderMid = Color(0x1EFFFFFF),
            glassBorderEnd = Color(0x34A6B0C8),
            rawBadgeContainer = Color(0xEAE6ECFF),
            rawBadgeContent = Color(0xFF050608),
            isDarkSurface = true
        )

        PrismTheme.DEEP_TECH_BLUE -> if (darkTheme) {
            PrismExtendedColors(
                backgroundTop = Color(0xFF0B1829),
                backgroundBottom = Color(0xFF07101C),
                ambientOrbPrimary = Color(0x3470B5FF),
                ambientOrbSecondary = Color(0x289AB0FF),
                glassFillTop = Color(0x381D3454),
                glassFillBottom = Color(0x22102036),
                glassBorderStart = Color(0x6CE0F1FF),
                glassBorderMid = Color(0x268CCBFF),
                glassBorderEnd = Color(0x489AB0FF),
                rawBadgeContainer = Color(0xEB8CCBFF),
                rawBadgeContent = Color(0xFF072138),
                isDarkSurface = true
            )
        } else {
            PrismExtendedColors(
                backgroundTop = Color(0xFFF3F8FF),
                backgroundBottom = Color(0xFFE3EEFE),
                ambientOrbPrimary = Color(0x369AC8FF),
                ambientOrbSecondary = Color(0x28B8C7FF),
                glassFillTop = Color(0xD8FFFFFF),
                glassFillBottom = Color(0xA4EAF3FF),
                glassBorderStart = Color(0xF6FFFFFF),
                glassBorderMid = Color(0x66B4D4FA),
                glassBorderEnd = Color(0x908CB8F2),
                rawBadgeContainer = Color(0xEB5895E8),
                rawBadgeContent = Color(0xFFFFFFFF),
                isDarkSurface = false
            )
        }

        PrismTheme.CRIMSON_RED -> if (darkTheme) {
            PrismExtendedColors(
                backgroundTop = Color(0xFF200B11),
                backgroundBottom = Color(0xFF13060A),
                ambientOrbPrimary = Color(0x36FF85A1),
                ambientOrbSecondary = Color(0x28FFB3A7),
                glassFillTop = Color(0x3A451925),
                glassFillBottom = Color(0x22240C13),
                glassBorderStart = Color(0x68FFE0E8),
                glassBorderMid = Color(0x28FF96AA),
                glassBorderEnd = Color(0x48FFB3A7),
                rawBadgeContainer = Color(0xEBFF96AA),
                rawBadgeContent = Color(0xFF380914),
                isDarkSurface = true
            )
        } else {
            PrismExtendedColors(
                backgroundTop = Color(0xFFFFF6F8),
                backgroundBottom = Color(0xFFFFE6EC),
                ambientOrbPrimary = Color(0x34FFAEC0),
                ambientOrbSecondary = Color(0x28FFC7BE),
                glassFillTop = Color(0xDCFFFFFF),
                glassFillBottom = Color(0xA6FFEBF0),
                glassBorderStart = Color(0xF8FFFFFF),
                glassBorderMid = Color(0x66F5BCC8),
                glassBorderEnd = Color(0x92EA91A4),
                rawBadgeContainer = Color(0xEBE06680),
                rawBadgeContent = Color(0xFFFFFFFF),
                isDarkSurface = false
            )
        }

        PrismTheme.EMERALD_GREEN -> if (darkTheme) {
            PrismExtendedColors(
                backgroundTop = Color(0xFF052014),
                backgroundBottom = Color(0xFF03130C),
                ambientOrbPrimary = Color(0x3C00E682),
                ambientOrbSecondary = Color(0x2C5CFFC0),
                glassFillTop = Color(0x3A11422D),
                glassFillBottom = Color(0x24072417),
                glassBorderStart = Color(0x74C8FFE5),
                glassBorderMid = Color(0x3018E28A),
                glassBorderEnd = Color(0x5269F0AE),
                rawBadgeContainer = Color(0xF018E28A),
                rawBadgeContent = Color(0xFF022414),
                isDarkSurface = true
            )
        } else {
            PrismExtendedColors(
                backgroundTop = Color(0xFFF2FFF8),
                backgroundBottom = Color(0xFFD8FCEC),
                ambientOrbPrimary = Color(0x3C42F0A0),
                ambientOrbSecondary = Color(0x2E8AFFC9),
                glassFillTop = Color(0xDCFFFFFF),
                glassFillBottom = Color(0xA6DCFAEC),
                glassBorderStart = Color(0xF8FFFFFF),
                glassBorderMid = Color(0x6880E8BA),
                glassBorderEnd = Color(0x9810C97B),
                rawBadgeContainer = Color(0xF000BE6E),
                rawBadgeContent = Color(0xFFFFFFFF),
                isDarkSurface = false
            )
        }
    }
}
