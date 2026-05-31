package com.t2h.ocr.data

import android.content.Context
import com.t2h.ocr.data.local.SummaryJsonStorage
import com.t2h.ocr.data.local.SummaryStorage
import com.t2h.ocr.data.models.SummaryMetadata
import com.t2h.ocr.data.sync.DriveUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class SummaryRepository private constructor(context: Context) {

    private val storage: SummaryStorage = SummaryJsonStorage(context)
    private val driveUploader = DriveUploader(context)

    val summaries: Flow<List<SummaryMetadata>> = storage.summaries

    fun addSummary(summary: SummaryMetadata) = storage.addSummary(summary)

    fun updateSummary(summary: SummaryMetadata) = storage.updateSummary(summary)

    fun loadSummaries(): List<SummaryMetadata> = storage.loadSummaries()

    /** Xóa summary: xóa metadata, file PDF local, và file trên Drive nếu có */
    suspend fun deleteSummaryWithFiles(summary: SummaryMetadata) = withContext(Dispatchers.IO) {
        // 1. Xóa file PDF local
        if (summary.summaryPdfPath.isNotBlank()) {
            try { File(summary.summaryPdfPath).delete() } catch (e: Exception) { e.printStackTrace() }
        }
        // 2. Xóa trên Drive nếu đã sync
        if (summary.isSynced && !summary.remotePdfUrl.isNullOrBlank()) {
            driveUploader.deleteFile(summary.remotePdfUrl)
        }
        // 3. Xóa metadata
        storage.deleteSummary(summary.id)
    }

    companion object {
        @Volatile
        private var INSTANCE: SummaryRepository? = null

        fun getInstance(context: Context): SummaryRepository =
            INSTANCE ?: synchronized(this) {
                SummaryRepository(context.applicationContext).also { INSTANCE = it }
            }
    }
}
