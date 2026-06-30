package com.linh.pianoflow.feature.sightreading.impl.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.linh.pianoflow.core.designsystem.AdaptiveRow
import com.linh.pianoflow.core.designsystem.AppIconButton
import com.linh.pianoflow.core.designsystem.MusicStaff
import com.linh.pianoflow.core.designsystem.PianoKeyboard
import com.linh.pianoflow.core.designsystem.theme.Pill
import com.linh.pianoflow.core.designsystem.theme.PianoFlowTheme
import com.linh.pianoflow.feature.sightreading.api.SightReadingKey
import com.linh.pianoflow.feature.sightreading.impl.domain.SlowNote
import dev.enro.annotations.NavigationDestination
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
@NavigationDestination(SightReadingKey::class)
fun SightReadingDestination() {
    SightReadingScreen()
}

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
        onToggleMiddleC = viewModel::setShowMiddleC,
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
    onToggleMiddleC: (Boolean) -> Unit,
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
                    SettingsSheetContent(
                        state = state,
                        onToggleNoteNames = onToggleNoteNames,
                        onToggleMiddleC = onToggleMiddleC,
                    )
                }
            }
        }
    }
}

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

@Composable
private fun DrillScreen(
    state: SightReadingUiState,
    onKeyTap: (Int) -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
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

        Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 24.dp, vertical = 0.dp)
                .padding(bottom = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            FeedbackZone(state)
        }

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
                // Middle-C landmark — its own learner toggle (independent of the staff note
                // name); orients without revealing the target note. 60 = middle C, always
                // within the C3–C5 range.
                anchorMidi = if (state.showMiddleC) 60 else null,
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
    // Three equal cards whose mono values clip in fixed columns at large font; the shared
    // AdaptiveRow stacks them full-width at >= 1.5x so each value shows in full. The
    // load-bearing values are tagged noTruncate (see StatCard) so Tier-1 catches any
    // regression deterministically, without waiting on the vision evaluator.
    AdaptiveRow(
        modifier = Modifier.fillMaxWidth(),
        stackAtFontScale = 1.5f,
        items = listOf(
            { m -> StatCard("Median", median, m) },
            { m -> StatCard("Accuracy", accuracy, m) },
            { m -> StatCard("Notes", notes, m) },
        ),
    )
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
                // Load-bearing data: Tier-1 fails the build if this ever truly clips
                // (the check counts dropped glyphs, not the noisy hasVisualOverflow flag).
                modifier = Modifier.testTag("noTruncate"),
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
    AppIconButton(onClick = onClick) {
        Icon(
            Icons.Filled.Tune,
            contentDescription = "Settings",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatSeconds(seconds: Double): String {
    val tenths = (seconds * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}s"
}
