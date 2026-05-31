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

    private val rootFolderName = "Android OCR Scans"
    private val summaryFolderName = "Summaries"

    private fun buildDriveService(): Drive? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account ?: return null
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("Android OCR").build()
    }

    /** Tìm hoặc tạo folder theo tên, trong parentId (null = root My Drive) */
    private fun getOrCreateFolder(driveService: Drive, name: String, parentId: String?): String {
        val parentClause = if (parentId != null) " and '$parentId' in parents" else ""
        val query = "mimeType='application/vnd.google-apps.folder' and name='$name' and trashed=false$parentClause"
        val list = driveService.files().list().setQ(query).setSpaces("drive").execute()
        if (list.files.isNotEmpty()) return list.files[0].id

        val meta = com.google.api.services.drive.model.File().apply {
            this.name = name
            this.mimeType = "application/vnd.google-apps.folder"
            if (parentId != null) parents = listOf(parentId)
        }
        return driveService.files().create(meta).setFields("id").execute().id
    }

    /** Upload PDF scan vào folder gốc "Android OCR Scans" */
    suspend fun uploadPdf(file: File, mimeType: String = "application/pdf"): UploadResult =
        withContext(Dispatchers.IO) {
            val driveService = buildDriveService()
                ?: return@withContext UploadResult.Error("No Google account signed in")
            try {
                val folderId = getOrCreateFolder(driveService, rootFolderName, null)
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = file.name
                    parents = listOf(folderId)
                }
                val uploaded = driveService.files()
                    .create(fileMetadata, FileContent(mimeType, file))
                    .setFields("id, webViewLink")
                    .execute()
                UploadResult.Success(uploaded.webViewLink)
            } catch (e: UserRecoverableAuthIOException) {
                UploadResult.NeedsConsent(e.intent)
            } catch (e: Exception) {
                e.printStackTrace()
                UploadResult.Error(e.message ?: "Unknown error")
            }
        }

    /** Upload PDF summary vào subfolder "Android OCR Scans/Summaries" */
    suspend fun uploadSummaryPdf(file: File, mimeType: String = "application/pdf"): UploadResult =
        withContext(Dispatchers.IO) {
            val driveService = buildDriveService()
                ?: return@withContext UploadResult.Error("No Google account signed in")
            try {
                val rootId = getOrCreateFolder(driveService, rootFolderName, null)
                val summaryFolderId = getOrCreateFolder(driveService, summaryFolderName, rootId)
                val fileMetadata = com.google.api.services.drive.model.File().apply {
                    name = file.name
                    parents = listOf(summaryFolderId)
                }
                val uploaded = driveService.files()
                    .create(fileMetadata, FileContent(mimeType, file))
                    .setFields("id, webViewLink")
                    .execute()
                UploadResult.Success(uploaded.webViewLink)
            } catch (e: UserRecoverableAuthIOException) {
                UploadResult.NeedsConsent(e.intent)
            } catch (e: Exception) {
                e.printStackTrace()
                UploadResult.Error(e.message ?: "Unknown error")
            }
        }

    /** Xóa file trên Drive theo webViewLink (trích file ID từ URL) */
    suspend fun deleteFile(webViewLink: String): Boolean = withContext(Dispatchers.IO) {
        val driveService = buildDriveService() ?: return@withContext false
        return@withContext try {
            // webViewLink dạng: https://drive.google.com/file/d/FILE_ID/view?usp=drivesdk
            val fileId = webViewLink
                .substringAfter("/file/d/")
                .substringBefore("/")
                .substringBefore("?")
            if (fileId.isBlank()) return@withContext false
            driveService.files().delete(fileId).execute()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
