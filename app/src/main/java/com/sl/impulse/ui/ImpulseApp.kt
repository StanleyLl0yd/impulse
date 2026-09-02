package com.sl.impulse.ui

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sl.impulse.R
import com.sl.impulse.feedback.GameSoundController
import com.sl.impulse.game.GameEngine
import com.sl.impulse.game.GameSnapshot
import com.sl.impulse.game.LevelCatalog
import com.sl.impulse.game.LevelDefinition
import com.sl.impulse.game.Vec2
import com.sl.impulse.game.calculateLevelScore
import com.sl.impulse.game.calculateLevelStars
import com.sl.impulse.progress.PlayerState
import com.sl.impulse.progress.PlayerStateRepository
import com.sl.impulse.progress.ProgressState
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
internal fun ImpulseGame(
    repository: PlayerStateRepository,
    playerState: PlayerState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedLevelNumber by rememberSaveable { mutableIntStateOf(1) }
    var attemptId by remember { mutableIntStateOf(0) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showLevelPicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(playerState.selectedLevel) {
        selectedLevelNumber = playerState.selectedLevel.coerceIn(1, LevelCatalog.levels.size)
    }

    val level = LevelCatalog.get(selectedLevelNumber)
    val engine = remember(level.number, attemptId) {
        GameEngine(
            seed = level.seed,
            particleCount = level.particleCount,
            requiredCount = level.requiredCount,
            particleMix = level.particleMix,
        )
    }
    val previousBestScore = remember(engine) { playerState.progress.bestScore(level.number) }
    var snapshot by remember(engine) { mutableStateOf(engine.snapshot()) }
    var previousTriggeredCount by remember(engine) { mutableIntStateOf(0) }
    val soundController = remember(context.applicationContext) {
        GameSoundController(context.applicationContext)
    }
    val view = LocalView.current

    DisposableEffect(soundController) {
        onDispose { soundController.release() }
    }

    LaunchedEffect(engine) {
        var previousFrame = 0L
        while (isActive) {
            val finished = withFrameNanos { frame ->
                if (previousFrame != 0L) {
                    engine.advance((frame - previousFrame) / 1_000_000_000.0)
                    snapshot = engine.snapshot()
                }
                previousFrame = frame
                snapshot.finished
            }
            if (finished) break
        }
    }

    LaunchedEffect(snapshot.triggeredCount, engine) {
        val delta = snapshot.triggeredCount - previousTriggeredCount
        if (delta > 0) {
            if (playerState.soundEnabled) {
                soundController.playChain(snapshot.maximumChainDepth, delta)
            }
            if (playerState.hapticsEnabled) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
        previousTriggeredCount = snapshot.triggeredCount
    }

    LaunchedEffect(snapshot.finished, engine) {
        if (snapshot.finished) {
            val score = calculateLevelScore(
                level = level,
                triggeredCount = snapshot.triggeredCount,
                maximumChainDepth = snapshot.maximumChainDepth,
                success = snapshot.success,
            )
            val stars = calculateLevelStars(level, snapshot.triggeredCount, snapshot.success)
            repository.recordResult(
                levelNumber = level.number,
                score = score,
                stars = stars,
                success = snapshot.success,
                triggeredCount = snapshot.triggeredCount,
                maximumChainDepth = snapshot.maximumChainDepth,
            )
            if (playerState.soundEnabled) soundController.playResult(snapshot.success)
            if (playerState.hapticsEnabled) view.performResultHaptic(snapshot.success)
        }
    }

    val selectLevel: (Int) -> Unit = { number ->
        selectedLevelNumber = number
        attemptId = 0
        showSettings = false
        showLevelPicker = false
        scope.launch { repository.selectLevel(number) }
    }

    GameScreen(
        level = level,
        progress = playerState.progress,
        snapshot = snapshot,
        previousBestScore = previousBestScore,
        reducedEffects = playerState.reducedEffects,
        showSettings = showSettings,
        showLevelPicker = showLevelPicker,
        soundEnabled = playerState.soundEnabled,
        hapticsEnabled = playerState.hapticsEnabled,
        onToggleSettings = {
            showLevelPicker = false
            showSettings = !showSettings
        },
        onToggleLevelPicker = {
            showSettings = false
            showLevelPicker = !showLevelPicker
        },
        onSoundChanged = { enabled ->
            scope.launch { repository.setSoundEnabled(enabled) }
        },
        onHapticsChanged = { enabled ->
            scope.launch { repository.setHapticsEnabled(enabled) }
        },
        onReducedEffectsChanged = { enabled ->
            scope.launch { repository.setReducedEffects(enabled) }
        },
        onSelectLevel = selectLevel,
        onTap = { position ->
            if (engine.tap(position)) {
                if (playerState.soundEnabled) soundController.playImpulse()
                if (playerState.hapticsEnabled) {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                snapshot = engine.snapshot()
                showSettings = false
                showLevelPicker = false
            }
        },
        onRetry = {
            showSettings = false
            showLevelPicker = false
            attemptId += 1
        },
        onNext = {
            if (level.number < LevelCatalog.levels.size) {
                selectLevel(level.number + 1)
            }
        },
    )
}

@Composable
private fun GameScreen(
    level: LevelDefinition,
    progress: ProgressState,
    snapshot: GameSnapshot,
    previousBestScore: Int,
    reducedEffects: Boolean,
    showSettings: Boolean,
    showLevelPicker: Boolean,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onToggleSettings: () -> Unit,
    onToggleLevelPicker: () -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onReducedEffectsChanged: (Boolean) -> Unit,
    onSelectLevel: (Int) -> Unit,
    onTap: (Vec2) -> Unit,
    onRetry: () -> Unit,
    onNext: () -> Unit,
) {
    val inputBlocked = showSettings || showLevelPicker

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
                .pointerInput(
                    snapshot.field,
                    snapshot.impulseUsed,
                    snapshot.finished,
                    inputBlocked,
                ) {
                    detectTapGestures { offset ->
                        if (inputBlocked || snapshot.impulseUsed || snapshot.finished) {
                            return@detectTapGestures
                        }
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
            drawGame(snapshot, reducedEffects)
        }

        TextButton(
            onClick = onToggleLevelPicker,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 5.dp, start = 6.dp)
                .testTag("level-button"),
        ) {
            Text(
                text = stringResource(R.string.level_short, level.number),
                color = ParticleCore.copy(alpha = 0.82f),
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = "${snapshot.triggeredCount} / ${snapshot.requiredCount}",
            color = Color.White.copy(alpha = 0.94f),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 17.dp)
                .testTag("score"),
        )

        TextButton(
            onClick = onToggleSettings,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 5.dp, end = 6.dp)
                .testTag("settings-button"),
        ) {
            Text(
                text = stringResource(R.string.settings_short),
                color = ParticleCore.copy(alpha = 0.82f),
                fontWeight = FontWeight.Bold,
            )
        }

        if (showSettings && !snapshot.finished) {
            SettingsPanel(
                soundEnabled = soundEnabled,
                hapticsEnabled = hapticsEnabled,
                reducedEffects = reducedEffects,
                onSoundChanged = onSoundChanged,
                onHapticsChanged = onHapticsChanged,
                onReducedEffectsChanged = onReducedEffectsChanged,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 58.dp, end = 12.dp),
            )
        }

        if (showLevelPicker && !snapshot.finished) {
            LevelPicker(
                selectedLevel = level.number,
                progress = progress,
                onSelectLevel = onSelectLevel,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 18.dp),
            )
        }

        if (!snapshot.impulseUsed && !inputBlocked) {
            GameHint(
                level = level,
                progress = progress,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 18.dp, vertical = 24.dp),
            )
        }

        if (snapshot.finished) {
            val score = calculateLevelScore(
                level = level,
                triggeredCount = snapshot.triggeredCount,
                maximumChainDepth = snapshot.maximumChainDepth,
                success = snapshot.success,
            )
            val stars = calculateLevelStars(level, snapshot.triggeredCount, snapshot.success)
            ResultPanel(
                level = level,
                snapshot = snapshot,
                score = score,
                stars = stars,
                previousBestScore = previousBestScore,
                hasNextLevel = snapshot.success && level.number < LevelCatalog.levels.size,
                onRetry = onRetry,
                onNext = onNext,
            )
        }
    }
}

private fun View.performResultHaptic(success: Boolean) {
    val feedback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (success) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT
    } else {
        HapticFeedbackConstants.LONG_PRESS
    }
    performHapticFeedback(feedback)
}
