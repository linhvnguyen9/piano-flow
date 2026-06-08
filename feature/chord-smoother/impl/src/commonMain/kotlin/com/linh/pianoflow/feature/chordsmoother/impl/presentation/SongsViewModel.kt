package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linh.pianoflow.audio.TonePlayer
import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.core.model.Pitch
import com.linh.pianoflow.feature.chordsmoother.api.ChordProgressionParser
import com.linh.pianoflow.feature.chordsmoother.api.ProgressionSolver
import com.linh.pianoflow.feature.chordsmoother.impl.domain.candidates
import com.linh.pianoflow.feature.chordsmoother.impl.domain.keyboardRange
import com.linh.pianoflow.feature.chordsmoother.impl.domain.moveCost
import com.linh.pianoflow.feature.chordsmoother.impl.domain.rootBaseline
import com.linh.pianoflow.feature.chordsmoother.impl.domain.totalMovement
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val PLAY_STEP_MS = 750L

class SongsViewModel(
    private val solver: ProgressionSolver,
    private val parser: ChordProgressionParser,
    private val tonePlayer: TonePlayer,
) : ViewModel() {

    private val _state = MutableStateFlow(SongsUiState())
    val state: StateFlow<SongsUiState> = _state.asStateFlow()

    private var pins: Map<Int, Int> = emptyMap()
    private var playJob: Job? = null

    init { recompute() }

    fun setTokens(tokens: List<String>) {
        _state.value = _state.value.copy(tokens = tokens)
        pins = emptyMap()
        pruneCollapsed()
        recompute()
    }

    fun setEditingText(text: String) {
        _state.value = _state.value.copy(editingText = text)
    }

    fun setAnchor(value: Boolean) {
        _state.value = _state.value.copy(anchor = value)
        recompute()
    }

    fun toggleCollapsed(index: Int) {
        val c = _state.value.collapsed
        _state.value = _state.value.copy(
            collapsed = if (index in c) c - index else c + index,
        )
    }

    fun pinVoicing(index: Int, candidateIndex: Int) {
        pins = pins + (index to candidateIndex)
        recompute()
    }

    fun autoVoicing(index: Int) {
        pins = pins - index
        recompute()
    }

    fun confirmChord(targetIndex: Int?, token: String) {
        val tokens = _state.value.tokens
        val next = if (targetIndex == null) tokens + token
        else tokens.toMutableList().also { it[targetIndex] = token }
        setTokens(next)
        _state.value = _state.value.copy(editingText = "")
    }

    fun deleteChord(index: Int) {
        setTokens(_state.value.tokens.toMutableList().also { it.removeAt(index) })
    }

    fun pickExample(preset: String) {
        setTokens(preset.split(Regex("[|,\\s]+")).filter { it.isNotBlank() })
    }

    fun playProgression() {
        if (_state.value.isPlaying) return
        playJob = viewModelScope.launch {
            _state.value = _state.value.copy(isPlaying = true)
            try {
                _state.value.rows.forEachIndexed { i, r ->
                    _state.value = _state.value.copy(activeRow = i)
                    tonePlayer.playChord(r.voicing.notes.map { Pitch.freq(it) })
                    delay(PLAY_STEP_MS)
                }
            } finally {
                _state.value = _state.value.copy(activeRow = null, isPlaying = false)
            }
        }
    }

    fun playRow(index: Int) {
        val r = _state.value.rows.getOrNull(index) ?: return
        viewModelScope.launch { tonePlayer.playChord(r.voicing.notes.map { Pitch.freq(it) }) }
    }

    private fun pruneCollapsed() {
        val labeledCount = _state.value.tokens.count { parser.parseChord(it) != null }
        _state.value = _state.value.copy(
            collapsed = _state.value.collapsed.filter { it in 0 until labeledCount }.toSet(),
        )
    }

    private fun recompute() {
        val s = _state.value
        val parsed = s.tokens.map { it to parser.parseChord(it) }
        val errors = parsed.filter { it.second == null }.map { it.first }
        val labeled: List<Pair<String, Chord>> =
            parsed.mapNotNull { (tok, c) -> if (c != null) tok to c else null }
        val chords = labeled.map { it.second }
        val voicings = solver.solve(chords, s.anchor, pins)
        val baseline = rootBaseline(chords)
        val rows = if (voicings.size != labeled.size) emptyList() else
            labeled.mapIndexed { i, (tok, c) ->
                val v = voicings[i]
                val move = if (i == 0) null else moveCost(voicings[i - 1].notes, v.notes)
                val cands = candidates(c)
                val curIdx = cands.indexOfFirst { it.notes == v.notes }.let { if (it < 0) 0 else it }
                ChordRow(tok, v, move, c, cands.size, curIdx, pins.containsKey(i))
            }
        val (kbStart, kbEnd) = keyboardRange(voicings)
        _state.value = s.copy(
            errors = errors,
            rows = rows,
            keyboardStart = kbStart,
            keyboardEnd = kbEnd,
            totalMovement = totalMovement(voicings),
            baselineMovement = totalMovement(baseline),
        )
    }
}
