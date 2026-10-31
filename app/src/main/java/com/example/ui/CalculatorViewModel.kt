package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.Puny4Database
import com.example.data.entity.CalculationRecord
import com.example.math.calculator.ScientificEngine
import com.example.math.model.MathSolutionResult
import com.example.math.solver.MathStudioEngine
import com.example.math.util.PersianNumberWords
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

enum class ActiveScreen {
    NORMAL, STUDIO
}

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Puny4Database.getDatabase(application)
    private val dao = db.calculationDao()

    // Active Screen & Drawer State
    private val _currentScreen = MutableStateFlow(ActiveScreen.NORMAL)
    val currentScreen: StateFlow<ActiveScreen> = _currentScreen.asStateFlow()

    private val _themeMode = MutableStateFlow(AppThemeMode.LIGHT) // Default automatically Light Mode
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _isDrawerOpen = MutableStateFlow(false)
    val isDrawerOpen: StateFlow<Boolean> = _isDrawerOpen.asStateFlow()

    private val _isHistoryOpen = MutableStateFlow(false)
    val isHistoryOpen: StateFlow<Boolean> = _isHistoryOpen.asStateFlow()

    private val _isScopeOpen = MutableStateFlow(false)
    val isScopeOpen: StateFlow<Boolean> = _isScopeOpen.asStateFlow()

    // Normal Calculator State
    private val _expression = MutableStateFlow("0")
    val expression: StateFlow<String> = _expression.asStateFlow()

    // Result is ONLY displayed after '=' is pressed
    private val _displayedResult = MutableStateFlow<String?>(null)
    val displayedResult: StateFlow<String?> = _displayedResult.asStateFlow()

    private val _resultWordsFa = MutableStateFlow<String?>(null)
    val resultWordsFa: StateFlow<String?> = _resultWordsFa.asStateFlow()

    private val _isResultError = MutableStateFlow(false)
    val isResultError: StateFlow<Boolean> = _isResultError.asStateFlow()

    private val _angleMode = MutableStateFlow(ScientificEngine.AngleMode.DEGREE)
    val angleMode: StateFlow<ScientificEngine.AngleMode> = _angleMode.asStateFlow()

    private val _isScientificExpanded = MutableStateFlow(false)
    val isScientificExpanded: StateFlow<Boolean> = _isScientificExpanded.asStateFlow()

    // Math Studio State
    private val _studioCategory = MutableStateFlow("کسر و توان")
    val studioCategory: StateFlow<String> = _studioCategory.asStateFlow()

    private val _studioSolution = MutableStateFlow<MathSolutionResult?>(null)
    val studioSolution: StateFlow<MathSolutionResult?> = _studioSolution.asStateFlow()

    private val _studioError = MutableStateFlow<String?>(null)
    val studioError: StateFlow<String?> = _studioError.asStateFlow()

    // History Flow from Room
    val historyList = dao.getAllHistory()

    fun setScreen(screen: ActiveScreen) {
        _currentScreen.value = screen
        _isDrawerOpen.value = false
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun setDrawerOpen(open: Boolean) {
        _isDrawerOpen.value = open
    }

    fun setHistoryOpen(open: Boolean) {
        _isHistoryOpen.value = open
    }

    fun setScopeOpen(open: Boolean) {
        _isScopeOpen.value = open
    }

    fun toggleAngleMode() {
        _angleMode.value = if (_angleMode.value == ScientificEngine.AngleMode.DEGREE) {
            ScientificEngine.AngleMode.RADIAN
        } else {
            ScientificEngine.AngleMode.DEGREE
        }
    }

    fun toggleScientificExpanded() {
        _isScientificExpanded.value = !_isScientificExpanded.value
    }

    // --- Normal Calculator Actions ---

    fun onInput(char: String) {
        // If a result was just calculated, starting a new number clears it
        if (_displayedResult.value != null && isDigitOrDecimal(char)) {
            _expression.value = char
            _displayedResult.value = null
            _resultWordsFa.value = null
            _isResultError.value = false
            return
        }

        // If starting an operator after result, continue expression
        if (_displayedResult.value != null && isOperator(char)) {
            val resClean = _displayedResult.value!!.replace(",", "")
            _expression.value = resClean + char
            _displayedResult.value = null
            _resultWordsFa.value = null
            _isResultError.value = false
            return
        }

        val cur = _expression.value
        if (cur == "0" && isDigit(char)) {
            _expression.value = char
        } else {
            // Count continuous digits in current operand to enforce 36 digit limit
            val lastOperand = cur.split('+', '−', '-', '×', '*', '÷', '/', '^', '(', ')').lastOrNull() ?: ""
            if (isDigit(char) && lastOperand.replace(".", "").length >= 36) {
                // Maximum 36 digits reached for single operand
                return
            }
            _expression.value = cur + char
        }
    }

    /**
     * Toggles the sign of the last number in the expression (or of the shown result).
     * Negative numbers after an operator are wrapped in parentheses, e.g. 2×(−5),
     * because the engine reads a leading minus as "0 − x".
     */
    fun onToggleSign() {
        val result = _displayedResult.value
        if (result != null && !_isResultError.value) {
            val clean = result.replace(",", "")
            _expression.value =
                if (clean.startsWith("-") || clean.startsWith("−")) clean.substring(1)
                else "−$clean"
            _displayedResult.value = null
            _resultWordsFa.value = null
            _isResultError.value = false
            return
        }
        if (result != null) return

        val cur = _expression.value
        if (cur == "0") {
            _expression.value = "−"
            return
        }
        if (cur == "−") {
            _expression.value = "0"
            return
        }

        // Unwrap an already negative last number: (−5) -> 5, or a leading −5 -> 5
        Regex("\\(−([0-9.]+)\\)$").find(cur)?.let { m ->
            _expression.value = cur.substring(0, m.range.first) + m.groupValues[1]
            return
        }
        Regex("^−([0-9.]+)$").find(cur)?.let { m ->
            _expression.value = m.groupValues[1]
            return
        }

        // Negate the last plain number
        val m = Regex("[0-9.]+$").find(cur) ?: return
        val number = m.value
        val prefix = cur.substring(0, m.range.first)
        _expression.value = if (prefix.isEmpty()) "−$number" else "$prefix(−$number)"
    }

    fun onBackspace() {
        if (_displayedResult.value != null) {
            _displayedResult.value = null
            _resultWordsFa.value = null
            _isResultError.value = false
        }
        val cur = _expression.value
        if (cur.length <= 1) {
            _expression.value = "0"
        } else {
            _expression.value = cur.dropLast(1)
        }
    }

    fun onClear() {
        _expression.value = "0"
        _displayedResult.value = null
        _resultWordsFa.value = null
        _isResultError.value = false
    }

    fun onCalculate() {
        val expr = _expression.value
        val result = ScientificEngine.evaluate(expr, _angleMode.value)
        when (result) {
            is ScientificEngine.EvaluationResult.Success -> {
                val formatted = result.formatted
                _displayedResult.value = formatted
                _isResultError.value = false

                // Convert to Persian Words
                val words = PersianNumberWords.convert(formatted)
                _resultWordsFa.value = if (words.isNotEmpty()) words else null

                // Save to Room Database
                viewModelScope.launch {
                    dao.insert(
                        CalculationRecord(
                            mode = "NORMAL",
                            categoryFa = "محاسبه عادی",
                            expression = expr,
                            exactResult = formatted,
                            approxResult = null
                        )
                    )
                }
            }
            is ScientificEngine.EvaluationResult.Error -> {
                _displayedResult.value = result.message
                _isResultError.value = true
                _resultWordsFa.value = null
            }
        }
    }

    private fun isDigit(str: String): Boolean = str.all { it.isDigit() }
    private fun isDigitOrDecimal(str: String): Boolean = str.all { it.isDigit() || it == '.' }
    private fun isOperator(str: String): Boolean = str in listOf("+", "−", "-", "×", "*", "÷", "/", "^", "%")

    // --- Math Studio Actions ---

    fun setStudioCategory(cat: String) {
        _studioCategory.value = cat
        _studioError.value = null
    }

    fun solveStudioFraction(num1: Long, den1: Long, op: String, num2: Long, den2: Long) {
        try {
            _studioError.value = null
            val solution = MathStudioEngine.solveFractionArithmetic(num1, den1, op, num2, den2)
            _studioSolution.value = solution
            saveStudioSolution(solution)
        } catch (e: Exception) {
            _studioError.value = e.message ?: "خطا در حل کسر"
        }
    }

    fun solveStudioQuadratic(a: Double, b: Double, c: Double) {
        try {
            _studioError.value = null
            val solution = MathStudioEngine.solveQuadraticEquation(a, b, c)
            _studioSolution.value = solution
            saveStudioSolution(solution)
        } catch (e: Exception) {
            _studioError.value = e.message ?: "خطا در حل معادله"
        }
    }

    fun solveStudioDerivative(a: Double, n: Double, b: Double, c: Double, atPoint: Double?) {
        try {
            _studioError.value = null
            val solution = MathStudioEngine.solvePolynomialDerivative(a, n, b, c, atPoint)
            _studioSolution.value = solution
            saveStudioSolution(solution)
        } catch (e: Exception) {
            _studioError.value = e.message ?: "خطا در محاسبه مشتق"
        }
    }

    fun solveStudioIntegral(a: Double, n: Double, b: Double, lower: Double?, upper: Double?) {
        try {
            _studioError.value = null
            val solution = MathStudioEngine.solvePolynomialIntegral(a, n, b, lower, upper)
            _studioSolution.value = solution
            saveStudioSolution(solution)
        } catch (e: Exception) {
            _studioError.value = e.message ?: "خطا در محاسبه انتگرال"
        }
    }

    fun solveStudioLimit(a: Double) {
        try {
            _studioError.value = null
            val solution = MathStudioEngine.solveIndeterminateLimit(a)
            _studioSolution.value = solution
            saveStudioSolution(solution)
        } catch (e: Exception) {
            _studioError.value = e.message ?: "خطا در محاسبه حد"
        }
    }

    fun solveStudioSigma(n: Long, power: Int) {
        try {
            _studioError.value = null
            val solution = MathStudioEngine.solveSigmaSummation(n, power)
            _studioSolution.value = solution
            saveStudioSolution(solution)
        } catch (e: Exception) {
            _studioError.value = e.message ?: "خطا در محاسبه سیگما"
        }
    }

    fun solveStudioMatrix(a: Double, b: Double, c: Double, d: Double) {
        try {
            _studioError.value = null
            val solution = MathStudioEngine.solveMatrix2x2(a, b, c, d)
            _studioSolution.value = solution
            saveStudioSolution(solution)
        } catch (e: Exception) {
            _studioError.value = e.message ?: "خطا در محاسبه ماتریس"
        }
    }

    fun solveStudioSystem(a1: Double, b1: Double, c1: Double, a2: Double, b2: Double, c2: Double) {
        try {
            _studioError.value = null
            val solution = MathStudioEngine.solveSystem2x2(a1, b1, c1, a2, b2, c2)
            _studioSolution.value = solution
            saveStudioSolution(solution)
        } catch (e: Exception) {
            _studioError.value = e.message ?: "خطا در حل دستگاه"
        }
    }

    private fun saveStudioSolution(solution: MathSolutionResult) {
        viewModelScope.launch {
            dao.insert(
                CalculationRecord(
                    mode = "STUDIO",
                    categoryFa = solution.categoryFa,
                    expression = solution.initialFormula.toStandardString(),
                    exactResult = solution.exactResult.toStandardString(),
                    approxResult = solution.approximateResult
                )
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }

    fun restoreCalculation(record: CalculationRecord) {
        if (record.mode == "NORMAL") {
            _expression.value = record.expression
            _displayedResult.value = record.exactResult
            _resultWordsFa.value = PersianNumberWords.convert(record.exactResult)
            _currentScreen.value = ActiveScreen.NORMAL
        }
        _isHistoryOpen.value = false
    }
}
