package com.sl.impulse.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import com.sl.impulse.game.GameSnapshot
import com.sl.impulse.game.ParticleSnapshot
import com.sl.impulse.game.Vec2
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val BURST_DURATION_SECONDS = 0.34f
private val GameBackgroundColors = listOf(Color(0xFF0D1731), Background)

internal fun DrawScope.drawGame(snapshot: GameSnapshot, reducedEffects: Boolean) {
    drawRect(
        brush = Brush.radialGradient(
            colors = GameBackgroundColors,
            center = Offset(size.width * 0.5f, size.height * 0.42f),
            radius = maxOf(size.width, size.height) * 0.82f,
        ),
    )

    val viewport = calculateGameViewport(size.width, size.height, snapshot.field)
    if (viewport.scale <= 0f) return

    clipRect(
        left = viewport.left,
        top = viewport.top,
        right = viewport.left + viewport.width,
        bottom = viewport.top + viewport.height,
    ) {
        if (!reducedEffects) {
            val impact = snapshot.particles.maxOfOrNull { particle ->
                if (!particle.triggered) {
                    0f
                } else {
                    (1f - particle.triggeredAgeSeconds.toFloat() / 0.14f).coerceIn(0f, 1f)
                }
            } ?: 0f
            if (impact > 0f) {
                drawRect(TriggeredGlow.copy(alpha = impact * 0.055f))
            }
        }

        snapshot.waves.forEach { wave ->
            val radius = interpolate(
                wave.previousRadius,
                wave.radius,
                snapshot.interpolationAlpha,
            ).toFloat() * viewport.scale
            val maximumRadius = wave.maximumRadius.toFloat() * viewport.scale
            val progress = (radius / maximumRadius).coerceIn(0f, 1f)
            val fade = 1f - progress
            val color = if (wave.chainDepth == 0) ParticleGlow else TriggeredGlow
            val center = Offset(
                viewport.left + wave.origin.x.toFloat() * viewport.scale,
                viewport.top + wave.origin.y.toFloat() * viewport.scale,
            )

            if (!reducedEffects) {
                drawCircle(
                    color = color.copy(alpha = fade * 0.07f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = 18f * fade + 5f),
                )
                drawCircle(
                    color = color.copy(alpha = fade * 0.18f),
                    radius = (radius - 2f).coerceAtLeast(0f),
                    center = center,
                    style = Stroke(width = 7f * fade + 2f),
                )
            }
            drawCircle(
                color = color.copy(alpha = fade * 0.9f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.8f + fade * 2.2f),
            )
        }

        snapshot.particles.forEach { particle ->
            val position = interpolatePosition(particle, snapshot.interpolationAlpha)
            val center = Offset(
                viewport.left + position.x.toFloat() * viewport.scale,
                viewport.top + position.y.toFloat() * viewport.scale,
            )
            val radius = particle.radius.toFloat() * viewport.scale
            val glow = if (particle.triggered) TriggeredGlow else ParticleGlow
            val core = if (particle.triggered) TriggeredCore else ParticleCore

            if (!reducedEffects && !particle.triggered) {
                val previousCenter = Offset(
                    viewport.left + particle.previousPosition.x.toFloat() * viewport.scale,
                    viewport.top + particle.previousPosition.y.toFloat() * viewport.scale,
                )
                val delta = center - previousCenter
                drawLine(
                    color = glow.copy(alpha = 0.18f),
                    start = center - delta * 14f,
                    end = center,
                    strokeWidth = (radius * 0.7f).coerceAtLeast(1f),
                    cap = StrokeCap.Round,
                )
            }

            if (!reducedEffects) {
                drawCircle(glow.copy(alpha = 0.08f), radius * 4.4f, center)
                drawCircle(glow.copy(alpha = 0.18f), radius * 2.5f, center)
            } else {
                drawCircle(glow.copy(alpha = 0.14f), radius * 1.8f, center)
            }

            if (particle.triggered) {
                val burstLife = (
                    1f - particle.triggeredAgeSeconds.toFloat() / BURST_DURATION_SECONDS
                ).coerceIn(0f, 1f)
                val pulse = 1f + burstLife * 0.34f
                drawCircle(core, radius * pulse, center)
                if (!reducedEffects && burstLife > 0f) {
                    drawActivationBurst(
                        center = center,
                        radius = radius,
                        life = burstLife,
                        chainDepth = particle.chainDepth,
                    )
                }
            } else {
                drawCircle(core, radius, center)
                drawCircle(Color.White.copy(alpha = 0.52f), radius * 0.34f, center)
            }
        }
    }
}

private fun DrawScope.drawActivationBurst(
    center: Offset,
    radius: Float,
    life: Float,
    chainDepth: Int,
) {
    val rayCount = 6
    val distance = radius * (2.2f + (1f - life) * 3.2f)
    val length = radius * (0.9f + life * 0.8f)
    val rotation = chainDepth * 0.37f

    repeat(rayCount) { index ->
        val angle = index * (2.0 * PI / rayCount) + rotation
        val direction = Offset(cos(angle).toFloat(), sin(angle).toFloat())
        val start = center + direction * distance
        val end = start + direction * length
        drawLine(
            color = TriggeredCore.copy(alpha = life * 0.72f),
            start = start,
            end = end,
            strokeWidth = (radius * 0.28f).coerceAtLeast(1f),
            cap = StrokeCap.Round,
        )
    }
}

private fun interpolatePosition(particle: ParticleSnapshot, alpha: Double): Vec2 = Vec2(
    x = interpolate(particle.previousPosition.x, particle.position.x, alpha),
    y = interpolate(particle.previousPosition.y, particle.position.y, alpha),
)

private fun interpolate(start: Double, end: Double, alpha: Double): Double =
    start + (end - start) * alpha
