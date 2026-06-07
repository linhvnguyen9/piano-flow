package com.linh.pianoflow.audio

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.get
import kotlinx.cinterop.usePinned
import platform.AVFAudio.AVAudioEngine
import platform.AVFAudio.AVAudioFormat
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayerNode
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual class TonePlayer actual constructor() {
    private val engine = AVAudioEngine()
    private val player = AVAudioPlayerNode()
    private val format = AVAudioFormat(
        standardFormatWithSampleRate = ChordSynth.SAMPLE_RATE.toDouble(),
        channels = 1u
    )
    private var started = false

    private fun ensureStarted() {
        if (started) return
        try {
            AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
            AVAudioSession.sharedInstance().setActive(true, null)
        } catch (_: Throwable) {
        }
        engine.attachNode(player)
        engine.connect(player, engine.mainMixerNode, format)
        try {
            engine.startAndReturnError(null)
        } catch (_: Throwable) {
        }
        player.play()
        started = true
    }

    actual fun playChord(freqsHz: List<Double>) {
        if (freqsHz.isEmpty()) return
        ensureStarted()
        val pcm = ChordSynth.renderFloat(freqsHz)
        val frameCount = pcm.size.toUInt()
        val buf = AVAudioPCMBuffer(format, frameCount) ?: return
        buf.frameLength = frameCount
        val channelData = buf.floatChannelData ?: return
        val ch0 = channelData[0] ?: return
        pcm.usePinned { pinned ->
            memcpy(ch0, pinned.addressOf(0), (pcm.size * Float.SIZE_BYTES).toULong())
        }
        player.scheduleBuffer(buf, null)
    }

    actual fun release() {
        if (!started) return
        try { player.stop() } catch (_: Throwable) {}
        try { engine.stop() } catch (_: Throwable) {}
        started = false
    }
}
