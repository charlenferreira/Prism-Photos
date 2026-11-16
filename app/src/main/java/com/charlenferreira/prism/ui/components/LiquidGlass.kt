package com.charlenferreira.prism.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

fun Modifier.liquidGlass(
    tint: Color = Color.White.copy(alpha = 0.4f),
    cornerRadius: Int = 24,
    blurRadius: Float = 25f,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val isPressed by (interactionSource?.collectIsPressedAsState() ?: remember { mutableStateOf(false) })
    
    // Animação MUITO mais lenta e fluida (spring com baixa rigidez)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 120f), 
        label = "liquid_scale"
    )

    var renderEffect by remember { mutableStateOf<RenderEffect?>(null) }

    LaunchedEffect(blurRadius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            renderEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
        }
    }

    // Brilho especular (o "reflexo" do vidro líquido nas bordas)
    val specularHighlight = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.8f),
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.0f),
            Color.White.copy(alpha = 0.3f)
        ),
        start = Offset.Zero,
        end = Offset.Infinite
    )

    this
        .graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                renderEffect?.let { this.renderEffect = it.asComposeRenderEffect() }
            }
        }
        .clip(RoundedCornerShape(cornerRadius.dp))
        .background(tint)
        .border(
            width = 1.5.dp, // Borda mais grossa e com gradiente realista
            brush = specularHighlight,
            shape = RoundedCornerShape(cornerRadius.dp)
        )
}
