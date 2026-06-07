package com.linh.pianoflow.songs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SolverTest {

    private fun bruteForceMin(chords: List<Chord>): Double {
        val cands = chords.map { candidates(it) }
        if (cands.any { it.isEmpty() }) return Double.POSITIVE_INFINITY
        var best = Double.POSITIVE_INFINITY
        fun recurse(i: Int, cost: Double, prev: Voicing?) {
            if (cost >= best) return
            if (i == cands.size) { best = cost; return }
            for (v in cands[i]) {
                val c = if (prev == null) cost else cost + moveCost(prev.notes, v.notes)
                recurse(i + 1, c, v)
            }
        }
        recurse(0, 0.0, null)
        return best
    }

    private fun chords(tokens: String): List<Chord> =
        parseProgression(tokens).map { it.second!! }

    @Test
    fun dp_equals_brute_force_C_G_Am_F() {
        val cs = chords("C | G | Am | F")
        val opt = totalMovement(solve(cs, anchor = false))
        assertEquals(bruteForceMin(cs), opt, 1e-9)
        assertEquals(9.0, opt, 1e-9)
    }

    @Test
    fun dp_equals_brute_force_F_G_Em_Am() {
        val cs = chords("F | G | Em | Am")
        val opt = totalMovement(solve(cs, anchor = false))
        assertEquals(bruteForceMin(cs), opt, 1e-9)
        assertEquals(11.0, opt, 1e-9)
    }

    @Test
    fun dp_equals_brute_force_Am_F_C_G() {
        val cs = chords("Am | F | C | G")
        val opt = totalMovement(solve(cs, anchor = false))
        assertEquals(bruteForceMin(cs), opt, 1e-9)
        assertEquals(7.0, opt, 1e-9)
    }

    @Test
    fun dp_equals_brute_force_mixed_cardinality() {
        val cs = chords("Asus4 | G7 | Cmaj7 | Am7")
        val opt = totalMovement(solve(cs, anchor = false))
        assertEquals(bruteForceMin(cs), opt, 1e-9)
    }

    @Test
    fun dp_equals_brute_force_jazz_ii_V_I_circle() {
        val cs = chords("C | Am7 | Dm7 | G7")
        val opt = totalMovement(solve(cs, anchor = false))
        assertEquals(bruteForceMin(cs), opt, 1e-9)
    }

    @Test
    fun optimized_never_worse_than_root_only() {
        val progressions = listOf(
            "C | G | Am | F",
            "F | G | Em | Am",
            "Am | F | C | G",
            "Asus4 | G7 | Cmaj7 | Am7",
            "C | Am7 | Dm7 | G7"
        )
        for (p in progressions) {
            val cs = chords(p)
            val opt = totalMovement(solve(cs, anchor = false))
            val base = totalMovement(rootBaseline(cs))
            assertTrue(opt <= base, "$p: opt=$opt baseline=$base")
        }
    }

    @Test
    fun keyboard_range_contains_voicings_and_aligns_to_C_and_B() {
        val cs = chords("C | G | Am | F")
        val path = solve(cs, anchor = true)
        val (lo, hi) = keyboardRange(path)
        assertEquals(0, ((lo % 12) + 12) % 12, "start should be a C pitch class")
        assertEquals(11, ((hi % 12) + 12) % 12, "end should be a B pitch class")
        val allNotes = path.flatMap { it.notes }
        assertTrue(allNotes.min() >= lo && allNotes.max() <= hi)
    }

    @Test
    fun solve_returns_empty_for_empty_input() {
        assertEquals(emptyList(), solve(emptyList(), anchor = true))
    }

    @Test
    fun pinned_voicing_is_honoured_and_others_reflow() {
        val cs = chords("C | G | Am | F")
        val gCands = candidates(cs[1])
        val pickIdx = gCands.indices.last { gCands[it].inv == 1 }
        val pinned = solve(cs, anchor = false, pins = mapOf(1 to pickIdx))
        assertEquals(gCands[pickIdx].notes, pinned[1].notes)
        // unpinned still optimal-given-the-pin: every other chord's voicing must be the
        // minimum-cost choice for its neighbours.
        val cands = cs.map { candidates(it) }
        for (i in cs.indices) {
            if (i == 1) continue
            val chosen = pinned[i].notes
            val best = cands[i].minOf { v ->
                val left = if (i == 0) 0.0 else moveCost(pinned[i - 1].notes, v.notes)
                val right = if (i == cs.lastIndex) 0.0 else moveCost(v.notes, pinned[i + 1].notes)
                left + right
            }
            val actual = run {
                val left = if (i == 0) 0.0 else moveCost(pinned[i - 1].notes, chosen)
                val right = if (i == cs.lastIndex) 0.0 else moveCost(chosen, pinned[i + 1].notes)
                left + right
            }
            assertEquals(best, actual, 1e-9, "chord $i is not locally optimal given pin")
        }
    }

    @Test
    fun pinning_does_not_break_no_pin_signature() {
        val cs = chords("C | G | Am | F")
        val a = totalMovement(solve(cs, anchor = false))
        val b = totalMovement(solve(cs, anchor = false, pins = emptyMap()))
        assertEquals(a, b, 1e-9)
    }
}
