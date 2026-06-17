package com.linh.pianoflow.showcase

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.android.showkase.annotation.ShowkaseComposable
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme
import com.linh.pianoflow.feature.chordsmoother.impl.presentation.ChordProgressionField

@ShowkaseComposable(name = "ChordProgressionField", group = "Songs")
@Composable
fun ChordProgressionFieldShowcase() {
    PianoFlowTheme {
        Surface {
            ChordProgressionField(
                tokens = listOf("Cmaj7", "Am7", "Dm7", "G7"),
                editingText = "",
                examples = listOf("ii-V-I", "12-bar blues"),
                onTokensChange = {},
                onEditingTextChange = {},
                onChipTap = {},
                onOpenPicker = {},
                onPickExample = {},
                modifier = Modifier.fillMaxWidth().padding(8.dp),
            )
        }
    }
}
