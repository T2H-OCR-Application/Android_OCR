package com.t2h.ocr.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.t2h.ocr.data.ScanRepository
import com.t2h.ocr.data.SummaryRepository

class SummaryViewModelFactory(
    private val scanRepository: ScanRepository,
    private val summaryRepository: SummaryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SummaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SummaryViewModel(scanRepository, summaryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
