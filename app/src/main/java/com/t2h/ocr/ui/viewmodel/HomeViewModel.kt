package com.t2h.ocr.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.t2h.ocr.ui.screens.home.HomeUiState
import com.t2h.ocr.ui.screens.home.RecentItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Success(emptyList()))
    val uiState: StateFlow<HomeUiState> = _uiState

    // TODO: Gọi API hoặc Room để lấy danh sách gần đây
    fun loadRecentItems() {
        _uiState.value = HomeUiState.Success(emptyList())
    }
}
