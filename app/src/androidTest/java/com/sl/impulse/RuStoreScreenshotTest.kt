package com.sl.impulse

import android.app.LocaleManager
import android.graphics.Bitmap
import android.os.LocaleList
import androidx.compose.ui.geometry.Offset
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
import kotlin.math.min
import org.junit.Rule
import org.junit.Test

class RuStoreScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun captureRuStoreScreens() {
        clearOutput()
        useRussianLocale()
        finishSplash()
        capture("01-main-menu")

        composeRule.onNodeWithTag("menu-new-game").performClick()
        advanceUntilExists("game-canvas")
        composeRule.mainClock.advanceTimeBy(300)
        capture("02-gameplay")

        tapGameCoordinate(0.18f, 0.84f)

        composeRule.mainClock.advanceTimeBy(600)
        capture("03-reaction-0600")
        composeRule.mainClock.advanceTimeBy(500)
        capture("04-reaction-1100")
        composeRule.mainClock.advanceTimeBy(10_000)

        composeRule.onNodeWithTag("result").assertExists()
        composeRule.onNodeWithTag("result-stars").assertExists()
        capture("05-result-success")
        Thread.sleep(250)

        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
        advanceUntilExists("menu-achievements")
        capture("06-main-menu-progress")

        composeRule.onNodeWithTag("menu-achievements").performClick()
        advanceUntilExists("achievements-screen")
        capture("07-achievements")
    }

    private fun tapGameCoordinate(gameX: Float, gameY: Float) {
        val canvas = composeRule.onNodeWithTag("game-canvas")
        val bounds = canvas.fetchSemanticsNode().boundsInRoot
        val fieldWidth = 1f
        val fieldHeight = 16f / 9f
        val scale = min(bounds.width / fieldWidth, bounds.height / fieldHeight)
        val viewportLeft = (bounds.width - fieldWidth * scale) / 2f
        val viewportTop = (bounds.height - fieldHeight * scale) / 2f
        canvas.performTouchInput {
            click(
                Offset(
                    x = viewportLeft + gameX * scale,
                    y = viewportTop + gameY * scale,
                ),
            )
        }
    }

    private fun useRussianLocale() {
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags("ru-RU")
        }
        Thread.sleep(300)
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
