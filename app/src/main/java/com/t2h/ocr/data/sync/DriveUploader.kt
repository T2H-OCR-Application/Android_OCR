package com.t2h.ocr.data.sync

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

sealed class UploadResult {
    data class Success(val downloadUrl: String) : UploadResult()
    data class Error(val message: String) : UploadResult()
    data class NeedsConsent(val intent: Intent) : UploadResult()
}

class DriveUploader(private val context: Context) {

    suspend fun uploadPdf(file: File, mimeType: String = "application/pdf"): UploadResult = withContext(Dispatchers.IO) {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext UploadResult.Error("No Google account signed in")
        
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account ?: return@withContext UploadResult.Error("No account selected")

        val driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
        .setApplicationName("Android OCR")
        .build()

        // 1. Check if folder exists
        val folderName = "Android OCR Scans"
        var folderId: String? = null
        val query = "mimeType='application/vnd.google-apps.folder' and name='$folderName' and trashed=false"
        
        try {
            val fileList = driveService.files().list().setQ(query).setSpaces("drive").execute()

            if (fileList.files.isNotEmpty()) {
                folderId = fileList.files[0].id
            } else {
                // Create folder
                val folderMetadata = com.google.api.services.drive.model.File().apply {
                    name = folderName
                    this.mimeType = "application/vnd.google-apps.folder"
                }
                val folder = driveService.files().create(folderMetadata).setFields("id").execute()
                folderId = folder.id
            }

            // 2. Upload file to folder
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = file.name
                parents = listOf(folderId)
            }
            
            val fileContent = FileContent(mimeType, file)
            val uploadedFile = driveService.files().create(fileMetadata, fileContent)
                .setFields("id, webViewLink")
                .execute()

            return@withContext UploadResult.Success(uploadedFile.webViewLink)
        } catch (e: UserRecoverableAuthIOException) {
            return@withContext UploadResult.NeedsConsent(e.intent)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext UploadResult.Error(e.message ?: "Unknown error")
        }
    }
}
