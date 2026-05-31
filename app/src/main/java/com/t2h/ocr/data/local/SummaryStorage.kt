package com.t2h.ocr.data.local

import com.t2h.ocr.data.models.SummaryMetadata
import kotlinx.coroutines.flow.Flow

interface SummaryStorage {
    val summaries: Flow<List<SummaryMetadata>>
    fun saveSummaries(summaries: List<SummaryMetadata>)
    fun loadSummaries(): List<SummaryMetadata>
    fun addSummary(summary: SummaryMetadata)
    fun updateSummary(summary: SummaryMetadata)
    fun deleteSummary(summaryId: String)
}
