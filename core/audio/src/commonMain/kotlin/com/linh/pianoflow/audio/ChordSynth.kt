package com.linh.pianoflow.audio

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor

object ChordSynth {
    const val SAMPLE_RATE = 44100
    const val DURATION_S = 0.9
    const val ATTACK_S = 0.02
    const val PEAK_GAIN = 0.18
    private const val DECAY_TAU = 0.30

    fun renderFloat(freqsHz: List<Double>): FloatArray {
        if (freqsHz.isEmpty()) return FloatArray(0)
        val total = (SAMPLE_RATE * DURATION_S).toInt()
        val out = FloatArray(total)
        val mix = 1.0 / freqsHz.size
        for (i in 0 until total) {
            val t = i.toDouble() / SAMPLE_RATE
            val env = if (t < ATTACK_S) (t / ATTACK_S) * PEAK_GAIN
            else PEAK_GAIN * exp(-(t - ATTACK_S) / DECAY_TAU)
            var s = 0.0
            for (f in freqsHz) {
                val x = t * f
                val tri = 2.0 * abs(2.0 * (x - floor(x + 0.5))) - 1.0
                s += tri
            }
            out[i] = (s * mix * env).toFloat()
        }
        return out
    }

    fun renderPcm16(freqsHz: List<Double>): ShortArray {
        val f = renderFloat(freqsHz)
        val out = ShortArray(f.size)
        for (i in f.indices) {
            val v = (f[i] * Short.MAX_VALUE).toInt()
            out[i] = (if (v > Short.MAX_VALUE.toInt()) Short.MAX_VALUE.toInt()
            else if (v < Short.MIN_VALUE.toInt()) Short.MIN_VALUE.toInt() else v).toShort()
        }
        return out
    }
}
