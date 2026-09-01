package com.sl.impulse.progress

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressStateTest {
    @Test
    fun successfulLevelUnlocksNextAndKeepsBestResult() {
        val first = calculateProgressUpdate(
            currentHighestUnlockedLevel = 1,
            currentBestScore = 0,
            currentBestStars = 0,
            levelNumber = 1,
            score = 1200,
            stars = 2,
            success = true,
            totalLevels = 20,
        )
        val weakerReplay = calculateProgressUpdate(
            currentHighestUnlockedLevel = first.highestUnlockedLevel,
            currentBestScore = first.bestScore,
            currentBestStars = first.bestStars,
            levelNumber = 1,
            score = 900,
            stars = 1,
            success = true,
            totalLevels = 20,
        )

        assertEquals(2, weakerReplay.highestUnlockedLevel)
        assertEquals(1200, weakerReplay.bestScore)
        assertEquals(2, weakerReplay.bestStars)
    }

    @Test
    fun failedLevelDoesNotUnlockNext() {
        val result = calculateProgressUpdate(
            currentHighestUnlockedLevel = 4,
            currentBestScore = 0,
            currentBestStars = 0,
            levelNumber = 4,
            score = 700,
            stars = 0,
            success = false,
            totalLevels = 20,
        )

        assertEquals(4, result.highestUnlockedLevel)
        assertEquals(700, result.bestScore)
        assertEquals(0, result.bestStars)
    }

    @Test
    fun totalStarsSumsStoredBestResults() {
        val progress = ProgressState(bestStars = mapOf(1 to 2, 2 to 3, 4 to 1))

        assertEquals(6, progress.totalStars)
    }
}
