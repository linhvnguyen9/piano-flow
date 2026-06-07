package com.linh.pianoflow.songs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoveCostTest {

    @Test
    fun equal_size_identity_is_zero() {
        assertEquals(0.0, moveCost(listOf(60, 64, 67), listOf(60, 64, 67)))
    }

    @Test
    fun equal_size_pairwise() {
        // (62-60) + (65-64) + (69-67) = 2 + 1 + 2 = 5
        assertEquals(5.0, moveCost(listOf(60, 64, 67), listOf(62, 65, 69)))
    }

    @Test
    fun unequal_size_uses_symmetric_nearest_note() {
        val c = moveCost(listOf(60, 64, 67), listOf(60, 64, 67, 70))
        assertTrue(c.isFinite())
        assertTrue(c >= 0.0)
    }
}
