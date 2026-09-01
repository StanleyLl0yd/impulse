package com.sl.impulse.ui

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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

private val LevelRows = LevelCatalog.levels.chunked(4)

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
        )
    }
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
            repository.recordResult(level.number, score, stars, snapshot.success)
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
            val score = calculateLevelScore(
                level = level,
                triggeredCount = snapshot.triggeredCount,
                maximumChainDepth = snapshot.maximumChainDepth,
                success = snapshot.success,
            )
            val stars = calculateLevelStars(level, snapshot.triggeredCount, snapshot.success)
            ResultPanel(
                snapshot = snapshot,
                score = score,
                stars = stars,
                hasNextLevel = snapshot.success && level.number < LevelCatalog.levels.size,
                onRetry = onRetry,
                onNext = onNext,
            )
        }
    }
}

@Composable
private fun SettingsPanel(
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    reducedEffects: Boolean,
    onSoundChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onReducedEffectsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = PanelBackground.copy(alpha = 0.96f),
        shape = RoundedCornerShape(22.dp),
        modifier = modifier
            .widthIn(min = 230.dp, max = 280.dp)
            .testTag("settings-panel"),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            SettingRow(
                label = stringResource(R.string.settings_sound),
                checked = soundEnabled,
                onCheckedChange = onSoundChanged,
                testTag = "sound-toggle",
            )
            SettingRow(
                label = stringResource(R.string.settings_haptics),
                checked = hapticsEnabled,
                onCheckedChange = onHapticsChanged,
                testTag = "haptics-toggle",
            )
            SettingRow(
                label = stringResource(R.string.settings_reduced_effects),
                checked = reducedEffects,
                onCheckedChange = onReducedEffectsChanged,
                testTag = "reduced-effects-toggle",
            )
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.86f),
            fontSize = 14.sp,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
        )
    }
}

@Composable
private fun LevelPicker(
    selectedLevel: Int,
    progress: ProgressState,
    onSelectLevel: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = PanelBackground.copy(alpha = 0.98f),
        shape = RoundedCornerShape(26.dp),
        modifier = modifier
            .widthIn(max = 380.dp)
            .testTag("level-picker"),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 18.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.level_picker_title),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
            )
            Text(
                text = stringResource(
                    R.string.total_stars,
                    progress.totalStars,
                    LevelCatalog.levels.size * 3,
                ),
                color = TriggeredCore.copy(alpha = 0.82f),
                fontSize = 13.sp,
            )

            LevelRows.forEach { rowLevels ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    rowLevels.forEach { level ->
                        val unlocked = progress.isUnlocked(level.number)
                        val selected = level.number == selectedLevel
                        val stars = progress.stars(level.number)
                        Button(
                            onClick = { onSelectLevel(level.number) },
                            enabled = unlocked,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) TriggeredGlow else Color(0xFF15203A),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF090D19),
                                disabledContentColor = Color.White.copy(alpha = 0.24f),
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("level-${level.number}"),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = level.number.toString().padStart(2, '0'),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                )
                                if (unlocked) {
                                    Text(
                                        text = starRating(stars),
                                        color = if (stars > 0) TriggeredCore else Color.White.copy(alpha = 0.28f),
                                        fontSize = 10.sp,
                                    )
                                }
                            }
                        }
                    }
                    repeat(4 - rowLevels.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            val bestScore = progress.bestScore(selectedLevel)
            if (bestScore > 0) {
                Text(
                    text = stringResource(R.string.best_score, bestScore),
                    color = ParticleCore.copy(alpha = 0.68f),
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.ResultPanel(
    snapshot: GameSnapshot,
    score: Int,
    stars: Int,
    hasNextLevel: Boolean,
    onRetry: () -> Unit,
    onNext: () -> Unit,
) {
    val missing = (snapshot.requiredCount - snapshot.triggeredCount).coerceAtLeast(0)
    val nearMiss = !snapshot.success && missing <= 2
    val title = when {
        snapshot.success -> stringResource(R.string.result_success_title)
        nearMiss -> stringResource(R.string.result_near_miss_title)
        else -> stringResource(R.string.result_failure_title)
    }
    val detail = when {
        snapshot.success -> stringResource(
            R.string.result_success_detail,
            snapshot.maximumChainDepth,
        )
        nearMiss -> stringResource(R.string.result_near_miss_detail, missing)
        else -> stringResource(R.string.result_failure_detail, missing)
    }
    val accent = when {
        snapshot.success -> TriggeredCore
        nearMiss -> NearMiss
        else -> ParticleCore
    }

    Surface(
        color = PanelBackground.copy(alpha = 0.97f),
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier
            .align(Alignment.Center)
            .padding(horizontal = 26.dp)
            .widthIn(max = 360.dp)
            .testTag("result"),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(11.dp),
            modifier = Modifier.padding(horizontal = 26.dp, vertical = 24.dp),
        ) {
            Text(
                text = title,
                color = accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp,
            )
            Text(
                text = "${snapshot.triggeredCount} / ${snapshot.requiredCount}",
                color = Color.White,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = detail,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 14.sp,
            )
            if (snapshot.success) {
                Text(
                    text = starRating(stars),
                    color = TriggeredCore,
                    fontSize = 22.sp,
                    letterSpacing = 3.sp,
                    modifier = Modifier.testTag("result-stars"),
                )
            }
            Text(
                text = stringResource(R.string.score_result, score),
                color = ParticleCore.copy(alpha = 0.72f),
                fontSize = 13.sp,
                modifier = Modifier.testTag("result-score"),
            )
            if (snapshot.triggeredCount > 0) {
                Text(
                    text = stringResource(
                        R.string.chain_depth,
                        snapshot.maximumChainDepth,
                    ),
                    color = TriggeredCore.copy(alpha = 0.72f),
                    fontSize = 13.sp,
                )
            }
            if (hasNextLevel) {
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Background,
                    ),
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .fillMaxWidth()
                        .testTag("next-level"),
                ) {
                    Text(
                        text = stringResource(R.string.next_level),
                        fontWeight = FontWeight.Bold,
                    )
                }
                TextButton(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("retry"),
                ) {
                    Text(
                        text = stringResource(R.string.retry),
                        color = ParticleCore,
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = Background,
                    ),
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .fillMaxWidth()
                        .testTag("retry"),
                ) {
                    Text(
                        text = stringResource(R.string.retry),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

private fun starRating(stars: Int): String {
    val safeStars = stars.coerceIn(0, 3)
    return "★".repeat(safeStars) + "☆".repeat(3 - safeStars)
}

private fun View.performResultHaptic(success: Boolean) {
    val feedback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (success) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT
    } else {
        HapticFeedbackConstants.LONG_PRESS
    }
    performHapticFeedback(feedback)
}
