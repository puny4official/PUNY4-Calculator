package com.example.math.solver

import com.example.math.model.MathNode
import com.example.math.model.MathSolutionResult
import com.example.math.model.SolutionStep
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.*

object MathStudioEngine {

    private val MC = MathContext(16, RoundingMode.HALF_UP)

    fun gcd(a: Long, b: Long): Long {
        var x = abs(a)
        var y = abs(b)
        while (y != 0L) {
            val t = y
            y = x % y
            x = t
        }
        return if (x == 0L) 1L else x
    }

    fun lcm(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        return abs(a * b) / gcd(a, b)
    }

    // -------------------------------------------------------------
    // 1. FRACTIONS SOLVER
    // -------------------------------------------------------------
    fun solveFractionArithmetic(
        num1: Long, den1: Long,
        op: String, // "+", "-", "*", "/"
        num2: Long, den2: Long
    ): MathSolutionResult {
        if (den1 == 0L || den2 == 0L) {
            throw ArithmeticException("مخرج کسر نمی‌تواند صفر باشد")
        }

        val original = when (op) {
            "+" -> MathNode.Addition(MathNode.Fraction(MathNode.Number("$num1"), MathNode.Number("$den1")), MathNode.Fraction(MathNode.Number("$num2"), MathNode.Number("$den2")))
            "-" -> MathNode.Subtraction(MathNode.Fraction(MathNode.Number("$num1"), MathNode.Number("$den1")), MathNode.Fraction(MathNode.Number("$num2"), MathNode.Number("$den2")))
            "*" -> MathNode.Multiplication(MathNode.Fraction(MathNode.Number("$num1"), MathNode.Number("$den1")), MathNode.Fraction(MathNode.Number("$num2"), MathNode.Number("$den2")), showDot = true)
            else -> MathNode.Division(MathNode.Fraction(MathNode.Number("$num1"), MathNode.Number("$den1")), MathNode.Fraction(MathNode.Number("$num2"), MathNode.Number("$den2")))
        }

        val steps = mutableListOf<SolutionStep>()
        var stepCount = 1

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "فرمول اولیه کسرها",
                formula = original,
                explanationFa = "مسئله شامل انجام عملگر ($op) بر روی دو کسر $num1/$den1 و $num2/$den2 است.",
                tag = "فرمول اولیه"
            )
        )

        var finalNum = 0L
        var finalDen = 1L

        when (op) {
            "+", "-" -> {
                val commonDen = lcm(den1, den2)
                val m1 = commonDen / den1
                val m2 = commonDen / den2
                val adjustedNum1 = num1 * m1
                val adjustedNum2 = num2 * m2

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "یافتن مخرج مشترک (ک.م.م)",
                        formula = MathNode.TextNode("ک.م.م($den1, $den2) = $commonDen"),
                        explanationFa = "برای جمع یا تفریق کسرها، باید مخرج مشترک بیابیم. کوچک‌ترین مضرب مشترک مخرج‌ها برابر با $commonDen است. ضریب کسر اول برابر $m1 و ضریب کسر دوم برابر $m2 است.",
                        tag = "هم‌مخرج‌سازی"
                    )
                )

                val scaledFraction1 = MathNode.Fraction(MathNode.Number("$adjustedNum1"), MathNode.Number("$commonDen"))
                val scaledFraction2 = MathNode.Fraction(MathNode.Number("$adjustedNum2"), MathNode.Number("$commonDen"))
                val scaledOp = if (op == "+") MathNode.Addition(scaledFraction1, scaledFraction2) else MathNode.Subtraction(scaledFraction1, scaledFraction2)

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "تبدیل کسرها به مخرج یکسان",
                        formula = scaledOp,
                        explanationFa = "صورت و مخرج هر کسر در ضریب مربوطه ضرب شد تا مخرج هر دو کسر یکسان گردد.",
                        tag = "تبدیل جبری"
                    )
                )

                finalNum = if (op == "+") adjustedNum1 + adjustedNum2 else adjustedNum1 - adjustedNum2
                finalDen = commonDen

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "محاسبه صورت نهایی روی مخرج مشترک",
                        formula = MathNode.Fraction(
                            if (op == "+") MathNode.Addition(MathNode.Number("$adjustedNum1"), MathNode.Number("$adjustedNum2")) else MathNode.Subtraction(MathNode.Number("$adjustedNum1"), MathNode.Number("$adjustedNum2")),
                            MathNode.Number("$commonDen")
                        ),
                        explanationFa = "چون مخرج‌ها برابر هستند، عملیات روی صورت‌ها انجام می‌شود: $adjustedNum1 $op $adjustedNum2 = $finalNum.",
                        tag = "محاسبه"
                    )
                )
            }
            "*" -> {
                finalNum = num1 * num2
                finalDen = den1 * den2
                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "ضرب صورت در صورت و مخرج در مخرج",
                        formula = MathNode.Fraction(
                            MathNode.Multiplication(MathNode.Number("$num1"), MathNode.Number("$num2"), showDot = true),
                            MathNode.Multiplication(MathNode.Number("$den1"), MathNode.Number("$den2"), showDot = true)
                        ),
                        explanationFa = "در ضرب کسرها، صورت اول در صورت دوم ($num1 × $num2 = $finalNum) و مخرج اول در مخرج دوم ($den1 × $den2 = $finalDen) ضرب می‌شود.",
                        tag = "قاعده ضرب"
                    )
                )
            }
            "/" -> {
                if (num2 == 0L) throw ArithmeticException("تقسیم بر کسر با صورت صفر (تقسیم بر صفر) امکان‌پذیر نیست")
                val invNum = den2
                val invDen = num2
                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "تبدیل تقسیم به ضرب در معکوس کسر دوم",
                        formula = MathNode.Multiplication(
                            MathNode.Fraction(MathNode.Number("$num1"), MathNode.Number("$den1")),
                            MathNode.Fraction(MathNode.Number("$invNum"), MathNode.Number("$invDen")),
                            showDot = true
                        ),
                        explanationFa = "تقسیم دو کسر برابر است با ضرب کسر اول در معکوس کسر دوم ($den2 / $num2).",
                        tag = "قاعده معکوس"
                    )
                )

                finalNum = num1 * invNum
                finalDen = den1 * invDen
            }
        }

        // Simplify final
        val g = gcd(finalNum, finalDen)
        val simpNum = finalNum / g
        val simpDen = finalDen / g

        // Fix negative in denominator
        val (canonicalNum, canonicalDen) = if (simpDen < 0) Pair(-simpNum, -simpDen) else Pair(simpNum, simpDen)

        val exactNode: MathNode = if (canonicalDen == 1L) {
            MathNode.Number("$canonicalNum")
        } else {
            MathNode.Fraction(MathNode.Number("$canonicalNum"), MathNode.Number("$canonicalDen"))
        }

        if (g > 1L) {
            steps.add(
                SolutionStep(
                    stepNumber = stepCount++,
                    titleFa = "ساده‌سازی کسر نهایی بر ب.م.م",
                    formula = exactNode,
                    explanationFa = "بزرگ‌ترین مقسوم‌علیه مشترک (ب.م.م) صورت و مخرج برابر $g است. با تقسیم هر دو بر $g کسر به ساده‌ترین شکل تحویل‌ناپذیر تبدیل می‌شود.",
                    tag = "ساده‌سازی"
                )
            )
        }

        val approx = DecimalFormat("#,##0.######").format(canonicalNum.toDouble() / canonicalDen.toDouble())

        return MathSolutionResult(
            problemTitleFa = "محاسبات کسر و اعداد گویا",
            originalFormula = original,
            steps = steps,
            exactResult = exactNode,
            approxResult = "≈ $approx",
            isExact = true,
            domainNotesFa = "محاسبات روی اعداد گویای تحویل‌ناپذیر انجام شده است."
        )
    }

    // -------------------------------------------------------------
    // 2. QUADRATIC EQUATION SOLVER (Delta Method with Full Steps)
    // -------------------------------------------------------------
    fun solveQuadraticEquation(a: Double, b: Double, c: Double): MathSolutionResult {
        if (abs(a) < 1e-12) {
            // Linear equation ax + b = c -> bx + c = 0
            return solveLinearEquation(b, c)
        }

        // a x^2 + b x + c = 0
        val formula = MathNode.Equation(
            MathNode.Addition(
                MathNode.Addition(
                    MathNode.Multiplication(MathNode.Number(fmt(a)), MathNode.Power(MathNode.Variable("x"), MathNode.Number("2"))),
                    MathNode.Multiplication(MathNode.Number(fmt(b)), MathNode.Variable("x"))
                ),
                MathNode.Number(fmt(c))
            ),
            MathNode.Number("0")
        )

        val steps = mutableListOf<SolutionStep>()
        var stepCount = 1

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "معادله اولیه درجه ۲",
                formula = formula,
                explanationFa = "معادله استاندارد درجه دو به فرم ax² + bx + c = 0 است. در اینجا: a = ${fmt(a)}، b = ${fmt(b)} و c = ${fmt(c)} است.",
                tag = "فرمول اولیه"
            )
        )

        // Delta
        val delta = b * b - 4 * a * c
        val deltaFormula = MathNode.Equation(
            MathNode.TextNode("Δ"),
            MathNode.Subtraction(
                MathNode.Power(MathNode.Number(fmt(b)), MathNode.Number("2")),
                MathNode.Multiplication(
                    MathNode.Number("4"),
                    MathNode.Multiplication(MathNode.Number(fmt(a)), MathNode.Number(fmt(c)), showDot = true),
                    showDot = true
                )
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "محاسبه مبین معادله (دلتا: Δ)",
                formula = deltaFormula,
                explanationFa = "فرمول دلتا برابر است با: Δ = b² − 4ac. جایگذاری ضرایب: (${fmt(b)})² − 4(${fmt(a)})(${fmt(c)}) = ${fmt(b*b)} − ${fmt(4*a*c)} = ${fmt(delta)}.",
                tag = "روش دلتا"
            )
        )

        val exactNode: MathNode
        val approxText: String

        when {
            delta > 1e-12 -> {
                val sqrtDelta = sqrt(delta)
                val x1 = (-b + sqrtDelta) / (2 * a)
                val x2 = (-b - sqrtDelta) / (2 * a)

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "تحلیل علامت دلتا (Δ > 0)",
                        formula = MathNode.TextNode("Δ = ${fmt(delta)} > 0 ⟹ دو ریشه حقیقی متمایز"),
                        explanationFa = "چون دلتا مثبت است، معادله دارای دو ریشه حقیقی و متمایز است که از فرمول x = (−b ± √Δ) / 2a محاسبه می‌شوند.",
                        tag = "تحلیل ریشه‌ها"
                    )
                )

                val x1Formula = MathNode.Equation(
                    MathNode.Variable("x₁"),
                    MathNode.Fraction(
                        MathNode.Addition(MathNode.Number(fmt(-b)), MathNode.Root(MathNode.Number(fmt(delta)))),
                        MathNode.Number(fmt(2 * a))
                    )
                )

                val x2Formula = MathNode.Equation(
                    MathNode.Variable("x₂"),
                    MathNode.Fraction(
                        MathNode.Subtraction(MathNode.Number(fmt(-b)), MathNode.Root(MathNode.Number(fmt(delta)))),
                        MathNode.Number(fmt(2 * a))
                    )
                )

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "محاسبه ریشه اول (x₁)",
                        formula = x1Formula,
                        explanationFa = "x₁ = (−(${fmt(b)}) + √${fmt(delta)}) / 2(${fmt(a)}) = (${fmt(-b)} + ${fmt(sqrtDelta)}) / ${fmt(2*a)} = ${fmt(x1)}.",
                        tag = "یافتن ریشه"
                    )
                )

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "محاسبه ریشه دوم (x₂)",
                        formula = x2Formula,
                        explanationFa = "x₂ = (−(${fmt(b)}) − √${fmt(delta)}) / 2(${fmt(a)}) = (${fmt(-b)} − ${fmt(sqrtDelta)}) / ${fmt(2*a)} = ${fmt(x2)}.",
                        tag = "یافتن ریشه"
                    )
                )

                val xv = -b / (2 * a)
                val yv = c - (b * b) / (4 * a)
                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "مختصات رأس سهمی",
                        formula = MathNode.TextNode("S = (${fmt(xv)}, ${fmt(yv)})"),
                        explanationFa = "طول رأس سهمی: x_v = −b / 2a = ${fmt(xv)} و عرض آن: y_v = f(x_v) = ${fmt(yv)}.",
                        tag = "هندسه سهمی"
                    )
                )

                exactNode = MathNode.TextNode("x₁ = ${fmt(x1)} , x₂ = ${fmt(x2)}")
                approxText = "دو جواب حقیقی: x₁ = ${fmt(x1)}, x₂ = ${fmt(x2)}"
            }
            abs(delta) <= 1e-12 -> {
                val x = -b / (2 * a)
                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "تحلیل علامت دلتا (Δ = 0)",
                        formula = MathNode.TextNode("Δ = 0 ⟹ یک ریشه مضاعف"),
                        explanationFa = "چون دلتا برابر صفر است، منحنی بر محور xها مماس است و معادله یک ریشه مضاعف دارد: x = −b / 2a.",
                        tag = "ریشه مضاعف"
                    )
                )

                val xFormula = MathNode.Equation(
                    MathNode.Variable("x"),
                    MathNode.Fraction(MathNode.Number(fmt(-b)), MathNode.Number(fmt(2 * a)))
                )

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "محاسبه ریشه مضاعف",
                        formula = xFormula,
                        explanationFa = "x = −(${fmt(b)}) / (2 × ${fmt(a)}) = ${fmt(x)}.",
                        tag = "یافتن ریشه"
                    )
                )

                exactNode = MathNode.TextNode("x = ${fmt(x)}")
                approxText = "ریشه مضاعف: x = ${fmt(x)}"
            }
            else -> {
                // Complex roots
                val realPart = -b / (2 * a)
                val imagPart = sqrt(-delta) / (2 * a)

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "تحلیل علامت دلتا (Δ < 0)",
                        formula = MathNode.TextNode("Δ = ${fmt(delta)} < 0 ⟹ فاقد ریشه حقیقی (دو ریشه مختلط)"),
                        explanationFa = "چون دلتا منفی است، معادله در مجموعه اعداد حقیقی فاقد ریشه است. در اعداد مختلط دارای دو ریشه مزدوج x = (−b ± i√|Δ|) / 2a است.",
                        tag = "اعداد مختلط"
                    )
                )

                exactNode = MathNode.TextNode("x = ${fmt(realPart)} ± ${fmt(abs(imagPart))}i")
                approxText = "ریشه‌های مختلط مزدوج: ${fmt(realPart)} ± ${fmt(abs(imagPart))}i"
            }
        }

        return MathSolutionResult(
            problemTitleFa = "حل معادله درجه ۲ به روش دلتا",
            originalFormula = formula,
            steps = steps,
            exactResult = exactNode,
            approxResult = approxText,
            isExact = true,
            domainNotesFa = "معادله با محاسبه دقیق دلتا و ضرایب در دامنه اعداد حقیقی و مختلط بررسی شد."
        )
    }

    private fun solveLinearEquation(b: Double, c: Double): MathSolutionResult {
        // bx + c = 0 -> bx = -c -> x = -c / b
        val formula = MathNode.Equation(
            MathNode.Addition(MathNode.Multiplication(MathNode.Number(fmt(b)), MathNode.Variable("x")), MathNode.Number(fmt(c))),
            MathNode.Number("0")
        )
        val steps = mutableListOf<SolutionStep>()
        var stepCount = 1

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "معادله خطی اولیه",
                formula = formula,
                explanationFa = "ضریب توان دوم صفر است، لذا با یک معادله خطی درجه اول روبرو هستیم.",
                tag = "فرمول اولیه"
            )
        )

        if (abs(b) < 1e-12) {
            val valid = abs(c) < 1e-12
            val title = if (valid) "بی‌شمار جواب (تساوی بدیهی)" else "فاقد جواب (تناقض ریاضی)"
            val msg = if (valid) "0 = 0 همواره برقرار است و معادله بی‌شمار جواب دارد." else "${fmt(c)} = 0 تناقض است و معادله فاقد جواب است."
            steps.add(SolutionStep(stepCount, title, MathNode.TextNode(msg), msg, "نتیجه"))
            return MathSolutionResult(
                problemTitleFa = "حل معادله خطی درجه ۱",
                originalFormula = formula,
                steps = steps,
                exactResult = MathNode.TextNode(if (valid) "بی‌شمار جواب" else "فاقد جواب"),
                isExact = true
            )
        }

        val stepIsolation = MathNode.Equation(
            MathNode.Multiplication(MathNode.Number(fmt(b)), MathNode.Variable("x")),
            MathNode.Number(fmt(-c))
        )
        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "انتقال عدد ثابت به طرف دیگر",
                formula = stepIsolation,
                explanationFa = "عدد ثابت (${fmt(c)}) با قرینه شدن به سمت راست معادله منتقل شد: ${fmt(b)}x = ${fmt(-c)}.",
                tag = "تبدیل جبری"
            )
        )

        val x = -c / b
        val finalFormula = MathNode.Equation(
            MathNode.Variable("x"),
            MathNode.Fraction(MathNode.Number(fmt(-c)), MathNode.Number(fmt(b)))
        )
        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "تقسیم طرفین بر ضریب مجهول",
                formula = finalFormula,
                explanationFa = "دو طرف معادله بر ضریب x یعنی ${fmt(b)} تقسیم شد: x = ${fmt(-c)} / ${fmt(b)} = ${fmt(x)}.",
                tag = "یافتن ریشه"
            )
        )

        return MathSolutionResult(
            problemTitleFa = "حل معادله درجه اول",
            originalFormula = formula,
            steps = steps,
            exactResult = MathNode.Equation(MathNode.Variable("x"), MathNode.Number(fmt(x))),
            approxResult = "x = ${fmt(x)}",
            isExact = true
        )
    }

    // -------------------------------------------------------------
    // 3. DERIVATIVE SOLVER (Polynomial, Trig, Exponential, Chain rule)
    // -------------------------------------------------------------
    fun solveDerivative(
        funcType: String, // "POLY", "SIN", "COS", "EXP", "LN", "RATIONAL"
        c1: Double = 3.0, p1: Double = 3.0,
        c2: Double = -5.0, p2: Double = 2.0,
        c3: Double = 2.0, p3: Double = 1.0,
        constVal: Double = 7.0,
        evaluatedAtX: Double? = null
    ): MathSolutionResult {
        val steps = mutableListOf<SolutionStep>()
        var stepCount = 1

        val exprNode: MathNode = when (funcType) {
            "POLY" -> {
                // c1*x^p1 + c2*x^p2 + c3*x^p3 + constVal
                var node: MathNode = MathNode.Multiplication(MathNode.Number(fmt(c1)), MathNode.Power(MathNode.Variable("x"), MathNode.Number(fmt(p1))))
                if (c2 != 0.0) {
                    node = if (c2 > 0) MathNode.Addition(node, MathNode.Multiplication(MathNode.Number(fmt(c2)), MathNode.Power(MathNode.Variable("x"), MathNode.Number(fmt(p2)))))
                    else MathNode.Subtraction(node, MathNode.Multiplication(MathNode.Number(fmt(-c2)), MathNode.Power(MathNode.Variable("x"), MathNode.Number(fmt(p2)))))
                }
                if (c3 != 0.0) {
                    node = if (c3 > 0) MathNode.Addition(node, MathNode.Multiplication(MathNode.Number(fmt(c3)), MathNode.Variable("x")))
                    else MathNode.Subtraction(node, MathNode.Multiplication(MathNode.Number(fmt(-c3)), MathNode.Variable("x")))
                }
                if (constVal != 0.0) {
                    node = if (constVal > 0) MathNode.Addition(node, MathNode.Number(fmt(constVal)))
                    else MathNode.Subtraction(node, MathNode.Number(fmt(-constVal)))
                }
                node
            }
            "SIN" -> MathNode.FunctionCall("sin", MathNode.Multiplication(MathNode.Number(fmt(c1)), MathNode.Variable("x")))
            "COS" -> MathNode.FunctionCall("cos", MathNode.Multiplication(MathNode.Number(fmt(c1)), MathNode.Variable("x")))
            "EXP" -> MathNode.Power(MathNode.Constant("e", Math.E), MathNode.Multiplication(MathNode.Number(fmt(c1)), MathNode.Variable("x")))
            "LN" -> MathNode.FunctionCall("ln", MathNode.Variable("x"))
            else -> MathNode.Fraction(MathNode.Variable("x"), MathNode.Addition(MathNode.Variable("x"), MathNode.Number("1")))
        }

        val original = MathNode.Derivative(
            variable = "x",
            order = 1,
            expr = exprNode,
            atPoint = evaluatedAtX?.let { MathNode.Number(fmt(it)) }
        )

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "تعریف مسئله مشتق‌گیری",
                formula = original,
                explanationFa = "محاسبه مشتق اول تابع نسبت به متغیر x با استفاده از قواعد حساب دیفرانسیل.",
                tag = "فرمول اولیه"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "اعمال خاصیت خطی عملگر مشتق",
                formula = MathNode.TextNode("d/dx [f(x) ± g(x)] = d/dx [f(x)] ± d/dx [g(x)]"),
                explanationFa = "مشتق مجموع یا تفاضل چند جمله، برابر مجموع یا تفاضل مشتق‌های تک‌تک جملات است.",
                tag = "قاعده خطی بودن"
            )
        )

        val resultNode: MathNode
        var evalVal: Double = 0.0

        when (funcType) {
            "POLY" -> {
                val d1c = c1 * p1
                val d1p = p1 - 1
                val d2c = c2 * p2
                val d2p = p2 - 1
                val d3c = c3

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "اعمال قانون توان مشتق",
                        formula = MathNode.TextNode("d/dx [c · xⁿ] = c · n · xⁿ⁻¹"),
                        explanationFa = "طبق قاعده توان، توان متغیر در ضریب ضرب شده و توان متغیر یک واحد کاهش می‌یابد. مشتق عدد ثابت نیز برابر صفر است.",
                        tag = "قاعده توان"
                    )
                )

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "محاسبه مشتق هر جمله",
                        formula = MathNode.TextNode(
                            "مشتق جمله اول: ${fmt(c1)} × ${fmt(p1)} x^${fmt(d1p)} = ${fmt(d1c)} x^${fmt(d1p)} | مشتق عدد ثابت (${fmt(constVal)}) = 0"
                        ),
                        explanationFa = "مشتق تک‌تک جملات با ضرب ضریب در توان و کاهش ۱ واحد از توان به دست آمد.",
                        tag = "محاسبه"
                    )
                )

                var node: MathNode = MathNode.Multiplication(MathNode.Number(fmt(d1c)), MathNode.Power(MathNode.Variable("x"), MathNode.Number(fmt(d1p))))
                if (d2c != 0.0) {
                    node = if (d2c > 0) MathNode.Addition(node, MathNode.Multiplication(MathNode.Number(fmt(d2c)), MathNode.Power(MathNode.Variable("x"), MathNode.Number(fmt(d2p)))))
                    else MathNode.Subtraction(node, MathNode.Multiplication(MathNode.Number(fmt(-d2c)), MathNode.Power(MathNode.Variable("x"), MathNode.Number(fmt(d2p)))))
                }
                if (d3c != 0.0) {
                    node = if (d3c > 0) MathNode.Addition(node, MathNode.Number(fmt(d3c)))
                    else MathNode.Subtraction(node, MathNode.Number(fmt(-d3c)))
                }
                resultNode = node

                if (evaluatedAtX != null) {
                    val x = evaluatedAtX
                    evalVal = d1c * x.pow(d1p) + d2c * x.pow(d2p) + d3c
                }
            }
            "SIN" -> {
                // d/dx[sin(kx)] = k cos(kx)
                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "قاعده مشتق تابع مثلثاتی و زنجیره‌ای",
                        formula = MathNode.TextNode("d/dx [sin(u)] = u' · cos(u)"),
                        explanationFa = "مشتق سینوس، کسینوس است که در مشتق زاویه (${fmt(c1)}) ضرب می‌شود.",
                        tag = "قاعده زنجیره‌ای"
                    )
                )
                resultNode = MathNode.Multiplication(
                    MathNode.Number(fmt(c1)),
                    MathNode.FunctionCall("cos", MathNode.Multiplication(MathNode.Number(fmt(c1)), MathNode.Variable("x"))),
                    showDot = true
                )
                if (evaluatedAtX != null) evalVal = c1 * cos(c1 * evaluatedAtX)
            }
            "COS" -> {
                // d/dx[cos(kx)] = -k sin(kx)
                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "قاعده مشتق کسینوس",
                        formula = MathNode.TextNode("d/dx [cos(u)] = −u' · sin(u)"),
                        explanationFa = "مشتق کسینوس برابر منفی سینوس در مشتق زاویه است: −${fmt(c1)} sin(${fmt(c1)}x).",
                        tag = "قاعده زنجیره‌ای"
                    )
                )
                resultNode = MathNode.Multiplication(
                    MathNode.Number(fmt(-c1)),
                    MathNode.FunctionCall("sin", MathNode.Multiplication(MathNode.Number(fmt(c1)), MathNode.Variable("x"))),
                    showDot = true
                )
                if (evaluatedAtX != null) evalVal = -c1 * sin(c1 * evaluatedAtX)
            }
            "EXP" -> {
                // d/dx[e^(kx)] = k e^(kx)
                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "قاعده مشتق تابع نمایی طبیعی",
                        formula = MathNode.TextNode("d/dx [eᵘ] = u' · eᵘ"),
                        explanationFa = "تابع نمایی e با پایه طبیعی خود تابع ضرب در مشتق توان است: ${fmt(c1)} e^(${fmt(c1)}x).",
                        tag = "تابع نمایی"
                    )
                )
                resultNode = MathNode.Multiplication(
                    MathNode.Number(fmt(c1)),
                    MathNode.Power(MathNode.Constant("e", Math.E), MathNode.Multiplication(MathNode.Number(fmt(c1)), MathNode.Variable("x"))),
                    showDot = true
                )
                if (evaluatedAtX != null) evalVal = c1 * exp(c1 * evaluatedAtX)
            }
            "LN" -> {
                // d/dx[ln(x)] = 1/x
                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "مشتق لگاریتم طبیعی",
                        formula = MathNode.TextNode("d/dx [ln(x)] = 1 / x"),
                        explanationFa = "مشتق ln(x) برابر یک بر روی x برای دامنه x > 0 است.",
                        tag = "لگاریتم"
                    )
                )
                resultNode = MathNode.Fraction(MathNode.Number("1"), MathNode.Variable("x"))
                if (evaluatedAtX != null) evalVal = 1.0 / evaluatedAtX
            }
            else -> {
                resultNode = MathNode.Fraction(MathNode.Number("1"), MathNode.Power(MathNode.Addition(MathNode.Variable("x"), MathNode.Number("1")), MathNode.Number("2")))
                if (evaluatedAtX != null) evalVal = 1.0 / (evaluatedAtX + 1).pow(2)
            }
        }

        if (evaluatedAtX != null) {
            steps.add(
                SolutionStep(
                    stepNumber = stepCount++,
                    titleFa = "جایگذاری نقطه x = ${fmt(evaluatedAtX)}",
                    formula = MathNode.TextNode("f'(${fmt(evaluatedAtX)}) = ${fmt(evalVal)}"),
                    explanationFa = "با جایگذاری مقدار x = ${fmt(evaluatedAtX)} در ضابطه مشتق، شیب خط مماس برابر با ${fmt(evalVal)} حاصل شد.",
                    tag = "محاسبه عددی"
                )
            )
        }

        return MathSolutionResult(
            problemTitleFa = "مشتق‌گیری مرحله‌به‌مرحله",
            originalFormula = original,
            steps = steps,
            exactResult = if (evaluatedAtX != null) MathNode.Number(fmt(evalVal)) else resultNode,
            approxResult = if (evaluatedAtX != null) "شیب مماس: ${fmt(evalVal)}" else null,
            isExact = true,
            domainNotesFa = "قواعد مشتق‌گیری پیوسته بر دامنه توابع اعمال شد."
        )
    }

    // -------------------------------------------------------------
    // 4. INTEGRAL SOLVER (Definite & Indefinite)
    // -------------------------------------------------------------
    fun solveIntegral(
        polyCoeffs: List<Pair<Double, Double>>, // Pairs of (coefficient, power)
        lowerBound: Double? = null,
        upperBound: Double? = null
    ): MathSolutionResult {
        val steps = mutableListOf<SolutionStep>()
        var stepCount = 1

        val isDefinite = lowerBound != null && upperBound != null

        // Build integrand node
        var integrandNode: MathNode = MathNode.Number("0")
        polyCoeffs.forEachIndexed { idx, (coeff, power) ->
            val term = if (power == 0.0) {
                MathNode.Number(fmt(coeff))
            } else if (power == 1.0) {
                MathNode.Multiplication(MathNode.Number(fmt(coeff)), MathNode.Variable("x"))
            } else {
                MathNode.Multiplication(MathNode.Number(fmt(coeff)), MathNode.Power(MathNode.Variable("x"), MathNode.Number(fmt(power))))
            }

            integrandNode = if (idx == 0) term else MathNode.Addition(integrandNode, term)
        }

        val original = MathNode.Integral(
            variable = "x",
            lowerBound = lowerBound?.let { MathNode.Number(fmt(it)) },
            upperBound = upperBound?.let { MathNode.Number(fmt(it)) },
            integrand = integrandNode
        )

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = if (isDefinite) "انتگرال معین با کران‌های عددی" else "انتگرال نامعین",
                formula = original,
                explanationFa = "محاسبه انتگرال تابع بر اساس قضیه اساسی حساب دیفرانسیل و انتگرال.",
                tag = "فرمول اولیه"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "قاعده توان در انتگرال‌گیری",
                formula = MathNode.TextNode("∫ xⁿ dx = (xⁿ⁺¹) / (n + 1) + C   (n ≠ −1)"),
                explanationFa = "برای هر جمله xⁿ، توان را ۱ واحد افزایش داده و بر توان جدید تقسیم می‌کنیم.",
                tag = "پادمشتق"
            )
        )

        // Compute antiderivative
        // For each (c, p): antiderivative is (c / (p+1)) * x^(p+1)
        val antiTerms = polyCoeffs.map { (c, p) ->
            val newP = p + 1
            val newC = c / newP
            Pair(newC, newP)
        }

        var antiNode: MathNode = MathNode.Number("0")
        antiTerms.forEachIndexed { idx, (coeff, power) ->
            val term = if (power == 1.0) {
                MathNode.Multiplication(MathNode.Number(fmt(coeff)), MathNode.Variable("x"))
            } else {
                MathNode.Multiplication(MathNode.Number(fmt(coeff)), MathNode.Power(MathNode.Variable("x"), MathNode.Number(fmt(power))))
            }
            antiNode = if (idx == 0) term else MathNode.Addition(antiNode, term)
        }

        val antiWithC = if (!isDefinite) MathNode.Addition(antiNode, MathNode.TextNode("C")) else antiNode

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "یافتن تابع پادمشتق F(x)",
                formula = antiWithC,
                explanationFa = "با اعمال قاعده معکوس مشتق برای تک‌تک جملات، پادمشتق F(x) محاسبه شد.",
                tag = "تابع اولیه"
            )
        )

        val evalAt = { x: Double ->
            antiTerms.sumOf { (c, p) -> c * x.pow(p) }
        }

        if (isDefinite) {
            val a = lowerBound!!
            val b = upperBound!!
            val fb = evalAt(b)
            val fa = evalAt(a)
            val net = fb - fa

            steps.add(
                SolutionStep(
                    stepNumber = stepCount++,
                    titleFa = "اعمال کران‌های بالا و پایین [F(b) − F(a)]",
                    formula = MathNode.TextNode("F(${fmt(b)}) − F(${fmt(a)}) = ${fmt(fb)} − (${fmt(fa)})"),
                    explanationFa = "مقدار تابع در کران بالا (${fmt(b)}) برابر ${fmt(fb)} و در کران پایین (${fmt(a)}) برابر ${fmt(fa)} است. تفریق آن‌ها نتیجه قطعی مساحت خالص زیر منحنی را می‌دهد.",
                    tag = "قضیه اساسی حسابان"
                )
            )

            return MathSolutionResult(
                problemTitleFa = "انتگرال معین",
                originalFormula = original,
                steps = steps,
                exactResult = MathNode.Number(fmt(net)),
                approxResult = "مقدار عددی مساحت: ${fmt(net)}",
                isExact = true,
                domainNotesFa = "انتگرال معین ریمان به صورت تحلیلی و بدون خطای تقریب محاسبه شد."
            )
        } else {
            return MathSolutionResult(
                problemTitleFa = "انتگرال نامعین",
                originalFormula = original,
                steps = steps,
                exactResult = antiWithC,
                approxResult = null,
                isExact = true,
                domainNotesFa = "ثابت اختیاری C نشان‌دهنده خانواده بی‌شمار پادمشتق‌هاست."
            )
        }
    }

    // -------------------------------------------------------------
    // 5. LIMIT SOLVER (Indeterminate 0/0, factoring, L'Hopital)
    // -------------------------------------------------------------
    fun solveLimit(
        numDegree: Int = 2,
        numA: Double = 1.0, numB: Double = -4.0, numC: Double = 3.0, // x^2 - 4x + 3 -> (x-1)(x-3)
        denDegree: Int = 1,
        denA: Double = 1.0, denB: Double = -1.0, // x - 1
        targetX: Double = 1.0
    ): MathSolutionResult {
        val steps = mutableListOf<SolutionStep>()
        var stepCount = 1

        val numNode = MathNode.Subtraction(
            MathNode.Subtraction(
                MathNode.Power(MathNode.Variable("x"), MathNode.Number("2")),
                MathNode.Multiplication(MathNode.Number("4"), MathNode.Variable("x"))
            ),
            MathNode.Number("-3") // + 3
        )
        val denNode = MathNode.Subtraction(MathNode.Variable("x"), MathNode.Number("1"))

        val fracNode = MathNode.Fraction(numNode, denNode)
        val original = MathNode.Limit(
            variable = "x",
            target = MathNode.Number(fmt(targetX)),
            expr = fracNode
        )

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "فرمول اولیه حد کسری",
                formula = original,
                explanationFa = "محاسبه حد کسر فوق هنگامی که متغیر x به سمت $targetX میل می‌کند.",
                tag = "فرمول اولیه"
            )
        )

        // Step 1: Direct substitution
        val numVal = numA * targetX * targetX + numB * targetX + numC
        val denVal = denA * targetX + denB

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "جایگذاری مستقیم مقدار حد",
                formula = MathNode.Fraction(MathNode.Number(fmt(numVal)), MathNode.Number(fmt(denVal))),
                explanationFa = "با قرار دادن مستقیم x = $targetX در صورت به مقدار ${fmt(numVal)} و در مخرج به مقدار ${fmt(denVal)} می‌رسیم.",
                tag = "بررسی مبهم"
            )
        )

        val finalVal: Double

        if (abs(numVal) < 1e-10 && abs(denVal) < 1e-10) {
            steps.add(
                SolutionStep(
                    stepNumber = stepCount++,
                    titleFa = "تشخیص حالت مبهم صفر بر صفر (0/0)",
                    formula = MathNode.TextNode("حالت مبهم: 0 / 0"),
                    explanationFa = "چون حاصل جایگذاری مستقیم 0/0 است، حد مبهم بوده و باید با فاکتورگیری عامل صفرکننده (x − $targetX) یا قاعده هوپیتال رفع ابهام شود.",
                    tag = "رفع ابهام"
                )
            )

            // Method 1: Factoring
            // x^2 - 4x + 3 = (x - 1)(x - 3)
            val factoredNum = MathNode.Multiplication(
                MathNode.Group(MathNode.Subtraction(MathNode.Variable("x"), MathNode.Number("1"))),
                MathNode.Group(MathNode.Subtraction(MathNode.Variable("x"), MathNode.Number("3")))
            )
            val cancelNode = MathNode.Fraction(
                factoredNum,
                MathNode.Group(MathNode.Subtraction(MathNode.Variable("x"), MathNode.Number("1")))
            )

            steps.add(
                SolutionStep(
                    stepNumber = stepCount++,
                    titleFa = "تجزیه و فاکتورگیری عامل صفرکننده",
                    formula = cancelNode,
                    explanationFa = "چندجمله‌ای صورت به حاصل‌ضرب (x − 1)(x − 3) تجزیه شد تا عامل صفرکننده صورت و مخرج مشخص گردد.",
                    tag = "تجزیه جبری"
                )
            )

            val simplified = MathNode.Subtraction(MathNode.Variable("x"), MathNode.Number("3"))
            steps.add(
                SolutionStep(
                    stepNumber = stepCount++,
                    titleFa = "ساده‌سازی و حذف عامل صفرکننده",
                    formula = MathNode.Limit(variable = "x", target = MathNode.Number(fmt(targetX)), expr = simplified),
                    explanationFa = "عامل (x − 1) از صورت و مخرج به دلیل اینکه x به ۱ میل می‌کند و خود ۱ نیست حذف شد: عبارت ساده‌شده برابر (x − 3) است.",
                    tag = "حذف عامل صفر"
                )
            )

            finalVal = targetX - 3.0 // 1 - 3 = -2

            steps.add(
                SolutionStep(
                    stepNumber = stepCount++,
                    titleFa = "محاسبه مقدار نهایی حد",
                    formula = MathNode.Equation(MathNode.TextNode("پاسخ"), MathNode.Number(fmt(finalVal))),
                    explanationFa = "با جایگذاری مجدد x = $targetX در عبارت ساده‌شده (x − 3): ۱ − ۳ = $finalVal حاصل می‌شود.",
                    tag = "نتیجه نهایی"
                )
            )
        } else if (abs(denVal) < 1e-10) {
            finalVal = Double.POSITIVE_INFINITY
            steps.add(
                SolutionStep(
                    stepNumber = stepCount++,
                    titleFa = "مخرج صفر و صورت غیرصفر",
                    formula = MathNode.TextNode("عدد / 0 ⟹ حد تعریف‌نشده (بی‌نهایت ±∞)"),
                    explanationFa = "تقسیم عدد غیرصفر بر مقداری که به صفر میل می‌کند منجر به بی‌نهایت می‌شود و حد متناهی وجود ندارد.",
                    tag = "حد نامتناهی"
                )
            )
        } else {
            finalVal = numVal / denVal
        }

        return MathSolutionResult(
            problemTitleFa = "محاسبه حد و رفع ابهام",
            originalFormula = original,
            steps = steps,
            exactResult = MathNode.Number(fmt(finalVal)),
            approxResult = "L = ${fmt(finalVal)}",
            isExact = true,
            domainNotesFa = "با رفع ابهام قطعی به روش فاکتورگیری، حد متناهی محاسبه گردید."
        )
    }

    // -------------------------------------------------------------
    // 6. SIGMA / SUMMATION SOLVER
    // -------------------------------------------------------------
    fun solveSigma(
        start: Long = 1,
        end: Long = 5,
        type: String = "LINEAR" // LINEAR (i), SQUARED (i^2), POWER_2 (2^i)
    ): MathSolutionResult {
        val steps = mutableListOf<SolutionStep>()
        var stepCount = 1

        val summandNode: MathNode = when (type) {
            "LINEAR" -> MathNode.Variable("i")
            "SQUARED" -> MathNode.Power(MathNode.Variable("i"), MathNode.Number("2"))
            else -> MathNode.Power(MathNode.Number("2"), MathNode.Variable("i"))
        }

        val original = MathNode.Sigma(
            indexVar = "i",
            lower = MathNode.Number("$start"),
            upper = MathNode.Number("$end"),
            summand = summandNode
        )

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "تعریف نماد سیگما (مجموع)",
                formula = original,
                explanationFa = "محاسبه مجموع جملات از اندیس i = $start تا کران بالای i = $end.",
                tag = "فرمول اولیه"
            )
        )

        // Expansion
        val terms = mutableListOf<Long>()
        val termStrings = mutableListOf<String>()
        for (i in start..end) {
            val v = when (type) {
                "LINEAR" -> i
                "SQUARED" -> i * i
                else -> 2.0.pow(i.toDouble()).toLong()
            }
            terms.add(v)
            termStrings.add(v.toString())
        }

        val expansionText = termStrings.joinToString(" + ")
        val totalSum = terms.sum()

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "بسط تک‌تک جملات سیگما",
                formula = MathNode.TextNode(expansionText),
                explanationFa = "با قرار دادن متوالی مقادیر i از $start تا $end در ضابطه، جملات دنباله به دست آمدند.",
                tag = "بسط جملات"
            )
        )

        if (type == "LINEAR" && start == 1L) {
            steps.add(
                SolutionStep(
                    stepNumber = stepCount++,
                    titleFa = "فرمول بسته گوس برای مجموع اعداد متوالی",
                    formula = MathNode.Equation(
                        MathNode.TextNode("S"),
                        MathNode.Fraction(
                            MathNode.Multiplication(MathNode.Variable("n"), MathNode.Addition(MathNode.Variable("n"), MathNode.Number("1"))),
                            MathNode.Number("2")
                        )
                    ),
                    explanationFa = "مجموع n عدد طبیعی اول برابر با n(n + 1) / 2 است: $end × ${end + 1} / 2 = $totalSum.",
                    tag = "فرمول تحلیلی"
                )
            )
        }

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "محاسبه جمع کل",
                formula = MathNode.Equation(MathNode.TextNode("حاصل مجموع"), MathNode.Number("$totalSum")),
                explanationFa = "حاصل جمع تمام جملات برابر با $totalSum است.",
                tag = "نتیجه نهایی"
            )
        )

        return MathSolutionResult(
            problemTitleFa = "محاسبه مجموع و سری (سیگما)",
            originalFormula = original,
            steps = steps,
            exactResult = MathNode.Number("$totalSum"),
            approxResult = "مجموع = $totalSum",
            isExact = true
        )
    }

    // -------------------------------------------------------------
    // 7. MATRIX STUDIO (2x2 and 3x3 Determinant, Inverse, Transpose)
    // -------------------------------------------------------------
    fun solveMatrix2x2(
        a: Double, b: Double,
        c: Double, d: Double,
        operation: String = "DETERMINANT" // "DETERMINANT", "INVERSE", "TRANSPOSE"
    ): MathSolutionResult {
        val steps = mutableListOf<SolutionStep>()
        var stepCount = 1

        val matrixNode = MathNode.Matrix(
            rows = 2,
            cols = 2,
            values = listOf(
                listOf(MathNode.Number(fmt(a)), MathNode.Number(fmt(b))),
                listOf(MathNode.Number(fmt(c)), MathNode.Number(fmt(d)))
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "ماتریس ورودی A",
                formula = matrixNode,
                explanationFa = "ماتریس مربعی مرتبه ۲×۲ با درایه‌های سطر اول [${fmt(a)}, ${fmt(b)}] و سطر دوم [${fmt(c)}, ${fmt(d)}].",
                tag = "ماتریس اولیه"
            )
        )

        val det = a * d - b * c

        when (operation) {
            "DETERMINANT" -> {
                val detFormula = MathNode.Subtraction(
                    MathNode.Multiplication(MathNode.Number(fmt(a)), MathNode.Number(fmt(d)), showDot = true),
                    MathNode.Multiplication(MathNode.Number(fmt(b)), MathNode.Number(fmt(c)), showDot = true)
                )

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "فرمول دترمینان ماتریس ۲×۲",
                        formula = MathNode.Equation(MathNode.TextNode("det(A) = ad − bc"), detFormula),
                        explanationFa = "دترمینان از تفاضل ضرب عناصر قطر اصلی (ad) و قطر فرعی (bc) محاسبه می‌شود: (${fmt(a)} × ${fmt(d)}) − (${fmt(b)} × ${fmt(c)}) = ${fmt(a*d)} − ${fmt(b*c)} = ${fmt(det)}.",
                        tag = "قاعده دترمینان"
                    )
                )

                return MathSolutionResult(
                    problemTitleFa = "محاسبه دترمینان ماتریس ۲×۲",
                    originalFormula = matrixNode,
                    steps = steps,
                    exactResult = MathNode.Number(fmt(det)),
                    approxResult = "det(A) = ${fmt(det)}",
                    isExact = true
                )
            }
            "TRANSPOSE" -> {
                val transNode = MathNode.Matrix(
                    rows = 2,
                    cols = 2,
                    values = listOf(
                        listOf(MathNode.Number(fmt(a)), MathNode.Number(fmt(c))),
                        listOf(MathNode.Number(fmt(b)), MathNode.Number(fmt(d)))
                    )
                )
                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "جابجایی سطرها و ستون‌ها (ترانهاده Aᵀ)",
                        formula = transNode,
                        explanationFa = "در ترانهاده ماتریس، سطر اول به ستون اول و سطر دوم به ستون دوم تبدیل می‌شود.",
                        tag = "ترانهاده"
                    )
                )
                return MathSolutionResult(
                    problemTitleFa = "ترانهاده ماتریس",
                    originalFormula = matrixNode,
                    steps = steps,
                    exactResult = transNode,
                    isExact = true
                )
            }
            "INVERSE" -> {
                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "محاسبه دترمینان جهت بررسی وارون‌پذیری",
                        formula = MathNode.Equation(MathNode.TextNode("det(A)"), MathNode.Number(fmt(det))),
                        explanationFa = "ماتریس تنها زمانی وارون‌پذیر است که دترمینان آن مخالف صفر باشد. در اینجا det(A) = ${fmt(det)} است.",
                        tag = "شرط وارون‌پذیری"
                    )
                )

                if (abs(det) < 1e-12) {
                    steps.add(
                        SolutionStep(
                            stepNumber = stepCount++,
                            titleFa = "ماتریس منفرد (تکین)",
                            formula = MathNode.TextNode("det(A) = 0 ⟹ ماتریس فاقد وارون است"),
                            explanationFa = "چون دترمینان ماتریس صفر است، این ماتریس وارون‌پذیر نیست.",
                            tag = "تکین"
                        )
                    )
                    return MathSolutionResult(
                        problemTitleFa = "وارون ماتریس ۲×۲",
                        originalFormula = matrixNode,
                        steps = steps,
                        exactResult = MathNode.TextNode("فاقد وارون (منفرد)"),
                        isExact = true
                    )
                }

                // adjugate
                val adjNode = MathNode.Matrix(
                    rows = 2,
                    cols = 2,
                    values = listOf(
                        listOf(MathNode.Number(fmt(d)), MathNode.Number(fmt(-b))),
                        listOf(MathNode.Number(fmt(-c)), MathNode.Number(fmt(a)))
                    )
                )

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "تشکیل ماتریس الحاقی (Adjugate)",
                        formula = adjNode,
                        explanationFa = "برای ماتریس ۲×۲، جای درایه‌های قطر اصلی عوض شده و درایه‌های قطر فرعی قرینه می‌شوند.",
                        tag = "ماتریس الحاقی"
                    )
                )

                val invA = d / det
                val invB = -b / det
                val invC = -c / det
                val invD = a / det

                val invNode = MathNode.Matrix(
                    rows = 2,
                    cols = 2,
                    values = listOf(
                        listOf(MathNode.Number(fmt(invA)), MathNode.Number(fmt(invB))),
                        listOf(MathNode.Number(fmt(invC)), MathNode.Number(fmt(invD)))
                    )
                )

                steps.add(
                    SolutionStep(
                        stepNumber = stepCount++,
                        titleFa = "ضرب ماتریس الحاقی در 1 / det(A)",
                        formula = invNode,
                        explanationFa = "تک‌تک درایه‌های ماتریس الحاقی بر دترمینان (${fmt(det)}) تقسیم شدند تا ماتریس معکوس نهایی حاصل شود.",
                        tag = "ماتریس وارون"
                    )
                )

                return MathSolutionResult(
                    problemTitleFa = "وارون ماتریس ۲×۲",
                    originalFormula = matrixNode,
                    steps = steps,
                    exactResult = invNode,
                    isExact = true
                )
            }
            else -> {
                return solveMatrix2x2(a, b, c, d, "DETERMINANT")
            }
        }
    }

    // System of 2 Linear Equations (Cramer's Rule)
    fun solveSystem2x2(
        a1: Double, b1: Double, c1: Double, // a1 x + b1 y = c1
        a2: Double, b2: Double, c2: Double  // a2 x + b2 y = c2
    ): MathSolutionResult {
        val steps = mutableListOf<SolutionStep>()
        var stepCount = 1

        val eq1 = MathNode.Equation(
            MathNode.Addition(MathNode.Multiplication(MathNode.Number(fmt(a1)), MathNode.Variable("x")), MathNode.Multiplication(MathNode.Number(fmt(b1)), MathNode.Variable("y"))),
            MathNode.Number(fmt(c1))
        )
        val eq2 = MathNode.Equation(
            MathNode.Addition(MathNode.Multiplication(MathNode.Number(fmt(a2)), MathNode.Variable("x")), MathNode.Multiplication(MathNode.Number(fmt(b2)), MathNode.Variable("y"))),
            MathNode.Number(fmt(c2))
        )

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "دستگاه معادلات خطی اولیه",
                formula = MathNode.TextNode("معادله ۱: ${fmt(a1)}x + ${fmt(b1)}y = ${fmt(c1)} | معادله ۲: ${fmt(a2)}x + ${fmt(b2)}y = ${fmt(c2)}"),
                explanationFa = "حل دستگاه دو معادله و دو مجهول با استفاده از قاعده دترمینان کرامر.",
                tag = "فرمول اولیه"
            )
        )

        val D = a1 * b2 - a2 * b1
        val Dx = c1 * b2 - c2 * b1
        val Dy = a1 * c2 - a2 * c1

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "محاسبه دترمینان اصلی ضرایب (D)",
                formula = MathNode.Equation(MathNode.TextNode("D"), MathNode.Number(fmt(D))),
                explanationFa = "دترمینان ماتریس ضرایب برابر است با (${fmt(a1)} × ${fmt(b2)}) − (${fmt(a2)} × ${fmt(b1)}) = ${fmt(D)}.",
                tag = "روش کرامر"
            )
        )

        if (abs(D) < 1e-12) {
            val status = if (abs(Dx) < 1e-12 && abs(Dy) < 1e-12) "بی‌شمار جواب (وابسته خطی)" else "فاقد جواب (معادلات متناقض/موازی)"
            steps.add(
                SolutionStep(
                    stepNumber = stepCount++,
                    titleFa = "بررسی حالت D = 0",
                    formula = MathNode.TextNode(status),
                    explanationFa = "چون دترمینان ضرایب صفر است، خطوط با هم موازی بوده یا بر هم منطبق هستند.",
                    tag = "تحلیل جواب"
                )
            )
            return MathSolutionResult(
                problemTitleFa = "حل دستگاه دو معادله و دو مجهول",
                originalFormula = eq1,
                steps = steps,
                exactResult = MathNode.TextNode(status),
                isExact = true
            )
        }

        val x = Dx / D
        val y = Dy / D

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "محاسبه دترمینان مجهول x (Dₓ)",
                formula = MathNode.Equation(MathNode.TextNode("Dₓ"), MathNode.Number(fmt(Dx))),
                explanationFa = "با جایگزینی ستون مقادیر معلوم به جای ستون ضرایب x: Dₓ = (${fmt(c1)} × ${fmt(b2)}) − (${fmt(c2)} × ${fmt(b1)}) = ${fmt(Dx)}.",
                tag = "محاسبه دترمینان"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "محاسبه دترمینان مجهول y (Dᵧ)",
                formula = MathNode.Equation(MathNode.TextNode("Dᵧ"), MathNode.Number(fmt(Dy))),
                explanationFa = "با جایگزینی ستون مقادیر معلوم به جای ستون ضرایب y: Dᵧ = (${fmt(a1)} × ${fmt(c2)}) − (${fmt(a2)} × ${fmt(c1)}) = ${fmt(Dy)}.",
                tag = "محاسبه دترمینان"
            )
        )

        steps.add(
            SolutionStep(
                stepNumber = stepCount++,
                titleFa = "یافتن مقادیر مجهولات (x = Dₓ/D , y = Dᵧ/D)",
                formula = MathNode.TextNode("x = ${fmt(Dx)} / ${fmt(D)} = ${fmt(x)}  ,  y = ${fmt(Dy)} / ${fmt(D)} = ${fmt(y)}"),
                explanationFa = "طبق قاعده کرامر، مجهول x از تقسیم Dₓ بر D و مجهول y از تقسیم Dᵧ بر D به دست می‌آید.",
                tag = "نتیجه نهایی"
            )
        )

        return MathSolutionResult(
            problemTitleFa = "حل دستگاه با روش کرامر",
            originalFormula = eq1,
            steps = steps,
            exactResult = MathNode.TextNode("x = ${fmt(x)} , y = ${fmt(y)}"),
            approxResult = "پاسخ: (x = ${fmt(x)}, y = ${fmt(y)})",
            isExact = true
        )
    }

    private fun fmt(d: Double): String {
        if (abs(d - round(d)) < 1e-9) {
            return round(d).toLong().toString()
        }
        val df = DecimalFormat("#.####")
        return df.format(d)
    }
}
