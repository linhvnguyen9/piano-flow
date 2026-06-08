package com.linh.pianoflow.core.model

import kotlin.math.pow

object Pitch {
    val NAMES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val WHITE_PCS = setOf(0, 2, 4, 5, 7, 9, 11)

    fun pc(midi: Int): Int = ((midi % 12) + 12) % 12
    fun name(midi: Int): String = NAMES[pc(midi)] + (midi / 12 - 1)
    fun freq(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)
    fun isWhite(midi: Int): Boolean = pc(midi) in WHITE_PCS
}
