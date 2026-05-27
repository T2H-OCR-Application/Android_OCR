package com.t2h.ocr.domain.observability

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Unified interface for tracking performance and errors via Firebase Analytics.
 */
class AnalyticsHelper(context: Context) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    /**
     * Tracks OCR processing performance.
     */
    fun logOcrPerformance(latencyMs: Long, pageCount: Int, status: String) {
        val bundle = Bundle().apply {
            putLong("latency_ms", latencyMs)
            putInt("page_count", pageCount)
            putString("status", status)
        }
        firebaseAnalytics.logEvent("ocr_performance", bundle)
    }

    /**
     * Tracks synchronization status and latency.
     */
    fun logSyncStatus(scanId: String, attemptCount: Int, durationMs: Long, errorType: String?) {
        val bundle = Bundle().apply {
            putString("scan_id", scanId)
            putInt("attempt_count", attemptCount)
            putLong("duration_ms", durationMs)
            errorType?.let { putString("error_type", it) }
        }
        firebaseAnalytics.logEvent("sync_status", bundle)
    }

    /**
     * Tracks user interactions and screen views.
     */
    fun logUserInteraction(screenName: String, action: String, details: String? = null) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString("action", action)
            details?.let { putString("details", it) }
        }
        firebaseAnalytics.logEvent("user_interaction", bundle)
    }

    /**
     * Tracks changes to application settings.
     */
    fun logSettingsChange(settingName: String, value: String) {
        val bundle = Bundle().apply {
            putString("setting_name", settingName)
            putString("value", value)
        }
        firebaseAnalytics.logEvent("settings_change", bundle)
    }
}
