package com.t2h.ocr.data.models

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a single scanned page in a batch scanning session.
 */
@Serializable
data class ScannedPage(
    val id: String = UUID.randomUUID().toString(),
    val imagePath: String,
    val text: String
)
