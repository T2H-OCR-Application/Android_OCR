package com.t2h.ocr.ui.scanner

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.t2h.ocr.domain.ocr.DocumentAnalyzer
import kotlinx.coroutines.delay
import org.opencv.core.Point
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = viewModel(),
    onDocumentCaptured: (String, List<Point>) -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val quadCoordinates by viewModel.quadCoordinates.collectAsState()
    val imageSize by viewModel.imageSize.collectAsState()
    
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var showFlash by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember(context, lifecycleOwner) {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            bindToLifecycle(lifecycleOwner)
        }
    }

    LaunchedEffect(controller) {
        val executor = ContextCompat.getMainExecutor(context)
        
        val docAnalyzer = DocumentAnalyzer { points, width, height ->
            viewModel.onQuadDetected(points, width, height)
        }

        controller.setImageAnalysisAnalyzer(executor) { image ->
            docAnalyzer.analyzeWithoutClosing(image)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    this.controller = controller
                    previewView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        DocumentOverlay(
            quadCoordinates = quadCoordinates,
            imageSize = imageSize,
            modifier = Modifier.fillMaxSize()
        )

        if (showFlash) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.5f))
            )
        }

        Button(
            onClick = {
                val bitmap = previewView?.bitmap
                if (bitmap != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showFlash = true
                    
                    val tempFile = File(context.cacheDir, "temp_capture_${UUID.randomUUID()}.jpg")
                    FileOutputStream(tempFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    onDocumentCaptured(tempFile.absolutePath, quadCoordinates)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            Text("Capture Document")
        }

        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile",
                tint = Color.White
            )
        }
    }

    LaunchedEffect(showFlash) {
        if (showFlash) {
            delay(100)
            showFlash = false
        }
    }
}

@Composable
fun DocumentOverlay(
    quadCoordinates: List<Point>,
    imageSize: Pair<Int, Int>?,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (quadCoordinates.size == 4 && imageSize != null) {
            val (imgWidth, imgHeight) = imageSize
            
            // CameraX images are often rotated. 
            // In portrait, imgHeight is usually the width of the frame if it's 640x480.
            // However, docAnalyzer seems to use raw width/height.
            
            val scaleX = size.width / imgWidth
            val scaleY = size.height / imgHeight
            
            val path = Path().apply {
                val p0 = quadCoordinates[0]
                moveTo(p0.x.toFloat() * scaleX, p0.y.toFloat() * scaleY)
                for (i in 1 until 4) {
                    val p = quadCoordinates[i]
                    lineTo(p.x.toFloat() * scaleX, p.y.toFloat() * scaleY)
                }
                close()
            }
            
            drawPath(
                path = path,
                color = Color.Green,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
