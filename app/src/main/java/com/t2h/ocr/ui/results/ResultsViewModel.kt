package com.t2h.ocr.ui.results

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.mlkit.vision.text.Text as VisionText
import com.t2h.ocr.data.ScanRepository
import com.t2h.ocr.data.local.UserPreferences
import com.t2h.ocr.data.models.ScanMetadata
import com.t2h.ocr.data.sync.SyncWorker
import com.t2h.ocr.domain.observability.AnalyticsHelper
import com.t2h.ocr.domain.ocr.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import java.util.concurrent.TimeUnit

class ResultsViewModel(private val scanRepository: ScanRepository) : ViewModel() {
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    private var analyticsHelper: AnalyticsHelper? = null

    fun initAnalytics(helper: AnalyticsHelper) {
        this.analyticsHelper = helper
    }

    fun clearError() {
        _errorState.value = null
    }

    fun saveBatchScan(
        context: Context,
        pages: List<String>,
        firstImagePath: String,
        allImagePaths: List<String>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.value = true
            _errorState.value = null
            val startTime = System.currentTimeMillis()
            try {
                val id = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis()
                
                // 1. Generate multi-page text PDF
                val pdfFile = File(context.filesDir, "doc_$id.pdf")
                FileOutputStream(pdfFile).use { out ->
                    PdfGenerator.generateTextPdf(pages, out)
                }

                // 2. Copy first image as thumbnail if exists
                var thumbnailPath = ""
                if (firstImagePath.isNotEmpty()) {
                    val firstImage = File(firstImagePath)
                    if (firstImage.exists()) {
                        val thumbnailFile = File(context.filesDir, "thumb_$id.jpg")
                        firstImage.copyTo(thumbnailFile, overwrite = true)
                        thumbnailPath = thumbnailFile.absolutePath
                    }
                }

                // 3. Persist metadata via Repository
                val metadata = ScanMetadata(
                    id = id,
                    title = "Batch Scan ${Date(timestamp)}",
                    timestamp = timestamp,
                    ocrText = pages.joinToString("\n\n"),
                    imagePath = thumbnailPath,
                    pdfPath = pdfFile.absolutePath,
                    language = "en"
                )
                scanRepository.addScan(metadata)

                // 4. Enqueue SyncWorker
                val userPrefs = UserPreferences(context)
                val isWifiOnly = userPrefs.syncWifiOnly.first()
                
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(if (isWifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                    .build()

                val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .addTag("sync")
                    .addTag("scan_$id")
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                    .setInputData(workDataOf("scan_id" to id))
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "sync_$id",
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )

                // 5. Cleanup all temporary images
                allImagePaths.forEach { path ->
                    try {
                        File(path).delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                analyticsHelper?.logOcrPerformance(
                    latencyMs = System.currentTimeMillis() - startTime,
                    pageCount = pages.size,
                    status = "success"
                )

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                analyticsHelper?.logOcrPerformance(
                    latencyMs = System.currentTimeMillis() - startTime,
                    pageCount = pages.size,
                    status = "error_${e.javaClass.simpleName}"
                )
                _errorState.value = "critical_sync_error"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun saveScan(
        context: Context,
        imagePath: String,
        recognizedText: VisionText,
        editedText: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.value = true
            val startTime = System.currentTimeMillis()
            try {
                val id = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis()
                
                val bitmap = BitmapFactory.decodeFile(imagePath) ?: return@launch

                val imageFile = File(context.filesDir, "image_$id.jpg")
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                val pdfFile = File(context.filesDir, "doc_$id.pdf")
                FileOutputStream(pdfFile).use { out ->
                    PdfGenerator.generateSearchablePdf(bitmap, recognizedText, editedText, out)
                }

                val metadata = ScanMetadata(
                    id = id,
                    title = "Scan ${Date(timestamp)}",
                    timestamp = timestamp,
                    ocrText = editedText,
                    imagePath = imageFile.absolutePath,
                    pdfPath = pdfFile.absolutePath,
                    language = "en"
                )
                scanRepository.addScan(metadata)

                val userPrefs = UserPreferences(context)
                val isWifiOnly = userPrefs.syncWifiOnly.first()

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(if (isWifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                    .build()

                val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .addTag("sync")
                    .addTag("scan_$id")
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                    .setInputData(workDataOf("scan_id" to id))
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "sync_$id",
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )

                bitmap.recycle()
                File(imagePath).delete()

                analyticsHelper?.logOcrPerformance(
                    latencyMs = System.currentTimeMillis() - startTime,
                    pageCount = 1,
                    status = "success"
                )

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                analyticsHelper?.logOcrPerformance(
                    latencyMs = System.currentTimeMillis() - startTime,
                    pageCount = 1,
                    status = "error_${e.javaClass.simpleName}"
                )
            } finally {
                _isSaving.value = false
            }
        }
    }
}
