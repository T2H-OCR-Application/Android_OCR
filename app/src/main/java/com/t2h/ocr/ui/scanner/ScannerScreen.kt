package com.t2h.ocr.ui.scanner

import android.graphics.Bitmap
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.text.Text as VisionText

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = viewModel(),
    onTextCaptured: (VisionText, Bitmap) -> Unit
) {
    val context = LocalContext.current
    val detectedText by viewModel.detectedText.collectAsState()
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        }
    }

    LaunchedEffect(controller) {
        val executor = ContextCompat.getMainExecutor(context)
        val analyzer = MlKitAnalyzer(
            listOf(viewModel.recognizer),
            CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED,
            executor
        ) { result ->
            val visionText = result.getValue(viewModel.recognizer)
            viewModel.onTextDetected(visionText)
        }
        controller.setMlKitAnalyzer(executor, analyzer)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    this.controller = controller
                    previewView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            detectedText?.textBlocks?.forEach { block ->
                block.boundingBox?.let { rect ->
                    drawRect(
                        color = Color.Cyan,
                        topLeft = Offset(rect.left.toFloat(), rect.top.toFloat()),
                        size = Size(rect.width().toFloat(), rect.height().toFloat()),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        Button(
            onClick = {
                val bitmap = previewView?.bitmap
                val text = detectedText
                if (bitmap != null && text != null) {
                    onTextCaptured(text, bitmap)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            enabled = detectedText != null
        ) {
            Text("Capture Text")
        }
    }
}

