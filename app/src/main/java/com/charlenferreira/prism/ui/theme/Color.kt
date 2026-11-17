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
    DYNAMIC(
        displayName = "Dinâmico",
        subtitle = "Cores do sistema",
        swatchPrimary = Color(0xFF8AB4F8),
        swatchSecondary = Color(0xFFA7F3D0)
    ),
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

fun deriveDynamicExtendedColors(
    scheme: ColorScheme,
    darkTheme: Boolean
): PrismExtendedColors {
    return if (darkTheme) {
        PrismExtendedColors(
            backgroundTop = scheme.background,
            backgroundBottom = scheme.surface,
            ambientOrbPrimary = scheme.primary.copy(alpha = 0.18f),
            ambientOrbSecondary = scheme.tertiary.copy(alpha = 0.14f),
            glassFillTop = scheme.surfaceVariant.copy(alpha = 0.78f),
            glassFillBottom = scheme.surface.copy(alpha = 0.68f),
            glassBorderStart = Color.White.copy(alpha = 0.24f),
            glassBorderMid = scheme.primary.copy(alpha = 0.16f),
            glassBorderEnd = scheme.outline.copy(alpha = 0.32f),
            rawBadgeContainer = scheme.primary.copy(alpha = 0.94f),
            rawBadgeContent = scheme.onPrimary,
            isDarkSurface = true
        )
    } else {
        PrismExtendedColors(
            backgroundTop = scheme.background,
            backgroundBottom = scheme.surfaceVariant.copy(alpha = 0.55f),
            ambientOrbPrimary = scheme.primaryContainer.copy(alpha = 0.52f),
            ambientOrbSecondary = scheme.tertiaryContainer.copy(alpha = 0.44f),
            glassFillTop = Color.White.copy(alpha = 0.88f),
            glassFillBottom = scheme.surface.copy(alpha = 0.78f),
            glassBorderStart = Color.White.copy(alpha = 0.96f),
            glassBorderMid = scheme.primary.copy(alpha = 0.18f),
            glassBorderEnd = scheme.outline.copy(alpha = 0.42f),
            rawBadgeContainer = scheme.primary.copy(alpha = 0.94f),
            rawBadgeContent = scheme.onPrimary,
            isDarkSurface = false
        )
    }
}

fun getColorScheme(theme: PrismTheme, darkTheme: Boolean): ColorScheme {
    return when (theme) {
        // Fallback scheme for API 26-30 when system Dynamic Color is unavailable
        PrismTheme.DYNAMIC -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFA8C7FA),
                onPrimary = Color(0xFF062E6F),
                primaryContainer = Color(0xFF0842A0),
                onPrimaryContainer = Color(0xFFD3E3FD),
                secondary = Color(0xFFC2E7FF),
                onSecondary = Color(0xFF003355),
                tertiary = Color(0xFFC4EED0),
                tertiaryContainer = Color(0xFF0F5223),
                background = Color(0xFF111318),
                onBackground = Color(0xFFE3E2E6),
                surface = Color(0xFF191C22),
                onSurface = Color(0xFFE3E2E6),
                surfaceVariant = Color(0xFF252932),
                onSurfaceVariant = Color(0xFFC4C6D0),
                outline = Color(0xFF44474F)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF4C7CE0),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFDCE7FF),
                onPrimaryContainer = Color(0xFF0B2358),
                secondary = Color(0xFF6285B8),
                onSecondary = Color(0xFFFFFFFF),
                tertiary = Color(0xFF3E8E6E),
                tertiaryContainer = Color(0xFFD4F5E5),
                background = Color(0xFFF6F8FC),
                onBackground = Color(0xFF191C22),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF191C22),
                surfaceVariant = Color(0xFFE8EDF6),
                onSurfaceVariant = Color(0xFF535B6B),
                outline = Color(0xFFC8D0DE)
            )
        }

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
        PrismTheme.DYNAMIC -> deriveDynamicExtendedColors(
            scheme = getColorScheme(PrismTheme.DYNAMIC, darkTheme),
            darkTheme = darkTheme
        )

        PrismTheme.LATTE -> PrismExtendedColors(
            backgroundTop = Color(0xFFF8F0E5),
            backgroundBottom = Color(0xFFEDE0CE),
            ambientOrbPrimary = Color(0x36DFBA95),
            ambientOrbSecondary = Color(0x2AC9A17D),
            glassFillTop = Color(0xE8FFFBF5),
            glassFillBottom = Color(0xD2F7ECDE),
            glassBorderStart = Color(0xF8FFFFFF),
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
            glassFillTop = Color(0xEBFFFFFF),
            glassFillBottom = Color(0xD6F4F7FC),
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
            glassFillTop = Color(0xC21D2028),
            glassFillBottom = Color(0xAE0E1015),
            glassBorderStart = Color(0x4DFFFFFF),
            glassBorderMid = Color(0x1EFFFFFF),
            glassBorderEnd = Color(0x34A6B0C8),
            rawBadgeContainer = Color(0xEAE6ECFF),
            rawBadgeContent = Color(0xFF050608),
            isDarkSurface = true
        )

        PrismTheme.CRIMSON_RED -> if (darkTheme) {
            PrismExtendedColors(
                backgroundTop = Color(0xFF200B11),
                backgroundBottom = Color(0xFF13060A),
                ambientOrbPrimary = Color(0x36FF85A1),
                ambientOrbSecondary = Color(0x28FFB3A7),
                glassFillTop = Color(0xBF3D1722),
                glassFillBottom = Color(0xAA240D14),
                glassBorderStart = Color(0x66FFE0E8),
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
                glassFillTop = Color(0xE8FFFFFF),
                glassFillBottom = Color(0xD2FFEBF0),
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
                glassFillTop = Color(0xBF0F3827),
                glassFillBottom = Color(0xAA082217),
                glassBorderStart = Color(0x6CC8FFE5),
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
                glassFillTop = Color(0xE8FFFFFF),
                glassFillBottom = Color(0xD2DCFAEC),
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
