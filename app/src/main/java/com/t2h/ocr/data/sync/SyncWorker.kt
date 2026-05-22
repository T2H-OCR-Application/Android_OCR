package com.t2h.ocr.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.t2h.ocr.data.local.JsonStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.io.File

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val jsonStorage = JsonStorage(appContext)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val driveUploader = DriveUploader(appContext)

    override suspend fun doWork(): Result {
        val scanId = inputData.getString("scan_id") ?: return Result.failure()
        val userId = auth.currentUser?.uid ?: return Result.failure()

        // 1. Load scan from local storage
        val scans = jsonStorage.loadScans()
        val scan = scans.find { it.id == scanId } ?: return Result.failure()

        if (scan.isSynced) return Result.success()

        // 2. Set foreground for visible progress
        try {
            setForeground(createForegroundInfo())
        } catch (e: Exception) {
            // Ignore foreground failures if they happen
            e.printStackTrace()
        }

        return try {
            // 3. Upload PDF to Google Drive
            val pdfFile = File(scan.pdfPath)
            if (!pdfFile.exists()) return Result.failure()

            val downloadUrl = driveUploader.uploadPdf(pdfFile)
            if (downloadUrl == null) {
                // Upload failed or missing Google account
                return Result.retry()
            }

            // 4. Save to Firestore with LWW retry logic
            var success = false
            var attempts = 0
            val maxAttempts = 3
            var lastUpdatedScan = scan

            while (attempts < maxAttempts && !success) {
                try {
                    attempts++
                    // Update metadata with fresh timestamp for LWW
                    lastUpdatedScan = scan.copy(
                        isSynced = true,
                        remotePdfUrl = downloadUrl,
                        updatedAt = System.currentTimeMillis()
                    )

                    firestore.collection("users")
                        .document(userId)
                        .collection("scans")
                        .document(scanId)
                        .set(lastUpdatedScan)
                        .await()
                    
                    success = true
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED && attempts < maxAttempts) {
                        // This could be an LWW failure. Wait and retry with a newer timestamp.
                        delay(500)
                        continue
                    }
                    throw e
                }
            }

            if (!success) return Result.retry()

            // 5. Update local storage
            jsonStorage.updateScan(lastUpdatedScan)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val channelId = "sync_channel"
        val notificationId = 1
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Sync Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Showing progress of scan synchronization"
            }
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Syncing Scan")
            .setTicker("Syncing Scan")
            .setContentText("Uploading your document...")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(notificationId, notification)
        }
    }
}
