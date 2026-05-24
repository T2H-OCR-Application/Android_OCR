package com.t2h.ocr.ui.screens.auth

sealed class AuthUiState(){
    object Idle: AuthUiState()
    object Loading: AuthUiState()
    object Success: AuthUiState()
    data class Error(val message: String): AuthUiState()
}
