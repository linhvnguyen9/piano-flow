package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.core.model.Pitch
import com.linh.pianoflow.core.model.Quality
import com.linh.pianoflow.feature.chordsmoother.impl.domain.candidates
import com.linh.pianoflow.feature.chordsmoother.impl.domain.keyboardRange
import com.linh.pianoflow.core.designsystem.PianoKeyboard
import com.linh.pianoflow.core.designsystem.theme.Pill
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme

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

    // Outer column splits into a scrollable form body + a pinned action footer.
    // Without this, a tall ModalBottomSheet bounds the content height and the
    // trailing action row gets compressed to a sliver (see ChordPickerInspection
    // .inspect_picker_edit_bounded).
    Column(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                if (isEdit) "Edit chord" else "Add chord",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Preview",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        Pitch.NAMES[rootPc] + quality.suffix(),
                        style = PianoFlowTheme.extendedTypography.musicData.copy(fontSize = 28.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(6.dp)
                    ) {
                        PianoKeyboard(
                            startMidi = range.first,
                            endMidi = range.second,
                            highlighted = previewVoicing?.notes?.toSet() ?: emptySet(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Text("Root", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Pitch.NAMES.forEachIndexed { pc, name ->
                    FilterChip(
                        selected = pc == rootPc,
                        onClick = { rootPc = pc },
                        label = { Text(name) },
                        shape = Pill,
                        colors = selectedTealChipColors(),
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
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
                            FilterChip(
                                selected = q == quality,
                                onClick = { quality = q },
                                label = { Text(label) },
                                shape = Pill,
                                colors = selectedTealChipColors(),
                                modifier = Modifier.heightIn(min = 48.dp),
                            )
                        }
                    }
                }
            }
        }

        // Pinned action footer — always visible at full height, never compressed.
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 12.dp),
        ) {
            if (isEdit) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { onDelete?.invoke() },
                        shape = Pill,
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    ) { Text("Delete") }
                    Button(
                        onClick = { onConfirm(Chord(rootPc, quality)) },
                        shape = Pill,
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) {
                        Text("Update")
                    }
                }
            } else {
                Button(
                    onClick = { onConfirm(Chord(rootPc, quality)) },
                    shape = Pill,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text("Add chord")
                }
            }
        }
    }
}

@Composable
private fun selectedTealChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
)
