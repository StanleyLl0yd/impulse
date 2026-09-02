package com.sl.impulse.progress

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sl.impulse.game.LevelCatalog
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.playerDataStore by preferencesDataStore(name = "player_state")

data class ReplayProgress(
    val endlessBestRound: Int = 0,
    val endlessBestScore: Int = 0,
    val dailyEpochDay: Long = Long.MIN_VALUE,
    val dailyBestScore: Int = 0,
    val dailyBestStars: Int = 0,
    val dailyCompletedDays: Int = 0,
)

data class PlayerState(
    val progress: ProgressState = ProgressState(),
    val statistics: PlayerStatistics = PlayerStatistics(),
    val replay: ReplayProgress = ReplayProgress(),
    val selectedLevel: Int = 1,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reducedEffects: Boolean = false,
)

class PlayerStateRepository(context: Context) {
    private val dataStore = context.applicationContext.playerDataStore
    private val totalLevels = LevelCatalog.levels.size

    val state: Flow<PlayerState> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            val highestUnlocked = expandedCampaignHighestUnlocked(
                storedHighestUnlocked = preferences[HIGHEST_UNLOCKED] ?: 1,
                completedPreviousFinalLevels = completedPreviousCampaigns(preferences),
                totalLevels = totalLevels,
            )
            val selectedLevel = (preferences[SELECTED_LEVEL] ?: highestUnlocked).coerceIn(1, totalLevels)
            val scores = buildMap {
                for (level in 1..totalLevels) {
                    val score = preferences[scoreKey(level)] ?: 0
                    if (score > 0) put(level, score)
                }
            }
            val stars = buildMap {
                for (level in 1..totalLevels) {
                    val value = (preferences[starsKey(level)] ?: 0).coerceIn(0, 3)
                    if (value > 0) put(level, value)
                }
            }

            PlayerState(
                progress = ProgressState(
                    highestUnlockedLevel = highestUnlocked,
                    bestScores = scores,
                    bestStars = stars,
                ),
                statistics = statisticsFrom(preferences),
                replay = replayFrom(preferences),
                selectedLevel = selectedLevel,
                soundEnabled = preferences[SOUND_ENABLED] ?: true,
                hapticsEnabled = preferences[HAPTICS_ENABLED] ?: true,
                reducedEffects = preferences[REDUCED_EFFECTS] ?: false,
            )
        }

    suspend fun recordResult(
        levelNumber: Int,
        score: Int,
        stars: Int,
        success: Boolean,
        triggeredCount: Int,
        maximumChainDepth: Int,
    ) {
        require(levelNumber in 1..totalLevels)
        require(score >= 0)
        require(stars in 0..3)
        require(triggeredCount in 0..LevelCatalog.get(levelNumber).particleCount)
        require(maximumChainDepth >= 0)

        dataStore.edit { preferences ->
            val levelScoreKey = scoreKey(levelNumber)
            val levelStarsKey = starsKey(levelNumber)
            val currentScore = preferences[levelScoreKey] ?: 0
            val currentStars = preferences[levelStarsKey] ?: 0
            val currentHighestUnlocked = expandedCampaignHighestUnlocked(
                storedHighestUnlocked = preferences[HIGHEST_UNLOCKED] ?: 1,
                completedPreviousFinalLevels = completedPreviousCampaigns(preferences),
                totalLevels = totalLevels,
            )
            val progressUpdate = calculateProgressUpdate(
                currentHighestUnlockedLevel = currentHighestUnlocked,
                currentBestScore = currentScore,
                currentBestStars = currentStars,
                levelNumber = levelNumber,
                score = score,
                stars = stars,
                success = success,
                totalLevels = totalLevels,
            )
            val statisticsUpdate = calculateStatisticsUpdate(
                current = statisticsFrom(preferences),
                triggeredCount = triggeredCount,
                maximumChainDepth = maximumChainDepth,
                success = success,
            )

            if (progressUpdate.bestScore != currentScore) preferences[levelScoreKey] = progressUpdate.bestScore
            if (progressUpdate.bestStars != currentStars) preferences[levelStarsKey] = progressUpdate.bestStars
            preferences[HIGHEST_UNLOCKED] = progressUpdate.highestUnlockedLevel
            writeStatistics(preferences, statisticsUpdate)
        }
    }

    suspend fun recordEndlessResult(
        completedRounds: Int,
        runScore: Int,
        triggeredCount: Int,
        maximumChainDepth: Int,
        success: Boolean,
    ) {
        require(completedRounds >= 0)
        require(runScore >= 0)
        require(triggeredCount >= 0)
        require(maximumChainDepth >= 0)
        dataStore.edit { preferences ->
            preferences[ENDLESS_BEST_ROUND] = maxOf(preferences[ENDLESS_BEST_ROUND] ?: 0, completedRounds)
            preferences[ENDLESS_BEST_SCORE] = maxOf(preferences[ENDLESS_BEST_SCORE] ?: 0, runScore)
            writeStatistics(
                preferences,
                calculateStatisticsUpdate(
                    current = statisticsFrom(preferences),
                    triggeredCount = triggeredCount,
                    maximumChainDepth = maximumChainDepth,
                    success = success,
                ),
            )
        }
    }

    suspend fun recordDailyResult(
        epochDay: Long,
        score: Int,
        stars: Int,
        success: Boolean,
        triggeredCount: Int,
        maximumChainDepth: Int,
    ) {
        require(score >= 0)
        require(stars in 0..3)
        require(triggeredCount >= 0)
        require(maximumChainDepth >= 0)
        dataStore.edit { preferences ->
            val sameDay = preferences[DAILY_EPOCH_DAY] == epochDay
            preferences[DAILY_EPOCH_DAY] = epochDay
            preferences[DAILY_BEST_SCORE] = if (sameDay) maxOf(preferences[DAILY_BEST_SCORE] ?: 0, score) else score
            preferences[DAILY_BEST_STARS] = if (sameDay) maxOf(preferences[DAILY_BEST_STARS] ?: 0, stars) else stars
            if (success && preferences[DAILY_LAST_COMPLETED_DAY] != epochDay) {
                preferences[DAILY_COMPLETED_DAYS] = (preferences[DAILY_COMPLETED_DAYS] ?: 0) + 1
                preferences[DAILY_LAST_COMPLETED_DAY] = epochDay
            }
            writeStatistics(
                preferences,
                calculateStatisticsUpdate(
                    current = statisticsFrom(preferences),
                    triggeredCount = triggeredCount,
                    maximumChainDepth = maximumChainDepth,
                    success = success,
                ),
            )
        }
    }

    suspend fun selectLevel(levelNumber: Int) {
        require(levelNumber in 1..totalLevels)
        dataStore.edit { preferences -> preferences[SELECTED_LEVEL] = levelNumber }
    }

    suspend fun setSoundEnabled(enabled: Boolean) = setBoolean(SOUND_ENABLED, enabled)
    suspend fun setHapticsEnabled(enabled: Boolean) = setBoolean(HAPTICS_ENABLED, enabled)
    suspend fun setReducedEffects(enabled: Boolean) = setBoolean(REDUCED_EFFECTS, enabled)

    private suspend fun setBoolean(key: Preferences.Key<Boolean>, enabled: Boolean) {
        dataStore.edit { preferences -> preferences[key] = enabled }
    }

    private fun completedPreviousCampaigns(preferences: Preferences): Set<Int> =
        PREVIOUS_FINAL_LEVELS.filterTo(mutableSetOf()) { level ->
            (preferences[starsKey(level)] ?: 0) > 0
        }

    private fun statisticsFrom(preferences: Preferences): PlayerStatistics = PlayerStatistics(
        totalAttempts = (preferences[TOTAL_ATTEMPTS] ?: 0).coerceAtLeast(0),
        successfulAttempts = (preferences[SUCCESSFUL_ATTEMPTS] ?: 0).coerceAtLeast(0),
        totalTriggeredParticles = (preferences[TOTAL_TRIGGERED_PARTICLES] ?: 0).coerceAtLeast(0),
        bestTriggeredCount = (preferences[BEST_TRIGGERED_COUNT] ?: 0).coerceAtLeast(0),
        bestChainDepth = (preferences[BEST_CHAIN_DEPTH] ?: 0).coerceAtLeast(0),
    )

    private fun replayFrom(preferences: Preferences): ReplayProgress = ReplayProgress(
        endlessBestRound = (preferences[ENDLESS_BEST_ROUND] ?: 0).coerceAtLeast(0),
        endlessBestScore = (preferences[ENDLESS_BEST_SCORE] ?: 0).coerceAtLeast(0),
        dailyEpochDay = preferences[DAILY_EPOCH_DAY] ?: Long.MIN_VALUE,
        dailyBestScore = (preferences[DAILY_BEST_SCORE] ?: 0).coerceAtLeast(0),
        dailyBestStars = (preferences[DAILY_BEST_STARS] ?: 0).coerceIn(0, 3),
        dailyCompletedDays = (preferences[DAILY_COMPLETED_DAYS] ?: 0).coerceAtLeast(0),
    )

    private fun writeStatistics(preferences: androidx.datastore.preferences.core.MutablePreferences, value: PlayerStatistics) {
        preferences[TOTAL_ATTEMPTS] = value.totalAttempts
        preferences[SUCCESSFUL_ATTEMPTS] = value.successfulAttempts
        preferences[TOTAL_TRIGGERED_PARTICLES] = value.totalTriggeredParticles
        preferences[BEST_TRIGGERED_COUNT] = value.bestTriggeredCount
        preferences[BEST_CHAIN_DEPTH] = value.bestChainDepth
    }

    private companion object {
        val PREVIOUS_FINAL_LEVELS = setOf(20, 40)

        val HIGHEST_UNLOCKED = intPreferencesKey("highest_unlocked_level")
        val SELECTED_LEVEL = intPreferencesKey("selected_level")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCED_EFFECTS = booleanPreferencesKey("reduced_effects")
        val TOTAL_ATTEMPTS = intPreferencesKey("total_attempts")
        val SUCCESSFUL_ATTEMPTS = intPreferencesKey("successful_attempts")
        val TOTAL_TRIGGERED_PARTICLES = intPreferencesKey("total_triggered_particles")
        val BEST_TRIGGERED_COUNT = intPreferencesKey("best_triggered_count")
        val BEST_CHAIN_DEPTH = intPreferencesKey("best_chain_depth")
        val ENDLESS_BEST_ROUND = intPreferencesKey("endless_best_round")
        val ENDLESS_BEST_SCORE = intPreferencesKey("endless_best_score")
        val DAILY_EPOCH_DAY = longPreferencesKey("daily_epoch_day")
        val DAILY_BEST_SCORE = intPreferencesKey("daily_best_score")
        val DAILY_BEST_STARS = intPreferencesKey("daily_best_stars")
        val DAILY_COMPLETED_DAYS = intPreferencesKey("daily_completed_days")
        val DAILY_LAST_COMPLETED_DAY = longPreferencesKey("daily_last_completed_day")

        fun scoreKey(levelNumber: Int) = intPreferencesKey("best_score_$levelNumber")
        fun starsKey(levelNumber: Int) = intPreferencesKey("best_stars_$levelNumber")
    }
}
