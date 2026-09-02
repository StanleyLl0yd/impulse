package com.sl.impulse.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sl.impulse.R
import com.sl.impulse.game.GameSnapshot
import com.sl.impulse.game.LevelCatalog
import com.sl.impulse.game.LevelDefinition
import com.sl.impulse.progress.ProgressState

private val LevelRows = LevelCatalog.levels.chunked(4)

@Composable
internal fun SettingsPanel(
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
internal fun LevelPicker(
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
internal fun GameHint(
    level: LevelDefinition,
    progress: ProgressState,
    modifier: Modifier = Modifier,
) {
    val tutorialText = when {
        level.number == 1 && progress.highestUnlockedLevel == 1 -> R.string.tutorial_level_1
        level.number == 2 && progress.highestUnlockedLevel == 2 -> R.string.tutorial_level_2
        level.number == 3 && progress.highestUnlockedLevel == 3 -> R.string.tutorial_level_3
        else -> null
    }
    val mechanicText = when {
        level.number == 21 && progress.highestUnlockedLevel == 21 -> R.string.mechanic_booster
        level.number == 26 && progress.highestUnlockedLevel == 26 -> R.string.mechanic_fuse
        level.number == 31 && progress.highestUnlockedLevel == 31 -> R.string.mechanic_anchor
        level.number == 36 && progress.highestUnlockedLevel == 36 -> R.string.mechanic_mix
        else -> null
    }
    val hintText = tutorialText ?: mechanicText

    if (hintText == null) {
        Text(
            text = stringResource(R.string.game_hint),
            color = Color.White.copy(alpha = 0.62f),
            fontSize = 14.sp,
            modifier = modifier.testTag("game-hint"),
        )
    } else {
        Surface(
            color = PanelBackground.copy(alpha = 0.9f),
            shape = RoundedCornerShape(18.dp),
            modifier = modifier
                .widthIn(max = 340.dp)
                .testTag("game-hint"),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Text(
                    text = stringResource(
                        if (tutorialText != null) R.string.tutorial_title else R.string.mechanic_title,
                    ),
                    color = if (tutorialText != null) ParticleGlow else TriggeredGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                )
                Text(
                    text = stringResource(hintText),
                    color = Color.White.copy(alpha = 0.84f),
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
internal fun BoxScope.ResultPanel(
    level: LevelDefinition,
    snapshot: GameSnapshot,
    score: Int,
    stars: Int,
    previousBestScore: Int,
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
        snapshot.success -> stringResource(R.string.result_success_detail, snapshot.maximumChainDepth)
        nearMiss -> stringResource(R.string.result_near_miss_detail, missing)
        else -> stringResource(R.string.result_failure_detail, missing)
    }
    val accent = when {
        snapshot.success -> TriggeredCore
        nearMiss -> NearMiss
        else -> ParticleCore
    }
    val isNewBest = score > previousBestScore
    val improvement = (score - previousBestScore).coerceAtLeast(0)

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
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
                text = stringResource(
                    R.string.result_chain,
                    snapshot.triggeredCount,
                    level.particleCount,
                ),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("result-chain"),
            )
            Text(
                text = stringResource(R.string.result_target, snapshot.requiredCount),
                color = Color.White.copy(alpha = 0.52f),
                fontSize = 12.sp,
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
                color = ParticleCore.copy(alpha = 0.86f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("result-score"),
            )
            Text(
                text = stringResource(R.string.chain_depth, snapshot.maximumChainDepth),
                color = TriggeredCore.copy(alpha = 0.72f),
                fontSize = 13.sp,
                modifier = Modifier.testTag("result-depth"),
            )
            if (isNewBest) {
                Text(
                    text = if (previousBestScore > 0) {
                        stringResource(R.string.result_new_best_delta, improvement)
                    } else {
                        stringResource(R.string.result_new_best)
                    },
                    color = NearMiss,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.testTag("result-new-best"),
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

internal fun starRating(stars: Int): String {
    val safeStars = stars.coerceIn(0, 3)
    return "★".repeat(safeStars) + "☆".repeat(3 - safeStars)
}
