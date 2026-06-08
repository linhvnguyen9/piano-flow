package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.linh.pianoflow.core.designsystem.theme.BottomSheet
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme
import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.core.model.Quality
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Committed screenshot goldens for the chord picker sheet (edit mode), in both
 * light and dark themes. Diffs fail the build. Goldens live under
 * src/androidHostTest/screenshots/.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h1200dp-xxhdpi", sdk = [35])
class ChordPickerScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun chordPickerSheet_editMode_light() =
        capture(dark = false, golden = "chord_picker_sheet_edit.png")

    @Test
    fun chordPickerSheet_editMode_dark() =
        capture(dark = true, golden = "chord_picker_sheet_edit_dark.png")

    private fun capture(dark: Boolean, golden: String) {
        composeRule.setContent {
            PianoFlowTheme(darkTheme = dark) {
                Surface(shape = BottomSheet) {
                    ChordPickerContent(
                        initial = Chord(0, Quality.MAJ7),
                        onConfirm = {},
                        onDelete = {},
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(
            filePath = "src/androidHostTest/screenshots/$golden",
        )
    }
}
