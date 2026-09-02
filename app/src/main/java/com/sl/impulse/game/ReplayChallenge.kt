package com.sl.impulse.game

import java.time.LocalDate

enum class ReplayMode {
    ENDLESS,
    DAILY,
}

data class ReplayChallenge(
    val mode: ReplayMode,
    val key: Long,
    val stage: Int,
    val level: LevelDefinition,
)

object ReplayChallenges {
    private val endlessSteps = intArrayOf(1, 3, 7, 9, 11, 13, 17, 19, 21, 23, 27, 29, 31, 33, 37, 39)
    private const val replayFirstLevel = 21
    private const val replayLevelCount = 40
    private const val dailyMask = 0x1A2B3C4D5E6F7801L
    private const val stepMask = 0x2545F4914F6CDD1DL

    fun daily(date: LocalDate): ReplayChallenge {
        val epochDay = date.toEpochDay()
        val index = index(mix64(epochDay xor dailyMask), replayLevelCount)
        return ReplayChallenge(
            mode = ReplayMode.DAILY,
            key = epochDay,
            stage = 1,
            level = LevelCatalog.get(replayFirstLevel + index),
        )
    }

    fun endless(runSeed: Long, round: Int): ReplayChallenge {
        require(round > 0)
        val offset = index(mix64(runSeed), replayLevelCount)
        val step = endlessSteps[index(mix64(runSeed xor stepMask), endlessSteps.size)]
        val position = (round - 1) % replayLevelCount
        val cycle = (round - 1) / replayLevelCount
        val source = LevelCatalog.get(replayFirstLevel + (offset + position * step) % replayLevelCount)
        val requiredCount = (source.requiredCount + cycle.coerceAtMost(3))
            .coerceAtMost(source.particleCount - 1)
        return ReplayChallenge(
            mode = ReplayMode.ENDLESS,
            key = runSeed,
            stage = round,
            level = source.copy(number = round, requiredCount = requiredCount),
        )
    }

    fun newRunSeed(timeMillis: Long): Long = mix64(timeMillis xor stepMask)

    private fun index(value: Long, size: Int): Int = Math.floorMod(value, size.toLong()).toInt()

    private fun mix64(value: Long): Long {
        var mixed = value
        mixed = (mixed xor (mixed ushr 33)) * -49064778989728563L
        mixed = (mixed xor (mixed ushr 33)) * -4265267296055464877L
        return mixed xor (mixed ushr 33)
    }
}

fun calculateReplayStars(challenge: ReplayChallenge, triggeredCount: Int, success: Boolean): Int = when {
    !success -> 0
    triggeredCount >= challenge.level.threeStarThreshold -> 3
    triggeredCount >= challenge.level.twoStarThreshold -> 2
    else -> 1
}

fun calculateReplayScore(
    challenge: ReplayChallenge,
    triggeredCount: Int,
    maximumChainDepth: Int,
    success: Boolean,
): Int {
    val tier = when (challenge.mode) {
        ReplayMode.ENDLESS -> challenge.stage
        ReplayMode.DAILY -> (challenge.level.number - 20).coerceAtLeast(1)
    }
    val completionBonus = if (success) 500 + tier.coerceAtMost(100) * 50 else 0
    val surplusBonus = (triggeredCount - challenge.level.requiredCount).coerceAtLeast(0) * 50
    return triggeredCount * 100 + maximumChainDepth * 25 + completionBonus + surplusBonus
}
