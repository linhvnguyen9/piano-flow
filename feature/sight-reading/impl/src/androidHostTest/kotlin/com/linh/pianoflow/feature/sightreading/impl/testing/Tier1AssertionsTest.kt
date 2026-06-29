package com.linh.pianoflow.feature.sightreading.impl.testing

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.test.assertFailsWith

/**
 * Locks in the `noTruncate` gate's two halves, because it changed from
 * `TextLayoutResult.hasVisualOverflow` (which false-positives on fitting single-line
 * text) to a dropped-glyph check. Without this test a future "simplification" back to
 * hasVisualOverflow — or a no-op regression — would pass silently.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class Tier1AssertionsTest {

    @get:Rule
    val rule = createComposeRule()

    /** A genuinely clipped label (box far too narrow for its text) MUST be flagged. */
    @Test
    fun flags_real_truncation() {
        rule.setContent {
            Text(
                "Voicing 3 of 5",
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier.width(24.dp).testTag("noTruncate"),
            )
        }
        rule.waitForIdle()
        assertFailsWith<AssertionError> {
            Tier1Assertions.assertAll(rule, "_inspect_t1_truncated.png")
        }
    }

    /**
     * A fitting numeral with exaggerated trailing letterSpacing — the exact shape that
     * tripped `hasVisualOverflow` on the summary stat values — MUST NOT be flagged.
     */
    @Test
    fun passes_fitting_label_with_letterspacing() {
        rule.setContent {
            Text(
                "20",
                maxLines = 1,
                letterSpacing = 0.1.em,
                modifier = Modifier.width(120.dp).testTag("noTruncate"),
            )
        }
        rule.waitForIdle()
        Tier1Assertions.assertAll(rule, "_inspect_t1_fits.png") // must not throw
    }
}
