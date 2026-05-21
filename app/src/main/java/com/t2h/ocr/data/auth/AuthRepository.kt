package com.t2h.ocr.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun signInAnonymously(onComplete: (Boolean) -> Unit = {}) {
        if (auth.currentUser != null) {
            Log.d(TAG, "User already signed in: ${auth.currentUser?.uid}")
            onComplete(true)
            return
        }

        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInAnonymously:success")
                    onComplete(true)
                } else {
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                    onComplete(false)
                }
            }
    }

    fun getUid(): String? = auth.currentUser?.uid

    companion object {
        private const val TAG = "AuthRepository"
    }
}
