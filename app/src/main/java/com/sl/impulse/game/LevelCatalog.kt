package com.sl.impulse.game

enum class ParticleType {
    STANDARD,
    BOOSTER,
    FUSE,
    ANCHOR,
}

enum class ChapterId {
    IMPULSE,
    MOMENTUM,
    BOOST,
    CONTROL,
    RESONANCE,
    CHAOS,
}

data class ChapterDefinition(
    val number: Int,
    val id: ChapterId,
    val levels: IntRange,
)

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
    val chapters: List<ChapterDefinition> = listOf(
        ChapterDefinition(1, ChapterId.IMPULSE, 1..10),
        ChapterDefinition(2, ChapterId.MOMENTUM, 11..20),
        ChapterDefinition(3, ChapterId.BOOST, 21..30),
        ChapterDefinition(4, ChapterId.CONTROL, 31..40),
        ChapterDefinition(5, ChapterId.RESONANCE, 41..50),
        ChapterDefinition(6, ChapterId.CHAOS, 51..60),
    )

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
        LevelDefinition(21, 0x2101L, 38, 28, ParticleMix(boosterCount = 2)),
        LevelDefinition(22, 0x2141L, 38, 29, ParticleMix(boosterCount = 3)),
        LevelDefinition(23, 0x2180L, 40, 29, ParticleMix(boosterCount = 4)),
        LevelDefinition(24, 0x21C1L, 40, 30, ParticleMix(boosterCount = 4)),
        LevelDefinition(25, 0x2201L, 42, 31, ParticleMix(boosterCount = 5)),
        LevelDefinition(26, 0x2242L, 40, 29, ParticleMix(fuseCount = 2)),
        LevelDefinition(27, 0x2282L, 40, 30, ParticleMix(fuseCount = 3)),
        LevelDefinition(28, 0x22C1L, 42, 31, ParticleMix(fuseCount = 4)),
        LevelDefinition(29, 0x2301L, 42, 32, ParticleMix(boosterCount = 2, fuseCount = 3)),
        LevelDefinition(30, 0x2341L, 44, 33, ParticleMix(boosterCount = 3, fuseCount = 4)),
        LevelDefinition(31, 0x2381L, 42, 31, ParticleMix(anchorCount = 3)),
        LevelDefinition(32, 0x23C7L, 42, 32, ParticleMix(anchorCount = 4)),
        LevelDefinition(33, 0x2402L, 44, 33, ParticleMix(anchorCount = 5)),
        LevelDefinition(34, 0x2440L, 44, 34, ParticleMix(boosterCount = 2, anchorCount = 4)),
        LevelDefinition(35, 0x2480L, 46, 35, ParticleMix(fuseCount = 2, anchorCount = 5)),
        LevelDefinition(36, 0x24C1L, 44, 33, ParticleMix(boosterCount = 3, fuseCount = 3, anchorCount = 3)),
        LevelDefinition(37, 0x2500L, 46, 34, ParticleMix(boosterCount = 4, fuseCount = 3, anchorCount = 4)),
        LevelDefinition(38, 0x2541L, 46, 35, ParticleMix(boosterCount = 4, fuseCount = 4, anchorCount = 4)),
        LevelDefinition(39, 0x2580L, 48, 36, ParticleMix(boosterCount = 5, fuseCount = 4, anchorCount = 5)),
        LevelDefinition(40, 0x25C1L, 48, 37, ParticleMix(boosterCount = 5, fuseCount = 5, anchorCount = 5)),
        LevelDefinition(41, 0x3001L, 50, 38, ParticleMix(boosterCount = 6, fuseCount = 5, anchorCount = 5)),
        LevelDefinition(42, 0x3041L, 50, 39, ParticleMix(boosterCount = 5, fuseCount = 6, anchorCount = 5)),
        LevelDefinition(43, 0x3081L, 52, 39, ParticleMix(boosterCount = 7, fuseCount = 5, anchorCount = 5)),
        LevelDefinition(44, 0x30C1L, 52, 40, ParticleMix(boosterCount = 6, fuseCount = 7, anchorCount = 5)),
        LevelDefinition(45, 0x3101L, 54, 41, ParticleMix(boosterCount = 7, fuseCount = 6, anchorCount = 6)),
        LevelDefinition(46, 0x3141L, 54, 41, ParticleMix(boosterCount = 8, fuseCount = 7, anchorCount = 6)),
        LevelDefinition(47, 0x3181L, 56, 42, ParticleMix(boosterCount = 8, fuseCount = 6, anchorCount = 7)),
        LevelDefinition(48, 0x31C1L, 56, 43, ParticleMix(boosterCount = 7, fuseCount = 8, anchorCount = 7)),
        LevelDefinition(49, 0x3201L, 58, 44, ParticleMix(boosterCount = 9, fuseCount = 7, anchorCount = 8)),
        LevelDefinition(50, 0x3241L, 58, 45, ParticleMix(boosterCount = 8, fuseCount = 9, anchorCount = 8)),
        LevelDefinition(51, 0x3281L, 54, 40, ParticleMix(boosterCount = 9, fuseCount = 9, anchorCount = 9)),
        LevelDefinition(52, 0x32C1L, 56, 42, ParticleMix(boosterCount = 10, fuseCount = 9, anchorCount = 9)),
        LevelDefinition(53, 0x3301L, 58, 43, ParticleMix(boosterCount = 10, fuseCount = 10, anchorCount = 9)),
        LevelDefinition(54, 0x3341L, 58, 44, ParticleMix(boosterCount = 10, fuseCount = 9, anchorCount = 10)),
        LevelDefinition(55, 0x3381L, 60, 45, ParticleMix(boosterCount = 11, fuseCount = 10, anchorCount = 10)),
        LevelDefinition(56, 0x33C1L, 60, 46, ParticleMix(boosterCount = 10, fuseCount = 11, anchorCount = 10)),
        LevelDefinition(57, 0x3401L, 62, 47, ParticleMix(boosterCount = 12, fuseCount = 10, anchorCount = 11)),
        LevelDefinition(58, 0x3441L, 62, 48, ParticleMix(boosterCount = 11, fuseCount = 12, anchorCount = 11)),
        LevelDefinition(59, 0x3481L, 64, 49, ParticleMix(boosterCount = 12, fuseCount = 12, anchorCount = 12)),
        LevelDefinition(60, 0x34C1L, 64, 50, ParticleMix(boosterCount = 13, fuseCount = 12, anchorCount = 12)),
    )

    init {
        require(levels.map { it.number } == (1..levels.size).toList())
        require(levels.map { it.seed }.distinct().size == levels.size)
        require(chapters.map { it.number } == (1..chapters.size).toList())
        require(chapters.flatMap { it.levels.toList() } == (1..levels.size).toList())
    }

    fun get(number: Int): LevelDefinition =
        levels.getOrElse(number - 1) { throw IllegalArgumentException("Unknown level: $number") }

    fun chapterFor(levelNumber: Int): ChapterDefinition =
        chapters.firstOrNull { levelNumber in it.levels }
            ?: throw IllegalArgumentException("Unknown level: $levelNumber")
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
