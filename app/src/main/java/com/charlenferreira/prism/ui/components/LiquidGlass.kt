package com.charlenferreira.prism.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charlenferreira.prism.R
import com.charlenferreira.prism.ui.theme.LocalPrismColors

/**
 * Lightweight GPU-accelerated Liquid Glass modifier.
 * Avoids per-frame RenderNode re-recording so press, scroll, and tab animations stay at 120fps.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(26.dp),
    isPressed: Boolean = false,
    borderWidth: Dp = 1.dp,
    tintOverride: Color? = null,
    cornerRadiusDp: Dp = 26.dp
): Modifier = composed {
    val prismColors = LocalPrismColors.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 620f
        ),
        label = "liquidGlassPressScale"
    )

    val topColor by animateColorAsState(
        targetValue = tintOverride?.copy(alpha = 0.34f) ?: prismColors.glassFillTop,
        animationSpec = tween(180),
        label = "liquidGlassTopTint"
    )
    val bottomColor by animateColorAsState(
        targetValue = tintOverride?.copy(alpha = 0.20f) ?: prismColors.glassFillBottom,
        animationSpec = tween(180),
        label = "liquidGlassBottomTint"
    )

    val specularBrush = remember(
        prismColors.glassBorderStart,
        prismColors.glassBorderMid,
        prismColors.glassBorderEnd
    ) {
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

    val topHighlight = if (prismColors.isDarkSurface) 0.12f else 0.36f

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clip(shape)
        .drawBehind {
            val radiusPx = cornerRadiusDp.toPx().coerceAtMost(size.minDimension / 2f)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                ),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = topHighlight),
                    0.32f to Color.Transparent,
                    1.0f to Color.Transparent
                ),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
        }
        .border(width = borderWidth, brush = specularBrush, shape = shape)
}

/**
 * Applies hardware RenderEffect blur on API 31+ for static/top scrim layers with gradient fallback.
 */
fun Modifier.liquidGlassBlurLayer(radius: Float = 24f): Modifier = graphicsLayer {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        renderEffect = RenderEffect.createBlurEffect(
            radius,
            radius,
            Shader.TileMode.CLAMP
        ).asComposeRenderEffect()
    }
}

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    cornerRadiusDp: Dp = 24.dp,
    @Suppress("UNUSED_PARAMETER") blurRadius: Float = 24f,
    tintOverride: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

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
            .then(clickModifier)
            .liquidGlass(
                shape = shape,
                isPressed = isPressed,
                tintOverride = tintOverride,
                cornerRadiusDp = cornerRadiusDp
            ),
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
        animationSpec = tween(260),
        label = "bgTop"
    )
    val bgBottom by animateColorAsState(
        targetValue = prismColors.backgroundBottom,
        animationSpec = tween(260),
        label = "bgBottom"
    )
    val orbPrimary by animateColorAsState(
        targetValue = prismColors.ambientOrbPrimary,
        animationSpec = tween(260),
        label = "orbPrimary"
    )
    val orbSecondary by animateColorAsState(
        targetValue = prismColors.ambientOrbSecondary,
        animationSpec = tween(260),
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
 * Detached Liquid Glass bottom bar:
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
    val prismColors = LocalPrismColors.current
    val galleryLabel = stringResource(R.string.tab_gallery)
    val settingsLabel = stringResource(R.string.tab_settings)

    val galleryScale by animateFloatAsState(
        targetValue = if (isGallerySelected) 1f else 0.95f,
        animationSpec = spring(dampingRatio = 0.74f, stiffness = 540f),
        label = "galleryPillScale"
    )
    val settingsScale by animateFloatAsState(
        targetValue = if (!isGallerySelected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 540f),
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
        // Left: Album / Gallery floating pill
        LiquidGlassSurface(
            shape = CircleShape,
            cornerRadiusDp = 30.dp,
            tintOverride = if (isGallerySelected) {
                MaterialTheme.colorScheme.primary.copy(
                    alpha = if (prismColors.isDarkSurface) 0.55f else 0.40f
                )
            } else {
                null
            },
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

        // Right: Separate standalone circular Settings button
        LiquidGlassSurface(
            shape = CircleShape,
            cornerRadiusDp = 28.dp,
            tintOverride = if (!isGallerySelected) {
                MaterialTheme.colorScheme.primary.copy(
                    alpha = if (prismColors.isDarkSurface) 0.65f else 0.45f
                )
            } else {
                null
            },
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
