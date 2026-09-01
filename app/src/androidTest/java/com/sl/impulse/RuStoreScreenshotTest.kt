package com.sl.impulse

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import org.junit.Rule
import org.junit.Test

class RuStoreScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun captureRuStoreScreens() {
        clearOutput()
        finishSplash()
        capture("01-main-menu")

        composeRule.onNodeWithTag("menu-new-game").performClick()
        advanceUntilExists("game-canvas")
        composeRule.mainClock.advanceTimeBy(300)
        capture("02-gameplay")

        composeRule.onNodeWithTag("game-canvas").performTouchInput { click() }

        composeRule.mainClock.advanceTimeBy(600)
        capture("03-reaction-0600")
        composeRule.mainClock.advanceTimeBy(500)
        capture("04-reaction-1100")
        composeRule.mainClock.advanceTimeBy(700)
        capture("05-reaction-1800")
        composeRule.mainClock.advanceTimeBy(800)
        capture("06-reaction-2600")
        composeRule.mainClock.advanceTimeBy(9_600)

        composeRule.onNodeWithTag("result").assertExists()
        capture("07-result")

        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        advanceUntilExists("menu-achievements")
        capture("08-main-menu-progress")

        composeRule.onNodeWithTag("menu-achievements").performClick()
        advanceUntilExists("achievements-screen")
        capture("09-achievements")
    }

    private fun capture(name: String) {
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val directory = File(
            InstrumentationRegistry.getInstrumentation().targetContext.filesDir,
            "rustore",
        ).apply { mkdirs() }
        FileOutputStream(File(directory, "$name.png")).use {
            check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
        }
        bitmap.recycle()
    }

    private fun clearOutput() {
        val directory = File(
            InstrumentationRegistry.getInstrumentation().targetContext.filesDir,
            "rustore",
        )
        directory.deleteRecursively()
    }

    private fun finishSplash() {
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(5_100)
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.mainClock.advanceTimeByFrame()
    }

    private fun advanceUntilExists(tag: String) {
        repeat(120) {
            composeRule.mainClock.advanceTimeByFrame()
            if (runCatching {
                    composeRule.onNodeWithTag(tag).assertExists()
                }.isSuccess
            ) {
                return
            }
            Thread.sleep(10)
        }
        composeRule.onNodeWithTag(tag).assertExists()
    }
}
