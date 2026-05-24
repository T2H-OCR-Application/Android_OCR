package com.t2h.ocr.ui.scanner

import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay
import org.opencv.core.Point
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel = viewModel(),
    onDocumentCaptured: (String, List<Point>) -> Unit,
    onProfileClick: () -> Unit,
    onBackClick: () -> Unit,
    onGalleryClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scannedPages by viewModel.scannedPages.collectAsState()
    val errorState by viewModel.errorState.collectAsState()
    
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var showFlash by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    
    // Explicit Use Cases
    val imageCapture = remember { ImageCapture.Builder().build() }

    // Bind Camera manually for maximum reliability
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView?.surfaceProvider
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
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
                        
                        val bitmap = currentPreview.bitmap
                        if (bitmap != null) {
                            val tempFile = File(context.cacheDir, "temp_capture_${UUID.randomUUID()}.jpg")
                            FileOutputStream(tempFile).use { out ->
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                            }

                            // Pass empty list to trigger manual crop default in next screen
                            onDocumentCaptured(tempFile.absolutePath, emptyList())
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
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.common_back),
                    tint = Color.White
                )
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
