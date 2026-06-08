package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.feature.chordsmoother.api.Voicing

data class ChordRow(
    val label: String,
    val voicing: Voicing,
    val moveFromPrev: Double?,
    val chord: Chord,
    val candidateCount: Int,
    val currentCandidateIndex: Int,
    val pinned: Boolean,
)

data class SongsUiState(
    val tokens: List<String> = listOf("C", "G", "Am", "F"),
    val editingText: String = "",
    val anchor: Boolean = true,
    val errors: List<String> = emptyList(),
    val rows: List<ChordRow> = emptyList(),
    val keyboardStart: Int = 60,
    val keyboardEnd: Int = 72,
    val totalMovement: Double = 0.0,
    val baselineMovement: Double = 0.0,
    val activeRow: Int? = null,
    val isPlaying: Boolean = false,
    val collapsed: Set<Int> = emptySet(),
)
