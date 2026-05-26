package com.t2h.ocr.ui.screens.home

data class RecentItem(
    val id: String,
    val title: String,
    val type: String,
    val createdAt: Long = System.currentTimeMillis()
)

sealed class HomeUiState {
    object Idle    : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val recentItems: List<RecentItem>) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
