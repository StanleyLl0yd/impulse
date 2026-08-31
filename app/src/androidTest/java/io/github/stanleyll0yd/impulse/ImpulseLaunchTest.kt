package io.github.stanleyll0yd.impulse

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

class ImpulseLaunchTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun gameCanvasIsDisplayed() {
        composeRule.onNodeWithTag("game-canvas").assertExists()
    }
}
