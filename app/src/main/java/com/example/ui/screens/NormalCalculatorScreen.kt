package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppMode
import com.example.ui.CalculatorViewModel
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.AmoledDisplaySurface
import com.example.ui.theme.AmoledKeyClearBg
import com.example.ui.theme.AmoledKeyClearText
import com.example.ui.theme.AmoledKeyEqualBg
import com.example.ui.theme.AmoledKeyEqualText
import com.example.ui.theme.AmoledKeyNumberBg
import com.example.ui.theme.AmoledKeyNumberText
import com.example.ui.theme.AmoledKeyOperatorBg
import com.example.ui.theme.AmoledKeyOperatorText
import com.example.ui.theme.AmoledPillBadgeBg
import com.example.ui.theme.AmoledPillBadgeBorder
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightBorder
import com.example.ui.theme.LightDisplaySurface
import com.example.ui.theme.LightKeyClearBg
import com.example.ui.theme.LightKeyClearText
import com.example.ui.theme.LightKeyEqualBg
import com.example.ui.theme.LightKeyEqualText
import com.example.ui.theme.LightKeyNumberBg
import com.example.ui.theme.LightKeyNumberText
import com.example.ui.theme.LightKeyOperatorBg
import com.example.ui.theme.LightKeyOperatorText
import com.example.ui.theme.LightPillBadgeBg
import com.example.ui.theme.LightPillBadgeBorder
import com.example.ui.theme.LightTextMuted
import com.example.ui.theme.LightTextPrimary
import com.example.ui.theme.LightTextSecondary
import com.example.ui.theme.MathGreen
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class KeyType {
    CLEAR, BACKSPACE, OPERATOR, NUMBER, PARENTHESES, EQUAL
}

data class KeyItem(val symbol: String, val type: KeyType)

@Composable
fun NormalCalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val expression by viewModel.expression.collectAsState()
    val previewResult by viewModel.previewResult.collectAsState()
    val previewWords by viewModel.previewWords.collectAsState()
    val themeMode by viewModel.appThemeMode.collectAsState()
    val isDark = themeMode == AppThemeMode.AMOLED_DARK

    // Theme adaptive colors
    val screenBg = if (isDark) AmoledBackground else LightBackground
    val cardBg = if (isDark) AmoledDisplaySurface else LightDisplaySurface
    val cardBorder = if (isDark) Color(0xFF261947) else LightBorder
    val pillBg = if (isDark) AmoledPillBadgeBg else LightPillBadgeBg
    val pillBorder = if (isDark) AmoledPillBadgeBorder else LightPillBadgeBorder
    val pillText = if (isDark) TextPrimary else LightTextPrimary
    val placeholderColor = if (isDark) TextMuted else LightTextMuted
    val expressionTextColor = if (isDark) TextSecondary else LightTextSecondary
    val wordsTextColor = if (isDark) PurpleSecondary else LightKeyOperatorText

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(screenBg)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // DISPLAY AREA CARD (Matching reference image)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 12.dp)
                    .testTag("calculator_display_card"),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                shape = RoundedCornerShape(26.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top-left Pill Badge: "ماشین‌حساب معمولی"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, pillBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setAppMode(AppMode.MATH_STUDIO)
                                }
                                .testTag("pill_badge_calculator_mode"),
                            color = pillBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = if (isDark) PurpleSecondary else PurplePrimary,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Text(
                                    text = "ماشین‌حساب معمولی",
                                    color = pillText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Display Value & Persian Words (bottom-right aligned)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (expression.isEmpty()) {
                            // Default 0 placeholder
                            Text(
                                text = "0",
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.SansSerif,
                                color = placeholderColor,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .padding(end = 8.dp, bottom = 4.dp)
                                    .testTag("display_zero_placeholder")
                            )
                        } else {
                            // Upper row: Expression text (scrolling)
                            val exprScrollState = rememberScrollState()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(exprScrollState, reverseScrolling = true),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = expression,
                                    fontSize = if (expression.length > 14) 22.sp else 28.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = FontFamily.SansSerif,
                                    color = expressionTextColor,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    modifier = Modifier.testTag("expression_text")
                                )
                            }

                            // Middle row: Live Auto-Calculated Result in VIBRANT GREEN (رنگ جواب رو سبز کن)
                            if (previewResult.isNotEmpty()) {
                                val resScrollState = rememberScrollState()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 2.dp)
                                        .horizontalScroll(resScrollState, reverseScrolling = true),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = previewResult,
                                        fontSize = if (previewResult.length > 12) 34.sp else 46.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.SansSerif,
                                        color = MathGreen, // Exact green color requested
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        modifier = Modifier.testTag("preview_result_text")
                                    )
                                }
                            }

                            // Bottom row: Persian Number Words (زیر جواب حروف همون عدد نوشته بشه)
                            if (previewWords.isNotEmpty()) {
                                val wordsScrollState = rememberScrollState()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .horizontalScroll(wordsScrollState, reverseScrolling = true),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = previewWords,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = wordsTextColor,
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        modifier = Modifier.testTag("persian_number_words_text")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // KEYPAD AREA (5 Rows x 4 Columns)
            val keypadGrid: List<List<KeyItem>> = listOf(
                listOf(
                    KeyItem("C", KeyType.CLEAR),
                    KeyItem("⌫", KeyType.BACKSPACE),
                    KeyItem("%", KeyType.OPERATOR),
                    KeyItem("÷", KeyType.OPERATOR)
                ),
                listOf(
                    KeyItem("7", KeyType.NUMBER),
                    KeyItem("8", KeyType.NUMBER),
                    KeyItem("9", KeyType.NUMBER),
                    KeyItem("×", KeyType.OPERATOR)
                ),
                listOf(
                    KeyItem("4", KeyType.NUMBER),
                    KeyItem("5", KeyType.NUMBER),
                    KeyItem("6", KeyType.NUMBER),
                    KeyItem("−", KeyType.OPERATOR)
                ),
                listOf(
                    KeyItem("1", KeyType.NUMBER),
                    KeyItem("2", KeyType.NUMBER),
                    KeyItem("3", KeyType.NUMBER),
                    KeyItem("+", KeyType.OPERATOR)
                ),
                listOf(
                    KeyItem("()", KeyType.PARENTHESES),
                    KeyItem("0", KeyType.NUMBER),
                    KeyItem(".", KeyType.NUMBER),
                    KeyItem("=", KeyType.EQUAL)
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                keypadGrid.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            ExactKeyButton(
                                item = item,
                                isDark = isDark,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .testTag("key_${item.symbol}"),
                                onClick = {
                                    viewModel.onKeypadInput(item.symbol)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExactKeyButton(
    item: KeyItem,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isDark) {
        when (item.type) {
            KeyType.CLEAR, KeyType.BACKSPACE -> AmoledKeyClearBg
            KeyType.OPERATOR -> AmoledKeyOperatorBg
            KeyType.NUMBER, KeyType.PARENTHESES -> AmoledKeyNumberBg
            KeyType.EQUAL -> AmoledKeyEqualBg
        }
    } else {
        when (item.type) {
            KeyType.CLEAR, KeyType.BACKSPACE -> LightKeyClearBg
            KeyType.OPERATOR -> LightKeyOperatorBg
            KeyType.NUMBER, KeyType.PARENTHESES -> LightKeyNumberBg
            KeyType.EQUAL -> LightKeyEqualBg
        }
    }

    val textColor = if (isDark) {
        when (item.type) {
            KeyType.CLEAR, KeyType.BACKSPACE -> AmoledKeyClearText
            KeyType.OPERATOR -> AmoledKeyOperatorText
            KeyType.NUMBER -> AmoledKeyNumberText
            KeyType.PARENTHESES -> AmoledKeyOperatorText
            KeyType.EQUAL -> AmoledKeyEqualText
        }
    } else {
        when (item.type) {
            KeyType.CLEAR, KeyType.BACKSPACE -> LightKeyClearText
            KeyType.OPERATOR -> LightKeyOperatorText
            KeyType.NUMBER -> LightKeyNumberText
            KeyType.PARENTHESES -> LightKeyOperatorText
            KeyType.EQUAL -> LightKeyEqualText
        }
    }

    val borderModifier = if (!isDark && (item.type == KeyType.NUMBER || item.type == KeyType.PARENTHESES)) {
        Modifier.border(1.dp, LightBorder, RoundedCornerShape(20.dp))
    } else {
        Modifier
    }

    val fontSize = when (item.symbol) {
        "C" -> 22.sp
        "%", "()" -> 20.sp
        "÷", "×", "+", "−" -> 26.sp
        "." -> 28.sp
        "=" -> 28.sp
        else -> 24.sp
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .then(borderModifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (item.type == KeyType.BACKSPACE) {
            Icon(
                imageVector = Icons.Default.Backspace,
                contentDescription = "پاک کردن",
                tint = textColor,
                modifier = Modifier.padding(2.dp)
            )
        } else {
            Text(
                text = item.symbol,
                fontSize = fontSize,
                fontWeight = if (item.type == KeyType.EQUAL || item.type == KeyType.CLEAR) FontWeight.Bold else FontWeight.SemiBold,
                fontFamily = FontFamily.SansSerif,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
