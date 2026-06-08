package com.linh.pianoflow.feature.chordsmoother.impl.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoveCostTest {

    @Test
    fun equal_size_identity_is_zero() {
        assertEquals(0.0, moveCost(listOf(60, 64, 67), listOf(60, 64, 67)))
    }

    @Test
    fun rigid_shift_up_one_semitone_costs_translation_only() {
        // C major -> C# major (parallel hand slide): deltas [1,1,1], shift=1, reshape=0
        // cost = 1*1 + 2*0 = 1
        assertEquals(1.0, moveCost(listOf(60, 64, 67), listOf(61, 65, 68)))
    }

    @Test
    fun rigid_shift_down_costs_same_as_shift_up() {
        // symmetry: deltas [-1,-1,-1], shift=-1, reshape=0
        assertEquals(1.0, moveCost(listOf(61, 65, 68), listOf(60, 64, 67)))
    }

    @Test
    fun single_finger_move_is_pure_reshape() {
        // [60,64,67] -> [60,64,70]: deltas [0,0,3], median=0, reshape=3
        // cost = 1*0 + 2*3 = 6   (was 3 under the old sum-of-abs metric)
        assertEquals(6.0, moveCost(listOf(60, 64, 67), listOf(60, 64, 70)))
    }

    @Test
    fun rigid_shift_beats_equivalent_reshape() {
        // Same total finger-travel (3 semitones), but distributed:
        //   rigid:  [60,64,67] -> [61,65,68]   cost = 1
        //   reshape:[60,64,67] -> [60,64,70]   cost = 6
        val rigid = moveCost(listOf(60, 64, 67), listOf(61, 65, 68))
        val reshape = moveCost(listOf(60, 64, 67), listOf(60, 64, 70))
        assertTrue(rigid < reshape, "rigid=$rigid reshape=$reshape")
    }

    @Test
    fun equal_size_mixed_translation_and_reshape() {
        // deltas [2,1,2], median=2, reshape=|2-2|+|1-2|+|2-2|=1, cost=2*1 + 1*2 = 4
        assertEquals(4.0, moveCost(listOf(60, 64, 67), listOf(62, 65, 69)))
    }

    @Test
    fun unequal_size_uses_symmetric_nearest_note() {
        val c = moveCost(listOf(60, 64, 67), listOf(60, 64, 67, 70))
        assertTrue(c.isFinite())
        assertTrue(c >= 0.0)
    }
}
