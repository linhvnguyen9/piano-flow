package com.linh.pianoflow.ui.songs

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linh.pianoflow.audio.TonePlayer
import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.core.model.Pitch
import com.linh.pianoflow.songs.Voicing
import com.linh.pianoflow.songs.candidates
import com.linh.pianoflow.songs.chordToToken
import com.linh.pianoflow.songs.inversionName
import com.linh.pianoflow.songs.keyboardRange
import com.linh.pianoflow.songs.moveCost
import com.linh.pianoflow.songs.parseChord
import com.linh.pianoflow.songs.rootBaseline
import com.linh.pianoflow.songs.solve
import com.linh.pianoflow.songs.totalMovement
import com.linh.pianoflow.core.designsystem.PianoKeyboard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val PLAY_STEP_MS = 750L

private val EXAMPLES = listOf("C | G | Am | F", "F | G | Em | Am", "Asus4 | G7 | Cmaj7 | Am7")

private sealed interface PickerTarget {
    data object Add : PickerTarget
    data class Edit(val index: Int, val initial: Chord?) : PickerTarget
}

private data class ChordRow(
    val label: String,
    val voicing: Voicing,
    val moveFromPrev: Double?,
    val chord: Chord,
    val candidateCount: Int,
    val currentCandidateIndex: Int,
    val pinned: Boolean,
)

@Composable
fun SongsScreen() {
    var tokens by remember { mutableStateOf(listOf("C", "G", "Am", "F")) }
    var editingText by remember { mutableStateOf("") }
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }
    var anchor by remember { mutableStateOf(true) }
    var activeRow by remember { mutableStateOf<Int?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var pins by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var collapsed by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val parsed by remember(tokens) { derivedStateOf { tokens.map { it to parseChord(it) } } }
    val errors by remember(parsed) {
        derivedStateOf { parsed.filter { it.second == null }.map { it.first } }
    }
    val labeledChords by remember(parsed) {
        derivedStateOf {
            parsed.mapNotNull { (tok, c) -> if (c != null) tok to c else null }
        }
    }
    val chordsKey = labeledChords.map { it.second }
    LaunchedEffect(chordsKey) {
        pins = emptyMap()
        collapsed = collapsed.filter { it in chordsKey.indices }.toSet()
    }

    val voicings by remember(chordsKey, anchor, pins) {
        derivedStateOf { solve(chordsKey, anchor, pins) }
    }
    val baseline by remember(chordsKey) {
        derivedStateOf { rootBaseline(chordsKey) }
    }
    val rows by remember(labeledChords, voicings, pins) {
        derivedStateOf {
            if (voicings.size != labeledChords.size) emptyList()
            else labeledChords.mapIndexed { i, (tok, c) ->
                val v = voicings[i]
                val move = if (i == 0) null else moveCost(voicings[i - 1].notes, v.notes)
                val cands = candidates(c)
                val curIdx = cands.indexOfFirst { it.notes == v.notes }.let {
                    if (it < 0) 0 else it
                }
                ChordRow(tok, v, move, c, cands.size, curIdx, pins.containsKey(i))
            }
        }
    }
    val totalMv by remember(voicings) { derivedStateOf { totalMovement(voicings) } }
    val baselineMv by remember(baseline) { derivedStateOf { totalMovement(baseline) } }
    val keyboard by remember(voicings) { derivedStateOf { keyboardRange(voicings) } }

    val player = remember { TonePlayer() }
    DisposableEffect(Unit) { onDispose { player.release() } }
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Songs — Chord Smoother",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                ChordProgressionField(
                    tokens = tokens,
                    editingText = editingText,
                    examples = EXAMPLES,
                    onTokensChange = { tokens = it },
                    onEditingTextChange = { editingText = it },
                    onChipTap = { i -> pickerTarget = PickerTarget.Edit(i, parseChord(tokens[i])) },
                    onOpenPicker = { pickerTarget = PickerTarget.Add },
                    onPickExample = { preset ->
                        tokens = preset.split(Regex("[|,\\s]+")).filter { it.isNotBlank() }
                        editingText = ""
                    },
                )
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Switch(checked = anchor, onCheckedChange = { anchor = it })
                    Text("Anchor register", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled = !isPlaying && rows.isNotEmpty(),
                        onClick = {
                            isPlaying = true
                            scope.launch {
                                try {
                                    rows.forEachIndexed { i, r ->
                                        activeRow = i
                                        player.playChord(r.voicing.notes.map { Pitch.freq(it) })
                                        delay(PLAY_STEP_MS)
                                    }
                                } finally {
                                    activeRow = null
                                    isPlaying = false
                                }
                            }
                        }
                    ) {
                        if (isPlaying) {
                            Text("Playing…")
                        } else {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Text("Play")
                        }
                    }
                }
            }

            if (errors.isNotEmpty()) {
                item {
                    Text(
                        "Couldn't read: " + errors.joinToString(", "),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            item {
                SummaryLine(rows.size, totalMv, baselineMv)
            }

            itemsIndexed(rows, key = { i, _ -> i }) { index, row ->
                Column {
                    if (index > 0) {
                        ConnectorPill(row.moveFromPrev ?: 0.0)
                    }
                    ChordCard(
                        row = row,
                        keyboardStart = keyboard.first,
                        keyboardEnd = keyboard.second,
                        active = activeRow == index,
                        keyboardCollapsed = index in collapsed,
                        onToggleCollapse = {
                            collapsed = if (index in collapsed) collapsed - index else collapsed + index
                        },
                        onTap = {
                            scope.launch {
                                player.playChord(row.voicing.notes.map { Pitch.freq(it) })
                            }
                        },
                        onPrev = {
                            if (row.candidateCount > 0) {
                                val next = (row.currentCandidateIndex - 1 + row.candidateCount) % row.candidateCount
                                pins = pins + (index to next)
                            }
                        },
                        onNext = {
                            if (row.candidateCount > 0) {
                                val next = (row.currentCandidateIndex + 1) % row.candidateCount
                                pins = pins + (index to next)
                            }
                        },
                        onAuto = {
                            pins = pins - index
                        }
                    )
                }
            }
        }

        val target = pickerTarget
        if (target != null) {
            ChordPickerSheet(
                initial = (target as? PickerTarget.Edit)?.initial,
                onConfirm = { chord ->
                    val tok = chordToToken(chord)
                    tokens = when (target) {
                        is PickerTarget.Add -> tokens + tok
                        is PickerTarget.Edit -> tokens.toMutableList().also { it[target.index] = tok }
                    }
                    editingText = ""
                    pickerTarget = null
                },
                onDelete = (target as? PickerTarget.Edit)?.let { t ->
                    {
                        tokens = tokens.toMutableList().also { it.removeAt(t.index) }
                        pickerTarget = null
                    }
                },
                onDismiss = { pickerTarget = null },
            )
        }
        }
    }
}

@Composable
private fun SummaryLine(rowCount: Int, optimized: Double, baseline: Double) {
    val text = when {
        rowCount == 0 -> "Type a progression to begin."
        rowCount == 1 -> "Nothing to smooth yet — add another chord."
        else -> "Hand movement: ${optimized.roundToInt()} semitones — " +
            "root-only would be ${baseline.roundToInt()}."
    }
    Text(text, style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun ConnectorPill(move: Double) {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                "↓ ${move.roundToInt()} semitones",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ChordCard(
    row: ChordRow,
    keyboardStart: Int,
    keyboardEnd: Int,
    active: Boolean,
    keyboardCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onTap: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onAuto: () -> Unit,
) {
    val targetColor = if (active) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceVariant
    val bg by animateColorAsState(targetColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggleCollapse() },
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    row.label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(72.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        inversionName(row.voicing.inv),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        row.voicing.notes.joinToString("  ") { Pitch.name(it) },
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                    )
                }
                IconButton(onClick = onTap) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Play chord",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                CollapseToggle(collapsed = keyboardCollapsed, onClick = onToggleCollapse)
            }
            if (!keyboardCollapsed) {
                Spacer(Modifier.height(8.dp))
                InversionPicker(
                    pinned = row.pinned,
                    positionLabel = "${row.currentCandidateIndex + 1} / ${row.candidateCount}",
                    enabled = row.candidateCount > 1,
                    onPrev = onPrev,
                    onNext = onNext,
                    onAuto = onAuto
                )
                Spacer(Modifier.height(8.dp))
                PianoKeyboard(
                    startMidi = keyboardStart,
                    endMidi = keyboardEnd,
                    highlighted = row.voicing.notes.toSet(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CollapseToggle(collapsed: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (collapsed) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
            contentDescription = if (collapsed) "Expand keyboard" else "Collapse keyboard",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun InversionPicker(
    pinned: Boolean,
    positionLabel: String,
    enabled: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onAuto: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StepButton(
            icon = Icons.Filled.KeyboardArrowLeft,
            contentDescription = "Previous voicing",
            enabled = enabled,
            onClick = onPrev
        )
        Text(
            "voicing $positionLabel",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        StepButton(
            icon = Icons.Filled.KeyboardArrowRight,
            contentDescription = "Next voicing",
            enabled = enabled,
            onClick = onNext
        )
        Spacer(Modifier.width(4.dp))
        AssistChip(
            onClick = onAuto,
            enabled = pinned,
            label = { Text(if (pinned) "Locked — reset" else "Auto") }
        )
    }
}

@Composable
private fun StepButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
