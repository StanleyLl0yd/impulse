package com.sl.impulse.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.sp
import com.sl.impulse.R
import com.sl.impulse.feedback.MusicPreferences
import kotlinx.coroutines.launch

@Composable
internal fun MusicSettingRow() {
    val context = LocalContext.current.applicationContext
    val preferences = remember(context) { MusicPreferences(context) }
    val enabled by preferences.enabled.collectAsState(initial = true)
    val scope = rememberCoroutineScope()

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.settings_music),
            color = Color.White.copy(alpha = 0.86f),
            fontSize = 14.sp,
        )
        Switch(
            checked = enabled,
            onCheckedChange = { value -> scope.launch { preferences.setEnabled(value) } },
            modifier = Modifier.testTag("music-toggle"),
        )
    }
}
