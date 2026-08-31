package io.github.stanleyll0yd.impulse.feedback

import android.media.AudioManager
import android.media.ToneGenerator

class GameSoundController {
    private var generator: ToneGenerator? = null

    fun playImpulse() {
        play(ToneGenerator.TONE_PROP_PROMPT, 70)
    }

    fun playChain(chainDepth: Int, triggeredDelta: Int) {
        val tone = when {
            chainDepth >= 6 -> ToneGenerator.TONE_PROP_BEEP2
            chainDepth >= 3 -> ToneGenerator.TONE_PROP_ACK
            else -> ToneGenerator.TONE_PROP_BEEP
        }
        play(tone, (35 + triggeredDelta.coerceAtMost(5) * 8))
    }

    fun playResult(success: Boolean) {
        play(
            if (success) ToneGenerator.TONE_PROP_BEEP2 else ToneGenerator.TONE_PROP_NACK,
            if (success) 180 else 140,
        )
    }

    fun release() {
        generator?.release()
        generator = null
    }

    private fun play(tone: Int, durationMs: Int) {
        val current = generator ?: ToneGenerator(AudioManager.STREAM_MUSIC, 48).also {
            generator = it
        }
        current.startTone(tone, durationMs)
    }
}
