package com.t2h.ocr.data.local

import android.content.Context
import androidx.core.util.AtomicFile
import com.t2h.ocr.data.models.SummaryMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

class SummaryJsonStorage(private val filesDir: File) : SummaryStorage {

    constructor(context: Context) : this(context.filesDir)

    private val fileName = "summaries.json"
    private val file = File(filesDir, fileName)
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val _summariesFlow = MutableStateFlow<List<SummaryMetadata>>(emptyList())
    override val summaries = _summariesFlow.asStateFlow()

    init {
        _summariesFlow.value = loadSummaries()
    }

    override fun saveSummaries(summaries: List<SummaryMetadata>) {
        val atomicFile = AtomicFile(file)
        var stream: FileOutputStream? = null
        try {
            stream = atomicFile.startWrite()
            stream.write(json.encodeToString(summaries).toByteArray())
            atomicFile.finishWrite(stream)
            _summariesFlow.value = summaries
        } catch (e: Exception) {
            atomicFile.failWrite(stream)
            e.printStackTrace()
        }
    }

    override fun loadSummaries(): List<SummaryMetadata> {
        if (!file.exists()) return emptyList()
        return try {
            json.decodeFromString<List<SummaryMetadata>>(file.readText())
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override fun addSummary(summary: SummaryMetadata) {
        val current = loadSummaries().toMutableList()
        current.add(0, summary)
        saveSummaries(current)
    }

    override fun updateSummary(summary: SummaryMetadata) {
        saveSummaries(loadSummaries().map { if (it.id == summary.id) summary else it })
    }

    override fun deleteSummary(summaryId: String) {
        saveSummaries(loadSummaries().filter { it.id != summaryId })
    }
}
