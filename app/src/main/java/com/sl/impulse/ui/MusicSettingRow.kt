package com.sl.impulse.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sl.impulse.R
import com.sl.impulse.feedback.MusicPreferences
import kotlinx.coroutines.launch

@Composable
internal fun MusicSettingRow() {
    val context = LocalContext.current.applicationContext
    val preferences = remember(context) { MusicPreferences(context) }
    val musicEnabled by preferences.enabled.collectAsState(initial = true)
    val highContrast = HighContrastMode.enabled.value
    val scope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ToggleRow(
            label = stringResource(R.string.settings_music),
            checked = musicEnabled,
            onCheckedChange = { value -> scope.launch { preferences.setEnabled(value) } },
            testTag = "music-toggle",
        )
        ToggleRow(
            label = stringResource(R.string.settings_high_contrast),
            checked = highContrast,
            onCheckedChange = { value -> HighContrastMode.setEnabled(context, value) },
            testTag = "high-contrast-toggle",
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.86f),
            fontSize = 14.sp,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
        )
    }
}
