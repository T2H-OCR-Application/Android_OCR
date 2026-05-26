package com.t2h.ocr.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.t2h.ocr.ui.screens.file.FileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<FileUiState>(FileUiState.Empty)
    val uiState: StateFlow<FileUiState> = _uiState

    init {
        loadFiles()
    }

    fun loadFiles() {
        // TODO: thay bằng Google Drive API sau
        _uiState.value = FileUiState.Empty
    }

    fun deleteSelected(ids: Set<String>) {
        // TODO: xóa trên Drive + cập nhật state
    }
}
