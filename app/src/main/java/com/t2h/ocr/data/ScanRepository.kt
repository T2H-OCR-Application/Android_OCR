package com.t2h.ocr.data

import android.content.Context
import com.t2h.ocr.data.local.JsonStorage
import com.t2h.ocr.data.local.ScanStorage
import com.t2h.ocr.data.models.ScanMetadata
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for scan data.
 * Ensures all view models share the same reactive state.
 */
class ScanRepository private constructor(context: Context) {
    private val storage: ScanStorage = JsonStorage(context)

    val scans: Flow<List<ScanMetadata>> = storage.scans

    fun addScan(scan: ScanMetadata) {
        storage.addScan(scan)
    }

    fun updateScan(scan: ScanMetadata) {
        storage.updateScan(scan)
    }

    fun deleteScan(scanId: String) {
        storage.deleteScan(scanId)
    }

    fun loadScans(): List<ScanMetadata> {
        return storage.loadScans()
    }

    companion object {
        @Volatile
        private var INSTANCE: ScanRepository? = null

        fun getInstance(context: Context): ScanRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ScanRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
