package com.charlenferreira.prism.ui.components

import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
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
        targetValue = if (isPressed) 0.968f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 800f
        ),
        label = "liquidGlassSpringScale"
    )

    val topColor = tintOverride?.copy(alpha = if (prismColors.isDarkSurface) 0.34f else 0.22f)
        ?: prismColors.glassFillTop
    val bottomColor = tintOverride?.copy(alpha = if (prismColors.isDarkSurface) 0.20f else 0.12f)
        ?: prismColors.glassFillBottom

    val specularBorderBrush = Brush.linearGradient(
        colors = listOf(
            prismColors.glassBorderStart,
            prismColors.glassBorderMid,
            prismColors.glassBorderEnd
        )
    )
    val glassRenderNode = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) RenderNode("PrismGlassSurface") else null
    }

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .shadow(
            elevation = if (prismColors.isDarkSurface) 10.dp else 8.dp,
            shape = shape,
            ambientColor = if (prismColors.ambientOrbPrimary == Color.Transparent) {
                Color.Black.copy(alpha = 0.5f)
            } else {
                prismColors.ambientOrbPrimary
            },
            spotColor = if (prismColors.ambientOrbSecondary == Color.Transparent) {
                Color.Black.copy(alpha = 0.4f)
            } else {
                prismColors.ambientOrbSecondary
            }
        )
        .clip(shape)
        .drawBehind {
            val radiusPx = cornerRadiusDp.toPx().coerceAtMost(size.minDimension / 2f)
            val nativeCanvas = drawContext.canvas.nativeCanvas

            // Uniform frosted translucent base (RenderEffect on API 31+, gradient fallback otherwise)
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

            // Smooth uniform frosted glass gradient without asymmetric inner blobs
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                ),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )

            // Subtle top-edge iOS-style specular reflection band
            drawRoundRect(
                brush = Brush.verticalGradient(
                    0.0f to prismColors.glassSpecularTop,
                    0.35f to Color.Transparent,
                    1.0f to Color.Transparent
                ),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )

            // Crisp hairline rim accent
            drawRoundRect(
                brush = specularBorderBrush,
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                style = Stroke(width = borderWidth.toPx())
            )
        }
        .border(width = borderWidth, brush = specularBorderBrush, shape = shape)
}

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    cornerRadiusDp: Dp = 26.dp,
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
        // Uniform frosted backdrop layer on API 31+ with zero off-center blotches
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
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        prismColors.backgroundTop,
                        prismColors.backgroundBottom
                    )
                )
            )
            if (prismColors.ambientOrbPrimary != Color.Transparent) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(prismColors.ambientOrbPrimary, Color.Transparent),
                        center = Offset(size.width * 0.18f, size.height * 0.14f),
                        radius = size.width * 0.9f
                    ),
                    radius = size.width * 0.9f,
                    center = Offset(size.width * 0.18f, size.height * 0.14f)
                )
            }
            if (prismColors.ambientOrbSecondary != Color.Transparent) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(prismColors.ambientOrbSecondary, Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.82f),
                        radius = size.width * 0.85f
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(size.width * 0.85f, size.height * 0.82f)
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
    val prismColors = LocalPrismColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 42.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            shape = CircleShape,
            cornerRadiusDp = 32.dp,
            blurRadius = 30f,
            modifier = Modifier
                .width(276.dp)
                .height(60.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
            ) {
                val tabWidth = maxWidth / 2f
                val indicatorOffset by animateDpAsState(
                    targetValue = if (isGallerySelected) 0.dp else tabWidth,
                    animationSpec = spring(
                        dampingRatio = 0.68f,
                        stiffness = 520f
                    ),
                    label = "pillIndicatorSlide"
                )

                // Sliding iOS Liquid Glass active capsule
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = if (prismColors.isDarkSurface) 0.24f else 0.16f
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (prismColors.isDarkSurface) 0.35f else 0.85f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PillNavItem(
                        selected = isGallerySelected,
                        label = stringResource(R.string.tab_gallery),
                        selectedIcon = Icons.Rounded.PhotoLibrary,
                        unselectedIcon = Icons.Outlined.PhotoLibrary,
                        testTag = "nav_gallery_tab",
                        onClick = onGalleryClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    PillNavItem(
                        selected = !isGallerySelected,
                        label = stringResource(R.string.tab_settings),
                        selectedIcon = Icons.Rounded.Settings,
                        unselectedIcon = Icons.Outlined.Settings,
                        testTag = "nav_settings_tab",
                        onClick = onSettingsClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun PillNavItem(
    selected: Boolean,
    label: String,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            selected -> 1f
            else -> 0.95f
        },
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 800f
        ),
        label = "pillTabSpring"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 420f),
        label = "pillTabContentColor"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}
