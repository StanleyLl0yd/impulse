package com.sl.impulse.progress

data class ProgressState(
    val highestUnlockedLevel: Int = 1,
    val bestScores: Map<Int, Int> = emptyMap(),
    val bestStars: Map<Int, Int> = emptyMap(),
) {
    fun isUnlocked(levelNumber: Int): Boolean = levelNumber in 1..highestUnlockedLevel

    fun bestScore(levelNumber: Int): Int = bestScores[levelNumber] ?: 0

    fun stars(levelNumber: Int): Int = bestStars[levelNumber] ?: 0

    val totalStars: Int
        get() = bestStars.values.sum()
}

internal data class LevelResultUpdate(
    val bestScore: Int? = null,
    val bestStars: Int? = null,
    val highestUnlockedLevel: Int? = null,
)

internal fun calculateLevelResultUpdate(
    currentBestScore: Int,
    currentBestStars: Int,
    highestUnlockedLevel: Int,
    levelNumber: Int,
    score: Int,
    stars: Int,
    success: Boolean,
    totalLevels: Int,
): LevelResultUpdate = LevelResultUpdate(
    bestScore = score.takeIf { it > currentBestScore },
    bestStars = stars.takeIf { it > currentBestStars },
    highestUnlockedLevel = if (success) {
        maxOf(highestUnlockedLevel, minOf(levelNumber + 1, totalLevels))
    } else {
        null
    },
)

fun recordLevelResult(
    state: ProgressState,
    levelNumber: Int,
    score: Int,
    stars: Int,
    success: Boolean,
    totalLevels: Int,
): ProgressState {
    require(levelNumber in 1..totalLevels)
    require(score >= 0)
    require(stars in 0..3)

    val update = calculateLevelResultUpdate(
        currentBestScore = state.bestScore(levelNumber),
        currentBestStars = state.stars(levelNumber),
        highestUnlockedLevel = state.highestUnlockedLevel,
        levelNumber = levelNumber,
        score = score,
        stars = stars,
        success = success,
        totalLevels = totalLevels,
    )

    return state.copy(
        highestUnlockedLevel = update.highestUnlockedLevel ?: state.highestUnlockedLevel,
        bestScores = update.bestScore?.let { state.bestScores + (levelNumber to it) } ?: state.bestScores,
        bestStars = update.bestStars?.let { state.bestStars + (levelNumber to it) } ?: state.bestStars,
    )
}
