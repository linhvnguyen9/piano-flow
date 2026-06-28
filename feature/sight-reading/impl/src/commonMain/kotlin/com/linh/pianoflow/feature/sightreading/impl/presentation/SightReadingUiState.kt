package com.linh.pianoflow.feature.sightreading.impl.presentation

import com.linh.pianoflow.core.designsystem.StaffNoteState
import com.linh.pianoflow.feature.sightreading.impl.domain.SightReadingSummary

/** Which of the trainer's three screens is showing. */
enum class SightReadingScreen { START, DRILL, SUMMARY }

/**
 * The post-answer feedback shown above the keyboard. [correct] picks the ✓/✗ glyph and
 * teal/error colour; [timeSeconds] is shown when correct, [noteHint] ("that was F4")
 * when wrong, and [showStreak] gates the "N in a row" line.
 */
data class Feedback(
    val correct: Boolean,
    val timeSeconds: Double?,
    val noteHint: String?,
    val streak: Int,
    val showStreak: Boolean,
)

/**
 * Everything the stateless `SightReadingScreenContent` needs to render — a render-ready
 * snapshot produced by [SightReadingViewModel]. Constructed directly in inspection tests
 * to exercise each screen/state without the live timers.
 */
data class SightReadingUiState(
    val screen: SightReadingScreen = SightReadingScreen.START,
    val note: Int = 65,
    val heroNote: Int = 67,
    val staffState: StaffNoteState = StaffNoteState.Default,
    val showNoteName: Boolean = true,
    val noteName: String = "F4",
    val progressText: String = "1 / 20",
    val progressFraction: Float = 0f,
    val feedback: Feedback? = null,
    val liveTimerSeconds: Double? = null,
    val litKeys: Set<Int> = emptySet(),
    val keyboardStart: Int = 48,
    val keyboardEnd: Int = 72,
    val rangeLabel: String = "C3–C5 · both clefs · naturals",
    val settingsOpen: Boolean = false,
    val summary: SightReadingSummary = SightReadingSummary(0.0, 0, 0, emptyList()),
    val touchHint: String = "",
)
