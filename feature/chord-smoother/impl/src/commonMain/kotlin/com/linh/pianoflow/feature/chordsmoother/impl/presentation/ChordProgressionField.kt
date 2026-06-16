package com.linh.pianoflow.feature.chordsmoother.impl.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.linh.pianoflow.core.designsystem.theme.Pill
import com.linh.pianoflow.feature.chordsmoother.impl.presentation.consumeTokens
import com.linh.pianoflow.feature.chordsmoother.impl.domain.normalizeChordToken

/**
 * Tokenized chord field. Committed chords are chips; the caret sits after the last
 * chip so typing is inline. The trailing + opens the picker. Backspace on an empty
 * caret removes the last chip (best-effort — soft keyboards may not deliver it).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChordProgressionField(
    tokens: List<String>,
    editingText: String,
    examples: List<String>,
    onTokensChange: (List<String>) -> Unit,
    onEditingTextChange: (String) -> Unit,
    onChipTap: (index: Int) -> Unit,
    onOpenPicker: () -> Unit,
    onPickExample: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val isEmpty = tokens.isEmpty() && editingText.isEmpty()
    Column(modifier.fillMaxWidth()) {
        Text(
            "Chord progression",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        // Tonal layer instead of a hard stroke: filled surface, 16dp radius, soft shadow.
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("chordFieldContainer")
                .clickable(interactionSource = interactionSource, indication = null) {
                    focusRequester.requestFocus()
                    keyboard?.show()
                },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlowRow(
                    modifier = Modifier.weight(1f).padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    tokens.forEachIndexed { i, token ->
                        ChordChip(token) { onChipTap(i) }
                    }
                    BasicTextField(
                        value = editingText,
                        onValueChange = { raw ->
                            val (completed, pending) = consumeTokens(raw)
                            if (completed.isNotEmpty()) {
                                onTokensChange(tokens + completed.map(::normalizeChordToken))
                            }
                            onEditingTextChange(pending)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            autoCorrectEnabled = false,
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .testTag("chordFieldInput")
                            .focusRequester(focusRequester)
                            .widthIn(min = 96.dp)
                            .padding(vertical = 8.dp)
                            .onPreviewKeyEvent { e ->
                                if (e.type == KeyEventType.KeyDown &&
                                    e.key == Key.Backspace &&
                                    editingText.isEmpty() &&
                                    tokens.isNotEmpty()
                                ) {
                                    onTokensChange(tokens.dropLast(1))
                                    true
                                } else {
                                    false
                                }
                            },
                    )
                }
                AddChordButton(onClick = onOpenPicker)
                Spacer(Modifier.width(8.dp))
            }
        }
        if (examples.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text(
                if (isEmpty) "Try an example" else "Or start from an example",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                examples.forEachIndexed { i, ex ->
                    AssistChip(
                        onClick = { onPickExample(ex) },
                        label = { Text(exampleLabel(i, ex)) },
                        shape = Pill,
                    )
                }
            }
        }
    }
}

/** Friendly, genre-flavored names for the canned example progressions. */
private fun exampleLabel(index: Int, raw: String): String = when (index) {
    0 -> "Pop"
    1 -> "Ballad"
    2 -> "Jazz"
    else -> raw
}

@Composable
private fun AddChordButton(onClick: () -> Unit) {
    Surface(
        shape = Pill,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp),
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "Add chord",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun ChordChip(label: String, onClick: () -> Unit) {
    Surface(
        shape = Pill,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
