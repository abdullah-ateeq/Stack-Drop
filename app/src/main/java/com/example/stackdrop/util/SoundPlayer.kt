package com.example.stackdrop.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundPlayer {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 44100
    @Volatile var isEnabled: Boolean = true

    fun playCountdown(isGo: Boolean) {
        if (!isEnabled) return
        scope.launch {
            val freq = if (isGo) 880.0 else 520.0
            val durationMs = if (isGo) 180 else 90
            playTone(freq, durationMs, 0.45f)
        }
    }

    fun playDrop() {
        if (!isEnabled) return
        scope.launch {
            // Frequency sweep downward: 600Hz -> 250Hz in 80ms
            val numSamples = (sampleRate * 0.08).toInt()
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val currentFreq = 600.0 - (progress * 350.0)
                val envelope = 1.0 - progress
                val sample = (sin(2.0 * PI * currentFreq * t) * envelope * 0.35 * Short.MAX_VALUE).toInt()
                samples[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(samples)
        }
    }

    fun playStack() {
        if (!isEnabled) return
        scope.launch {
            // Snappy pleasant marimba/woodblock pop: 540Hz with sharp exponential decay
            val numSamples = (sampleRate * 0.12).toInt()
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val envelope = exp(- progress * 15.0)
                val sample = (sin(2.0 * PI * 540.0 * t) * envelope * 0.6 * Short.MAX_VALUE).toInt()
                samples[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(samples)
        }
    }

    fun playPerfect() {
        if (!isEnabled) return
        scope.launch {
            // Bright cheerful double chime: 880Hz then 1320Hz
            val partSamples = (sampleRate * 0.11).toInt()
            val totalSamples = partSamples * 2
            val samples = ShortArray(totalSamples)

            for (i in 0 until partSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / partSamples
                val envelope = exp(- progress * 8.0)
                val sample = (sin(2.0 * PI * 880.0 * t) * envelope * 0.5 * Short.MAX_VALUE).toInt()
                samples[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            for (i in 0 until partSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / partSamples
                val envelope = exp(- progress * 7.0)
                val sample = (sin(2.0 * PI * 1320.0 * t) * envelope * 0.55 * Short.MAX_VALUE).toInt()
                samples[partSamples + i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(samples)
        }
    }

    fun playMilestone() {
        if (!isEnabled) return
        scope.launch {
            // Triad fanfare: C5 (523Hz), E5 (659Hz), G5 (784Hz)
            val notes = doubleArrayOf(523.25, 659.25, 783.99)
            val noteDuration = 0.10
            val noteSamples = (sampleRate * noteDuration).toInt()
            val totalSamples = noteSamples * notes.size
            val samples = ShortArray(totalSamples)

            notes.forEachIndexed { index, freq ->
                val offset = index * noteSamples
                for (i in 0 until noteSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / noteSamples
                    val envelope = exp(- progress * 6.0)
                    val sample = (sin(2.0 * PI * freq * t) * envelope * 0.5 * Short.MAX_VALUE).toInt()
                    samples[offset + i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            playPcm(samples)
        }
    }

    fun playGameOver() {
        if (!isEnabled) return
        scope.launch {
            // Gentle descending minor tone: 440Hz -> 311Hz -> 220Hz
            val notes = doubleArrayOf(440.0, 370.0, 311.13, 220.0)
            val noteDuration = 0.14
            val noteSamples = (sampleRate * noteDuration).toInt()
            val totalSamples = noteSamples * notes.size
            val samples = ShortArray(totalSamples)

            notes.forEachIndexed { index, freq ->
                val offset = index * noteSamples
                for (i in 0 until noteSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / noteSamples
                    val envelope = exp(- progress * 6.0)
                    val sample = (sin(2.0 * PI * freq * t) * envelope * 0.45 * Short.MAX_VALUE).toInt()
                    samples[offset + i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
            playPcm(samples)
        }
    }

    private fun playTone(freq: Double, durationMs: Int, volume: Float) {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val envelope = 1.0 - progress
            val sample = (sin(2.0 * PI * freq * t) * envelope * volume * Short.MAX_VALUE).toInt()
            samples[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        playPcm(samples)
    }

    private fun playPcm(pcm: ShortArray) {
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufSize, pcm.size * 2)
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcm, 0, pcm.size)
            track.play()
            // Release after playback finishes
            val sleepMs = ((pcm.size.toDouble() / sampleRate) * 1000).toLong() + 50
            Thread.sleep(sleepMs)
            track.stop()
            track.release()
        } catch (_: Exception) {
            // Silently handle audio track failure on unsupported environments
        }
    }
}
