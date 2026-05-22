package com.t2h.ocr.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t2h.ocr.data.local.JsonStorage
import com.t2h.ocr.data.models.ScanMetadata
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(private val jsonStorage: JsonStorage) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _scans = MutableStateFlow<List<ScanMetadata>>(emptyList())
    
    val filteredScans: StateFlow<List<ScanMetadata>> = combine(_scans, _searchQuery) { scans, query ->
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

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _scans.value = jsonStorage.loadScans()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
