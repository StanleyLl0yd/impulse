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

internal data class ProgressUpdate(
    val highestUnlockedLevel: Int,
    val bestScore: Int,
    val bestStars: Int,
)

internal fun calculateProgressUpdate(
    currentHighestUnlockedLevel: Int,
    currentBestScore: Int,
    currentBestStars: Int,
    levelNumber: Int,
    score: Int,
    stars: Int,
    success: Boolean,
    totalLevels: Int,
): ProgressUpdate = ProgressUpdate(
    highestUnlockedLevel = if (success) {
        maxOf(currentHighestUnlockedLevel, minOf(levelNumber + 1, totalLevels))
    } else {
        currentHighestUnlockedLevel
    },
    bestScore = maxOf(currentBestScore, score),
    bestStars = maxOf(currentBestStars, stars),
)
