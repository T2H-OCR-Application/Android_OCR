package com.t2h.ocr.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun signInWithGoogle(activityContext: Context): Result<FirebaseUser> {
        return try {
            // 1. Tạo request Google ID token
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // false = cho phép chọn tài khoản mới
                .setServerClientId(WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            // 2. Mở bottom sheet chọn tài khoản Google
            val result = credentialManager.getCredential(
                request = request,
                context = activityContext  // phải là Activity context
            )

            // 3. Lấy ID token từ kết quả
            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdToken = GoogleIdTokenCredential
                    .createFrom(credential.data)
                    .idToken

                // 4. Xác thực với Firebase
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                Result.success(authResult.user!!)
            } else {
                Result.failure(Exception("Credential không hợp lệ"))
            }
        } catch (e: GetCredentialException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    companion object {
        // Lấy từ Firebase Console → Project Settings → Web API Key
        // hoặc từ google-services.json → oauth_client → client_type: 3 → client_id
        private const val WEB_CLIENT_ID =
            "759174019118-voeqf7jsh7o6brp5rk6cen1bdsvidpvn.apps.googleusercontent.com"

    }
}