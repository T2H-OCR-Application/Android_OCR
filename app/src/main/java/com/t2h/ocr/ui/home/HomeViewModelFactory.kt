package com.t2h.ocr.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.t2h.ocr.data.local.JsonStorage

class HomeViewModelFactory(private val jsonStorage: JsonStorage) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(jsonStorage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
