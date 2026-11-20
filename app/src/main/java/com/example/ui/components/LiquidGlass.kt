package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Colours of a liquid-glass surface: three fill stops (top to bottom), rim colour and optional glow. */
class LiquidGlassStyle(
    val fill: List<Color>,
    val border: Color,
    val glow: Color? = null
)

/**
 * Liquid-glass look drawn in code: a translucent tinted body, light gathered along the lower
 * edge, a curved gloss band, specular spots, an inner rim and a bright outer rim.
 * [pill] makes the shape a full pill, otherwise the corners use [corner].
 */
fun Modifier.liquidGlass(
    style: LiquidGlassStyle,
    isDark: Boolean,
    pill: Boolean = false,
    corner: Dp = 14.dp
): Modifier {
    val shape = if (pill) RoundedCornerShape(percent = 50) else RoundedCornerShape(corner)
    val shadowTint = style.glow ?: (if (isDark) Color(0xFF000000) else Color(0xFF6A6480))
    return this
        .shadow(
            elevation = if (style.glow != null) 10.dp else 6.dp,
            shape = shape,
            clip = false,
            ambientColor = shadowTint.copy(alpha = 0.45f),
            spotColor = shadowTint.copy(alpha = 0.45f)
        )
        .clip(shape)
        .background(Brush.verticalGradient(style.fill.map { it.copy(alpha = if (isDark) 0.78f else 0.88f) }))
        .drawBehind {
            val w = size.width
            val h = size.height
            val px = 1.dp.toPx()
            val radius = if (pill) h / 2 else minOf(corner.toPx(), h / 2)
            val tint = style.fill[0]

            // light collected along the bottom edge
            drawRect(
                brush = Brush.verticalGradient(
                    0.55f to Color.Transparent,
                    1f to tint.copy(alpha = 0.55f)
                        .compositeOver(Color.White.copy(alpha = if (isDark) 0.10f else 0.0f))
                )
            )
            // curved gloss band on the upper half
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isDark) 0.34f else 0.75f),
                        Color.White.copy(alpha = if (isDark) 0.04f else 0.10f)
                    ),
                    startY = 3 * px,
                    endY = h * 0.52f
                ),
                topLeft = Offset(radius * 0.7f, 3 * px),
                size = Size(w - radius * 1.4f, h * 0.46f),
                cornerRadius = CornerRadius(radius * 0.5f)
            )
            // bright specular spot, top-left
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDark) 0.45f else 0.9f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.20f, h * 0.16f),
                    radius = w * 0.26f
                )
            )
            // fainter spot, bottom-right
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = if (isDark) 0.28f else 0.6f), Color.Transparent),
                    center = Offset(w * 0.86f, h * 0.88f),
                    radius = w * 0.22f
                )
            )
            // inner rim, just inside the edge
            drawRoundRect(
                color = Color.White.copy(alpha = if (isDark) 0.10f else 0.35f),
                topLeft = Offset(2.5f * px, 2.5f * px),
                size = Size(w - 5 * px, h - 5 * px),
                cornerRadius = CornerRadius(radius - 2.5f * px),
                style = Stroke(width = px)
            )
        }
        // outer rim: bright at top-left and bottom-right, dim on the sides
        .border(
            1.5.dp,
            Brush.linearGradient(
                0f to Color.White.copy(alpha = if (isDark) 0.85f else 1f),
                0.35f to style.border.copy(alpha = style.border.alpha * 0.5f),
                0.65f to style.border.copy(alpha = style.border.alpha * 0.5f),
                1f to Color.White.copy(alpha = if (isDark) 0.65f else 0.9f)
            ),
            shape
        )
}
