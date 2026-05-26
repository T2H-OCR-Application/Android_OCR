package com.t2h.ocr.ui.screens.tools

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Màn hình Công cụ.
 * TODO: Implement danh sách công cụ OCR, chỉnh sửa, xuất file...
 */
@Composable
fun ToolsScreen() {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Màn hình Công cụ", color = Color.White)
    }
}
