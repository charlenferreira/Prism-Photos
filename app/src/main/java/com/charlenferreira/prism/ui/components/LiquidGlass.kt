package com.charlenferreira.prism.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
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
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@Stable
class PrismBackdropState(
    val layerBackdrop: LayerBackdrop
) {
    var invalidationSignal: () -> Int = { 0 }
}

val LocalPrismBackdrop = compositionLocalOf<PrismBackdropState?> { null }
val LocalLiquidGlassConfig = compositionLocalOf { LiquidGlassConfig() }

@Composable
fun rememberPrismBackdropState(
    invalidationSignal: () -> Int = { 0 }
): PrismBackdropState {
    val backgroundColor = MaterialTheme.colorScheme.background
    val layerBackdrop = rememberLayerBackdrop {
        drawRect(backgroundColor)
        drawContent()
    }
    val state = remember(layerBackdrop) {
        PrismBackdropState(layerBackdrop = layerBackdrop)
    }
    state.invalidationSignal = invalidationSignal
    return state
}

fun Modifier.prismBackdropSource(
    state: PrismBackdropState,
    @Suppress("UNUSED_PARAMETER") blurRadius: Float = 14f
): Modifier = this.layerBackdrop(state.layerBackdrop)

@Composable
fun ProgressiveTopBlurScrim(
    modifier: Modifier = Modifier,
    height: Dp = 112.dp
) {
    val prismColors = LocalPrismColors.current
    val topColor = if (prismColors.isDarkSurface) {
        Color.Black.copy(alpha = 0.56f)
    } else {
        prismColors.backgroundTop.copy(alpha = 0.84f)
    }
    val midColor = if (prismColors.isDarkSurface) {
        Color.Black.copy(alpha = 0.22f)
    } else {
        prismColors.backgroundTop.copy(alpha = 0.36f)
    }

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

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    cornerRadiusDp: Dp = 24.dp,
    @Suppress("UNUSED_PARAMETER") blurRadius: Float = 12f,
    tintOverride: Color? = null,
    darkFrostedOverride: Boolean = false,
    config: LiquidGlassConfig = LocalLiquidGlassConfig.current,
    contentPadding: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val prismColors = LocalPrismColors.current
    val backdropState = LocalPrismBackdrop.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.945f else 1f,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 580f
        ),
        label = "liquidGlassPressScale"
    )

    val useDarkFrost = darkFrostedOverride || prismColors.isDarkSurface

    val baseTop = if (darkFrostedOverride) Color(0xCC161A24) else prismColors.glassFillTop
    val baseBottom = if (darkFrostedOverride) Color(0xBA0E1018) else prismColors.glassFillBottom

    val targetTopColor = if (tintOverride != null) {
        if (darkFrostedOverride) {
            tintOverride.copy(alpha = 0.72f)
        } else {
            tintOverride.copy(alpha = if (useDarkFrost) 0.24f else 0.16f).compositeOver(baseTop)
        }
    } else {
        baseTop
    }

    val targetBottomColor = if (tintOverride != null) {
        if (darkFrostedOverride) {
            tintOverride.copy(alpha = 0.64f)
        } else {
            tintOverride.copy(alpha = if (useDarkFrost) 0.18f else 0.12f).compositeOver(baseBottom)
        }
    } else {
        baseBottom
    }

    val topColor by animateColorAsState(
        targetValue = targetTopColor,
        animationSpec = tween(160),
        label = "liquidGlassTopTint"
    )
    val bottomColor by animateColorAsState(
        targetValue = targetBottomColor,
        animationSpec = tween(160),
        label = "liquidGlassBottomTint"
    )

    val effectiveAlpha = config.tintAlpha.coerceIn(0.10f, 0.85f)
    val backdropSurfaceTint = if (backdropState != null) {
        topColor.copy(alpha = effectiveAlpha)
    } else {
        topColor
    }

    val specularBrush = remember(
        darkFrostedOverride,
        prismColors.glassBorderStart,
        prismColors.glassBorderMid,
        prismColors.glassBorderEnd,
        config.rimLightAlpha,
        config.prismEdge
    ) {
        val rim = config.rimLightAlpha.coerceIn(0.10f, 1.0f)
        if (config.prismEdge) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF7DD3FC).copy(alpha = 0.65f * rim),
                    Color(0xFFF472B6).copy(alpha = 0.45f * rim),
                    Color(0xFFFDE047).copy(alpha = 0.45f * rim),
                    prismColors.glassBorderEnd.copy(alpha = 0.60f * rim)
                ),
                start = Offset.Zero,
                end = Offset(520f, 520f)
            )
        } else if (darkFrostedOverride) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.36f * rim),
                    Color.White.copy(alpha = 0.10f * rim),
                    Color.White.copy(alpha = 0.20f * rim)
                ),
                start = Offset.Zero,
                end = Offset(520f, 520f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    prismColors.glassBorderStart.copy(alpha = rim),
                    prismColors.glassBorderMid.copy(alpha = rim),
                    prismColors.glassBorderEnd.copy(alpha = rim)
                ),
                start = Offset.Zero,
                end = Offset(520f, 520f)
            )
        }
    }

    val shadowModifier = if (config.depthShadow) {
        Modifier.shadow(
            elevation = 10.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.14f),
            spotColor = Color.Black.copy(alpha = 0.22f)
        )
    } else {
        Modifier
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

    val satinHighlightAlpha = if (useDarkFrost) 0.08f else 0.24f

    val glassModifier = if (backdropState != null) {
        Modifier
            .drawBackdrop(
                backdrop = backdropState.layerBackdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(config.frostDp.dp.toPx())
                    lens(
                        refractionHeight = config.lensDepthDp.dp.toPx(),
                        refractionAmount = config.lensStrengthDp.dp.toPx(),
                        chromaticAberration = config.prismEdge
                    )
                },
                onDrawSurface = {
                    drawRect(backdropSurfaceTint)
                    drawRect(
                        brush = Brush.verticalGradient(
                            0.0f to Color.White.copy(alpha = satinHighlightAlpha),
                            0.42f to Color.Transparent,
                            1.0f to Color.Transparent
                        )
                    )
                }
            )
            .border(width = 0.9.dp, brush = specularBrush, shape = shape)
    } else {
        Modifier
            .clip(shape)
            .drawWithCache {
                val radiusPx = cornerRadiusDp.toPx().coerceAtMost(size.minDimension / 2f)
                val corner = CornerRadius(radiusPx, radiusPx)
                val fillBrush = Brush.verticalGradient(colors = listOf(topColor, bottomColor))
                val satinBrush = Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = satinHighlightAlpha),
                    0.40f to Color.Transparent,
                    1.0f to Color.Transparent
                )
                onDrawBehind {
                    drawRoundRect(brush = fillBrush, cornerRadius = corner)
                    drawRoundRect(brush = satinBrush, cornerRadius = corner)
                }
            }
            .border(width = 0.9.dp, brush = specularBrush, shape = shape)
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .then(clickModifier)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .then(glassModifier)
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
    val bgTop by animateColorAsState(
        targetValue = prismColors.backgroundTop,
        animationSpec = tween(240),
        label = "bgTop"
    )
    val bgBottom by animateColorAsState(
        targetValue = prismColors.backgroundBottom,
        animationSpec = tween(240),
        label = "bgBottom"
    )
    val orbPrimary by animateColorAsState(
        targetValue = prismColors.ambientOrbPrimary,
        animationSpec = tween(240),
        label = "orbPrimary"
    )
    val orbSecondary by animateColorAsState(
        targetValue = prismColors.ambientOrbSecondary,
        animationSpec = tween(240),
        label = "orbSecondary"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(bgTop, bgBottom)
                )
            )
            if (orbPrimary != Color.Transparent) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orbPrimary, Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.10f),
                        radius = size.width * 0.95f
                    ),
                    radius = size.width * 0.95f,
                    center = Offset(size.width * 0.15f, size.height * 0.10f)
                )
            }
            if (orbSecondary != Color.Transparent) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(orbSecondary, Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.84f),
                        radius = size.width * 0.85f
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(size.width * 0.88f, size.height * 0.84f)
                )
            }
        }
        content()
    }
}

@Composable
fun LiquidGlassPillNavBar(
    isGallerySelected: Boolean,
    onGalleryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val galleryLabel = stringResource(R.string.tab_gallery)
    val settingsLabel = stringResource(R.string.tab_settings)

    val galleryScale by animateFloatAsState(
        targetValue = if (isGallerySelected) 1f else 0.96f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 500f),
        label = "galleryPillScale"
    )
    val settingsScale by animateFloatAsState(
        targetValue = if (!isGallerySelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.80f, stiffness = 500f),
        label = "settingsOrbScale"
    )

    val galleryTint by animateColorAsState(
        targetValue = if (isGallerySelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(150),
        label = "galleryTint"
    )
    val settingsTint by animateColorAsState(
        targetValue = if (!isGallerySelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(150),
        label = "settingsTint"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiquidGlassSurface(
            shape = CircleShape,
            cornerRadiusDp = 30.dp,
            tintOverride = if (isGallerySelected) MaterialTheme.colorScheme.primary else null,
            onClick = onGalleryClick,
            modifier = Modifier
                .height(54.dp)
                .graphicsLayer {
                    scaleX = galleryScale
                    scaleY = galleryScale
                }
                .testTag("nav_gallery_tab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
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
                    modifier = Modifier.size(21.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = galleryLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = galleryTint,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        LiquidGlassSurface(
            shape = CircleShape,
            cornerRadiusDp = 28.dp,
            tintOverride = if (!isGallerySelected) MaterialTheme.colorScheme.primary else null,
            onClick = onSettingsClick,
            modifier = Modifier
                .size(54.dp)
                .graphicsLayer {
                    scaleX = settingsScale
                    scaleY = settingsScale
                }
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
