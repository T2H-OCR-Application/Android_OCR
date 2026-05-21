package com.t2h.ocr.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.t2h.ocr.data.local.JsonStorage
import kotlinx.coroutines.tasks.await
import java.io.File

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val jsonStorage = JsonStorage(appContext)
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

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
            // 3. Upload PDF to Storage
            val pdfFile = File(scan.pdfPath)
            if (!pdfFile.exists()) return Result.failure()

            val storageRef = storage.reference.child("users/$userId/scans/$scanId.pdf")
            storageRef.putFile(Uri.fromFile(pdfFile)).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // 4. Update metadata
            val updatedScan = scan.copy(
                isSynced = true,
                remotePdfUrl = downloadUrl,
                updatedAt = System.currentTimeMillis()
            )

            // 5. Save to Firestore
            firestore.collection("users")
                .document(userId)
                .collection("scans")
                .document(scanId)
                .set(updatedScan)
                .await()

            // 6. Update local storage
            jsonStorage.updateScan(updatedScan)

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

        return ForegroundInfo(notificationId, notification)
    }
}
