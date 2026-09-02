package com.sl.impulse

import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBack
import org.junit.Rule
import org.junit.Test

class ImpulseLaunchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainMenuIsDisplayedAfterSplash() {
        finishSplash()
        composeRule.onNodeWithTag("menu-continue").assertExists()
        composeRule.onNodeWithTag("menu-new-game").assertExists()
        composeRule.onNodeWithTag("menu-endless").assertExists()
        composeRule.onNodeWithTag("menu-daily").assertExists()
        composeRule.onNodeWithTag("menu-achievements").assertExists()
        composeRule.onNodeWithTag("menu-about").assertExists()
        composeRule.onNodeWithTag("menu-exit").assertExists()
    }

    @Test
    fun replayModesOpenDeterministicGameSurface() {
        finishSplash()
        composeRule.onNodeWithTag("menu-endless").performClick()
        advanceUntilExists("game-canvas")
        composeRule.onNodeWithTag("replay-mode").assertExists()
        pressBack()
        advanceUntilExists("menu-daily")
        composeRule.onNodeWithTag("menu-daily").performClick()
        advanceUntilExists("game-canvas")
        composeRule.onNodeWithTag("replay-mode").assertExists()
    }

    @Test
    fun achievementsAndAboutAreAvailable() {
        finishSplash()
        composeRule.onNodeWithTag("menu-achievements").performClick()
        advanceUntilExists("achievements-screen")
        composeRule.onNodeWithTag("achievements-level").assertExists()
        composeRule.onNodeWithTag("achievements-stars").assertExists()
        composeRule.onNodeWithTag("achievements-unlocked").assertExists()
        composeRule.onNodeWithTag("chapter-progress-1").assertExists()
        composeRule.onNodeWithTag("statistics-endless-round").assertExists()
        composeRule.onNodeWithTag("statistics-daily-days").assertExists()
        composeRule.onNodeWithTag("statistics-attempts").assertExists()
        composeRule.onNodeWithTag("statistics-best-chain").assertExists()
        pressBack()
        advanceUntilExists("menu-about")
        composeRule.onNodeWithTag("menu-about").performClick()
        advanceUntilExists("about-screen")
    }

    @Test
    fun gameCanvasIsDisplayed() {
        enterNewGame()
        composeRule.onNodeWithTag("game-canvas").assertExists()
        composeRule.onNodeWithTag("game-hint").assertExists()
        composeRule.onNodeWithTag("level-button").assertExists()
        composeRule.onNodeWithTag("settings-button").assertExists()
    }

    @Test
    fun levelPickerShowsSixChapterCampaign() {
        enterNewGame()
        composeRule.onNodeWithTag("level-button").performClick()
        advanceUntilExists("level-picker")
        composeRule.onNodeWithTag("chapter-1").assertExists()
        composeRule.onNodeWithTag("chapter-6").assertExists()
        composeRule.onNodeWithTag("level-1").assertExists()
        composeRule.onNodeWithTag("level-60").assertExists()
    }

    @Test
    fun gameFeelSettingsAreAvailable() {
        enterNewGame()
        composeRule.onNodeWithTag("settings-button").performClick()
        advanceUntilExists("settings-panel")
        composeRule.onNodeWithTag("sound-toggle").assertExists()
        composeRule.onNodeWithTag("music-toggle").assertExists()
        composeRule.onNodeWithTag("high-contrast-toggle").assertExists()
        composeRule.onNodeWithTag("haptics-toggle").assertExists()
        composeRule.onNodeWithTag("reduced-effects-toggle").assertExists()
    }

    @Test
    fun highContrastCanBeChangedWithoutLeavingGameplay() {
        enterNewGame()
        composeRule.onNodeWithTag("settings-button").performClick()
        advanceUntilExists("high-contrast-toggle")
        composeRule.onNodeWithTag("high-contrast-toggle").performClick()
        composeRule.onNodeWithTag("game-canvas").assertExists()
        composeRule.onNodeWithTag("settings-panel").assertExists()
    }

    @Test
    fun tapResultAndRetryFlowWorks() {
        enterNewGame()
        composeRule.onNodeWithTag("game-canvas").performTouchInput { click() }
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.onNodeWithTag("game-hint").assertDoesNotExist()
        composeRule.mainClock.advanceTimeBy(12_000)
        composeRule.onNodeWithTag("result").assertExists()
        composeRule.onNodeWithTag("result-chain").assertExists()
        composeRule.onNodeWithTag("result-score").assertExists()
        composeRule.onNodeWithTag("result-depth").assertExists()
        composeRule.onNodeWithTag("retry").performClick()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.onNodeWithTag("game-hint").assertExists()
    }

    private fun enterNewGame() {
        finishSplash()
        composeRule.onNodeWithTag("menu-new-game").performClick()
        advanceUntilExists("game-canvas")
    }

    private fun finishSplash() {
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(5_100)
        composeRule.mainClock.advanceTimeByFrame()
    }

    private fun advanceUntilExists(tag: String) {
        repeat(120) {
            composeRule.mainClock.advanceTimeByFrame()
            if (runCatching { composeRule.onNodeWithTag(tag).assertExists() }.isSuccess) return
            Thread.sleep(10)
        }
        composeRule.onNodeWithTag(tag).assertExists()
    }
}
