package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.model.MathNode
import com.example.ui.theme.MathAmber
import com.example.ui.theme.MathCyan
import com.example.ui.theme.MathGreen
import com.example.ui.theme.MathPink
import com.example.ui.theme.PurpleSecondary

/**
 * High quality visual mathematical formula renderer for Compose.
 * Displays real fractions (numerator over denominator with fraction bar),
 * radicals with indices, powers, integrals with upper/lower bounds,
 * limits, sigmas, and matrices.
 */
@Composable
fun MathFormulaView(
    node: MathNode,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    accentColor: Color = PurpleSecondary
) {
    Box(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        contentAlignment = Alignment.CenterStart
    ) {
        RenderMathNode(
            node = node,
            fontSize = fontSize,
            textColor = textColor,
            accentColor = accentColor
        )
    }
}

@Composable
fun RenderMathNode(
    node: MathNode,
    fontSize: TextUnit = 20.sp,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    accentColor: Color = PurpleSecondary,
    subScale: Float = 0.72f
) {
    val smallFontSize = (fontSize.value * subScale).sp

    when (node) {
        is MathNode.Number -> {
            Text(
                text = node.value,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Serif,
                color = textColor
            )
        }
        is MathNode.Variable -> {
            Text(
                text = node.name,
                fontSize = fontSize,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                color = MathCyan
            )
        }
        is MathNode.Constant -> {
            Text(
                text = node.symbol,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = MathAmber
            )
        }
        is MathNode.Fraction -> {
            // Real fraction layout: stacked numerator, horizontal bar, denominator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                // Numerator
                Box(
                    modifier = Modifier.padding(bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RenderMathNode(
                        node = node.numerator,
                        fontSize = (fontSize.value * 0.9f).sp,
                        textColor = textColor,
                        accentColor = accentColor
                    )
                }
                // Fraction line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(accentColor, RoundedCornerShape(1.dp))
                )
                // Denominator
                Box(
                    modifier = Modifier.padding(top = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RenderMathNode(
                        node = node.denominator,
                        fontSize = (fontSize.value * 0.9f).sp,
                        textColor = textColor,
                        accentColor = accentColor
                    )
                }
            }
        }
        is MathNode.Power -> {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RenderMathNode(
                    node = node.base,
                    fontSize = fontSize,
                    textColor = textColor,
                    accentColor = accentColor
                )
                // Elevated exponent
                Box(
                    modifier = Modifier
                        .offset(y = (-fontSize.value * 0.35f).dp)
                        .padding(start = 2.dp)
                ) {
                    RenderMathNode(
                        node = node.exponent,
                        fontSize = smallFontSize,
                        textColor = accentColor,
                        accentColor = accentColor
                    )
                }
            }
        }
        is MathNode.Root -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                if (node.index != null) {
                    Box(
                        modifier = Modifier
                            .offset(y = (-fontSize.value * 0.3f).dp)
                            .padding(end = 1.dp)
                    ) {
                        RenderMathNode(
                            node = node.index,
                            fontSize = (fontSize.value * 0.6f).sp,
                            textColor = accentColor,
                            accentColor = accentColor
                        )
                    }
                }
                Text(
                    text = "√",
                    fontSize = (fontSize.value * 1.3f).sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Serif,
                    color = accentColor
                )
                Box(
                    modifier = Modifier
                        .padding(start = 1.dp)
                        .border(
                            width = 0.dp,
                            color = Color.Transparent
                        )
                ) {
                    // Overbar across radicand
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(accentColor)
                        )
                        Box(modifier = Modifier.padding(top = 2.dp, start = 2.dp, end = 2.dp)) {
                            RenderMathNode(
                                node = node.radicand,
                                fontSize = fontSize,
                                textColor = textColor,
                                accentColor = accentColor
                            )
                        }
                    }
                }
            }
        }
        is MathNode.Addition -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RenderMathNode(node.left, fontSize, textColor, accentColor)
                Text(
                    text = " + ",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Normal,
                    color = textColor
                )
                RenderMathNode(node.right, fontSize, textColor, accentColor)
            }
        }
        is MathNode.Subtraction -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RenderMathNode(node.left, fontSize, textColor, accentColor)
                Text(
                    text = " − ",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Normal,
                    color = textColor
                )
                RenderMathNode(node.right, fontSize, textColor, accentColor)
            }
        }
        is MathNode.Multiplication -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RenderMathNode(node.left, fontSize, textColor, accentColor)
                val sign = if (node.showDot) " · " else " × "
                Text(
                    text = sign,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Normal,
                    color = textColor
                )
                RenderMathNode(node.right, fontSize, textColor, accentColor)
            }
        }
        is MathNode.Division -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RenderMathNode(node.left, fontSize, textColor, accentColor)
                Text(
                    text = " ÷ ",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Normal,
                    color = textColor
                )
                RenderMathNode(node.right, fontSize, textColor, accentColor)
            }
        }
        is MathNode.FunctionCall -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${node.name}",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = MathPink
                )
                Text(
                    text = "(",
                    fontSize = fontSize,
                    color = textColor
                )
                RenderMathNode(node.arg, fontSize, textColor, accentColor)
                Text(
                    text = ")",
                    fontSize = fontSize,
                    color = textColor
                )
            }
        }
        is MathNode.Limit -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text(
                        text = "lim",
                        fontSize = (fontSize.value * 1.1f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = accentColor
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${node.variable} → ",
                            fontSize = (fontSize.value * 0.65f).sp,
                            color = textColor
                        )
                        RenderMathNode(
                            node = node.target,
                            fontSize = (fontSize.value * 0.65f).sp,
                            textColor = textColor,
                            accentColor = accentColor
                        )
                        if (node.direction.isNotEmpty()) {
                            Text(
                                text = node.direction,
                                fontSize = (fontSize.value * 0.55f).sp,
                                color = accentColor
                            )
                        }
                    }
                }
                RenderMathNode(node.expr, fontSize, textColor, accentColor)
            }
        }
        is MathNode.Derivative -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // d / dx or d^n / dx^n
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    val dTop = if (node.order > 1) "d${node.order}" else "d"
                    Text(
                        text = dTop,
                        fontSize = (fontSize.value * 0.85f).sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        color = accentColor
                    )
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(1.5.dp)
                            .background(accentColor)
                    )
                    val dBottom = if (node.order > 1) "d${node.variable}${node.order}" else "d${node.variable}"
                    Text(
                        text = dBottom,
                        fontSize = (fontSize.value * 0.85f).sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Serif,
                        color = accentColor
                    )
                }
                Text(text = "[", fontSize = (fontSize.value * 1.2f).sp, color = textColor)
                RenderMathNode(node.expr, fontSize, textColor, accentColor)
                Text(text = "]", fontSize = (fontSize.value * 1.2f).sp, color = textColor)

                if (node.atPoint != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "|",
                            fontSize = (fontSize.value * 1.4f).sp,
                            fontWeight = FontWeight.Light,
                            color = accentColor
                        )
                        Column(modifier = Modifier.padding(start = 2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${node.variable} = ",
                                    fontSize = (fontSize.value * 0.65f).sp,
                                    color = textColor
                                )
                                RenderMathNode(
                                    node = node.atPoint,
                                    fontSize = (fontSize.value * 0.65f).sp,
                                    textColor = textColor,
                                    accentColor = accentColor
                                )
                            }
                        }
                    }
                }
            }
        }
        is MathNode.Integral -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Integral sign with bounds
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = "∫",
                        fontSize = (fontSize.value * 1.8f).sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Serif,
                        color = accentColor
                    )
                    if (node.upperBound != null) {
                        Box(
                            modifier = Modifier
                                .offset(x = 12.dp, y = (-fontSize.value * 0.6f).dp)
                        ) {
                            RenderMathNode(
                                node = node.upperBound,
                                fontSize = (fontSize.value * 0.65f).sp,
                                textColor = textColor,
                                accentColor = accentColor
                            )
                        }
                    }
                    if (node.lowerBound != null) {
                        Box(
                            modifier = Modifier
                                .offset(x = 10.dp, y = (fontSize.value * 0.6f).dp)
                        ) {
                            RenderMathNode(
                                node = node.lowerBound,
                                fontSize = (fontSize.value * 0.65f).sp,
                                textColor = textColor,
                                accentColor = accentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))
                RenderMathNode(node.integrand, fontSize, textColor, accentColor)
                Text(
                    text = " d${node.variable}",
                    fontSize = fontSize,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Serif,
                    color = MathCyan
                )
            }
        }
        is MathNode.Sigma -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    // Upper bound
                    RenderMathNode(
                        node = node.upper,
                        fontSize = (fontSize.value * 0.65f).sp,
                        textColor = textColor,
                        accentColor = accentColor
                    )
                    // Sigma glyph
                    Text(
                        text = "∑",
                        fontSize = (fontSize.value * 1.7f).sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.Serif,
                        color = accentColor
                    )
                    // Lower bound
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${node.indexVar}=",
                            fontSize = (fontSize.value * 0.6f).sp,
                            color = textColor
                        )
                        RenderMathNode(
                            node = node.lower,
                            fontSize = (fontSize.value * 0.6f).sp,
                            textColor = textColor,
                            accentColor = accentColor
                        )
                    }
                }
                RenderMathNode(node.summand, fontSize, textColor, accentColor)
            }
        }
        is MathNode.Matrix -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                // Left bracket
                Text(
                    text = "[",
                    fontSize = (fontSize.value * (node.rows * 0.9f + 0.6f)).sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = accentColor
                )
                // Grid of values
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    for (row in node.values) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (cell in row) {
                                Box(
                                    modifier = Modifier.widthIn(min = 28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    RenderMathNode(
                                        node = cell,
                                        fontSize = (fontSize.value * 0.9f).sp,
                                        textColor = textColor,
                                        accentColor = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
                // Right bracket
                Text(
                    text = "]",
                    fontSize = (fontSize.value * (node.rows * 0.9f + 0.6f)).sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = accentColor
                )
            }
        }
        is MathNode.Group -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "(",
                    fontSize = (fontSize.value * 1.15f).sp,
                    color = textColor
                )
                RenderMathNode(node.inner, fontSize, textColor, accentColor)
                Text(
                    text = ")",
                    fontSize = (fontSize.value * 1.15f).sp,
                    color = textColor
                )
            }
        }
        is MathNode.Equation -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RenderMathNode(node.left, fontSize, textColor, accentColor)
                Text(
                    text = " = ",
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = MathGreen
                )
                RenderMathNode(node.right, fontSize, textColor, accentColor)
            }
        }
        is MathNode.TextNode -> {
            Text(
                text = node.text,
                fontSize = fontSize,
                color = textColor
            )
        }
    }
}
