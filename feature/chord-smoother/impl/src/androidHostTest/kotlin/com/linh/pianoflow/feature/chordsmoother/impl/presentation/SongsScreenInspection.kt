package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme
import com.linh.pianoflow.feature.chordsmoother.impl.domain.DefaultChordProgressionParser
import com.linh.pianoflow.feature.chordsmoother.impl.domain.DefaultProgressionSolver
import com.linh.pianoflow.feature.chordsmoother.impl.domain.candidates
import com.linh.pianoflow.feature.chordsmoother.impl.domain.keyboardRange
import com.linh.pianoflow.feature.chordsmoother.impl.domain.moveCost
import com.linh.pianoflow.feature.chordsmoother.impl.domain.rootBaseline
import com.linh.pianoflow.feature.chordsmoother.impl.domain.totalMovement
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Inspection tests render screens to PNGs in `build/outputs/roborazzi/` for
 * visual review. They are not screenshot regression tests — there is no
 * committed golden, and a "passing" run just means the composable rendered
 * without crashing. Useful for sanity-checking layout while iterating.
 *
 * Renders the stateless [SongsScreenContent] with a sample state computed from
 * the real domain, so no Koin/ViewModel wiring is needed in the test.
 *
 * Run with: `./gradlew :feature:impl:chord-smoother:testAndroidHostTest \
 *   --tests "com.linh.pianoflow.feature.chordsmoother.impl.presentation.SongsScreenInspection" \
 *   -Proborazzi.test.record=true`
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h800dp-xxhdpi", sdk = [35])
class SongsScreenInspection {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun inspect_songsScreen_default() {
        composeRule.setContent {
            PianoFlowTheme { SampleSongsScreen() }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/_inspect_songs_screen.png",
        )
    }

    /**
     * Tall canvas variant: makes the viewport taller than any reasonable
     * progression so the LazyColumn lays out every row, then snapshots
     * the whole thing.
     */
    @Test
    @Config(qualifiers = "w360dp-h2000dp-xxhdpi", sdk = [35])
    fun inspect_songsScreen_fullProgression() {
        composeRule.setContent {
            PianoFlowTheme { SampleSongsScreen() }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/_inspect_songs_screen_full.png",
        )
    }
}

@Composable
private fun SampleSongsScreen() {
    SongsScreenContent(
        state = sampleState(listOf("C", "G", "Am", "F")),
        onTokensChange = {},
        onEditingTextChange = {},
        onAnchorChange = {},
        onPlay = {},
        onPlayRow = {},
        onToggleCollapse = {},
        onPin = { _, _ -> },
        onAuto = {},
        onConfirmChord = { _, _ -> },
        onDeleteChord = {},
        onPickExample = {},
        parseToken = { null },
    )
}

private fun sampleState(tokens: List<String>): SongsUiState {
    val parser = DefaultChordProgressionParser()
    val solver = DefaultProgressionSolver()
    val labeled = tokens.mapNotNull { t -> parser.parseChord(t)?.let { t to it } }
    val chords = labeled.map { it.second }
    val voicings = solver.solve(chords, anchor = true)
    val rows = labeled.mapIndexed { i, (t, c) ->
        val v = voicings[i]
        val cands = candidates(c)
        ChordRow(
            label = t,
            voicing = v,
            moveFromPrev = if (i == 0) null else moveCost(voicings[i - 1].notes, v.notes),
            chord = c,
            candidateCount = cands.size,
            currentCandidateIndex = cands.indexOfFirst { it.notes == v.notes }.coerceAtLeast(0),
            pinned = false,
        )
    }
    val (kbStart, kbEnd) = keyboardRange(voicings)
    return SongsUiState(
        tokens = tokens,
        rows = rows,
        keyboardStart = kbStart,
        keyboardEnd = kbEnd,
        totalMovement = totalMovement(voicings),
        baselineMovement = totalMovement(rootBaseline(chords)),
    )
}
