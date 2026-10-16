package com.example.math.calculator

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.*

object ScientificEngine {

    enum class AngleMode {
        DEGREE, RADIAN
    }

    private val MC = MathContext(34, RoundingMode.HALF_UP)

    fun factorial(n: Long): BigInteger {
        if (n < 0) throw IllegalArgumentException("فاکتوریل برای اعداد منفی تعریف نشده است")
        if (n == 0L || n == 1L) return BigInteger.ONE
        var result = BigInteger.ONE
        for (i in 2L..n) {
            result = result.multiply(BigInteger.valueOf(i))
        }
        return result
    }

    fun combinations(n: Long, r: Long): BigInteger {
        if (r < 0 || r > n) return BigInteger.ZERO
        val k = min(r, n - r)
        var num = BigInteger.ONE
        var den = BigInteger.ONE
        for (i in 1L..k) {
            num = num.multiply(BigInteger.valueOf(n - i + 1))
            den = den.multiply(BigInteger.valueOf(i))
        }
        return num.divide(den)
    }

    fun permutations(n: Long, r: Long): BigInteger {
        if (r < 0 || r > n) return BigInteger.ZERO
        var res = BigInteger.ONE
        for (i in (n - r + 1)..n) {
            res = res.multiply(BigInteger.valueOf(i))
        }
        return res
    }

    /**
     * Evaluates a mathematical expression string with high precision
     */
    fun evaluate(expression: String, angleMode: AngleMode): EvaluationResult {
        try {
            val sanitized = sanitize(expression)
            if (sanitized.isBlank()) return EvaluationResult.Success(BigDecimal.ZERO, "0", true)

            val tokens = tokenize(sanitized)
            val rpn = shuntingYard(tokens)
            val result = evaluateRpn(rpn, angleMode)

            val formatted = formatResult(result)
            return EvaluationResult.Success(result, formatted, true)
        } catch (e: Exception) {
            return EvaluationResult.Error(e.message ?: "خطا در محاسبه عبارت")
        }
    }

    private fun sanitize(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "3.1415926535897932384626433832795028841971")
            .replace("e", "2.7182818284590452353602874713526624977572")
            .replace(",", "")
            .trim()
    }

    private sealed class Token {
        data class Num(val value: BigDecimal) : Token()
        data class Op(val symbol: String, val precedence: Int, val rightAssociative: Boolean) : Token()
        data class Func(val name: String) : Token()
        object LeftParen : Token()
        object RightParen : Token()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var expectUnary = true

        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> {
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.' || expr[i] == 'E' || expr[i] == 'e')) {
                        if ((expr[i] == 'E' || expr[i] == 'e') && i + 1 < expr.length && (expr[i + 1] == '+' || expr[i + 1] == '-')) {
                            sb.append(expr[i])
                            i++
                        }
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(Token.Num(BigDecimal(sb.toString())))
                    expectUnary = false
                }
                c == '(' -> {
                    // Check implicit multiplication: e.g. 5(2) -> 5*(2)
                    if (tokens.isNotEmpty() && (tokens.last() is Token.Num || tokens.last() is Token.RightParen)) {
                        tokens.add(Token.Op("*", 2, false))
                    }
                    tokens.add(Token.LeftParen)
                    i++
                    expectUnary = true
                }
                c == ')' -> {
                    tokens.add(Token.RightParen)
                    i++
                    expectUnary = false
                }
                c == '+' || c == '-' -> {
                    if (expectUnary) {
                        if (c == '-') {
                            // Unary minus: represented as 0 - x
                            tokens.add(Token.Num(BigDecimal.ZERO))
                            tokens.add(Token.Op("-", 1, false))
                        }
                        i++
                    } else {
                        tokens.add(Token.Op(c.toString(), 1, false))
                        i++
                        expectUnary = true
                    }
                }
                c == '*' || c == '/' -> {
                    tokens.add(Token.Op(c.toString(), 2, false))
                    i++
                    expectUnary = true
                }
                c == '%' -> {
                    tokens.add(Token.Op("*", 2, false))
                    tokens.add(Token.Num(BigDecimal("0.01")))
                    i++
                    expectUnary = false
                }
                c == '^' -> {
                    tokens.add(Token.Op("^", 3, true))
                    i++
                    expectUnary = true
                }
                c == '!' -> {
                    tokens.add(Token.Op("!", 4, false))
                    i++
                    expectUnary = false
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < expr.length && expr[i].isLetter()) {
                        sb.append(expr[i])
                        i++
                    }
                    val word = sb.toString().lowercase()
                    // If preceding was a number, e.g. 2sin(x) -> 2*sin(x)
                    if (tokens.isNotEmpty() && (tokens.last() is Token.Num || tokens.last() is Token.RightParen)) {
                        tokens.add(Token.Op("*", 2, false))
                    }
                    when (word) {
                        "ncr" -> tokens.add(Token.Op("nCr", 2, false))
                        "npr" -> tokens.add(Token.Op("nPr", 2, false))
                        else -> tokens.add(Token.Func(word))
                    }
                    expectUnary = true
                }
                else -> {
                    i++
                }
            }
        }
        return tokens
    }

    private fun shuntingYard(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Num -> output.add(token)
                is Token.Func -> stack.addLast(token)
                is Token.Op -> {
                    while (stack.isNotEmpty()) {
                        val top = stack.last()
                        if (top is Token.Op) {
                            if ((!token.rightAssociative && token.precedence <= top.precedence) ||
                                (token.rightAssociative && token.precedence < top.precedence)
                            ) {
                                output.add(stack.removeLast())
                            } else {
                                break
                            }
                        } else if (top is Token.Func) {
                            output.add(stack.removeLast())
                        } else {
                            break
                        }
                    }
                    stack.addLast(token)
                }
                is Token.LeftParen -> stack.addLast(token)
                is Token.RightParen -> {
                    while (stack.isNotEmpty() && stack.last() !is Token.LeftParen) {
                        output.add(stack.removeLast())
                    }
                    if (stack.isNotEmpty() && stack.last() is Token.LeftParen) {
                        stack.removeLast()
                    }
                    if (stack.isNotEmpty() && stack.last() is Token.Func) {
                        output.add(stack.removeLast())
                    }
                }
            }
        }

        while (stack.isNotEmpty()) {
            output.add(stack.removeLast())
        }
        return output
    }

    private fun evaluateRpn(rpn: List<Token>, angleMode: AngleMode): BigDecimal {
        val stack = ArrayDeque<BigDecimal>()

        for (token in rpn) {
            when (token) {
                is Token.Num -> stack.addLast(token.value)
                is Token.Op -> {
                    if (token.symbol == "!") {
                        if (stack.isEmpty()) throw IllegalArgumentException("عملگر فاکتوریل نیاز به آرگومان دارد")
                        val v = stack.removeLast().toLong()
                        stack.addLast(BigDecimal(factorial(v)))
                        continue
                    }

                    if (stack.size < 2) throw IllegalArgumentException("تعداد عملوندها برای عملگر ${token.symbol} کافی نیست")
                    val b = stack.removeLast()
                    val a = stack.removeLast()

                    val res = when (token.symbol) {
                        "+" -> a.add(b, MC)
                        "-" -> a.subtract(b, MC)
                        "*" -> a.multiply(b, MC)
                        "/" -> {
                            if (b.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("تقسیم بر صفر امکان‌پذیر نیست")
                            a.divide(b, MC)
                        }
                        "%" -> {
                            // a % of b or remainder
                            a.remainder(b, MC)
                        }
                        "^" -> {
                            // arbitrary power
                            try {
                                val bInt = b.intValueExact()
                                a.pow(bInt, MC)
                            } catch (e: Exception) {
                                val doubleVal = a.toDouble().pow(b.toDouble())
                                if (doubleVal.isNaN() || doubleVal.isInfinite()) throw ArithmeticException("محاسبه توان با این اعداد نامعتبر است")
                                BigDecimal(doubleVal, MC)
                            }
                        }
                        "nCr" -> {
                            val n = a.toLong()
                            val r = b.toLong()
                            BigDecimal(combinations(n, r))
                        }
                        "nPr" -> {
                            val n = a.toLong()
                            val r = b.toLong()
                            BigDecimal(permutations(n, r))
                        }
                        else -> throw IllegalArgumentException("عملگر ناشناخته: ${token.symbol}")
                    }
                    stack.addLast(res)
                }
                is Token.Func -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("ورودی تابع ${token.name} یافت نشد")
                    val a = stack.removeLast()
                    val aD = a.toDouble()

                    val toRadians = if (angleMode == AngleMode.DEGREE) Math.toRadians(aD) else aD
                    val fromRadians: (Double) -> Double = { rad ->
                        if (angleMode == AngleMode.DEGREE) Math.toDegrees(rad) else rad
                    }

                    val resD = when (token.name) {
                        "sin" -> sin(toRadians)
                        "cos" -> cos(toRadians)
                        "tan" -> {
                            val c = cos(toRadians)
                            if (abs(c) < 1e-12) throw ArithmeticException("تانژانت در این نقطه تعریف نشده است")
                            tan(toRadians)
                        }
                        "cot" -> {
                            val s = sin(toRadians)
                            if (abs(s) < 1e-12) throw ArithmeticException("کتانژانت در این نقطه تعریف نشده است")
                            1.0 / tan(toRadians)
                        }
                        "asin", "arcsin" -> fromRadians(asin(aD))
                        "acos", "arccos" -> fromRadians(acos(aD))
                        "atan", "arctan" -> fromRadians(atan(aD))
                        "ln" -> {
                            if (aD <= 0) throw ArithmeticException("لگاریتم طبیعی برای اعداد نامثبت تعریف نشده است")
                            ln(aD)
                        }
                        "log", "log10" -> {
                            if (aD <= 0) throw ArithmeticException("لگاریتم برای اعداد نامثبت تعریف نشده است")
                            log10(aD)
                        }
                        "log2" -> {
                            if (aD <= 0) throw ArithmeticException("لگاریتم برای اعداد نامثبت تعریف نشده است")
                            ln(aD) / ln(2.0)
                        }
                        "sqrt" -> {
                            if (aD < 0) throw ArithmeticException("رادیکال با فرجه ۲ برای اعداد منفی تعریف نشده است")
                            sqrt(aD)
                        }
                        "cbrt" -> cbrt(aD)
                        "exp" -> exp(aD)
                        "abs" -> abs(aD)
                        else -> throw IllegalArgumentException("تابع ناشناخته: ${token.name}")
                    }

                    if (resD.isNaN() || resD.isInfinite()) {
                        throw ArithmeticException("نتیجه تابع نامعتبر است (NaN یا بی‌نهایت)")
                    }

                    stack.addLast(BigDecimal(resD, MC))
                }
                else -> {}
            }
        }

        if (stack.size != 1) throw IllegalArgumentException("فرمول نامعتبر است")
        return stack.single().stripTrailingZeros()
    }

    private fun formatResult(num: BigDecimal): String {
        // Handle zero
        if (num.compareTo(BigDecimal.ZERO) == 0) return "0"

        // If integer or scale <= 0
        if (num.scale() <= 0 || num.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            val bigInt = num.toBigInteger()
            val plain = bigInt.toString()
            if (plain.length > 20) {
                // scientific notation
                val df = DecimalFormat("0.######E0")
                return df.format(num.toDouble()).replace("E", " × 10^")
            }
            val df = DecimalFormat("#,##0")
            return df.format(bigInt)
        }

        // Float formatting
        val doubleVal = num.toDouble()
        if (abs(doubleVal) >= 1e15 || (abs(doubleVal) < 1e-6 && abs(doubleVal) > 0)) {
            val df = DecimalFormat("0.########E0")
            return df.format(doubleVal).replace("E", " × 10^")
        }

        val df = DecimalFormat("#,##0.##########")
        return df.format(num)
    }

    sealed class EvaluationResult {
        data class Success(val value: BigDecimal, val formatted: String, val isExact: Boolean) : EvaluationResult()
        data class Error(val message: String) : EvaluationResult()
    }
}
