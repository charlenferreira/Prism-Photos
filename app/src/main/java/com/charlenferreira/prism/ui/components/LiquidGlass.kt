package com.charlenferreira.prism.ui.components

import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charlenferreira.prism.R
import com.charlenferreira.prism.ui.theme.LocalPrismColors

fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(26.dp),
    blurRadius: Float = 24f,
    isPressed: Boolean = false,
    borderWidth: Dp = 1.dp,
    tintOverride: Color? = null,
    cornerRadiusDp: Dp = 26.dp
): Modifier = composed {
    val prismColors = LocalPrismColors.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 420f
        ),
        label = "liquidGlassPressScale"
    )

    val topColor by animateColorAsState(
        targetValue = tintOverride?.copy(alpha = 0.30f) ?: prismColors.glassFillTop,
        animationSpec = tween(260),
        label = "liquidGlassTopTint"
    )
    val bottomColor by animateColorAsState(
        targetValue = tintOverride?.copy(alpha = 0.16f) ?: prismColors.glassFillBottom,
        animationSpec = tween(260),
        label = "liquidGlassBottomTint"
    )

    val specularBrush = Brush.linearGradient(
        colors = listOf(
            prismColors.glassBorderStart,
            prismColors.glassBorderMid,
            prismColors.glassBorderEnd
        ),
        start = Offset.Zero,
        end = Offset(600f, 600f)
    )
    val glassRenderNode = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) RenderNode("PrismLiquidGlass") else null
    }

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .shadow(
            elevation = if (prismColors.isDarkSurface) 10.dp else 8.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = if (prismColors.isDarkSurface) 0.45f else 0.10f),
            spotColor = Color.Black.copy(alpha = if (prismColors.isDarkSurface) 0.50f else 0.12f)
        )
        .clip(shape)
        .drawBehind {
            val radiusPx = cornerRadiusDp.toPx().coerceAtMost(size.minDimension / 2f)
            val nativeCanvas = drawContext.canvas.nativeCanvas
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                glassRenderNode != null &&
                nativeCanvas.isHardwareAccelerated &&
                size.width > 0f &&
                size.height > 0f
            ) {
                val w = size.width.toInt().coerceAtLeast(1)
                val h = size.height.toInt().coerceAtLeast(1)
                glassRenderNode.setPosition(0, 0, w, h)
                glassRenderNode.setRenderEffect(
                    RenderEffect.createBlurEffect(
                        blurRadius,
                        blurRadius,
                        Shader.TileMode.CLAMP
                    )
                )
                val rec = glassRenderNode.beginRecording(w, h)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = topColor.toArgb()
                }
                rec.drawRoundRect(0f, 0f, size.width, size.height, radiusPx, radiusPx, paint)
                glassRenderNode.endRecording()
                nativeCanvas.drawRenderNode(glassRenderNode)
            }

            // Smooth uniform frosted glass gradient without localized blotches
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                ),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )

            // Subtle upper specular rim sheen
            drawRoundRect(
                brush = Brush.verticalGradient(
                    0.0f to Color.White.copy(alpha = if (prismColors.isDarkSurface) 0.10f else 0.34f),
                    0.35f to Color.Transparent,
                    1.0f to Color.Transparent
                ),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
        }
        .border(width = borderWidth, brush = specularBrush, shape = shape)
}

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    cornerRadiusDp: Dp = 24.dp,
    blurRadius: Float = 24f,
    tintOverride: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val prismColors = LocalPrismColors.current
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
                blurRadius = blurRadius,
                isPressed = isPressed,
                tintOverride = tintOverride,
                cornerRadiusDp = cornerRadiusDp
            )
    ) {
        // Uniform blurred glass backing on API 31+ (no clipped circles that cause diagonal seams)
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        renderEffect = RenderEffect.createBlurEffect(
                            blurRadius,
                            blurRadius,
                            Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            prismColors.glassFillTop.copy(alpha = 0.25f),
                            prismColors.glassFillBottom.copy(alpha = 0.15f)
                        )
                    )
                )
        )
        content()
    }
}

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val prismColors = LocalPrismColors.current
    val bgTop by animateColorAsState(
        targetValue = prismColors.backgroundTop,
        animationSpec = tween(360),
        label = "bgTop"
    )
    val bgBottom by animateColorAsState(
        targetValue = prismColors.backgroundBottom,
        animationSpec = tween(360),
        label = "bgBottom"
    )
    val orbPrimary by animateColorAsState(
        targetValue = prismColors.ambientOrbPrimary,
        animationSpec = tween(360),
        label = "orbPrimary"
    )
    val orbSecondary by animateColorAsState(
        targetValue = prismColors.ambientOrbSecondary,
        animationSpec = tween(360),
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            shape = CircleShape,
            cornerRadiusDp = 34.dp,
            blurRadius = 30f,
            modifier = Modifier
                .height(64.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = 340f
                    )
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExpandableGlassTab(
                    selected = isGallerySelected,
                    label = stringResource(R.string.tab_gallery),
                    selectedIcon = Icons.Rounded.PhotoLibrary,
                    unselectedIcon = Icons.Outlined.PhotoLibrary,
                    testTag = "nav_gallery_tab",
                    onClick = onGalleryClick
                )
                ExpandableGlassTab(
                    selected = !isGallerySelected,
                    label = stringResource(R.string.tab_settings),
                    selectedIcon = Icons.Rounded.Settings,
                    unselectedIcon = Icons.Outlined.Settings,
                    testTag = "nav_settings_tab",
                    onClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
private fun ExpandableGlassTab(
    selected: Boolean,
    label: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    val prismColors = LocalPrismColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = 460f
        ),
        label = "tabPressScale"
    )
    val horizontalPadding by animateDpAsState(
        targetValue = if (selected) 20.dp else 12.dp,
        animationSpec = spring(
            dampingRatio = 0.78f,
            stiffness = 340f
        ),
        label = "tabHorizontalPadding"
    )
    val capsuleFill by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(
                alpha = if (prismColors.isDarkSurface) 0.24f else 0.16f
            )
        } else {
            Color.Transparent
        },
        animationSpec = tween(240),
        label = "tabCapsuleFill"
    )
    val capsuleStroke by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(
                alpha = if (prismColors.isDarkSurface) 0.45f else 0.32f
            )
        } else {
            Color.Transparent
        },
        animationSpec = tween(240),
        label = "tabCapsuleStroke"
    )
    val contentTint by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(220),
        label = "tabContentTint"
    )

    Box(
        modifier = Modifier
            .testTag(testTag)
            .height(48.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .clip(CircleShape)
            .background(capsuleFill)
            .border(width = 1.dp, color = capsuleStroke, shape = CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.78f,
                    stiffness = 340f
                )
            )
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = contentTint,
                modifier = Modifier.size(22.dp)
            )
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(animationSpec = tween(200, delayMillis = 40)) +
                    expandHorizontally(
                        animationSpec = spring(
                            dampingRatio = 0.78f,
                            stiffness = 340f
                        ),
                        expandFrom = Alignment.Start
                    ),
                exit = fadeOut(animationSpec = tween(140)) +
                    shrinkHorizontally(
                        animationSpec = spring(
                            dampingRatio = 0.82f,
                            stiffness = 380f
                        ),
                        shrinkTowards = Alignment.Start
                    )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = contentTint,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
