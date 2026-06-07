package com.linh.pianoflow.songs

private val BASE = mapOf('C' to 0, 'D' to 2, 'E' to 4, 'F' to 5, 'G' to 7, 'A' to 9, 'B' to 11)

private val ALIAS = mapOf(
    "" to Quality.MAJ, "maj" to Quality.MAJ, "M" to Quality.MAJ, "major" to Quality.MAJ,
    "m" to Quality.MIN, "min" to Quality.MIN, "-" to Quality.MIN, "minor" to Quality.MIN,
    "dim" to Quality.DIM, "°" to Quality.DIM, "o" to Quality.DIM,
    "aug" to Quality.AUG, "+" to Quality.AUG,
    "sus" to Quality.SUS4, "sus4" to Quality.SUS4, "sus2" to Quality.SUS2,
    "7" to Quality.DOM7, "dom7" to Quality.DOM7,
    "maj7" to Quality.MAJ7, "M7" to Quality.MAJ7, "Δ" to Quality.MAJ7, "Δ7" to Quality.MAJ7,
    "m7" to Quality.MIN7, "min7" to Quality.MIN7, "-7" to Quality.MIN7,
    "dim7" to Quality.DIM7, "°7" to Quality.DIM7, "o7" to Quality.DIM7,
    "m7b5" to Quality.M7B5, "ø" to Quality.M7B5, "ø7" to Quality.M7B5, "halfdim" to Quality.M7B5,
    "6" to Quality.MAJ6, "maj6" to Quality.MAJ6, "m6" to Quality.MIN6, "min6" to Quality.MIN6,
)

private val ROOT_REGEX = Regex("^([A-Ga-g])([#b]?)(.*)$")

fun parseChord(tok: String): Chord? {
    val m = ROOT_REGEX.find(tok.trim()) ?: return null
    val (letter, acc, suffix) = m.destructured
    var pc = BASE[letter.uppercase()[0]] ?: return null
    if (acc == "#") pc = (pc + 1) % 12 else if (acc == "b") pc = (pc + 11) % 12
    val q = ALIAS[suffix] ?: return null
    return Chord(pc, q)
}

fun parseProgression(s: String): List<Pair<String, Chord?>> =
    s.split(Regex("[|,\\s]+")).filter { it.isNotBlank() }.map { it to parseChord(it) }
