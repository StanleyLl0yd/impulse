package com.sl.impulse.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelCatalogTest {
    @Test
    fun catalogContainsTwentyOrderedValidLevels() {
        assertEquals(20, LevelCatalog.levels.size)
        assertEquals((1..20).toList(), LevelCatalog.levels.map { it.number })
        assertEquals(20, LevelCatalog.levels.map { it.seed }.distinct().size)
        assertTrue(LevelCatalog.levels.all { it.requiredCount < it.particleCount })
    }

    @Test
    fun starThresholdsAndScoreRewardBetterResults() {
        val level = LevelCatalog.get(1)

        assertEquals(0, calculateLevelStars(level, level.requiredCount - 1, false))
        assertEquals(1, calculateLevelStars(level, level.requiredCount, true))
        assertEquals(2, calculateLevelStars(level, level.twoStarThreshold, true))
        assertEquals(3, calculateLevelStars(level, level.particleCount, true))

        val baseScore = calculateLevelScore(level, level.requiredCount, 2, true)
        val strongerScore = calculateLevelScore(level, level.requiredCount + 2, 4, true)
        assertTrue(strongerScore > baseScore)
    }
}
