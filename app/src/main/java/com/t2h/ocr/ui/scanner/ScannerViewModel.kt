package com.t2h.ocr.ui.scanner

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.opencv.core.Point

class ScannerViewModel : ViewModel() {

    private val _quadCoordinates = MutableStateFlow<List<Point>>(emptyList())
    val quadCoordinates = _quadCoordinates.asStateFlow()

    private val _imageSize = MutableStateFlow<Pair<Int, Int>?>(null)
    val imageSize = _imageSize.asStateFlow()

    fun onQuadDetected(points: List<Point>, width: Int, height: Int) {
        _quadCoordinates.value = points
        _imageSize.value = width to height
    }
}
