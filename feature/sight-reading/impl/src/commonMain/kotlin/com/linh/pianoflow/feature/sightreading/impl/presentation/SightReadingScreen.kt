package com.linh.pianoflow.feature.sightreading.impl.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linh.pianoflow.core.designsystem.AppIconButton
import com.linh.pianoflow.core.designsystem.MusicStaff
import com.linh.pianoflow.core.designsystem.PianoKeyboard
import com.linh.pianoflow.core.designsystem.theme.Pill
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme
import com.linh.pianoflow.feature.sightreading.impl.domain.SlowNote
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun SightReadingScreen(
    modifier: Modifier = Modifier,
    viewModel: SightReadingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SightReadingScreenContent(
        state = state,
        modifier = modifier,
        onStartSession = viewModel::startSession,
        onKeyTap = viewModel::handleTap,
        onOpenSettings = viewModel::openSettings,
        onCloseSettings = viewModel::closeSettings,
        onToggleNoteNames = viewModel::setShowNoteNames,
    )
}

@Composable
fun SightReadingScreenContent(
    state: SightReadingUiState,
    modifier: Modifier = Modifier,
    onStartSession: () -> Unit,
    onKeyTap: (Int) -> Unit,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onToggleNoteNames: (Boolean) -> Unit,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxSize()) {
            when (state.screen) {
                SightReadingScreen.START -> StartScreen(state, onStartSession, onOpenSettings)
                SightReadingScreen.DRILL -> DrillScreen(state, onKeyTap, onOpenSettings)
                SightReadingScreen.SUMMARY -> SummaryScreen(state, onStartSession, onOpenSettings)
            }

            if (state.settingsOpen) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(onDismissRequest = onCloseSettings, sheetState = sheetState) {
                    SettingsSheetContent(state = state, onToggleNoteNames = onToggleNoteNames)
                }
            }
        }
    }
}

// ============================ START ============================

@Composable
private fun StartScreen(
    state: SightReadingUiState,
    onStartSession: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.End) {
            SettingsButton(onOpenSettings)
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MusicStaff(
                midi = state.heroNote,
                width = 200.dp,
                space = 22.dp,
                modifier = Modifier.alpha(0.9f),
            )
            Text(
                "Sight Reading",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                "A note appears — tap the matching key, fast.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            RangeChip(state.rangeLabel)
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp, top = 8.dp)) {
            PrimaryButton("Start session", onStartSession)
        }
    }
}

@Composable
private fun RangeChip(label: String) {
    Surface(shape = Pill, color = MaterialTheme.colorScheme.surfaceContainer) {
        Text(
            label,
            style = PianoFlowTheme.extendedTypography.musicData.copy(fontSize = 13.sp, letterSpacing = 0.04.em),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}

// ============================ DRILL ============================

@Composable
private fun DrillScreen(
    state: SightReadingUiState,
    onKeyTap: (Int) -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        // header: settings + progress count
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SettingsButton(onOpenSettings)
            Text(
                state.progressText,
                style = PianoFlowTheme.extendedTypography.musicData,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        // progress bar
        Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp)) {
            Box(
                Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Box(
                    Modifier.fillMaxWidth(state.progressFraction).fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.primary),
                )
            }
        }

        // staff hero
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (state.showNoteName) {
                Text(
                    state.noteName,
                    style = PianoFlowTheme.extendedTypography.musicData,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            BoxWithConstraints {
                val staffWidth = minOf(288.dp, maxWidth)
                MusicStaff(
                    midi = state.note,
                    state = state.staffState,
                    width = staffWidth,
                    space = staffWidth / 12,
                )
            }
        }

        // feedback zone (kept above the keyboard)
        Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 24.dp, vertical = 0.dp)
                .padding(bottom = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            FeedbackZone(state)
        }

        // docked keyboard in a warm tray
        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 16.dp),
        ) {
            PianoKeyboard(
                startMidi = state.keyboardStart,
                endMidi = state.keyboardEnd,
                highlighted = state.litKeys,
                onKeyTap = onKeyTap,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FeedbackZone(state: SightReadingUiState) {
    val feedback = state.feedback
    if (feedback != null) {
        val accent = if (feedback.correct) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (feedback.correct) "✓" else "✗",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                    color = accent,
                )
                if (feedback.correct && feedback.timeSeconds != null) {
                    Text(
                        formatSeconds(feedback.timeSeconds),
                        style = PianoFlowTheme.extendedTypography.musicData.copy(fontSize = 22.sp),
                        color = accent,
                    )
                }
            }
            if (feedback.noteHint != null) {
                Text(
                    feedback.noteHint,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (feedback.showStreak) {
                Text(
                    "${feedback.streak} IN A ROW",
                    style = PianoFlowTheme.extendedTypography.musicData.copy(fontSize = 12.sp, letterSpacing = 0.08.em, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    } else if (state.liveTimerSeconds != null) {
        Text(
            formatSeconds(state.liveTimerSeconds),
            style = PianoFlowTheme.extendedTypography.musicData.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
        )
    }
}

// ============================ SUMMARY ============================

@Composable
private fun SummaryScreen(
    state: SightReadingUiState,
    onNewSession: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.End) {
            SettingsButton(onOpenSettings)
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                "Session complete",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            StatRow(
                median = formatSeconds(state.summary.medianSeconds),
                accuracy = "${state.summary.accuracyPercent}%",
                notes = state.summary.notesDone.toString(),
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "SLOWEST TO FIND",
                    style = PianoFlowTheme.extendedTypography.labelCaps,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                state.summary.slowest.forEach { SlowNoteRow(it) }
            }

            PrimaryButton("New session", onNewSession)
        }
    }
}

@Composable
private fun StatRow(median: String, accuracy: String, notes: String) {
    // At accessibility font scales the three fixed columns can't hold the mono value
    // without clipping ("85%" → "85"); stack them full-width so each value shows in full
    // and the labels stay un-abbreviated. Mirrors the Songs InversionPicker reflow.
    val stacked = LocalDensity.current.fontScale >= 1.5f
    if (stacked) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            StatCard("Median", median, Modifier.fillMaxWidth())
            StatCard("Accuracy", accuracy, Modifier.fillMaxWidth())
            StatCard("Notes", notes, Modifier.fillMaxWidth())
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Median", median, Modifier.weight(1f))
            StatCard("Accuracy", accuracy, Modifier.weight(1f))
            StatCard("Notes", notes, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = modifier) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                label.uppercase(),
                style = PianoFlowTheme.extendedTypography.labelCaps,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Text(
                value,
                style = PianoFlowTheme.extendedTypography.musicData.copy(fontSize = 26.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SlowNoteRow(slow: SlowNote) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            slow.name,
            style = PianoFlowTheme.extendedTypography.musicData.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.widthIn(min = 36.dp),
        )
        Box(
            Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Box(
                Modifier.fillMaxWidth(slow.barFraction.coerceIn(0f, 1f)).fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp)).background(MaterialTheme.colorScheme.secondaryFixedDim),
            )
        }
        Text(
            formatSeconds(slow.seconds),
            style = PianoFlowTheme.extendedTypography.musicData.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

// ============================ SETTINGS SHEET ============================

@Composable
internal fun SettingsSheetContent(
    state: SightReadingUiState,
    onToggleNoteNames: (Boolean) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 28.dp)) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 18.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Show note names",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Letter name above the staff while you learn.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = state.showNoteName, onCheckedChange = onToggleNoteNames)
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 14.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        )

        // "Soon" — clef & range is not yet configurable.
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).alpha(0.5f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Clef & range",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "Treble + bass · C3–C5 · naturals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                "SOON",
                style = PianoFlowTheme.extendedTypography.labelCaps.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        ) {
            Text(
                state.touchHint,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            )
        }
    }
}

// ============================ shared bits ============================

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = Pill,
        // 56dp is the Serene Practice pill baseline, but a minimum only: at large font
        // scales the label wraps and the button grows taller rather than clipping.
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 56.dp),
    ) {
        Text(label, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SettingsButton(onClick: () -> Unit) {
    val tint = MaterialTheme.colorScheme.onSurfaceVariant
    val background = MaterialTheme.colorScheme.background
    AppIconButton(onClick = onClick) {
        Canvas(Modifier.size(22.dp)) {
            val u = size.minDimension / 24f
            val sw = 2f * u
            drawLine(tint, Offset(4 * u, 8 * u), Offset(20 * u, 8 * u), strokeWidth = sw, cap = StrokeCap.Round)
            drawLine(tint, Offset(4 * u, 16 * u), Offset(20 * u, 16 * u), strokeWidth = sw, cap = StrokeCap.Round)
            drawCircle(background, radius = 2.6f * u, center = Offset(15 * u, 8 * u))
            drawCircle(tint, radius = 2.6f * u, center = Offset(15 * u, 8 * u), style = Stroke(width = sw))
            drawCircle(background, radius = 2.6f * u, center = Offset(9 * u, 16 * u))
            drawCircle(tint, radius = 2.6f * u, center = Offset(9 * u, 16 * u), style = Stroke(width = sw))
        }
    }
}

private fun formatSeconds(seconds: Double): String {
    val tenths = (seconds * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}s"
}
