package com.charlenferreira.prism.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalPrismColors = staticCompositionLocalOf {
    getExtendedColors(PrismTheme.DYNAMIC, darkTheme = true)
}

@Composable
fun PrismAppTheme(
    prismTheme: PrismTheme = PrismTheme.DYNAMIC,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val effectiveDark = when (prismTheme) {
        PrismTheme.LATTE, PrismTheme.CLEAN_WHITE -> false
        PrismTheme.AMOLED_BLACK -> true
        PrismTheme.DYNAMIC,
        PrismTheme.CRIMSON_RED,
        PrismTheme.EMERALD_GREEN -> darkTheme
    }

    val colorScheme = if (prismTheme == PrismTheme.DYNAMIC &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    ) {
        if (effectiveDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        getColorScheme(prismTheme, effectiveDark)
    }

    val extendedColors = if (prismTheme == PrismTheme.DYNAMIC) {
        deriveDynamicExtendedColors(colorScheme, effectiveDark)
    } else {
        getExtendedColors(prismTheme, effectiveDark)
    }

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
