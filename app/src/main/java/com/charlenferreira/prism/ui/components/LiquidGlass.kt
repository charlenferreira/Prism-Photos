package com.charlenferreira.prism.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.charlenferreira.prism.R
import com.charlenferreira.prism.ui.theme.LocalPrismColors
import kotlin.math.roundToInt

private const val BACKDROP_DOWNSCALE = 0.25f
private const val BACKDROP_INV_SCALE = 4f
private const val BACKDROP_BLUR_RADIUS = 20f // 20f at 0.25x == ~80px smooth matte iOS frost

@Stable
class PrismBackdropState(
    val sourceLayer: GraphicsLayer,
    val blurredLayer: GraphicsLayer
) {
    // Plain fields read only in Layout/Draw phases so scroll never triggers recomposition
    var sourceOffsetInRoot: Offset = Offset.Zero
    var hasBlurredFrame: Boolean = false
    var invalidationSignal: () -> Int = { 0 }
}

val LocalPrismBackdrop = compositionLocalOf<PrismBackdropState?> { null }

@Composable
fun rememberPrismBackdropState(
    invalidationSignal: () -> Int = { 0 }
): PrismBackdropState {
    val sourceLayer = rememberGraphicsLayer()
    val blurredLayer = rememberGraphicsLayer()
    val state = remember(sourceLayer, blurredLayer) {
        PrismBackdropState(
            sourceLayer = sourceLayer,
            blurredLayer = blurredLayer
        )
    }
    state.invalidationSignal = invalidationSignal
    return state
}

/**
 * Captures the backdrop content once per frame into a 0.25x downsampled GPU-blurred layer
 * so sibling frosted glass pills & scrims render real blurred backgrounds at 120fps.
 */
fun Modifier.prismBackdropSource(
    state: PrismBackdropState,
    blurRadius: Float = BACKDROP_BLUR_RADIUS
): Modifier {
    val cachedBlurEffect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        RenderEffect.createBlurEffect(
            blurRadius,
            blurRadius,
            Shader.TileMode.CLAMP
        ).asComposeRenderEffect()
    } else {
        null
    }

    return this
        .onGloballyPositioned { coords ->
            state.sourceOffsetInRoot = coords.positionInRoot()
        }
        .drawWithContent {
            // Subscribe draw phase only to scroll/page updates without recomposing
            state.invalidationSignal()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                size.width >= 8f &&
                size.height >= 8f
            ) {
                state.sourceLayer.record {
                    this@drawWithContent.drawContent()
                }
                drawLayer(state.sourceLayer)

                val downW = (size.width * BACKDROP_DOWNSCALE).roundToInt().coerceAtLeast(1)
                val downH = (size.height * BACKDROP_DOWNSCALE).roundToInt().coerceAtLeast(1)
                state.blurredLayer.renderEffect = cachedBlurEffect
                state.blurredLayer.record(size = IntSize(downW, downH)) {
                    scale(
                        scaleX = BACKDROP_DOWNSCALE,
                        scaleY = BACKDROP_DOWNSCALE,
                        pivot = Offset.Zero
                    ) {
                        drawLayer(state.sourceLayer)
                    }
                }
                state.hasBlurredFrame = true
            } else {
                drawContent()
            }
        }
}

/**
 * Progressive top scrim combining real downsampled backdrop blur masked smoothly to 0%
 * at the bottom edge with a black-to-transparent vertical gradient.
 */
@Composable
fun ProgressiveTopBlurScrim(
    modifier: Modifier = Modifier
) {
    val backdropState = LocalPrismBackdrop.current
    val posHolder = remember { PositionHolder() }

    Box(modifier = modifier) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && backdropState != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .onGloballyPositioned { coords ->
                        posHolder.offsetInRoot = coords.positionInRoot()
                    }
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
                    .drawWithCache {
                        val maskBrush = Brush.verticalGradient(
                            0.0f to Color.Black,
                            0.46f to Color.Black.copy(alpha = 0.78f),
                            0.80f to Color.Black.copy(alpha = 0.25f),
                            1.0f to Color.Transparent
                        )
                        onDrawBehind {
                            backdropState.invalidationSignal()
                            if (backdropState.hasBlurredFrame) {
                                val dx = posHolder.offsetInRoot.x - backdropState.sourceOffsetInRoot.x
                                val dy = posHolder.offsetInRoot.y - backdropState.sourceOffsetInRoot.y
                                clipRect {
                                    translate(left = -dx, top = -dy) {
                                        scale(
                                            scaleX = BACKDROP_INV_SCALE,
                                            scaleY = BACKDROP_INV_SCALE,
                                            pivot = Offset.Zero
                                        ) {
                                            drawLayer(backdropState.blurredLayer)
                                        }
                                    }
                                }
                                drawRect(
                                    brush = maskBrush,
                                    blendMode = BlendMode.DstIn
                                )
                            }
                        }
                    }
            )
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .drawWithCache {
                    val scrimBrush = Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.56f),
                        0.50f to Color.Black.copy(alpha = 0.20f),
                        1.0f to Color.Transparent
                    )
                    onDrawBehind {
                        drawRect(brush = scrimBrush)
                    }
                }
        )
    }
}

private class PositionHolder {
    var offsetInRoot: Offset = Offset.Zero
}

/**
 * High-performance frosted matte Liquid Glass surface (iPhone-inspired).
 * Uses zero Modifier.composed, caches brushes, and samples a shared 0.25x blurred backdrop layer.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    cornerRadiusDp: Dp = 24.dp,
    @Suppress("UNUSED_PARAMETER") blurRadius: Float = 24f,
    tintOverride: Color? = null,
    darkFrostedOverride: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val prismColors = LocalPrismColors.current
    val backdropState = LocalPrismBackdrop.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val posHolder = remember { PositionHolder() }

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
            tintOverride.copy(alpha = 0.80f)
        } else {
            tintOverride.copy(alpha = if (useDarkFrost) 0.24f else 0.16f).compositeOver(baseTop)
        }
    } else {
        baseTop
    }

    val targetBottomColor = if (tintOverride != null) {
        if (darkFrostedOverride) {
            tintOverride.copy(alpha = 0.72f)
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

    val specularBrush = remember(
        darkFrostedOverride,
        prismColors.glassBorderStart,
        prismColors.glassBorderMid,
        prismColors.glassBorderEnd
    ) {
        if (darkFrostedOverride) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.34f),
                    Color.White.copy(alpha = 0.10f),
                    Color.White.copy(alpha = 0.18f)
                ),
                start = Offset.Zero,
                end = Offset(520f, 520f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    prismColors.glassBorderStart,
                    prismColors.glassBorderMid,
                    prismColors.glassBorderEnd
                ),
                start = Offset.Zero,
                end = Offset(520f, 520f)
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

    val satinHighlightAlpha = if (useDarkFrost) 0.065f else 0.22f

    Box(
        modifier = modifier
            .then(clickModifier)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .onGloballyPositioned { coords ->
                posHolder.offsetInRoot = coords.positionInRoot()
            }
            .clip(shape)
            .drawBehind {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    backdropState != null
                ) {
                    backdropState.invalidationSignal()
                    if (backdropState.hasBlurredFrame) {
                        val dx = posHolder.offsetInRoot.x - backdropState.sourceOffsetInRoot.x
                        val dy = posHolder.offsetInRoot.y - backdropState.sourceOffsetInRoot.y
                        clipRect {
                            translate(left = -dx, top = -dy) {
                                scale(
                                    scaleX = BACKDROP_INV_SCALE,
                                    scaleY = BACKDROP_INV_SCALE,
                                    pivot = Offset.Zero
                                ) {
                                    drawLayer(backdropState.blurredLayer)
                                }
                            }
                        }
                    }
                }
            }
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
            .border(width = 0.9.dp, brush = specularBrush, shape = shape),
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

/**
 * Detached frosted Liquid Glass bottom bar:
 * - Left: floating Album/Gallery pill capsule
 * - Right: standalone separate circular Settings button
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
