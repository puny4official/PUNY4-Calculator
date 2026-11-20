package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
    val previousLine by viewModel.previousLine.collectAsState()
    val resultWordsFa by viewModel.resultWordsFa.collectAsState()
    val isResultError by viewModel.isResultError.collectAsState()
    val angleMode by viewModel.angleMode.collectAsState()
    val isScientificExpanded by viewModel.isScientificExpanded.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val isDark = themeMode == AppThemeMode.AMOLED_DARK

    // Theme adaptive colors
    val displayBg = if (isDark) Color(0xFF020307) else LightDisplaySurface
    val displayBorder = if (isDark) Color(0xFF373641) else LightBorder
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
            shape = RoundedCornerShape(if (isDark) 18.dp else 24.dp),
            colors = CardDefaults.cardColors(containerColor = displayBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, displayBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                val hasResult = displayedResult != null

                // Earlier calculation we continued from, faded at the top (e.g. "1+2= 3")
                if (!hasResult && previousLine != null) {
                    AutoSizeText(
                        text = formatExpressionWithCommas(previousLine!!),
                        color = secondaryText.copy(alpha = 0.55f),
                        maxFontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        testTag = "previous_line_text"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Question: sits at the bottom while typing. After "=" it moves to the top
                // of the card and stays visible, faded, while the answer is shown below.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = if (hasResult) Alignment.TopEnd else Alignment.BottomEnd
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(
                                rememberScrollState(),
                                reverseScrolling = !hasResult
                            ),
                        horizontalAlignment = Alignment.End
                    ) {
                        // Font shrinks as digits increase so it always fits the card width
                        AutoSizeText(
                            text = formatExpressionWithCommas(expression),
                            color = if (hasResult) secondaryText.copy(alpha = 0.55f) else primaryText,
                            maxFontSize = 38.sp,
                            fontWeight = FontWeight.SemiBold,
                            testTag = "expression_text",
                            onLongPress = { copyToClipboard(expression) }
                        )
                    }
                }

                // Small live preview of the answer while typing, e.g. "= 2"
                if (!hasResult) {
                    val preview = remember(expression, angleMode) {
                        val hasOp = expression.drop(1).any { it in "+-−×÷*/^%" }
                        if (!hasOp) null else (
                            ScientificEngine.evaluate(expression, angleMode)
                                as? ScientificEngine.EvaluationResult.Success
                            )?.formatted
                    }
                    if (preview != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        AutoSizeText(
                            text = "= $preview",
                            color = secondaryText,
                            maxFontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            testTag = "preview_text",
                            referenceText = "= 100,000,000,000,000"
                        )
                    }
                }

                // Answer (only after "="), always at the bottom of the card
                if (hasResult) {
                    val resultText = displayedResult!!

                    Spacer(modifier = Modifier.height(8.dp))

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

                    // Persian words beneath the answer
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
                                fontSize = 19.sp,
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
            // Return to the question after "=" so it can be edited again.
            // The whole button (glass pill, glow, arrow and text) is the supplied image.
            val canReturnToExpression = displayedResult != null
            Image(
                painter = painterResource(
                    id = if (isDark) R.drawable.btn_return_to_question_dark
                    else R.drawable.btn_return_to_question_light
                ),
                contentDescription = "برگشت به روی سوال",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (canReturnToExpression) 1f else 0.4f)
                    .clickable(
                        enabled = canReturnToExpression,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { viewModel.onReturnToExpression() }
                    .testTag("key_return_to_expression")
            )

            // "C" clears the question only. Once it is cleared and an earlier line is still
            // kept at the top, the key turns into "AC", which clears everything.
            val isAllClear = expression == "0" && displayedResult == null && previousLine != null
            GlassKeypad(viewModel = viewModel, isDark = isDark, isAllClear = isAllClear)
        }
    }
}

private enum class KeyKind { NUMBER, OPERATOR, CLEAR, EQUAL }

/** One key of the keypad. Either [label] or [icon] is drawn on the button. */
private class GlassKey(
    val tag: String,
    val description: String,
    val kind: KeyKind,
    val label: String? = null,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    val action: (CalculatorViewModel) -> Unit
)

private fun glassInput(symbol: String, kind: KeyKind, description: String = symbol) =
    GlassKey("key_$symbol", description, kind, label = symbol) { it.onInput(symbol) }

private val glassKeyRows: List<List<GlassKey>> = listOf(
    listOf(
        GlassKey("key_AC", "پاک کردن", KeyKind.CLEAR, label = "C") { it.onClear() },
        GlassKey("key_backspace", "حذف", KeyKind.CLEAR, icon = Icons.Default.Backspace) { it.onBackspace() },
        GlassKey("key_toggle_sign", "تغییر علامت", KeyKind.OPERATOR, label = "+/−") { it.onToggleSign() },
        glassInput("÷", KeyKind.OPERATOR, "تقسیم")
    ),
    listOf(
        glassInput("7", KeyKind.NUMBER), glassInput("8", KeyKind.NUMBER),
        glassInput("9", KeyKind.NUMBER), glassInput("×", KeyKind.OPERATOR, "ضرب")
    ),
    listOf(
        glassInput("4", KeyKind.NUMBER), glassInput("5", KeyKind.NUMBER),
        glassInput("6", KeyKind.NUMBER), glassInput("−", KeyKind.OPERATOR, "تفریق")
    ),
    listOf(
        glassInput("1", KeyKind.NUMBER), glassInput("2", KeyKind.NUMBER),
        glassInput("3", KeyKind.NUMBER), glassInput("+", KeyKind.OPERATOR, "جمع")
    ),
    listOf(
        glassInput(".", KeyKind.NUMBER, "ممیز"),
        glassInput("0", KeyKind.NUMBER),
        glassInput("%", KeyKind.OPERATOR, "درصد"),
        GlassKey("key_=", "مساوی", KeyKind.EQUAL, label = "=") { it.onCalculate() }
    )
)

/** Width / height of every key. Same in light and dark mode. */
private const val KEY_ASPECT_RATIO = 1.66f

/** Colours of a glass key: three fill stops (top to bottom), border, text and glow. */
private class GlassKeyColors(
    val fill: List<Color>,
    val border: Color,
    val text: Color,
    val glow: Color?
)

private fun glassKeyColors(kind: KeyKind, isDark: Boolean): GlassKeyColors = if (isDark) {
    when (kind) {
        KeyKind.NUMBER -> GlassKeyColors(
            listOf(Color(0xFF3A3548), Color(0xFF1A1724), Color(0xFF0E0C16)),
            Color(0xFFB9B3CC).copy(alpha = 0.40f), Color(0xFFFFFFFF), null
        )
        KeyKind.OPERATOR -> GlassKeyColors(
            listOf(Color(0xFF3E5A4C), Color(0xFF1C2B24), Color(0xFF101A15)),
            Color(0xFF9CD8B4).copy(alpha = 0.45f), Color(0xFF8A1FFF), null
        )
        KeyKind.CLEAR -> GlassKeyColors(
            listOf(Color(0xFF5A1230), Color(0xFF2A0A18), Color(0xFF140610)),
            Color(0xFFFF7BA3).copy(alpha = 0.40f), Color(0xFFE8113F), null
        )
        KeyKind.EQUAL -> GlassKeyColors(
            listOf(Color(0xFF3DBE5E), Color(0xFF1E8E3C), Color(0xFF0E6B28)),
            Color(0xFF7BE39A).copy(alpha = 0.70f), Color(0xFFFFFFFF), Color(0xFF22C55E)
        )
    }
} else {
    when (kind) {
        KeyKind.NUMBER -> GlassKeyColors(
            listOf(Color(0xFFFFFFFF), Color(0xFFFDFBFF), Color(0xFFF4F2FA)),
            Color(0xFFE6E2EF), LightKeyNumberText, null
        )
        KeyKind.OPERATOR -> GlassKeyColors(
            listOf(Color(0xFFFAFFFC), Color(0xFFEFF8F2), Color(0xFFE8F2EC)),
            Color(0xFFDDEBE2), LightKeyOperatorText, null
        )
        KeyKind.CLEAR -> GlassKeyColors(
            listOf(Color(0xFFFFFAFC), Color(0xFFFFF0F5), Color(0xFFF8E6ED)),
            Color(0xFFF2DCE5), LightKeyClearText, null
        )
        KeyKind.EQUAL -> GlassKeyColors(
            listOf(Color(0xFF35B85F), Color(0xFF16A34A), Color(0xFF0D873C)),
            Color(0xFF79D494), Color(0xFFFFFFFF), Color(0xFF188F43)
        )
    }
}

/** A single glass-style pill key, drawn in code (no pictures). */
@Composable
private fun GlassKeyButton(
    key: GlassKey,
    label: String?,
    description: String,
    isDark: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val colors = glassKeyColors(key.kind, isDark)
    val shape = RoundedCornerShape(percent = 50)
    val shadowTint = colors.glow ?: (if (isDark) Color(0xFF000000) else Color(0xFF6A6480))
    Box(
        modifier = modifier
            .aspectRatio(KEY_ASPECT_RATIO)
            .shadow(
                elevation = if (colors.glow != null) 10.dp else if (isDark) 0.dp else 6.dp,
                shape = shape,
                clip = false,
                ambientColor = shadowTint.copy(alpha = 0.45f),
                spotColor = shadowTint.copy(alpha = 0.45f)
            )
            .clip(shape)
            // translucent tinted body of the glass
            .background(Brush.verticalGradient(colors.fill.map { it.copy(alpha = if (isDark) 0.78f else 0.88f) }))
            .drawBehind {
                val w = size.width
                val h = size.height
                val px = 1.dp.toPx()
                val tint = colors.fill[0]

                // light collected along the bottom edge (glass refracts light to the lower rim)
                drawRect(
                    brush = Brush.verticalGradient(
                        0.55f to Color.Transparent,
                        1f to tint.copy(alpha = 0.55f).compositeOver(Color.White.copy(alpha = if (isDark) 0.10f else 0.0f))
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
                    topLeft = Offset(h * 0.16f, 3 * px),
                    size = androidx.compose.ui.geometry.Size(w - h * 0.32f, h * 0.46f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(h * 0.23f)
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
                // faint second spot, bottom-right
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
                    size = androidx.compose.ui.geometry.Size(w - 5 * px, h - 5 * px),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2 - 2.5f * px),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = px)
                )
            }
            // outer rim: bright at the top-left and bottom-right, dim on the sides, like a glass edge
            .border(
                1.5.dp,
                Brush.linearGradient(
                    0f to Color.White.copy(alpha = if (isDark) 0.85f else 1f),
                    0.35f to colors.border.copy(alpha = colors.border.alpha * 0.5f),
                    0.65f to colors.border.copy(alpha = colors.border.alpha * 0.5f),
                    1f to Color.White.copy(alpha = if (isDark) 0.65f else 0.9f)
                ),
                shape
            )
            .clickable(onClickLabel = description, onClick = onClick)
            .semantics { contentDescription = description }
            .testTag(key.tag),
        contentAlignment = Alignment.Center
    ) {
        if (key.icon != null) {
            Icon(
                imageVector = key.icon,
                contentDescription = null,
                tint = colors.text,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Text(
                text = label ?: "",
                color = colors.text,
                fontSize = if ((label?.length ?: 0) > 1) 22.sp else 28.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/**
 * The keypad, drawn in code. Every key has the same size in light and dark mode;
 * only the colours change with the theme.
 */
@Composable
private fun GlassKeypad(viewModel: CalculatorViewModel, isDark: Boolean, isAllClear: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        glassKeyRows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEachIndexed { columnIndex, key ->
                    val isClearKey = rowIndex == 0 && columnIndex == 0
                    // "C" clears the question; once cleared it turns into "AC" (clears everything)
                    val label = if (isClearKey && isAllClear) "AC" else key.label
                    val description = if (isClearKey && isAllClear) "پاک کردن همه" else key.description
                    GlassKeyButton(
                        key = key,
                        label = label,
                        description = description,
                        isDark = isDark,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (isClearKey && isAllClear) viewModel.onClearAll() else key.action(viewModel)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Monospace text whose font size shrinks (1sp at a time) until it fits the available width.
 * The smallest size it may shrink to is the size at which [referenceText] (15 digits) just
 * fits the width. Once the text would need to go below that size, the font size stays there
 * and the text continues on the next lines: every line is filled completely and the rest of
 * the number goes below it, left-aligned, in reading order.
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

        // Same style for measuring and drawing. It is explicit on purpose: the inherited
        // Material style adds letter spacing and a fixed 24sp line height, which made
        // wrapped lines overlap and the measured width differ from the drawn width.
        fun styleAt(fontSizeSp: Float) = TextStyle(
            fontSize = fontSizeSp.sp,
            fontWeight = fontWeight,
            fontFamily = SevenSegmentFont,
            letterSpacing = 0.sp,
            lineHeight = (fontSizeSp * 1.2f).sp,
            textDirection = TextDirection.Ltr
        )

        fun widthAt(value: String, fontSizeSp: Float): Int = measurer.measure(
            text = value,
            style = styleAt(fontSizeSp),
            softWrap = false,
            maxLines = 1,
            constraints = Constraints()
        ).size.width

        // Triple of (font size in sp, needs wrapping, characters that fit on one line)
        val layoutChoice = remember(text, availableWidth, maxFontSize, referenceText, fontWeight) {
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

            val charsPerLine = if (fits) {
                0
            } else {
                // Monospace: every character has the same width
                val tenCharsWidth = widthAt("0".repeat(10), size).coerceAtLeast(1)
                maxOf(1, (availableWidth * 10f / tenCharsWidth).toInt())
            }
            Triple(size, !fits, charsPerLine)
        }
        val fontSizeSp = layoutChoice.first
        val needsWrap = layoutChoice.second
        val charsPerLine = layoutChoice.third

        // Numbers and expressions are cut into full lines; Persian messages wrap by words
        val hasPersian = text.any { it.code in 0x0600..0x06FF }
        val chunked = needsWrap && !hasPersian
        val shownText = if (chunked) text.chunked(charsPerLine).joinToString("\n") else text

        Text(
            text = shownText,
            color = color,
            style = styleAt(fontSizeSp),
            textAlign = if (needsWrap) TextAlign.Start else TextAlign.End,
            maxLines = if (needsWrap) Int.MAX_VALUE else 1,
            softWrap = needsWrap && !chunked,
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

private fun calculatorButtonFace(
    modifier: Modifier,
    containerColor: Color,
    shape: RoundedCornerShape
): Modifier {
    val isEqual = containerColor == LightKeyEqualBg || containerColor == AmoledKeyEqualBg
    val colors = when (containerColor) {
        LightKeyNumberBg -> listOf(Color(0xFFFFFFFF), Color(0xFFFDFBFF), Color(0xFFF4F2FA))
        LightKeyOperatorBg -> listOf(Color(0xFFFAFFFC), Color(0xFFEFF8F2), Color(0xFFE8F2EC))
        LightKeyClearBg -> listOf(Color(0xFFFFFAFC), Color(0xFFFFF0F5), Color(0xFFF8E6ED))
        LightKeyEqualBg -> listOf(Color(0xFF35B85F), Color(0xFF16A34A), Color(0xFF0D873C))
        else -> listOf(containerColor, containerColor, containerColor)
    }
    val outline = when (containerColor) {
        LightKeyNumberBg -> Color(0xFFE6E2EF)
        LightKeyOperatorBg -> Color(0xFFDDEBE2)
        LightKeyClearBg -> Color(0xFFF2DCE5)
        LightKeyEqualBg -> Color(0xFF79D494)
        else -> Color.White.copy(alpha = 0.16f)
    }
    val shadowColor = when {
        containerColor == LightKeyEqualBg -> Color(0xFF188F43).copy(alpha = 0.46f)
        containerColor == AmoledKeyEqualBg -> Color(0xFFA855F7).copy(alpha = 0.32f)
        else -> Color(0xFF6A6480).copy(alpha = 0.16f)
    }

    return modifier
        .shadow(
            elevation = if (isEqual) 8.dp else 5.dp,
            shape = shape,
            clip = false,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .clip(shape)
        .background(Brush.verticalGradient(colors))
        .border(1.dp, outline, shape)
}

@Composable
fun CalcMiniButton(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Button(
        onClick = onClick,
        modifier = calculatorButtonFace(
            modifier = modifier.height(38.dp).testTag("key_mini_$text"),
            containerColor = containerColor,
            shape = shape
        ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
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


/** Digital (seven-segment) font used for the numbers on the display. */
private val SevenSegmentFont = FontFamily(Font(R.font.seven_segment))
