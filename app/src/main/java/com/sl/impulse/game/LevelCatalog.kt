package com.sl.impulse.game

enum class ParticleType {
    STANDARD,
    BOOSTER,
    FUSE,
    ANCHOR,
}

data class ParticleMix(
    val boosterCount: Int = 0,
    val fuseCount: Int = 0,
    val anchorCount: Int = 0,
) {
    init {
        require(boosterCount >= 0)
        require(fuseCount >= 0)
        require(anchorCount >= 0)
    }

    val specialCount: Int = boosterCount + fuseCount + anchorCount
}

data class LevelDefinition(
    val number: Int,
    val seed: Long,
    val particleCount: Int,
    val requiredCount: Int,
    val particleMix: ParticleMix = ParticleMix(),
) {
    init {
        require(number > 0)
        require(particleCount > 0)
        require(requiredCount in 1 until particleCount)
        require(particleMix.specialCount <= particleCount)
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
        LevelDefinition(21, 0x1121L, 38, 28, ParticleMix(boosterCount = 2)),
        LevelDefinition(22, 0x1122L, 38, 29, ParticleMix(boosterCount = 3)),
        LevelDefinition(23, 0x1123L, 40, 29, ParticleMix(boosterCount = 4)),
        LevelDefinition(24, 0x1124L, 40, 30, ParticleMix(boosterCount = 4)),
        LevelDefinition(25, 0x1125L, 42, 31, ParticleMix(boosterCount = 5)),
        LevelDefinition(26, 0x1126L, 40, 29, ParticleMix(fuseCount = 2)),
        LevelDefinition(27, 0x1127L, 40, 30, ParticleMix(fuseCount = 3)),
        LevelDefinition(28, 0x1128L, 42, 31, ParticleMix(fuseCount = 4)),
        LevelDefinition(29, 0x1129L, 42, 32, ParticleMix(boosterCount = 2, fuseCount = 3)),
        LevelDefinition(30, 0x1130L, 44, 33, ParticleMix(boosterCount = 3, fuseCount = 4)),
        LevelDefinition(31, 0x1131L, 42, 31, ParticleMix(anchorCount = 3)),
        LevelDefinition(32, 0x1132L, 42, 32, ParticleMix(anchorCount = 4)),
        LevelDefinition(33, 0x1133L, 44, 33, ParticleMix(anchorCount = 5)),
        LevelDefinition(34, 0x1134L, 44, 34, ParticleMix(boosterCount = 2, anchorCount = 4)),
        LevelDefinition(35, 0x1135L, 46, 35, ParticleMix(fuseCount = 2, anchorCount = 5)),
        LevelDefinition(36, 0x1136L, 44, 33, ParticleMix(boosterCount = 3, fuseCount = 3, anchorCount = 3)),
        LevelDefinition(37, 0x1137L, 46, 34, ParticleMix(boosterCount = 4, fuseCount = 3, anchorCount = 4)),
        LevelDefinition(38, 0x1138L, 46, 35, ParticleMix(boosterCount = 4, fuseCount = 4, anchorCount = 4)),
        LevelDefinition(39, 0x1139L, 48, 36, ParticleMix(boosterCount = 5, fuseCount = 4, anchorCount = 5)),
        LevelDefinition(40, 0x1140L, 48, 37, ParticleMix(boosterCount = 5, fuseCount = 5, anchorCount = 5)),
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
