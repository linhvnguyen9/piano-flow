package com.linh.pianoflow.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linh.pianoflow.core.designsystem.theme.PianoBlackKey
import com.linh.pianoflow.core.designsystem.theme.PianoKeyLabel
import com.linh.pianoflow.core.designsystem.theme.PianoKeyOutline
import com.linh.pianoflow.core.designsystem.theme.PianoWhiteKey
import com.linh.pianoflow.core.model.Pitch

/**
 * Semantics tag on **interactive** piano keys. A multi-octave keyboard is intrinsically
 * narrower than 48 dp per key (e.g. ~22 dp for 15 keys on a 360 dp screen), so a Tier-1
 * touch-target check would flag every key. The mitigation is the full-height (120 dp) hit
 * area; the Sight Reading settings sheet surfaces the trade-off and offers a narrower
 * range. Tier-1 assertions skip nodes carrying this tag.
 */
const val PianoKeyTestTag = "pianoKey"

@Composable
fun PianoKeyboard(
    startMidi: Int,
    endMidi: Int,
    highlighted: Set<Int>,
    modifier: Modifier = Modifier,
    labels: Boolean = false,
    anchorMidi: Int? = null,
    onKeyTap: ((Int) -> Unit)? = null,
) {
    val whiteLit = MaterialTheme.colorScheme.primaryContainer
    val whiteBg = PianoWhiteKey
    val blackLit = MaterialTheme.colorScheme.primary
    val blackBg = PianoBlackKey
    val borderColor = PianoKeyOutline
    val whiteText = PianoKeyLabel

    BoxWithConstraints(modifier.height(120.dp)) {
        val whites = (startMidi..endMidi).filter { Pitch.isWhite(it) }
        if (whites.isEmpty()) return@BoxWithConstraints
        val keyW = maxWidth / whites.size
        val keyH = maxHeight
        val blackH = keyH * 0.62f
        val blackW = keyW * 0.6f

        Row(Modifier.fillMaxSize()) {
            whites.forEach { midi ->
                val lit = midi in highlighted
                val rowMod = Modifier
                    .width(keyW)
                    .fillMaxHeight()
                    .padding(end = 0.dp)
                    .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(if (lit) whiteLit else whiteBg)
                    .border(1.dp, borderColor, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                Box(
                    modifier = if (onKeyTap != null)
                        rowMod.testTag(PianoKeyTestTag).clickable { onKeyTap(midi) } else rowMod,
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // `labels` names every key; `anchorMidi` names just one — a subtle
                    // orientation landmark (e.g. middle C) so a learner has a key to count
                    // from without every answer being given away.
                    val keyLabel = when {
                        labels -> Pitch.name(midi)
                        midi == anchorMidi -> Pitch.NAMES[Pitch.pc(midi)]
                        else -> null
                    }
                    if (keyLabel != null) {
                        Text(
                            keyLabel,
                            color = whiteText,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }

        whites.forEachIndexed { i, midi ->
            if (i == whites.lastIndex) return@forEachIndexed
            val pc = Pitch.pc(midi)
            if (pc !in setOf(0, 2, 5, 7, 9)) return@forEachIndexed
            val blackMidi = midi + 1
            val lit = blackMidi in highlighted
            val xOffset = keyW * (i + 1) - blackW / 2
            val mod = Modifier
                .offset(x = xOffset)
                .width(blackW)
                .height(blackH)
                .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                .background(if (lit) blackLit else blackBg)
            Box(
                modifier = if (onKeyTap != null) mod.testTag(PianoKeyTestTag).clickable { onKeyTap(blackMidi) } else mod
            )
        }

        Spacer(Modifier)
    }
}
