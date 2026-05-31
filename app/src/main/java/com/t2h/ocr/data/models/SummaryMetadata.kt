package com.t2h.ocr.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SummaryMetadata(
    val id: String = "",
    val title: String = "",            // "[tên_gốc]_summary"
    val sourceScanId: String = "",     // ID của ScanMetadata gốc
    val sourcePdfPath: String = "",    // Đường dẫn PDF gốc
    val summaryPdfPath: String = "",   // Đường dẫn PDF tóm tắt
    val summaryText: String = "",      // Nội dung tóm tắt thuần
    val timestamp: Long = 0L,
    val language: String = "",         // Ngôn ngữ AI detect từ văn bản gốc
    val isSynced: Boolean = false,
    val remotePdfUrl: String? = null
)
