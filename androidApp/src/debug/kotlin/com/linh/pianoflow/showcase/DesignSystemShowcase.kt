package com.linh.pianoflow.showcase

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.linh.pianoflow.core.designsystem.PianoKeyboard
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme

/**
 * Showcase wrappers for :core:designsystem components. Zero-arg, themed renders used by
 * the Showkase browser and the Roborazzi catalog. Debug-only — these are catalog entries,
 * not shipped UI. Only module-public components belong here.
 */
@ShowkaseComposable(name = "PianoKeyboard", group = "DesignSystem")
@Composable
fun PianoKeyboardShowcase() {
    PianoFlowTheme {
        Surface {
            PianoKeyboard(
                startMidi = 60,
                endMidi = 72,
                highlighted = setOf(60, 64, 67),
                labels = true,
                modifier = Modifier.fillMaxWidth().padding(8.dp),
            )
        }
    }
}
