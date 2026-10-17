package com.example.math.model

data class SolutionStep(
    val stepNumber: Int,
    val titleFa: String,
    val formula: MathNode,
    val explanationFa: String,
    val tag: String = "مرحله" // e.g. "فرمول اولیه", "تبدیل جبری", "محاسبه عددی", "پاسخ نهایی"
)

data class MathSolutionResult(
    val problemTitleFa: String,
    val initialFormula: MathNode,
    val steps: List<SolutionStep>,
    val exactResult: MathNode,
    val approximateResult: String? = null,
    val isExact: Boolean = true,
    val categoryFa: String = "عمومی"
)
