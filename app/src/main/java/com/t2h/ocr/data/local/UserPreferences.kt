package com.t2h.ocr.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    companion object {
        val SYNC_WIFI_ONLY = booleanPreferencesKey("sync_wifi_only")
        val CLEAR_CACHE_ON_SYNC = booleanPreferencesKey("clear_cache_on_sync")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
    }

    val syncWifiOnly: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SYNC_WIFI_ONLY] ?: true
    }

    val clearCacheOnSync: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[CLEAR_CACHE_ON_SYNC] ?: false
    }

    val geminiApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[GEMINI_API_KEY] ?: ""
    }

    suspend fun setSyncWifiOnly(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_WIFI_ONLY] = enabled
        }
    }

    suspend fun setClearCacheOnSync(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CLEAR_CACHE_ON_SYNC] = enabled
        }
    }

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[GEMINI_API_KEY] = key.trim()
        }
    }

    fun clearLocalCache(): Boolean {
        return try {
            context.filesDir.listFiles()?.forEach { file -> file.deleteRecursively() }
            context.cacheDir.listFiles()?.forEach { file -> file.deleteRecursively() }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
