package com.sl.impulse.ui

import com.sl.impulse.R
import com.sl.impulse.game.ChapterId
import com.sl.impulse.progress.AchievementGroup
import com.sl.impulse.progress.AchievementId

internal fun chapterNameRes(id: ChapterId): Int = when (id) {
    ChapterId.IMPULSE -> R.string.chapter_impulse
    ChapterId.MOMENTUM -> R.string.chapter_momentum
    ChapterId.BOOST -> R.string.chapter_boost
    ChapterId.CONTROL -> R.string.chapter_control
    ChapterId.RESONANCE -> R.string.chapter_resonance
    ChapterId.CHAOS -> R.string.chapter_chaos
}

internal fun achievementGroupTitleRes(group: AchievementGroup): Int = when (group) {
    AchievementGroup.JOURNEY -> R.string.achievements_journey_section
    AchievementGroup.MASTERY -> R.string.achievements_mastery_section
    AchievementGroup.CHAIN -> R.string.achievements_chain_section
    AchievementGroup.ENDURANCE -> R.string.achievements_endurance_section
    AchievementGroup.REPLAY -> R.string.achievements_replay_section
}

internal fun achievementTitleRes(id: AchievementId): Int = when (id) {
    AchievementId.FIRST_IMPULSE -> R.string.achievement_first_impulse
    AchievementId.TEN_LEVELS -> R.string.achievement_ten_levels
    AchievementId.HALFWAY -> R.string.achievement_halfway
    AchievementId.CHAPTER_IMPULSE -> R.string.achievement_chapter_impulse
    AchievementId.CHAPTER_MOMENTUM -> R.string.achievement_chapter_momentum
    AchievementId.CHAPTER_BOOST -> R.string.achievement_chapter_boost
    AchievementId.CHAPTER_CONTROL -> R.string.achievement_chapter_control
    AchievementId.CHAPTER_RESONANCE -> R.string.achievement_chapter_resonance
    AchievementId.CHAPTER_CHAOS -> R.string.achievement_chapter_chaos
    AchievementId.MASTER -> R.string.achievement_master
    AchievementId.PERFECT_CHAIN -> R.string.achievement_perfect_chain
    AchievementId.UNSTOPPABLE -> R.string.achievement_unstoppable
    AchievementId.FLAWLESS_CHAPTER -> R.string.achievement_flawless_chapter
    AchievementId.PERFECTIONIST -> R.string.achievement_perfectionist
    AchievementId.DEEP_IMPACT -> R.string.achievement_deep_impact
    AchievementId.EVENT_HORIZON -> R.string.achievement_event_horizon
    AchievementId.CHAIN_REACTION -> R.string.achievement_chain_reaction
    AchievementId.RESONANCE_CHAIN -> R.string.achievement_resonance_chain
    AchievementId.PARTICLE_STORM -> R.string.achievement_particle_storm
    AchievementId.VETERAN -> R.string.achievement_veteran
    AchievementId.CONSISTENT -> R.string.achievement_consistent
    AchievementId.DAILY_IMPULSE -> R.string.achievement_daily_impulse
    AchievementId.DAILY_WEEK -> R.string.achievement_daily_week
    AchievementId.ENDLESS_FIVE -> R.string.achievement_endless_five
    AchievementId.ENDLESS_TEN -> R.string.achievement_endless_ten
}
