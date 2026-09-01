package com.sl.impulse.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val Background = Color(0xFF050814)
internal val PanelBackground = Color(0xFF0B1022)
internal val ParticleCore = Color(0xFFB6FCFF)
internal val ParticleGlow = Color(0xFF00E5FF)
internal val TriggeredCore = Color(0xFFFFC4FF)
internal val TriggeredGlow = Color(0xFFB25CFF)
internal val NearMiss = Color(0xFFFFC857)

@Composable
internal fun ImpulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = ParticleGlow,
            secondary = TriggeredGlow,
            background = Background,
            surface = PanelBackground,
            onPrimary = Background,
            onSurface = Color.White,
        ),
        content = content,
    )
}
