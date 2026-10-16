package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.Puny4Database
import com.example.data.entity.CalculationRecord
import com.example.math.calculator.ScientificEngine
import com.example.math.model.MathNode
import com.example.math.model.MathSolutionResult
import com.example.math.solver.MathStudioEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.math.util.PersianNumberWords
import com.example.ui.theme.AppThemeMode

enum class AppMode {
    NORMAL, MATH_STUDIO
}

enum class MathStudioCategory(val titleFa: String) {
    FRACTIONS("کسر و توان"),
    EQUATIONS("معادلات و دستگاه"),
    DERIVATIVES("مشتق"),
    INTEGRALS("انتگرال"),
    LIMITS("حد و رفع ابهام"),
    SIGMA("سیگما و سری"),
    MATRIX("ماتریس ۲×۲")
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Puny4Database.getInstance(application)
    private val dao = db.calculationDao()

    val historyList: StateFlow<List<CalculationRecord>> = dao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteList: StateFlow<List<CalculationRecord>> = dao.getFavoriteFormulas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Theme Mode: defaults automatically to LIGHT as requested
    val appThemeMode = MutableStateFlow(AppThemeMode.LIGHT)

    // Side Drawer (Half-Screen Drawer)
    val isDrawerOpen = MutableStateFlow(false)

    // Current Mode
    private val _currentMode = MutableStateFlow(AppMode.NORMAL)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    // -------------------------------------------------------------
    // NORMAL SCIENTIFIC CALCULATOR STATE
    // -------------------------------------------------------------
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _previewResult = MutableStateFlow("")
    val previewResult: StateFlow<String> = _previewResult.asStateFlow()

    private val _previewWords = MutableStateFlow("")
    val previewWords: StateFlow<String> = _previewWords.asStateFlow()

    private val _angleMode = MutableStateFlow(ScientificEngine.AngleMode.DEGREE)
    val angleMode: StateFlow<ScientificEngine.AngleMode> = _angleMode.asStateFlow()

    private val _isScientificExpanded = MutableStateFlow(true)
    val isScientificExpanded: StateFlow<Boolean> = _isScientificExpanded.asStateFlow()

    // -------------------------------------------------------------
    // MATH STUDIO STATE
    // -------------------------------------------------------------
    private val _activeCategory = MutableStateFlow(MathStudioCategory.FRACTIONS)
    val activeCategory: StateFlow<MathStudioCategory> = _activeCategory.asStateFlow()

    // Fraction inputs: num1/den1 [op] num2/den2
    val fracNum1 = MutableStateFlow("3")
    val fracDen1 = MutableStateFlow("4")
    val fracOp = MutableStateFlow("+")
    val fracNum2 = MutableStateFlow("5")
    val fracDen2 = MutableStateFlow("6")

    // Quadratic / Linear inputs: ax^2 + bx + c = 0
    val quadA = MutableStateFlow("1")
    val quadB = MutableStateFlow("-5")
    val quadC = MutableStateFlow("6")

    // System 2x2: a1 x + b1 y = c1, a2 x + b2 y = c2
    val sysA1 = MutableStateFlow("2")
    val sysB1 = MutableStateFlow("1")
    val sysC1 = MutableStateFlow("7")
    val sysA2 = MutableStateFlow("1")
    val sysB2 = MutableStateFlow("-1")
    val sysC2 = MutableStateFlow("2")
    val isSystemMode = MutableStateFlow(false)

    // Derivative inputs
    val derivType = MutableStateFlow("POLY") // "POLY", "SIN", "COS", "EXP", "LN"
    val derivC1 = MutableStateFlow("2")
    val derivP1 = MutableStateFlow("3")
    val derivC2 = MutableStateFlow("-4")
    val derivP2 = MutableStateFlow("2")
    val derivAtX = MutableStateFlow("2")
    val derivHasAtPoint = MutableStateFlow(false)

    // Integral inputs
    val integC1 = MutableStateFlow("3")
    val integP1 = MutableStateFlow("2")
    val integC2 = MutableStateFlow("2")
    val integP2 = MutableStateFlow("1")
    val integIsDefinite = MutableStateFlow(true)
    val integLower = MutableStateFlow("0")
    val integUpper = MutableStateFlow("2")

    // Limit inputs
    val limTargetX = MutableStateFlow("1")

    // Sigma inputs
    val sigmaStart = MutableStateFlow("1")
    val sigmaEnd = MutableStateFlow("5")
    val sigmaType = MutableStateFlow("LINEAR") // "LINEAR", "SQUARED", "POWER_2"

    // Matrix 2x2 inputs
    val matA = MutableStateFlow("4")
    val matB = MutableStateFlow("7")
    val matC = MutableStateFlow("2")
    val matD = MutableStateFlow("6")
    val matOp = MutableStateFlow("DETERMINANT") // "DETERMINANT", "INVERSE", "TRANSPOSE"

    // Current Math Studio Solution
    private val _studioSolution = MutableStateFlow<MathSolutionResult?>(null)
    val studioSolution: StateFlow<MathSolutionResult?> = _studioSolution.asStateFlow()

    private val _studioError = MutableStateFlow<String?>(null)
    val studioError: StateFlow<String?> = _studioError.asStateFlow()

    // Dialogs
    val showHistoryDialog = MutableStateFlow(false)
    val showScopeDialog = MutableStateFlow(false)

    init {
        // Automatically solve initial problem for Math Studio
        solveStudioProblem()
    }

    fun setAppMode(mode: AppMode) {
        _currentMode.value = mode
    }

    fun setCategory(category: MathStudioCategory) {
        _activeCategory.value = category
        solveStudioProblem()
    }

    fun toggleAngleMode() {
        _angleMode.value = if (_angleMode.value == ScientificEngine.AngleMode.DEGREE) {
            ScientificEngine.AngleMode.RADIAN
        } else {
            ScientificEngine.AngleMode.DEGREE
        }
        recalculatePreview()
    }

    fun toggleScientificKeyboard() {
        _isScientificExpanded.value = !_isScientificExpanded.value
    }

    // Normal Calculator actions
    fun onKeypadInput(token: String) {
        when (token) {
            "C", "AC" -> {
                _expression.value = ""
                _previewResult.value = ""
            }
            "DEL", "⌫" -> {
                if (_expression.value.isNotEmpty()) {
                    _expression.value = _expression.value.dropLast(1)
                    recalculatePreview()
                } else {
                    _previewResult.value = ""
                }
            }
            "=" -> {
                executeNormalCalculation()
            }
            "()" -> {
                val expr = _expression.value
                val openCount = expr.count { it == '(' }
                val closeCount = expr.count { it == ')' }
                val lastChar = expr.lastOrNull()
                if (openCount > closeCount && lastChar != null && (lastChar.isDigit() || lastChar == ')' || lastChar == '%')) {
                    _expression.value += ")"
                } else {
                    if (lastChar != null && (lastChar.isDigit() || lastChar == ')' || lastChar == '%')) {
                        _expression.value += "×("
                    } else {
                        _expression.value += "("
                    }
                }
                recalculatePreview()
            }
            "±" -> {
                if (_expression.value.startsWith("-")) {
                    _expression.value = _expression.value.removePrefix("-")
                } else if (_expression.value.isNotEmpty()) {
                    _expression.value = "-(" + _expression.value + ")"
                }
                recalculatePreview()
            }
            "+", "−", "×", "÷" -> {
                val expr = _expression.value
                if (expr.isNotEmpty()) {
                    val last = expr.last()
                    if (last in listOf('+', '−', '×', '÷', '-')) {
                        _expression.value = expr.dropLast(1) + token
                    } else {
                        _expression.value += token
                    }
                    recalculatePreview()
                }
            }
            else -> {
                _expression.value += token
                recalculatePreview()
            }
        }
    }

    private fun recalculatePreview() {
        val expr = _expression.value.trim()
        if (expr.isBlank()) {
            _previewResult.value = ""
            _previewWords.value = ""
            return
        }

        // Try direct evaluation
        val result = ScientificEngine.evaluate(expr, _angleMode.value)
        if (result is ScientificEngine.EvaluationResult.Success) {
            _previewResult.value = result.formatted
            _previewWords.value = PersianNumberWords.convert(result.formatted)
            return
        }

        // If trailing operator (e.g. "12 +"), evaluate prefix
        val trimmed = expr.dropLastWhile { it in listOf('+', '−', '×', '÷', '-', '*', '/', '^') }.trim()
        if (trimmed.isNotEmpty() && trimmed != expr) {
            val prefixResult = ScientificEngine.evaluate(trimmed, _angleMode.value)
            if (prefixResult is ScientificEngine.EvaluationResult.Success) {
                _previewResult.value = prefixResult.formatted
                _previewWords.value = PersianNumberWords.convert(prefixResult.formatted)
                return
            }
        }

        _previewResult.value = ""
        _previewWords.value = ""
    }

    fun executeNormalCalculation() {
        val expr = _expression.value
        if (expr.isBlank()) return

        val result = ScientificEngine.evaluate(expr, _angleMode.value)
        when (result) {
            is ScientificEngine.EvaluationResult.Success -> {
                val formatted = result.formatted
                _previewResult.value = formatted
                _previewWords.value = PersianNumberWords.convert(formatted)

                // Save to local Room history
                viewModelScope.launch {
                    dao.insertCalculation(
                        CalculationRecord(
                            mode = "NORMAL",
                            category = "SCIENTIFIC",
                            title = "محاسبه علمی",
                            expression = expr,
                            exactResult = formatted,
                            approxResult = formatted
                        )
                    )
                }
            }
            is ScientificEngine.EvaluationResult.Error -> {
                _previewResult.value = result.message
            }
        }
    }

    // Solve Math Studio Problem based on active category
    fun solveStudioProblem() {
        _studioError.value = null
        try {
            val solution = when (_activeCategory.value) {
                MathStudioCategory.FRACTIONS -> {
                    val n1 = fracNum1.value.toLongOrNull() ?: 1L
                    val d1 = fracDen1.value.toLongOrNull() ?: 2L
                    val n2 = fracNum2.value.toLongOrNull() ?: 1L
                    val d2 = fracDen2.value.toLongOrNull() ?: 3L
                    MathStudioEngine.solveFractionArithmetic(n1, d1, fracOp.value, n2, d2)
                }
                MathStudioCategory.EQUATIONS -> {
                    if (isSystemMode.value) {
                        val a1 = sysA1.value.toDoubleOrNull() ?: 2.0
                        val b1 = sysB1.value.toDoubleOrNull() ?: 1.0
                        val c1 = sysC1.value.toDoubleOrNull() ?: 7.0
                        val a2 = sysA2.value.toDoubleOrNull() ?: 1.0
                        val b2 = sysB2.value.toDoubleOrNull() ?: -1.0
                        val c2 = sysC2.value.toDoubleOrNull() ?: 2.0
                        MathStudioEngine.solveSystem2x2(a1, b1, c1, a2, b2, c2)
                    } else {
                        val a = quadA.value.toDoubleOrNull() ?: 1.0
                        val b = quadB.value.toDoubleOrNull() ?: -5.0
                        val c = quadC.value.toDoubleOrNull() ?: 6.0
                        MathStudioEngine.solveQuadraticEquation(a, b, c)
                    }
                }
                MathStudioCategory.DERIVATIVES -> {
                    val c1 = derivC1.value.toDoubleOrNull() ?: 2.0
                    val p1 = derivP1.value.toDoubleOrNull() ?: 3.0
                    val c2 = derivC2.value.toDoubleOrNull() ?: -4.0
                    val p2 = derivP2.value.toDoubleOrNull() ?: 2.0
                    val atX = if (derivHasAtPoint.value) derivAtX.value.toDoubleOrNull() else null
                    MathStudioEngine.solveDerivative(
                        funcType = derivType.value,
                        c1 = c1, p1 = p1,
                        c2 = c2, p2 = p2,
                        evaluatedAtX = atX
                    )
                }
                MathStudioCategory.INTEGRALS -> {
                    val c1 = integC1.value.toDoubleOrNull() ?: 3.0
                    val p1 = integP1.value.toDoubleOrNull() ?: 2.0
                    val c2 = integC2.value.toDoubleOrNull() ?: 2.0
                    val p2 = integP2.value.toDoubleOrNull() ?: 1.0
                    val lower = if (integIsDefinite.value) integLower.value.toDoubleOrNull() ?: 0.0 else null
                    val upper = if (integIsDefinite.value) integUpper.value.toDoubleOrNull() ?: 2.0 else null
                    MathStudioEngine.solveIntegral(
                        polyCoeffs = listOf(Pair(c1, p1), Pair(c2, p2)),
                        lowerBound = lower,
                        upperBound = upper
                    )
                }
                MathStudioCategory.LIMITS -> {
                    val target = limTargetX.value.toDoubleOrNull() ?: 1.0
                    MathStudioEngine.solveLimit(targetX = target)
                }
                MathStudioCategory.SIGMA -> {
                    val st = sigmaStart.value.toLongOrNull() ?: 1L
                    val en = sigmaEnd.value.toLongOrNull() ?: 5L
                    MathStudioEngine.solveSigma(st, en, sigmaType.value)
                }
                MathStudioCategory.MATRIX -> {
                    val a = matA.value.toDoubleOrNull() ?: 4.0
                    val b = matB.value.toDoubleOrNull() ?: 7.0
                    val c = matC.value.toDoubleOrNull() ?: 2.0
                    val d = matD.value.toDoubleOrNull() ?: 6.0
                    MathStudioEngine.solveMatrix2x2(a, b, c, d, matOp.value)
                }
            }

            _studioSolution.value = solution

            // Save calculation to local history
            viewModelScope.launch {
                dao.insertCalculation(
                    CalculationRecord(
                        mode = "MATH_STUDIO",
                        category = _activeCategory.value.name,
                        title = solution.problemTitleFa,
                        expression = solution.originalFormula.toStandardString(),
                        exactResult = solution.exactResult.toStandardString(),
                        approxResult = solution.approxResult
                    )
                )
            }
        } catch (e: Exception) {
            _studioError.value = e.message ?: "خطا در حل مسئله"
            _studioSolution.value = null
        }
    }

    fun toggleFavorite(record: CalculationRecord) {
        viewModelScope.launch {
            dao.setFavorite(record.id, !record.isFavorite)
        }
    }

    fun deleteHistoryItem(record: CalculationRecord) {
        viewModelScope.launch {
            dao.deleteCalculation(record)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            dao.clearNonFavoriteHistory()
        }
    }

    fun restoreCalculation(record: CalculationRecord) {
        if (record.mode == "NORMAL") {
            _currentMode.value = AppMode.NORMAL
            _expression.value = record.expression
            _previewResult.value = record.exactResult
        } else {
            _currentMode.value = AppMode.MATH_STUDIO
            val cat = MathStudioCategory.values().find { it.name == record.category }
            if (cat != null) {
                _activeCategory.value = cat
            }
        }
        showHistoryDialog.value = false
    }
}
