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
        subtitle = "Creme quente & âmbar",
        swatchPrimary = Color(0xFFD6B99B),
        swatchSecondary = Color(0xFF7D5A3C)
    ),
    CLEAN_WHITE(
        displayName = "Branco Clean",
        subtitle = "Minimalismo puro",
        swatchPrimary = Color(0xFFF5F7FA),
        swatchSecondary = Color(0xFF2B3445)
    ),
    AMOLED_BLACK(
        displayName = "Preto AMOLED",
        subtitle = "#000000 infinito",
        swatchPrimary = Color(0xFF000000),
        swatchSecondary = Color(0xFF91A7FF)
    ),
    DEEP_TECH_BLUE(
        displayName = "Azul Deep Tech",
        subtitle = "Cobalto & ciano glacial",
        swatchPrimary = Color(0xFF0A192F),
        swatchSecondary = Color(0xFF38BDF8)
    ),
    CRIMSON_RED(
        displayName = "Vermelho Crimson",
        subtitle = "Rubi cinematográfico",
        swatchPrimary = Color(0xFF2B0912),
        swatchSecondary = Color(0xFFFF4D6D)
    ),
    EMERALD_GREEN(
        displayName = "Verde Esmeralda",
        subtitle = "Jade & menta prismática",
        swatchPrimary = Color(0xFF07221B),
        swatchSecondary = Color(0xFF10B981)
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
        PrismTheme.LATTE -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFE3C2A3),
                onPrimary = Color(0xFF2C1B0E),
                primaryContainer = Color(0xFF4B321F),
                onPrimaryContainer = Color(0xFFFFE6CE),
                secondary = Color(0xFFCBB19A),
                onSecondary = Color(0xFF312217),
                background = Color(0xFF18130F),
                onBackground = Color(0xFFF4ECE4),
                surface = Color(0xFF211A14),
                onSurface = Color(0xFFF4ECE4),
                surfaceVariant = Color(0xFF31271E),
                onSurfaceVariant = Color(0xFFD0C2B5),
                outline = Color(0xFF6B5848)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF7D5A3C),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFEFDEC9),
                onPrimaryContainer = Color(0xFF2F1D0E),
                secondary = Color(0xFF685140),
                onSecondary = Color(0xFFFFFFFF),
                background = Color(0xFFF5EDE3),
                onBackground = Color(0xFF231A12),
                surface = Color(0xFFFCF7F1),
                onSurface = Color(0xFF231A12),
                surfaceVariant = Color(0xFFE9DCCD),
                onSurfaceVariant = Color(0xFF5A4A3C),
                outline = Color(0xFFB5A391)
            )
        }

        PrismTheme.CLEAN_WHITE -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFD8E2F0),
                onPrimary = Color(0xFF111827),
                primaryContainer = Color(0xFF2A3444),
                onPrimaryContainer = Color(0xFFF1F5F9),
                secondary = Color(0xFF94A3B8),
                onSecondary = Color(0xFF0F172A),
                background = Color(0xFF111318),
                onBackground = Color(0xFFF8FAFC),
                surface = Color(0xFF181C24),
                onSurface = Color(0xFFF8FAFC),
                surfaceVariant = Color(0xFF242A36),
                onSurfaceVariant = Color(0xFFCBD5E1),
                outline = Color(0xFF475569)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF1E293B),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFE2E8F0),
                onPrimaryContainer = Color(0xFF0F172A),
                secondary = Color(0xFF475569),
                onSecondary = Color(0xFFFFFFFF),
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFEEF2F6),
                onSurfaceVariant = Color(0xFF475569),
                outline = Color(0xFFCBD5E1)
            )
        }

        PrismTheme.AMOLED_BLACK -> darkColorScheme(
            primary = Color(0xFF8FA8FF),
            onPrimary = Color(0xFF040A1E),
            primaryContainer = Color(0xFF161D33),
            onPrimaryContainer = Color(0xFFE1E8FF),
            secondary = Color(0xFF6CE5E8),
            onSecondary = Color(0xFF001C1E),
            background = Color(0xFF000000),
            onBackground = Color(0xFFF8FAFC),
            surface = Color(0xFF000000),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF11131A),
            onSurfaceVariant = Color(0xFFB4BCD0),
            outline = Color(0xFF2C3142)
        )

        PrismTheme.DEEP_TECH_BLUE -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF38BDF8),
                onPrimary = Color(0xFF041C2C),
                primaryContainer = Color(0xFF0C3554),
                onPrimaryContainer = Color(0xFFD6F1FF),
                secondary = Color(0xFF818CF8),
                onSecondary = Color(0xFF12153B),
                background = Color(0xFF07111F),
                onBackground = Color(0xFFEAF4FF),
                surface = Color(0xFF0D1B2E),
                onSurface = Color(0xFFEAF4FF),
                surfaceVariant = Color(0xFF162944),
                onSurfaceVariant = Color(0xFFA7C2DF),
                outline = Color(0xFF2B4870)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF0369A1),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFD3EEFF),
                onPrimaryContainer = Color(0xFF05273D),
                secondary = Color(0xFF4F46E5),
                onSecondary = Color(0xFFFFFFFF),
                background = Color(0xFFECF5FF),
                onBackground = Color(0xFF0A192F),
                surface = Color(0xFFF6FAFF),
                onSurface = Color(0xFF0A192F),
                surfaceVariant = Color(0xFFDCEBFB),
                onSurfaceVariant = Color(0xFF334E68),
                outline = Color(0xFF90B8DC)
            )
        }

        PrismTheme.CRIMSON_RED -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFFF5C77),
                onPrimary = Color(0xFF2D040D),
                primaryContainer = Color(0xFF5B1223),
                onPrimaryContainer = Color(0xFFFFD9E0),
                secondary = Color(0xFFFF9E7D),
                onSecondary = Color(0xFF331107),
                background = Color(0xFF16070B),
                onBackground = Color(0xFFFFF0F3),
                surface = Color(0xFF210B11),
                onSurface = Color(0xFFFFF0F3),
                surfaceVariant = Color(0xFF35141E),
                onSurfaceVariant = Color(0xFFE0B4BE),
                outline = Color(0xFF692D3C)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFBE123C),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFFFD9E2),
                onPrimaryContainer = Color(0xFF3D0313),
                secondary = Color(0xFFE11D48),
                onSecondary = Color(0xFFFFFFFF),
                background = Color(0xFFFFF1F4),
                onBackground = Color(0xFF2B0912),
                surface = Color(0xFFFFF8FA),
                onSurface = Color(0xFF2B0912),
                surfaceVariant = Color(0xFFFCE0E7),
                onSurfaceVariant = Color(0xFF6B3240),
                outline = Color(0xFFE4A7B5)
            )
        }

        PrismTheme.EMERALD_GREEN -> if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFF34D399),
                onPrimary = Color(0xFF032117),
                primaryContainer = Color(0xFF0B4634),
                onPrimaryContainer = Color(0xFFC8FBE5),
                secondary = Color(0xFF2DD4BF),
                onSecondary = Color(0xFF02201D),
                background = Color(0xFF061611),
                onBackground = Color(0xFFE9FBF4),
                surface = Color(0xFF0C221A),
                onSurface = Color(0xFFE9FBF4),
                surfaceVariant = Color(0xFF15362A),
                onSurfaceVariant = Color(0xFFA7D3C2),
                outline = Color(0xFF295E4A)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF047857),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFCFFAEA),
                onPrimaryContainer = Color(0xFF032B1F),
                secondary = Color(0xFF0D9488),
                onSecondary = Color(0xFFFFFFFF),
                background = Color(0xFFECFBF4),
                onBackground = Color(0xFF06231A),
                surface = Color(0xFFF6FDF9),
                onSurface = Color(0xFF06231A),
                surfaceVariant = Color(0xFFD7F2E5),
                onSurfaceVariant = Color(0xFF2D5949),
                outline = Color(0xFF8CCAB2)
            )
        }
    }
}

fun getExtendedColors(theme: PrismTheme, darkTheme: Boolean): PrismExtendedColors {
    return when (theme) {
        PrismTheme.LATTE -> if (darkTheme) {
            PrismExtendedColors(
                backgroundTop = Color(0xFF1C1510),
                backgroundBottom = Color(0xFF120E0A),
                ambientOrbPrimary = Color(0x44D6A578),
                ambientOrbSecondary = Color(0x338C5C38),
                glassFillTop = Color(0x383A2C22),
                glassFillBottom = Color(0x22201711),
                glassBorderStart = Color(0x88FFE6D0),
                glassBorderMid = Color(0x28D6B99B),
                glassBorderEnd = Color(0x55A47C5B),
                rawBadgeContainer = Color(0xCCE3C2A3),
                rawBadgeContent = Color(0xFF241409),
                isDarkSurface = true
            )
        } else {
            PrismExtendedColors(
                backgroundTop = Color(0xFFF8F2EA),
                backgroundBottom = Color(0xFFEBDFD0),
                ambientOrbPrimary = Color(0x45D6A97A),
                ambientOrbSecondary = Color(0x35B0845D),
                glassFillTop = Color(0xB8FFFFFF),
                glassFillBottom = Color(0x78EFE3D4),
                glassBorderStart = Color(0xE6FFFFFF),
                glassBorderMid = Color(0x55D6B99B),
                glassBorderEnd = Color(0x99C7A787),
                rawBadgeContainer = Color(0xE07D5A3C),
                rawBadgeContent = Color(0xFFFFFFFF),
                isDarkSurface = false
            )
        }

        PrismTheme.CLEAN_WHITE -> if (darkTheme) {
            PrismExtendedColors(
                backgroundTop = Color(0xFF131720),
                backgroundBottom = Color(0xFF0C0E13),
                ambientOrbPrimary = Color(0x3894A3B8),
                ambientOrbSecondary = Color(0x2C64748B),
                glassFillTop = Color(0x36323A48),
                glassFillBottom = Color(0x20181D26),
                glassBorderStart = Color(0x80FFFFFF),
                glassBorderMid = Color(0x24CBD5E1),
                glassBorderEnd = Color(0x5094A3B8),
                rawBadgeContainer = Color(0xDDE2E8F0),
                rawBadgeContent = Color(0xFF0F172A),
                isDarkSurface = true
            )
        } else {
            PrismExtendedColors(
                backgroundTop = Color(0xFFFFFFFF),
                backgroundBottom = Color(0xFFEFF3F8),
                ambientOrbPrimary = Color(0x40CBD5E1),
                ambientOrbSecondary = Color(0x3093C5FD),
                glassFillTop = Color(0xCCFFFFFF),
                glassFillBottom = Color(0x88F1F5F9),
                glassBorderStart = Color(0xFFFFFFFF),
                glassBorderMid = Color(0x55CBD5E1),
                glassBorderEnd = Color(0x9994A3B8),
                rawBadgeContainer = Color(0xE61E293B),
                rawBadgeContent = Color(0xFFFFFFFF),
                isDarkSurface = false
            )
        }

        PrismTheme.AMOLED_BLACK -> PrismExtendedColors(
            backgroundTop = Color(0xFF000000),
            backgroundBottom = Color(0xFF000000),
            ambientOrbPrimary = Color(0x366B8AFF),
            ambientOrbSecondary = Color(0x2838BDF8),
            glassFillTop = Color(0x321D2336),
            glassFillBottom = Color(0x1C0A0C12),
            glassBorderStart = Color(0x78D8E3FF),
            glassBorderMid = Color(0x246B8AFF),
            glassBorderEnd = Color(0x5238BDF8),
            rawBadgeContainer = Color(0xDE8FA8FF),
            rawBadgeContent = Color(0xFF040A1E),
            isDarkSurface = true
        )

        PrismTheme.DEEP_TECH_BLUE -> if (darkTheme) {
            PrismExtendedColors(
                backgroundTop = Color(0xFF0A192F),
                backgroundBottom = Color(0xFF050B16),
                ambientOrbPrimary = Color(0x4838BDF8),
                ambientOrbSecondary = Color(0x386366F1),
                glassFillTop = Color(0x3C173256),
                glassFillBottom = Color(0x220B192E),
                glassBorderStart = Color(0x8CC7EEFF),
                glassBorderMid = Color(0x2E38BDF8),
                glassBorderEnd = Color(0x66818CF8),
                rawBadgeContainer = Color(0xE038BDF8),
                rawBadgeContent = Color(0xFF031926),
                isDarkSurface = true
            )
        } else {
            PrismExtendedColors(
                backgroundTop = Color(0xFFF0F7FF),
                backgroundBottom = Color(0xFFDCEBFF),
                ambientOrbPrimary = Color(0x4238BDF8),
                ambientOrbSecondary = Color(0x32818CF8),
                glassFillTop = Color(0xC8FFFFFF),
                glassFillBottom = Color(0x88DCEEFF),
                glassBorderStart = Color(0xF0FFFFFF),
                glassBorderMid = Color(0x587DD3FC),
                glassBorderEnd = Color(0x8838BDF8),
                rawBadgeContainer = Color(0xE60369A1),
                rawBadgeContent = Color(0xFFFFFFFF),
                isDarkSurface = false
            )
        }

        PrismTheme.CRIMSON_RED -> if (darkTheme) {
            PrismExtendedColors(
                backgroundTop = Color(0xFF1F080F),
                backgroundBottom = Color(0xFF0F0307),
                ambientOrbPrimary = Color(0x48FF4D6D),
                ambientOrbSecondary = Color(0x34FB7185),
                glassFillTop = Color(0x3E461522),
                glassFillBottom = Color(0x24240911),
                glassBorderStart = Color(0x88FFD2DC),
                glassBorderMid = Color(0x2EFF4D6D),
                glassBorderEnd = Color(0x60FB7185),
                rawBadgeContainer = Color(0xE0FF5C77),
                rawBadgeContent = Color(0xFF28030B),
                isDarkSurface = true
            )
        } else {
            PrismExtendedColors(
                backgroundTop = Color(0xFFFFF4F6),
                backgroundBottom = Color(0xFFFFE1E8),
                ambientOrbPrimary = Color(0x3EFB7185),
                ambientOrbSecondary = Color(0x30FDA4AF),
                glassFillTop = Color(0xCCFFFFFF),
                glassFillBottom = Color(0x8AFFDCE5),
                glassBorderStart = Color(0xF2FFFFFF),
                glassBorderMid = Color(0x55FB7185),
                glassBorderEnd = Color(0x88F43F5E),
                rawBadgeContainer = Color(0xE6BE123C),
                rawBadgeContent = Color(0xFFFFFFFF),
                isDarkSurface = false
            )
        }

        PrismTheme.EMERALD_GREEN -> if (darkTheme) {
            PrismExtendedColors(
                backgroundTop = Color(0xFF081E17),
                backgroundBottom = Color(0xFF040F0B),
                ambientOrbPrimary = Color(0x4610B981),
                ambientOrbSecondary = Color(0x342DD4BF),
                glassFillTop = Color(0x3C123B2D),
                glassFillBottom = Color(0x22081F17),
                glassBorderStart = Color(0x88C8FBE5),
                glassBorderMid = Color(0x2E34D399),
                glassBorderEnd = Color(0x5E2DD4BF),
                rawBadgeContainer = Color(0xE034D399),
                rawBadgeContent = Color(0xFF022116),
                isDarkSurface = true
            )
        } else {
            PrismExtendedColors(
                backgroundTop = Color(0xFFF2FCF7),
                backgroundBottom = Color(0xFFD6F5E7),
                ambientOrbPrimary = Color(0x3E34D399),
                ambientOrbSecondary = Color(0x302DD4BF),
                glassFillTop = Color(0xCCFFFFFF),
                glassFillBottom = Color(0x88D7F4E7),
                glassBorderStart = Color(0xF2FFFFFF),
                glassBorderMid = Color(0x5534D399),
                glassBorderEnd = Color(0x8810B981),
                rawBadgeContainer = Color(0xE6047857),
                rawBadgeContent = Color(0xFFFFFFFF),
                isDarkSurface = false
            )
        }
    }
}
