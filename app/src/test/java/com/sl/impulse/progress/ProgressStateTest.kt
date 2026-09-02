package com.sl.impulse.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
            totalLevels = 60,
        )
        val weakerReplay = calculateProgressUpdate(
            currentHighestUnlockedLevel = first.highestUnlockedLevel,
            currentBestScore = first.bestScore,
            currentBestStars = first.bestStars,
            levelNumber = 1,
            score = 900,
            stars = 1,
            success = true,
            totalLevels = 60,
        )

        assertEquals(2, weakerReplay.highestUnlockedLevel)
        assertEquals(1200, weakerReplay.bestScore)
        assertEquals(2, weakerReplay.bestStars)
    }

    @Test
    fun successfulFinalLevelDoesNotUnlockPastCampaign() {
        val result = calculateProgressUpdate(
            currentHighestUnlockedLevel = 60,
            currentBestScore = 2500,
            currentBestStars = 2,
            levelNumber = 60,
            score = 3000,
            stars = 3,
            success = true,
            totalLevels = 60,
        )

        assertEquals(60, result.highestUnlockedLevel)
        assertEquals(3000, result.bestScore)
        assertEquals(3, result.bestStars)
    }

    @Test
    fun historicalCampaignBoundariesUnlockTheirNextContent() {
        assertEquals(
            21,
            expandedCampaignHighestUnlocked(
                storedHighestUnlocked = 20,
                completedPreviousFinalLevels = setOf(20),
                totalLevels = 60,
            ),
        )
        assertEquals(
            41,
            expandedCampaignHighestUnlocked(
                storedHighestUnlocked = 40,
                completedPreviousFinalLevels = setOf(20, 40),
                totalLevels = 60,
            ),
        )
        assertEquals(
            20,
            expandedCampaignHighestUnlocked(
                storedHighestUnlocked = 20,
                completedPreviousFinalLevels = emptySet(),
                totalLevels = 60,
            ),
        )
        assertEquals(
            40,
            expandedCampaignHighestUnlocked(
                storedHighestUnlocked = 40,
                completedPreviousFinalLevels = setOf(20),
                totalLevels = 60,
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
            totalLevels = 60,
        )

        assertEquals(4, result.highestUnlockedLevel)
        assertEquals(700, result.bestScore)
        assertEquals(0, result.bestStars)
    }

    @Test
    fun progressSummariesUseStoredBestResultsAndRanges() {
        val progress = ProgressState(
            bestScores = mapOf(1 to 1200, 2 to 900, 4 to 1400),
            bestStars = mapOf(1 to 2, 2 to 3, 4 to 1),
        )

        assertEquals(6, progress.totalStars)
        assertEquals(3, progress.completedLevels)
        assertEquals(1, progress.perfectLevels)
        assertEquals(1400, progress.bestOverallScore)
        assertEquals(3, progress.completedLevels(1..4))
        assertEquals(1, progress.perfectLevels(1..4))
        assertFalse(progress.isCompleted(1..4))
        assertFalse(progress.isPerfect(1..4))

        val perfectChapter = ProgressState(bestStars = (1..10).associateWith { 3 })
        assertTrue(perfectChapter.isCompleted(1..10))
        assertTrue(perfectChapter.isPerfect(1..10))
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
