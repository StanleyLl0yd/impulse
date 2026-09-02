package com.sl.impulse.feedback

internal fun ambientIntensityForDepth(chainDepth: Int): Float =
    ((chainDepth - 1).coerceAtLeast(0) / 7f).coerceIn(0f, 1f)
