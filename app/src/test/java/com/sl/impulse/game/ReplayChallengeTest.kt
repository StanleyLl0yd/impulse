package com.sl.impulse.game

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReplayChallengeTest {
    @Test
    fun dailyChallengeIsStableForTheSameDate() {
        val date = LocalDate.of(2026, 9, 2)
        assertEquals(ReplayChallenges.daily(date), ReplayChallenges.daily(date))
        assertNotEquals(ReplayChallenges.daily(date).key, ReplayChallenges.daily(date.plusDays(1)).key)
    }

    @Test
    fun endlessRunVisitsEveryReplayFieldBeforeRepeating() {
        val seeds = (1..40).map { round -> ReplayChallenges.endless(123456L, round).level.seed }
        assertEquals(40, seeds.distinct().size)
        assertEquals(seeds.first(), ReplayChallenges.endless(123456L, 41).level.seed)
    }

    @Test
    fun endlessDifficultyTightensAcrossCycles() {
        val first = ReplayChallenges.endless(987654L, 1)
        val repeated = ReplayChallenges.endless(987654L, 41)
        assertEquals(first.level.seed, repeated.level.seed)
        assertTrue(repeated.level.requiredCount >= first.level.requiredCount)
    }

    @Test
    fun generatedReplayChallengesRemainValid() {
        repeat(200) { offset ->
            val daily = ReplayChallenges.daily(LocalDate.of(2026, 1, 1).plusDays(offset.toLong()))
            assertTrue(daily.level.requiredCount in 1 until daily.level.particleCount)
        }
        for (round in 1..200) {
            val endless = ReplayChallenges.endless(42L, round)
            assertTrue(endless.level.requiredCount in 1 until endless.level.particleCount)
            assertTrue(endless.level.particleMix.specialCount <= endless.level.particleCount)
        }
    }
}
