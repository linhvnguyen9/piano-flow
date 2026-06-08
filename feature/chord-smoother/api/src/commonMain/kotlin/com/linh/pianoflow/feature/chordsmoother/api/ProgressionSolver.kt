package com.linh.pianoflow.feature.chordsmoother.api

import com.linh.pianoflow.core.model.Chord

interface ProgressionSolver {
    fun solve(
        chords: List<Chord>,
        anchor: Boolean,
        pins: Map<Int, Int> = emptyMap(),
    ): List<Voicing>
}
