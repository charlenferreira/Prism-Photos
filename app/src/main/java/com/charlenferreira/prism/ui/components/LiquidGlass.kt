package com.charlenferreira.prism.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charlenferreira.prism.R
import com.charlenferreira.prism.data.repository.LiquidGlassConfig
import com.charlenferreira.prism.ui.theme.LocalPrismColors

@Stable
class PrismBackdropState {
    var invalidationSignal: () -> Int = { 0 }
}

val LocalPrismBackdrop = compositionLocalOf<PrismBackdropState?> { null }
val LocalLiquidGlassConfig = compositionLocalOf { LiquidGlassConfig() }

@Composable
fun rememberPrismBackdropState(
    invalidationSignal: () -> Int = { 0 }
): PrismBackdropState {
    val state = remember { PrismBackdropState() }
    state.invalidationSignal = invalidationSignal
    return state
}

fun Modifier.prismBackdropSource(
    @Suppress("UNUSED_PARAMETER") state: PrismBackdropState,
    @Suppress("UNUSED_PARAMETER") blurRadius: Float = 0f
): Modifier = this

@Composable
fun ProgressiveTopBlurScrim(
    modifier: Modifier = Modifier,
    height: Dp = 104.dp
) {
    val prismColors = LocalPrismColors.current
    val topColor = prismColors.backgroundTop.copy(alpha = 0.96f)
    val midColor = prismColors.backgroundTop.copy(alpha = 0.68f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawWithCache {
                val scrimBrush = Brush.verticalGradient(
                    0.0f to topColor,
                    0.58f to midColor,
                    1.0f to Color.Transparent
                )
                onDrawBehind {
                    drawRect(brush = scrimBrush)
                }
            }
    )
}

/**
 * Superfície acetinada estilo Apple Photos (iOS 18/26).
 * Zero shaders pesados / zero alocação de shadow por frame -> 60fps constantes na Adreno 610.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    @Suppress("UNUSED_PARAMETER") cornerRadiusDp: Dp = 22.dp,
    @Suppress("UNUSED_PARAMETER") blurRadius: Float = 0f,
    tintOverride: Color? = null,
    darkFrostedOverride: Boolean = false,
    @Suppress("UNUSED_PARAMETER") useHeavyShader: Boolean = false,
    @Suppress("UNUSED_PARAMETER") config: LiquidGlassConfig = LocalLiquidGlassConfig.current,
    contentPadding: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val prismColors = LocalPrismColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = 620f),
        label = "iosSurfacePress"
    )

    val useDark = darkFrostedOverride || prismColors.isDarkSurface

    val baseSurfaceColor = when {
        darkFrostedOverride -> Color(0xF01C1C1E)
        useDark -> Color(0xF21C1C1E)
        else -> Color(0xF5F5F5F7)
    }

    val resolvedBgColor = remember(tintOverride, useDark, baseSurfaceColor) {
        if (tintOverride != null) {
            tintOverride.copy(alpha = if (useDark) 0.20f else 0.11f).compositeOver(baseSurfaceColor)
        } else {
            baseSurfaceColor
        }
    }

    val rimBorderBrush = remember(useDark) {
        if (useDark) {
            Brush.verticalGradient(
                0.0f to Color.White.copy(alpha = 0.22f),
                0.5f to Color.White.copy(alpha = 0.10f),
                1.0f to Color.White.copy(alpha = 0.14f)
            )
        } else {
            Brush.verticalGradient(
                0.0f to Color.White.copy(alpha = 0.90f),
                0.5f to Color.Black.copy(alpha = 0.08f),
                1.0f to Color.Black.copy(alpha = 0.12f)
            )
        }
    }

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(resolvedBgColor)
            .border(width = 0.8.dp, brush = rimBorderBrush, shape = shape)
            .then(clickModifier)
            .graphicsLayer {
                if (pressScale != 1f) {
                    scaleX = pressScale
                    scaleY = pressScale
                }
            }
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val prismColors = LocalPrismColors.current
    Box(
        modifier = modifier.background(prismColors.backgroundTop),
        content = content
    )
}

/**
 * Barra inferior flutuante em cápsula dupla inspirada na Fototeca do iPhone:
 * Pílula principal à esquerda + botão circular satélite à direita.
 */
@Composable
fun LiquidGlassPillNavBar(
    isGallerySelected: Boolean,
    onGalleryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val galleryLabel = stringResource(R.string.tab_gallery)
    val settingsLabel = stringResource(R.string.tab_settings)
    val prismColors = LocalPrismColors.current

    val activeInnerPillColor = if (prismColors.isDarkSurface) {
        Color.White.copy(alpha = 0.13f)
    } else {
        Color.White.copy(alpha = 0.92f)
    }

    val galleryTint by animateColorAsState(
        targetValue = if (isGallerySelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(140),
        label = "iosGalleryTint"
    )

    val settingsTint by animateColorAsState(
        targetValue = if (!isGallerySelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(140),
        label = "iosSettingsTint"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cápsula principal da Fototeca (esquerda)
        LiquidGlassSurface(
            shape = CircleShape,
            contentPadding = 4.dp,
            onClick = onGalleryClick,
            modifier = Modifier
                .height(54.dp)
                .testTag("nav_gallery_tab")
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isGallerySelected) activeInnerPillColor else Color.Transparent)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isGallerySelected) {
                        Icons.Rounded.PhotoLibrary
                    } else {
                        Icons.Outlined.PhotoLibrary
                    },
                    contentDescription = galleryLabel,
                    tint = galleryTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = galleryLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isGallerySelected) FontWeight.Bold else FontWeight.Medium,
                    letterSpacing = (-0.2).sp,
                    color = galleryTint,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Botão circular satélite separado (direita — estilo lupa/ação do iOS Photos)
        LiquidGlassSurface(
            shape = CircleShape,
            tintOverride = if (!isGallerySelected) MaterialTheme.colorScheme.primary else null,
            onClick = onSettingsClick,
            modifier = Modifier
                .size(54.dp)
                .testTag("nav_settings_tab")
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (!isGallerySelected) {
                        Icons.Rounded.Settings
                    } else {
                        Icons.Outlined.Settings
                    },
                    contentDescription = settingsLabel,
                    tint = settingsTint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
