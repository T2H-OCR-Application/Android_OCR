package com.t2h.ocr.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.t2h.ocr.ui.screens.file.FileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TextFileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<FileUiState>(FileUiState.Empty)
    val uiState: StateFlow<FileUiState> = _uiState

    init {
        loadFiles()
    }

    fun loadFiles() {
        // TODO: load danh sách văn bản đã OCR
        _uiState.value = FileUiState.Empty
    }

    fun deleteSelected(ids: Set<String>) {
        // TODO: xóa các văn bản được chọn
    }
}
