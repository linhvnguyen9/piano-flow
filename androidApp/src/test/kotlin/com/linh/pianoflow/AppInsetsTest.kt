package com.linh.pianoflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.WindowInsets
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.abs

/**
 * Tier-1 **insets** assertion (mobile-design rubric, structural blockers) — the last of the
 * deterministic structural checks, asserted at the layer that owns insets.
 *
 * Insets are owned by the app shell, not the feature screens: [App] wraps navigation in
 * [SafeAreaContainer] (vertical safe area), and the feature content composables deliberately
 * do NOT pad insets — doing so would double-pad. So this check lives here, not in the feature
 * module's `Tier1Assertions`.
 *
 * Robolectric reports zero insets by default (a naive check is a no-op), so we inject non-zero
 * system bars via `DeviceConfigurationOverride.WindowInsets` — the canonical headless technique
 * (used by Now in Android's host-side Roborazzi tests) — and assert the content clears them.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class AppInsetsTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val statusBarTopPx = 63
    private val navBarBottomPx = 48

    /** Non-zero system bars injected into the composition (Robolectric defaults to zero). */
    private val systemBars: WindowInsetsCompat = WindowInsetsCompat.Builder()
        .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, statusBarTopPx, 0, 0))
        .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, navBarBottomPx))
        .build()

    @Test
    fun `SafeAreaContainer insets content below the status bar`() {
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.WindowInsets(systemBars)) {
                PianoFlowTheme {
                    SafeAreaContainer {
                        Box(Modifier.testTag("content").fillMaxSize())
                    }
                }
            }
        }
        composeRule.waitForIdle()

        // boundsInRoot is in px; content's top must equal the injected status-bar inset.
        val top = composeRule.onNodeWithTag("content").fetchSemanticsNode().boundsInRoot.top
        assert(abs(top - statusBarTopPx) <= 1f) {
            "Expected content top ≈ ${statusBarTopPx}px (cleared the status bar), got ${top}px — " +
                "SafeAreaContainer is not applying the top safe-area inset."
        }
    }
}
