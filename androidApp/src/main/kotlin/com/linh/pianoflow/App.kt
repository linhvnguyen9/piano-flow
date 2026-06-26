package com.linh.pianoflow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme
import com.linh.pianoflow.feature.chordsmoother.api.SongsKey
import dev.enro.asInstance
import dev.enro.backstackOf
import dev.enro.ui.NavigationDisplay
import dev.enro.ui.rememberNavigationContainer

@Composable
fun App() {
    PianoFlowTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            SafeAreaContainer {
                val container = rememberNavigationContainer(
                    backstack = backstackOf(SongsKey.asInstance()),
                )
                NavigationDisplay(state = container)
            }
        }
    }
}

/**
 * Edge-to-edge content container. The background fills the whole window; this pads
 * [content] by [insets] — the **vertical** safe area by default (status bar + home
 * indicator) only, because screens own their horizontal 24dp margin and padding both
 * axes would double it.
 *
 * Extracted from [App] so the inset contract is a named, testable seam: `AppInsetsTest`
 * renders it with injected non-zero insets and asserts the content clears them. This is
 * the app-layer Tier-1 inset assertion — feature content composables deliberately do NOT
 * pad insets (App does it for them), so the check lives here, not in the feature module.
 */
@Composable
internal fun SafeAreaContainer(
    modifier: Modifier = Modifier,
    insets: WindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical),
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.windowInsetsPadding(insets)) { content() }
}
