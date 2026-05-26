package com.t2h.ocr.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.t2h.ocr.data.repository.GoogleAuthRepository
import com.t2h.ocr.ui.screens.auth.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GoogleAuthRepository(application)

    private val _googleAuthState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val googleAuthState: StateFlow<AuthUiState> = _googleAuthState

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _googleAuthState.value = AuthUiState.Loading
            val result = repository.signInWithGoogle(activityContext)
            _googleAuthState.value = if (result.isSuccess) {
                AuthUiState.Success
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Đăng ký thất bại"
                AuthUiState.Error(msg)
            }
        }
    }

    fun resetState() {
        _googleAuthState.value = AuthUiState.Idle
    }
}
