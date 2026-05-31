package com.t2h.ocr.auth

import android.content.Context
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

/**
 * Requests Drive file scope right after Google auth so first upload won't need a delayed consent flow.
 */
fun requestDriveFileScope(
    context: Context,
    onNeedsResolution: (IntentSenderRequest) -> Unit,
    onAuthorizedOrSkipped: () -> Unit,
    onFailure: (Throwable) -> Unit,
) {
    val authRequest = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(DriveScopes.DRIVE_FILE)))
        .build()

    Identity.getAuthorizationClient(context)
        .authorize(authRequest)
        .addOnSuccessListener { result ->
            if (result.hasResolution()) {
                val pendingIntent = result.pendingIntent
                if (pendingIntent != null) {
                    onNeedsResolution(IntentSenderRequest.Builder(pendingIntent).build())
                } else {
                    onAuthorizedOrSkipped()
                }
            } else {
                onAuthorizedOrSkipped()
            }
        }
        .addOnFailureListener { error ->
            onFailure(error)
        }
}

