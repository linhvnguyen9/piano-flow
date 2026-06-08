package com.linh.pianoflow.feature.chordsmoother.impl.domain

import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.core.model.Quality
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VoicingTest {

    @Test
    fun rootPosition_C_major_octave4_is_60_64_67() {
        assertEquals(listOf(60, 64, 67), rootPosition(Chord(0, Quality.MAJ), 4))
    }

    @Test
    fun invert_C_major_root_to_first_inversion() {
        assertEquals(listOf(64, 67, 72), invert(rootPosition(Chord(0, Quality.MAJ), 4), 1))
    }

    @Test
    fun candidates_triad_at_most_nine_dedup_ascending_in_window() {
        val cands = candidates(Chord(0, Quality.MAJ))
        assertTrue(cands.size <= 9, "got ${cands.size}")
        for (c in cands) {
            assertEquals(c.notes.sorted(), c.notes, "not ascending: ${c.notes}")
            assertTrue(c.notes.first() >= 48, "below window: ${c.notes}")
            assertTrue(c.notes.last() <= 86, "above window: ${c.notes}")
        }
        val seen = HashSet<String>()
        for (c in cands) assertTrue(seen.add(c.notes.joinToString(",")), "dup: ${c.notes}")
    }
}
