package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.model.SolutionStep
import com.example.ui.CalculatorViewModel
import com.example.ui.MathStudioCategory
import com.example.ui.components.ScrollableMathFormula
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.BlackBorder
import com.example.ui.theme.BlackSurface
import com.example.ui.theme.BlackSurfaceElevated
import com.example.ui.theme.BlackSurfaceVariant
import com.example.ui.theme.MathAmber
import com.example.ui.theme.MathCyan
import com.example.ui.theme.MathGreen
import com.example.ui.theme.MathPink
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.PurpleSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MathStudioScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val activeCategory by viewModel.activeCategory.collectAsState()
    val solution by viewModel.studioSolution.collectAsState()
    val error by viewModel.studioError.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(horizontal = 14.dp)
            .testTag("math_studio_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // CATEGORY CHIPS
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MathStudioCategory.values().forEach { cat ->
                    val isSelected = cat == activeCategory
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.setCategory(cat) }
                            .testTag("category_chip_${cat.name}"),
                        color = if (isSelected) PurplePrimary else BlackSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PurpleAccent else BlackBorder
                        )
                    ) {
                        Text(
                            text = cat.titleFa,
                            color = if (isSelected) BlackBackground else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // FORMULA EDITOR / CONFIGURATION CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("formula_editor_card"),
                colors = CardDefaults.cardColors(containerColor = BlackSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BlackBorder)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = PurpleAccent,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = "تنظیم پارامترهای ${activeCategory.titleFa}",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Category Specific Inputs
                    CategoryInputs(activeCategory, viewModel)

                    // Solve Button
                    Button(
                        onClick = { viewModel.solveStudioProblem() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("solve_problem_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PurplePrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = BlackBackground,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "حل مرحله‌به‌مرحله مسئله",
                            color = BlackBackground,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // ERROR DISPLAY
        if (error != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3F1322)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "خطا: $error",
                        color = Color(0xFFFDA4AF),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        // SOLUTION RESULT & FORMULA
        if (solution != null) {
            item {
                val sol = solution!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("solution_result_card"),
                    colors = CardDefaults.cardColors(containerColor = BlackSurfaceElevated),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurplePrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sol.problemTitleFa,
                                color = PurpleAccent,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(sol.exactResult.toStandardString()))
                                    Toast.makeText(context, "پاسخ در کلیپ‌بورد کپی شد", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "کپی",
                                    tint = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual Math Formula
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BlackSurface, RoundedCornerShape(14.dp))
                                .border(1.dp, BlackBorder, RoundedCornerShape(14.dp))
                                .padding(16.dp)
                        ) {
                            ScrollableMathFormula(
                                node = sol.originalFormula,
                                baseFontSize = 22.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Results Tag Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Exact Result Badge
                            Surface(
                                color = MathGreen.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MathGreen.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "جواب دقیق: ",
                                        color = MathGreen,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    ScrollableMathFormula(
                                        node = sol.exactResult,
                                        baseFontSize = 14.sp,
                                        textColor = MathGreen,
                                        accentColor = MathGreen
                                    )
                                }
                            }

                            // Approximate result if available
                            if (sol.approxResult != null) {
                                Surface(
                                    color = MathAmber.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MathAmber.copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = sol.approxResult!!,
                                        color = MathAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        if (sol.domainNotesFa != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "یادداشت دامنه: ${sol.domainNotesFa}",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // STEP-BY-STEP SOLUTION TITLE
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مراحل حل گام‌به‌گام به زبان فارسی",
                        color = PurpleAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = PurplePrimary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${solution!!.steps.size} مرحله",
                            color = PurpleAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // INDIVIDUAL STEP CARDS
            items(solution!!.steps) { step ->
                StepCard(step = step)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StepCard(step: SolutionStep) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("solution_step_${step.stepNumber}"),
        colors = CardDefaults.cardColors(containerColor = BlackSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlackBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Step Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PurplePrimary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "گام ${step.stepNumber}",
                            color = BlackBackground,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step.titleFa,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = BlackSurfaceElevated,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleSecondary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = step.tag,
                        color = PurpleSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Step Mathematical Formula Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlackBackground, RoundedCornerShape(10.dp))
                    .border(1.dp, BlackBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                ScrollableMathFormula(
                    node = step.formula,
                    baseFontSize = 18.sp,
                    textColor = TextPrimary,
                    accentColor = PurpleAccent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Persian Explanation and Reason
            Text(
                text = step.explanationFa,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun CategoryInputs(category: MathStudioCategory, viewModel: CalculatorViewModel) {
    when (category) {
        MathStudioCategory.FRACTIONS -> {
            val num1 by viewModel.fracNum1.collectAsState()
            val den1 by viewModel.fracDen1.collectAsState()
            val op by viewModel.fracOp.collectAsState()
            val num2 by viewModel.fracNum2.collectAsState()
            val den2 by viewModel.fracDen2.collectAsState()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fraction 1
                    Column(modifier = Modifier.weight(1f)) {
                        SmallNumField("صورت ۱", num1) { viewModel.fracNum1.value = it }
                        Spacer(modifier = Modifier.height(4.dp))
                        SmallNumField("مخرج ۱", den1) { viewModel.fracDen1.value = it }
                    }

                    // Operator selector
                    Row(
                        modifier = Modifier.weight(1.2f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("+", "−", "×", "÷").forEach { operator ->
                            val opKey = when (operator) {
                                "−" -> "-"
                                "×" -> "*"
                                "÷" -> "/"
                                else -> "+"
                            }
                            val isSel = op == opKey
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.fracOp.value = opKey },
                                color = if (isSel) PurplePrimary else BlackSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PurpleAccent else BlackBorder)
                            ) {
                                Text(
                                    text = operator,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) BlackBackground else TextPrimary,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    // Fraction 2
                    Column(modifier = Modifier.weight(1f)) {
                        SmallNumField("صورت ۲", num2) { viewModel.fracNum2.value = it }
                        Spacer(modifier = Modifier.height(4.dp))
                        SmallNumField("مخرج ۲", den2) { viewModel.fracDen2.value = it }
                    }
                }
            }
        }
        MathStudioCategory.EQUATIONS -> {
            val a by viewModel.quadA.collectAsState()
            val b by viewModel.quadB.collectAsState()
            val c by viewModel.quadC.collectAsState()
            val isSys by viewModel.isSystemMode.collectAsState()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Switch between Quadratic and System
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.isSystemMode.value = false },
                        color = if (!isSys) PurplePrimary else BlackSurfaceElevated
                    ) {
                        Text(
                            text = "معادله درجه ۲ (ax²+bx+c=0)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isSys) BlackBackground else TextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.isSystemMode.value = true },
                        color = if (isSys) PurplePrimary else BlackSurfaceElevated
                    ) {
                        Text(
                            text = "دستگاه ۲×۲ (کرامر)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSys) BlackBackground else TextSecondary,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                if (!isSys) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SmallNumField("ضریب a", a, Modifier.weight(1f)) { viewModel.quadA.value = it }
                        SmallNumField("ضریب b", b, Modifier.weight(1f)) { viewModel.quadB.value = it }
                        SmallNumField("عدد ثابت c", c, Modifier.weight(1f)) { viewModel.quadC.value = it }
                    }
                } else {
                    val a1 by viewModel.sysA1.collectAsState()
                    val b1 by viewModel.sysB1.collectAsState()
                    val c1 by viewModel.sysC1.collectAsState()
                    val a2 by viewModel.sysA2.collectAsState()
                    val b2 by viewModel.sysB2.collectAsState()
                    val c2 by viewModel.sysC2.collectAsState()

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("معادله اول: a₁x + b₁y = c₁", fontSize = 11.sp, color = PurpleSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SmallNumField("a₁", a1, Modifier.weight(1f)) { viewModel.sysA1.value = it }
                            SmallNumField("b₁", b1, Modifier.weight(1f)) { viewModel.sysB1.value = it }
                            SmallNumField("c₁", c1, Modifier.weight(1f)) { viewModel.sysC1.value = it }
                        }
                        Text("معادله دوم: a₂x + b₂y = c₂", fontSize = 11.sp, color = PurpleSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SmallNumField("a₂", a2, Modifier.weight(1f)) { viewModel.sysA2.value = it }
                            SmallNumField("b₂", b2, Modifier.weight(1f)) { viewModel.sysB2.value = it }
                            SmallNumField("c₂", c2, Modifier.weight(1f)) { viewModel.sysC2.value = it }
                        }
                    }
                }
            }
        }
        MathStudioCategory.DERIVATIVES -> {
            val type by viewModel.derivType.collectAsState()
            val c1 by viewModel.derivC1.collectAsState()
            val p1 by viewModel.derivP1.collectAsState()
            val atX by viewModel.derivAtX.collectAsState()
            val hasAt by viewModel.derivHasAtPoint.collectAsState()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Function Type Chooser
                val types = listOf(
                    Pair("POLY", "چندجمله‌ای"),
                    Pair("SIN", "سینوس sin(kx)"),
                    Pair("COS", "کسینوس cos(kx)"),
                    Pair("EXP", "نمایی e^(kx)"),
                    Pair("LN", "لگاریتم ln(x)")
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEach { (key, label) ->
                        val isSel = type == key
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.derivType.value = key },
                            color = if (isSel) PurplePrimary else BlackSurfaceElevated
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                color = if (isSel) BlackBackground else TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallNumField("ضریب (k)", c1, Modifier.weight(1f)) { viewModel.derivC1.value = it }
                    if (type == "POLY") {
                        SmallNumField("توان (n)", p1, Modifier.weight(1f)) { viewModel.derivP1.value = it }
                    }
                    SmallNumField("در نقطه x=", atX, Modifier.weight(1f)) {
                        viewModel.derivAtX.value = it
                        viewModel.derivHasAtPoint.value = it.isNotBlank()
                    }
                }
            }
        }
        MathStudioCategory.INTEGRALS -> {
            val c1 by viewModel.integC1.collectAsState()
            val p1 by viewModel.integP1.collectAsState()
            val isDef by viewModel.integIsDefinite.collectAsState()
            val lower by viewModel.integLower.collectAsState()
            val upper by viewModel.integUpper.collectAsState()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.integIsDefinite.value = true },
                        color = if (isDef) PurplePrimary else BlackSurfaceElevated
                    ) {
                        Text(
                            text = "انتگرال معین (با کران)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDef) BlackBackground else TextSecondary,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.integIsDefinite.value = false },
                        color = if (!isDef) PurplePrimary else BlackSurfaceElevated
                    ) {
                        Text(
                            text = "انتگرال نامعین (+ C)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isDef) BlackBackground else TextSecondary,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallNumField("ضریب جمله", c1, Modifier.weight(1f)) { viewModel.integC1.value = it }
                    SmallNumField("توان x", p1, Modifier.weight(1f)) { viewModel.integP1.value = it }
                    if (isDef) {
                        SmallNumField("کران پایین a", lower, Modifier.weight(1f)) { viewModel.integLower.value = it }
                        SmallNumField("کران بالا b", upper, Modifier.weight(1f)) { viewModel.integUpper.value = it }
                    }
                }
            }
        }
        MathStudioCategory.LIMITS -> {
            val targetX by viewModel.limTargetX.collectAsState()
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "حد کسر (x² − 4x + 3) / (x − 1) با ابهام 0/0 و رفع ابهام",
                    fontSize = 12.sp,
                    color = PurpleSecondary
                )
                SmallNumField("مقدار میل کردن x →", targetX) { viewModel.limTargetX.value = it }
            }
        }
        MathStudioCategory.SIGMA -> {
            val st by viewModel.sigmaStart.collectAsState()
            val en by viewModel.sigmaEnd.collectAsState()
            val type by viewModel.sigmaType.collectAsState()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        Pair("LINEAR", "جمله i"),
                        Pair("SQUARED", "جمله i²"),
                        Pair("POWER_2", "جمله 2ⁱ")
                    ).forEach { (k, title) ->
                        val isSel = type == k
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.sigmaType.value = k },
                            color = if (isSel) PurplePrimary else BlackSurfaceElevated
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                color = if (isSel) BlackBackground else TextSecondary,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SmallNumField("کران پایین (شروع)", st, Modifier.weight(1f)) { viewModel.sigmaStart.value = it }
                    SmallNumField("کران بالا (پایان)", en, Modifier.weight(1f)) { viewModel.sigmaEnd.value = it }
                }
            }
        }
        MathStudioCategory.MATRIX -> {
            val a by viewModel.matA.collectAsState()
            val b by viewModel.matB.collectAsState()
            val c by viewModel.matC.collectAsState()
            val d by viewModel.matD.collectAsState()
            val op by viewModel.matOp.collectAsState()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Operation selector
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        Pair("DETERMINANT", "دترمینان"),
                        Pair("INVERSE", "ماتریس وارون"),
                        Pair("TRANSPOSE", "ترانهاده Aᵀ")
                    ).forEach { (k, title) ->
                        val isSel = op == k
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.matOp.value = k },
                            color = if (isSel) PurplePrimary else BlackSurfaceElevated
                        ) {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                color = if (isSel) BlackBackground else TextSecondary,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // 2x2 Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SmallNumField("سطر ۱ ستون ۱ (a)", a) { viewModel.matA.value = it }
                        SmallNumField("سطر ۲ ستون ۱ (c)", c) { viewModel.matC.value = it }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SmallNumField("سطر ۱ ستون ۲ (b)", b) { viewModel.matB.value = it }
                        SmallNumField("سطر ۲ ستون ۲ (d)", d) { viewModel.matD.value = it }
                    }
                }
            }
        }
    }
}

@Composable
fun SmallNumField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        singleLine = true,
        modifier = modifier.height(54.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PurplePrimary,
            unfocusedBorderColor = BlackBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedLabelColor = PurpleAccent,
            unfocusedLabelColor = TextMuted,
            cursorColor = PurplePrimary
        ),
        shape = RoundedCornerShape(10.dp)
    )
}
