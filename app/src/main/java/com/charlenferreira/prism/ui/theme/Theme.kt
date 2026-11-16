package com.charlenferreira.prism.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalPrismColors = staticCompositionLocalOf {
    getExtendedColors(PrismTheme.DEEP_TECH_BLUE, darkTheme = false)
}

@Composable
fun PrismAppTheme(
    prismTheme: PrismTheme = PrismTheme.DEEP_TECH_BLUE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val effectiveDark = isDarkThemeFor(prismTheme, darkTheme)
    val targetScheme = getColorScheme(prismTheme, darkTheme)
    val targetExtended = getExtendedColors(prismTheme, darkTheme)

    val animatedColorScheme = animateColorScheme(targetScheme)
    val animatedExtended = animateExtendedColors(targetExtended)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !effectiveDark
            insetsController.isAppearanceLightNavigationBars = !effectiveDark
        }
    }

    CompositionLocalProvider(LocalPrismColors provides animatedExtended) {
        MaterialTheme(
            colorScheme = animatedColorScheme,
            typography = PrismTypography,
            content = content
        )
    }
}

@Composable
private fun animColor(target: Color, label: String): Color {
    val color by animateColorAsState(
        targetValue = target,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 380f
        ),
        label = label
    )
    return color
}

@Composable
private fun animateColorScheme(target: ColorScheme): ColorScheme {
    return target.copy(
        primary = animColor(target.primary, "primary"),
        onPrimary = animColor(target.onPrimary, "onPrimary"),
        primaryContainer = animColor(target.primaryContainer, "primaryContainer"),
        onPrimaryContainer = animColor(target.onPrimaryContainer, "onPrimaryContainer"),
        secondary = animColor(target.secondary, "secondary"),
        onSecondary = animColor(target.onSecondary, "onSecondary"),
        background = animColor(target.background, "background"),
        onBackground = animColor(target.onBackground, "onBackground"),
        surface = animColor(target.surface, "surface"),
        onSurface = animColor(target.onSurface, "onSurface"),
        surfaceVariant = animColor(target.surfaceVariant, "surfaceVariant"),
        onSurfaceVariant = animColor(target.onSurfaceVariant, "onSurfaceVariant"),
        outline = animColor(target.outline, "outline")
    )
}

@Composable
private fun animateExtendedColors(target: PrismExtendedColors): PrismExtendedColors {
    return target.copy(
        backgroundTop = animColor(target.backgroundTop, "bgTop"),
        backgroundBottom = animColor(target.backgroundBottom, "bgBottom"),
        ambientOrbPrimary = animColor(target.ambientOrbPrimary, "orb1"),
        ambientOrbSecondary = animColor(target.ambientOrbSecondary, "orb2"),
        glassFillTop = animColor(target.glassFillTop, "glassTop"),
        glassFillBottom = animColor(target.glassFillBottom, "glassBottom"),
        glassSpecularTop = animColor(target.glassSpecularTop, "glassSpecTop"),
        glassBorderStart = animColor(target.glassBorderStart, "borderStart"),
        glassBorderMid = animColor(target.glassBorderMid, "borderMid"),
        glassBorderEnd = animColor(target.glassBorderEnd, "borderEnd"),
        rawBadgeContainer = animColor(target.rawBadgeContainer, "rawBg"),
        rawBadgeContent = animColor(target.rawBadgeContent, "rawFg")
    )
}
