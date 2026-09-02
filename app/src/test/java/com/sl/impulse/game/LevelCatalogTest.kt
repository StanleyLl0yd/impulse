package com.sl.impulse.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelCatalogTest {
    @Test
    fun catalogContainsSixtyOrderedValidLevels() {
        assertEquals(60, LevelCatalog.levels.size)
        assertEquals((1..60).toList(), LevelCatalog.levels.map { it.number })
        assertEquals(60, LevelCatalog.levels.map { it.seed }.distinct().size)
        assertTrue(LevelCatalog.levels.all { it.requiredCount < it.particleCount })
        assertTrue(LevelCatalog.levels.all { it.particleMix.specialCount <= it.particleCount })
    }

    @Test
    fun campaignIsSplitIntoSixOrderedTenLevelChapters() {
        assertEquals(6, LevelCatalog.chapters.size)
        assertEquals(
            listOf(ChapterId.IMPULSE, ChapterId.MOMENTUM, ChapterId.BOOST, ChapterId.CONTROL, ChapterId.RESONANCE, ChapterId.CHAOS),
            LevelCatalog.chapters.map { it.id },
        )
        assertTrue(LevelCatalog.chapters.all { it.levels.count() == 10 })
        assertEquals((1..60).toList(), LevelCatalog.chapters.flatMap { it.levels.toList() })
        assertEquals(ChapterId.RESONANCE, LevelCatalog.chapterFor(41).id)
        assertEquals(ChapterId.CHAOS, LevelCatalog.chapterFor(60).id)
    }

    @Test
    fun originalCampaignRemainsStandardAndMechanicsProgressIntoMixedContent() {
        assertTrue(LevelCatalog.levels.take(20).all { it.particleMix.specialCount == 0 })
        assertTrue(LevelCatalog.get(21).particleMix.boosterCount > 0)
        assertTrue(LevelCatalog.get(26).particleMix.fuseCount > 0)
        assertTrue(LevelCatalog.get(31).particleMix.anchorCount > 0)

        val mixed = LevelCatalog.get(36).particleMix
        assertTrue(mixed.boosterCount > 0)
        assertTrue(mixed.fuseCount > 0)
        assertTrue(mixed.anchorCount > 0)
        assertTrue(LevelCatalog.levels.drop(40).all { level ->
            level.particleMix.boosterCount > 0 &&
                level.particleMix.fuseCount > 0 &&
                level.particleMix.anchorCount > 0
        })
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
