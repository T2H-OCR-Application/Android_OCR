package com.t2h.ocr.ui.summary

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t2h.ocr.data.ScanRepository
import com.t2h.ocr.data.SummaryRepository
import com.t2h.ocr.data.models.ScanMetadata
import com.t2h.ocr.data.models.SummaryMetadata
import com.t2h.ocr.data.sync.DriveUploader
import com.t2h.ocr.data.sync.UploadResult
import com.t2h.ocr.domain.ocr.PdfGenerator
import com.t2h.ocr.domain.summary.PdfSummaryService
import com.t2h.ocr.domain.summary.SummaryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

sealed class SummaryUiState {
    object SelectingScan : SummaryUiState()
    data class Summarizing(val progress: String = "Đang tóm tắt...") : SummaryUiState()
    /** Bước 3: cho phép chỉnh sửa trước khi lưu */
    data class Editing(
        val summaryText: String,
        val sourceScan: ScanMetadata,
        val detectedLanguage: String
    ) : SummaryUiState()
    object Saving : SummaryUiState()
    data class Saved(val summaryTitle: String) : SummaryUiState()
    data class Error(val message: String) : SummaryUiState()
}

class SummaryViewModel(
    private val scanRepository: ScanRepository,
    private val summaryRepository: SummaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SummaryUiState>(SummaryUiState.SelectingScan)
    val uiState: StateFlow<SummaryUiState> = _uiState

    /** Bắt đầu tóm tắt — nhận context để kiểm tra mạng và gọi API */
    fun startSummary(context: Context, scan: ScanMetadata) {
        viewModelScope.launch {
            _uiState.value = SummaryUiState.Summarizing()
            // PdfSummaryService.summarize đã dùng withContext(IO) bên trong
            val result = PdfSummaryService.summarize(context, scan)
            _uiState.value = when (result) {
                is SummaryState.Done -> SummaryUiState.Editing(
                    summaryText = result.summaryText,
                    sourceScan = scan,
                    detectedLanguage = result.detectedLanguage
                )
                is SummaryState.Error -> SummaryUiState.Error(result.message)
                else -> SummaryUiState.Error("Lỗi không xác định.")
            }
        }
    }

    /** Lưu kết quả tóm tắt (có thể đã được chỉnh sửa) thành PDF + upload Drive */
    fun saveSummary(
        context: Context,
        summaryText: String,
        sourceScan: ScanMetadata,
        detectedLanguage: String
    ) {
        viewModelScope.launch {
            _uiState.value = SummaryUiState.Saving
            try {
                val id = UUID.randomUUID().toString()
                val title = buildTitle(sourceScan.title)

                // 1. Tạo PDF trên IO thread
                val pdfFile = withContext(Dispatchers.IO) {
                    val file = File(context.filesDir, "summary_$id.pdf")
                    FileOutputStream(file).use { out ->
                        PdfGenerator.generateTextPdf(listOf(summaryText), out)
                    }
                    file
                }

                // 2. Lưu metadata ban đầu (chưa sync)
                val metadata = SummaryMetadata(
                    id = id,
                    title = title,
                    sourceScanId = sourceScan.id,
                    sourcePdfPath = sourceScan.pdfPath,
                    summaryPdfPath = pdfFile.absolutePath,
                    summaryText = summaryText,
                    timestamp = System.currentTimeMillis(),
                    language = detectedLanguage,
                    isSynced = false,
                    remotePdfUrl = null
                )
                summaryRepository.addSummary(metadata)

                // 3. Upload lên Drive subfolder Summaries trên IO thread
                val uploadResult = withContext(Dispatchers.IO) {
                    DriveUploader(context).uploadSummaryPdf(pdfFile)
                }
                if (uploadResult is UploadResult.Success) {
                    summaryRepository.updateSummary(
                        metadata.copy(isSynced = true, remotePdfUrl = uploadResult.downloadUrl)
                    )
                }

                _uiState.value = SummaryUiState.Saved(title)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = SummaryUiState.Error("Không thể lưu PDF: ${e.message}")
            }
        }
    }

    fun reset() {
        _uiState.value = SummaryUiState.SelectingScan
    }

    private fun buildTitle(sourceTitle: String): String {
        val base = sourceTitle.ifBlank { "Tài liệu" }
        return "${base}_summary"
    }
}
