package com.linh.pianoflow.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.util.concurrent.CopyOnWriteArrayList

actual class TonePlayer actual constructor() {
    private val active = CopyOnWriteArrayList<AudioTrack>()

    actual fun playChord(freqsHz: List<Double>) {
        if (freqsHz.isEmpty()) return
        val pcm = ChordSynth.renderPcm16(freqsHz)
        val byteCount = pcm.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(ChordSynth.SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(byteCount)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        val written = track.write(pcm, 0, pcm.size)
        if (written <= 0) {
            track.release()
            return
        }
        active += track
        track.setNotificationMarkerPosition(pcm.size)
        track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(t: AudioTrack) {
                try { t.stop() } catch (_: Throwable) {}
                t.release()
                active.remove(t)
            }
            override fun onPeriodicNotification(t: AudioTrack) {}
        })
        track.play()
    }

    actual fun release() {
        for (t in active) {
            try { t.stop() } catch (_: Throwable) {}
            try { t.release() } catch (_: Throwable) {}
        }
        active.clear()
    }

    @Suppress("unused")
    private val streamMusic: Int = AudioManager.STREAM_MUSIC
}
