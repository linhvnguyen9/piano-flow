package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.core.model.Quality
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Committed screenshot golden for the chord picker sheet (edit mode). Diffs fail
 * the build. Mirrors PianoKeyboardScreenshotTest — golden lives under
 * src/androidHostTest/screenshots/.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h1200dp-xxhdpi", sdk = [35])
class ChordPickerScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun chordPickerSheet_editMode() {
        composeRule.setContent {
            MaterialTheme {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                ) {
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
            filePath = "src/androidHostTest/screenshots/chord_picker_sheet_edit.png",
        )
    }
}
