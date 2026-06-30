package com.linh.pianoflow.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.linh.pianoflow.core.model.Pitch
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

/** Which clef a [MusicStaff] draws. */
enum class Clef { Treble, Bass }

/** Tints only the notehead — the clef + lines stay neutral ink in every state. */
enum class StaffNoteState { Default, Correct, Incorrect }

/** Treble for middle C (60) and above, bass below — matching the design helper. */
fun clefForMidi(midi: Int): Clef = if (midi >= 60) Clef.Treble else Clef.Bass

/**
 * Draws a single note on an engraved five-line staff — the focal "what note is
 * this?" view for sight-reading. Pair it with [PianoKeyboard] (the answer surface).
 *
 * This is a 1:1 Compose port of the design system's `MusicStaff` (the geometry in
 * `templates/sight-reading/musicstaff.js`): same clef glyph paths, staff-space
 * layout, automatic ledger lines, and MIDI→notehead placement. Geometry is driven by
 * [space] (height of one staff space) and [width] in **dp**, so it scales with the
 * screen — **not** the OS font scale (keep the note-name label outside, as text, so
 * that label does honor font scale).
 *
 * The clef is chosen automatically from [midi] ([clefForMidi]); force one with [clef].
 * [state] tints the notehead with a 200ms fade: `Default` onSurface, `Correct`
 * primary (teal), `Incorrect` error.
 */
@Composable
fun MusicStaff(
    midi: Int,
    modifier: Modifier = Modifier,
    state: StaffNoteState = StaffNoteState.Default,
    clef: Clef? = null,
    space: Dp = 18.dp,
    width: Dp = 264.dp,
) {
    val ink = MaterialTheme.colorScheme.onSurface
    val targetNoteColor = when (state) {
        StaffNoteState.Correct -> MaterialTheme.colorScheme.primary
        StaffNoteState.Incorrect -> MaterialTheme.colorScheme.error
        StaffNoteState.Default -> ink
    }
    val noteColor by animateColorAsState(targetNoteColor, animationSpec = tween(200), label = "notehead")

    val which = clef ?: clefForMidi(midi)
    val glyph = if (which == Clef.Treble) TrebleGlyph else BassGlyph
    val clefPath = remember(glyph) { PathParser().parsePathString(glyph.path).toPath() }

    Canvas(modifier.size(width = width, height = space * STAFF_HEIGHT_SPACES)) {
        val s = size.height / STAFF_HEIGHT_SPACES        // px per staff space
        val w = size.width
        val staffTopY = 3f * s                            // 3 spaces of head-room
        val lineWidth = max(1f, s * 0.07f)
        val lineX0 = s * 0.6f
        val lineX1 = w - s * 0.6f

        for (i in 0..4) {
            val y = staffTopY + i * s
            drawLine(
                color = ink.copy(alpha = 0.32f),
                start = Offset(lineX0, y),
                end = Offset(lineX1, y),
                strokeWidth = lineWidth,
                cap = StrokeCap.Round,
            )
        }

        val refLineY = staffTopY + (if (which == Clef.Treble) 3 else 1) * s
        val k = (glyph.targetSpaces * s) / (glyph.maxY - glyph.minY)
        val tx = glyph.padX - k * glyph.minX
        val ty = refLineY - k * glyph.refY
        withTransform({
            translate(tx, ty)
            scale(k, k, pivot = Offset.Zero)
        }) {
            drawPath(clefPath, color = ink, alpha = 0.9f)
            glyph.dots.forEach { dot -> drawCircle(ink, radius = 22f, center = dot, alpha = 0.9f) }
        }

        val topLineDiatonic = if (which == Clef.Treble) 38 else 26
        val halfSpaces = topLineDiatonic - diatonic(midi)
        val noteY = staffTopY + halfSpaces * (s / 2f)
        val noteX = w * 0.6f

        val spacesFromTop = halfSpaces / 2f
        val ledgerHalf = s * 1.2f
        if (spacesFromTop > 4f) {
            for (i in 5..floor(spacesFromTop + 1e-3f).toInt()) {
                drawLedger(noteX, staffTopY + i * s, ledgerHalf, ink, lineWidth)
            }
        }
        if (spacesFromTop < 0f) {
            var i = -1
            val end = ceil(spacesFromTop - 1e-3f).toInt()
            while (i >= end) {
                drawLedger(noteX, staffTopY + i * s, ledgerHalf, ink, lineWidth)
                i--
            }
        }

        val rx = s * 0.74f
        val ry = s * 0.56f
        withTransform({ rotate(-22f, pivot = Offset(noteX, noteY)) }) {
            drawOval(
                color = noteColor,
                topLeft = Offset(noteX - rx, noteY - ry),
                size = Size(rx * 2f, ry * 2f),
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLedger(
    cx: Float,
    y: Float,
    half: Float,
    color: Color,
    strokeWidth: Float,
) = drawLine(
    color = color.copy(alpha = 0.5f),
    start = Offset(cx - half, y),
    end = Offset(cx + half, y),
    strokeWidth = strokeWidth,
    cap = StrokeCap.Round,
)

// Total canvas height in staff-spaces: 3 head-room + 4 staff + 3 foot-room.
private const val STAFF_HEIGHT_SPACES = 10f

/** Diatonic step number (C0 = 0, D0 = 1, …) for ledger/notehead placement. */
private fun diatonic(midi: Int): Int = (midi / 12 - 1) * 7 + letterOfPc(Pitch.pc(midi))

private fun letterOfPc(pc: Int): Int = when (pc) {
    0 -> 0; 2 -> 1; 4 -> 2; 5 -> 3; 7 -> 4; 9 -> 5; 11 -> 6
    else -> 0 // accidentals are never drilled; fall back to the natural below.
}

private val TrebleGlyph = ClefGlyph(
    path = "M196 12 c-34 36 -52 78 -52 122 c0 30 10 58 28 86 c-58 40 -96 96 -96 162 " +
        "c0 78 60 138 138 138 c14 0 27 -2 40 -6 l16 96 c4 26 -14 48 -40 48 c-20 0 -36 -12 -42 -30 " +
        "c18 -2 32 -18 32 -38 c0 -22 -18 -40 -40 -40 c-24 0 -42 20 -42 46 c0 40 36 72 90 72 " +
        "c50 0 86 -34 78 -86 l-18 -110 c44 -18 74 -60 74 -110 c0 -56 -44 -100 -100 -100 " +
        "c-8 0 -16 1 -24 3 l-12 -74 c44 -44 72 -92 72 -142 c0 -54 -28 -90 -60 -90 c-8 0 -14 2 -18 6 z " +
        "M214 86 c16 0 28 22 28 56 c0 38 -20 74 -52 104 l-16 -96 c8 -42 24 -64 40 -64 z " +
        "M188 386 l18 110 c-40 -2 -72 -36 -72 -78 c0 -36 22 -68 56 -86 l8 50 c-18 8 -30 26 -30 46 " +
        "c0 18 10 34 26 42 z " +
        "M236 408 c34 2 60 30 60 66 c0 30 -18 56 -44 68 l-20 -122 c1 0 3 -1 4 -12z",
    minX = 30f, minY = 12f, maxY = 640f, refY = 466f, targetSpaces = 6.75f, padX = 24f,
    dots = emptyList(),
)

private val BassGlyph = ClefGlyph(
    path = "M70 64 c70 -44 196 -34 232 60 c30 76 -8 168 -86 226 c-54 40 -120 64 -188 78 " +
        "c-6 1 -10 -6 -5 -10 c70 -40 132 -86 168 -150 c28 -50 32 -110 6 -150 c-22 -34 -64 -44 -98 -28 " +
        "c20 6 34 24 34 46 c0 28 -22 50 -50 50 c-30 0 -52 -24 -52 -56 c0 -28 14 -50 36 -66 z",
    minX = 32f, minY = 36f, maxY = 250f, refY = 86f, targetSpaces = 3.25f, padX = 18f,
    dots = listOf(Offset(338f, 64f), Offset(338f, 120f)),
)

private class ClefGlyph(
    val path: String,
    val minX: Float,
    val minY: Float,
    val maxY: Float,
    val refY: Float,
    val targetSpaces: Float,
    val padX: Float,
    val dots: List<Offset>,
)
