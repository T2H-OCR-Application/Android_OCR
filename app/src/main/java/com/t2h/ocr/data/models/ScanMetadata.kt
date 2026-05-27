package com.t2h.ocr.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ScanMetadata(
    val id: String = "",
    val title: String = "",
    val timestamp: Long = 0L,
    val ocrText: String = "",
    val imagePath: String = "",
    val pdfPath: String = "",
    val language: String = "",
    val isSynced: Boolean = false,
    val remotePdfUrl: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
