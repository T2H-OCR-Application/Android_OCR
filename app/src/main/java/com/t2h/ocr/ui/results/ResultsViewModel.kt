package com.t2h.ocr.ui.results

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.google.mlkit.vision.text.Text as VisionText
import com.t2h.ocr.data.local.JsonStorage
import com.t2h.ocr.data.models.ScanMetadata
import com.t2h.ocr.data.sync.SyncWorker
import com.t2h.ocr.domain.ocr.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

class ResultsViewModel : ViewModel() {
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    fun saveScan(
        context: Context,
        imagePath: String,
        recognizedText: VisionText,
        editedText: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isSaving.value = true
            try {
                val id = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis()
                
                // 1. Load the original captured bitmap
                val bitmap = BitmapFactory.decodeFile(imagePath) ?: return@launch

                // 2. Save Image to app-private storage (permanent location)
                val imageFile = File(context.filesDir, "image_$id.jpg")
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                // 3. Generate searchable PDF
                // Note: We use the original recognizedText for positions, 
                // but we SHOULD ideally use editedText for content.
                // For Phase 1, we will pass editedText to a revised PdfGenerator.
                val pdfFile = File(context.filesDir, "doc_$id.pdf")
                FileOutputStream(pdfFile).use { out ->
                    PdfGenerator.generateSearchablePdf(bitmap, recognizedText, editedText, out)
                }

                // 4. Persist metadata locally
                val jsonStorage = JsonStorage(context)
                val metadata = ScanMetadata(
                    id = id,
                    title = "Scan ${Date(timestamp)}",
                    timestamp = timestamp,
                    ocrText = editedText,
                    imagePath = imageFile.absolutePath,
                    pdfPath = pdfFile.absolutePath,
                    language = "en"
                )
                jsonStorage.addScan(metadata)

                // 5. Enqueue SyncWorker
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .setInputData(workDataOf("scan_id" to id))
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "sync_$id",
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )

                // Cleanup: recycle bitmap and delete temp file
                bitmap.recycle()
                File(imagePath).delete()

                withContext(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }
}
