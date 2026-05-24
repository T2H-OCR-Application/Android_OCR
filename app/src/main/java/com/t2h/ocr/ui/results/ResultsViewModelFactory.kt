package com.t2h.ocr.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.t2h.ocr.data.ScanRepository

class ResultsViewModelFactory(
    private val scanRepository: ScanRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResultsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ResultsViewModel(scanRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
