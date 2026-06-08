package com.linh.pianoflow.ui.songs

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
 * Inspection renders of the real chord-picker components (field + sheet). These
 * write to build/outputs/roborazzi/ for visual review; there is no committed
 * golden here (see ChordPickerScreenshotTest for the regression golden).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xxhdpi", sdk = [35])
class ChordPickerInspection {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun inspect_field_empty() {
        composeRule.setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ChordProgressionField(
                        tokens = emptyList(),
                        editingText = "",
                        examples = listOf("C | G | Am | F", "F | G | Em | Am", "Asus4 | G7 | Cmaj7 | Am7"),
                        onTokensChange = {},
                        onEditingTextChange = {},
                        onChipTap = {},
                        onOpenPicker = {},
                        onPickExample = {},
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/_inspect_field_empty.png",
        )
    }

    @Test
    fun inspect_field_populated() {
        composeRule.setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ChordProgressionField(
                        tokens = listOf("C", "G", "Am", "F"),
                        editingText = "Dm7",
                        examples = emptyList(),
                        onTokensChange = {},
                        onEditingTextChange = {},
                        onChipTap = {},
                        onOpenPicker = {},
                        onPickExample = {},
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/_inspect_field_populated.png",
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h1200dp-xxhdpi", sdk = [35])
    fun inspect_picker_add() {
        composeRule.setContent {
            MaterialTheme {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                ) {
                    ChordPickerContent(initial = null, onConfirm = {}, onDelete = null)
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/_inspect_picker_add.png",
        )
    }

    @Test
    @Config(qualifiers = "w360dp-h1200dp-xxhdpi", sdk = [35])
    fun inspect_picker_edit() {
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
            filePath = "build/outputs/roborazzi/_inspect_picker_edit.png",
        )
    }
}
