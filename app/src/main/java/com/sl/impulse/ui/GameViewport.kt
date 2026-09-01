package com.sl.impulse.ui

import com.sl.impulse.game.GameField
import com.sl.impulse.game.Vec2
import kotlin.math.min

internal data class GameViewport(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val scale: Float,
) {
    fun toGame(x: Float, y: Float, field: GameField): Vec2? {
        if (scale <= 0f) return null
        if (x < left || x > left + width || y < top || y > top + height) return null

        return Vec2(
            x = ((x - left) / scale).toDouble().coerceIn(0.0, field.width),
            y = ((y - top) / scale).toDouble().coerceIn(0.0, field.height),
        )
    }
}

internal fun calculateGameViewport(
    canvasWidth: Float,
    canvasHeight: Float,
    field: GameField,
): GameViewport {
    if (canvasWidth <= 0f || canvasHeight <= 0f) {
        return GameViewport(0f, 0f, 0f, 0f, 0f)
    }

    val scale = min(
        canvasWidth / field.width.toFloat(),
        canvasHeight / field.height.toFloat(),
    )
    val width = field.width.toFloat() * scale
    val height = field.height.toFloat() * scale

    return GameViewport(
        left = (canvasWidth - width) / 2f,
        top = (canvasHeight - height) / 2f,
        width = width,
        height = height,
        scale = scale,
    )
}
