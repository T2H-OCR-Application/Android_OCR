package com.t2h.ocr.ui.screens.file

sealed class FileUiState {
    object Loading                              : FileUiState()
    object Empty                                : FileUiState()
    data class Success(val items: List<FileItem>) : FileUiState()
    data class Error(val message: String)       : FileUiState()
}
