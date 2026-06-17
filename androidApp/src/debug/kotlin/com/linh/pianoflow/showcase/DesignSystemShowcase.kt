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
