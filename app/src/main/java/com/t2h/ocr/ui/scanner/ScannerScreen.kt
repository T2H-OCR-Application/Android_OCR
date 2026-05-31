package com.t2h.ocr.ui.scanner

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.t2h.ocr.R
import com.t2h.ocr.domain.ocr.ImageProcessor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    val scannedPages by viewModel.scannedPages.collectAsState()
    val errorState by viewModel.errorState.collectAsState()

    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var showFlash by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }

    val imageCapture = remember { ImageCapture.Builder().build() }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val path = ImageProcessor.copyUriToCache(context, uri)
                    val corners = ImageProcessor.detectCornersInFile(path)
                    onDocumentCaptured(path, corners)
                } catch (e: Exception) {
                    Log.e("ScannerScreen", "Gallery Import FAILED", e)
                }
            }
        }
    }

    // Đồng bộ luồng liên kết Camera với vòng đời giao diện
    LaunchedEffect(previewView) {
        if (previewView == null) return@LaunchedEffect
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
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // LỚP 1: HIỂN THỊ LUỒNG CAMERA HOẠT HÌNH
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                        this.scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // LỚP 2: KHUNG NGẮM TÀI LIỆU AI TRỰC QUAN (Hộp Bento Tâm Ngắm)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Xác định kích thước khung chữ nhật ngắm ở giữa màn hình
                val boxWidth = canvasWidth * 0.85f
                val boxHeight = boxWidth * 1.414f // Tỉ lệ chuẩn khổ giấy A4

                val left = (canvasWidth - boxWidth) / 2
                val top = (canvasHeight - boxHeight) / 2.3f
                val right = left + boxWidth
                val bottom = top + boxHeight

                // Tạo lớp phủ mờ bóng tối xung quanh rìa khung ngắm tài liệu
                val backgroundPath = Path().apply {
                    addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                }
                val cutoutPath = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(left, top, right, bottom),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        )
                    )
                }

                // Cắt rỗng tâm để nhìn thấy luồng camera sắc nét bên trong khung
                val finalPath = Path.combine(
                    operation = PathOperation.Difference,
                    path1 = backgroundPath,
                    path2 = cutoutPath
                )

                drawPath(path = finalPath, color = Color.Black.copy(alpha = 0.6f))

                // Vẽ khung ranh giới sắc nét xanh ngọc bo quanh viền
                drawRoundRect(
                    color = Color(0xFF14B8A6),
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Hiệu ứng nháy trắng màn hình (Mô phỏng đèn Flash cơ học khi chụp)
            if (showFlash) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.6f))
                )
            }

            // LỚP 3: THANH CÔNG CỤ ĐIỀU HƯỚNG PHÍA TRÊN ĐỈNH
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút đóng quay về (Được bo tròn mờ)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF252329).copy(alpha = 0.7f))
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.common_back),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "Quét tài liệu",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                // Nút xem thông tin hồ sơ
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF252329).copy(alpha = 0.7f))
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.scanner_profile),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // LỚP 4: HỆ THỐNG ĐIỀU KHIỂN CHỤP ẢNH TẬP TRUNG Ở ĐÁY
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nút Nhập từ Thư viện (Import)
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF252329).copy(alpha = 0.8f))
                            .clickable {
                                pickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.tabler_photo_plus),
                            contentDescription = "Nhập từ thư viện",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
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
                                        onDocumentCaptured(tempFile.absolutePath, emptyList())
                                    }
                                }
                            }
                    )


                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF252329).copy(alpha = 0.8f))
                            .clickable { onGalleryClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Thư viện trang quét",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )

                        if (scannedPages.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF14B8A6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = scannedPages.size.toString(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(showFlash) {
        if (showFlash) {
            delay(80)
            showFlash = false
        }
    }
}