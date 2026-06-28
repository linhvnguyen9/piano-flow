package com.linh.pianoflow.feature.sightreading.impl.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SightReadingScoringTest {

    @Test
    fun whiteNotesIn_C3_to_C5_has_15_naturals() {
        val whites = whiteNotesIn(48, 72)
        assertEquals(15, whites.size)
        assertEquals(48, whites.first()) // C3
        assertEquals(72, whites.last())  // C5
        assertTrue(whites.none { (it % 12) in setOf(1, 3, 6, 8, 10) }, "pool must contain no sharps")
    }

    @Test
    fun summarize_empty_is_zeroed() {
        val s = summarize(emptyList())
        assertEquals(0, s.notesDone)
        assertEquals(0, s.accuracyPercent)
        assertTrue(s.slowest.isEmpty())
    }

    @Test
    fun summarize_computes_median_accuracy_and_slowest() {
        val records = listOf(
            NoteRecord("C4", 1.0, missed = false),
            NoteRecord("F4", 3.0, missed = true),
            NoteRecord("G4", 2.0, missed = false),
        )
        val s = summarize(records)
        assertEquals(3, s.notesDone)
        assertEquals(2.0, s.medianSeconds, 0.0001) // middle of [1, 2, 3]
        assertEquals(67, s.accuracyPercent)         // 2 of 3 correct on the first tap
        assertEquals("F4", s.slowest.first().name)  // slowest average time
        assertEquals(1f, s.slowest.first().barFraction, 0.0001f)
    }

    @Test
    fun summarize_caps_slowest_at_four() {
        val records = (0 until 6).map { NoteRecord("N$it", (it + 1).toDouble(), missed = false) }
        assertEquals(4, summarize(records).slowest.size)
    }
}
