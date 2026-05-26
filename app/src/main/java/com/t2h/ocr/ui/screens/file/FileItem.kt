package com.t2h.ocr.ui.screens.file

data class FileItem(
    val id          : String,
    val name        : String,
    val type        : FileType,
    val timeLabel   : String,   // ví dụ: "Hôm qua", "2 ngày trước"
    val thumbnailUrl: String? = null,   // null → hiện placeholder
)

enum class FileType { IMAGE, PDF }
