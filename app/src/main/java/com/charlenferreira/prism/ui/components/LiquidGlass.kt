package com.charlenferreira.prism.ui.components

import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
    shape: Shape = RoundedCornerShape(28.dp),
    blurRadius: Float = 28f,
    isPressed: Boolean = false,
    borderWidth: Dp = 1.25.dp,
    tintOverride: Color? = null,
    cornerRadiusDp: Dp = 28.dp
): Modifier = composed {
    val prismColors = LocalPrismColors.current
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 800f
        ),
        label = "liquidGlassSpringScale"
    )

    val topColor = tintOverride?.copy(alpha = 0.34f) ?: prismColors.glassFillTop
    val bottomColor = tintOverride?.copy(alpha = 0.16f) ?: prismColors.glassFillBottom
    val specularBrush = Brush.linearGradient(
        colors = listOf(
            prismColors.glassBorderStart,
            prismColors.glassBorderMid,
            prismColors.glassBorderEnd
        )
    )
    val glassRenderNode = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) RenderNode("LiquidGlassLayer") else null
    }

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .shadow(
            elevation = if (prismColors.isDarkSurface) 14.dp else 10.dp,
            shape = shape,
            ambientColor = prismColors.ambientOrbPrimary,
            spotColor = prismColors.ambientOrbSecondary
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
                val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = prismColors.ambientOrbPrimary.copy(alpha = 0.25f).toArgb()
                }
                rec.drawCircle(size.width * 0.22f, size.height * 0.15f, size.minDimension * 0.6f, orbPaint)
                glassRenderNode.endRecording()
                nativeCanvas.drawRenderNode(glassRenderNode)
            } else {
                // Fallback gradient for API < 31 and software canvases
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(topColor, bottomColor)
                    ),
                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                )
            }

            // Specular inner sheen gradient
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (prismColors.isDarkSurface) 0.16f else 0.48f),
                        Color.Transparent,
                        bottomColor
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                ),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
            // Crisp specular hairline accent
            drawRoundRect(
                brush = specularBrush,
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                style = Stroke(width = borderWidth.toPx())
            )
        }
        .border(width = borderWidth, brush = specularBrush, shape = shape)
}

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    cornerRadiusDp: Dp = 24.dp,
    blurRadius: Float = 32f,
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
        // Dedicated blurred chromatic glass underlay using Compose graphicsLayer RenderEffect on API 31+
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
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = prismColors.ambientOrbPrimary.copy(alpha = 0.22f),
                    radius = size.minDimension * 0.7f,
                    center = Offset(size.width * 0.18f, size.height * 0.2f)
                )
                drawCircle(
                    color = prismColors.ambientOrbSecondary.copy(alpha = 0.18f),
                    radius = size.minDimension * 0.65f,
                    center = Offset(size.width * 0.85f, size.height * 0.85f)
                )
            }
        }
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
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(prismColors.ambientOrbPrimary, Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.12f),
                    radius = size.width * 0.85f
                ),
                radius = size.width * 0.85f,
                center = Offset(size.width * 0.15f, size.height * 0.12f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(prismColors.ambientOrbSecondary, Color.Transparent),
                    center = Offset(size.width * 0.88f, size.height * 0.78f),
                    radius = size.width * 0.75f
                ),
                radius = size.width * 0.75f,
                center = Offset(size.width * 0.88f, size.height * 0.78f)
            )
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
            .padding(horizontal = 36.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            shape = CircleShape,
            cornerRadiusDp = 32.dp,
            blurRadius = 36f,
            modifier = Modifier.height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillNavItem(
                    selected = isGallerySelected,
                    label = stringResource(R.string.tab_gallery),
                    selectedIcon = Icons.Rounded.PhotoLibrary,
                    unselectedIcon = Icons.Outlined.PhotoLibrary,
                    testTag = "nav_gallery_tab",
                    onClick = onGalleryClick
                )
                PillNavItem(
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
private fun PillNavItem(
    selected: Boolean,
    label: String,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.94f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 800f
        ),
        label = "pillTabSpring"
    )
    val prismColors = LocalPrismColors.current
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = if (prismColors.isDarkSurface) 0.28f else 0.16f)
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .testTag(testTag)
            .height(50.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .drawBehind {
                if (selected) {
                    drawRoundRect(
                        color = containerColor,
                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                    )
                    drawRoundRect(
                        color = contentColor.copy(alpha = 0.35f),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp),
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
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}
