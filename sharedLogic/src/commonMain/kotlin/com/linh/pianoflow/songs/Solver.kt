package com.linh.pianoflow.songs

import kotlin.math.abs

fun solve(
    chords: List<Chord>,
    anchor: Boolean,
    pins: Map<Int, Int> = emptyMap(),
): List<Voicing> {
    if (chords.isEmpty()) return emptyList()
    val lambda = if (anchor) 0.35 else 0.0
    val center = 60.0
    val rawCands = chords.map { candidates(it) }
    if (rawCands.any { it.isEmpty() }) return emptyList()
    val cands = rawCands.mapIndexed { i, list ->
        val k = pins[i]
        if (k != null && k in list.indices) listOf(list[k]) else list
    }
    val n = cands.size
    val dp = cands.map { DoubleArray(it.size) { Double.POSITIVE_INFINITY } }
    val bp = cands.map { IntArray(it.size) { -1 } }
    fun anc(v: Voicing) = lambda * abs(v.notes.average() - center)
    for (j in cands[0].indices) dp[0][j] = anc(cands[0][j])
    for (i in 1 until n) {
        for (j in cands[i].indices) {
            val a = anc(cands[i][j])
            for (p in cands[i - 1].indices) {
                val c = dp[i - 1][p] + moveCost(cands[i - 1][p].notes, cands[i][j].notes) + a
                if (c < dp[i][j]) {
                    dp[i][j] = c
                    bp[i][j] = p
                }
            }
        }
    }
    var j = dp[n - 1].indices.minByOrNull { dp[n - 1][it] }!!
    val path = arrayOfNulls<Voicing>(n)
    for (i in n - 1 downTo 0) {
        path[i] = cands[i][j]
        if (i > 0) j = bp[i][j]
    }
    return path.filterNotNull()
}

fun totalMovement(path: List<Voicing>): Double =
    (1 until path.size).sumOf { moveCost(path[it - 1].notes, path[it].notes) }

fun rootBaseline(chords: List<Chord>): List<Voicing> =
    chords.map { Voicing(rootPosition(it, 4), 0) }

fun keyboardRange(voicings: List<Voicing>): Pair<Int, Int> {
    if (voicings.isEmpty()) return 60 to 72
    val allNotes = voicings.flatMap { it.notes }
    val lo = allNotes.min()
    val hi = allNotes.max()
    val start = lo - ((lo % 12) + 12) % 12
    val end = hi + (11 - ((hi % 12) + 12) % 12)
    return start to end
}
