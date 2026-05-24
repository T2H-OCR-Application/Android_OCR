package com.t2h.ocr.data.local

import android.content.Context
import androidx.core.util.AtomicFile
import com.t2h.ocr.data.models.ScanMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

/**
 * Handles local persistence of scan metadata using JSON serialization.
 */
open class JsonStorage(private val filesDir: File) : ScanStorage {
    constructor(context: Context) : this(context.filesDir)

    private val fileName = "scans.json"
    private val file = File(filesDir, fileName)
    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true 
    }

    private val _scansFlow = MutableStateFlow<List<ScanMetadata>>(emptyList())
    override val scans = _scansFlow.asStateFlow()

    init {
        _scansFlow.value = loadScans()
    }

    /**
     * Saves the entire list of scans to the local JSON file atomically.
     */
    override fun saveScans(scans: List<ScanMetadata>) {
        val atomicFile = AtomicFile(file)
        var stream: FileOutputStream? = null
        try {
            val jsonString = json.encodeToString(scans)
            stream = atomicFile.startWrite()
            stream?.write(jsonString.toByteArray())
            atomicFile.finishWrite(stream)
            _scansFlow.value = scans
        } catch (e: Exception) {
            atomicFile.failWrite(stream)
            // In a real app, we might want to log this to a crash reporting tool
            e.printStackTrace()
        }
    }

    /**
     * Loads the list of scans from the local JSON file.
     * Returns an empty list if the file doesn't exist or is invalid.
     */
    override fun loadScans(): List<ScanMetadata> {
        if (!file.exists()) return emptyList()
        return try {
            val jsonString = file.readText()
            json.decodeFromString<List<ScanMetadata>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Adds a single scan to the storage.
     */
    override fun addScan(scan: ScanMetadata) {
        val currentScans = loadScans().toMutableList()
        currentScans.add(0, scan) // Newest first
        saveScans(currentScans)
    }

    /**
     * Updates an existing scan in the storage.
     */
    override fun updateScan(updatedScan: ScanMetadata) {
        val currentScans = loadScans().map {
            if (it.id == updatedScan.id) updatedScan else it
        }
        saveScans(currentScans)
    }

    /**
     * Deletes a single scan from storage.
     */
    override fun deleteScan(scanId: String) {
        val currentScans = loadScans().filter { it.id != scanId }
        saveScans(currentScans)
    }
}
