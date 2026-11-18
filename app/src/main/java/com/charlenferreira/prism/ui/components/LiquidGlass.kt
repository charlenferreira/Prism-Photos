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
import androidx.compose.ui.draw.shadow
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

/**
 * No-op leve: não captura camada offscreen nem roda shader na GPU.
 */
fun Modifier.prismBackdropSource(
    @Suppress("UNUSED_PARAMETER") state: PrismBackdropState,
    @Suppress("UNUSED_PARAMETER") blurRadius: Float = 0f
): Modifier = this

/**
 * Degradê superior suave estilo iOS Photos para legibilidade da barra de status.
 */
@Composable
fun ProgressiveTopBlurScrim(
    modifier: Modifier = Modifier,
    height: Dp = 104.dp
) {
    val prismColors = LocalPrismColors.current
    val topColor = prismColors.backgroundTop.copy(alpha = 0.94f)
    val midColor = prismColors.backgroundTop.copy(alpha = 0.55f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawWithCache {
                val scrimBrush = Brush.verticalGradient(
                    0.0f to topColor,
                    0.55f to midColor,
                    1.0f to Color.Transparent
                )
                onDrawBehind {
                    drawRect(brush = scrimBrush)
                }
            }
    )
}

/**
 * Superfície translúcida leve estilo Apple Photos (iOS 18/26).
 * Zero shaders AGSL / zero render offscreen -> 120fps cravados em qualquer GPU.
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
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 620f),
        label = "iosSurfacePress"
    )

    val useDark = darkFrostedOverride || prismColors.isDarkSurface

    val baseSurfaceColor = when {
        darkFrostedOverride -> Color(0xE61C1C1E)
        useDark -> Color(0xEB18181B)
        else -> prismColors.glassFillTop.copy(alpha = 0.92f)
    }

    val resolvedBgColor = remember(tintOverride, useDark, baseSurfaceColor) {
        if (tintOverride != null) {
            tintOverride.copy(alpha = if (useDark) 0.22f else 0.12f).compositeOver(baseSurfaceColor)
        } else {
            baseSurfaceColor
        }
    }

    val borderColor = if (useDark) {
        Color.White.copy(alpha = 0.14f)
    } else {
        Color.Black.copy(alpha = 0.08f)
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
            .shadow(
                elevation = if (useDark) 4.dp else 6.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.12f)
            )
            .clip(shape)
            .background(resolvedBgColor)
            .border(width = 0.75.dp, color = borderColor, shape = shape)
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
 * Barra flutuante estilo Apple Photos (cápsula segmentada + botão circular satélite).
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

    val activePillBg = if (prismColors.isDarkSurface) {
        Color.White.copy(alpha = 0.14f)
    } else {
        Color.Black.copy(alpha = 0.07f)
    }

    val galleryColor by animateColorAsState(
        targetValue = if (isGallerySelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(150),
        label = "iosGalleryTabColor"
    )

    val settingsColor by animateColorAsState(
        targetValue = if (!isGallerySelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(150),
        label = "iosSettingsTabColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cápsula principal estilo Library | Collections da referência
        LiquidGlassSurface(
            shape = CircleShape,
            contentPadding = 4.dp,
            modifier = Modifier.height(56.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isGallerySelected) activePillBg else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClick = onGalleryClick
                        )
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .testTag("nav_gallery_tab"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isGallerySelected) {
                            Icons.Rounded.PhotoLibrary
                        } else {
                            Icons.Outlined.PhotoLibrary
                        },
                        contentDescription = galleryLabel,
                        tint = galleryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = galleryLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isGallerySelected) FontWeight.Bold else FontWeight.Medium,
                        color = galleryColor,
                        maxLines = 1
                    )
                }

                // Botão circular satélite integrado ou ao lado
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (!isGallerySelected) activePillBg else Color.Transparent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Tab,
                            onClick = onSettingsClick
                        )
                        .testTag("nav_settings_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (!isGallerySelected) {
                            Icons.Rounded.Settings
                        } else {
                            Icons.Outlined.Settings
                        },
                        contentDescription = settingsLabel,
                        tint = settingsColor,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }
}
