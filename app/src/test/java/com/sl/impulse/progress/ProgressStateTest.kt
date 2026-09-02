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
    fun successfulFinalLevelDoesNotUnlockPastCampaign() {
        val result = calculateProgressUpdate(
            currentHighestUnlockedLevel = 20,
            currentBestScore = 2500,
            currentBestStars = 2,
            levelNumber = 20,
            score = 3000,
            stars = 3,
            success = true,
            totalLevels = 20,
        )

        assertEquals(20, result.highestUnlockedLevel)
        assertEquals(3000, result.bestScore)
        assertEquals(3, result.bestStars)
    }

    @Test
    fun completedPreviousCampaignUnlocksFirstExpandedLevel() {
        assertEquals(
            21,
            expandedCampaignHighestUnlocked(
                storedHighestUnlocked = 20,
                previousFinalLevel = 20,
                previousFinalStars = 2,
                totalLevels = 40,
            ),
        )
        assertEquals(
            20,
            expandedCampaignHighestUnlocked(
                storedHighestUnlocked = 20,
                previousFinalLevel = 20,
                previousFinalStars = 0,
                totalLevels = 40,
            ),
        )
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
    fun progressSummariesUseStoredBestResults() {
        val progress = ProgressState(
            bestScores = mapOf(1 to 1200, 2 to 900, 4 to 1400),
            bestStars = mapOf(1 to 2, 2 to 3, 4 to 1),
        )

        assertEquals(6, progress.totalStars)
        assertEquals(3, progress.completedLevels)
        assertEquals(1, progress.perfectLevels)
        assertEquals(1400, progress.bestOverallScore)
    }

    @Test
    fun statisticsAccumulateAttemptsAndKeepPeaks() {
        val first = calculateStatisticsUpdate(
            current = PlayerStatistics(),
            triggeredCount = 18,
            maximumChainDepth = 6,
            success = true,
        )
        val second = calculateStatisticsUpdate(
            current = first,
            triggeredCount = 11,
            maximumChainDepth = 4,
            success = false,
        )

        assertEquals(2, second.totalAttempts)
        assertEquals(1, second.successfulAttempts)
        assertEquals(29, second.totalTriggeredParticles)
        assertEquals(18, second.bestTriggeredCount)
        assertEquals(6, second.bestChainDepth)
        assertEquals(50, second.successRatePercent)
    }
}
