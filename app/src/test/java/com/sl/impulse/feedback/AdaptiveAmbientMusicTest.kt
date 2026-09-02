package com.sl.impulse.feedback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveAmbientMusicTest {
    @Test
    fun intensityStaysCalmAtShallowDepthAndSaturatesForDeepChains() {
        assertEquals(0f, ambientIntensityForDepth(0), 0f)
        assertEquals(0f, ambientIntensityForDepth(1), 0f)
        assertTrue(ambientIntensityForDepth(4) in 0.4f..0.5f)
        assertEquals(1f, ambientIntensityForDepth(8), 0f)
        assertEquals(1f, ambientIntensityForDepth(20), 0f)
    }
}
