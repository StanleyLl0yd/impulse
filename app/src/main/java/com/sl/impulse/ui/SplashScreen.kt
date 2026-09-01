package com.sl.impulse.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.sl.impulse.R
import com.sl.impulse.progress.PlayerState
import com.sl.impulse.progress.PlayerStateRepository
import kotlinx.coroutines.launch

private const val SPLASH_FADE_DURATION_MILLIS = 5_000

private enum class RootScreen {
    Splash,
    Menu,
    Achievements,
    About,
    Game,
}

@Composable
fun ImpulseRoot(onExit: () -> Unit = {}) {
    val context = LocalContext.current
    val repository = remember(context.applicationContext) {
        PlayerStateRepository(context.applicationContext)
    }
    val playerState by repository.state.collectAsState(initial = PlayerState())
    val scope = rememberCoroutineScope()
    var screenName by rememberSaveable { mutableStateOf(RootScreen.Splash.name) }
    val screen = RootScreen.valueOf(screenName)
    val showMenu = { screenName = RootScreen.Menu.name }
    val startGame: (Int) -> Unit = { levelNumber ->
        scope.launch {
            repository.selectLevel(levelNumber)
            screenName = RootScreen.Game.name
        }
    }

    BackHandler(enabled = screen != RootScreen.Splash) {
        if (screen == RootScreen.Menu) onExit() else showMenu()
    }

    ImpulseTheme {
        when (screen) {
            RootScreen.Splash -> ImpulseSplash(onFinished = showMenu)

            RootScreen.Menu -> MainMenuScreen(
                progress = playerState.progress,
                onContinue = { startGame(playerState.progress.highestUnlockedLevel) },
                onNewGame = { startGame(1) },
                onAchievements = { screenName = RootScreen.Achievements.name },
                onAbout = { screenName = RootScreen.About.name },
                onExit = onExit,
            )

            RootScreen.Achievements -> AchievementsScreen(
                progress = playerState.progress,
                onBack = showMenu,
            )

            RootScreen.About -> AboutScreen(onBack = showMenu)

            RootScreen.Game -> ImpulseGame(
                repository = repository,
                playerState = playerState,
            )
        }
    }
}

@Composable
private fun ImpulseSplash(onFinished: () -> Unit) {
    val imageAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        imageAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = SPLASH_FADE_DURATION_MILLIS,
                easing = LinearEasing,
            ),
        )
        withFrameNanos { }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("splash-screen"),
    ) {
        Image(
            painter = painterResource(R.drawable.impulse_splash),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .alpha(imageAlpha.value)
                .testTag("splash-image"),
        )
    }
}
