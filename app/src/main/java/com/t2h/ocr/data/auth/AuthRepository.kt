package com.t2h.ocr.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
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

    fun linkWithGoogle(idToken: String, onComplete: (Boolean) -> Unit = {}) {
        val user = auth.currentUser
        if (user == null) {
            Log.w(TAG, "linkWithGoogle: No user logged in")
            onComplete(false)
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        user.linkWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "linkWithCredential:success")
                    onComplete(true)
                } else {
                    Log.w(TAG, "linkWithCredential:failure", task.exception)
                    onComplete(false)
                }
            }
    }

    fun signInWithGoogle(idToken: String, onComplete: (Boolean) -> Unit = {}) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithGoogle:success")
                    onComplete(true)
                } else {
                    Log.w(TAG, "signInWithGoogle:failure", task.exception)
                    onComplete(false)
                }
            }
    }

    fun getUid(): String? = auth.currentUser?.uid

    companion object {
        private const val TAG = "AuthRepository"
    }
}
