package com.sl.impulse.feedback

import android.app.Activity
import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.musicDataStore by preferencesDataStore(name = "music_settings")

internal class MusicPreferences(context: Context) {
    private val dataStore = context.applicationContext.musicDataStore

    val enabled: Flow<Boolean> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> preferences[MUSIC_ENABLED] ?: true }

    suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[MUSIC_ENABLED] = enabled }
    }

    private companion object {
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
    }
}

internal class AdaptiveAmbientMusic(context: Context) {
    private val application = context.applicationContext as Application
    private val preferences = MusicPreferences(application)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val disposed = AtomicBoolean(false)
    private val lock = Any()

    @Volatile
    private var enabled = false

    @Volatile
    private var foreground = true

    @Volatile
    private var intensityTarget = 0f

    @Volatile
    private var lastReactionNanos = 0L

    private var worker: Thread? = null

    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityStarted(activity: Activity) {
            foreground = true
            ensurePlayback()
        }

        override fun onActivityStopped(activity: Activity) {
            foreground = false
        }

        override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }

    init {
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
        scope.launch {
            preferences.enabled.collect { value ->
                enabled = value
                ensurePlayback()
            }
        }
    }

    fun react(chainDepth: Int) {
        intensityTarget = ambientIntensityForDepth(chainDepth)
        lastReactionNanos = System.nanoTime()
    }

    fun settle() {
        intensityTarget = 0f
        lastReactionNanos = 0L
    }

    fun release() {
        if (!disposed.compareAndSet(false, true)) return
        enabled = false
        foreground = false
        intensityTarget = 0f
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        scope.cancel()
    }

    private fun ensurePlayback() {
        if (disposed.get() || !enabled || !foreground) return
        synchronized(lock) {
            if (worker?.isAlive == true) return
            worker = Thread(::runAudio, "impulse-ambient").apply {
                isDaemon = true
                start()
            }
        }
    }

    private fun runAudio() {
        val minBuffer = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBuffer <= 0) {
            clearWorker()
            return
        }

        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build(),
                )
                .setBufferSizeInBytes(max(minBuffer, BUFFER_FRAMES * 8))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }.getOrNull()

        if (track == null || track.state != AudioTrack.STATE_INITIALIZED) {
            track?.release()
            clearWorker()
            return
        }

        val synth = AmbientSynth()
        val buffer = ShortArray(BUFFER_FRAMES * 2)
        runCatching { track.play() }

        try {
            while (true) {
                val active = !disposed.get() && enabled && foreground
                val reactionFresh = lastReactionNanos != 0L &&
                    System.nanoTime() - lastReactionNanos < REACTION_HOLD_NANOS
                val target = if (active && reactionFresh) intensityTarget else 0f
                val audible = synth.fill(buffer, active, target)
                if (!active && !audible) break
                if (track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING) < 0) break
            }
        } finally {
            runCatching { track.stop() }
            track.release()
            clearWorker()
        }
    }

    private fun clearWorker() {
        synchronized(lock) {
            if (worker === Thread.currentThread()) worker = null
        }
    }

    private class AmbientSynth {
        private val baseFrequencies = doubleArrayOf(73.416, 110.0, 146.832, 174.614, 220.0, 261.626, 329.628)
        private val baseAmplitudes = doubleArrayOf(0.16, 0.115, 0.095, 0.065, 0.043, 0.028, 0.018)
        private val basePans = doubleArrayOf(-0.55, 0.38, -0.18, 0.58, -0.42, 0.16, 0.62)
        private val shimmerFrequencies = doubleArrayOf(293.665, 349.228, 440.0, 523.251, 659.255)
        private val shimmerAmplitudes = doubleArrayOf(0.050, 0.040, 0.030, 0.021, 0.014)
        private val shimmerPans = doubleArrayOf(0.62, -0.58, 0.28, -0.22, 0.74)
        private val baseLeftPhase = DoubleArray(baseFrequencies.size) { it * 0.071 }
        private val baseRightPhase = DoubleArray(baseFrequencies.size) { it * 0.071 + 0.13 }
        private val shimmerLeftPhase = DoubleArray(shimmerFrequencies.size) { it * 0.113 + 0.19 }
        private val shimmerRightPhase = DoubleArray(shimmerFrequencies.size) { it * 0.113 + 0.47 }
        private val baseLeftPan = DoubleArray(basePans.size) { panLeft(basePans[it]) }
        private val baseRightPan = DoubleArray(basePans.size) { panRight(basePans[it]) }
        private val shimmerLeftPan = DoubleArray(shimmerPans.size) { panLeft(shimmerPans[it]) }
        private val shimmerRightPan = DoubleArray(shimmerPans.size) { panRight(shimmerPans[it]) }
        private val noteMovement = DoubleArray(baseFrequencies.size)
        private val shimmerMovement = DoubleArray(shimmerFrequencies.size)
        private var sampleIndex = 0L
        private var fade = 0.0
        private var intensity = 0.0

        fun fill(buffer: ShortArray, active: Boolean, targetIntensity: Float): Boolean {
            val seconds = sampleIndex.toDouble() / SAMPLE_RATE
            val baseMovement = 0.86 +
                0.08 * sin(2.0 * PI * 0.0125 * seconds) +
                0.05 * sin(2.0 * PI * 0.0208 * seconds + 1.7)
            baseFrequencies.indices.forEach { index ->
                noteMovement[index] = 0.78 +
                    0.22 * sin(2.0 * PI * (0.0038 + index * 0.0007) * seconds + index * 0.83)
            }
            shimmerFrequencies.indices.forEach { index ->
                shimmerMovement[index] = 0.58 +
                    0.42 * sin(2.0 * PI * (0.0052 + index * 0.0009) * seconds + index * 1.11)
            }
            val fadeStep = 1.0 / (SAMPLE_RATE * if (active) 1.4 else 0.7)
            val intensityStep = 1.0 / (SAMPLE_RATE * 0.8)
            val target = targetIntensity.toDouble()

            repeat(BUFFER_FRAMES) { frame ->
                fade = if (active) (fade + fadeStep).coerceAtMost(1.0) else (fade - fadeStep).coerceAtLeast(0.0)
                intensity += (target - intensity).coerceIn(-intensityStep, intensityStep)

                var left = 0.0
                var right = 0.0
                baseFrequencies.indices.forEach { index ->
                    val amplitude = baseAmplitudes[index] * noteMovement[index] * baseMovement
                    left += sample(baseLeftPhase[index]) * amplitude * baseLeftPan[index]
                    right += sample(baseRightPhase[index]) * amplitude * baseRightPan[index]
                    baseLeftPhase[index] = advance(baseLeftPhase[index], baseFrequencies[index] * 0.99955)
                    baseRightPhase[index] = advance(baseRightPhase[index], baseFrequencies[index] * 1.00045)
                }
                shimmerFrequencies.indices.forEach { index ->
                    val amplitude = shimmerAmplitudes[index] * shimmerMovement[index] * intensity
                    left += sample(shimmerLeftPhase[index]) * amplitude * shimmerLeftPan[index]
                    right += sample(shimmerRightPhase[index]) * amplitude * shimmerRightPan[index]
                    shimmerLeftPhase[index] = advance(shimmerLeftPhase[index], shimmerFrequencies[index] * 0.9992)
                    shimmerRightPhase[index] = advance(shimmerRightPhase[index], shimmerFrequencies[index] * 1.0008)
                }

                val gain = MASTER_GAIN * fade
                buffer[frame * 2] = toPcm(left * gain)
                buffer[frame * 2 + 1] = toPcm(right * gain)
                sampleIndex++
            }
            return fade > 0.0001
        }

        private fun advance(phase: Double, frequency: Double): Double {
            val next = phase + frequency / SAMPLE_RATE
            return if (next >= 1.0) next - 1.0 else next
        }

        private fun sample(phase: Double): Double = SIN_TABLE[(phase * TABLE_SIZE).toInt() and TABLE_MASK]

        private fun toPcm(value: Double): Short =
            (value.coerceIn(-0.95, 0.95) * Short.MAX_VALUE).toInt().toShort()

        companion object {
            private fun panLeft(pan: Double): Double = sqrt((1.0 - pan) * 0.5)
            private fun panRight(pan: Double): Double = sqrt((1.0 + pan) * 0.5)
        }
    }

    private companion object {
        const val SAMPLE_RATE = 22_050
        const val BUFFER_FRAMES = 1_024
        const val MASTER_GAIN = 0.42
        const val REACTION_HOLD_NANOS = 2_400_000_000L
        const val TABLE_SIZE = 4_096
        const val TABLE_MASK = TABLE_SIZE - 1
        val SIN_TABLE = DoubleArray(TABLE_SIZE) { index -> sin(2.0 * PI * index / TABLE_SIZE) }
    }
}
