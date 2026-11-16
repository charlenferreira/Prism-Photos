package com.charlenferreira.prism.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalPrismColors = staticCompositionLocalOf {
    getExtendedColors(PrismTheme.DEEP_TECH_BLUE, darkTheme = true)
}

@Composable
fun PrismAppTheme(
    prismTheme: PrismTheme = PrismTheme.DEEP_TECH_BLUE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val effectiveDark = when (prismTheme) {
        PrismTheme.AMOLED_BLACK -> true
        else -> darkTheme
    }
    val colorScheme = getColorScheme(prismTheme, effectiveDark)
    val extendedColors = getExtendedColors(prismTheme, effectiveDark)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !effectiveDark
            insetsController.isAppearanceLightNavigationBars = !effectiveDark
        }
    }

    CompositionLocalProvider(LocalPrismColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PrismTypography,
            content = content
        )
    }
}
