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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
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
import com.sl.impulse.game.ReplayChallenge
import com.sl.impulse.game.ReplayChallenges
import com.sl.impulse.game.ReplayMode
import com.sl.impulse.game.Vec2
import com.sl.impulse.game.calculateReplayScore
import com.sl.impulse.game.calculateReplayStars
import com.sl.impulse.progress.PlayerState
import com.sl.impulse.progress.PlayerStateRepository
import java.time.LocalDate
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
internal fun ReplayGame(
    mode: ReplayMode,
    repository: PlayerStateRepository,
    playerState: PlayerState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dailyEpochDay = rememberSaveable(mode) { LocalDate.now().toEpochDay() }
    var runSeed by rememberSaveable(mode) {
        mutableStateOf(ReplayChallenges.newRunSeed(System.currentTimeMillis()))
    }
    var round by rememberSaveable(mode) { mutableIntStateOf(1) }
    var runScore by rememberSaveable(mode) { mutableIntStateOf(0) }
    var attemptId by remember(mode) { mutableIntStateOf(0) }
    var showSettings by rememberSaveable(mode) { mutableStateOf(false) }

    val challenge = remember(mode, dailyEpochDay, runSeed, round) {
        when (mode) {
            ReplayMode.ENDLESS -> ReplayChallenges.endless(runSeed, round)
            ReplayMode.DAILY -> ReplayChallenges.daily(LocalDate.ofEpochDay(dailyEpochDay))
        }
    }
    val level = challenge.level
    val engine = remember(challenge, attemptId) {
        GameEngine(
            seed = level.seed,
            particleCount = level.particleCount,
            requiredCount = level.requiredCount,
            particleMix = level.particleMix,
        )
    }
    val previousBestScore = remember(engine) {
        when (mode) {
            ReplayMode.ENDLESS -> playerState.replay.endlessBestScore
            ReplayMode.DAILY -> if (playerState.replay.dailyEpochDay == dailyEpochDay) {
                playerState.replay.dailyBestScore
            } else {
                0
            }
        }
    }
    val previousBestRound = remember(engine) { playerState.replay.endlessBestRound }
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
            if (playerState.soundEnabled) soundController.playChain(snapshot.maximumChainDepth, delta)
            if (playerState.hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
        previousTriggeredCount = snapshot.triggeredCount
    }

    LaunchedEffect(snapshot.finished, engine) {
        if (!snapshot.finished) return@LaunchedEffect
        val score = calculateReplayScore(
            challenge = challenge,
            triggeredCount = snapshot.triggeredCount,
            maximumChainDepth = snapshot.maximumChainDepth,
            success = snapshot.success,
        )
        when (mode) {
            ReplayMode.ENDLESS -> repository.recordEndlessResult(
                completedRounds = if (snapshot.success) round else round - 1,
                runScore = runScore + score,
                triggeredCount = snapshot.triggeredCount,
                maximumChainDepth = snapshot.maximumChainDepth,
                success = snapshot.success,
            )
            ReplayMode.DAILY -> repository.recordDailyResult(
                epochDay = dailyEpochDay,
                score = score,
                stars = calculateReplayStars(challenge, snapshot.triggeredCount, snapshot.success),
                success = snapshot.success,
                triggeredCount = snapshot.triggeredCount,
                maximumChainDepth = snapshot.maximumChainDepth,
            )
        }
        if (playerState.soundEnabled) soundController.playResult(snapshot.success)
        if (playerState.hapticsEnabled) view.performReplayResultHaptic(snapshot.success)
    }

    val attemptScore = if (snapshot.finished) {
        calculateReplayScore(challenge, snapshot.triggeredCount, snapshot.maximumChainDepth, snapshot.success)
    } else {
        0
    }

    ReplayGameScreen(
        challenge = challenge,
        snapshot = snapshot,
        attemptScore = attemptScore,
        runScore = runScore,
        previousBestScore = previousBestScore,
        previousBestRound = previousBestRound,
        dailyEpochDay = dailyEpochDay,
        reducedEffects = playerState.reducedEffects,
        showSettings = showSettings,
        soundEnabled = playerState.soundEnabled,
        hapticsEnabled = playerState.hapticsEnabled,
        onToggleSettings = { showSettings = !showSettings },
        onSoundChanged = { enabled -> scope.launch { repository.setSoundEnabled(enabled) } },
        onHapticsChanged = { enabled -> scope.launch { repository.setHapticsEnabled(enabled) } },
        onReducedEffectsChanged = { enabled -> scope.launch { repository.setReducedEffects(enabled) } },
        onTap = { position ->
            if (engine.tap(position)) {
                if (playerState.soundEnabled) soundController.playImpulse()
                if (playerState.hapticsEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                snapshot = engine.snapshot()
                showSettings = false
            }
        },
        onPrimary = {
            showSettings = false
            when (mode) {
                ReplayMode.DAILY -> attemptId += 1
                ReplayMode.ENDLESS -> if (snapshot.success) {
                    runScore += attemptScore
                    round += 1
                    attemptId = 0
                } else {
                    runSeed = ReplayChallenges.newRunSeed(System.currentTimeMillis() xor runSeed)
                    round = 1
                    runScore = 0
                    attemptId = 0
                }
            }
        },
    )
}

@Composable
private fun ReplayGameScreen(
    challenge: ReplayChallenge,
    snapshot: GameSnapshot,
    attemptScore: Int,
    runScore: Int,
    previousBestScore: Int,
    previousBestRound: Int,
    dailyEpochDay: Long,
    reducedEffects: Boolean,
    showSettings: Boolean,
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onToggleSettings: () -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onReducedEffectsChanged: (Boolean) -> Unit,
    onTap: (Vec2) -> Unit,
    onPrimary: () -> Unit,
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
                .pointerInput(snapshot.field, snapshot.impulseUsed, snapshot.finished, showSettings) {
                    detectTapGestures { offset ->
                        if (showSettings || snapshot.impulseUsed || snapshot.finished) return@detectTapGestures
                        val viewport = calculateGameViewport(size.width.toFloat(), size.height.toFloat(), snapshot.field)
                        viewport.toGame(offset.x, offset.y, snapshot.field)?.let(onTap)
                    }
                },
        ) {
            drawGame(snapshot, reducedEffects)
        }

        Text(
            text = when (challenge.mode) {
                ReplayMode.ENDLESS -> stringResource(R.string.replay_endless_header, challenge.stage)
                ReplayMode.DAILY -> stringResource(R.string.replay_daily_title)
            },
            color = ParticleCore.copy(alpha = 0.82f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 17.dp, start = 14.dp)
                .testTag("replay-mode"),
        )

        Text(
            text = "${snapshot.triggeredCount} / ${snapshot.requiredCount}",
            color = Color.White.copy(alpha = 0.94f),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 17.dp),
        )

        TextButton(
            onClick = onToggleSettings,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 5.dp, end = 6.dp).testTag("settings-button"),
        ) {
            Text(stringResource(R.string.settings_short), color = ParticleCore.copy(alpha = 0.82f), fontWeight = FontWeight.Bold)
        }

        if (showSettings && !snapshot.finished) {
            SettingsPanel(
                soundEnabled = soundEnabled,
                hapticsEnabled = hapticsEnabled,
                reducedEffects = reducedEffects,
                onSoundChanged = onSoundChanged,
                onHapticsChanged = onHapticsChanged,
                onReducedEffectsChanged = onReducedEffectsChanged,
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 58.dp, end = 12.dp),
            )
        }

        if (!snapshot.impulseUsed && !showSettings) {
            Text(
                text = stringResource(
                    if (challenge.mode == ReplayMode.ENDLESS) R.string.replay_endless_hint else R.string.replay_daily_hint,
                ),
                color = Color.White.copy(alpha = 0.62f),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 22.dp, vertical = 24.dp).testTag("game-hint"),
            )
        }

        if (snapshot.finished) {
            ReplayResultPanel(
                challenge = challenge,
                snapshot = snapshot,
                attemptScore = attemptScore,
                runScore = runScore + attemptScore,
                previousBestScore = previousBestScore,
                previousBestRound = previousBestRound,
                dailyEpochDay = dailyEpochDay,
                onPrimary = onPrimary,
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.ReplayResultPanel(
    challenge: ReplayChallenge,
    snapshot: GameSnapshot,
    attemptScore: Int,
    runScore: Int,
    previousBestScore: Int,
    previousBestRound: Int,
    dailyEpochDay: Long,
    onPrimary: () -> Unit,
) {
    val context = LocalContext.current
    val stars = calculateReplayStars(challenge, snapshot.triggeredCount, snapshot.success)
    val isNewBest = when (challenge.mode) {
        ReplayMode.DAILY -> attemptScore > previousBestScore
        ReplayMode.ENDLESS -> runScore > previousBestScore || (snapshot.success && challenge.stage > previousBestRound)
    }
    val dateLabel = LocalDate.ofEpochDay(dailyEpochDay).toString()
    val shareText = when (challenge.mode) {
        ReplayMode.DAILY -> stringResource(
            R.string.share_daily_text,
            dateLabel,
            snapshot.triggeredCount,
            challenge.level.particleCount,
            snapshot.maximumChainDepth,
            attemptScore,
            starRating(stars),
        )
        ReplayMode.ENDLESS -> stringResource(
            R.string.share_endless_text,
            challenge.stage,
            snapshot.triggeredCount,
            challenge.level.particleCount,
            snapshot.maximumChainDepth,
            runScore,
        )
    }

    Surface(
        color = PanelBackground.copy(alpha = 0.97f),
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier.align(Alignment.Center).padding(horizontal = 26.dp).widthIn(max = 360.dp).testTag("replay-result"),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 26.dp, vertical = 24.dp),
        ) {
            Text(
                text = when {
                    challenge.mode == ReplayMode.ENDLESS && snapshot.success -> stringResource(R.string.replay_round_cleared)
                    challenge.mode == ReplayMode.ENDLESS -> stringResource(R.string.replay_run_ended)
                    snapshot.success -> stringResource(R.string.result_success_title)
                    else -> stringResource(R.string.result_failure_title)
                },
                color = if (snapshot.success) TriggeredCore else ParticleCore,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp,
            )
            Text(
                text = stringResource(R.string.result_chain, snapshot.triggeredCount, challenge.level.particleCount),
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("result-chain"),
            )
            Text(stringResource(R.string.result_target, snapshot.requiredCount), color = Color.White.copy(alpha = 0.52f), fontSize = 12.sp)
            if (challenge.mode == ReplayMode.DAILY && snapshot.success) {
                Text(starRating(stars), color = TriggeredCore, fontSize = 22.sp, letterSpacing = 3.sp)
            }
            Text(
                text = if (challenge.mode == ReplayMode.ENDLESS) {
                    stringResource(R.string.replay_run_score, runScore)
                } else {
                    stringResource(R.string.score_result, attemptScore)
                },
                color = ParticleCore.copy(alpha = 0.86f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("result-score"),
            )
            Text(stringResource(R.string.chain_depth, snapshot.maximumChainDepth), color = TriggeredCore.copy(alpha = 0.72f), fontSize = 13.sp)
            if (isNewBest) {
                Text(stringResource(R.string.result_new_best), color = NearMiss, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { shareResult(context, shareText) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15203A), contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().testTag("share-result"),
            ) {
                Text(stringResource(R.string.share_result), fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onPrimary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (snapshot.success) TriggeredCore else ParticleCore,
                    contentColor = Background,
                ),
                modifier = Modifier.fillMaxWidth().testTag("replay-primary"),
            ) {
                Text(
                    text = when (challenge.mode) {
                        ReplayMode.DAILY -> stringResource(R.string.retry)
                        ReplayMode.ENDLESS -> stringResource(
                            if (snapshot.success) R.string.replay_next_round else R.string.replay_new_run,
                        )
                    },
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun View.performReplayResultHaptic(success: Boolean) {
    val feedback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (success) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.REJECT
    } else {
        HapticFeedbackConstants.LONG_PRESS
    }
    performHapticFeedback(feedback)
}
