package com.linh.pianoflow.core.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xxhdpi", sdk = [35])
class PianoKeyboardScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pianoKeyboard_light_cMajorTriad() =
        capture(dark = false, golden = "piano_keyboard_default.png")

    @Test
    fun pianoKeyboard_dark_cMajorTriad() =
        capture(dark = true, golden = "piano_keyboard_default_dark.png")

    private fun capture(dark: Boolean, golden: String) {
        composeRule.setContent {
            PianoFlowTheme(darkTheme = dark) {
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
            filePath = "src/androidHostTest/screenshots/$golden",
        )
    }
}
