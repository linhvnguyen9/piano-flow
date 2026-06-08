package com.linh.pianoflow.feature.chordsmoother.api

import com.linh.pianoflow.core.model.Chord

interface ChordProgressionParser {
    fun parse(input: String): List<Pair<String, Chord?>>
    fun parseChord(token: String): Chord?
}
