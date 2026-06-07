package com.linh.pianoflow.songs

enum class Quality(val intervals: List<Int>) {
    MAJ(listOf(0, 4, 7)),
    MIN(listOf(0, 3, 7)),
    DIM(listOf(0, 3, 6)),
    AUG(listOf(0, 4, 8)),
    SUS2(listOf(0, 2, 7)),
    SUS4(listOf(0, 5, 7)),
    DOM7(listOf(0, 4, 7, 10)),
    MAJ7(listOf(0, 4, 7, 11)),
    MIN7(listOf(0, 3, 7, 10)),
    DIM7(listOf(0, 3, 6, 9)),
    M7B5(listOf(0, 3, 6, 10)),
    MAJ6(listOf(0, 4, 7, 9)),
    MIN6(listOf(0, 3, 7, 9));

    fun suffix(): String = when (this) {
        MAJ -> ""
        MIN -> "m"
        DIM -> "dim"
        AUG -> "aug"
        SUS2 -> "sus2"
        SUS4 -> "sus4"
        DOM7 -> "7"
        MAJ7 -> "maj7"
        MIN7 -> "m7"
        DIM7 -> "dim7"
        M7B5 -> "m7♭5"
        MAJ6 -> "6"
        MIN6 -> "m6"
    }
}

data class Chord(val rootPc: Int, val quality: Quality)
