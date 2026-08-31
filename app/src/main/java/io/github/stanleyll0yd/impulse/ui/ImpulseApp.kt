package io.github.stanleyll0yd.impulse.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.stanleyll0yd.impulse.R
import io.github.stanleyll0yd.impulse.game.GameEngine
import io.github.stanleyll0yd.impulse.game.GameSnapshot
import io.github.stanleyll0yd.impulse.game.ParticleSnapshot
import io.github.stanleyll0yd.impulse.game.Vec2
import kotlinx.coroutines.isActive

private val Background = Color(0xFF050814)
private val ParticleCore = Color(0xFF8FF8FF)
private val ParticleGlow = Color(0xFF00E5FF)
private val TriggeredCore = Color(0xFFF7A8FF)
private val TriggeredGlow = Color(0xFF9E4DFF)

@Composable
fun ImpulseApp() {
    MaterialTheme {
        var gameId by remember { mutableStateOf(0) }
        val engine = remember(gameId) { GameEngine(seed = GameEngine.DEFAULT_SEED + gameId) }
        var snapshot by remember(engine) { mutableStateOf(engine.snapshot()) }

        LaunchedEffect(engine) {
            var previousFrame = 0L
            while (isActive) {
                withFrameNanos { frame ->
                    if (previousFrame != 0L) {
                        engine.advance((frame - previousFrame) / 1_000_000_000.0)
                        snapshot = engine.snapshot()
                    }
                    previousFrame = frame
                }
            }
        }

        GameScreen(
            snapshot = snapshot,
            onTap = { position ->
                if (engine.tap(position)) snapshot = engine.snapshot()
            },
            onRetry = { gameId += 1 },
        )
    }
}

@Composable
private fun GameScreen(
    snapshot: GameSnapshot,
    onTap: (Vec2) -> Unit,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game-canvas")
                .pointerInput(snapshot.field, snapshot.impulseUsed, snapshot.finished) {
                    detectTapGestures { offset ->
                        if (snapshot.impulseUsed || snapshot.finished) return@detectTapGestures
                        val viewport = calculateGameViewport(
                            canvasWidth = size.width.toFloat(),
                            canvasHeight = size.height.toFloat(),
                            field = snapshot.field,
                        )
                        val position = viewport.toGame(offset.x, offset.y, snapshot.field)
                        if (position != null) onTap(position)
                    }
                },
        ) {
            drawGame(snapshot)
        }

        Text(
            text = "${snapshot.triggeredCount} / ${snapshot.requiredCount}",
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .testTag("score"),
        )

        if (!snapshot.impulseUsed) {
            Text(
                text = stringResource(R.string.game_hint),
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .testTag("game-hint"),
            )
        }

        if (snapshot.finished) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("result"),
            ) {
                Text(
                    text = if (snapshot.success) {
                        stringResource(R.string.chain_result, snapshot.triggeredCount)
                    } else {
                        "${snapshot.triggeredCount} / ${snapshot.requiredCount}"
                    },
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
                Button(
                    onClick = onRetry,
                    modifier = Modifier.testTag("retry"),
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

private fun DrawScope.drawGame(snapshot: GameSnapshot) {
    val viewport = calculateGameViewport(size.width, size.height, snapshot.field)
    if (viewport.scale <= 0f) return

    clipRect(
        left = viewport.left,
        top = viewport.top,
        right = viewport.left + viewport.width,
        bottom = viewport.top + viewport.height,
    ) {
        snapshot.waves.forEach { wave ->
            val radius = interpolate(
                wave.previousRadius,
                wave.radius,
                snapshot.interpolationAlpha,
            ).toFloat() * viewport.scale
            val progress = (radius / (wave.maximumRadius.toFloat() * viewport.scale))
                .coerceIn(0f, 1f)
            val alpha = (1f - progress) * 0.8f
            val center = Offset(
                viewport.left + wave.origin.x.toFloat() * viewport.scale,
                viewport.top + wave.origin.y.toFloat() * viewport.scale,
            )
            drawCircle(
                color = if (wave.chainDepth == 0) {
                    ParticleGlow.copy(alpha = alpha)
                } else {
                    TriggeredGlow.copy(alpha = alpha)
                },
                radius = radius,
                center = center,
                style = Stroke(width = 2.5f + (1f - progress) * 3f),
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

            drawCircle(glow.copy(alpha = 0.12f), radius * 3.0f, center)
            drawCircle(glow.copy(alpha = 0.28f), radius * 1.8f, center)
            drawCircle(core, radius, center)
        }
    }
}

private fun interpolatePosition(particle: ParticleSnapshot, alpha: Double): Vec2 = Vec2(
    x = interpolate(particle.previousPosition.x, particle.position.x, alpha),
    y = interpolate(particle.previousPosition.y, particle.position.y, alpha),
)

private fun interpolate(start: Double, end: Double, alpha: Double): Double =
    start + (end - start) * alpha
