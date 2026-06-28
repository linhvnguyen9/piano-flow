package com.linh.pianoflow.feature.sightreading.impl.domain

import com.linh.pianoflow.core.model.Pitch
import kotlin.math.max
import kotlin.math.roundToInt

/** One answered note: its name, time-to-find (seconds), and whether it was missed first. */
data class NoteRecord(val name: String, val seconds: Double, val missed: Boolean)

/** A "slowest to find" row in the session summary (bar width is [barFraction] of the max). */
data class SlowNote(val name: String, val seconds: Double, val barFraction: Float)

/** The end-of-session report shown on the Summary screen. */
data class SightReadingSummary(
    val medianSeconds: Double,
    val accuracyPercent: Int,
    val notesDone: Int,
    val slowest: List<SlowNote>,
)

/** White (natural) MIDI notes in [lo]..[hi] inclusive — the pool the drill picks from. */
fun whiteNotesIn(lo: Int, hi: Int): List<Int> = (lo..hi).filter { Pitch.isWhite(it) }

/**
 * Computes the session [SightReadingSummary] from the per-note [records]. Mirrors the
 * design engine's `finish()`:
 *  - **median** of every answer time,
 *  - **accuracy** = share of notes found correctly on the first tap,
 *  - **slowest** = up to four note names, ranked by average time (then miss count),
 *    each with a bar fraction relative to the slowest.
 */
fun summarize(records: List<NoteRecord>): SightReadingSummary {
    val done = records.size
    if (done == 0) return SightReadingSummary(0.0, 0, 0, emptyList())

    val times = records.map { it.seconds }.sorted()
    val median = if (times.size % 2 == 1) {
        times[times.size / 2]
    } else {
        (times[times.size / 2 - 1] + times[times.size / 2]) / 2.0
    }

    val correctFirst = records.count { !it.missed }
    val accuracy = ((correctFirst.toDouble() / done) * 100).roundToInt()

    val aggregated = records.groupBy { it.name }
        .map { (name, recs) ->
            Triple(name, recs.map { it.seconds }.average(), recs.count { it.missed })
        }
        .sortedWith(
            compareByDescending<Triple<String, Double, Int>> { it.second }.thenByDescending { it.third },
        )
    val top = aggregated.take(4)
    val maxAvg = max(top.maxOfOrNull { it.second } ?: 0.001, 0.001)
    val slowest = top.map { (name, avg, _) -> SlowNote(name, avg, (avg / maxAvg).toFloat()) }

    return SightReadingSummary(median, accuracy, done, slowest)
}
