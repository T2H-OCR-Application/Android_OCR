package com.t2h.ocr.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.t2h.ocr.data.ScanRepository

/**
 * Factory for creating [HomeViewModel] with its dependencies.
 */
class HomeViewModelFactory(
    private val scanRepository: ScanRepository,
    private val workManager: WorkManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(scanRepository, workManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
