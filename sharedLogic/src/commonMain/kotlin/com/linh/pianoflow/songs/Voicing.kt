package com.linh.pianoflow.songs

import kotlin.math.abs

data class Voicing(val notes: List<Int>, val inv: Int)

fun rootPosition(chord: Chord, octave: Int): List<Int> {
    val root = chord.rootPc + 12 * (octave + 1)
    return chord.quality.intervals.map { root + it }
}

fun invert(v: List<Int>, k: Int): List<Int> {
    val out = v.toMutableList()
    repeat(k) { out.add(out.removeAt(0) + 12) }
    return out.sorted()
}

fun candidates(chord: Chord): List<Voicing> {
    val size = chord.quality.intervals.size
    val seen = HashSet<String>()
    val out = ArrayList<Voicing>()
    for (oct in 3..5) {
        for (k in 0 until size) {
            val v = invert(rootPosition(chord, oct), k)
            if (v.first() < 48 || v.last() > 86) continue
            if (seen.add(v.joinToString(","))) out += Voicing(v, k)
        }
    }
    return out
}

private fun nearestSum(from: List<Int>, to: List<Int>): Int =
    from.sumOf { x -> to.minOf { y -> abs(x - y) } }

fun moveCost(a: List<Int>, b: List<Int>): Double =
    if (a.size == b.size) a.indices.sumOf { abs(a[it] - b[it]).toDouble() }
    else (nearestSum(a, b) + nearestSum(b, a)) / 2.0

fun inversionName(inv: Int): String = when (inv) {
    0 -> "root"
    1 -> "1st inv"
    2 -> "2nd inv"
    3 -> "3rd inv"
    else -> "inv $inv"
}
