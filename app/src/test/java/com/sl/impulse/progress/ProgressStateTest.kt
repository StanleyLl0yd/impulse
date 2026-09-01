package com.sl.impulse.progress

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressStateTest {
    @Test
    fun successfulLevelUnlocksNextAndKeepsBestResult() {
        val first = recordLevelResult(
            state = ProgressState(),
            levelNumber = 1,
            score = 1200,
            stars = 2,
            success = true,
            totalLevels = 20,
        )
        val weakerReplay = recordLevelResult(
            state = first,
            levelNumber = 1,
            score = 900,
            stars = 1,
            success = true,
            totalLevels = 20,
        )

        assertEquals(2, weakerReplay.highestUnlockedLevel)
        assertEquals(1200, weakerReplay.bestScore(1))
        assertEquals(2, weakerReplay.stars(1))
        assertEquals(2, weakerReplay.totalStars)
    }

    @Test
    fun failedLevelDoesNotUnlockNext() {
        val result = recordLevelResult(
            state = ProgressState(highestUnlockedLevel = 4),
            levelNumber = 4,
            score = 700,
            stars = 0,
            success = false,
            totalLevels = 20,
        )

        assertEquals(4, result.highestUnlockedLevel)
        assertEquals(700, result.bestScore(4))
        assertEquals(0, result.stars(4))
    }
}
