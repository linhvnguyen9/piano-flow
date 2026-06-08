package com.linh.pianoflow.feature.chordsmoother.impl.domain

import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.core.model.Quality
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertNotEquals

class ChordParserTest {

    @Test fun parse_Asus4() = assertEquals(Chord(9, Quality.SUS4), parseChordToken("Asus4"))
    @Test fun parse_Csus2() = assertEquals(Chord(0, Quality.SUS2), parseChordToken("Csus2"))
    @Test fun parse_G7() = assertEquals(Chord(7, Quality.DOM7), parseChordToken("G7"))
    @Test fun parse_Cmaj7() = assertEquals(Chord(0, Quality.MAJ7), parseChordToken("Cmaj7"))
    @Test fun parse_CM7_is_maj7() = assertEquals(Chord(0, Quality.MAJ7), parseChordToken("CM7"))
    @Test fun parse_Cm7() = assertEquals(Chord(0, Quality.MIN7), parseChordToken("Cm7"))
    @Test fun parse_Bbmaj7() = assertEquals(Chord(10, Quality.MAJ7), parseChordToken("Bbmaj7"))
    @Test fun parse_FSharpM7() = assertEquals(Chord(6, Quality.MIN7), parseChordToken("F#m7"))
    @Test fun parse_Bm7b5() = assertEquals(Chord(11, Quality.M7B5), parseChordToken("Bm7b5"))
    @Test fun parse_C6() = assertEquals(Chord(0, Quality.MAJ6), parseChordToken("C6"))
    @Test fun parse_Am6() = assertEquals(Chord(9, Quality.MIN6), parseChordToken("Am6"))

    @Test
    fun case_distinction_M7_vs_m7() {
        val cap = parseChordToken("CM7")
        val low = parseChordToken("Cm7")
        assertNotNull(cap)
        assertNotNull(low)
        assertNotEquals(cap, low)
        assertEquals(Quality.MAJ7, cap!!.quality)
        assertEquals(Quality.MIN7, low!!.quality)
    }

    @Test fun invalid_H() = assertNull(parseChordToken("H"))
    @Test fun invalid_G7sus() = assertNull(parseChordToken("G7sus"))
    @Test fun invalid_Csus9() = assertNull(parseChordToken("Csus9"))
    @Test fun invalid_Xyz() = assertNull(parseChordToken("Xyz"))

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

    @Test
    fun chordToToken_roundTrips_forEveryRootAndQuality() {
        for (pc in 0 until 12) {
            for (q in Quality.entries) {
                val chord = Chord(pc, q)
                val token = chordToToken(chord)
                assertEquals(
                    chord,
                    parseChordToken(token),
                    "round-trip failed for $token (pc=$pc, q=$q)",
                )
            }
        }
    }

    @Test
    fun chordToToken_usesSharpRootsAndPlainMajor() {
        assertEquals("C", chordToToken(Chord(0, Quality.MAJ)))
        assertEquals("C#m", chordToToken(Chord(1, Quality.MIN)))
        assertEquals("Gmaj7", chordToToken(Chord(7, Quality.MAJ7)))
        assertEquals("Am7b5", chordToToken(Chord(9, Quality.M7B5)))
    }

    @Test
    fun normalizeChordToken_capitalizesRecognizedRootOnly() {
        assertEquals("E", normalizeChordToken("e"))
        assertEquals("Cmaj7", normalizeChordToken("cmaj7"))
        assertEquals("Bb7", normalizeChordToken("bb7"))   // flat spelling preserved
        assertEquals("F#m7", normalizeChordToken("f#m7"))
        assertEquals("Cm7", normalizeChordToken("cm7"))   // minor 'm' stays lowercase
    }

    @Test
    fun normalizeChordToken_leavesUnrecognizedAndEmptyUnchanged() {
        assertEquals("xyz", normalizeChordToken("xyz"))
        assertEquals("", normalizeChordToken(""))
    }

    @Test
    fun normalizeChordToken_isIdempotentOnCanonical() {
        assertEquals("E", normalizeChordToken("E"))
        assertEquals("Am", normalizeChordToken("Am"))
    }
}
