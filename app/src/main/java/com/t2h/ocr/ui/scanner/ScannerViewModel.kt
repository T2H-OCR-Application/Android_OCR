package com.t2h.ocr.ui.scanner

import androidx.lifecycle.ViewModel
import com.t2h.ocr.data.models.ScannedPage
import com.t2h.ocr.domain.observability.AnalyticsHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.opencv.core.Point
import java.io.File

class ScannerViewModel : ViewModel() {

    private val _scannedPages = MutableStateFlow<List<ScannedPage>>(emptyList())
    val scannedPages = _scannedPages.asStateFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState = _errorState.asStateFlow()

    private var analyticsHelper: AnalyticsHelper? = null

    fun setCameraError() {
        _errorState.value = "camera_failure"
    }

    fun clearError() {
        _errorState.value = null
    }

    fun initAnalytics(helper: AnalyticsHelper) {
        this.analyticsHelper = helper
    }

    fun addPage(page: ScannedPage) {
        _scannedPages.value = _scannedPages.value + page
    }

    fun removePage(pageId: String) {
        val pageToRemove = _scannedPages.value.find { it.id == pageId }
        if (pageToRemove != null) {
            try {
                File(pageToRemove.imagePath).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _scannedPages.value = _scannedPages.value.filter { it.id != pageId }
    }

    fun clearPages() {
        _scannedPages.value.forEach { page ->
            try {
                File(page.imagePath).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _scannedPages.value = emptyList()
    }

    fun logOcrPerformance(latencyMs: Long, pageCount: Int, status: String) {
        analyticsHelper?.logOcrPerformance(latencyMs, pageCount, status)
    }
}
