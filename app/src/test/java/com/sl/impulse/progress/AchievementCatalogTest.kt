package com.sl.impulse.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementCatalogTest {
    @Test
    fun newPlayerStartsWithAllAchievementsLocked() {
        val achievements = AchievementCatalog.evaluate(PlayerState())
        assertEquals(25, achievements.size)
        assertTrue(achievements.none { it.unlocked })
    }

    @Test
    fun completePerfectCampaignAndReplayProgressUnlocksEverything() {
        val state = PlayerState(
            progress = ProgressState(
                highestUnlockedLevel = 60,
                bestScores = (1..60).associateWith { 1_000 + it },
                bestStars = (1..60).associateWith { 3 },
            ),
            statistics = PlayerStatistics(
                totalAttempts = 100,
                successfulAttempts = 50,
                totalTriggeredParticles = 1_000,
                bestTriggeredCount = 50,
                bestChainDepth = 15,
            ),
            replay = ReplayProgress(endlessBestRound = 10, endlessBestScore = 25_000, dailyCompletedDays = 7),
        )
        val achievements = AchievementCatalog.evaluate(state)
        assertEquals(25, achievements.size)
        assertTrue(achievements.all { it.unlocked })
    }

    @Test
    fun completingFirstChapterDoesNotUnlockLaterJourneyAchievements() {
        val state = PlayerState(
            progress = ProgressState(highestUnlockedLevel = 11, bestStars = (1..10).associateWith { 1 }),
        )
        val achievements = AchievementCatalog.evaluate(state).associateBy { it.id }
        assertTrue(requireNotNull(achievements[AchievementId.FIRST_IMPULSE]).unlocked)
        assertTrue(requireNotNull(achievements[AchievementId.TEN_LEVELS]).unlocked)
        assertTrue(requireNotNull(achievements[AchievementId.CHAPTER_IMPULSE]).unlocked)
        assertFalse(requireNotNull(achievements[AchievementId.HALFWAY]).unlocked)
        assertFalse(requireNotNull(achievements[AchievementId.MASTER]).unlocked)
        assertFalse(requireNotNull(achievements[AchievementId.DAILY_IMPULSE]).unlocked)
    }
}
