package com.example.math.solver

import com.example.math.model.MathNode
import com.example.math.model.MathSolutionResult
import com.example.math.model.SolutionStep
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

/** Exact rational number (always reduced, denominator > 0). */
class Fr private constructor(val n: BigInteger, val d: BigInteger) {
    companion object {
        val ZERO = of(BigInteger.ZERO, BigInteger.ONE)
        val ONE = of(BigInteger.ONE, BigInteger.ONE)
        fun of(n: BigInteger, d: BigInteger): Fr {
            require(d.signum() != 0)
            var nn = n; var dd = d
            if (dd.signum() < 0) { nn = nn.negate(); dd = dd.negate() }
            val g = nn.gcd(dd)
            if (g.signum() != 0 && g != BigInteger.ONE) { nn = nn.divide(g); dd = dd.divide(g) }
            if (nn.signum() == 0) dd = BigInteger.ONE
            return Fr(nn, dd)
        }
        fun of(v: Long) = of(BigInteger.valueOf(v), BigInteger.ONE)

        /** Parses "2", "-3.5", "3/4", "٣" style input. Returns null if not a number. */
        fun parse(raw: String): Fr? {
            var t = raw.trim().replace('−', '-').replace('٫', '.').replace(',', '.')
            t = t.map { ch -> if (ch in '٠'..'٩') ('0' + (ch - '٠')) else if (ch in '۰'..'۹') ('0' + (ch - '۰')) else ch }.joinToString("")
            if (t.isEmpty()) return null
            return try {
                if (t.contains('/')) {
                    val parts = t.split('/')
                    if (parts.size != 2) return null
                    val a = parse(parts[0]) ?: return null
                    val b = parse(parts[1]) ?: return null
                    if (b.isZero()) null else a.div(b)
                } else {
                    val bd = BigDecimal(t)
                    val scale = bd.scale()
                    if (scale > 0) of(bd.unscaledValue(), BigInteger.TEN.pow(scale))
                    else of(bd.toBigIntegerExact(), BigInteger.ONE)
                }
            } catch (_: Exception) { null }
        }
    }

    fun isZero() = n.signum() == 0
    fun signum() = n.signum()
    fun isInt() = d == BigInteger.ONE
    operator fun plus(o: Fr) = of(n * o.d + o.n * d, d * o.d)
    operator fun minus(o: Fr) = of(n * o.d - o.n * d, d * o.d)
    operator fun times(o: Fr) = of(n * o.n, d * o.d)
    fun div(o: Fr) = of(n * o.d, d * o.n)
    operator fun unaryMinus() = of(n.negate(), d)
    fun abs() = of(n.abs(), d)
    fun isOne() = n == BigInteger.ONE && d == BigInteger.ONE
    fun isMinusOne() = n == BigInteger.ONE.negate() && d == BigInteger.ONE
    override fun equals(other: Any?) = other is Fr && n == other.n && d == other.d
    override fun hashCode() = n.hashCode() * 31 + d.hashCode()

    /** "-3", "3/4" with a real minus sign. */
    override fun toString(): String = (if (isInt()) n.toString() else "$n/$d").replace("-", "−")

    /** Same, but wrapped in parentheses when negative or fractional (for use inside products). */
    fun p(): String = if (signum() < 0 || !isInt()) "($this)" else toString()

    fun approx(): String {
        if (isInt()) return n.toString().replace("-", "−")
        val v = BigDecimal(n).divide(BigDecimal(d), MathContext(8, RoundingMode.HALF_UP)).stripTrailingZeros().toPlainString()
        return v.replace("-", "−")
    }
}

object LinearSystemSolver {

    /** Text of  a·x + b·y = c  . [showZero] keeps terms with a zero coefficient (as typed). */
    fun equationText(a: Fr, b: Fr, c: Fr, showZero: Boolean = false): String {
        fun coef(f: Fr, v: String): String = when {
            f.isOne() -> v
            f.isMinusOne() -> "−$v"
            f.isInt() -> "$f$v"
            else -> "(${f})$v"
        }
        val sb = StringBuilder()
        val hasX = showZero || !a.isZero()
        val hasY = showZero || !b.isZero()
        if (hasX) sb.append(coef(a, "x"))
        if (hasY) {
            if (hasX) {
                sb.append(if (b.signum() < 0) " − " else " + ")
                sb.append(coef(b.abs(), "y"))
            } else sb.append(coef(b, "y"))
        }
        if (!hasX && !hasY) sb.append("0")
        sb.append(" = ").append(c.toString())
        return sb.toString()
    }

    private fun lcmDen(vararg fs: Fr): BigInteger =
        fs.fold(BigInteger.ONE) { acc, f -> acc * f.d / acc.gcd(f.d) }

    fun solve(a1: Fr, b1: Fr, c1: Fr, a2: Fr, b2: Fr, c2: Fr): MathSolutionResult {
        val steps = mutableListOf<SolutionStep>()
        var k = 1
        fun add(title: String, formula: String, expl: String, tag: String) {
            steps.add(SolutionStep(k++, title, MathNode.TextNode(formula), expl, tag))
        }

        val e1o = equationText(a1, b1, c1, true)
        val e2o = equationText(a2, b2, c2, true)
        val initial = MathNode.TextNode("$e1o     ،     $e2o")

        add(
            "صورت مسئله", "$e1o\n$e2o",
            "دستگاه دو معادلهٔ دو مجهولی داریم. دو مجهول x و y هستند. هر جا ضریب x یا y ننوشته باشد، ضریبش ۱ است. هدف پیدا کردن x و y است به طوری که در هر دو معادله صدق کنند.",
            "فرمول اولیه"
        )

        // --- clear denominators ---
        var A1 = a1; var B1 = b1; var C1 = c1; var A2 = a2; var B2 = b2; var C2 = c2
        val m1 = lcmDen(a1, b1, c1)
        val m2 = lcmDen(a2, b2, c2)
        if (m1 != BigInteger.ONE || m2 != BigInteger.ONE) {
            val f1 = Fr.of(m1, BigInteger.ONE); val f2 = Fr.of(m2, BigInteger.ONE)
            A1 = a1 * f1; B1 = b1 * f1; C1 = c1 * f1
            A2 = a2 * f2; B2 = b2 * f2; C2 = c2 * f2
            val lines = mutableListOf<String>()
            if (m1 != BigInteger.ONE) lines.add("معادلهٔ اول را در $m1 ضرب می‌کنیم: ${equationText(A1, B1, C1)}")
            if (m2 != BigInteger.ONE) lines.add("معادلهٔ دوم را در $m2 ضرب می‌کنیم: ${equationText(A2, B2, C2)}")
            add(
                "حذف کسرها (اعداد صحیح کردن ضریب‌ها)",
                "${equationText(A1, B1, C1)}\n${equationText(A2, B2, C2)}",
                "برای راحتی کار، ضریب‌های کسری یا اعشاری را با ضرب کردن هر معادله در مخرج مشترک (ک.م.م، کوچک‌ترین مخرج مشترک) از بین می‌بریم. ضرب دو طرف یک معادله در یک عدد غیر صفر، جواب را تغییر نمی‌دهد.\n" + lines.joinToString("\n"),
                "آماده‌سازی"
            )
        }

        // --- determinant ---
        val D = A1 * B2 - A2 * B1
        add(
            "بررسی تعداد جواب‌ها (دترمینان)",
            "D = a₁b₂ − a₂b₁ = ${A1.p()} × ${B2.p()} − ${A2.p()} × ${B1.p()} = $D",
            if (D.isZero())
                "مقدار D برابر صفر شد؛ یعنی دو معادله «موازی» یا «هم‌راستا» هستند و دستگاه جواب یکتا ندارد. در ادامه بررسی می‌کنیم که جواب ندارد یا بی‌شمار جواب دارد."
            else
                "D = ضریب x در معادلهٔ اول × ضریب y در معادلهٔ دوم − ضریب x در معادلهٔ دوم × ضریب y در معادلهٔ اول. چون D مخالف صفر است، دستگاه دقیقاً یک جواب دارد.",
            "بررسی"
        )

        if (D.isZero()) return degenerate(steps, k, initial, A1, B1, C1, A2, B2, C2)

        // --- eliminate ---
        val y: Fr
        if (A1.isZero() || A2.isZero()) {
            val useFirst = A1.isZero()
            val B = if (useFirst) B1 else B2
            val C = if (useFirst) C1 else C2
            y = C.div(B)
            add(
                "پیدا کردن y",
                "${equationText(if (useFirst) A1 else A2, B, C)}\ny = ${C.p()} ÷ ${B.p()} = $y",
                "در معادلهٔ ${if (useFirst) "اول" else "دوم"} ضریب x صفر است، پس x در آن وجود ندارد و y مستقیم به دست می‌آید: ${B.p()} × y = $C، بنابراین y = $C ÷ ${B.p()} = $y.",
                "محاسبه"
            )
        } else {
            val mu1 = A2; val mu2 = A1
            val ea1 = A1 * mu1; val eb1 = B1 * mu1; val ec1 = C1 * mu1
            val ea2 = A2 * mu2; val eb2 = B2 * mu2; val ec2 = C2 * mu2
            add(
                "برابر کردن ضریب x در دو معادله",
                "${equationText(ea1, eb1, ec1)}\n${equationText(ea2, eb2, ec2)}",
                "می‌خواهیم x را حذف کنیم. ضریب x در معادلهٔ اول $A1 و در دومی $A2 است. معادلهٔ اول را در ${mu1.p()} و معادلهٔ دوم را در ${mu2.p()} ضرب می‌کنیم تا ضریب x در هر دو برابر $ea1 شود:\nمعادلهٔ اول × ${mu1.p()}: ${equationText(ea1, eb1, ec1)}\nمعادلهٔ دوم × ${mu2.p()}: ${equationText(ea2, eb2, ec2)}",
                "حذف مجهول"
            )
            val rb = eb1 - eb2
            val rc = ec1 - ec2
            add(
                "کم کردن دو معادله و حذف x",
                "(${eb1}) y − (${eb2}) y = $ec1 − ($ec2)\n${rb} y = $rc",
                "حالا معادلهٔ دوم را از اول کم می‌کنیم. جمله‌های x با هم صفر می‌شوند:\nضریب y: $eb1 − (${eb2}) = $rb\nطرف راست: $ec1 − (${ec2}) = $rc\nپس ${rb}y = $rc.",
                "حذف مجهول"
            )
            y = rc.div(rb)
            add(
                "پیدا کردن y",
                "y = ${rc.p()} ÷ ${rb.p()} = $y",
                "دو طرف را بر ضریب y یعنی ${rb.p()} تقسیم می‌کنیم: y = $rc ÷ ${rb.p()} = $y." +
                    if (!y.isInt()) " (کسر را تا جای ممکن ساده کرده‌ایم؛ تقریباً ${y.approx()})" else "",
                "محاسبه"
            )
        }

        // --- find x by substitution ---
        val useFirstForX = !A1.isZero()
        val Ax = if (useFirstForX) A1 else A2
        val Bx = if (useFirstForX) B1 else B2
        val Cx = if (useFirstForX) C1 else C2
        val by = Bx * y
        val rhs = Cx - by
        val x = rhs.div(Ax)
        add(
            "جای‌گذاری y و پیدا کردن x",
            "${equationText(Ax, Bx, Cx)}\n${Ax.p()} x + ${Bx.p()} × ${y.p()} = $Cx\n${Ax.p()} x = $Cx − (${by}) = $rhs\nx = ${rhs.p()} ÷ ${Ax.p()} = $x",
            "مقدار y را در معادلهٔ ${if (useFirstForX) "اول" else "دوم"} می‌گذاریم:\n${Ax.p()}x + ${Bx.p()} × ${y.p()} = $Cx\n${Bx.p()} × ${y.p()} = $by پس ${Ax.p()}x = $Cx − (${by}) = $rhs\nدو طرف را بر ${Ax.p()} تقسیم می‌کنیم: x = $rhs ÷ ${Ax.p()} = $x.",
            "جای‌گذاری"
        )

        // --- verification with the original equations ---
        val l1 = a1 * x + b1 * y
        val l2 = a2 * x + b2 * y
        add(
            "بررسی جواب",
            "${a1.p()} × ${x.p()} + ${b1.p()} × ${y.p()} = $l1\n${a2.p()} × ${x.p()} + ${b2.p()} × ${y.p()} = $l2",
            "برای مطمئن شدن، x = $x و y = $y را در دو معادلهٔ اصلی می‌گذاریم:\nمعادلهٔ اول: ${a1.p()} × ${x.p()} + ${b1.p()} × ${y.p()} = $l1 ${if (l1 == c1) "✓ (برابر $c1 است)" else "✗"}\nمعادلهٔ دوم: ${a2.p()} × ${x.p()} + ${b2.p()} × ${y.p()} = $l2 ${if (l2 == c2) "✓ (برابر $c2 است)" else "✗"}",
            "راستی‌آزمایی"
        )

        val ans = "x = $x ،  y = $y"
        add(
            "پاسخ نهایی", "x = $x\ny = $y",
            "جواب دقیق دستگاه: x = $x و y = $y" + if (!x.isInt() || !y.isInt()) " (تقریباً x ≈ ${x.approx()} و y ≈ ${y.approx()})." else ".",
            "پاسخ نهایی"
        )
        return MathSolutionResult("حل دستگاه معادلات خطی", initial, steps, MathNode.TextNode(ans), "x ≈ ${x.approx()}, y ≈ ${y.approx()}", true, "دستگاه معادلات")
    }

    private fun degenerate(
        steps: MutableList<SolutionStep>, startK: Int, initial: MathNode,
        A1: Fr, B1: Fr, C1: Fr, A2: Fr, B2: Fr, C2: Fr
    ): MathSolutionResult {
        var k = startK
        fun add(title: String, formula: String, expl: String, tag: String) {
            steps.add(SolutionStep(k++, title, MathNode.TextNode(formula), expl, tag))
        }
        val zero1 = A1.isZero() && B1.isZero()
        val zero2 = A2.isZero() && B2.isZero()

        val inconsistent: Boolean
        val why: String
        when {
            zero1 && !C1.isZero() -> { inconsistent = true; why = "معادلهٔ اول به شکل 0 = $C1 درمی‌آید که هرگز درست نیست." }
            zero2 && !C2.isZero() -> { inconsistent = true; why = "معادلهٔ دوم به شکل 0 = $C2 درمی‌آید که هرگز درست نیست." }
            zero1 || zero2 -> { inconsistent = false; why = "یکی از معادله‌ها به شکل 0 = 0 است که همیشه درست است و اطلاعاتی نمی‌دهد." }
            else -> {
                val t1 = A1 * C2 - A2 * C1
                val t2 = B1 * C2 - B2 * C1
                inconsistent = !(t1.isZero() && t2.isZero())
                why = "نسبت ضریب‌های دو معادله برابر است. برای دانستن اینکه ثابت‌ها هم با همان نسبت‌اند، a₁c₂ − a₂c₁ = ${A1.p()}×${C2.p()} − ${A2.p()}×${C1.p()} = $t1 و b₁c₂ − b₂c₁ = ${B1.p()}×${C2.p()} − ${B2.p()}×${C1.p()} = $t2 را حساب می‌کنیم." +
                    if (inconsistent) " همهٔ این دو مقدار صفر نیستند، پس ثابت‌ها با نسبت ضریب‌ها نمی‌خوانند." else " هر دو صفر شدند، پس دو معادله در واقع یکی هستند."
            }
        }

        if (inconsistent) {
            add("نتیجه: دستگاه جواب ندارد", "∅", "$why\nپس دو معادله با هم ناسازگارند و هیچ x و yی نمی‌تواند هر دو را برقرار کند (مثل دو خط موازی که هیچ‌وقت به هم نمی‌رسند).", "پاسخ نهایی")
            return MathSolutionResult("حل دستگاه معادلات خطی", initial, steps, MathNode.TextNode("دستگاه جواب ندارد"), null, true, "دستگاه معادلات")
        }

        val rowA: Triple<Fr, Fr, Fr>? = when {
            !zero1 -> Triple(A1, B1, C1)
            !zero2 -> Triple(A2, B2, C2)
            else -> null
        }
        val general: String
        if (rowA == null) {
            general = "هر دو معادله 0 = 0 هستند؛ پس هر جفت عدد (x، y) جواب است."
        } else {
            val (a, b, c) = rowA
            general = if (!b.isZero()) {
                val sx = (-a).div(b); val s0 = c.div(b)
                "از معادلهٔ ${equationText(a, b, c)}، y را بر حسب x می‌نویسیم: y = ${s0} ${if (sx.signum() < 0) "−" else "+"} ${sx.abs()}x. هر x دلخواه با این y یک جواب است."
            } else {
                "از معادلهٔ ${equationText(a, b, c)} نتیجه می‌شود x = ${c.div(a)} و y هر عدد دلخواهی می‌تواند باشد."
            }
        }
        add("نتیجه: بی‌شمار جواب", "بی‌شمار جواب", "$why\nپس دو معادله یک خط را نشان می‌دهند و همهٔ نقطه‌های آن جواب‌اند.\n$general", "پاسخ نهایی")
        return MathSolutionResult("حل دستگاه معادلات خطی", initial, steps, MathNode.TextNode("بی‌شمار جواب"), null, true, "دستگاه معادلات")
    }
}

/** Turns the typed boxes into numbers: empty x/y box = 1, empty constant = 0, sign chip applies to y. */
object LinearSystemParse {
    /** Returns [a1, b1, c1, a2, b2, c2] or null if something is not a number. */
    fun parse(a1: String, s1: String, b1: String, c1: String, a2: String, s2: String, b2: String, c2: String): List<Fr>? {
        fun coef(t: String): Fr? = if (t.isBlank()) Fr.ONE else Fr.parse(t)
        fun const(t: String): Fr? = if (t.isBlank()) Fr.ZERO else Fr.parse(t)
        val fa1 = coef(a1) ?: return null
        var fb1 = coef(b1) ?: return null
        val fc1 = const(c1) ?: return null
        val fa2 = coef(a2) ?: return null
        var fb2 = coef(b2) ?: return null
        val fc2 = const(c2) ?: return null
        if (s1 == "−" || s1 == "-") fb1 = -fb1
        if (s2 == "−" || s2 == "-") fb2 = -fb2
        return listOf(fa1, fb1, fc1, fa2, fb2, fc2)
    }
}
