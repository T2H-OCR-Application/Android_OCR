package com.t2h.ocr.data.local

import android.content.Context
import com.t2h.ocr.data.models.ScanMetadata
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Handles local persistence of scan metadata using JSON serialization.
 */
class JsonStorage(private val context: Context) {
    private val fileName = "scans.json"
    private val file = File(context.filesDir, fileName)
    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true 
    }

    /**
     * Saves the entire list of scans to the local JSON file.
     */
    fun saveScans(scans: List<ScanMetadata>) {
        try {
            val jsonString = json.encodeToString(scans)
            file.writeText(jsonString)
        } catch (e: Exception) {
            // In a real app, we might want to log this to a crash reporting tool
            e.printStackTrace()
        }
    }

    /**
     * Loads the list of scans from the local JSON file.
     * Returns an empty list if the file doesn't exist or is invalid.
     */
    fun loadScans(): List<ScanMetadata> {
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
    fun addScan(scan: ScanMetadata) {
        val currentScans = loadScans().toMutableList()
        currentScans.add(0, scan) // Newest first
        saveScans(currentScans)
    }

    /**
     * Updates an existing scan in the storage.
     */
    fun updateScan(updatedScan: ScanMetadata) {
        val currentScans = loadScans().map {
            if (it.id == updatedScan.id) updatedScan else it
        }
        saveScans(currentScans)
    }
}
