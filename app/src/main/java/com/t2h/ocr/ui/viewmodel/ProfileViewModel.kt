package com.t2h.ocr.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseUser
import com.t2h.ocr.data.repository.GoogleAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GoogleAuthRepository(application)

    private val _currentUser = MutableStateFlow<FirebaseUser?>(repository.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    fun signOut() {
        repository.signOut()
        _currentUser.value = null
    }
}
