package com.sl.impulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
    progress: ProgressState,
    onBack: () -> Unit,
) {
    MenuBackground {
        MenuPanel(
            title = stringResource(R.string.achievements_title),
            testTag = "achievements-screen",
            onBack = onBack,
        ) {
            Text(
                text = stringResource(
                    R.string.achievements_level,
                    progress.highestUnlockedLevel,
                    LevelCatalog.levels.size,
                ),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("achievements-level"),
            )
            Text(
                text = stringResource(
                    R.string.achievements_stars,
                    progress.totalStars,
                    LevelCatalog.levels.size * 3,
                ),
                color = ParticleCore.copy(alpha = 0.82f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("achievements-stars"),
            )
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
            .padding(horizontal = 24.dp)
            .widthIn(max = 380.dp)
            .testTag(testTag),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.padding(horizontal = 26.dp, vertical = 26.dp),
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
