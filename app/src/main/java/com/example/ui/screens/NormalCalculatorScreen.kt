package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Constraints
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

    // Long-press on the expression or result copies it to the clipboard
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val copyToClipboard: (String) -> Unit = { value ->
        clipboardManager.setText(AnnotatedString(value))
        Toast.makeText(context, "کپی شد: $value", Toast.LENGTH_SHORT).show()
    }
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
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                // Middle & Bottom: Expression, Result, and Persian Words
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState(), reverseScrolling = true),
                    horizontalAlignment = Alignment.End
                ) {
                    // Expression: font shrinks as digits increase so it always fits the card width
                    AutoSizeText(
                        text = formatExpressionWithCommas(expression),
                        color = if (displayedResult != null) secondaryText else primaryText,
                        maxFontSize = 38.sp,
                        fontWeight = FontWeight.SemiBold,
                        testTag = "expression_text",
                        onLongPress = { copyToClipboard(expression) }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Result: ONLY displayed when '=' has been pressed!
                    if (displayedResult != null) {
                        val resultText = displayedResult!!

                        AutoSizeText(
                            text = if (isResultError) resultText else "= $resultText",
                            color = if (isResultError) Color(0xFFEF4444) else MathGreen, // VIBRANT GREEN
                            maxFontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            testTag = "result_text",
                            referenceText = "= 100,000,000,000,000",
                            onLongPress = if (isResultError) null else {
                                { copyToClipboard(resultText.replace(",", "")) }
                            }
                        )

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
                                    color = if (isDark) Color.White else Color.Black,
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
            // Return to the question after "=" so it can be edited again
            Button(
                onClick = { viewModel.onReturnToExpression() },
                enabled = displayedResult != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("key_return_to_expression"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = keyOperatorBg,
                    contentColor = keyOperatorText,
                    disabledContainerColor = keyOperatorBg.copy(alpha = 0.4f),
                    disabledContentColor = keyOperatorText.copy(alpha = 0.4f)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "برگشت به روی سوال",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

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
                    containerColor = keyClearBg,
                    contentColor = keyClearText,
                    onClick = { viewModel.onBackspace() }
                )
                CalcIconButton(
                    icon = PlusMinusIcon,
                    modifier = Modifier.weight(1f),
                    containerColor = keyOperatorBg,
                    contentColor = keyOperatorText,
                    testTag = "key_toggle_sign",
                    contentDescription = "تغییر علامت",
                    onClick = { viewModel.onToggleSign() }
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

            // Row 5: ., 0, %, =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalcButton(".", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput(".") }
                CalcButton("0", Modifier.weight(1f), keyNumberBg, keyNumberText) { viewModel.onInput("0") }
                CalcButton("%", Modifier.weight(1f), keyOperatorBg, keyOperatorText) { viewModel.onInput("%") }
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
 * Monospace text whose font size shrinks (1sp at a time) until it fits the available width.
 * The smallest size it may shrink to is the size at which [referenceText] (15 digits) just
 * fits the width. Once the text would need to go below that size, the font size stays there
 * and the text wraps onto additional lines instead.
 */
@Composable
private fun AutoSizeText(
    text: String,
    color: Color,
    maxFontSize: TextUnit,
    fontWeight: FontWeight,
    testTag: String,
    referenceText: String = "100,000,000,000,000",
    onLongPress: (() -> Unit)? = null
) {
    val measurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val availableWidth = constraints.maxWidth

        // Pair of (font size, needs wrapping)
        val layoutChoice = remember(text, availableWidth, maxFontSize, referenceText, fontWeight) {
            fun widthAt(value: String, fontSizeSp: Float): Int = measurer.measure(
                text = value,
                style = TextStyle(
                    fontSize = fontSizeSp.sp,
                    fontWeight = fontWeight,
                    fontFamily = FontFamily.Monospace
                ),
                softWrap = false,
                maxLines = 1,
                constraints = Constraints()
            ).size.width

            // Minimum size: where the reference number just fits the width
            var minSize = maxFontSize.value
            while (minSize > 8f && widthAt(referenceText, minSize) > availableWidth) {
                minSize -= 1f
            }

            var size = maxFontSize.value
            var fits = false
            while (true) {
                if (widthAt(text, size) <= availableWidth) {
                    fits = true
                    break
                }
                if (size <= minSize) break
                size = maxOf(size - 1f, minSize)
            }
            size.sp to !fits
        }
        val fontSize = layoutChoice.first
        val needsWrap = layoutChoice.second

        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
            maxLines = if (needsWrap) Int.MAX_VALUE else 1,
            softWrap = needsWrap,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onLongPress != null) {
                        Modifier.pointerInput(text) {
                            detectTapGestures(onLongPress = { onLongPress() })
                        }
                    } else {
                        Modifier
                    }
                )
                .testTag(testTag)
        )
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
    testTag: String = "key_backspace",
    contentDescription: String = "حذف",
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(58.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
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

/** "+/−" sign toggle icon: plus (top-left), minus (bottom-right) and a diagonal slash. */
private val PlusMinusIcon: ImageVector = ImageVector.Builder(
    name = "PlusMinus",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round
    ) {
        // plus
        moveTo(3.5f, 7f); lineTo(10.5f, 7f)
        moveTo(7f, 3.5f); lineTo(7f, 10.5f)
        // slash
        moveTo(17f, 3.5f); lineTo(8f, 20.5f)
        // minus
        moveTo(13.5f, 18f); lineTo(20.5f, 18f)
    }
}.build()
