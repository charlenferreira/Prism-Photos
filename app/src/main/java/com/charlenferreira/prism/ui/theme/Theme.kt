package com.charlenferreira.prism.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalPrismColors = staticCompositionLocalOf {
    getExtendedColors(PrismTheme.WHITE, darkTheme = false)
}

@Composable
fun PrismAppTheme(
    prismTheme: PrismTheme = PrismTheme.WHITE,
    content: @Composable () -> Unit
) {
    val effectiveDark = prismTheme == PrismTheme.AMOLED_BLACK
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
