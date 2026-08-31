package io.github.stanleyll0yd.impulse.ui

import io.github.stanleyll0yd.impulse.game.GameField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameViewportTest {
    @Test
    fun portraitDisplayUsesUniformScaleAndVerticalLetterboxing() {
        val viewport = calculateGameViewport(
            canvasWidth = 1080f,
            canvasHeight = 2400f,
            field = GameField.DEFAULT,
        )

        assertEquals(1080f, viewport.width, 0.001f)
        assertEquals(1920f, viewport.height, 0.001f)
        assertEquals(0f, viewport.left, 0.001f)
        assertEquals(240f, viewport.top, 0.001f)
        assertEquals(1080f, viewport.scale, 0.001f)
    }

    @Test
    fun screenCenterMapsToLogicalFieldCenter() {
        val viewport = calculateGameViewport(1080f, 2400f, GameField.DEFAULT)
        val point = requireNotNull(viewport.toGame(540f, 1200f, GameField.DEFAULT))

        assertEquals(GameField.DEFAULT.width / 2.0, point.x, 1e-6)
        assertEquals(GameField.DEFAULT.height / 2.0, point.y, 1e-6)
    }

    @Test
    fun letterboxAreaDoesNotMapToGameplay() {
        val viewport = calculateGameViewport(1080f, 2400f, GameField.DEFAULT)

        assertNull(viewport.toGame(540f, 100f, GameField.DEFAULT))
        assertNull(viewport.toGame(540f, 2300f, GameField.DEFAULT))
    }
}
