package com.t2h.ocr.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.t2h.ocr.data.ScanRepository
import com.t2h.ocr.data.models.ScanMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

enum class SyncStatus {
    IDLE,
    SYNCING,
    WAITING_FOR_NETWORK,
    FAILED,
    SYNCED
}

class HomeViewModel(
    private val scanRepository: ScanRepository,
    private val workManager: WorkManager
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val syncStatuses: StateFlow<Map<String, SyncStatus>> = workManager.getWorkInfosByTagFlow("sync")
        .map { workInfos ->
            workInfos.associate { info ->
                val scanId = info.tags.find { it.startsWith("scan_") }?.removePrefix("scan_") ?: ""
                val status = when (info.state) {
                    WorkInfo.State.RUNNING -> SyncStatus.SYNCING
                    WorkInfo.State.ENQUEUED -> {
                        if (info.runAttemptCount > 0) SyncStatus.FAILED 
                        else SyncStatus.WAITING_FOR_NETWORK
                    }
                    WorkInfo.State.FAILED -> SyncStatus.FAILED
                    WorkInfo.State.BLOCKED -> SyncStatus.WAITING_FOR_NETWORK
                    WorkInfo.State.SUCCEEDED -> SyncStatus.SYNCED
                    else -> SyncStatus.IDLE
                }
                scanId to status
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val filteredScans: StateFlow<List<ScanMetadata>> = combine(scanRepository.scans, _searchQuery) { scans, query ->
        if (query.isBlank()) {
            scans
        } else {
            scans.filter { scan ->
                scan.title.contains(query, ignoreCase = true) ||
                        scan.ocrText.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteScan(scan: ScanMetadata) {
        viewModelScope.launch(Dispatchers.IO) {
            workManager.cancelUniqueWork("sync_${scan.id}")
            try {
                if (scan.imagePath.isNotEmpty()) File(scan.imagePath).delete()
                if (scan.pdfPath.isNotEmpty()) File(scan.pdfPath).delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            scanRepository.deleteScan(scan.id)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
