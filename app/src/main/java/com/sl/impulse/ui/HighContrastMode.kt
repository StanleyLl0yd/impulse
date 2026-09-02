package com.sl.impulse.ui

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.visualDataStore by preferencesDataStore(name = "visual_settings")

internal object HighContrastMode {
    private val initialized = AtomicBoolean(false)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val state = mutableStateOf(false)
    private var applicationContext: Context? = null

    val enabled: State<Boolean> = state

    fun initialize(context: Context) {
        if (!initialized.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        applicationContext = appContext
        scope.launch {
            appContext.visualDataStore.data
                .catch { error ->
                    if (error is IOException) emit(emptyPreferences()) else throw error
                }
                .map { preferences -> preferences[HIGH_CONTRAST] ?: false }
                .collect { value -> state.value = value }
        }
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        initialize(context)
        state.value = enabled
        val appContext = applicationContext ?: context.applicationContext
        scope.launch {
            appContext.visualDataStore.edit { preferences -> preferences[HIGH_CONTRAST] = enabled }
        }
    }

    private val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
}
