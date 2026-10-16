package com.example.math.model

data class SolutionStep(
    val stepNumber: Int,
    val titleFa: String,
    val formula: MathNode,
    val explanationFa: String,
    val tag: String = "مرحله" // e.g. "فرمول اولیه", "تبدیل جبری", "قاعده مشتق", "نتیجه نهایی"
)

data class MathSolutionResult(
    val problemTitleFa: String,
    val originalFormula: MathNode,
    val steps: List<SolutionStep>,
    val exactResult: MathNode,
    val approxResult: String? = null,
    val isExact: Boolean = true,
    val domainNotesFa: String? = null
)
