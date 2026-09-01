package com.sl.impulse.game

import kotlin.math.hypot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {
    @Test
    fun sameSeedProducesSameInitialState() {
        val first = GameEngine(seed = 1234L).snapshot()
        val second = GameEngine(seed = 1234L).snapshot()

        assertEquals(first.particles, second.particles)
    }

    @Test
    fun fixedStepChunkingProducesSameSimulationState() {
        val first = GameEngine(seed = 42L)
        val second = GameEngine(seed = 42L)

        repeat(180) {
            first.advance(1.0 / 30.0)
            second.advance(1.0 / 60.0)
            second.advance(1.0 / 60.0)
        }

        assertEquals(
            first.snapshot().particles.map { it.position },
            second.snapshot().particles.map { it.position },
        )
    }

    @Test
    fun onlyOnePlayerImpulseIsAccepted() {
        val engine = GameEngine(seed = 1L)

        assertTrue(engine.tap(Vec2(0.5, 0.5)))
        assertFalse(engine.tap(Vec2(0.25, 0.25)))
    }

    @Test
    fun tapOnOnlyParticleTriggersItAndSucceeds() {
        val engine = GameEngine(seed = 9L, particleCount = 1, requiredCount = 1)
        val particle = engine.snapshot().particles.single()

        engine.tap(particle.position)
        advanceUntilFinished(engine)

        val result = engine.snapshot()
        assertEquals(1, result.triggeredCount)
        assertTrue(result.success)
        assertEquals(1, result.maximumChainDepth)
        assertTrue(result.particles.single().triggeredAgeSeconds > 0.0)
    }

    @Test
    fun tapFarFromOnlyParticleFails() {
        val engine = GameEngine(seed = 11L, particleCount = 1, requiredCount = 1)
        val snapshot = engine.snapshot()
        val particle = snapshot.particles.single()
        val farCorner = Vec2(
            x = if (particle.position.x < snapshot.field.width / 2.0) snapshot.field.width else 0.0,
            y = if (particle.position.y < snapshot.field.height / 2.0) snapshot.field.height else 0.0,
        )

        engine.tap(farCorner)
        advanceUntilFinished(engine)

        val result = engine.snapshot()
        assertEquals(0, result.triggeredCount)
        assertFalse(result.success)
    }

    @Test
    fun particlesRemainInsideCanonicalField() {
        val engine = GameEngine(seed = 17L, particleCount = 100, requiredCount = 1)

        repeat(6_000) {
            engine.advance(1.0 / 60.0)
        }

        val snapshot = engine.snapshot()
        snapshot.particles.forEach { particle ->
            assertTrue(particle.position.x >= particle.radius)
            assertTrue(particle.position.x <= snapshot.field.width - particle.radius)
            assertTrue(particle.position.y >= particle.radius)
            assertTrue(particle.position.y <= snapshot.field.height - particle.radius)
        }
    }

    @Test
    fun generatedParticlesDoNotOverlap() {
        val snapshot = GameEngine(seed = 23L, particleCount = 100, requiredCount = 1).snapshot()

        for (firstIndex in snapshot.particles.indices) {
            for (secondIndex in firstIndex + 1 until snapshot.particles.size) {
                val first = snapshot.particles[firstIndex]
                val second = snapshot.particles[secondIndex]
                val distance = hypot(
                    first.position.x - second.position.x,
                    first.position.y - second.position.y,
                )
                assertTrue(distance >= first.radius + second.radius)
            }
        }
    }

    @Test
    fun largeChainReactionStressScenarioFinishes() {
        val engine = GameEngine(seed = 31L, particleCount = 200, requiredCount = 100)
        engine.tap(Vec2(GameField.DEFAULT.width / 2.0, GameField.DEFAULT.height / 2.0))

        repeat(2_400) {
            if (engine.snapshot().finished) return@repeat
            engine.advance(1.0 / 60.0)
        }

        val result = engine.snapshot()
        assertTrue(result.finished)
        assertEquals(200, result.particles.size)
        assertTrue(result.triggeredCount in 0..200)
    }

    @Test
    fun simulationEventuallyFinishesAfterTap() {
        val engine = GameEngine(seed = 2L)
        engine.tap(Vec2(0.5, GameField.DEFAULT.height / 2.0))

        advanceUntilFinished(engine)

        assertTrue(engine.snapshot().finished)
    }

    private fun advanceUntilFinished(engine: GameEngine) {
        repeat(1_200) {
            if (engine.snapshot().finished) return
            engine.advance(1.0 / 60.0)
        }
        assertTrue("Simulation did not finish", engine.snapshot().finished)
    }
}
