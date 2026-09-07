package com.sl.impulse

import android.app.LocaleManager
import android.os.LocaleList
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBack
import java.io.File
import java.io.FileOutputStream
import org.junit.Rule
import org.junit.Test

class RuStoreScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun captureRuStoreScreenshots() {
        val localeManager = composeRule.activity.getSystemService(LocaleManager::class.java)
        localeManager.applicationLocales = LocaleList.forLanguageTags("ru-RU")
        advanceFrames(30)

        finishSplash()
        capture("00_menu.png")

        composeRule.onNodeWithTag("menu-new-game").performClick()
        advanceUntilExists("game-canvas")
        composeRule.onNodeWithTag("game-canvas").performTouchInput { click() }
        composeRule.mainClock.advanceTimeBy(900)
        composeRule.mainClock.advanceTimeByFrame()
        capture("01_gameplay.png")

        pressBack()
        advanceUntilExists("menu-new-game")
        composeRule.onNodeWithTag("menu-new-game").performClick()
        advanceUntilExists("level-button")
        composeRule.onNodeWithTag("level-button").performClick()
        advanceUntilExists("level-picker")
        capture("02_campaign.png")

        pressBack()
        advanceUntilExists("menu-endless")
        composeRule.onNodeWithTag("menu-endless").performClick()
        advanceUntilExists("replay-mode")
        capture("03_endless.png")

        pressBack()
        advanceUntilExists("menu-daily")
        composeRule.onNodeWithTag("menu-daily").performClick()
        advanceUntilExists("replay-mode")
        capture("04_daily.png")

        pressBack()
        advanceUntilExists("menu-achievements")
        composeRule.onNodeWithTag("menu-achievements").performClick()
        advanceUntilExists("achievements-screen")
        capture("05_achievements.png")

        pressBack()
        advanceUntilExists("menu-new-game")
        composeRule.onNodeWithTag("menu-new-game").performClick()
        advanceUntilExists("settings-button")
        composeRule.onNodeWithTag("settings-button").performClick()
        advanceUntilExists("settings-panel")
        capture("06_settings.png")
    }

    private fun capture(name: String) {
        composeRule.waitForIdle()
        val bitmap = composeRule.onRoot(useUnmergedTree = true).captureToImage().asAndroidBitmap()
        val directory = File(composeRule.activity.getExternalFilesDir(null), "rustore")
        check(directory.exists() || directory.mkdirs())
        FileOutputStream(File(directory, name)).use { output ->
            check(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, output))
        }
    }

    private fun finishSplash() {
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(5_100)
        composeRule.mainClock.advanceTimeByFrame()
    }

    private fun advanceFrames(count: Int) {
        repeat(count) {
            composeRule.mainClock.advanceTimeByFrame()
            Thread.sleep(5)
        }
    }

    private fun advanceUntilExists(tag: String) {
        repeat(180) {
            composeRule.mainClock.advanceTimeByFrame()
            if (runCatching { composeRule.onNodeWithTag(tag).assertExists() }.isSuccess) return
            Thread.sleep(10)
        }
        composeRule.onNodeWithTag(tag).assertExists()
    }
}
