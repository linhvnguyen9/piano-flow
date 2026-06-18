package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.github.takahirom.roborazzi.captureRoboImage
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme
import com.linh.pianoflow.feature.chordsmoother.impl.testing.Tier1Assertions
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
 * Inspection tests render the stateless [SongsScreenContent] to PNGs in
 * `build/outputs/roborazzi/` for visual review. They are not screenshot
 * regression tests — there is no committed golden, and a "passing" run just means
 * the composable rendered without crashing.
 *
 * **This class renders the evaluation matrix from the `mobile-design` skill** so an
 * evaluator pass can grade the worst case, not just the happy path. Two stress
 * axes are crossed against the default state, plus three content-resilience states:
 *
 *  - **Width** — `default` (411dp reference) and `compact` (360dp, small phone /
 *    split screen). Set per-method via `@Config(qualifiers = "wNNNdp-…")`; this is
 *    the lever Robolectric actually honors for display metrics.
 *  - **Font scale** — 1.0, 1.5 and 2.0 (accessibility text size, where overflow and
 *    truncation surface). Applied by overriding [LocalDensity]'s `fontScale`, so
 *    `sp` text grows while `dp` containers stay fixed — exactly how a real device
 *    scales text and where the layout has to absorb it. The worst case (compact +
 *    2.0) is rendered explicitly.
 *  - **Theme** — light and dark, every config.
 *  - **Content** — `default`, `long` (12-chord progression), `empty` (nothing
 *    entered) and `error` (unreadable tokens), so empty/error states and edge
 *    content are exercised, not just the four-chord happy path.
 *
 * Each capture is its own `@Test` because `setContent` may be called only once per
 * compose rule. Canvases are tall (`h…dp` qualifier) so the `LazyColumn` lays every
 * row out in one frame; this means vertical scroll-overflow is not exercised here
 * (the screen scrolls by design) — horizontal overflow/truncation, the main
 * font-scale failure mode, is height-independent and IS caught.
 *
 * Run the whole matrix and view the PNGs:
 * `./gradlew :feature:chord-smoother:impl:testAndroidHostTest \
 *   --tests "com.linh.pianoflow.feature.chordsmoother.impl.presentation.SongsScreenInspection" \
 *   -Proborazzi.test.record=true`
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class SongsScreenInspection {

    @get:Rule
    val composeRule = createComposeRule()

    // --- Default state × device matrix (width × font scale × theme) -------------

    @Test
    @Config(qualifiers = "w411dp-h2000dp-xxhdpi", sdk = [35])
    fun inspect_default_411_light() =
        capture(dark = false, fontScale = 1f, state = defaultState(), name = "_inspect_songs_default_411.png")

    @Test
    @Config(qualifiers = "w411dp-h2000dp-xxhdpi", sdk = [35])
    fun inspect_default_411_dark() =
        capture(dark = true, fontScale = 1f, state = defaultState(), name = "_inspect_songs_default_411_dark.png")

    @Test
    @Config(qualifiers = "w360dp-h2000dp-xxhdpi", sdk = [35])
    fun inspect_compact_360_light() =
        capture(dark = false, fontScale = 1f, state = defaultState(), name = "_inspect_songs_compact_360.png")

    @Test
    @Config(qualifiers = "w360dp-h2000dp-xxhdpi", sdk = [35])
    fun inspect_compact_360_dark() =
        capture(dark = true, fontScale = 1f, state = defaultState(), name = "_inspect_songs_compact_360_dark.png")

    @Test
    @Config(qualifiers = "w411dp-h2600dp-xxhdpi", sdk = [35])
    fun inspect_fontScale1_5_light() =
        capture(dark = false, fontScale = 1.5f, state = defaultState(), name = "_inspect_songs_font1_5.png")

    @Test
    @Config(qualifiers = "w411dp-h2600dp-xxhdpi", sdk = [35])
    fun inspect_fontScale1_5_dark() =
        capture(dark = true, fontScale = 1.5f, state = defaultState(), name = "_inspect_songs_font1_5_dark.png")

    /** Worst case: smallest width crossed with the largest accessibility font. */
    @Test
    @Config(qualifiers = "w360dp-h3200dp-xxhdpi", sdk = [35])
    fun inspect_fontScale2_0_compact_light() =
        capture(dark = false, fontScale = 2f, state = defaultState(), name = "_inspect_songs_font2_0_compact.png")

    @Test
    @Config(qualifiers = "w360dp-h3200dp-xxhdpi", sdk = [35])
    fun inspect_fontScale2_0_compact_dark() =
        capture(dark = true, fontScale = 2f, state = defaultState(), name = "_inspect_songs_font2_0_compact_dark.png")

    // --- Content-resilience states (rendered at a 360dp / 1.5 stress config) -----

    @Test
    @Config(qualifiers = "w360dp-h4000dp-xxhdpi", sdk = [35])
    fun inspect_longProgression_light() =
        capture(dark = false, fontScale = 1.5f, state = longProgressionState(), name = "_inspect_songs_long_compact_font1_5.png")

    @Test
    @Config(qualifiers = "w360dp-h4000dp-xxhdpi", sdk = [35])
    fun inspect_longProgression_dark() =
        capture(dark = true, fontScale = 1.5f, state = longProgressionState(), name = "_inspect_songs_long_compact_font1_5_dark.png")

    @Test
    @Config(qualifiers = "w360dp-h1400dp-xxhdpi", sdk = [35])
    fun inspect_empty_light() =
        capture(dark = false, fontScale = 1.5f, state = emptyState(), name = "_inspect_songs_empty_compact_font1_5.png")

    @Test
    @Config(qualifiers = "w360dp-h1400dp-xxhdpi", sdk = [35])
    fun inspect_empty_dark() =
        capture(dark = true, fontScale = 1.5f, state = emptyState(), name = "_inspect_songs_empty_compact_font1_5_dark.png")

    @Test
    @Config(qualifiers = "w360dp-h2400dp-xxhdpi", sdk = [35])
    fun inspect_error_light() =
        capture(dark = false, fontScale = 1.5f, state = errorState(), name = "_inspect_songs_error_compact_font1_5.png")

    @Test
    @Config(qualifiers = "w360dp-h2400dp-xxhdpi", sdk = [35])
    fun inspect_error_dark() =
        capture(dark = true, fontScale = 1.5f, state = errorState(), name = "_inspect_songs_error_compact_font1_5_dark.png")

    /**
     * Renders the screen with [fontScale] applied via [LocalDensity] (so `sp` text
     * scales but `dp` layout does not) in the requested theme, then snapshots it.
     */
    private fun capture(dark: Boolean, fontScale: Float, state: SongsUiState, name: String) {
        composeRule.setContent {
            val base = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = base.density, fontScale = fontScale),
            ) {
                PianoFlowTheme(darkTheme = dark) { SampleSongsScreen(state) }
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(filePath = "build/outputs/roborazzi/$name")
        Tier1Assertions.assertAll(composeRule, label = name)
    }
}

@Composable
private fun SampleSongsScreen(state: SongsUiState) {
    SongsScreenContent(
        state = state,
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

/** The four-chord happy path. */
private fun defaultState(): SongsUiState = buildState(listOf("C", "G", "Am", "F"))

/** A long, 12-chord progression — many rows, larger movement numbers, tall layout. */
private fun longProgressionState(): SongsUiState = buildState(
    listOf("C", "Am7", "Dm7", "G7", "Cmaj7", "Fmaj7", "Em", "Asus4", "D7", "G", "Em7", "Am"),
)

/** Nothing entered yet — the empty/initial state (no rows, Play disabled). */
private fun emptyState(): SongsUiState = SongsUiState(tokens = emptyList(), rows = emptyList())

/** Some tokens unreadable — exercises the `errors` row. */
private fun errorState(): SongsUiState = buildState(listOf("C", "H7", "Am", "Zz9"))

/**
 * Mirrors [SongsViewModel.recompute]: parseable tokens become [ChordRow]s, the rest
 * land in `errors`. Empty `tokens` is handled by [emptyState] (skips the solver).
 */
private fun buildState(tokens: List<String>): SongsUiState {
    val parser = DefaultChordProgressionParser()
    val solver = DefaultProgressionSolver()
    val parsed = tokens.map { it to parser.parseChord(it) }
    val errors = parsed.filter { it.second == null }.map { it.first }
    val labeled = parsed.mapNotNull { (tok, c) -> if (c != null) tok to c else null }
    val chords = labeled.map { it.second }
    val voicings = solver.solve(chords, anchor = true)
    val rows = if (voicings.size != labeled.size) emptyList() else
        labeled.mapIndexed { i, (tok, c) ->
            val v = voicings[i]
            val cands = candidates(c)
            ChordRow(
                label = tok,
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
        errors = errors,
        keyboardStart = kbStart,
        keyboardEnd = kbEnd,
        totalMovement = totalMovement(voicings),
        baselineMovement = totalMovement(rootBaseline(chords)),
    )
}
