package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.calculator.ScientificEngine
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.*

@Composable
fun NormalCalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsState()
    val displayedResult by viewModel.displayedResult.collectAsState()
    val resultWordsFa by viewModel.resultWordsFa.collectAsState()
    val isResultError by viewModel.isResultError.collectAsState()
    val angleMode by viewModel.angleMode.collectAsState()
    val isScientificExpanded by viewModel.isScientificExpanded.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val isDark = themeMode == AppThemeMode.AMOLED_DARK

    // Theme adaptive colors
    val displayBg = if (isDark) AmoledDisplaySurface else LightDisplaySurface
    val displayBorder = if (isDark) BlackBorder else LightBorder
    val primaryText = if (isDark) TextPrimary else LightTextPrimary
    val secondaryText = if (isDark) TextSecondary else LightTextSecondary
    val keyNumberBg = if (isDark) AmoledKeyNumberBg else LightKeyNumberBg
    val keyNumberText = if (isDark) AmoledKeyNumberText else LightKeyNumberText
    val keyOperatorBg = if (isDark) AmoledKeyOperatorBg else LightKeyOperatorBg
    val keyOperatorText = if (isDark) AmoledKeyOperatorText else LightKeyOperatorText
    val keyClearBg = if (isDark) AmoledKeyClearBg else LightKeyClearBg
    val keyClearText = if (isDark) AmoledKeyClearText else LightKeyClearText
    val keyEqualBg = if (isDark) AmoledKeyEqualBg else LightKeyEqualBg

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- DISPLAY AREA ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = displayBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, displayBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                // Top status badges (DEG/RAD, 36-digits, Scientific toggle)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // DEG / RAD Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) AmoledPillBadgeBg else LightPillBadgeBg)
                                .clickable { viewModel.toggleAngleMode() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (angleMode == ScientificEngine.AngleMode.DEGREE) "DEG" else "RAD",
                                color = PurplePrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // 36 Digits badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) AmoledPillBadgeBg else LightPillBadgeBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "دقت ۳۶ رقم",
                                color = PurpleSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Scientific toggle button
                    IconButton(
                        onClick = { viewModel.toggleScientificExpanded() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isScientificExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "توابع علمی",
                            tint = PurplePrimary
                        )
                    }
                }

                // Middle & Bottom: Expression, Result, and Persian Words
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    // Expression: Starts large, automatically shrinks as digits increase so it never overflows
                    val exprFontSize = calculateExpressionFontSize(expression.length)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState(), reverseScrolling = true),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = formatExpressionWithCommas(expression),
                            color = if (displayedResult != null) secondaryText else primaryText,
                            fontSize = exprFontSize,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier.testTag("expression_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Result: ONLY displayed when '=' has been pressed!
                    if (displayedResult != null) {
                        val resultText = displayedResult!!
                        val resultFontSize = calculateResultFontSize(resultText.length)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState(), reverseScrolling = true),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = if (isResultError) resultText else "= $resultText",
                                color = if (isResultError) Color(0xFFEF4444) else MathGreen, // VIBRANT GREEN
                                fontSize = resultFontSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                modifier = Modifier.testTag("result_text")
                            )
                        }

                        // Persian Words beneath the result (Made LARGER per request!)
                        if (resultWordsFa != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) AmoledPillBadgeBg else LightPillBadgeBg
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isDark) AmoledPillBadgeBorder else LightPillBadgeBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp)
                            ) {
                                Text(
                                    text = resultWordsFa!!,
                                    color = if (isDark) PurpleAccent else PurpleTertiary,
                                    fontSize = 19.sp, // LARGER PER REQUEST
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 27.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .testTag("persian_words_text")
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- SCIENTIFIC EXPANDABLE ROW ---
        if (isScientificExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcMiniButton("sin", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("sin(") }
                    CalcMiniButton("cos", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("cos(") }
                    CalcMiniButton("tan", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("tan(") }
                    CalcMiniButton("ln", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("ln(") }
                    CalcMiniButton("log", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("log(") }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CalcMiniButton("√", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("sqrt(") }
                    CalcMiniButton("xʸ", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("^") }
                    CalcMiniButton("x!", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("!") }
                    CalcMiniButton("π", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("π") }
                    CalcMiniButton("e", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("e") }
                }
            }
        }

        // --- KEYPAD AREA ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: AC, Backspace, ( ), ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton(
                    text = "AC",
                    modifier = Modifier.weight(1f),
                    containerColor = keyClearBg,
                    contentColor = keyClearText,
                    onClick = { viewModel.onClear() }
                )
                CalcIconButton(
                    icon = Icons.Default.Backspace,
                    modifier = Modifier.weight(1f),
                    containerColor = keyOperatorBg,
                    contentColor = keyOperatorText,
                    onClick = { viewModel.onBackspace() }
                )
                CalcButton(
                    text = "( )",
                    modifier = Modifier.weight(1f),
                    containerColor = keyOperatorBg,
                    contentColor = keyOperatorText,
                    onClick = {
                        val expr = expression
                        val opens = expr.count { it == '(' }
                        val closes = expr.count { it == ')' }
                        if (opens > closes && expr.lastOrNull() !in listOf('+', '−', '-', '×', '*', '÷', '/', '(')) {
                            viewModel.onInput(")")
                        } else {
                            viewModel.onInput("(")
                        }
                    }
                )
                CalcButton(
                    text = "÷",
                    modifier = Modifier.weight(1f),
                    containerColor = keyOperatorBg,
                    contentColor = keyOperatorText,
                    onClick = { viewModel.onInput("÷") }
                )
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton("7", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput("7") }
                CalcButton("8", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput("8") }
                CalcButton("9", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput("9") }
                CalcButton("×", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("×") }
            }

            // Row 3: 4, 5, 6, −
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton("4", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput("4") }
                CalcButton("5", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput("5") }
                CalcButton("6", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput("6") }
                CalcButton("−", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("−") }
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton("1", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput("1") }
                CalcButton("2", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput("2") }
                CalcButton("3", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput("3") }
                CalcButton("+", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("+") }
            }

            // Row 5: %, 0, ., =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton("%", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("%") }
                CalcButton("0", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput("0") }
                CalcButton(".", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput(".") }
                CalcButton(
                    text = "=",
                    modifier = Modifier.weight(1f),
                    containerColor = keyEqualBg,
                    contentColor = Color.White,
                    onClick = { viewModel.onCalculate() }
                )
            }
        }
    }
}

/**
 * Calculates adaptive font size for expression:
 * Starts large at 38sp and shrinks gradually down to 18sp as digits increase
 */
private fun calculateExpressionFontSize(length: Int): TextUnit {
    return when {
        length <= 10 -> 38.sp
        length <= 16 -> 32.sp
        length <= 22 -> 26.sp
        length <= 28 -> 22.sp
        length <= 36 -> 19.sp
        else -> 17.sp
    }
}

/**
 * Calculates adaptive font size for result:
 * Starts at 44sp and shrinks down to 18sp so it never overflows
 */
private fun calculateResultFontSize(length: Int): TextUnit {
    return when {
        length <= 10 -> 44.sp
        length <= 16 -> 36.sp
        length <= 22 -> 28.sp
        length <= 28 -> 22.sp
        length <= 36 -> 19.sp
        else -> 17.sp
    }
}

/**
 * Formats multi-digit operands inside expressions with commas
 */
private fun formatExpressionWithCommas(expr: String): String {
    val sb = StringBuilder()
    var i = 0
    while (i < expr.length) {
        val c = expr[i]
        if (c.isDigit()) {
            val numSb = StringBuilder()
            while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                numSb.append(expr[i])
                i++
            }
            val numStr = numSb.toString()
            val parts = numStr.split(".")
            val intPart = parts[0]
            val decPart = if (parts.size > 1) parts[1] else null

            // insert commas
            val intLen = intPart.length
            val formattedInt = StringBuilder()
            for (j in 0 until intLen) {
                if (j > 0 && (intLen - j) % 3 == 0) {
                    formattedInt.append(",")
                }
                formattedInt.append(intPart[j])
            }
            sb.append(formattedInt)
            if (decPart != null) {
                sb.append(".$decPart")
            }
        } else {
            sb.append(c)
            i++
        }
    }
    return sb.toString()
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(58.dp)
            .testTag("key_$text"),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CalcIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(58.dp)
            .testTag("key_backspace"),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "حذف",
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun CalcMiniButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(38.dp)
            .testTag("key_mini_$text"),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
