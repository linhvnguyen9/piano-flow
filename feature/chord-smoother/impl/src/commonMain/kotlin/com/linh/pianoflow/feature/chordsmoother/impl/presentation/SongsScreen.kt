package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linh.pianoflow.core.designsystem.PianoKeyboard
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme
import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.core.model.Pitch
import com.linh.pianoflow.feature.chordsmoother.api.ChordProgressionParser
import com.linh.pianoflow.feature.chordsmoother.impl.domain.chordToToken
import com.linh.pianoflow.feature.chordsmoother.impl.domain.inversionName
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private val EXAMPLES = listOf("C | G | Am | F", "F | G | Em | Am", "Asus4 | G7 | Cmaj7 | Am7")

private sealed interface PickerTarget {
    data object Add : PickerTarget
    data class Edit(val index: Int, val initial: Chord?) : PickerTarget
}

@Composable
fun SongsScreen(
    modifier: Modifier = Modifier,
    viewModel: SongsViewModel = koinViewModel(),
    parser: ChordProgressionParser = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SongsScreenContent(
        state = state,
        modifier = modifier,
        onTokensChange = viewModel::setTokens,
        onEditingTextChange = viewModel::setEditingText,
        onAnchorChange = viewModel::setAnchor,
        onPlay = viewModel::playProgression,
        onPlayRow = viewModel::playRow,
        onToggleCollapse = viewModel::toggleCollapsed,
        onPin = viewModel::pinVoicing,
        onAuto = viewModel::autoVoicing,
        onConfirmChord = viewModel::confirmChord,
        onDeleteChord = viewModel::deleteChord,
        onPickExample = viewModel::pickExample,
        parseToken = parser::parseChord,
    )
}

@Composable
fun SongsScreenContent(
    state: SongsUiState,
    modifier: Modifier = Modifier,
    onTokensChange: (List<String>) -> Unit,
    onEditingTextChange: (String) -> Unit,
    onAnchorChange: (Boolean) -> Unit,
    onPlay: () -> Unit,
    onPlayRow: (Int) -> Unit,
    onToggleCollapse: (Int) -> Unit,
    onPin: (Int, Int) -> Unit,
    onAuto: (Int) -> Unit,
    onConfirmChord: (Int?, String) -> Unit,
    onDeleteChord: (Int) -> Unit,
    onPickExample: (String) -> Unit,
    parseToken: (String) -> Chord?,
) {
    var pickerTarget by remember { mutableStateOf<PickerTarget?>(null) }

    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
                        tokens = state.tokens,
                        editingText = state.editingText,
                        examples = EXAMPLES,
                        onTokensChange = onTokensChange,
                        onEditingTextChange = onEditingTextChange,
                        onChipTap = { i -> pickerTarget = PickerTarget.Edit(i, parseToken(state.tokens[i])) },
                        onOpenPicker = { pickerTarget = PickerTarget.Add },
                        onPickExample = onPickExample,
                    )
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Switch(checked = state.anchor, onCheckedChange = onAnchorChange)
                        Text("Anchor register", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.width(8.dp))
                        Button(
                            enabled = !state.isPlaying && state.rows.isNotEmpty(),
                            onClick = onPlay
                        ) {
                            if (state.isPlaying) {
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

                if (state.errors.isNotEmpty()) {
                    item {
                        Text(
                            "Couldn't read: " + state.errors.joinToString(", "),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {
                    SummaryLine(state.rows.size, state.totalMovement, state.baselineMovement)
                }

                itemsIndexed(state.rows, key = { i, _ -> i }) { index, row ->
                    Column {
                        if (index > 0) {
                            ConnectorPill(row.moveFromPrev ?: 0.0)
                        }
                        ChordCard(
                            row = row,
                            keyboardStart = state.keyboardStart,
                            keyboardEnd = state.keyboardEnd,
                            active = state.activeRow == index,
                            keyboardCollapsed = index in state.collapsed,
                            onToggleCollapse = { onToggleCollapse(index) },
                            onTap = { onPlayRow(index) },
                            onPrev = {
                                if (row.candidateCount > 0) {
                                    val next = (row.currentCandidateIndex - 1 + row.candidateCount) % row.candidateCount
                                    onPin(index, next)
                                }
                            },
                            onNext = {
                                if (row.candidateCount > 0) {
                                    val next = (row.currentCandidateIndex + 1) % row.candidateCount
                                    onPin(index, next)
                                }
                            },
                            onAuto = { onAuto(index) }
                        )
                    }
                }
            }

            val target = pickerTarget
            if (target != null) {
                ChordPickerSheet(
                    initial = (target as? PickerTarget.Edit)?.initial,
                    onConfirm = { chord ->
                        onConfirmChord((target as? PickerTarget.Edit)?.index, chordToToken(chord))
                        pickerTarget = null
                    },
                    onDelete = (target as? PickerTarget.Edit)?.let { t ->
                        {
                            onDeleteChord(t.index)
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
                        style = PianoFlowTheme.extendedTypography.musicData
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
