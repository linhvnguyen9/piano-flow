package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linh.pianoflow.core.designsystem.AppIconButton
import com.linh.pianoflow.core.designsystem.PianoKeyboard
import com.linh.pianoflow.core.designsystem.isExpandedFontScale
import com.linh.pianoflow.core.designsystem.theme.Pill
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme
import com.linh.pianoflow.core.model.Chord
import com.linh.pianoflow.core.model.Pitch
import com.linh.pianoflow.core.model.Quality
import com.linh.pianoflow.feature.chordsmoother.api.ChordProgressionParser
import com.linh.pianoflow.feature.chordsmoother.impl.domain.chordToToken
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
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    Text(
                        "Chord Smoother",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    // Typeable chord field is retained from the previous design.
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
                    Button(
                        onClick = onPlay,
                        enabled = !state.isPlaying && state.rows.isNotEmpty(),
                        shape = Pill,
                        // 56dp is the Serene Practice pill-button baseline, but only a
                        // minimum: at large font scales the label wraps and the button
                        // grows taller rather than clipping "Play Progression" to "Play".
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp)
                    ) {
                        if (state.isPlaying) {
                            Text(
                                "Playing…",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Play Progression",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                item {
                    MovementSummary(state.rows.size, state.totalMovement, state.baselineMovement)
                }

                if (state.errors.isNotEmpty()) {
                    item {
                        Text(
                            "Couldn't read: " + state.errors.joinToString(", "),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (state.rows.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Anchor register",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Switch(checked = state.anchor, onCheckedChange = onAnchorChange)
                        }
                    }
                }

                itemsIndexed(state.rows, key = { i, _ -> i }) { index, row ->
                    Column {
                        if (index > 0) {
                            // The LazyColumn's 32dp item gap sits above this connector;
                            // match it below so the pill reads centered in the card seam.
                            ConnectorPill(row.moveFromPrev ?: 0.0, showCaption = index == 1)
                            Spacer(Modifier.height(32.dp))
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
private fun MovementSummary(rowCount: Int, optimized: Double, baseline: Double) {
    if (rowCount < 2) {
        val text = if (rowCount == 0) "Type a progression to begin."
        else "Nothing to smooth yet — add another chord."
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        return
    }
    val opt = optimized.roundToInt()
    val base = baseline.roundToInt()
    val saved = base - opt
    val improved = saved > 0
    val percent = if (base > 0) (saved * 100 / base) else 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Warm Honey Amber in both modes via the mode-independent fixed tokens.
        // The capsule holds only the headline metric so it never has to absorb the
        // before→after detail; that moves to the caption, which wraps freely. At large
        // font scale the headline " semitones" word wraps inside the (height-free)
        // capsule rather than overrunning its right edge.
        Surface(
            shape = Pill,
            color = MaterialTheme.colorScheme.secondaryFixedDim,
            shadowElevation = 1.dp,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    "$opt",
                    style = PianoFlowTheme.extendedTypography.musicData,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryFixed
                )
                Text(
                    " semitones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryFixed
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            when {
                improved && percent > 0 -> "Down from $base — saved $saved semitones ($percent% less hand movement)"
                improved -> "Down from $base — saved $saved semitones of hand movement"
                else -> "Already as smooth as it gets — no extra movement to trim"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ConnectorPill(move: Double, showCaption: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Honey Amber connector, mode-independent so it stays warm in dark mode.
        // One annotated Text (not a Row of two) so the "· smoothed" qualifier wraps as
        // part of the same string at large font scale — the capsule grows taller instead
        // of the suffix fragmenting and floating over the rounded edge.
        Surface(
            shape = Pill,
            color = MaterialTheme.colorScheme.secondaryFixedDim
        ) {
            Text(
                buildAnnotatedString {
                    append("↓ ${move.roundToInt()} semitones")
                    // Subtle qualifier so the hop reads as the optimized result.
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Default,
                            fontSize = MaterialTheme.typography.labelMedium.fontSize,
                            color = MaterialTheme.colorScheme.onSecondaryFixed.copy(alpha = 0.7f),
                        )
                    ) {
                        append("  ·  smoothed")
                    }
                },
                style = PianoFlowTheme.extendedTypography.musicData.copy(
                    color = MaterialTheme.colorScheme.onSecondaryFixed
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
        // One-time, calm caption near the first connector only.
        if (showCaption) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Smallest hand move between chords",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
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
    // Active rows lift via soft elevation + a subtle tonal shift rather than a
    // hard outline, per the Serene Practice "tonal layers, not borders" rule.
    val elevation by animateDpAsState(if (active) 8.dp else 1.dp)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (active) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        shadowElevation = elevation,
        tonalElevation = if (active) 2.dp else 0.dp,
        onClick = onTap,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    chordDisplayName(row.chord),
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.weight(1f)
                )
                PlayChordButton(onClick = onTap)
                CollapseToggle(collapsed = keyboardCollapsed, onClick = onToggleCollapse)
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.voicing.notes.joinToString(" ") { Pitch.name(it) },
                    style = PianoFlowTheme.extendedTypography.musicData,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                InversionBadge(row.voicing.inv)
            }
            if (!keyboardCollapsed) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(6.dp)
                ) {
                    PianoKeyboard(
                        startMidi = keyboardStart,
                        endMidi = keyboardEnd,
                        highlighted = row.voicing.notes.toSet(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (row.candidateCount > 1) {
                    Spacer(Modifier.height(12.dp))
                    InversionPicker(
                        pinned = row.pinned,
                        position = row.currentCandidateIndex + 1,
                        total = row.candidateCount,
                        onPrev = onPrev,
                        onNext = onNext,
                        onAuto = onAuto
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayChordButton(onClick: () -> Unit) {
    Surface(shape = Pill, color = MaterialTheme.colorScheme.surface) {
        AppIconButton(onClick = onClick) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Play chord",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun InversionBadge(inv: Int) {
    Surface(
        shape = Pill,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            inversionBadge(inv),
            style = PianoFlowTheme.extendedTypography.labelCaps.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun CollapseToggle(collapsed: Boolean, onClick: () -> Unit) {
    AppIconButton(onClick = onClick) {
        Icon(
            imageVector = if (collapsed) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
            contentDescription = if (collapsed) "Expand keyboard" else "Collapse keyboard",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InversionPicker(
    pinned: Boolean,
    position: Int,
    total: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onAuto: () -> Unit,
) {
    // At accessibility font scales the label + ‹ › + Auto can't share one line without
    // truncating "Voicing X of Y" to "Voici", so the label moves to its own row above
    // the controls. At 1.0 the compact single-row layout is kept. Shared reflow predicate.
    val stacked = isExpandedFontScale()
    if (stacked) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VoicingLabel(pinned = pinned, position = position, total = total)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StepButton(
                    icon = Icons.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous voicing",
                    enabled = true,
                    onClick = onPrev
                )
                StepButton(
                    icon = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "Next voicing",
                    enabled = true,
                    onClick = onNext
                )
                Spacer(Modifier.weight(1f))
                AssistChip(
                    onClick = onAuto,
                    enabled = pinned,
                    label = {
                        Text(
                            if (pinned) "Reset" else "Auto",
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StepButton(
                icon = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "Previous voicing",
                enabled = true,
                onClick = onPrev
            )
            VoicingLabel(
                pinned = pinned,
                position = position,
                total = total,
                modifier = Modifier.weight(1f)
            )
            StepButton(
                icon = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Next voicing",
                enabled = true,
                onClick = onNext
            )
            AssistChip(
                onClick = onAuto,
                enabled = pinned,
                label = {
                    Text(
                        if (pinned) "Reset" else "Auto",
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            )
        }
    }
}

@Composable
private fun VoicingLabel(
    pinned: Boolean,
    position: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        // Voicing index in mono (musical data) + a plain-language "of N".
        Text(
            buildAnnotatedString {
                append("Voicing ")
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                    )
                ) { append("$position") }
                append(" of $total")
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            if (pinned) "Pinned by you" else "Auto-picked",
            style = MaterialTheme.typography.labelSmall,
            color = if (pinned) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
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
    AppIconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

/**
 * Friendly card title, e.g. "C Major" / "A Minor". The root note is musical data
 * (IBM Plex Mono); the narrative quality word inherits the surrounding UI font.
 */
@Composable
private fun chordDisplayName(chord: Chord) = buildAnnotatedString {
    withStyle(
        SpanStyle(fontFamily = PianoFlowTheme.extendedTypography.musicData.fontFamily)
    ) {
        append(Pitch.NAMES[chord.rootPc])
    }
    append(" ")
    append(qualityDisplayName(chord.quality))
}

private fun qualityDisplayName(q: Quality): String = when (q) {
    Quality.MAJ -> "Major"
    Quality.MIN -> "Minor"
    Quality.DIM -> "Diminished"
    Quality.AUG -> "Augmented"
    Quality.SUS2 -> "Sus2"
    Quality.SUS4 -> "Sus4"
    Quality.DOM7 -> "Dom 7th"
    Quality.MAJ7 -> "Major 7th"
    Quality.MIN7 -> "Minor 7th"
    Quality.DIM7 -> "Dim 7th"
    Quality.M7B5 -> "Half-Dim"
    Quality.MAJ6 -> "Major 6th"
    Quality.MIN6 -> "Minor 6th"
}

private fun inversionBadge(inv: Int): String = when (inv) {
    0 -> "Root Pos"
    1 -> "1st Inv"
    2 -> "2nd Inv"
    3 -> "3rd Inv"
    else -> "Inv $inv"
}
