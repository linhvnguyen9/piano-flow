package com.linh.pianoflow.feature.chordsmoother.impl.domain

import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.feature.chordsmoother.api.ProgressionSolver
import com.linh.pianoflow.feature.chordsmoother.api.Voicing

class DefaultProgressionSolver : ProgressionSolver {
    override fun solve(chords: List<Chord>, anchor: Boolean, pins: Map<Int, Int>): List<Voicing> =
        solveVoiceLeading(chords, anchor, pins)
}
