package com.sl.impulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sl.impulse.R
import com.sl.impulse.game.LevelCatalog
import com.sl.impulse.progress.AchievementCatalog
import com.sl.impulse.progress.AchievementGroup
import com.sl.impulse.progress.PlayerState
import com.sl.impulse.progress.ProgressState

@Composable
fun MainMenuScreen(
    progress: ProgressState,
    onContinue: () -> Unit,
    onNewGame: () -> Unit,
    onAchievements: () -> Unit,
    onAbout: () -> Unit,
    onExit: () -> Unit,
) {
    MenuBackground {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name),
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
            )
            Text(
                text = stringResource(R.string.menu_tagline),
                color = ParticleCore.copy(alpha = 0.62f),
                fontSize = 12.sp,
                letterSpacing = 1.4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(modifier = Modifier.height(42.dp))
            MenuButton(
                text = stringResource(R.string.menu_continue),
                onClick = onContinue,
                testTag = "menu-continue",
                primary = true,
            )
            MenuButton(
                text = stringResource(R.string.menu_new_game),
                onClick = onNewGame,
                testTag = "menu-new-game",
            )
            MenuButton(
                text = stringResource(R.string.menu_achievements),
                onClick = onAchievements,
                testTag = "menu-achievements",
            )
            Text(
                text = stringResource(
                    R.string.menu_progress_summary,
                    progress.highestUnlockedLevel,
                    LevelCatalog.levels.size,
                ),
                color = ParticleCore.copy(alpha = 0.52f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
            )
            MenuButton(
                text = stringResource(R.string.menu_about),
                onClick = onAbout,
                testTag = "menu-about",
            )
            TextButton(
                onClick = onExit,
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .fillMaxWidth()
                    .testTag("menu-exit"),
            ) {
                Text(
                    text = stringResource(R.string.menu_exit),
                    color = Color.White.copy(alpha = 0.58f),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
fun AchievementsScreen(
    playerState: PlayerState,
    onBack: () -> Unit,
) {
    val progress = playerState.progress
    val statistics = playerState.statistics
    val totalLevels = LevelCatalog.levels.size
    val achievements = AchievementCatalog.evaluate(playerState)

    MenuBackground {
        MenuPanel(
            title = stringResource(R.string.achievements_title),
            testTag = "achievements-screen",
            onBack = onBack,
        ) {
            SectionTitle(stringResource(R.string.achievements_progress_section))
            Text(
                text = stringResource(
                    R.string.achievements_level,
                    progress.completedLevels,
                    totalLevels,
                ),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("achievements-level"),
            )
            Text(
                text = stringResource(
                    R.string.achievements_stars,
                    progress.totalStars,
                    totalLevels * 3,
                ),
                color = ParticleCore.copy(alpha = 0.82f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("achievements-stars"),
            )
            Text(
                text = stringResource(
                    R.string.achievements_perfect_levels,
                    progress.perfectLevels,
                    totalLevels,
                ),
                color = TriggeredCore.copy(alpha = 0.76f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(
                    R.string.achievements_unlocked,
                    achievements.count { it.unlocked },
                    achievements.size,
                ),
                color = NearMiss.copy(alpha = 0.86f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("achievements-unlocked"),
            )

            SectionTitle(stringResource(R.string.achievements_chapters_section))
            LevelCatalog.chapters.forEach { chapter ->
                StatRow(
                    label = stringResource(chapterNameRes(chapter.id)),
                    value = stringResource(
                        R.string.chapter_progress,
                        progress.completedLevels(chapter.levels),
                        chapter.levels.count(),
                        progress.perfectLevels(chapter.levels),
                    ),
                    testTag = "chapter-progress-${chapter.number}",
                )
            }

            SectionTitle(stringResource(R.string.achievements_statistics_section))
            StatRow(
                label = stringResource(R.string.statistics_attempts),
                value = statistics.totalAttempts.toString(),
                testTag = "statistics-attempts",
            )
            StatRow(
                label = stringResource(R.string.statistics_successes),
                value = statistics.successfulAttempts.toString(),
            )
            StatRow(
                label = stringResource(R.string.statistics_success_rate),
                value = "${statistics.successRatePercent}%",
            )
            StatRow(
                label = stringResource(R.string.statistics_total_triggered),
                value = statistics.totalTriggeredParticles.toString(),
            )
            StatRow(
                label = stringResource(R.string.statistics_best_chain),
                value = statistics.bestTriggeredCount.toString(),
                testTag = "statistics-best-chain",
            )
            StatRow(
                label = stringResource(R.string.statistics_best_depth),
                value = statistics.bestChainDepth.toString(),
            )
            StatRow(
                label = stringResource(R.string.statistics_best_score),
                value = progress.bestOverallScore.toString(),
            )

            AchievementGroup.entries.forEach { group ->
                SectionTitle(stringResource(achievementGroupTitleRes(group)))
                achievements.filter { it.group == group }.forEach { achievement ->
                    AchievementRow(
                        title = stringResource(achievementTitleRes(achievement.id)),
                        unlocked = achievement.unlocked,
                    )
                }
            }
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    MenuBackground {
        MenuPanel(
            title = stringResource(R.string.about_title),
            testTag = "about-screen",
            onBack = onBack,
        ) {
            Text(
                text = stringResource(R.string.about_description),
                color = Color.White.copy(alpha = 0.84f),
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.about_copyright),
                color = ParticleCore.copy(alpha = 0.56f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = TriggeredGlow,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    testTag: String? = null,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (testTag == null) Modifier else Modifier.testTag(testTag)),
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.68f),
            fontSize = 13.sp,
        )
        Text(
            text = value,
            color = ParticleCore,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun AchievementRow(
    title: String,
    unlocked: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = if (unlocked) "✓" else "○",
            color = if (unlocked) TriggeredCore else Color.White.copy(alpha = 0.24f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = title,
            color = if (unlocked) Color.White.copy(alpha = 0.86f) else Color.White.copy(alpha = 0.38f),
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit,
    testTag: String,
    primary: Boolean = false,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) ParticleGlow else Color(0xFF15203A),
            contentColor = if (primary) Background else Color.White,
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .padding(vertical = 5.dp)
            .widthIn(max = 320.dp)
            .fillMaxWidth()
            .testTag(testTag),
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(vertical = 5.dp),
        )
    }
}

@Composable
private fun MenuBackground(content: @Composable () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0D1731), Background),
                    radius = 1_100f,
                ),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("main-menu-background"),
    ) {
        content()
    }
}

@Composable
private fun MenuPanel(
    title: String,
    testTag: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        color = PanelBackground.copy(alpha = 0.97f),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 18.dp)
            .widthIn(max = 380.dp)
            .testTag(testTag),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .padding(horizontal = 26.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = title,
                color = TriggeredGlow,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
            content()
            TextButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("menu-back"),
            ) {
                Text(
                    text = stringResource(R.string.back),
                    color = ParticleGlow,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
