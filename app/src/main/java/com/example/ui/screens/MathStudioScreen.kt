package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.model.MathNode
import com.example.math.model.SolutionStep
import com.example.ui.CalculatorViewModel
import com.example.ui.components.MathFormulaView
import com.example.ui.theme.*

@Composable
fun MathStudioScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val activeCategory by viewModel.studioCategory.collectAsState()
    val solution by viewModel.studioSolution.collectAsState()
    val studioError by viewModel.studioError.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val isDark = themeMode == AppThemeMode.AMOLED_DARK

    val cardBg = if (isDark) AmoledDisplaySurface else LightDisplaySurface
    val cardBorder = if (isDark) BlackBorder else LightBorder
    val primaryText = if (isDark) TextPrimary else LightTextPrimary
    val secondaryText = if (isDark) TextSecondary else LightTextSecondary

    val categories = listOf(
        "کسر و توان",
        "معادلات و دستگاه",
        "مشتق",
        "انتگرال",
        "حد و رفع ابهام",
        "سیگما و سری",
        "ماتریس ۲×۲"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- HORIZONTAL CATEGORY CHIPS ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == activeCategory
                    val chipBg = if (isSelected) PurplePrimary else (if (isDark) AmoledPillBadgeBg else LightPillBadgeBg)
                    val chipText = if (isSelected) Color.White else (if (isDark) PurpleAccent else PurpleTertiary)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(chipBg)
                            .border(
                                1.dp,
                                if (isSelected) PurplePrimary else (if (isDark) AmoledPillBadgeBorder else LightPillBadgeBorder),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.setStudioCategory(cat) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            color = chipText,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // --- DYNAMIC FORMULA CONFIGURATION CARD ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تنظیم پارامترهای $activeCategory",
                            color = primaryText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = null,
                            tint = PurplePrimary
                        )
                    }

                    // Render Inputs Based on Category
                    when (activeCategory) {
                        "کسر و توان" -> FractionInputSection(viewModel, isDark)
                        "معادلات و دستگاه" -> EquationInputSection(viewModel, isDark)
                        "مشتق" -> DerivativeInputSection(viewModel, isDark)
                        "انتگرال" -> IntegralInputSection(viewModel, isDark)
                        "حد و رفع ابهام" -> LimitInputSection(viewModel, isDark)
                        "سیگما و سری" -> SigmaInputSection(viewModel, isDark)
                        "ماتریس ۲×۲" -> MatrixInputSection(viewModel, isDark)
                    }
                }
            }
        }

        // --- ERROR DISPLAY IF ANY ---
        if (studioError != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF45152F)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = studioError!!,
                        color = Color(0xFFFDA4AF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // --- SOLUTION & STEP-BY-STEP DISPLAY ---
        if (solution != null) {
            // Problem Overview Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PurplePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = solution!!.problemTitleFa,
                                color = PurplePrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (solution!!.isExact) MathGreen.copy(alpha = 0.2f) else MathAmber.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (solution!!.isExact) "پاسخ دقیق تحلیلی" else "پاسخ تقریبی عددی",
                                    color = if (solution!!.isExact) MathGreen else MathAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Formula Render
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) AmoledBackground else LightBackground)
                                .padding(12.dp)
                        ) {
                            MathFormulaView(
                                node = solution!!.initialFormula,
                                fontSize = 22.sp
                            )
                        }

                        Divider(color = cardBorder)

                        // Final Result Highlight
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "نتیجه نهایی:",
                                color = secondaryText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            MathFormulaView(
                                node = solution!!.exactResult,
                                fontSize = 20.sp,
                                textColor = MathGreen
                            )
                        }
                    }
                }
            }

            // Steps Header
            item {
                Text(
                    text = "مراحل حل گام‌به‌گام (به زبان فارسی):",
                    color = primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Step items
            items(solution!!.steps) { step ->
                StepCard(step = step, isDark = isDark)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StepCard(step: SolutionStep, isDark: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) AmoledDisplaySurface else LightDisplaySurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) BlackBorder else LightBorder
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PurplePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${step.stepNumber}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = step.titleFa,
                        color = if (isDark) TextPrimary else LightTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) AmoledPillBadgeBg else LightPillBadgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = step.tag,
                        color = PurpleSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Formula Transformation View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) AmoledBackground else LightBackground)
                    .padding(10.dp)
            ) {
                MathFormulaView(
                    node = step.formula,
                    fontSize = 18.sp
                )
            }

            // Persian Explanation
            Text(
                text = step.explanationFa,
                color = if (isDark) TextSecondary else LightTextSecondary,
                fontSize = 13.5.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --- SUB-SECTIONS FOR MATH INPUTS ---

@Composable
fun FractionInputSection(viewModel: CalculatorViewModel, isDark: Boolean) {
    var num1 by remember { mutableStateOf("3") }
    var den1 by remember { mutableStateOf("4") }
    var op by remember { mutableStateOf("+") }
    var num2 by remember { mutableStateOf("2") }
    var den2 by remember { mutableStateOf("5") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fraction 1
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
                OutlinedTextField(
                    value = num1,
                    onValueChange = { num1 = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                )
                Divider(modifier = Modifier.padding(vertical = 4.dp), color = PurplePrimary, thickness = 2.dp)
                OutlinedTextField(
                    value = den1,
                    onValueChange = { den1 = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                )
            }

            // Operation selector
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("+", "-", "*", "÷").forEach { sign ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (op == sign) PurplePrimary else (if (isDark) AmoledPillBadgeBg else LightPillBadgeBg))
                            .clickable { op = sign }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (sign == "*") "×" else sign,
                            color = if (op == sign) Color.White else PurpleSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Fraction 2
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
                OutlinedTextField(
                    value = num2,
                    onValueChange = { num2 = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                )
                Divider(modifier = Modifier.padding(vertical = 4.dp), color = PurplePrimary, thickness = 2.dp)
                OutlinedTextField(
                    value = den2,
                    onValueChange = { den2 = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                )
            }
        }

        SolveButton(title = "محاسبه کسر با مراحل") {
            viewModel.solveStudioFraction(
                num1 = num1.toLongOrNull() ?: 1L,
                den1 = den1.toLongOrNull() ?: 1L,
                op = op,
                num2 = num2.toLongOrNull() ?: 1L,
                den2 = den2.toLongOrNull() ?: 1L
            )
        }
    }
}

@Composable
fun EquationInputSection(viewModel: CalculatorViewModel, isDark: Boolean) {
    var a by remember { mutableStateOf("1") }
    var b by remember { mutableStateOf("-5") }
    var c by remember { mutableStateOf("6") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "معادله درجه دو: ax² + bx + c = 0",
            fontSize = 14.sp,
            color = PurpleSecondary,
            fontWeight = FontWeight.Medium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = a,
                onValueChange = { a = it },
                label = { Text("a") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = b,
                onValueChange = { b = it },
                label = { Text("b") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = c,
                onValueChange = { c = it },
                label = { Text("c") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        SolveButton(title = "حل معادله با روش دلتا") {
            viewModel.solveStudioQuadratic(
                a = a.toDoubleOrNull() ?: 1.0,
                b = b.toDoubleOrNull() ?: 0.0,
                c = c.toDoubleOrNull() ?: 0.0
            )
        }
    }
}

@Composable
fun DerivativeInputSection(viewModel: CalculatorViewModel, isDark: Boolean) {
    var a by remember { mutableStateOf("3") }
    var n by remember { mutableStateOf("2") }
    var b by remember { mutableStateOf("4") }
    var c by remember { mutableStateOf("-1") }
    var atPoint by remember { mutableStateOf("2") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "تابع: f(x) = axⁿ + bx + c",
            fontSize = 14.sp,
            color = PurpleSecondary,
            fontWeight = FontWeight.Medium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("a") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text("n (توان)") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("b") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("c") }, modifier = Modifier.weight(1f), singleLine = true)
        }
        OutlinedTextField(
            value = atPoint,
            onValueChange = { atPoint = it },
            label = { Text("در نقطه x = (اختیاری)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        SolveButton(title = "محاسبه مشتق با قواعد تحلیلی") {
            viewModel.solveStudioDerivative(
                a = a.toDoubleOrNull() ?: 1.0,
                n = n.toDoubleOrNull() ?: 1.0,
                b = b.toDoubleOrNull() ?: 0.0,
                c = c.toDoubleOrNull() ?: 0.0,
                atPoint = atPoint.toDoubleOrNull()
            )
        }
    }
}

@Composable
fun IntegralInputSection(viewModel: CalculatorViewModel, isDark: Boolean) {
    var a by remember { mutableStateOf("2") }
    var n by remember { mutableStateOf("1") }
    var b by remember { mutableStateOf("3") }
    var lower by remember { mutableStateOf("0") }
    var upper by remember { mutableStateOf("2") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "تابع انتگرال‌ده: axⁿ + b",
            fontSize = 14.sp,
            color = PurpleSecondary,
            fontWeight = FontWeight.Medium
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("a") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text("n (توان)") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("b") }, modifier = Modifier.weight(1f), singleLine = true)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = lower, onValueChange = { lower = it }, label = { Text("کران پایین a") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = upper, onValueChange = { upper = it }, label = { Text("کران بالای b") }, modifier = Modifier.weight(1f), singleLine = true)
        }

        SolveButton(title = "محاسبه انتگرال با پادمشتق") {
            viewModel.solvePolynomialIntegral(
                a = a.toDoubleOrNull() ?: 1.0,
                n = n.toDoubleOrNull() ?: 1.0,
                b = b.toDoubleOrNull() ?: 0.0,
                lower = lower.toDoubleOrNull(),
                upper = upper.toDoubleOrNull()
            )
        }
    }
}

@Composable
fun LimitInputSection(viewModel: CalculatorViewModel, isDark: Boolean) {
    var a by remember { mutableStateOf("3") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "رفع ابهام صفر صفرم: lim_{x → a} (x² − a²) / (x − a)",
            fontSize = 14.sp,
            color = PurpleSecondary,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = a,
            onValueChange = { a = it },
            label = { Text("مقدار حد a") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        SolveButton(title = "محاسبه حد و رفع ابهام") {
            viewModel.solveStudioLimit(a.toDoubleOrNull() ?: 1.0)
        }
    }
}

@Composable
fun SigmaInputSection(viewModel: CalculatorViewModel, isDark: Boolean) {
    var n by remember { mutableStateOf("10") }
    var power by remember { mutableStateOf("1") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "سیگما و مجموع جملات: ∑_{k=1}^n k^p",
            fontSize = 14.sp,
            color = PurpleSecondary,
            fontWeight = FontWeight.Medium
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text("تعداد جملات n") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = power, onValueChange = { power = it }, label = { Text("توان جملات p (1 یا 2)") }, modifier = Modifier.weight(1f), singleLine = true)
        }

        SolveButton(title = "محاسبه فرمول مجموع") {
            viewModel.solveStudioSigma(
                n = n.toLongOrNull() ?: 10L,
                power = power.toIntOrNull() ?: 1
            )
        }
    }
}

@Composable
fun MatrixInputSection(viewModel: CalculatorViewModel, isDark: Boolean) {
    var a by remember { mutableStateOf("4") }
    var b by remember { mutableStateOf("2") }
    var c by remember { mutableStateOf("3") }
    var d by remember { mutableStateOf("1") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "ماتریس ۲×۲ : [ a  b ; c  d ]",
            fontSize = 14.sp,
            color = PurpleSecondary,
            fontWeight = FontWeight.Medium
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = a, onValueChange = { a = it }, label = { Text("a") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = b, onValueChange = { b = it }, label = { Text("b") }, modifier = Modifier.weight(1f), singleLine = true)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("c") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(value = d, onValueChange = { d = it }, label = { Text("d") }, modifier = Modifier.weight(1f), singleLine = true)
        }

        SolveButton(title = "محاسبه دترمینان و ماتریس وارون") {
            viewModel.solveStudioMatrix(
                a = a.toDoubleOrNull() ?: 1.0,
                b = b.toDoubleOrNull() ?: 0.0,
                c = c.toDoubleOrNull() ?: 0.0,
                d = d.toDoubleOrNull() ?: 1.0
            )
        }
    }
}

@Composable
fun SolveButton(title: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("solve_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PurplePrimary,
            contentColor = Color.White
        )
    ) {
        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
