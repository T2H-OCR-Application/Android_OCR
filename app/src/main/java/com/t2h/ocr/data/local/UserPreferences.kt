package com.t2h.ocr.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Manages user preferences using Jetpack DataStore.
 * Stores resource management toggles.
 */
class UserPreferences(private val context: Context) {

    companion object {
        val SYNC_WIFI_ONLY = booleanPreferencesKey("sync_wifi_only")
        val CLEAR_CACHE_ON_SYNC = booleanPreferencesKey("clear_cache_on_sync")
    }

    /**
     * Flow emitting the current 'Wi-Fi only sync' preference.
     */
    val syncWifiOnly: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SYNC_WIFI_ONLY] ?: true // Default to true
    }

    /**
     * Flow emitting the current 'Clear cache on sync' preference.
     */
    val clearCacheOnSync: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CLEAR_CACHE_ON_SYNC] ?: false // Default to false
    }

    /**
     * Updates the 'Wi-Fi only sync' preference.
     */
    suspend fun setSyncWifiOnly(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_WIFI_ONLY] = enabled
        }
    }

    /**
     * Updates the 'Clear cache on sync' preference.
     */
    suspend fun setClearCacheOnSync(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLEAR_CACHE_ON_SYNC] = enabled
        }
    }

    /**
     * Clears the application's internal files and cache directories.
     */
    fun clearLocalCache(): Boolean {
        return try {
            // Clear permanent files
            context.filesDir.listFiles()?.forEach { file ->
                file.deleteRecursively()
            }
            // Clear temporary cache
            context.cacheDir.listFiles()?.forEach { file ->
                file.deleteRecursively()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
