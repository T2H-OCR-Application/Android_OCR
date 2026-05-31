package com.t2h.ocr.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.t2h.ocr.R
import com.t2h.ocr.data.ScanRepository
import com.t2h.ocr.domain.observability.AnalyticsHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.io.File

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val scanRepository = ScanRepository.getInstance(appContext)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val driveUploader = DriveUploader(appContext)
    private val analyticsHelper = AnalyticsHelper(appContext)
    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val TAG = "DEBUG_OCR"

    override suspend fun doWork(): Result {
        val scanId = inputData.getString("scan_id") ?: return Result.failure()
        val userId = auth.currentUser?.uid ?: return Result.failure()
        val notificationId = scanId.hashCode()
        val startTime = System.currentTimeMillis()
        val attemptCount = runAttemptCount + 1

        Log.e(TAG, "SyncWorker: [START] id=$scanId, attempt=$attemptCount")

        // 1. Load scan
        val scans = scanRepository.loadScans()
        val scan = scans.find { it.id == scanId } ?: return Result.failure()
        if (scan.isSynced) return Result.success()

        // 2. Set foreground IMMEDIATELY to ensure visibility
        val builder = createBaseNotificationBuilder(applicationContext.getString(R.string.sync_notification_connecting))
        setForeground(createForegroundInfo(notificationId, builder))

        return try {
            // 3. Check for linked Google account (works with both legacy GoogleSignIn and Credential Manager)
            val hasGoogleAccount = GoogleSignIn.getLastSignedInAccount(applicationContext) != null ||
                auth.currentUser?.providerData?.any { it.providerId == "google.com" } == true
            if (!hasGoogleAccount) {
                Log.e(TAG, "SyncWorker: [FAIL] No Google Account")
                analyticsHelper.logSyncStatus(scanId, attemptCount, System.currentTimeMillis() - startTime, "no_google_account")
                return Result.failure()
            }

            // 4. Upload PDF to Drive
            val pdfFile = File(scan.pdfPath)
            if (!pdfFile.exists()) {
                analyticsHelper.logSyncStatus(scanId, attemptCount, System.currentTimeMillis() - startTime, "file_not_found")
                return Result.failure()
            }

            builder.setContentText(applicationContext.getString(R.string.sync_notification_uploading))
            notificationManager.notify(notificationId, builder.build())
            
            // Artificial delay to ensure user sees the "Syncing" state
            delay(1000)

            val result = driveUploader.uploadPdf(pdfFile)
            val downloadUrl = when (result) {
                is UploadResult.Success -> result.downloadUrl
                is UploadResult.NeedsConsent -> {
                    Log.e(TAG, "SyncWorker: [FAIL] Consent needed")
                    val pendingIntent = android.app.PendingIntent.getActivity(
                        applicationContext,
                        0,
                        result.intent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                    )
                    
                    val consentNotification = NotificationCompat.Builder(applicationContext, "sync_channel")
                        .setContentTitle(applicationContext.getString(R.string.sync_notification_consent_title))
                        .setContentText(applicationContext.getString(R.string.sync_notification_consent_text))
                        .setSmallIcon(R.drawable.google_icon)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()
                    
                    notificationManager.notify(notificationId + 10, consentNotification)
                    return Result.failure()
                }
                is UploadResult.Error -> {
                    Log.e(TAG, "SyncWorker: [RETRY] Drive upload failed: ${result.message}")
                    analyticsHelper.logSyncStatus(scanId, attemptCount, System.currentTimeMillis() - startTime, "drive_upload_failed")
                    return Result.retry()
                }
            }

            Log.e(TAG, "SyncWorker: [STEP] Drive upload SUCCESS")

            // 5. IMPORTANT: Update local repo first with the Drive URL
            var updatedScan = scan.copy(
                remotePdfUrl = downloadUrl,
                updatedAt = System.currentTimeMillis()
            )
            scanRepository.updateScan(updatedScan)

            // 6. Save to Firestore
            builder.setContentText(applicationContext.getString(R.string.sync_notification_saving))
            builder.setSmallIcon(android.R.drawable.stat_sys_download_done)
            notificationManager.notify(notificationId, builder.build())

            try {
                Log.e(TAG, "SyncWorker: [STEP] Starting Firestore write (5s timeout)...")
                
                withTimeout(5000) {
                    val finalScan = updatedScan.copy(isSynced = true)
                    firestore.collection("users")
                        .document(userId)
                        .collection("scans")
                        .document(scanId)
                        .set(finalScan)
                        .await()
                    
                    scanRepository.updateScan(finalScan)
                }
                Log.e(TAG, "SyncWorker: [STEP] Firestore write SUCCESS")
            } catch (e: Exception) {
                Log.e(TAG, "SyncWorker: [WARN] Firestore write timed out, but file is on Drive. Will retry metadata later.", e)
            }

            // 7. Success tracking
            analyticsHelper.logSyncStatus(scanId, attemptCount, System.currentTimeMillis() - startTime, null)

            // 8. Post final notification
            val successNotification = NotificationCompat.Builder(applicationContext, "sync_channel")
                .setContentTitle(applicationContext.getString(R.string.sync_notification_complete_title))
                .setContentText(applicationContext.getString(R.string.sync_notification_complete_text))
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .build()
            
            notificationManager.notify(notificationId + 5, successNotification)
            notificationManager.cancel(notificationId)

            Log.e(TAG, "SyncWorker: [DONE] $scanId")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "SyncWorker: [CRITICAL] Unexpected error", e)
            analyticsHelper.logSyncStatus(scanId, attemptCount, System.currentTimeMillis() - startTime, e.javaClass.simpleName)
            Result.retry()
        }
    }

    private fun createBaseNotificationBuilder(message: String): NotificationCompat.Builder {
        val channelId = "sync_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, applicationContext.getString(R.string.sync_notification_channel_name), NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(applicationContext.getString(R.string.sync_notification_title))
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
    }

    private fun createForegroundInfo(notificationId: Int, builder: NotificationCompat.Builder): ForegroundInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, builder.build())
        }
    }
}
