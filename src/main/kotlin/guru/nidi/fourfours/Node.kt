package guru.nidi.fourfours

abstract class Node(val level: Int, val size: Int, val left: Node?, val right: Node?,
                    val fours: Int, val value: Rational) {
}

abstract class Binary(level: Int, left: Node, right: Node, value: Rational) :
        Node(Math.max(Math.max(level, left.level), right.level), left.size + right.size + 1,
                left, right, left.fours + right.fours, value)

abstract class Unary(level: Int, left: Node, value: Rational) :
        Node(Math.max(level, left.level), left.size + 1, left, null, left.fours, value)

abstract class Nullary(level: Int, size: Int, fours: Int, value: Rational) :
        Node(level, size, null, null, fours, value)

class Add(left: Node, right: Node, value: Rational) : Binary(0, left, right, value) {
    companion object {
        operator fun invoke(left: Node, right: Node): Add? {
            val v = left.value + right.value
            return if (v == null) null else Add(left, right, v)
        }
    }

    override fun toString() = "($left + $right)"
}

class Sub(left: Node, right: Node, value: Rational) : Binary(0, left, right, value) {
    companion object {
        operator fun invoke(left: Node, right: Node): Sub? {
            val v = left.value - right.value
            return if (v == null) null else Sub(left, right, v)
        }
    }

    override fun toString() = "($left - $right)"
}

class Mul(left: Node, right: Node, value: Rational) : Binary(0, left, right, value) {
    companion object {
        operator fun invoke(left: Node, right: Node): Mul? {
            val v = left.value * right.value
            return if (v == null) null else Mul(left, right, v)
        }
    }

    override fun toString() = "($left * $right)"
}

class Div(left: Node, right: Node, value: Rational) : Binary(0, left, right, value) {
    companion object {
        operator fun invoke(left: Node, right: Node): Div? {
            if (right.value.isZero()) return null
            val v = left.value / right.value
            return if (v == null) null else Div(left, right, v)
        }
    }

    override fun toString() = "($left / $right)"
}

class Pow(left: Node, right: Node, value: Rational) : Binary(0, left, right, value) {
    companion object {
        operator fun invoke(left: Node, right: Node): Pow? {
            val rv = right.value
            if (rv < 2.0 || rv > 8.0 || left.value < .1) {
                return null
            }
            val v = left.value.pow(rv)
            return if (v == null) null else Pow(left, right, v)
        }
    }

    override fun toString() = "($left ^ $right)"
}

class Fac(left: Node, value: Rational) : Unary(0, left, value) {
    companion object {
        operator fun invoke(left: Node): Fac? {
            val lv = left.value
            return if (!lv.isNatural() || lv < 3.0 || lv > 8.0) null
            else Fac(left, lv.fac())
        }
    }

    override fun toString() = "$left!"
}

class Sqr(left: Node, value: Rational) : Unary(0, left, value) {
    companion object {
        operator fun invoke(left: Node): Sqr? {
            val v = left.value.pow(Rational(1, 2))
            return if (v == null || left.value <= 0.0) null
            else Sqr(left, v)
        }
    }

    override fun toString() = "sqr($left)"
}

class Root(left: Node, right: Node, value: Rational) : Binary(3, left, right, value) {
    companion object {
        operator fun invoke(left: Node, right: Node): Root? {
            if (left.value <= 0.0 || right.value <= 0.0 || left.value > 10.0) {
                return null
            }
            val v = right.value.pow(left.value.inv())
            return if (v == null) null else Root(left, right, v)
        }
    }

    override fun toString() = "($left root $right)"
}

class Gamma(left: Node, value: Rational) : Unary(4, left, value) {
    companion object {
        operator fun invoke(left: Node): Gamma? {
            val lv = left.value
            return if (!lv.isNatural() || lv < 2.0 || lv > 9.0) null
            else Gamma(left, Rational(org.apache.commons.math3.special.Gamma.gamma(lv.toDouble()).toInt()))
        }
    }

    override fun toString() = "gamma($left)"
}

class Percent(left: Node, value: Rational) : Unary(5, left, value) {
    companion object {
        operator fun invoke(left: Node): Percent? {
            val v = Div(left, One(100))
            return if (v == null) null else Percent(left, v.value)
        }
    }

    override fun toString() = "($left)%"
}

class Square(left: Node, value: Rational) : Unary(6, left, value) {
    companion object {
        operator fun invoke(left: Node): Square? {
            val mul = Mul(left, left)
            return if (mul == null) null else Square(left, mul.value)
        }
    }

    override fun toString() = "sq($left)"
}

class Or(left: Node, right: Node, value: Rational) : Binary(7, left, right, value) {
    companion object {
        operator fun invoke(left: Node, right: Node): Or? {
            return if (!left.value.isNatural() || !right.value.isNatural()) null
            else Or(left, right, Rational(left.value.num or right.value.num))
        }
    }

    override fun toString() = "($left or $right)"
}

class And(left: Node, right: Node, value: Rational) : Binary(7, left, right, value) {
    companion object {
        operator fun invoke(left: Node, right: Node): And? {
            return if (!left.value.isNatural() || !right.value.isNatural()) null
            else And(left, right, Rational(left.value.num and right.value.num))
        }
    }

    override fun toString() = "($left and $right)"
}

class Xor(left: Node, right: Node, value: Rational) : Binary(7, left, right, value) {
    companion object {
        operator fun invoke(left: Node, right: Node): Xor? {
            return if (!left.value.isNatural() || !right.value.isNatural()) null
            else Xor(left, right, Rational(left.value.num xor right.value.num))
        }
    }

    override fun toString() = "($left xor $right)"
}

class Shl(left: Node, right: Node, value: Rational) : Binary(8, left, right, value) {
    companion object {
        operator fun invoke(left: Node, right: Node): Shl? {
            return if (!left.value.isNatural() || !right.value.isNatural() || right.value.num > 64) null
            else Shl(left, right, Rational(left.value.num shl right.value.num.toInt()))
        }
    }

    override fun toString() = "($left << $right)"
}

class Shr(left: Node, right: Node, value: Rational) : Binary(9, left, right, value) {
    companion object {
        operator fun invoke(left: Node, right: Node): Shr? {
            return if (!left.value.isNatural() || !right.value.isNatural() || right.value.num > 64) null
            else Shr(left, right, Rational(left.value.num shr right.value.num.toInt()))
        }
    }

    override fun toString() = "($left >> $right)"
}

class PointOne(val digit: Int) : Nullary(0, 0, 1, Rational(digit, 10)) {
    override fun toString() = ".$digit"
}

class PointOneRecur(val digit: Int) : Nullary(2, 1, 1, Rational(digit, 9)) {
    override fun toString() = ".$digit..."
}

class One(val digit: Int) : Nullary(0, 0, 1, Rational(digit)) {
    override fun toString() = "$digit"
}

class Eleven(val digit: Int) : Nullary(0, 0, 2, Rational(11 * digit)) {
    override fun toString() = "$digit$digit"
}

class OnePointOne(val digit: Int) : Nullary(0, 0, 2, Rational(11 * digit, 10)) {
    override fun toString() = "$digit.$digit"
}

class OnePointOneRecur(val digit: Int) : Nullary(2, 1, 2, Rational(10 * digit, 9)) {
    override fun toString() = "$digit.$digit..."
}

class HundredEleven(val digit: Int) : Nullary(0, 0, 3, Rational(111 * digit)) {
    override fun toString() = "$digit$digit$digit"
}

class ThousandOneHundredEleven(val digit: Int) : Nullary(0, 0, 4, Rational(1111 * digit)) {
    override fun toString() = "$digit$digit$digit$digit"
}
