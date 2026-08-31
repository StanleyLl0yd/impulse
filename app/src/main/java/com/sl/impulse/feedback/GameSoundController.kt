package com.sl.impulse.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import com.sl.impulse.R
import java.util.Collections
import kotlin.math.min

class GameSoundController(context: Context) {
    private data class Playback(
        val sampleId: Int,
        val volume: Float,
        val rate: Float,
    )

    private val handler = Handler(Looper.getMainLooper())
    private val loaded = Collections.synchronizedSet(mutableSetOf<Int>())
    private var released = false
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
        .also { pool ->
            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) loaded += sampleId
            }
        }

    private val impulse = soundPool.load(context, R.raw.sound_impulse, 1)
    private val chain = soundPool.load(context, R.raw.sound_chain, 1)
    private val success = soundPool.load(context, R.raw.sound_success, 1)
    private val failure = soundPool.load(context, R.raw.sound_failure, 1)

    fun playImpulse() {
        play(Playback(impulse, 0.52f, 1.0f))
    }

    fun playChain(chainDepth: Int, triggeredDelta: Int) {
        val depth = min(chainDepth, 8)
        val rate = (0.92f + depth * 0.045f).coerceAtMost(1.28f)
        val volume = (0.24f + min(triggeredDelta, 5) * 0.03f).coerceAtMost(0.38f)
        play(Playback(chain, volume, rate))
    }

    fun playResult(successful: Boolean) {
        if (successful) {
            play(Playback(success, 0.48f, 1.0f))
        } else {
            play(Playback(failure, 0.34f, 1.0f))
        }
    }

    fun release() {
        released = true
        handler.removeCallbacksAndMessages(null)
        soundPool.release()
        loaded.clear()
    }

    private fun play(playback: Playback, attempt: Int = 0) {
        if (released) return
        if (loaded.contains(playback.sampleId)) {
            soundPool.play(
                playback.sampleId,
                playback.volume,
                playback.volume,
                1,
                0,
                playback.rate,
            )
            return
        }
        if (attempt < 6) {
            handler.postDelayed({ play(playback, attempt + 1) }, 25L)
        }
    }
}
