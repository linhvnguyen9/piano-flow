package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Behavior test: tapping anywhere on the field container must focus the text input,
 * not just a tap that happens to land on the small editor region. Regression guard
 * for the "tap the box, no cursor/keyboard" bug.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xxhdpi", sdk = [35])
class ChordProgressionFieldTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tappingContainer_focusesTextInput() {
        composeRule.setContent {
            MaterialTheme {
                ChordProgressionField(
                    tokens = emptyList(),
                    editingText = "",
                    examples = emptyList(),
                    onTokensChange = {},
                    onEditingTextChange = {},
                    onChipTap = {},
                    onOpenPicker = {},
                    onPickExample = {},
                )
            }
        }
        // Center of the (full-width) container is over empty space, not the editor.
        composeRule.onNodeWithTag("chordFieldContainer").performClick()
        composeRule.onNodeWithTag("chordFieldInput", useUnmergedTree = true).assertIsFocused()
    }

    @Test
    fun typingLowercaseChord_commitsCapitalizedToken() {
        var captured: List<String> = emptyList()
        composeRule.setContent {
            MaterialTheme {
                var tokens by remember { mutableStateOf(emptyList<String>()) }
                var editing by remember { mutableStateOf("") }
                ChordProgressionField(
                    tokens = tokens,
                    editingText = editing,
                    examples = emptyList(),
                    onTokensChange = { tokens = it; captured = it },
                    onEditingTextChange = { editing = it },
                    onChipTap = {},
                    onOpenPicker = {},
                    onPickExample = {},
                )
            }
        }
        // Literal input (no IME capitalization in tests); normalization must capitalize.
        composeRule.onNodeWithTag("chordFieldInput", useUnmergedTree = true).performTextInput("e ")
        composeRule.runOnIdle { assertEquals(listOf("E"), captured) }
    }
}
