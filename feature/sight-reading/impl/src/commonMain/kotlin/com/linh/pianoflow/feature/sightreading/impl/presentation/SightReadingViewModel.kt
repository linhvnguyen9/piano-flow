package com.linh.pianoflow.feature.sightreading.impl.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linh.pianoflow.core.designsystem.StaffNoteState
import com.linh.pianoflow.core.model.Pitch
import com.linh.pianoflow.feature.sightreading.impl.domain.NoteRecord
import com.linh.pianoflow.feature.sightreading.impl.domain.SightReadingSettingsRepository
import com.linh.pianoflow.feature.sightreading.impl.domain.SightReadingSummary
import com.linh.pianoflow.feature.sightreading.impl.domain.summarize
import com.linh.pianoflow.feature.sightreading.impl.domain.whiteNotesIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.time.TimeSource
import kotlin.time.DurationUnit

/**
 * Drives the single-note sight-reading drill — a Compose/Kotlin port of the design
 * template's engine. Picks a random natural note in range, times how long the player
 * takes to tap the matching key, and (on a wrong tap) waits for the correct key before
 * advancing. A fixed session is [SESSION_LENGTH] notes, then the [SightReadingScreen.SUMMARY]
 * report is computed by [summarize].
 *
 * The clef/range and "require the correct key" miss behaviour are fixed to the design's
 * defaults; the only in-app setting is "show note names" (the sheet's clef/range row is
 * "Soon"). All timing-sensitive raw state lives in private fields; [publish] maps it to
 * the immutable [SightReadingUiState] the stateless screen renders.
 */
class SightReadingViewModel(
    private val settings: SightReadingSettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SightReadingUiState())
    val state: StateFlow<SightReadingUiState> = _state.asStateFlow()

    private val lowMidi = 48   // C3
    private val highMidi = 72  // C5

    private var screen = SightReadingScreen.START
    private var note = 65
    private var phase = Phase.IDLE
    private var missedThisNote = false
    private var index = 0
    private var streak = 0
    private val records = mutableListOf<NoteRecord>()
    private var noteStart: TimeSource.Monotonic.ValueTimeMark? = null

    private var showNames = true
    private var showMiddleC = true
    private var settingsOpen = false

    private var fbVisible = false
    private var fbCorrect = false
    private var fbTimeSeconds: Double? = null
    private var fbNoteHint: String? = null
    private var litKeys: Set<Int> = emptySet()

    private var summary = SightReadingSummary(0.0, 0, 0, emptyList())

    private var tickJob: Job? = null
    private val pending = mutableListOf<Job>()

    init {
        publish()
        // Seed from disk on creation and reflect any later change (e.g. a fresh VM after a
        // tab switch re-reads the persisted toggles).
        viewModelScope.launch {
            settings.preferences.collect { prefs ->
                showNames = prefs.showNoteNames
                showMiddleC = prefs.showMiddleC
                publish()
            }
        }
    }

    // --- intents --------------------------------------------------------------

    fun startSession() {
        cancelPending()
        screen = SightReadingScreen.DRILL
        index = 0
        records.clear()
        streak = 0
        phase = Phase.IDLE
        missedThisNote = false
        clearFeedback()
        settingsOpen = false
        pickNote()
        startTicker()
        publish()
    }

    fun handleTap(midi: Int) {
        if (screen != SightReadingScreen.DRILL) return
        if (phase == Phase.CORRECT) return

        if (phase == Phase.INCORRECT_WAIT) {
            // Must find the correct key before moving on; the note already counts as missed.
            if (midi != note) return
            records += NoteRecord(Pitch.name(note), elapsedSeconds(), missed = true)
            phase = Phase.CORRECT
            litKeys = emptySet()
            publish()
            schedule(260) { advance() }
            return
        }

        val elapsed = elapsedSeconds()
        if (midi == note) {
            records += NoteRecord(Pitch.name(note), elapsed, missed = missedThisNote)
            phase = Phase.CORRECT
            streak = if (missedThisNote) 0 else streak + 1
            litKeys = emptySet()
            fbVisible = true
            fbCorrect = true
            fbTimeSeconds = elapsed
            fbNoteHint = null
            publish()
            schedule(300) { advance() }
        } else {
            missedThisNote = true
            phase = Phase.INCORRECT_WAIT
            streak = 0
            litKeys = setOf(note)
            fbVisible = true
            fbCorrect = false
            fbTimeSeconds = null
            fbNoteHint = "that was ${Pitch.name(note)}"
            publish()
        }
    }

    fun openSettings() { settingsOpen = true; publish() }
    fun closeSettings() { settingsOpen = false; publish() }
    // Update locally for an instant toggle, then persist; the preferences flow re-emits the
    // same value and reconciles (idempotent).
    fun setShowNoteNames(value: Boolean) {
        showNames = value
        publish()
        viewModelScope.launch { settings.setShowNoteNames(value) }
    }

    fun setShowMiddleC(value: Boolean) {
        showMiddleC = value
        publish()
        viewModelScope.launch { settings.setShowMiddleC(value) }
    }

    // --- flow -----------------------------------------------------------------

    private fun advance() {
        val next = index + 1
        if (next >= SESSION_LENGTH) {
            index = next
            finish()
            return
        }
        index = next
        phase = Phase.IDLE
        missedThisNote = false
        clearFeedback()
        pickNote()
        publish()
    }

    private fun finish() {
        cancelPending()
        tickJob?.cancel()
        tickJob = null
        screen = SightReadingScreen.SUMMARY
        summary = summarize(records)
        publish()
    }

    private fun pickNote() {
        val pool = whiteNotesIn(lowMidi, highMidi).filter { it != note }
        note = if (pool.isEmpty()) note else pool.random()
        noteStart = TimeSource.Monotonic.markNow()
    }

    private fun elapsedSeconds(): Double =
        noteStart?.elapsedNow()?.toDouble(DurationUnit.SECONDS) ?: 0.0

    private fun clearFeedback() {
        fbVisible = false
        fbCorrect = false
        fbTimeSeconds = null
        fbNoteHint = null
        litKeys = emptySet()
    }

    private fun startTicker() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (screen == SightReadingScreen.DRILL) {
                delay(100)
                if (!fbVisible && phase == Phase.IDLE) publish() // refresh the live timer
            }
        }
    }

    private fun schedule(ms: Long, block: () -> Unit) {
        pending += viewModelScope.launch {
            delay(ms)
            block()
        }
    }

    private fun cancelPending() {
        pending.forEach { it.cancel() }
        pending.clear()
    }

    override fun onCleared() {
        tickJob?.cancel()
        cancelPending()
        super.onCleared()
    }

    // --- render ---------------------------------------------------------------

    private fun publish() {
        val staffState = when (phase) {
            Phase.CORRECT -> StaffNoteState.Correct
            Phase.IDLE -> StaffNoteState.Default
            Phase.INCORRECT_WAIT -> StaffNoteState.Incorrect
        }
        val feedback = if (fbVisible) {
            Feedback(
                correct = fbCorrect,
                timeSeconds = fbTimeSeconds,
                noteHint = fbNoteHint,
                streak = streak,
                showStreak = fbCorrect && streak >= 3,
            )
        } else {
            null
        }
        _state.value = SightReadingUiState(
            screen = screen,
            note = note,
            heroNote = HERO_NOTE,
            staffState = staffState,
            showNoteName = showNames,
            showMiddleC = showMiddleC,
            noteName = Pitch.name(note),
            progressText = "${minOf(index + 1, SESSION_LENGTH)} / $SESSION_LENGTH",
            progressFraction = (index.toFloat() / SESSION_LENGTH).coerceIn(0f, 1f),
            feedback = feedback,
            liveTimerSeconds = if (screen == SightReadingScreen.DRILL && !fbVisible) elapsedSeconds() else null,
            litKeys = litKeys,
            keyboardStart = lowMidi,
            keyboardEnd = highMidi,
            rangeLabel = RANGE_LABEL,
            settingsOpen = settingsOpen,
            summary = summary,
            touchHint = TOUCH_HINT,
        )
    }

    private enum class Phase { IDLE, CORRECT, INCORRECT_WAIT }

    private companion object {
        const val SESSION_LENGTH = 20
        const val HERO_NOTE = 67 // G4, the welcoming note on the Start screen
        const val RANGE_LABEL = "C3–C5 · both clefs · naturals"
        const val TOUCH_HINT =
            "Heads-up: 15 keys ≈ 22dp each on a 360dp screen — below the 48dp touch target. " +
                "Keys use full-height hit areas; switch to a narrower range if misses cluster."
    }
}
