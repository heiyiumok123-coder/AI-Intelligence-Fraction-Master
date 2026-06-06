package com.example.myapplication

import kotlin.math.abs

data class Fraction(val numerator: Long, val denominator: Long) {
    init {
        require(denominator != 0L) { "Denominator cannot be zero" }
    }

    private fun gcd(a: Long, b: Long): Long {
        var x = abs(a)
        var y = abs(b)
        while (y != 0L) {
            val temp = y
            y = x % y
            x = temp
        }
        return x
    }

    fun simplify(): Fraction {
        if (numerator == 0L) return Fraction(0, 1)
        val common = gcd(numerator, denominator)
        val sign = if (denominator < 0) -1 else 1
        return Fraction(numerator * sign / common, abs(denominator) / common)
    }

    fun addRaw(other: Fraction): Fraction {
        val commonDenominator = lcm(abs(this.denominator), abs(other.denominator))
        val newNumerator = this.numerator * (commonDenominator / this.denominator) +
                other.numerator * (commonDenominator / other.denominator)
        return Fraction(newNumerator, commonDenominator)
    }

    fun minusRaw(other: Fraction): Fraction {
        val commonDenominator = lcm(abs(this.denominator), abs(other.denominator))
        val newNumerator = this.numerator * (commonDenominator / this.denominator) -
                other.numerator * (commonDenominator / other.denominator)
        return Fraction(newNumerator, commonDenominator)
    }

    operator fun plus(other: Fraction): Fraction {
        return addRaw(other).simplify()
    }

    operator fun minus(other: Fraction): Fraction {
        return minusRaw(other).simplify()
    }

    operator fun times(other: Fraction): Fraction {
        return Fraction(this.numerator * other.numerator, this.denominator * other.denominator).simplify()
    }

    operator fun div(other: Fraction): Fraction {
        require(other.numerator != 0L) { "Cannot divide by zero" }
        return Fraction(this.numerator * other.denominator, this.denominator * other.numerator).simplify()
    }

    override fun toString(): String {
        return if (denominator == 1L) "$numerator" else "$numerator/$denominator"
    }

    companion object {
        fun lcm(a: Long, b: Long): Long {
            if (a == 0L || b == 0L) return 0
            val x = abs(a)
            val y = abs(b)
            // Use GCD to find LCM: (a*b)/gcd(a,b)
            fun gcdInternal(m: Long, n: Long): Long {
                var a1 = m
                var b1 = n
                while (b1 != 0L) {
                    val t = b1
                    b1 = a1 % b1
                    a1 = t
                }
                return a1
            }
            return (x / gcdInternal(x, y)) * y
        }

        fun fromString(s: String): Fraction? {
            return try {
                if (s.contains("/")) {
                    val parts = s.split("/")
                    Fraction(parts[0].trim().toLong(), parts[1].trim().toLong())
                } else {
                    Fraction(s.trim().toLong(), 1)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}

fun getAdditionSteps(f1: Fraction, f2: Fraction, shouldSimplify: Boolean = true): String {
    val commonDenominator = Fraction.lcm(abs(f1.denominator), abs(f2.denominator))
    val m1 = commonDenominator / f1.denominator
    val m2 = commonDenominator / f2.denominator
    val n1 = f1.numerator * m1
    val n2 = f2.numerator * m2
    
    val sb = StringBuilder()
    sb.append("1. 找出分母 ${f1.denominator} 和 ${f2.denominator} 的最小公倍數：$commonDenominator\n")
    sb.append("2. 將分數通分：\n")
    sb.append("   $f1 = ($n1)/$commonDenominator\n")
    sb.append("   $f2 = ($n2)/$commonDenominator\n")
    sb.append("3. 分子相加：$n1 + $n2 = ${n1 + n2}\n")
    val rawResult = f1.addRaw(f2)
    sb.append("4. 結果：$rawResult")
    
    if (shouldSimplify) {
        val simplifiedResult = rawResult.simplify()
        if (simplifiedResult.toString() != rawResult.toString()) {
            sb.append(" = 化簡後為 $simplifiedResult")
        }
    }
    return sb.toString()
}

fun getSubtractionSteps(f1: Fraction, f2: Fraction, shouldSimplify: Boolean = true): String {
    val commonDenominator = Fraction.lcm(abs(f1.denominator), abs(f2.denominator))
    val m1 = commonDenominator / f1.denominator
    val m2 = commonDenominator / f2.denominator
    val n1 = f1.numerator * m1
    val n2 = f2.numerator * m2
    
    val sb = StringBuilder()
    sb.append("1. 找出分母 ${f1.denominator} 和 ${f2.denominator} 的最小公倍數：$commonDenominator\n")
    sb.append("2. 將分數通分：\n")
    sb.append("   $f1 = ($n1)/$commonDenominator\n")
    sb.append("   $f2 = ($n2)/$commonDenominator\n")
    sb.append("3. 分子相減：$n1 - $n2 = ${n1 - n2}\n")
    val rawResult = f1.minusRaw(f2)
    sb.append("4. 結果：$rawResult")
    
    if (shouldSimplify) {
        val simplifiedResult = rawResult.simplify()
        if (simplifiedResult.toString() != rawResult.toString()) {
            sb.append(" = 化簡後為 $simplifiedResult")
        }
    }
    return sb.toString()
}
