package com.sl.impulse.game

import org.junit.Assert.assertTrue
import org.junit.Test

class ContentCampaignTest {
    @Test
    fun campaignDifficultyCurveIsProgressiveAndEveryLevelHasSampledWinningOpenings() {
        val metrics = LevelCatalog.levels.associate { level ->
            level.number to evaluateDifficulty(level)
        }

        assertTrue(
            "Every campaign level must expose at least one sampled winning opening",
            metrics.values.all { it.successCount > 0 },
        )

        val chapterAverages = LevelCatalog.chapters.map { chapter ->
            chapter.levels.map { metrics.getValue(it).successRate }.average()
        }
        assertTrue(
            "Average sampled success rate must tighten from chapter to chapter: $chapterAverages",
            chapterAverages.zipWithNext().all { (earlier, later) -> earlier > later },
        )

        assertTrue(
            "Levels 1-5 must remain forgiving while the player learns the core mechanic",
            (1..5).all { metrics.getValue(it).successRate >= 0.30 },
        )
        assertTrue(
            "Levels 31-40 must not contain broad trivial openings",
            (31..40).all { metrics.getValue(it).successRate <= 0.25 },
        )
        assertTrue(
            "Levels 41-50 must stay tighter than the middle campaign",
            (41..50).all { metrics.getValue(it).successRate <= 0.22 },
        )
        assertTrue(
            "Levels 51-60 must remain endgame puzzles without becoming effectively unsolvable",
            (51..60).all {
                metrics.getValue(it).successRate in 0.04..0.20
            },
        )
    }

    private fun evaluateDifficulty(level: LevelDefinition): DifficultyMetrics {
        var successes = 0
        var attempts = 0

        for (waitSteps in listOf(0, 60)) {
            for (xStep in 1..5) {
                for (yStep in 1..8) {
                    val engine = GameEngine(
                        seed = level.seed,
                        particleCount = level.particleCount,
                        requiredCount = level.requiredCount,
                        particleMix = level.particleMix,
                    )
                    repeat(waitSteps) { engine.advance(STEP_SECONDS) }

                    val tap = Vec2(
                        x = xStep / 6.0,
                        y = GameField.DEFAULT.height * yStep / 9.0,
                    )
                    assertTrue(engine.tap(tap))

                    repeat(MAX_SIMULATION_STEPS) {
                        if (!engine.snapshot().finished) engine.advance(STEP_SECONDS)
                    }

                    val result = engine.snapshot()
                    assertTrue("Level ${level.number} simulation must finish", result.finished)
                    if (result.success) successes += 1
                    attempts += 1
                }
            }
        }

        return DifficultyMetrics(successes, attempts)
    }

    private data class DifficultyMetrics(
        val successCount: Int,
        val sampleCount: Int,
    ) {
        val successRate: Double = successCount.toDouble() / sampleCount
    }

    private companion object {
        const val STEP_SECONDS = 1.0 / 60.0
        const val MAX_SIMULATION_STEPS = 1_200
    }
}
