package io.github.stanleyll0yd.impulse.game

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
    fun onlyOnePlayerImpulseIsAccepted() {
        val engine = GameEngine(seed = 1L)

        assertTrue(engine.tap(Vec2(0.5, 0.5)))
        assertFalse(engine.tap(Vec2(0.25, 0.25)))
    }

    @Test
    fun simulationEventuallyFinishesAfterTap() {
        val engine = GameEngine(seed = 2L)
        engine.tap(Vec2(0.5, 0.5))

        repeat(600) {
            engine.advance(1.0 / 60.0)
        }

        assertTrue(engine.snapshot().finished)
    }
}
