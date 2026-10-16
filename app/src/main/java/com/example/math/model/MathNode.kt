package com.example.math.model

sealed class MathNode {
    data class Number(val value: String) : MathNode()
    data class Variable(val name: String) : MathNode()
    data class Constant(val symbol: String, val value: Double) : MathNode()
    data class Fraction(val numerator: MathNode, val denominator: MathNode) : MathNode()
    data class Power(val base: MathNode, val exponent: MathNode) : MathNode()
    data class Root(val radicand: MathNode, val index: MathNode? = null) : MathNode()
    data class Addition(val left: MathNode, val right: MathNode) : MathNode()
    data class Subtraction(val left: MathNode, val right: MathNode) : MathNode()
    data class Multiplication(val left: MathNode, val right: MathNode, val showDot: Boolean = false) : MathNode()
    data class Division(val left: MathNode, val right: MathNode) : MathNode()
    data class FunctionCall(val name: String, val arg: MathNode) : MathNode()
    data class Limit(val variable: String, val target: MathNode, val expr: MathNode, val direction: String = "") : MathNode()
    data class Derivative(val variable: String, val order: Int = 1, val expr: MathNode, val atPoint: MathNode? = null) : MathNode()
    data class Integral(val variable: String, val lowerBound: MathNode? = null, val upperBound: MathNode? = null, val integrand: MathNode) : MathNode()
    data class Sigma(val indexVar: String, val lower: MathNode, val upper: MathNode, val summand: MathNode) : MathNode()
    data class Matrix(val rows: Int, val cols: Int, val values: List<List<MathNode>>) : MathNode()
    data class Group(val inner: MathNode) : MathNode()
    data class Equation(val left: MathNode, val right: MathNode) : MathNode()
    data class TextNode(val text: String) : MathNode()

    fun toStandardString(): String = when (this) {
        is Number -> value
        is Variable -> name
        is Constant -> symbol
        is Fraction -> "(${numerator.toStandardString()})/(${denominator.toStandardString()})"
        is Power -> "(${base.toStandardString()})^(${exponent.toStandardString()})"
        is Root -> if (index == null) "√(${radicand.toStandardString()})" else "${index.toStandardString()}√(${radicand.toStandardString()})"
        is Addition -> "${left.toStandardString()} + ${right.toStandardString()}"
        is Subtraction -> "${left.toStandardString()} - ${right.toStandardString()}"
        is Multiplication -> if (showDot) "${left.toStandardString()} · ${right.toStandardString()}" else "${left.toStandardString()} * ${right.toStandardString()}"
        is Division -> "${left.toStandardString()} ÷ ${right.toStandardString()}"
        is FunctionCall -> "$name(${arg.toStandardString()})"
        is Limit -> "lim_{$variable -> ${target.toStandardString()}} ${expr.toStandardString()}"
        is Derivative -> "d/d$variable [${expr.toStandardString()}]" + (if (atPoint != null) " at $variable=${atPoint.toStandardString()}" else "")
        is Integral -> {
            val bounds = if (lowerBound != null && upperBound != null) "_${lowerBound.toStandardString()}^${upperBound.toStandardString()}" else ""
            "∫$bounds ${integrand.toStandardString()} d$variable"
        }
        is Sigma -> "∑_{$indexVar=${lower.toStandardString()}}^${upper.toStandardString()} ${summand.toStandardString()}"
        is Matrix -> {
            values.joinToString(prefix = "[", postfix = "]") { row ->
                row.joinToString(prefix = "[", postfix = "]") { it.toStandardString() }
            }
        }
        is Group -> "(${inner.toStandardString()})"
        is Equation -> "${left.toStandardString()} = ${right.toStandardString()}"
        is TextNode -> text
    }
}
