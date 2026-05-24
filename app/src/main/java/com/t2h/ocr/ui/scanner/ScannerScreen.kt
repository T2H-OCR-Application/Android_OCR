package com.t2h.ocr.ui.scanner

import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.t2h.ocr.R
import com.t2h.ocr.domain.ocr.DocumentAnalyzer
import kotlinx.coroutines.delay
import org.opencv.core.Point
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.Executor

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = viewModel(),
    onDocumentCaptured: (String, List<Point>) -> Unit,
    onProfileClick: () -> Unit,
    onGalleryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val quadCoordinates by viewModel.quadCoordinates.collectAsState()
    val imageSize by viewModel.imageSize.collectAsState()
    val scannedPages by viewModel.scannedPages.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var showFlash by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    
    // Explicit Use Cases
    val imageCapture = remember { ImageCapture.Builder().build() }
    val imageAnalysis = remember { 
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(Size(1280, 720))
            .build()
    }

    val docAnalyzer = remember {
        DocumentAnalyzer { points, width, height ->
            viewModel.onQuadDetected(points, width, height)
        }
    }

    DisposableEffect(docAnalyzer) {
        onDispose {
            docAnalyzer.release()
        }
    }

    // Bind Camera manually for maximum reliability
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView?.surfaceProvider
            }

            imageAnalysis.setAnalyzer(mainExecutor, docAnalyzer)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                    imageAnalysis
                )
                Log.e("DEBUG_OCR", "Manual Camera Binding SUCCESSFUL")
            } catch (e: Exception) {
                Log.e("DEBUG_OCR", "Manual Camera Binding FAILED", e)
                viewModel.setCameraError()
            }
        }, mainExecutor)
    }

    if (errorState == "camera_failure") {
        com.t2h.ocr.ui.components.CameraErrorState(onRetry = { viewModel.clearError() })
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                        previewView = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = {
                    // Update surface provider if needed
                }
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

            // Capture Button
            Button(
                onClick = {
                    val currentPreview = previewView
                    if (currentPreview != null) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showFlash = true
                        
                        // We use the PreviewView's bitmap for consistency with the analysis overlay
                        val bitmap = currentPreview.bitmap
                        if (bitmap != null) {
                            val tempFile = File(context.cacheDir, "temp_capture_${UUID.randomUUID()}.jpg")
                            FileOutputStream(tempFile).use { out ->
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                            }
                            onDocumentCaptured(tempFile.absolutePath, quadCoordinates)
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            ) {
                Text(stringResource(R.string.scanner_capture))
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
                    contentDescription = stringResource(R.string.scanner_profile),
                    tint = Color.White
                )
            }

            if (scannedPages.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .size(56.dp)
                ) {
                    IconButton(
                        onClick = onGalleryClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = scannedPages.size.toString(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = stringResource(R.string.scanner_pages),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
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
        if (imageSize != null && quadCoordinates.size == 4) {
            val (imgWidth, imgHeight) = imageSize
            
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            val scaleX = canvasWidth / imgWidth
            val scaleY = canvasHeight / imgHeight
            val scale = maxOf(scaleX, scaleY)
            
            val offsetX = (canvasWidth - (imgWidth * scale)) / 2
            val offsetY = (canvasHeight - (imgHeight * scale)) / 2

            val path = Path().apply {
                val p0 = quadCoordinates[0]
                moveTo(
                    (p0.x.toFloat() * scale) + offsetX, 
                    (p0.y.toFloat() * scale) + offsetY
                )
                for (i in 1 until 4) {
                    val p = quadCoordinates[i]
                    lineTo(
                        (p.x.toFloat() * scale) + offsetX, 
                        (p.y.toFloat() * scale) + offsetY
                    )
                }
                close()
            }
            
            drawPath(
                path = path,
                color = Color.Green.copy(alpha = 0.8f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
