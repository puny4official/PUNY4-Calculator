package com.example

import com.example.math.calculator.ScientificEngine
import com.example.math.solver.MathStudioEngine
import com.example.math.util.PersianNumberWords
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger

class ExampleUnitTest {

    @Test
    fun testScientificEngineBasicArithmetic() {
        val result = ScientificEngine.evaluate("2 + 3 * 4", ScientificEngine.AngleMode.DEGREE)
        assertTrue(result is ScientificEngine.EvaluationResult.Success)
        assertEquals("14", (result as ScientificEngine.EvaluationResult.Success).formatted)
    }

    @Test
    fun testScientificEngineThousandsCommas() {
        // 1000000 + 205000 -> 1,205,000
        val result = ScientificEngine.evaluate("1000000 + 205000", ScientificEngine.AngleMode.DEGREE)
        assertTrue(result is ScientificEngine.EvaluationResult.Success)
        assertEquals("1,205,000", (result as ScientificEngine.EvaluationResult.Success).formatted)
    }

    @Test
    fun testPersianNumberWordsConversion() {
        assertEquals("یک میلیون و دویست و پنج هزار", PersianNumberWords.convert("1,205,000"))
        assertEquals("صفر", PersianNumberWords.convert("0"))
        assertEquals("چهارده", PersianNumberWords.convert("14"))
        assertEquals("منفی بیست و پنج", PersianNumberWords.convert("-25"))
    }

    @Test
    fun testScientificEngineBigFactorial() {
        val fact20 = ScientificEngine.factorial(20)
        assertEquals(BigInteger("2432902008176640000"), fact20)
    }

    @Test
    fun testScientificEngineTrigonometry() {
        val res = ScientificEngine.evaluate("sin(30)", ScientificEngine.AngleMode.DEGREE)
        assertTrue(res is ScientificEngine.EvaluationResult.Success)
        val formatted = (res as ScientificEngine.EvaluationResult.Success).formatted
        assertTrue(formatted.startsWith("0.5"))
    }

    @Test
    fun testFractionSolver() {
        // 3/4 + 5/6 = 9/12 + 10/12 = 19/12
        val sol = MathStudioEngine.solveFractionArithmetic(3, 4, "+", 5, 6)
        assertEquals("19/12", (sol.exactResult as com.example.math.model.MathNode.Fraction).let {
            "${it.numerator.toStandardString()}/${it.denominator.toStandardString()}"
        })
        assertTrue(sol.steps.isNotEmpty())
    }

    @Test
    fun testQuadraticEquationDeltaMethod() {
        // x^2 - 5x + 6 = 0 -> roots 3, 2
        val sol = MathStudioEngine.solveQuadraticEquation(1.0, -5.0, 6.0)
        assertTrue(sol.steps.size >= 4)
        assertTrue(sol.approxResult?.contains("3") == true && sol.approxResult?.contains("2") == true)
    }

    @Test
    fun testDerivativeSolver() {
        // d/dx [2x^3 - 4x^2] at x=2 -> 6x^2 - 8x -> 6(4) - 16 = 8
        val sol = MathStudioEngine.solveDerivative(
            funcType = "POLY",
            c1 = 2.0, p1 = 3.0,
            c2 = -4.0, p2 = 2.0,
            c3 = 0.0, constVal = 0.0,
            evaluatedAtX = 2.0
        )
        assertEquals("8", (sol.exactResult as com.example.math.model.MathNode.Number).value)
    }

    @Test
    fun testMatrixDeterminant() {
        // [[4, 7], [2, 6]] -> det = 4*6 - 7*2 = 24 - 14 = 10
        val sol = MathStudioEngine.solveMatrix2x2(4.0, 7.0, 2.0, 6.0, "DETERMINANT")
        assertEquals("10", (sol.exactResult as com.example.math.model.MathNode.Number).value)
    }
}
