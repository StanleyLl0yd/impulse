package com.sl.impulse.game

import org.junit.Assert.assertTrue
import org.junit.Test

class ContentCampaignTest {
    @Test
    fun contentExpansionHasKnownWinningOpenings() {
        val openings = mapOf(
            41 to (1 to 4),
            42 to (2 to 1),
            43 to (1 to 1),
            44 to (2 to 1),
            45 to (1 to 6),
            46 to (1 to 8),
            47 to (1 to 1),
            48 to (1 to 2),
            49 to (1 to 3),
            50 to (1 to 3),
            51 to (2 to 6),
            52 to (1 to 1),
            53 to (1 to 1),
            54 to (1 to 3),
            55 to (1 to 3),
            56 to (1 to 2),
            57 to (1 to 1),
            58 to (1 to 1),
            59 to (1 to 1),
            60 to (1 to 8),
        )

        for ((levelNumber, grid) in openings) {
            val level = LevelCatalog.get(levelNumber)
            val engine = GameEngine(
                seed = level.seed,
                particleCount = level.particleCount,
                requiredCount = level.requiredCount,
                particleMix = level.particleMix,
            )
            val tap = Vec2(
                x = grid.first / 8.0,
                y = GameField.DEFAULT.height * grid.second / 13.0,
            )

            assertTrue(engine.tap(tap))
            repeat(1_200) {
                if (!engine.snapshot().finished) engine.advance(1.0 / 60.0)
            }
            assertTrue("Level $levelNumber should finish", engine.snapshot().finished)
            assertTrue("Level $levelNumber should have a verified winning opening", engine.snapshot().success)
        }
    }
}
