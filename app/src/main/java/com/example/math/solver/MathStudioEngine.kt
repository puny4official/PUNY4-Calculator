package com.example.math.solver

import com.example.math.model.MathNode
import com.example.math.model.MathSolutionResult
import com.example.math.model.SolutionStep
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.sqrt

object MathStudioEngine {

    private fun gcd(a: BigInteger, b: BigInteger): BigInteger = a.gcd(b)

    /**
     * Solves fraction arithmetic (Addition, Subtraction, Multiplication, Division)
     * with detailed Persian steps.
     */
    fun solveFractionArithmetic(
        num1: Long, den1: Long,
        op: String,
        num2: Long, den2: Long
    ): MathSolutionResult {
        if (den1 == 0L || den2 == 0L) {
            throw IllegalArgumentException("مخرج کسر نمی‌تواند صفر باشد.")
        }

        val bNum1 = BigInteger.valueOf(num1)
        val bDen1 = BigInteger.valueOf(den1)
        val bNum2 = BigInteger.valueOf(num2)
        val bDen2 = BigInteger.valueOf(den2)

        val frac1Node = MathNode.Fraction(MathNode.Number(num1.toString()), MathNode.Number(den1.toString()))
        val frac2Node = MathNode.Fraction(MathNode.Number(num2.toString()), MathNode.Number(den2.toString()))

        val initialFormula = when (op) {
            "+" -> MathNode.Addition(frac1Node, frac2Node)
            "-" -> MathNode.Subtraction(frac1Node, frac2Node)
            "*" -> MathNode.Multiplication(frac1Node, frac2Node, true)
            else -> MathNode.Division(frac1Node, frac2Node)
        }

        val steps = mutableListOf<SolutionStep>()
        var stepNum = 1

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "صورت مسئله",
                formula = initialFormula,
                explanationFa = "عملیات مورد نظر بر روی دو کسر با مخرج‌های $den1 و $den2 تعریف شده است.",
                tag = "فرمول اولیه"
            )
        )

        val resNum: BigInteger
        val resDen: BigInteger

        when (op) {
            "+", "-" -> {
                val lcmDen = (bDen1.multiply(bDen2)).divide(gcd(bDen1, bDen2)).abs()
                val factor1 = lcmDen.divide(bDen1)
                val factor2 = lcmDen.divide(bDen2)

                steps.add(
                    SolutionStep(
                        stepNumber = stepNum++,
                        titleFa = "یافتن مخرج مشترک (ک.م.م)",
                        formula = MathNode.Equation(
                            MathNode.TextNode("ک.م.م($den1, $den2)"),
                            MathNode.Number(lcmDen.toString())
                        ),
                        explanationFa = "برای جمع یا تفریق دو کسر، ابتدا مخرج مشترک را از طریق کمترین مضرب مشترک مخرج‌ها پیدا می‌کنیم که برابر $lcmDen است.",
                        tag = "تبدیل جبری"
                    )
                )

                val scaledNum1 = bNum1.multiply(factor1)
                val scaledNum2 = bNum2.multiply(factor2)

                steps.add(
                    SolutionStep(
                        stepNumber = stepNum++,
                        titleFa = "هم‌مخرج کردن کسرها",
                        formula = MathNode.Equation(
                            initialFormula,
                            if (op == "+") {
                                MathNode.Addition(
                                    MathNode.Fraction(MathNode.Number(scaledNum1.toString()), MathNode.Number(lcmDen.toString())),
                                    MathNode.Fraction(MathNode.Number(scaledNum2.toString()), MathNode.Number(lcmDen.toString()))
                                )
                            } else {
                                MathNode.Subtraction(
                                    MathNode.Fraction(MathNode.Number(scaledNum1.toString()), MathNode.Number(lcmDen.toString())),
                                    MathNode.Fraction(MathNode.Number(scaledNum2.toString()), MathNode.Number(lcmDen.toString()))
                                )
                            }
                        ),
                        explanationFa = "صورت هر کسر در ضریب متناظر ضرب می‌شود: $num1 × $factor1 = $scaledNum1 و $num2 × $factor2 = $scaledNum2.",
                        tag = "محاسبه"
                    )
                )

                resNum = if (op == "+") scaledNum1.add(scaledNum2) else scaledNum1.subtract(scaledNum2)
                resDen = lcmDen
            }
            "*" -> {
                steps.add(
                    SolutionStep(
                        stepNumber = stepNum++,
                        titleFa = "قاعده ضرب کسرها",
                        formula = MathNode.Fraction(
                            MathNode.Multiplication(MathNode.Number(num1.toString()), MathNode.Number(num2.toString()), true),
                            MathNode.Multiplication(MathNode.Number(den1.toString()), MathNode.Number(den2.toString()), true)
                        ),
                        explanationFa = "در ضرب کسرها، صورت‌ها در یکدیگر و مخرج‌ها نیز در یکدیگر ضرب می‌شوند.",
                        tag = "قاعده"
                    )
                )

                resNum = bNum1.multiply(bNum2)
                resDen = bDen1.multiply(bDen2)
            }
            else -> {
                steps.add(
                    SolutionStep(
                        stepNumber = stepNum++,
                        titleFa = "قاعده تقسیم کسرها (ضرب در معکوس)",
                        formula = MathNode.Multiplication(
                            frac1Node,
                            MathNode.Fraction(MathNode.Number(den2.toString()), MathNode.Number(num2.toString())),
                            true
                        ),
                        explanationFa = "تقسیم کسر اول بر دوم برابر با ضرب کسر اول در معکوس کسر دوم است.",
                        tag = "قاعده"
                    )
                )

                resNum = bNum1.multiply(bDen2)
                resDen = bDen1.multiply(bNum2)
            }
        }

        val g = gcd(resNum.abs(), resDen.abs())
        var finalNum = resNum.divide(g)
        var finalDen = resDen.divide(g)
        if (finalDen < BigInteger.ZERO) {
            finalNum = finalNum.negate()
            finalDen = finalDen.negate()
        }

        if (g > BigInteger.ONE) {
            steps.add(
                SolutionStep(
                    stepNumber = stepNum++,
                    titleFa = "ساده‌سازی با ب.م.م",
                    formula = MathNode.Equation(
                        MathNode.Fraction(MathNode.Number(resNum.toString()), MathNode.Number(resDen.toString())),
                        MathNode.Fraction(MathNode.Number(finalNum.toString()), MathNode.Number(finalDen.toString()))
                    ),
                    explanationFa = "بزرگ‌ترین مقسوم‌علیه مشترک (ب.م.م) صورت و مخرج برابر با $g است؛ بنابراین هر دو را بر $g تقسیم می‌کنیم.",
                    tag = "ساده‌سازی"
                )
            )
        }

        val exactNode: MathNode = if (finalDen == BigInteger.ONE) {
            MathNode.Number(finalNum.toString())
        } else {
            MathNode.Fraction(MathNode.Number(finalNum.toString()), MathNode.Number(finalDen.toString()))
        }

        val approx = BigDecimal(finalNum).divide(BigDecimal(finalDen), MathContext(8, RoundingMode.HALF_UP)).toPlainString()

        steps.add(
            SolutionStep(
                stepNumber = stepNum,
                titleFa = "پاسخ نهایی",
                formula = exactNode,
                explanationFa = "پاسخ دقیق کسر برابر است با ${finalNum}${if (finalDen != BigInteger.ONE) "/$finalDen" else ""} (تقریباً $approx).",
                tag = "پاسخ نهایی"
            )
        )

        return MathSolutionResult(
            problemTitleFa = "محاسبه کسر",
            initialFormula = initialFormula,
            steps = steps,
            exactResult = exactNode,
            approximateResult = approx,
            isExact = true,
            categoryFa = "کسر و توان"
        )
    }

    /**
     * Solves quadratic equation: a*x^2 + b*x + c = 0
     * using Delta (discriminant) method.
     */
    fun solveQuadraticEquation(a: Double, b: Double, c: Double): MathSolutionResult {
        if (a == 0.0) {
            throw IllegalArgumentException("ضریب a در معادله درجه دو نمی‌تواند صفر باشد.")
        }

        val initialFormula = MathNode.Equation(
            MathNode.Addition(
                MathNode.Addition(
                    MathNode.Multiplication(MathNode.Number(a.toString()), MathNode.Power(MathNode.Variable("x"), MathNode.Number("2"))),
                    MathNode.Multiplication(MathNode.Number(b.toString()), MathNode.Variable("x"))
                ),
                MathNode.Number(c.toString())
            ),
            MathNode.Number("0")
        )

        val steps = mutableListOf<SolutionStep>()
        var stepNum = 1

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "صورت استاندارد معادله درجه دوم",
                formula = initialFormula,
                explanationFa = "معادله به فرم کلی ax² + bx + c = 0 است که در آن: a = $a ، b = $b ، c = $c.",
                tag = "فرمول اولیه"
            )
        )

        val delta = b * b - 4 * a * c

        val deltaFormula = MathNode.Equation(
            MathNode.Variable("Δ"),
            MathNode.Subtraction(
                MathNode.Power(MathNode.Variable("b"), MathNode.Number("2")),
                MathNode.Multiplication(
                    MathNode.Multiplication(MathNode.Number("4"), MathNode.Variable("a")),
                    MathNode.Variable("c")
                )
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "محاسبه دلتا (ممیز معادله)",
                formula = deltaFormula,
                explanationFa = "فرمول دلتا برابر با Δ = b² − 4ac است.",
                tag = "قاعده دلتا"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "جای‌گذاری مقادیر در دلتا",
                formula = MathNode.Equation(
                    MathNode.Variable("Δ"),
                    MathNode.Number(String.format("%.4f", delta))
                ),
                explanationFa = "Δ = ($b)² − 4($a)($c) = ${b*b} − ${4*a*c} = $delta",
                tag = "محاسبه عددی"
            )
        )

        val exactResultNode: MathNode
        val approxText: String

        if (delta > 0) {
            val sqrtDelta = sqrt(delta)
            val x1 = (-b + sqrtDelta) / (2 * a)
            val x2 = (-b - sqrtDelta) / (2 * a)

            steps.add(
                SolutionStep(
                    stepNumber = stepNum++,
                    titleFa = "تحلیل علامت دلتا (Δ > 0)",
                    formula = MathNode.TextNode("Δ > 0 → دارای دو ریشه حقیقی متمایز"),
                    explanationFa = "چون دلتا مثبت است، معادله دارای دو ریشه حقیقی و متمایز x₁ و x₂ می‌باشد.",
                    tag = "نتیجه دلتا"
                )
            )

            steps.add(
                SolutionStep(
                    stepNumber = stepNum++,
                    titleFa = "فرمول ریشه‌های معادله",
                    formula = MathNode.Equation(
                        MathNode.Variable("x"),
                        MathNode.Fraction(
                            MathNode.TextNode("-b ± √Δ"),
                            MathNode.Multiplication(MathNode.Number("2"), MathNode.Variable("a"))
                        )
                    ),
                    explanationFa = "مقدار ریشه‌ها از رابطه (-b ± √Δ) / 2a به دست می‌آید.",
                    tag = "قاعده ریشه"
                )
            )

            exactResultNode = MathNode.TextNode("x₁ = ${String.format("%.4f", x1)} , x₂ = ${String.format("%.4f", x2)}")
            approxText = "x₁ ≈ ${String.format("%.4f", x1)}, x₂ ≈ ${String.format("%.4f", x2)}"

            steps.add(
                SolutionStep(
                    stepNumber = stepNum,
                    titleFa = "پاسخ نهایی",
                    formula = exactResultNode,
                    explanationFa = "ریشه‌های حقیقی معادله عبارتند از: x₁ = $x1 و x₂ = $x2.",
                    tag = "پاسخ نهایی"
                )
            )
        } else if (delta == 0.0) {
            val x = -b / (2 * a)

            steps.add(
                SolutionStep(
                    stepNumber = stepNum++,
                    titleFa = "تحلیل علامت دلتا (Δ = 0)",
                    formula = MathNode.TextNode("Δ = 0 → دارای یک ریشه مضاعف"),
                    explanationFa = "چون دلتا صفر است، معادله دارای یک ریشه حقیقی مضاعف است.",
                    tag = "نتیجه دلتا"
                )
            )

            exactResultNode = MathNode.Equation(MathNode.Variable("x"), MathNode.Number(String.format("%.4f", x)))
            approxText = "x = ${String.format("%.4f", x)}"

            steps.add(
                SolutionStep(
                    stepNumber = stepNum,
                    titleFa = "پاسخ نهایی",
                    formula = exactResultNode,
                    explanationFa = "ریشه مضاعف معادله x = -b / 2a = $x می‌باشد.",
                    tag = "پاسخ نهایی"
                )
            )
        } else {
            val realPart = -b / (2 * a)
            val imagPart = sqrt(-delta) / (2 * a)

            steps.add(
                SolutionStep(
                    stepNumber = stepNum++,
                    titleFa = "تحلیل علامت دلتا (Δ < 0)",
                    formula = MathNode.TextNode("Δ < 0 → فاقد ریشه حقیقی (دارای ریشه‌های مختلط)"),
                    explanationFa = "چون دلتا منفی است، معادله در مجموعه اعداد حقیقی ریشه ندارد و دارای دو ریشه مزدوج مختلط است.",
                    tag = "نتیجه دلتا"
                )
            )

            val rootText = "${String.format("%.4f", realPart)} ± ${String.format("%.4f", Math.abs(imagPart))}i"
            exactResultNode = MathNode.Equation(MathNode.Variable("x"), MathNode.TextNode(rootText))
            approxText = rootText

            steps.add(
                SolutionStep(
                    stepNumber = stepNum,
                    titleFa = "پاسخ نهایی (اعداد مختلط)",
                    formula = exactResultNode,
                    explanationFa = "ریشه‌های مختلط معادله: $rootText.",
                    tag = "پاسخ نهایی"
                )
            )
        }

        return MathSolutionResult(
            problemTitleFa = "حل معادله درجه دوم",
            initialFormula = initialFormula,
            steps = steps,
            exactResult = exactResultNode,
            approximateResult = approxText,
            isExact = true,
            categoryFa = "معادلات و دستگاه"
        )
    }

    /**
     * Solves polynomial derivative: d/dx [a*x^n + b*x + c]
     * with step-by-step power rule explanations in Persian.
     */
    fun solvePolynomialDerivative(a: Double, n: Double, b: Double, c: Double, atPoint: Double? = null): MathSolutionResult {
        val polyNode = MathNode.Addition(
            MathNode.Addition(
                MathNode.Multiplication(MathNode.Number(a.toString()), MathNode.Power(MathNode.Variable("x"), MathNode.Number(n.toString()))),
                MathNode.Multiplication(MathNode.Number(b.toString()), MathNode.Variable("x"))
            ),
            MathNode.Number(c.toString())
        )

        val initialFormula = MathNode.Derivative(
            variable = "x",
            order = 1,
            expr = polyNode,
            atPoint = atPoint?.let { MathNode.Number(it.toString()) }
        )

        val steps = mutableListOf<SolutionStep>()
        var stepNum = 1

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "تابع اولیه",
                formula = MathNode.Equation(MathNode.FunctionCall("f", MathNode.Variable("x")), polyNode),
                explanationFa = "قصد داریم مشتق تابع چندجمله‌ای را برحسب x محاسبه کنیم.",
                tag = "فرمول اولیه"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "قاعده مشتق توان و جمع توابع",
                formula = MathNode.Equation(
                    MathNode.Derivative("x", 1, MathNode.Power(MathNode.Variable("x"), MathNode.Variable("n"))),
                    MathNode.Multiplication(MathNode.Variable("n"), MathNode.Power(MathNode.Variable("x"), MathNode.Subtraction(MathNode.Variable("n"), MathNode.Number("1"))))
                ),
                explanationFa = "بر اساس قاعده توان، مشتق xⁿ برابر است با n·xⁿ⁻¹. همچنین مشتق مجموع توابع برابر با مجموع مشتق‌های آن‌هاست و مشتق عدد ثابت صفر است.",
                tag = "قاعده مشتق"
            )
        )

        val derivA = a * n
        val derivN = n - 1.0
        val derivB = b

        val derivNode = if (derivN == 1.0) {
            MathNode.Addition(
                MathNode.Multiplication(MathNode.Number(String.format("%.2f", derivA)), MathNode.Variable("x")),
                MathNode.Number(String.format("%.2f", derivB))
            )
        } else if (derivN == 0.0) {
            MathNode.Number(String.format("%.2f", derivA + derivB))
        } else {
            MathNode.Addition(
                MathNode.Multiplication(MathNode.Number(String.format("%.2f", derivA)), MathNode.Power(MathNode.Variable("x"), MathNode.Number(String.format("%.2f", derivN)))),
                MathNode.Number(String.format("%.2f", derivB))
            )
        }

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "محاسبه ضریب‌ها و توان‌ها",
                formula = MathNode.Equation(
                    MathNode.FunctionCall("f'", MathNode.Variable("x")),
                    derivNode
                ),
                explanationFa = "جمله اول: $a × $n·x^($n − 1) = $derivA·x^$derivN. جمله دوم: مشتق ${b}x برابر $b است. مشتق عدد ثابت $c برابر صفر است.",
                tag = "مشتق‌گیری"
            )
        )

        val finalExactNode: MathNode
        var approxStr: String? = null

        if (atPoint != null) {
            val evaluated = derivA * Math.pow(atPoint, derivN) + derivB
            finalExactNode = MathNode.Equation(
                MathNode.FunctionCall("f'", MathNode.Number(atPoint.toString())),
                MathNode.Number(String.format("%.4f", evaluated))
            )
            approxStr = String.format("%.4f", evaluated)

            steps.add(
                SolutionStep(
                    stepNumber = stepNum,
                    titleFa = "محاسبه مقدار مشتق در نقطه x = $atPoint",
                    formula = finalExactNode,
                    explanationFa = "مقدار x = $atPoint را در تابع مشتق جای‌گذاری می‌کنیم: f'($atPoint) = $evaluated.",
                    tag = "پاسخ نهایی"
                )
            )
        } else {
            finalExactNode = MathNode.Equation(MathNode.FunctionCall("f'", MathNode.Variable("x")), derivNode)
            steps.add(
                SolutionStep(
                    stepNumber = stepNum,
                    titleFa = "پاسخ نهایی (ضابطه تابع مشتق)",
                    formula = finalExactNode,
                    explanationFa = "ضابطه مشتق اول تابع f(x) به دست آمد.",
                    tag = "پاسخ نهایی"
                )
            )
        }

        return MathSolutionResult(
            problemTitleFa = "مشتق چندجمله‌ای",
            initialFormula = initialFormula,
            steps = steps,
            exactResult = finalExactNode,
            approximateResult = approxStr,
            isExact = true,
            categoryFa = "مشتق"
        )
    }

    /**
     * Solves polynomial definite & indefinite integral:
     * ∫ (a*x^n + b) dx from lower to upper bound
     */
    fun solvePolynomialIntegral(a: Double, n: Double, b: Double, lower: Double? = null, upper: Double? = null): MathSolutionResult {
        val integrand = MathNode.Addition(
            MathNode.Multiplication(MathNode.Number(a.toString()), MathNode.Power(MathNode.Variable("x"), MathNode.Number(n.toString()))),
            MathNode.Number(b.toString())
        )

        val initialFormula = MathNode.Integral(
            variable = "x",
            lowerBound = lower?.let { MathNode.Number(it.toString()) },
            upperBound = upper?.let { MathNode.Number(it.toString()) },
            integrand = integrand
        )

        val steps = mutableListOf<SolutionStep>()
        var stepNum = 1

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "صورت انتگرال",
                formula = initialFormula,
                explanationFa = if (lower != null && upper != null) {
                    "انتگرال معین از کران پایین $lower تا کران بالای $upper."
                } else {
                    "انتگرال نامعین تابع برحسب متغیر x."
                },
                tag = "فرمول اولیه"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "قاعده انتگرال‌گیری توان",
                formula = MathNode.Equation(
                    MathNode.Integral("x", null, null, MathNode.Power(MathNode.Variable("x"), MathNode.Variable("n"))),
                    MathNode.Addition(
                        MathNode.Fraction(
                            MathNode.Power(MathNode.Variable("x"), MathNode.Addition(MathNode.Variable("n"), MathNode.Number("1"))),
                            MathNode.Addition(MathNode.Variable("n"), MathNode.Number("1"))
                        ),
                        MathNode.Variable("C")
                    )
                ),
                explanationFa = "پادمشتق xⁿ به صورت (xⁿ⁺¹) / (n + 1) محاسبه می‌شود.",
                tag = "قاعده انتگرال"
            )
        )

        val newN = n + 1.0
        val antiA = a / newN
        val antiB = b

        val antiderivativeNode = MathNode.Addition(
            MathNode.Multiplication(MathNode.Number(String.format("%.2f", antiA)), MathNode.Power(MathNode.Variable("x"), MathNode.Number(String.format("%.2f", newN)))),
            MathNode.Multiplication(MathNode.Number(String.format("%.2f", antiB)), MathNode.Variable("x"))
        )

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "محاسبه تابع پادمشتق F(x)",
                formula = MathNode.Equation(MathNode.FunctionCall("F", MathNode.Variable("x")), antiderivativeNode),
                explanationFa = "F(x) = ($a / $newN)·x^$newN + ${b}x = ${String.format("%.2f", antiA)}·x^$newN + ${antiB}x.",
                tag = "پادمشتق"
            )
        )

        val exactResultNode: MathNode
        var approxStr: String? = null

        if (lower != null && upper != null) {
            val fUpper = antiA * Math.pow(upper, newN) + antiB * upper
            val fLower = antiA * Math.pow(lower, newN) + antiB * lower
            val defValue = fUpper - fLower

            steps.add(
                SolutionStep(
                    stepNumber = stepNum++,
                    titleFa = "قضیه اساسی حساب دیفرانسیل و انتگرال",
                    formula = MathNode.Equation(
                        MathNode.TextNode("∫_{a}^{b} f(x) dx"),
                        MathNode.Subtraction(MathNode.FunctionCall("F", MathNode.Variable("b")), MathNode.FunctionCall("F", MathNode.Variable("a")))
                    ),
                    explanationFa = "مقدار انتگرال معین برابر است با تفاضل پادمشتق در کران بالا و کران پایین: F(b) − F(a).",
                    tag = "محاسبه معین"
                )
            )

            steps.add(
                SolutionStep(
                    stepNumber = stepNum++,
                    titleFa = "جای‌گذاری کران‌ها",
                    formula = MathNode.Equation(
                        MathNode.TextNode("F($upper) − F($lower)"),
                        MathNode.TextNode("${String.format("%.4f", fUpper)} − ${String.format("%.4f", fLower)} = ${String.format("%.4f", defValue)}")
                    ),
                    explanationFa = "F($upper) = $fUpper و F($lower) = $fLower.",
                    tag = "جای‌گذاری"
                )
            )

            exactResultNode = MathNode.Number(String.format("%.4f", defValue))
            approxStr = String.format("%.4f", defValue)

            steps.add(
                SolutionStep(
                    stepNumber = stepNum,
                    titleFa = "پاسخ نهایی",
                    formula = exactResultNode,
                    explanationFa = "حاصل انتگرال معین دقیقاً برابر است با $defValue.",
                    tag = "پاسخ نهایی"
                )
            )
        } else {
            exactResultNode = MathNode.Addition(antiderivativeNode, MathNode.Variable("C"))
            steps.add(
                SolutionStep(
                    stepNumber = stepNum,
                    titleFa = "پاسخ نهایی",
                    formula = exactResultNode,
                    explanationFa = "حاصل انتگرال نامعین همراه با ثابت اختیاری C به دست آمد.",
                    tag = "پاسخ نهایی"
                )
            )
        }

        return MathSolutionResult(
            problemTitleFa = "انتگرال تابع",
            initialFormula = initialFormula,
            steps = steps,
            exactResult = exactResultNode,
            approximateResult = approxStr,
            isExact = true,
            categoryFa = "انتگرال"
        )
    }

    /**
     * Solves 2x2 Matrix operations: Determinant and Inverse
     */
    fun solveMatrix2x2(a: Double, b: Double, c: Double, d: Double): MathSolutionResult {
        val matNode = MathNode.Matrix(
            rows = 2,
            cols = 2,
            values = listOf(
                listOf(MathNode.Number(a.toString()), MathNode.Number(b.toString())),
                listOf(MathNode.Number(c.toString()), MathNode.Number(d.toString()))
            )
        )

        val steps = mutableListOf<SolutionStep>()
        var stepNum = 1

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "ماتریس اولیه A",
                formula = MathNode.Equation(MathNode.Variable("A"), matNode),
                explanationFa = "ماتریس مربعی مرتبه ۲ با درایه‌های a = $a, b = $b, c = $c, d = $d.",
                tag = "فرمول اولیه"
            )
        )

        val det = a * d - b * c

        val detFormula = MathNode.Equation(
            MathNode.TextNode("det(A) = |A|"),
            MathNode.Subtraction(
                MathNode.Multiplication(MathNode.Variable("a"), MathNode.Variable("d")),
                MathNode.Multiplication(MathNode.Variable("b"), MathNode.Variable("c"))
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "فرمول دترمینان ماتریس ۲×۲",
                formula = detFormula,
                explanationFa = "دترمینان ماتریس از ضرب قطر اصلی منهای ضرب قطر فرعی محاسبه می‌شود: ad − bc.",
                tag = "قاعده دترمینان"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "محاسبه عددی دترمینان",
                formula = MathNode.Equation(
                    MathNode.TextNode("det(A)"),
                    MathNode.Number(String.format("%.4f", det))
                ),
                explanationFa = "det(A) = ($a × $d) − ($b × $c) = ${a*d} − ${b*c} = $det.",
                tag = "محاسبه"
            )
        )

        val exactResultNode: MathNode
        var approxStr: String?

        if (det != 0.0) {
            val invA = d / det
            val invB = -b / det
            val invC = -c / det
            val invD = a / det

            val invNode = MathNode.Matrix(
                rows = 2,
                cols = 2,
                values = listOf(
                    listOf(MathNode.Number(String.format("%.4f", invA)), MathNode.Number(String.format("%.4f", invB))),
                    listOf(MathNode.Number(String.format("%.4f", invC)), MathNode.Number(String.format("%.4f", invD)))
                )
            )

            steps.add(
                SolutionStep(
                    stepNumber = stepNum++,
                    titleFa = "فرمول ماتریس وارون A⁻¹",
                    formula = MathNode.Equation(
                        MathNode.Power(MathNode.Variable("A"), MathNode.Number("-1")),
                        MathNode.Multiplication(
                            MathNode.Fraction(MathNode.Number("1"), MathNode.TextNode("det(A)")),
                            MathNode.Matrix(
                                rows = 2,
                                cols = 2,
                                values = listOf(
                                    listOf(MathNode.Variable("d"), MathNode.TextNode("-b")),
                                    listOf(MathNode.TextNode("-c"), MathNode.Variable("a"))
                                )
                            )
                        )
                    ),
                    explanationFa = "چون دترمینان مخالف صفر است، ماتریس وارون‌پذیر است. جای درایه‌های قطر اصلی عوض شده و قطر فرعی قرینه می‌شود.",
                    tag = "قاعده وارون"
                )
            )

            exactResultNode = MathNode.Equation(MathNode.Power(MathNode.Variable("A"), MathNode.Number("-1")), invNode)
            approxStr = "det = $det"

            steps.add(
                SolutionStep(
                    stepNumber = stepNum,
                    titleFa = "پاسخ نهایی: ماتریس وارون",
                    formula = exactResultNode,
                    explanationFa = "ماتریس معکوس A⁻¹ با تقسیم هر درایه بر دترمینان ($det) به دست آمد.",
                    tag = "پاسخ نهایی"
                )
            )
        } else {
            exactResultNode = MathNode.TextNode("فاقد وارون (ماتریس منفرد/تکین است)")
            approxStr = "det = 0"

            steps.add(
                SolutionStep(
                    stepNumber = stepNum,
                    titleFa = "پاسخ نهایی",
                    formula = exactResultNode,
                    explanationFa = "چون det(A) = 0 است، ماتریس وارون‌پذیر نیست (ماتریس تکین است).",
                    tag = "پاسخ نهایی"
                )
            )
        }

        return MathSolutionResult(
            problemTitleFa = "تحلیل ماتریس ۲×۲",
            initialFormula = matNode,
            steps = steps,
            exactResult = exactResultNode,
            approximateResult = approxStr,
            isExact = true,
            categoryFa = "ماتریس ۲×۲"
        )
    }

    /**
     * Solves System of 2 Linear Equations by Cramer's Rule:
     * a1*x + b1*y = c1
     * a2*x + b2*y = c2
     */
    fun solveSystem2x2(a1: Double, b1: Double, c1: Double, a2: Double, b2: Double, c2: Double): MathSolutionResult {
        val initialFormula = MathNode.TextNode(
            "⎧ ${a1}x + ${b1}y = $c1\n⎩ ${a2}x + ${b2}y = $c2"
        )

        val steps = mutableListOf<SolutionStep>()
        var stepNum = 1

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "دستگاه دو معادله و دو مجهول",
                formula = initialFormula,
                explanationFa = "دستگاه خطی را به روش کرامر (دترمینان) حل می‌کنیم.",
                tag = "فرمول اولیه"
            )
        )

        val d = a1 * b2 - a2 * b1
        val dx = c1 * b2 - c2 * b1
        val dy = a1 * c2 - a2 * c1

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "محاسبه دترمینان اصلی (D)",
                formula = MathNode.Equation(
                    MathNode.Variable("D"),
                    MathNode.Subtraction(
                        MathNode.Multiplication(MathNode.Number(a1.toString()), MathNode.Number(b2.toString())),
                        MathNode.Multiplication(MathNode.Number(a2.toString()), MathNode.Number(b1.toString()))
                    )
                ),
                explanationFa = "D = ($a1)($b2) − ($a2)($b1) = $d.",
                tag = "کرامر"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "محاسبه دترمینان‌های Dx و Dy",
                formula = MathNode.TextNode("Dx = $dx , Dy = $dy"),
                explanationFa = "برای Dx ستون ضرایب x با مقادیر ثابت جایگزین می‌شود و برای Dy ستون ضرایب y.",
                tag = "کرامر"
            )
        )

        val exactResultNode: MathNode
        val approxStr: String

        if (d != 0.0) {
            val x = dx / d
            val y = dy / d

            steps.add(
                SolutionStep(
                    stepNumber = stepNum++,
                    titleFa = "قاعده کرامر برای مجهول‌ها",
                    formula = MathNode.TextNode("x = Dx / D  ,  y = Dy / D"),
                    explanationFa = "مجهول x از نسبت Dx به D و مجهول y از نسبت Dy به D به دست می‌آید.",
                    tag = "قاعده"
                )
            )

            exactResultNode = MathNode.TextNode("x = ${String.format("%.4f", x)} , y = ${String.format("%.4f", y)}")
            approxStr = "x = $x, y = $y"

            steps.add(
                SolutionStep(
                    stepNumber = stepNum,
                    titleFa = "پاسخ نهایی",
                    formula = exactResultNode,
                    explanationFa = "دستگاه دارای جواب منحصر به‌فرد است: x = $x و y = $y.",
                    tag = "پاسخ نهایی"
                )
            )
        } else {
            if (dx == 0.0 && dy == 0.0) {
                exactResultNode = MathNode.TextNode("بی‌شمار جواب (معادلات وابسته به هم هستند)")
                approxStr = "بی‌شمار جواب"
            } else {
                exactResultNode = MathNode.TextNode("فاقد جواب (معادلات موازی و ناسازگار)")
                approxStr = "فاقد جواب"
            }

            steps.add(
                SolutionStep(
                    stepNumber = stepNum,
                    titleFa = "پاسخ نهایی",
                    formula = exactResultNode,
                    explanationFa = if (dx == 0.0) "چون همه دترمینان‌ها صفرند، خطوط منطبق بوده و دستگاه بی‌شمار جواب دارد." else "چون D=0 ولی دترمینان‌های صورت غیرصفرند، خطوط موازی بوده و دستگاه جواب ندارد.",
                    tag = "پاسخ نهایی"
                )
            )
        }

        return MathSolutionResult(
            problemTitleFa = "دستگاه معادلات خطی",
            initialFormula = initialFormula,
            steps = steps,
            exactResult = exactResultNode,
            approximateResult = approxStr,
            isExact = true,
            categoryFa = "معادلات و دستگاه"
        )
    }

    /**
     * Solves Limit: lim_{x -> a} (x^2 - a^2) / (x - a)
     * Demonstrates 0/0 indeterminate resolution by factoring!
     */
    fun solveIndeterminateLimit(a: Double): MathSolutionResult {
        val aSq = a * a
        val numNode = MathNode.Subtraction(MathNode.Power(MathNode.Variable("x"), MathNode.Number("2")), MathNode.Number(String.format("%.2f", aSq)))
        val denNode = MathNode.Subtraction(MathNode.Variable("x"), MathNode.Number(String.format("%.2f", a)))
        val fracNode = MathNode.Fraction(numNode, denNode)

        val initialFormula = MathNode.Limit(
            variable = "x",
            target = MathNode.Number(String.format("%.2f", a)),
            expr = fracNode
        )

        val steps = mutableListOf<SolutionStep>()
        var stepNum = 1

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "صورت حد",
                formula = initialFormula,
                explanationFa = "قصد داریم حد کسر را هنگام میل کردن x به سمت $a به دست آوریم.",
                tag = "فرمول اولیه"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "جای‌گذاری مستقیم و مواجهه با ابهام ۰/۰",
                formula = MathNode.Equation(
                    MathNode.Fraction(MathNode.Subtraction(MathNode.Power(MathNode.Number(a.toString()), MathNode.Number("2")), MathNode.Number(aSq.toString())), MathNode.Subtraction(MathNode.Number(a.toString()), MathNode.Number(a.toString()))),
                    MathNode.TextNode("0 / 0 (مبهم)")
                ),
                explanationFa = "با جای‌گذاری مستقیم x = $a صورت و مخرج کسر هر دو برابر صفر می‌شوند، لذا حالت مبهم صفر بر صفر به دست می‌آید.",
                tag = "ابهام ۰/۰"
            )
        )

        val factoredNum = MathNode.Multiplication(
            MathNode.Subtraction(MathNode.Variable("x"), MathNode.Number(String.format("%.2f", a))),
            MathNode.Addition(MathNode.Variable("x"), MathNode.Number(String.format("%.2f", a)))
        )

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "رفع ابهام با تجزیه اتحاد مزدوج",
                formula = MathNode.Equation(
                    numNode,
                    factoredNum
                ),
                explanationFa = "صورت کسر به کمک اتحاد مزدوج a² − b² = (a − b)(a + b) تجزیه می‌شود تا عامل صفرکننده (x − $a) مشخص گردد.",
                tag = "تجزیه جبری"
            )
        )

        val simplifiedExpr = MathNode.Addition(MathNode.Variable("x"), MathNode.Number(String.format("%.2f", a)))

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "حذف عامل صفرکننده",
                formula = MathNode.Equation(
                    fracNode,
                    simplifiedExpr
                ),
                explanationFa = "چون x به $a نزدیک می‌شود ولی دقیقاً برابر با آن نیست (x ≠ $a)، می‌توان عامل مشترک (x − $a) را از صورت و مخرج ساده کرد.",
                tag = "ساده‌سازی"
            )
        )

        val limitVal = 2 * a
        val exactResultNode = MathNode.Number(String.format("%.4f", limitVal))

        steps.add(
            SolutionStep(
                stepNumber = stepNum,
                titleFa = "پاسخ نهایی (جای‌گذاری پس از رفع ابهام)",
                formula = MathNode.Equation(
                    initialFormula,
                    exactResultNode
                ),
                explanationFa = "مقدار x = $a را در عبارت ساده‌شده قرار می‌دهیم: $a + $a = $limitVal.",
                tag = "پاسخ نهایی"
            )
        )

        return MathSolutionResult(
            problemTitleFa = "حد و رفع ابهام کسر",
            initialFormula = initialFormula,
            steps = steps,
            exactResult = exactResultNode,
            approximateResult = String.format("%.4f", limitVal),
            isExact = true,
            categoryFa = "حد و رفع ابهام"
        )
    }

    /**
     * Solves Sigma summation: Sum_{k=1}^n k^p
     */
    fun solveSigmaSummation(n: Long, power: Int): MathSolutionResult {
        if (n <= 0) throw IllegalArgumentException("n در سیگما باید بزرگتر از صفر باشد.")

        val summandNode = if (power == 1) MathNode.Variable("k") else MathNode.Power(MathNode.Variable("k"), MathNode.Number(power.toString()))
        val initialFormula = MathNode.Sigma(
            indexVar = "k",
            lower = MathNode.Number("1"),
            upper = MathNode.Number(n.toString()),
            summand = summandNode
        )

        val steps = mutableListOf<SolutionStep>()
        var stepNum = 1

        steps.add(
            SolutionStep(
                stepNumber = stepNum++,
                titleFa = "صورت نماد مجموع (سیگما)",
                formula = initialFormula,
                explanationFa = "مجموع جملات از k = 1 تا n = $n با توان $power محاسبه می‌شود.",
                tag = "فرمول اولیه"
            )
        )

        val sumBig: BigInteger
        val bN = BigInteger.valueOf(n)

        if (power == 1) {
            steps.add(
                SolutionStep(
                    stepNumber = stepNum++,
                    titleFa = "فرمول گاوس برای مجموع اعداد متوالی",
                    formula = MathNode.Equation(
                        MathNode.Sigma("k", MathNode.Number("1"), MathNode.Variable("n"), MathNode.Variable("k")),
                        MathNode.Fraction(
                            MathNode.Multiplication(MathNode.Variable("n"), MathNode.Addition(MathNode.Variable("n"), MathNode.Number("1"))),
                            MathNode.Number("2")
                        )
                    ),
                    explanationFa = "مجموع اعداد طبیعی ۱ تا n از فرمول n(n + 1) / 2 به دست می‌آید.",
                    tag = "فرمول گاوس"
                )
            )

            sumBig = bN.multiply(bN.add(BigInteger.ONE)).divide(BigInteger.valueOf(2))
        } else if (power == 2) {
            steps.add(
                SolutionStep(
                    stepNumber = stepNum++,
                    titleFa = "فرمول مجموع مربعات اعداد متوالی",
                    formula = MathNode.Equation(
                        MathNode.Sigma("k", MathNode.Number("1"), MathNode.Variable("n"), MathNode.Power(MathNode.Variable("k"), MathNode.Number("2"))),
                        MathNode.Fraction(
                            MathNode.Multiplication(
                                MathNode.Multiplication(MathNode.Variable("n"), MathNode.Addition(MathNode.Variable("n"), MathNode.Number("1"))),
                                MathNode.Addition(MathNode.Multiplication(MathNode.Number("2"), MathNode.Variable("n")), MathNode.Number("1"))
                            ),
                            MathNode.Number("6")
                        )
                    ),
                    explanationFa = "مجموع مربعات ۱² + ۲² + ... + n² از فرمول n(n + 1)(2n + 1) / 6 محاسبه می‌شود.",
                    tag = "فرمول مربعات"
                )
            )

            val part1 = bN.multiply(bN.add(BigInteger.ONE))
            val part2 = BigInteger.valueOf(2).multiply(bN).add(BigInteger.ONE)
            sumBig = part1.multiply(part2).divide(BigInteger.valueOf(6))
        } else {
            // direct sum
            var acc = BigInteger.ZERO
            for (k in 1L..n) {
                acc = acc.add(BigInteger.valueOf(k).pow(power))
            }
            sumBig = acc
        }

        val exactResultNode = MathNode.Number(sumBig.toString())

        steps.add(
            SolutionStep(
                stepNumber = stepNum,
                titleFa = "پاسخ نهایی",
                formula = exactResultNode,
                explanationFa = "حاصل کل مجموع برابر است با $sumBig.",
                tag = "پاسخ نهایی"
            )
        )

        return MathSolutionResult(
            problemTitleFa = "محاسبه سیگما و سری",
            initialFormula = initialFormula,
            steps = steps,
            exactResult = exactResultNode,
            approximateResult = sumBig.toString(),
            isExact = true,
            categoryFa = "سیگما و سری"
        )
    }
}
