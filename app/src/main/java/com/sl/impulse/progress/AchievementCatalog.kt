package com.sl.impulse.progress

import com.sl.impulse.game.ChapterId
import com.sl.impulse.game.LevelCatalog

enum class AchievementGroup {
    JOURNEY,
    MASTERY,
    CHAIN,
    ENDURANCE,
}

enum class AchievementId {
    FIRST_IMPULSE,
    TEN_LEVELS,
    HALFWAY,
    CHAPTER_IMPULSE,
    CHAPTER_MOMENTUM,
    CHAPTER_BOOST,
    CHAPTER_CONTROL,
    CHAPTER_RESONANCE,
    CHAPTER_CHAOS,
    MASTER,
    PERFECT_CHAIN,
    UNSTOPPABLE,
    FLAWLESS_CHAPTER,
    PERFECTIONIST,
    DEEP_IMPACT,
    EVENT_HORIZON,
    CHAIN_REACTION,
    RESONANCE_CHAIN,
    PARTICLE_STORM,
    VETERAN,
    CONSISTENT,
}

data class AchievementStatus(
    val id: AchievementId,
    val group: AchievementGroup,
    val unlocked: Boolean,
)

object AchievementCatalog {
    fun evaluate(playerState: PlayerState): List<AchievementStatus> {
        val progress = playerState.progress
        val statistics = playerState.statistics
        val chapters = LevelCatalog.chapters.associateBy { it.id }
        val totalLevels = LevelCatalog.levels.size
        fun chapterCompleted(id: ChapterId): Boolean = progress.isCompleted(requireNotNull(chapters[id]).levels)

        return listOf(
            AchievementStatus(AchievementId.FIRST_IMPULSE, AchievementGroup.JOURNEY, progress.completedLevels >= 1),
            AchievementStatus(AchievementId.TEN_LEVELS, AchievementGroup.JOURNEY, progress.completedLevels >= 10),
            AchievementStatus(AchievementId.HALFWAY, AchievementGroup.JOURNEY, progress.completedLevels >= 30),
            AchievementStatus(AchievementId.CHAPTER_IMPULSE, AchievementGroup.JOURNEY, chapterCompleted(ChapterId.IMPULSE)),
            AchievementStatus(AchievementId.CHAPTER_MOMENTUM, AchievementGroup.JOURNEY, chapterCompleted(ChapterId.MOMENTUM)),
            AchievementStatus(AchievementId.CHAPTER_BOOST, AchievementGroup.JOURNEY, chapterCompleted(ChapterId.BOOST)),
            AchievementStatus(AchievementId.CHAPTER_CONTROL, AchievementGroup.JOURNEY, chapterCompleted(ChapterId.CONTROL)),
            AchievementStatus(AchievementId.CHAPTER_RESONANCE, AchievementGroup.JOURNEY, chapterCompleted(ChapterId.RESONANCE)),
            AchievementStatus(AchievementId.CHAPTER_CHAOS, AchievementGroup.JOURNEY, chapterCompleted(ChapterId.CHAOS)),
            AchievementStatus(AchievementId.MASTER, AchievementGroup.JOURNEY, progress.isCompleted(1..totalLevels)),
            AchievementStatus(AchievementId.PERFECT_CHAIN, AchievementGroup.MASTERY, progress.perfectLevels >= 1),
            AchievementStatus(AchievementId.UNSTOPPABLE, AchievementGroup.MASTERY, progress.perfectLevels >= 10),
            AchievementStatus(
                AchievementId.FLAWLESS_CHAPTER,
                AchievementGroup.MASTERY,
                LevelCatalog.chapters.any { progress.isPerfect(it.levels) },
            ),
            AchievementStatus(AchievementId.PERFECTIONIST, AchievementGroup.MASTERY, progress.isPerfect(1..totalLevels)),
            AchievementStatus(AchievementId.DEEP_IMPACT, AchievementGroup.CHAIN, statistics.bestChainDepth >= 10),
            AchievementStatus(AchievementId.EVENT_HORIZON, AchievementGroup.CHAIN, statistics.bestChainDepth >= 15),
            AchievementStatus(AchievementId.CHAIN_REACTION, AchievementGroup.CHAIN, statistics.bestTriggeredCount >= 30),
            AchievementStatus(AchievementId.RESONANCE_CHAIN, AchievementGroup.CHAIN, statistics.bestTriggeredCount >= 50),
            AchievementStatus(AchievementId.PARTICLE_STORM, AchievementGroup.ENDURANCE, statistics.totalTriggeredParticles >= 1_000),
            AchievementStatus(AchievementId.VETERAN, AchievementGroup.ENDURANCE, statistics.totalAttempts >= 100),
            AchievementStatus(AchievementId.CONSISTENT, AchievementGroup.ENDURANCE, statistics.successfulAttempts >= 50),
        )
    }
}
