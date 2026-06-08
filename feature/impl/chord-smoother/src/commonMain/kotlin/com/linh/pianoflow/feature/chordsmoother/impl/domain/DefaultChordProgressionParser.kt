package com.linh.pianoflow.feature.chordsmoother.impl.domain

import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.feature.chordsmoother.api.ChordProgressionParser

class DefaultChordProgressionParser : ChordProgressionParser {
    override fun parse(input: String): List<Pair<String, Chord?>> = parseProgression(input)
    override fun parseChord(token: String): Chord? = parseChordToken(token)
}
