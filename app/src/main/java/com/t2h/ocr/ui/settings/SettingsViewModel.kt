package com.t2h.ocr.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t2h.ocr.data.local.UserPreferences
import com.t2h.ocr.domain.observability.AnalyticsHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private var analyticsHelper: AnalyticsHelper? = null

    val syncWifiOnly: StateFlow<Boolean> = userPreferences.syncWifiOnly
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val clearCacheOnSync: StateFlow<Boolean> = userPreferences.clearCacheOnSync
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val geminiApiKey: StateFlow<String> = userPreferences.geminiApiKey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun initAnalytics(helper: AnalyticsHelper) {
        this.analyticsHelper = helper
    }

    fun setSyncWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setSyncWifiOnly(enabled)
            analyticsHelper?.logSettingsChange("sync_wifi_only", enabled.toString())
        }
    }

    fun setClearCacheOnSync(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setClearCacheOnSync(enabled)
            analyticsHelper?.logSettingsChange("clear_cache_on_sync", enabled.toString())
        }
    }

    fun saveGeminiApiKey(key: String) {
        viewModelScope.launch {
            userPreferences.setGeminiApiKey(key)
        }
    }

    fun clearLocalCache(): Boolean {
        analyticsHelper?.logUserInteraction("SettingsScreen", "clear_local_cache")
        return userPreferences.clearLocalCache()
    }
}
