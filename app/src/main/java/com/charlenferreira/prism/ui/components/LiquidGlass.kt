package com.charlenferreira.prism.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
)

val LocalPrismBackdrop = compositionLocalOf<PrismBackdropState?> { null }
val LocalLiquidGlassConfig = compositionLocalOf { LiquidGlassConfig() }

@Composable
fun rememberPrismBackdropState(): PrismBackdropState {
    val backdrop = rememberLayerBackdrop()
    return remember(backdrop) { PrismBackdropState(backdrop) }
}

fun Modifier.prismBackdropSource(state: PrismBackdropState?): Modifier {
    return if (state != null) {
        this.layerBackdrop(state.layerBackdrop)
    } else {
        this
    }
}

@Composable
fun ProgressiveTopBlurScrim(
    modifier: Modifier = Modifier,
    height: Dp = 112.dp
) {
    val extendedColors = LocalPrismColors.current
    val topColor = extendedColors.backgroundTop.copy(alpha = 0.88f)
    val midColor = extendedColors.backgroundTop.copy(alpha = 0.42f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        0.0f to topColor,
                        0.55f to midColor,
                        1.0f to Color.Transparent
                    )
                )
            }
    )
}

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    config: LiquidGlassConfig = LocalLiquidGlassConfig.current,
    contentPadding: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val extendedColors = LocalPrismColors.current
    val backdropState = LocalPrismBackdrop.current

    val tintColor = extendedColors.glassFillTop.copy(
        alpha = config.tintAlpha.coerceIn(0.10f, 0.85f)
    )

    val borderBrush = remember(extendedColors, config.rimLightAlpha, config.prismEdge) {
        val rim = config.rimLightAlpha.coerceIn(0.1f, 1.0f)
        if (config.prismEdge) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF7DD3FC).copy(alpha = 0.65f * rim),
                    Color(0xFFF472B6).copy(alpha = 0.45f * rim),
                    Color(0xFFFDE047).copy(alpha = 0.45f * rim),
                    extendedColors.glassBorderEnd.copy(alpha = 0.60f * rim)
                ),
                start = Offset.Zero,
                end = Offset(600f, 600f)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    extendedColors.glassBorderStart.copy(alpha = rim),
                    extendedColors.glassBorderMid.copy(alpha = rim),
                    extendedColors.glassBorderEnd.copy(alpha = rim)
                ),
                start = Offset.Zero,
                end = Offset(400f, 400f)
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

    val glassModifier = if (backdropState != null) {
        Modifier.drawBackdrop(
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
                drawRect(tintColor)
            }
        )
    } else {
        Modifier.drawBehind {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        extendedColors.glassFillTop,
                        extendedColors.glassFillBottom
                    )
                )
            )
        }
    }

    Box(
        modifier = modifier
            .then(shadowModifier)
            .clip(shape)
            .then(glassModifier)
            .border(width = 0.85.dp, brush = borderBrush, shape = shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlassSurface(
        modifier = modifier,
        shape = shape,
        content = content
    )
}

@Composable
fun LiquidGlassPillNavBar(
    currentRoute: String,
    onSelectRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        LiquidGlassSurface(
            shape = CircleShape,
            contentPadding = 6.dp
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillNavItem(
                    label = stringResource(R.string.nav_gallery),
                    selected = currentRoute == "gallery",
                    selectedIcon = Icons.Rounded.PhotoLibrary,
                    unselectedIcon = Icons.Outlined.PhotoLibrary,
                    testTag = "nav_gallery",
                    onClick = { onSelectRoute("gallery") }
                )
                PillNavItem(
                    label = stringResource(R.string.nav_settings),
                    selected = currentRoute == "settings",
                    selectedIcon = Icons.Rounded.Settings,
                    unselectedIcon = Icons.Outlined.Settings,
                    testTag = "nav_settings",
                    onClick = { onSelectRoute("settings") }
                )
            }
        }
    }
}

@Composable
private fun PillNavItem(
    label: String,
    selected: Boolean,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "pill_scale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(220),
        label = "pill_container"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(220),
        label = "pill_content"
    )

    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .drawBehind { drawRect(containerColor) }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
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
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor
        )
    }
}
