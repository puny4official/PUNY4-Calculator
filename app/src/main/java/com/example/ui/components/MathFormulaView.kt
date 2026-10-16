package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.model.MathNode
import com.example.ui.theme.MathCyan
import com.example.ui.theme.MathPink
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ScrollableMathFormula(
    node: MathNode,
    modifier: Modifier = Modifier,
    baseFontSize: TextUnit = 22.sp,
    textColor: Color = TextPrimary,
    accentColor: Color = PurpleAccent
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = modifier.horizontalScroll(scrollState),
        contentAlignment = Alignment.CenterStart
    ) {
        MathFormulaView(
            node = node,
            baseFontSize = baseFontSize,
            textColor = textColor,
            accentColor = accentColor
        )
    }
}

@Composable
fun MathFormulaView(
    node: MathNode,
    modifier: Modifier = Modifier,
    baseFontSize: TextUnit = 20.sp,
    textColor: Color = TextPrimary,
    accentColor: Color = PurpleAccent,
    isExponent: Boolean = false
) {
    val currentFontSize = if (isExponent) (baseFontSize.value * 0.75f).sp else baseFontSize

    when (node) {
        is MathNode.Number -> {
            Text(
                text = node.value,
                fontSize = currentFontSize,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Serif,
                color = textColor,
                modifier = modifier
            )
        }
        is MathNode.Variable -> {
            Text(
                text = node.name,
                fontSize = currentFontSize,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                color = MathCyan,
                modifier = modifier
            )
        }
        is MathNode.Constant -> {
            Text(
                text = node.symbol,
                fontSize = currentFontSize,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = PurplePrimary,
                modifier = modifier
            )
        }
        is MathNode.Fraction -> {
            // Real 2D vertical fraction: Numerator over fraction bar over Denominator
            Column(
                modifier = modifier.padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Numerator
                Box(modifier = Modifier.padding(bottom = 3.dp)) {
                    MathFormulaView(
                        node = node.numerator,
                        baseFontSize = (currentFontSize.value * 0.9f).sp,
                        textColor = textColor,
                        accentColor = accentColor
                    )
                }
                // Fraction horizontal line
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .height(2.dp)
                        .widthIn(min = 24.dp)
                        .background(accentColor, RoundedCornerShape(1.dp))
                )
                // Denominator
                Box(modifier = Modifier.padding(top = 3.dp)) {
                    MathFormulaView(
                        node = node.denominator,
                        baseFontSize = (currentFontSize.value * 0.9f).sp,
                        textColor = textColor,
                        accentColor = accentColor
                    )
                }
            }
        }
        is MathNode.Power -> {
            // Real superscript layout
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.Top
            ) {
                MathFormulaView(
                    node = node.base,
                    baseFontSize = currentFontSize,
                    textColor = textColor,
                    accentColor = accentColor
                )
                Box(modifier = Modifier.offset(y = (-6).dp)) {
                    MathFormulaView(
                        node = node.exponent,
                        baseFontSize = currentFontSize,
                        textColor = accentColor,
                        accentColor = accentColor,
                        isExponent = true
                    )
                }
            }
        }
        is MathNode.Root -> {
            // Radical with vinculum bar
            Row(
                modifier = modifier.padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Index (if cube root, 4th root, etc.)
                if (node.index != null) {
                    Box(modifier = Modifier.offset(y = (-6).dp)) {
                        MathFormulaView(
                            node = node.index,
                            baseFontSize = (currentFontSize.value * 0.65f).sp,
                            textColor = accentColor,
                            accentColor = accentColor
                        )
                    }
                }
                // Radical sign
                Text(
                    text = "√",
                    fontSize = (currentFontSize.value * 1.35f).sp,
                    color = accentColor,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Serif
                )
                // Overline vinculum covering the radicand
                Column(
                    modifier = Modifier
                        .offset(x = (-1).dp)
                        .padding(end = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(accentColor)
                    )
                    Box(modifier = Modifier.padding(top = 2.dp, start = 2.dp, end = 2.dp)) {
                        MathFormulaView(
                            node = node.radicand,
                            baseFontSize = currentFontSize,
                            textColor = textColor,
                            accentColor = accentColor
                        )
                    }
                }
            }
        }
        is MathNode.Addition -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MathFormulaView(node.left, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
                Text(
                    text = " + ",
                    fontSize = currentFontSize,
                    color = MathPink,
                    fontWeight = FontWeight.Bold
                )
                MathFormulaView(node.right, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
            }
        }
        is MathNode.Subtraction -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MathFormulaView(node.left, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
                Text(
                    text = " − ",
                    fontSize = currentFontSize,
                    color = MathPink,
                    fontWeight = FontWeight.Bold
                )
                MathFormulaView(node.right, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
            }
        }
        is MathNode.Multiplication -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MathFormulaView(node.left, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
                if (node.showDot) {
                    Text(
                        text = " · ",
                        fontSize = currentFontSize,
                        color = MathPink,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Spacer(modifier = Modifier.width(2.dp))
                }
                MathFormulaView(node.right, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
            }
        }
        is MathNode.Division -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MathFormulaView(node.left, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
                Text(
                    text = " ÷ ",
                    fontSize = currentFontSize,
                    color = MathPink,
                    fontWeight = FontWeight.Bold
                )
                MathFormulaView(node.right, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
            }
        }
        is MathNode.FunctionCall -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = node.name,
                    fontSize = currentFontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = PurpleSecondary
                )
                Text(text = "(", fontSize = currentFontSize, color = TextSecondary)
                MathFormulaView(node.arg, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
                Text(text = ")", fontSize = currentFontSize, color = TextSecondary)
            }
        }
        is MathNode.Limit -> {
            // Limit notation: "lim" with "x -> a" stacked below
            Row(
                modifier = modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "lim",
                        fontSize = (currentFontSize.value * 1.1f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = PurplePrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset(y = (-2).dp)
                    ) {
                        Text(
                            text = "${node.variable}→",
                            fontSize = (currentFontSize.value * 0.65f).sp,
                            color = MathCyan
                        )
                        MathFormulaView(
                            node = node.target,
                            baseFontSize = (currentFontSize.value * 0.65f).sp,
                            textColor = textColor,
                            accentColor = accentColor
                        )
                        if (node.direction.isNotEmpty()) {
                            Text(
                                text = node.direction,
                                fontSize = (currentFontSize.value * 0.5f).sp,
                                color = accentColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                MathFormulaView(node.expr, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
            }
        }
        is MathNode.Derivative -> {
            // Derivative notation: d/dx [f(x)] or d^n/dx^n
            Row(
                modifier = modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val orderStr = if (node.order > 1) "${node.order}" else ""
                    Text(
                        text = "d$orderStr",
                        fontSize = (currentFontSize.value * 0.8f).sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        color = PurplePrimary
                    )
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(1.5.dp)
                            .background(accentColor)
                    )
                    Text(
                        text = "d${node.variable}$orderStr",
                        fontSize = (currentFontSize.value * 0.8f).sp,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Serif,
                        color = PurplePrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "[", fontSize = (currentFontSize.value * 1.2f).sp, color = TextSecondary)
                MathFormulaView(node.expr, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
                Text(text = "]", fontSize = (currentFontSize.value * 1.2f).sp, color = TextSecondary)

                if (node.atPoint != null) {
                    Text(
                        text = " |",
                        fontSize = (currentFontSize.value * 1.3f).sp,
                        color = accentColor
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset(y = 4.dp)
                    ) {
                        Text(
                            text = "${node.variable}=",
                            fontSize = (currentFontSize.value * 0.7f).sp,
                            color = MathCyan
                        )
                        MathFormulaView(
                            node = node.atPoint,
                            baseFontSize = (currentFontSize.value * 0.7f).sp,
                            textColor = textColor,
                            accentColor = accentColor
                        )
                    }
                }
            }
        }
        is MathNode.Integral -> {
            // Integral sign with bounds
            Row(
                modifier = modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "∫",
                        fontSize = (currentFontSize.value * 1.9f).sp,
                        color = PurplePrimary,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal
                    )
                    if (node.lowerBound != null || node.upperBound != null) {
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.offset(x = (-4).dp)
                        ) {
                            if (node.upperBound != null) {
                                Box(modifier = Modifier.offset(y = (-4).dp)) {
                                    MathFormulaView(
                                        node = node.upperBound,
                                        baseFontSize = (currentFontSize.value * 0.65f).sp,
                                        textColor = accentColor,
                                        accentColor = accentColor
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            if (node.lowerBound != null) {
                                Box(modifier = Modifier.offset(y = 4.dp)) {
                                    MathFormulaView(
                                        node = node.lowerBound,
                                        baseFontSize = (currentFontSize.value * 0.65f).sp,
                                        textColor = accentColor,
                                        accentColor = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                MathFormulaView(node.integrand, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "d${node.variable}",
                    fontSize = currentFontSize,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Serif,
                    color = MathCyan
                )
            }
        }
        is MathNode.Sigma -> {
            // Summation with bounds above and below
            Row(
                modifier = modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Upper bound
                    Box(modifier = Modifier.offset(y = 2.dp)) {
                        MathFormulaView(
                            node = node.upper,
                            baseFontSize = (currentFontSize.value * 0.65f).sp,
                            textColor = accentColor,
                            accentColor = accentColor
                        )
                    }
                    Text(
                        text = "∑",
                        fontSize = (currentFontSize.value * 1.7f).sp,
                        color = PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                    // Lower bound (i = a)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.offset(y = (-2).dp)
                    ) {
                        Text(
                            text = "${node.indexVar}=",
                            fontSize = (currentFontSize.value * 0.65f).sp,
                            color = MathCyan
                        )
                        MathFormulaView(
                            node = node.lower,
                            baseFontSize = (currentFontSize.value * 0.65f).sp,
                            textColor = textColor,
                            accentColor = accentColor
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                MathFormulaView(node.summand, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
            }
        }
        is MathNode.Matrix -> {
            // Matrix bracketed 2D grid
            Row(
                modifier = modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[",
                    fontSize = (currentFontSize.value * (1.1f + node.rows * 0.5f)).sp,
                    color = PurplePrimary,
                    fontWeight = FontWeight.Light
                )
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    node.values.forEach { rowCells ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            rowCells.forEach { cell ->
                                Box(
                                    modifier = Modifier.widthIn(min = 28.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MathFormulaView(
                                        node = cell,
                                        baseFontSize = currentFontSize,
                                        textColor = textColor,
                                        accentColor = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
                Text(
                    text = "]",
                    fontSize = (currentFontSize.value * (1.1f + node.rows * 0.5f)).sp,
                    color = PurplePrimary,
                    fontWeight = FontWeight.Light
                )
            }
        }
        is MathNode.Group -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "(", fontSize = (currentFontSize.value * 1.1f).sp, color = TextSecondary)
                MathFormulaView(node.inner, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
                Text(text = ")", fontSize = (currentFontSize.value * 1.1f).sp, color = TextSecondary)
            }
        }
        is MathNode.Equation -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MathFormulaView(node.left, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
                Text(
                    text = " = ",
                    fontSize = currentFontSize,
                    color = MathPink,
                    fontWeight = FontWeight.Bold
                )
                MathFormulaView(node.right, baseFontSize = currentFontSize, textColor = textColor, accentColor = accentColor)
            }
        }
        is MathNode.TextNode -> {
            Text(
                text = node.text,
                fontSize = currentFontSize,
                color = textColor,
                fontFamily = FontFamily.Serif,
                modifier = modifier
            )
        }
    }
}
