# Chord Smoother — "Songs" Module

## Implementation Spec (Kotlin Multiplatform — module of the Sight-Reading app)

**Audience:** Claude Code, implementing inside the existing KMP app described in `sight-reading-trainer-SPEC.md`.
**What this is:** a second mode, "Songs," that takes a chord progression (triads through 7ths) and renders the **inversions that minimise hand movement** (voice leading), drawn on the shared keyboard and playable.
**Reference prototype:** `chord-smoother.html` — verified, source of truth for all behaviour. Every constant, recipe, and algorithm below is copied from it and checked numerically (DP output matches brute-force optimum; see §8).

This module is **pure-domain-heavy and UI-light**. The hard part (the solver) is small, deterministic, and fully unit-testable — do that first, against §8.

---

## 1. Why a module, not a new app

It reuses three things the sight-reading app already has. **Generalise these shared pieces; don't fork them.**

| Shared piece | Current use (sight-reading) | Change needed for Songs |
|---|---|---|
| **Pitch / MIDI model** | note tokens like `"C4"` + a `FREQ` map | add a canonical MIDI representation (§2.1) |
| **`PianoKeyboard` composable** | one-range keyboard, highlights the single correct key | generalise to *(startMidi, endMidi, highlighted: Set<Int>)* (§2.2) |
| **`TonePlayer`** (expect/actual) | plays one sine tone | add "play N notes at once" + "play a scheduled sequence" (§2.3) |
| Design tokens | — | reuse as-is |

Navigation: add a top-level mode switch (e.g. bottom nav or a menu) between **Drill** (existing) and **Songs** (new). No shared mutable state between them.

---

## 2. Shared-layer contract (generalise these)

### 2.1 Pitch / MIDI
Introduce one canonical pitch type in the shared `domain/music` package and express both modes through it.
```kotlin
// MIDI integer, 60 = C4 = middle C (matches the app's existing C4 = middle C convention)
object Pitch {
    val NAMES = listOf("C","C#","D","D#","E","F","F#","G","G#","A","A#","B")
    fun name(midi: Int): String = NAMES[((midi % 12) + 12) % 12] + (midi / 12 - 1)
    fun freq(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)
    fun isWhite(midi: Int): Boolean = ((midi % 12) + 12) % 12 in setOf(0,2,4,5,7,9,11)
}
```
The sight-reading note token `"C4"` corresponds to MIDI 60; reuse this for that mode's `FREQ`/audio rather than the separate table if convenient. (Not required for parity — just don't introduce a *second* conflicting pitch convention.)

### 2.2 `PianoKeyboard` composable
Generalise the existing keyboard to this signature:
```kotlin
@Composable
fun PianoKeyboard(
    startMidi: Int,            // inclusive; should be a C at/below the lowest shown note
    endMidi: Int,              // inclusive; should be a B at/above the highest shown note
    highlighted: Set<Int>,     // MIDI notes to light up
    labels: Boolean = false,
    onKeyTap: ((Int) -> Unit)? = null,
)
```
- White keys: every natural MIDI in `[startMidi, endMidi]`, equal width.
- Black key between white *i* and *i+1* iff `pc(white[i]) in {0,2,5,7,9}` (C,D,F,G,A) and *i* < last.
- A key renders "lit" when its MIDI ∈ `highlighted` (white by its own MIDI; black by `whiteMidi+1`).
- The sight-reading mode's current keyboard is the special case "narrow range, one highlighted key" — refactor it to call this.

### 2.3 `TonePlayer`
Extend the expect interface:
```kotlin
expect class TonePlayer {
    fun playChord(freqsHz: List<Double>, atSecondsFromNow: Double = 0.0) // simultaneous notes
    // existing: fun play(freqHz: Double, correct: Boolean)
}
```
- A chord = sum of short sine/triangle voices, ~0.9 s, quick attack + exponential decay (prototype uses `triangle`, gain ramp to ~0.18 in 20 ms, decay to silence by ~0.9 s).
- Progression playback schedules `playChord` calls at `i * dt` (prototype `dt = 0.75 s`) and pulses the active row. Implement scheduling with coroutines/`delay` in the ViewModel, or pass absolute start offsets to actuals that support timed scheduling (Android `AudioTrack`/`SoundPool`, iOS `AVAudioEngine`).

---

## 3. Domain — chords

### 3.1 Model
```kotlin
enum class Quality(val intervals: List<Int>) {   // semitones from root, ascending
    MAJ(listOf(0,4,7)), MIN(listOf(0,3,7)), DIM(listOf(0,3,6)), AUG(listOf(0,4,8)),
    SUS2(listOf(0,2,7)), SUS4(listOf(0,5,7)),
    DOM7(listOf(0,4,7,10)), MAJ7(listOf(0,4,7,11)), MIN7(listOf(0,3,7,10)),
    DIM7(listOf(0,3,6,9)), M7B5(listOf(0,3,6,10)),
    MAJ6(listOf(0,4,7,9)), MIN6(listOf(0,3,7,9));
}
data class Chord(val rootPc: Int, val quality: Quality)        // rootPc in 0..11
```

### 3.2 Parser (verified — case-sensitive: `M7` ≠ `m7`)
```kotlin
private val BASE = mapOf('C' to 0,'D' to 2,'E' to 4,'F' to 5,'G' to 7,'A' to 9,'B' to 11)
private val ALIAS = mapOf(
  "" to MAJ, "maj" to MAJ, "M" to MAJ, "major" to MAJ,
  "m" to MIN, "min" to MIN, "-" to MIN, "minor" to MIN,
  "dim" to DIM, "°" to DIM, "o" to DIM,
  "aug" to AUG, "+" to AUG,
  "sus" to SUS4, "sus4" to SUS4, "sus2" to SUS2,
  "7" to DOM7, "dom7" to DOM7,
  "maj7" to MAJ7, "M7" to MAJ7, "Δ" to MAJ7, "Δ7" to MAJ7,
  "m7" to MIN7, "min7" to MIN7, "-7" to MIN7,
  "dim7" to DIM7, "°7" to DIM7, "o7" to DIM7,
  "m7b5" to M7B5, "ø" to M7B5, "ø7" to M7B5, "halfdim" to M7B5,
  "6" to MAJ6, "maj6" to MAJ6, "m6" to MIN6, "min6" to MIN6,
)
// token -> Chord?  (null = unparseable, surface as an error chip)
fun parseChord(tok: String): Chord? {
    val m = Regex("^([A-Ga-g])([#b]?)(.*)$").find(tok.trim()) ?: return null
    val (letter, acc, suffix) = m.destructured
    var pc = BASE[letter.uppercase()[0]]!!
    if (acc == "#") pc = (pc + 1) % 12 else if (acc == "b") pc = (pc + 11) % 12
    val q = ALIAS[suffix] ?: return null
    return Chord(pc, q)
}
fun parseProgression(s: String): List<Pair<String, Chord?>> =
    s.split(Regex("[|,\\s]+")).filter { it.isNotBlank() }.map { it to parseChord(it) }
```
Display label suffix: `MAJ→"" MIN→"m" DIM→"dim" AUG→"aug" SUS2→"sus2" SUS4→"sus4" DOM7→"7" MAJ7→"maj7" MIN7→"m7" DIM7→"dim7" M7B5→"m7♭5" MAJ6→"6" MIN6→"m6"`.
Input fields must use `autocapitalize=off` equivalent so suffixes stay lowercase.

---

## 4. Domain — voicings & the solver

A **voicing** is an ascending list of MIDI notes plus its inversion index.
```kotlin
data class Voicing(val notes: List<Int>, val inv: Int)   // notes sorted ascending
```

### 4.1 Candidate generation (verified)
```kotlin
fun rootPosition(chord: Chord, octave: Int): List<Int> {
    val root = chord.rootPc + 12 * (octave + 1)     // octave 4 -> C4 = 60
    return chord.quality.intervals.map { root + it } // ascending (intervals ascending)
}
fun invert(v: List<Int>, k: Int): List<Int> {
    val out = v.toMutableList()
    repeat(k) { out.add(out.removeAt(0) + 12) }
    return out.sorted()
}
fun candidates(chord: Chord): List<Voicing> {
    val size = chord.quality.intervals.size            // 3 or 4
    val seen = HashSet<String>(); val out = ArrayList<Voicing>()
    for (oct in 3..5) for (k in 0 until size) {
        val v = invert(rootPosition(chord, oct), k)
        if (v.first() < 48 || v.last() > 86) continue   // playable window C3..~D6
        if (seen.add(v.joinToString(","))) out += Voicing(v, k)
    }
    return out
}
```

### 4.2 Movement cost (verified)
Equal-size chords → exact 1:1 finger movement (sorted pairwise). Different sizes → symmetric nearest-note (you're adding/removing a finger; no clean pairing).
```kotlin
private fun nearestSum(from: List<Int>, to: List<Int>): Int =
    from.sumOf { x -> to.minOf { y -> abs(x - y) } }
fun moveCost(a: List<Int>, b: List<Int>): Double =
    if (a.size == b.size) a.indices.sumOf { abs(a[it] - b[it]).toDouble() }
    else (nearestSum(a, b) + nearestSum(b, a)) / 2.0
```

### 4.3 Solver — shortest path (DP / Viterbi, verified optimal)
```kotlin
// anchor=true keeps voicings near the center register (prevents slow drift over long songs)
fun solve(chords: List<Chord>, anchor: Boolean): List<Voicing> {
    val lambda = if (anchor) 0.35 else 0.0
    val center = 60.0
    val cands = chords.map { candidates(it) }
    val n = cands.size
    val dp = cands.map { DoubleArray(it.size) { Double.POSITIVE_INFINITY } }
    val bp = cands.map { IntArray(it.size) { -1 } }
    fun anc(v: Voicing) = lambda * abs(v.notes.average() - center)
    for (j in cands[0].indices) dp[0][j] = anc(cands[0][j])
    for (i in 1 until n) for (j in cands[i].indices) {
        val a = anc(cands[i][j])
        for (p in cands[i-1].indices) {
            val c = dp[i-1][p] + moveCost(cands[i-1][p].notes, cands[i][j].notes) + a
            if (c < dp[i][j]) { dp[i][j] = c; bp[i][j] = p }
        }
    }
    var last = dp[n-1].indices.minByOrNull { dp[n-1][it] }!!
    val path = arrayOfNulls<Voicing>(n)
    var j = last
    for (i in n-1 downTo 0) { path[i] = cands[i][j]; j = bp[i][j] }
    return path.filterNotNull()
}
fun totalMovement(path: List<Voicing>): Double =
    (1 until path.size).sumOf { moveCost(path[it-1].notes, path[it].notes) }
fun rootBaseline(chords: List<Chord>): List<Voicing> =        // naive comparison
    chords.map { Voicing(rootPosition(it, 4), 0) }
```
Complexity `O(n · k²)` with `k ≤ ~9`. Trivial. The result is **provably the minimum**; it is not a heuristic.

---

## 5. State

```kotlin
data class ChordResult(val label: String, val voicing: Voicing, val moveFromPrev: Double?)
data class SongsUiState(
    val input: String,
    val anchor: Boolean = true,          // default ON
    val rows: List<ChordResult> = emptyList(),
    val keyboardStart: Int = 60,         // C at/below lowest voiced note
    val keyboardEnd: Int = 72,           // B at/above highest
    val totalMovement: Double = 0.0,
    val baselineMovement: Double = 0.0,  // root-position-only, for the "vs" stat
    val errors: List<String> = emptyList(),
    val activeRow: Int? = null,          // pulsing during playback
)
```
`SongsViewModel` recomputes on every input change or anchor toggle: parse → split good/bad → `solve` → build rows (label + voicing + per-chord movement from previous) → derive keyboard range from the chosen voicings (`start = lowest − lowest%12`, `end = highest + (11 − highest%12)`).

---

## 6. UI (`SongsScreen`)

```
┌─────────────────────────────────────────┐
│  [ C|G|Am|F ] [ F|G|Em|Am ] … example chips
│  ┌─────────────────────────────────────┐ │
│  │ chord input (monospace-ish)         │ │
│  └─────────────────────────────────────┘ │
│  ◐ Anchor register      [ ▶ Play ]        │
│  "Couldn't read: Xyz" (errors, if any)    │
│  Hand movement: 11 semitones — root-only  │
│                 would be 31.              │
│  ┌── C   root   C4 E4 G4 ───────────────┐ │
│  │  [ keyboard, C/E/G lit ]             │ │
│  ├── ↓ 4 semitones ────────────────────┤ │
│  │  G   1st inv   B3 D4 G4              │ │
│  │  [ keyboard, lit ]                   │ │
│  └──────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```
- All chord rows share the **same** `PianoKeyboard` range so the lit cluster visibly barely moves between chords — that's the product's "aha." Keep it.
- Each row: chord label, inversion name (`root / 1st inv / 2nd inv / 3rd inv`), note names, keyboard with that voicing highlighted, and a "↓ N semitones" connector to the previous row. Tap a row → `TonePlayer.playChord`.
- Summary line shows optimized vs root-only movement. With one chord, show "nothing to smooth yet."
- Example chips, anchor toggle, Play button (sequential playback with active-row pulse).

---

## 7. Design notes (carry these into the build)

- **Default anchor ON.** Pure local smoothness can drift the hand up/down over a long song; the anchor term holds it near middle C.
- **Single-hand assumption.** Every voicing is one right-hand grab. Triads always fit; 7ths can span a major 7th (~stretchy) but stay under an octave in these inversions. Two-hand splitting is **out of scope** (§9.1).
- **"Smoothest" ≠ "best-sounding."** This is the known open question. The mechanical optimum can bury a chord's character (e.g. roaming bass, no clear root). The backlog items §9.4/§9.5 exist to add a *musical* objective on top; v1 ships the mechanical one and exposes the anchor toggle.

---

## 8. Acceptance criteria / tests (port to `commonTest`)

Run against the pure domain layer; no UI.

**Parser**
- `Asus4`→(A,SUS4), `Csus2`→(C,SUS2), `G7`→(G,DOM7), `Cmaj7`/`CM7`→(C,MAJ7), `Cm7`→(C,MIN7), `Bbmaj7`→(A#,MAJ7), `F#m7`→(F#,MIN7), `Bm7b5`→(B,M7B5), `C6`→(C,MAJ6), `Am6`→(A,MIN6).
- Case distinction: `M7` = MAJ7, `m7` = MIN7 (must differ).
- `H`, `G7sus`, `Csus9`, `Xyz` → null (error).
- Quality note counts: triads/sus = 3; all 7ths and 6ths = 4.

**Voicings**
- `candidates` for a triad returns ≤ 9, each ascending, within `[48, 86]`, deduped.
- `invert(rootPosition(C maj, 4), 1)` == `[64, 67, 72]` (E4 G4 C5).

**Cost**
- Equal size: `moveCost([60,64,67],[60,64,67])` == 0; `[60,64,67]` vs `[62,65,69]` == 5 (2+1+2).
- Unequal size falls back to symmetric nearest-note and returns a finite value.

**Solver (the important one)** — for each progression, `totalMovement(solve(·, anchor=false))` **equals** the brute-force minimum over all candidate combinations:
- `C | G | Am | F` → 9
- `F | G | Em | Am` → 11
- `Am | F | C | G` → 7
- mixed cardinality `Asus4 | G7 | Cmaj7 | Am7` → DP == brute-force min (≈9)
- `C | Am7 | Dm7 | G7` → DP == brute-force min
- In every case `totalMovement(optimized) ≤ totalMovement(rootBaseline)`.

(Implement the brute-force checker in the test only; it's exponential but fine for ≤ ~6 chords.)

**State**
- Keyboard range fully contains every voiced note; `start` is a C, `end` is a B.
- Unparseable tokens appear in `errors` and are excluded from solving.

---

## 9. Backlog — OUT OF SCOPE for v1

Prioritised by the signal that should trigger each. Several came directly from open questions in the design conversation.

1. **Two-hand voicing split** — left-hand bass (root/shell) + right-hand upper notes, with independent movement costs per hand. The natural next step once 7ths and wider chords are in; this is "real" piano arranging. Biggest item. *Trigger:* single-hand voicings feel cramped or muddy on 7th chords.
2. **Accompaniment / comping patterns for playback** — replace block-chord playback with selectable rhythmic patterns (sustained, block-on-beat, bass+chord "oom-pah", broken-chord/arpeggio, waltz, syncopated push), tied to a time signature. Turns the "Play" button into something you'd actually play along with while singing. *Trigger:* users want to *practice playing*, not just preview voicings.
3. **Melody / top-note lock** — pin the soprano voice to a sung melody line and voice-lead underneath it. Directly addresses "smoothest ≠ best-sounding." *Trigger:* users want voicings under a known tune.
4. **Bass-preference term in the cost** — soft penalty for voicings that don't keep the root (or a chosen bass) in the lowest voice, so progressions stay harmonically grounded. *Trigger:* optimized output sounds rootless.
5. **Extended & slash chords** — 9/11/13, add9, `C/E` slash bass. Requires >4-note voicings and explicit bass handling.
6. **Transpose / capo** — shift a whole progression to another key (for vocal range).
7. **Whole-song import** — paste a song's chord sheet (sections, repeats) and voice-lead the lot.
8. **Cross-mode link** — open Songs directly from a piece the user is working on in Drill mode; shared "current song" concept.

---

## 10. Reference

Companion to `sight-reading-trainer-SPEC.md` (same app). Prototype `chord-smoother.html` is the behavioural source of truth — when wording here is ambiguous, read its `<script>` block (~230 lines) and reproduce it, then re-run §8 against the Kotlin port.
