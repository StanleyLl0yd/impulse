package com.sl.impulse.progress

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sl.impulse.game.LevelCatalog
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.playerDataStore by preferencesDataStore(name = "player_state")

data class PlayerState(
    val progress: ProgressState = ProgressState(),
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
            val highestUnlocked = (preferences[HIGHEST_UNLOCKED] ?: 1).coerceIn(1, totalLevels)
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
    ) {
        require(levelNumber in 1..totalLevels)
        require(score >= 0)
        require(stars in 0..3)

        dataStore.edit { preferences ->
            val scoreKey = scoreKey(levelNumber)
            if (score > (preferences[scoreKey] ?: 0)) preferences[scoreKey] = score

            val starsKey = starsKey(levelNumber)
            if (stars > (preferences[starsKey] ?: 0)) preferences[starsKey] = stars

            if (success) {
                val nextLevel = minOf(levelNumber + 1, totalLevels)
                preferences[HIGHEST_UNLOCKED] = maxOf(
                    preferences[HIGHEST_UNLOCKED] ?: 1,
                    nextLevel,
                )
            }
        }
    }

    suspend fun selectLevel(levelNumber: Int) {
        require(levelNumber in 1..totalLevels)
        dataStore.edit { preferences -> preferences[SELECTED_LEVEL] = levelNumber }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[HAPTICS_ENABLED] = enabled }
    }

    suspend fun setReducedEffects(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[REDUCED_EFFECTS] = enabled }
    }

    private companion object {
        val HIGHEST_UNLOCKED = intPreferencesKey("highest_unlocked_level")
        val SELECTED_LEVEL = intPreferencesKey("selected_level")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCED_EFFECTS = booleanPreferencesKey("reduced_effects")

        fun scoreKey(levelNumber: Int) = intPreferencesKey("best_score_$levelNumber")
        fun starsKey(levelNumber: Int) = intPreferencesKey("best_stars_$levelNumber")
    }
}
