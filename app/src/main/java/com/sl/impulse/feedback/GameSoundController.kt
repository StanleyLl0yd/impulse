package com.sl.impulse.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import com.sl.impulse.R
import java.util.Collections

class GameSoundController(context: Context) {
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
        play(impulse, 0.52f)
    }

    fun playChain(chainDepth: Int, triggeredDelta: Int) {
        val rate = 0.92f + minOf(chainDepth, 8) * 0.045f
        val volume = (0.24f + minOf(triggeredDelta, 5) * 0.03f).coerceAtMost(0.38f)
        play(chain, volume, rate)
    }

    fun playResult(successful: Boolean) {
        if (successful) {
            play(success, 0.48f)
        } else {
            play(failure, 0.34f)
        }
    }

    fun release() {
        released = true
        handler.removeCallbacksAndMessages(null)
        soundPool.release()
        loaded.clear()
    }

    private fun play(sampleId: Int, volume: Float, rate: Float = 1.0f, attempt: Int = 0) {
        if (released) return
        if (loaded.contains(sampleId)) {
            soundPool.play(sampleId, volume, volume, 1, 0, rate)
            return
        }
        if (attempt < 6) {
            handler.postDelayed({ play(sampleId, volume, rate, attempt + 1) }, 25L)
        }
    }
}
