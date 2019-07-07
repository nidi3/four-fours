/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.fourfours

import java.io.File
import kotlin.system.measureTimeMillis

fun main() {
    for (depth in 1..4) {
        for (digit in 1..9) {
            run(digit, depth)
        }
    }
}

fun run(digit: Int, depth: Int) {
    File("target/result-$digit-$depth.txt").printWriter().use { out ->

        val time = measureTimeMillis {
            val res = Builder(digit, 40000, depth).build()

            var level = 0
            val miss = mutableListOf<Int>()
            val levels = mutableMapOf<Int, Int>()
            for (i in 0..40000) {
                val tree = res[Rational(i)]
                if (tree == null) {
                    miss.add(i)
                } else {
                    out.println("$i: $tree")
                    if (tree.level > level) {
                        level = tree.level
                        levels[i] = level
                    }
                }
            }

            out.println("Next Levels at: $levels")
            out.println("Misses (${miss.size}): $miss")
        }
        out.println("time: ${time}ms")
    }
//    Thread.sleep(100000000000)
}

