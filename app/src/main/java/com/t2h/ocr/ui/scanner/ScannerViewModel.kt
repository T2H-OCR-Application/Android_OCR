package com.t2h.ocr.ui.scanner

import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScannerViewModel : ViewModel() {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val _detectedText = MutableStateFlow<Text?>(null)
    val detectedText = _detectedText.asStateFlow()

    fun onTextDetected(visionText: Text?) {
        _detectedText.value = visionText
    }

    override fun onCleared() {
        super.onCleared()
        recognizer.close()
    }
}
