package guru.nidi.fourfours

import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Builder(val digit: Int, val max: Int,val unaryDepth:Int) {
    val maps = listOf(
            HashMap<Rational, Node>(),
            HashMap<Rational, Node>(),
            HashMap<Rational, Node>(),
            HashMap<Rational, Node>())

    init {
        add(PointOne(digit))
        add(PointOneRecur(digit))
        add(One(digit))
        add(Eleven(digit))
        add(OnePointOne(digit))
        add(OnePointOneRecur(digit))
        add(HundredEleven(digit))
        add(ThousandOneHundredEleven(digit))
    }

    fun add(node: Node) {
        add(maps[node.fours - 1], node)
    }

    fun add(map: MutableMap<Rational, Node>, node: Node?, depth: Int = unaryDepth) {
        if (node != null && node.value >= 0.0 && node.value <= max.toDouble() * max &&
                (node.fours < 4 || node.value.isNatural())) {
            if (node.fours == 4 && node.value > max.toDouble() && depth > 0) {
                add(map, Sqr(node), depth - 1)
                if (node !is Percent) {
                    add(map, Percent(node), depth - 1)
                }
            }
            if (node.fours < 4 || node.value <= max.toDouble()) {
                val existing = map.get(node.value)
                if (existing == null || node.level < existing.level ||
                        (node.level == existing.level && node.size < existing.size)) {
                    map.put(node.value, node)
//                    if (map.size % 10000 == 0) {
//                        println("" + node.fours + "->" + map.size)
//                    }
                    if (depth > 0) {
                        add(map, Fac(node), depth - 1)
                        add(map, Gamma(node), depth - 1)
                        add(map, Square(node), depth - 1)
                        add(map, Sqr(node), depth - 1)
                        if (node !is Percent) {
                            add(map, Percent(node), depth - 1)
                        }
                    }
                }
            }
        }
    }

    fun build(): Map<Rational, Node> {
        combineMaps(0, 0)
        combineMaps(1, 0)
        combineMaps(1, 1)
        combineMaps(2, 0)
        return maps[3]
    }

    fun combineMaps(a: Int, b: Int, threading: Boolean = true) {
        val threads = Runtime.getRuntime().availableProcessors()
        val pool = Executors.newFixedThreadPool(if (threading) threads else 1)
        val tasks = if (threading) threads else 1
        val data = maps[a].values.toList()
        val piece = data.size / tasks
        val targetMap = maps[a + b + 1]
        val localMaps = mutableListOf<Map<Rational, Node>>()
        for (task in 0..tasks - 1) {
            val local = HashMap(targetMap)
            localMaps.add(local)
            pool.submit {
                try {
                    val part = data.subList(task * piece, if (task == tasks - 1) data.size else (task + 1) * piece)
                    doCombineMaps(local, part, maps[b].values)
                }catch(e:Exception){
                    e.printStackTrace()
                }
            }
        }
        pool.shutdown()
        pool.awaitTermination(10, TimeUnit.MINUTES)
        targetMap.clear()
        for (task in 0..tasks - 1) {
            for (entry in localMaps[task].entries) {
                val existing = targetMap[entry.key]
                if (existing == null || entry.value.level < existing.level ||
                        (entry.value.level == existing.level && entry.value.size < existing.size)) {
                    targetMap.put(entry.key, entry.value)
                }
            }
        }
    }

    fun doCombineMaps(map: MutableMap<Rational, Node>, a: Collection<Node>, b: Collection<Node>) {
        for (n in a) {
            for (m in b) {
                combine(map, n, m)
            }
        }
    }

    fun combine(map: MutableMap<Rational, Node>, left: Node, right: Node) {
        add(map, Add(left, right))
        add(map, Sub(left, right))
        add(map, Mul(left, right))
        add(map, Div(left, right))
        add(map, Pow(left, right))
        add(map, Root(left, right))
        add(map, Or(left, right))
        add(map, And(left, right))
        add(map, Xor(left, right))
        add(map, Shl(left, right))
        add(map, Shr(left, right))
        if (left.value != right.value) {
            add(map, Sub(right, left))
            add(map, Div(right, left))
            add(map, Pow(right, left))
            add(map, Root(right, left))
            add(map, Shl(right, left))
            add(map, Shr(right, left))
        }
    }

}