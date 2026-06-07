package com.linh.pianoflow.songs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertNotEquals

class ChordParserTest {

    @Test fun parse_Asus4() = assertEquals(Chord(9, Quality.SUS4), parseChord("Asus4"))
    @Test fun parse_Csus2() = assertEquals(Chord(0, Quality.SUS2), parseChord("Csus2"))
    @Test fun parse_G7() = assertEquals(Chord(7, Quality.DOM7), parseChord("G7"))
    @Test fun parse_Cmaj7() = assertEquals(Chord(0, Quality.MAJ7), parseChord("Cmaj7"))
    @Test fun parse_CM7_is_maj7() = assertEquals(Chord(0, Quality.MAJ7), parseChord("CM7"))
    @Test fun parse_Cm7() = assertEquals(Chord(0, Quality.MIN7), parseChord("Cm7"))
    @Test fun parse_Bbmaj7() = assertEquals(Chord(10, Quality.MAJ7), parseChord("Bbmaj7"))
    @Test fun parse_FSharpM7() = assertEquals(Chord(6, Quality.MIN7), parseChord("F#m7"))
    @Test fun parse_Bm7b5() = assertEquals(Chord(11, Quality.M7B5), parseChord("Bm7b5"))
    @Test fun parse_C6() = assertEquals(Chord(0, Quality.MAJ6), parseChord("C6"))
    @Test fun parse_Am6() = assertEquals(Chord(9, Quality.MIN6), parseChord("Am6"))

    @Test
    fun case_distinction_M7_vs_m7() {
        val cap = parseChord("CM7")
        val low = parseChord("Cm7")
        assertNotNull(cap)
        assertNotNull(low)
        assertNotEquals(cap, low)
        assertEquals(Quality.MAJ7, cap!!.quality)
        assertEquals(Quality.MIN7, low!!.quality)
    }

    @Test fun invalid_H() = assertNull(parseChord("H"))
    @Test fun invalid_G7sus() = assertNull(parseChord("G7sus"))
    @Test fun invalid_Csus9() = assertNull(parseChord("Csus9"))
    @Test fun invalid_Xyz() = assertNull(parseChord("Xyz"))

    @Test
    fun quality_note_counts() {
        val triadsAndSus = listOf(Quality.MAJ, Quality.MIN, Quality.DIM, Quality.AUG, Quality.SUS2, Quality.SUS4)
        triadsAndSus.forEach { assertEquals(3, it.intervals.size, "$it should be triad-sized") }
        val fours = listOf(Quality.DOM7, Quality.MAJ7, Quality.MIN7, Quality.DIM7, Quality.M7B5, Quality.MAJ6, Quality.MIN6)
        fours.forEach { assertEquals(4, it.intervals.size, "$it should have 4 notes") }
    }

    @Test
    fun parseProgression_splits_separators_and_keeps_tokens() {
        val out = parseProgression("C | G | Am | F")
        assertEquals(listOf("C", "G", "Am", "F"), out.map { it.first })
        assertEquals(listOf(Quality.MAJ, Quality.MAJ, Quality.MIN, Quality.MAJ), out.map { it.second!!.quality })
    }

    @Test
    fun parseProgression_surfaces_errors_as_nulls() {
        val out = parseProgression("C, Xyz, G")
        assertEquals(3, out.size)
        assertNotNull(out[0].second)
        assertNull(out[1].second)
        assertNotNull(out[2].second)
    }
}
