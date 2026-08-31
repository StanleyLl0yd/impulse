package io.github.stanleyll0yd.impulse

import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Rule
import org.junit.Test

class ImpulseLaunchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun gameCanvasIsDisplayed() {
        composeRule.mainClock.autoAdvance = false
        composeRule.onNodeWithTag("game-canvas").assertExists()
        composeRule.onNodeWithTag("game-hint").assertExists()
        composeRule.onNodeWithTag("settings-button").assertExists()
    }

    @Test
    fun gameFeelSettingsAreAvailable() {
        composeRule.mainClock.autoAdvance = false

        composeRule.onNodeWithTag("settings-button").performClick()
        composeRule.onNodeWithTag("settings-panel").assertExists()
        composeRule.onNodeWithTag("sound-toggle").assertExists()
        composeRule.onNodeWithTag("haptics-toggle").assertExists()
        composeRule.onNodeWithTag("reduced-effects-toggle").assertExists()
    }

    @Test
    fun tapResultAndRetryFlowWorks() {
        composeRule.mainClock.autoAdvance = false

        composeRule.onNodeWithTag("game-canvas").performTouchInput { click() }
        composeRule.mainClock.advanceTimeBy(100)
        composeRule.onNodeWithTag("game-hint").assertDoesNotExist()

        composeRule.mainClock.advanceTimeBy(12_000)
        composeRule.onNodeWithTag("result").assertExists()

        composeRule.onNodeWithTag("retry").performClick()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.onNodeWithTag("game-hint").assertExists()
    }
}
