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

    val scores = state.bestScores.toMutableMap()
    val previousScore = scores[levelNumber] ?: 0
    if (score > previousScore) scores[levelNumber] = score

    val starMap = state.bestStars.toMutableMap()
    val previousStars = starMap[levelNumber] ?: 0
    if (stars > previousStars) starMap[levelNumber] = stars

    val unlocked = if (success) {
        maxOf(state.highestUnlockedLevel, minOf(levelNumber + 1, totalLevels))
    } else {
        state.highestUnlockedLevel
    }

    return ProgressState(
        highestUnlockedLevel = unlocked,
        bestScores = scores,
        bestStars = starMap,
    )
}
