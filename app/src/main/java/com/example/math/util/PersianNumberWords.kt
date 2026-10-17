package com.example.math.util

import java.math.BigInteger

object PersianNumberWords {

    private val ones = arrayOf(
        "", "یک", "دو", "سه", "چهار", "پنج", "شش", "هفت", "هشت", "نه",
        "ده", "یازده", "دوازده", "سیزده", "چهارده", "پانزده", "شانزده", "هفده", "هجده", "نوزده"
    )

    private val tens = arrayOf(
        "", "", "بیست", "سی", "چهل", "پنجاه", "شصت", "هفتاد", "هشتاد", "نود"
    )

    private val hundreds = arrayOf(
        "", "صد", "دویست", "سیصد", "چهارصد", "پانصد", "ششصد", "هفتصد", "هشتصد", "نهصد"
    )

    private val scales = arrayOf(
        "", "هزار", "میلیون", "میلیارد", "تریلیون", "کوآدریلیون", "کوینتیلیون", "سکستیلیون", "سپتیلیون", "اکتیلیون", "نونیلیون", "دسیلیون",
        "آندسیلیون", "دودسیلیون", "تریدسیلیون", "کوآتوردسیلیون", "کویندسیلیون"
    )

    fun convert(numberStr: String): String {
        try {
            val clean = numberStr.replace(",", "").replace("٬", "").trim()
            if (clean.isEmpty()) return ""
            if (clean == "0" || clean == "۰") return "صفر"

            var isNegative = false
            var workStr = clean
            if (workStr.startsWith("-") || workStr.startsWith("−")) {
                isNegative = true
                workStr = workStr.substring(1).trim()
            }

            // Split integer and decimal parts
            val parts = workStr.split(".")
            val intPartStr = parts[0]
            val decPartStr = if (parts.size > 1) parts[1] else ""

            if (intPartStr.isEmpty() && decPartStr.isEmpty()) return ""

            val intBig = try {
                BigInteger(if (intPartStr.isEmpty()) "0" else intPartStr)
            } catch (e: Exception) {
                return ""
            }

            val intWords = convertBigInteger(intBig)

            var result = if (intBig == BigInteger.ZERO && decPartStr.isNotEmpty()) {
                "صفر"
            } else {
                intWords
            }

            if (decPartStr.isNotEmpty()) {
                val decTrimmed = decPartStr.trimEnd('0')
                if (decTrimmed.isNotEmpty()) {
                    val decBig = try {
                        BigInteger(decTrimmed)
                    } catch (e: Exception) {
                        null
                    }
                    if (decBig != null && decBig > BigInteger.ZERO) {
                        val decWords = convertBigInteger(decBig)
                        val decScale = getDecimalScaleWord(decTrimmed.length)
                        result += " و $decWords $decScale"
                    }
                }
            }

            return if (isNegative) "منفی $result" else result
        } catch (e: Exception) {
            return ""
        }
    }

    private fun convertBigInteger(number: BigInteger): String {
        if (number == BigInteger.ZERO) return "صفر"

        val chunks = mutableListOf<Int>()
        var temp = number
        val thousand = BigInteger.valueOf(1000)

        while (temp > BigInteger.ZERO) {
            val rem = temp.mod(thousand).toInt()
            chunks.add(rem)
            temp = temp.divide(thousand)
        }

        val parts = mutableListOf<String>()
        for (i in chunks.indices.reversed()) {
            val chunk = chunks[i]
            if (chunk > 0) {
                val chunkText = convertChunk(chunk)
                val scaleText = if (i < scales.size) scales[i] else ""
                val full = if (scaleText.isNotEmpty()) "$chunkText $scaleText" else chunkText
                parts.add(full)
            }
        }

        return parts.joinToString(" و ")
    }

    private fun convertChunk(n: Int): String {
        val parts = mutableListOf<String>()
        val h = n / 100
        val rem = n % 100

        if (h > 0) {
            parts.add(hundreds[h])
        }

        if (rem in 1..19) {
            parts.add(ones[rem])
        } else if (rem >= 20) {
            val t = rem / 10
            val o = rem % 10
            if (t > 0) parts.add(tens[t])
            if (o > 0) parts.add(ones[o])
        }

        return parts.joinToString(" و ")
    }

    private fun getDecimalScaleWord(digitsCount: Int): String {
        return when (digitsCount) {
            1 -> "دهم"
            2 -> "صدم"
            3 -> "هزارم"
            4 -> "ده‌هزارم"
            5 -> "صد‌هزارم"
            6 -> "میلیونیم"
            else -> "اعشاری"
        }
    }
}
