package com.linh.pianoflow.feature.sightreading.impl.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import com.linh.pianoflow.core.designsystem.StaffNoteState
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme
import com.linh.pianoflow.feature.sightreading.impl.domain.SightReadingSummary
import com.linh.pianoflow.feature.sightreading.impl.domain.SlowNote
import com.linh.pianoflow.feature.sightreading.impl.testing.Tier1Assertions
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Inspection tests render the stateless [SightReadingScreenContent] (and the settings
 * sheet content) to PNGs in `build/outputs/roborazzi/` for visual review by the
 * `mobile-design-evaluator`. They are not screenshot regression tests — no committed
 * golden; a "passing" run just means it rendered and cleared the Tier-1 structural bar.
 *
 * The matrix crosses each of the trainer's screens (Start · Drill · Summary · Settings)
 * with the `mobile-design` stress axes: width {360, 411}, font scale {1.0, 1.5, 2.0}
 * (via [LocalDensity] override, so `sp` grows while `dp` layout doesn't), and light/dark.
 * Drill is also captured mid-feedback (correct & incorrect). Filenames are
 * `_inspect_sightreading_<config>.png` — the single-token screen id `sightreading` is
 * shared with the evaluator so both lanes cluster on the same screen in the ledger.
 *
 * Heights are device-like (these screens are full-height flex layouts, not scrolling
 * lists), so vertical overflow at large font — e.g. the docked keyboard being pushed off
 * — surfaces rather than being hidden by an arbitrarily tall canvas.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class SightReadingScreenInspection {

    @get:Rule
    val composeRule = createComposeRule()

    // --- Start ----------------------------------------------------------------

    @Test
    @Config(qualifiers = "w411dp-h900dp-xxhdpi", sdk = [35])
    fun inspect_start_411_light() =
        captureScreen(false, 1f, startState(), "_inspect_sightreading_start_411.png")

    @Test
    @Config(qualifiers = "w411dp-h900dp-xxhdpi", sdk = [35])
    fun inspect_start_411_dark() =
        captureScreen(true, 1f, startState(), "_inspect_sightreading_start_411_dark.png")

    @Test
    @Config(qualifiers = "w360dp-h1500dp-xxhdpi", sdk = [35])
    fun inspect_start_compact_font2_0() =
        captureScreen(false, 2f, startState(), "_inspect_sightreading_start_compact_font2_0.png")

    // --- Drill (idle) ---------------------------------------------------------

    @Test
    @Config(qualifiers = "w411dp-h900dp-xxhdpi", sdk = [35])
    fun inspect_drill_411_light() =
        captureScreen(false, 1f, drillIdleState(), "_inspect_sightreading_drill_411.png")

    @Test
    @Config(qualifiers = "w411dp-h900dp-xxhdpi", sdk = [35])
    fun inspect_drill_411_dark() =
        captureScreen(true, 1f, drillIdleState(), "_inspect_sightreading_drill_411_dark.png")

    @Test
    @Config(qualifiers = "w360dp-h900dp-xxhdpi", sdk = [35])
    fun inspect_drill_compact_360_light() =
        captureScreen(false, 1f, drillIdleState(), "_inspect_sightreading_drill_compact_360.png")

    @Test
    @Config(qualifiers = "w411dp-h1100dp-xxhdpi", sdk = [35])
    fun inspect_drill_font1_5_light() =
        captureScreen(false, 1.5f, drillIdleState(), "_inspect_sightreading_drill_font1_5.png")

    /** Worst case: smallest width crossed with the largest accessibility font. */
    @Test
    @Config(qualifiers = "w360dp-h1500dp-xxhdpi", sdk = [35])
    fun inspect_drill_compact_font2_0_light() =
        captureScreen(false, 2f, drillIdleState(), "_inspect_sightreading_drill_compact_font2_0.png")

    // --- Drill (feedback) -----------------------------------------------------

    @Test
    @Config(qualifiers = "w411dp-h900dp-xxhdpi", sdk = [35])
    fun inspect_drillcorrect_411_light() =
        captureScreen(false, 1f, drillCorrectState(), "_inspect_sightreading_drillcorrect_411.png")

    @Test
    @Config(qualifiers = "w411dp-h900dp-xxhdpi", sdk = [35])
    fun inspect_drillincorrect_411_dark() =
        captureScreen(true, 1f, drillIncorrectState(), "_inspect_sightreading_drillincorrect_411_dark.png")

    // --- Summary --------------------------------------------------------------

    @Test
    @Config(qualifiers = "w411dp-h1700dp-xxhdpi", sdk = [35])
    fun inspect_summary_411_light() =
        captureScreen(false, 1f, summaryState(), "_inspect_sightreading_summary_411.png")

    @Test
    @Config(qualifiers = "w411dp-h1700dp-xxhdpi", sdk = [35])
    fun inspect_summary_411_dark() =
        captureScreen(true, 1f, summaryState(), "_inspect_sightreading_summary_411_dark.png")

    @Test
    @Config(qualifiers = "w360dp-h3400dp-xxhdpi", sdk = [35])
    fun inspect_summary_compact_font2_0() =
        captureScreen(false, 2f, summaryState(), "_inspect_sightreading_summary_compact_font2_0.png")

    // --- Settings sheet -------------------------------------------------------

    @Test
    @Config(qualifiers = "w411dp-h1000dp-xxhdpi", sdk = [35])
    fun inspect_settings_411_light() =
        captureSettings(false, 1f, settingsState(), "_inspect_sightreading_settings_411.png")

    @Test
    @Config(qualifiers = "w360dp-h1900dp-xxhdpi", sdk = [35])
    fun inspect_settings_compact_font2_0() =
        captureSettings(false, 2f, settingsState(), "_inspect_sightreading_settings_compact_font2_0.png")

    // --- helpers --------------------------------------------------------------

    private fun captureScreen(dark: Boolean, fontScale: Float, state: SightReadingUiState, name: String) =
        capture(dark, fontScale, name) {
            SightReadingScreenContent(
                state = state,
                onStartSession = {},
                onKeyTap = {},
                onOpenSettings = {},
                onCloseSettings = {},
                onToggleNoteNames = {},
            )
        }

    private fun captureSettings(dark: Boolean, fontScale: Float, state: SightReadingUiState, name: String) =
        capture(dark, fontScale, name) {
            Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                SettingsSheetContent(state = state, onToggleNoteNames = {})
            }
        }

    private fun capture(dark: Boolean, fontScale: Float, name: String, content: @Composable () -> Unit) {
        composeRule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = base.density, fontScale = fontScale),
            ) {
                PianoFlowTheme(darkTheme = dark) { content() }
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(filePath = "build/outputs/roborazzi/$name")
        Tier1Assertions.assertAll(composeRule, label = name)
    }
}

private fun startState() = SightReadingUiState(screen = SightReadingScreen.START)

private fun drillIdleState() = SightReadingUiState(
    screen = SightReadingScreen.DRILL,
    note = 65,
    staffState = StaffNoteState.Default,
    showNoteName = true,
    noteName = "F4",
    progressText = "3 / 20",
    progressFraction = 3f / 20f,
    feedback = null,
    liveTimerSeconds = 1.2,
    litKeys = emptySet(),
)

private fun drillCorrectState() = drillIdleState().copy(
    note = 67,
    staffState = StaffNoteState.Correct,
    noteName = "G4",
    liveTimerSeconds = null,
    feedback = Feedback(correct = true, timeSeconds = 1.4, noteHint = null, streak = 4, showStreak = true),
)

private fun drillIncorrectState() = drillIdleState().copy(
    note = 65,
    staffState = StaffNoteState.Incorrect,
    noteName = "F4",
    liveTimerSeconds = null,
    litKeys = setOf(65),
    feedback = Feedback(correct = false, timeSeconds = null, noteHint = "that was F4", streak = 0, showStreak = false),
)

private fun summaryState() = SightReadingUiState(
    screen = SightReadingScreen.SUMMARY,
    summary = SightReadingSummary(
        medianSeconds = 1.6,
        accuracyPercent = 85,
        notesDone = 20,
        slowest = listOf(
            SlowNote("F4", 2.4, 1f),
            SlowNote("B3", 2.1, 0.88f),
            SlowNote("E4", 1.9, 0.79f),
            SlowNote("C5", 1.5, 0.63f),
        ),
    ),
)

private fun settingsState() = SightReadingUiState(
    settingsOpen = true,
    showNoteName = true,
    touchHint = "Heads-up: 15 keys ≈ 22dp each on a 360dp screen — below the 48dp touch " +
        "target. Keys use full-height hit areas; switch to a narrower range if misses cluster.",
)
