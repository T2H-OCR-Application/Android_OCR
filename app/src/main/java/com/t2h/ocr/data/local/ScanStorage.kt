package com.t2h.ocr.data.local

import com.t2h.ocr.data.models.ScanMetadata
import kotlinx.coroutines.flow.Flow

interface ScanStorage {
    val scans: Flow<List<ScanMetadata>>
    fun saveScans(scans: List<ScanMetadata>)
    fun loadScans(): List<ScanMetadata>
    fun addScan(scan: ScanMetadata)
    fun updateScan(updatedScan: ScanMetadata)
    fun deleteScan(scanId: String)
}
