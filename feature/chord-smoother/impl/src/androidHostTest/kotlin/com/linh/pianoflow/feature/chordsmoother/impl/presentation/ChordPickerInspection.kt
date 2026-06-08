package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
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
 * Inspection renders of the real chord-picker components (field + sheet), in both
 * light and dark themes. These write to build/outputs/roborazzi/ for visual
 * review; there is no committed golden here (see ChordPickerScreenshotTest for the
 * regression golden).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xxhdpi", sdk = [35])
class ChordPickerInspection {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun inspect_field_empty_light() =
        render(dark = false, name = "_inspect_field_empty.png") { EmptyField() }

    @Test
    fun inspect_field_empty_dark() =
        render(dark = true, name = "_inspect_field_empty_dark.png") { EmptyField() }

    @Test
    fun inspect_field_populated_light() =
        render(dark = false, name = "_inspect_field_populated.png") { PopulatedField() }

    @Test
    fun inspect_field_populated_dark() =
        render(dark = true, name = "_inspect_field_populated_dark.png") { PopulatedField() }

    @Test
    @Config(qualifiers = "w360dp-h1200dp-xxhdpi", sdk = [35])
    fun inspect_picker_add_light() =
        render(dark = false, name = "_inspect_picker_add.png", sheet = true) {
            ChordPickerContent(initial = null, onConfirm = {}, onDelete = null)
        }

    @Test
    @Config(qualifiers = "w360dp-h1200dp-xxhdpi", sdk = [35])
    fun inspect_picker_add_dark() =
        render(dark = true, name = "_inspect_picker_add_dark.png", sheet = true) {
            ChordPickerContent(initial = null, onConfirm = {}, onDelete = null)
        }

    @Test
    @Config(qualifiers = "w360dp-h1200dp-xxhdpi", sdk = [35])
    fun inspect_picker_edit_light() =
        render(dark = false, name = "_inspect_picker_edit.png", sheet = true) {
            ChordPickerContent(initial = Chord(0, Quality.MAJ7), onConfirm = {}, onDelete = {})
        }

    @Test
    @Config(qualifiers = "w360dp-h1200dp-xxhdpi", sdk = [35])
    fun inspect_picker_edit_dark() =
        render(dark = true, name = "_inspect_picker_edit_dark.png", sheet = true) {
            ChordPickerContent(initial = Chord(0, Quality.MAJ7), onConfirm = {}, onDelete = {})
        }

    /** Wrap [content] in the theme + an appropriate Surface, then snapshot it. */
    private fun render(
        dark: Boolean,
        name: String,
        sheet: Boolean = false,
        content: @Composable () -> Unit,
    ) {
        composeRule.setContent {
            PianoFlowTheme(darkTheme = dark) {
                if (sheet) {
                    Surface(color = MaterialTheme.colorScheme.surface, shape = BottomSheet) { content() }
                } else {
                    Surface(color = MaterialTheme.colorScheme.background) { content() }
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(filePath = "build/outputs/roborazzi/$name")
    }
}

@Composable
private fun EmptyField() {
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

@Composable
private fun PopulatedField() {
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
