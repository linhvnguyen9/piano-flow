package com.linh.pianoflow.ui.songs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.core.model.Pitch
import com.linh.pianoflow.core.model.Quality
import com.linh.pianoflow.songs.candidates
import com.linh.pianoflow.songs.keyboardRange
import com.linh.pianoflow.core.designsystem.PianoKeyboard

private val QUALITY_GROUPS: List<Pair<String, List<Pair<Quality, String>>>> = listOf(
    "Triads" to listOf(
        Quality.MAJ to "Maj", Quality.MIN to "Min", Quality.DIM to "Dim",
        Quality.AUG to "Aug", Quality.SUS2 to "Sus2", Quality.SUS4 to "Sus4",
    ),
    "Sevenths" to listOf(
        Quality.DOM7 to "7", Quality.MAJ7 to "Maj7", Quality.MIN7 to "m7",
        Quality.DIM7 to "dim7", Quality.M7B5 to "m7♭5",
    ),
    "Sixths" to listOf(Quality.MAJ6 to "6", Quality.MIN6 to "m6"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChordPickerSheet(
    initial: Chord?,
    onConfirm: (Chord) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        ChordPickerContent(initial = initial, onConfirm = onConfirm, onDelete = onDelete)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChordPickerContent(
    initial: Chord?,
    onConfirm: (Chord) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var rootPc by remember { mutableStateOf(initial?.rootPc ?: 0) }
    var quality by remember { mutableStateOf(initial?.quality ?: Quality.MAJ) }
    val isEdit = initial != null
    val previewVoicing = remember(rootPc, quality) {
        candidates(Chord(rootPc, quality)).firstOrNull()
    }
    val range = previewVoicing?.let { keyboardRange(listOf(it)) } ?: (60 to 72)

    Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            if (isEdit) "Edit chord" else "Add chord",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Preview",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    Pitch.NAMES[rootPc] + quality.suffix(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                PianoKeyboard(
                    startMidi = range.first,
                    endMidi = range.second,
                    highlighted = previewVoicing?.notes?.toSet() ?: emptySet(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Text("Root", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Pitch.NAMES.forEachIndexed { pc, name ->
                FilterChip(selected = pc == rootPc, onClick = { rootPc = pc }, label = { Text(name) })
            }
        }

        Text("Quality", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        QUALITY_GROUPS.forEach { (title, items) ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items.forEach { (q, label) ->
                        FilterChip(selected = q == quality, onClick = { quality = q }, label = { Text(label) })
                    }
                }
            }
        }

        if (isEdit) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onDelete?.invoke() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) { Text("Delete") }
                Button(onClick = { onConfirm(Chord(rootPc, quality)) }, modifier = Modifier.weight(1f)) {
                    Text("Update")
                }
            }
        } else {
            Button(onClick = { onConfirm(Chord(rootPc, quality)) }, modifier = Modifier.fillMaxWidth()) {
                Text("Add chord")
            }
        }
    }
}
