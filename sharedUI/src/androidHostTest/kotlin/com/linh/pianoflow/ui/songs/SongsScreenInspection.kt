package com.linh.pianoflow.ui.songs

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
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
 * Run with: `./gradlew :sharedUI:testAndroidHostTest \
 *   --tests "com.linh.pianoflow.ui.songs.SongsScreenInspection" \
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
            MaterialTheme {
                SongsScreen()
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/_inspect_songs_screen.png",
        )
    }

    /**
     * Tall canvas variant: makes the viewport taller than any reasonable
     * progression so the LazyColumn lays out every row, then snapshots
     * the whole thing. Useful for reviewing the full scrolled content at
     * a glance.
     */
    @Test
    @Config(qualifiers = "w360dp-h4000dp-xxhdpi", sdk = [35])
    fun inspect_songsScreen_fullProgression() {
        composeRule.setContent {
            MaterialTheme {
                SongsScreen()
            }
        }
        composeRule.waitForIdle()
        composeRule.onRoot().captureRoboImage(
            filePath = "build/outputs/roborazzi/_inspect_songs_screen_full.png",
        )
    }
}
