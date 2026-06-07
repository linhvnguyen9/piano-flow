package com.linh.pianoflow.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xxhdpi")
class PianoKeyboardScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pianoKeyboard_defaultOneOctaveCMajorTriadHighlighted() {
        composeRule.setContent {
            MaterialTheme {
                Surface {
                    PianoKeyboard(
                        startMidi = 60,
                        endMidi = 72,
                        highlighted = setOf(60, 64, 67),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        composeRule.onRoot().captureRoboImage(
            filePath = "piano_keyboard_default.png",
        )
    }
}
