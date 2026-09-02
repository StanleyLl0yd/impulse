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

    val completedLevels: Int
        get() = bestStars.values.count { it > 0 }

    val perfectLevels: Int
        get() = bestStars.values.count { it == 3 }

    val bestOverallScore: Int
        get() = bestScores.values.maxOrNull() ?: 0
}

data class PlayerStatistics(
    val totalAttempts: Int = 0,
    val successfulAttempts: Int = 0,
    val totalTriggeredParticles: Int = 0,
    val bestTriggeredCount: Int = 0,
    val bestChainDepth: Int = 0,
) {
    val successRatePercent: Int
        get() = if (totalAttempts == 0) 0 else successfulAttempts * 100 / totalAttempts
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

internal fun calculateStatisticsUpdate(
    current: PlayerStatistics,
    triggeredCount: Int,
    maximumChainDepth: Int,
    success: Boolean,
): PlayerStatistics = PlayerStatistics(
    totalAttempts = current.totalAttempts + 1,
    successfulAttempts = current.successfulAttempts + if (success) 1 else 0,
    totalTriggeredParticles = current.totalTriggeredParticles + triggeredCount,
    bestTriggeredCount = maxOf(current.bestTriggeredCount, triggeredCount),
    bestChainDepth = maxOf(current.bestChainDepth, maximumChainDepth),
)

internal fun expandedCampaignHighestUnlocked(
    storedHighestUnlocked: Int,
    previousFinalLevel: Int,
    previousFinalStars: Int,
    totalLevels: Int,
): Int {
    val stored = storedHighestUnlocked.coerceIn(1, totalLevels)
    return if (
        totalLevels > previousFinalLevel &&
        stored == previousFinalLevel &&
        previousFinalStars > 0
    ) {
        previousFinalLevel + 1
    } else {
        stored
    }
}
