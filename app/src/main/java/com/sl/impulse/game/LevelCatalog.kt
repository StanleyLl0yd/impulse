package com.sl.impulse.game

data class LevelDefinition(
    val number: Int,
    val seed: Long,
    val particleCount: Int,
    val requiredCount: Int,
) {
    init {
        require(number > 0)
        require(particleCount > 0)
        require(requiredCount in 1 until particleCount)
    }

    val twoStarThreshold: Int = requiredCount + ((particleCount - requiredCount) + 1) / 2
    val threeStarThreshold: Int = particleCount
}

object LevelCatalog {
    val levels: List<LevelDefinition> = listOf(
        LevelDefinition(1, 0x1101L, 18, 8),
        LevelDefinition(2, 0x1102L, 18, 9),
        LevelDefinition(3, 0x1103L, 20, 10),
        LevelDefinition(4, 0x1104L, 20, 11),
        LevelDefinition(5, 0x1105L, 22, 12),
        LevelDefinition(6, 0x1106L, 22, 13),
        LevelDefinition(7, 0x1107L, 24, 14),
        LevelDefinition(8, 0x1108L, 24, 15),
        LevelDefinition(9, 0x1109L, 26, 16),
        LevelDefinition(10, 0x1110L, 26, 17),
        LevelDefinition(11, 0x1111L, 28, 18),
        LevelDefinition(12, 0x1112L, 28, 19),
        LevelDefinition(13, 0x1113L, 30, 20),
        LevelDefinition(14, 0x1114L, 30, 21),
        LevelDefinition(15, 0x1115L, 32, 22),
        LevelDefinition(16, 0x1116L, 32, 23),
        LevelDefinition(17, 0x1117L, 34, 24),
        LevelDefinition(18, 0x1118L, 34, 25),
        LevelDefinition(19, 0x1119L, 36, 26),
        LevelDefinition(20, 0x1120L, 36, 27),
    )

    init {
        require(levels.map { it.number } == (1..levels.size).toList())
        require(levels.map { it.seed }.distinct().size == levels.size)
    }

    fun get(number: Int): LevelDefinition =
        levels.getOrElse(number - 1) { throw IllegalArgumentException("Unknown level: $number") }
}

fun calculateLevelStars(level: LevelDefinition, triggeredCount: Int, success: Boolean): Int = when {
    !success -> 0
    triggeredCount >= level.threeStarThreshold -> 3
    triggeredCount >= level.twoStarThreshold -> 2
    else -> 1
}

fun calculateLevelScore(
    level: LevelDefinition,
    triggeredCount: Int,
    maximumChainDepth: Int,
    success: Boolean,
): Int {
    val completionBonus = if (success) 500 + level.number * 50 else 0
    val surplusBonus = (triggeredCount - level.requiredCount).coerceAtLeast(0) * 50
    return triggeredCount * 100 + maximumChainDepth * 25 + completionBonus + surplusBonus
}
