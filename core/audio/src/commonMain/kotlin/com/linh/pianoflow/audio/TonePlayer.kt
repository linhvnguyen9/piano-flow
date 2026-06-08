package com.linh.pianoflow.audio

expect class TonePlayer() {
    fun playChord(freqsHz: List<Double>)
    fun release()
}
