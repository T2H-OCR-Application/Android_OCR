package com.t2h.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text as VisionText
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.t2h.ocr.data.ScanRepository
import com.t2h.ocr.data.auth.AuthRepository
import com.t2h.ocr.data.local.JsonStorage
import com.t2h.ocr.data.models.ScannedPage
import com.t2h.ocr.domain.ocr.ImageProcessor
import com.t2h.ocr.ui.components.CameraPermissionRationale
import com.t2h.ocr.ui.home.HomeScreen
import com.t2h.ocr.ui.home.HomeViewModel
import com.t2h.ocr.ui.home.HomeViewModelFactory
import com.t2h.ocr.ui.login.LoginScreen
import com.t2h.ocr.ui.login.RegisterScreen
import com.t2h.ocr.ui.profile.ProfileScreen
import com.t2h.ocr.ui.results.ResultsScreen
import com.t2h.ocr.ui.results.ResultsViewModel
import com.t2h.ocr.ui.results.ResultsViewModelFactory
import com.t2h.ocr.ui.scanner.CropScreen
import com.t2h.ocr.ui.scanner.GalleryScreen
import com.t2h.ocr.ui.scanner.ScannerScreen
import com.t2h.ocr.ui.scanner.ScannerViewModel
import com.t2h.ocr.ui.theme.AndroidOCRTheme
import com.t2h.ocr.domain.observability.AnalyticsHelper
import com.t2h.ocr.ui.home.HistoryItemData
import com.t2h.ocr.ui.home.HistoryScreen
import com.t2h.ocr.ui.fileP.FilesScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.core.Point
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class Screen {
    object Loading : Screen()
    object Login : Screen()
    object Home : Screen()
    object History : Screen()
    object Files : Screen()
    object Permission : Screen()
    object Scanner : Screen()
    object Profile : Screen()
    object Settings : Screen()
    object Gallery : Screen()
    object Register : Screen()
    data class Crop(val imagePath: String, val points: List<Point>) : Screen()
    data class Results(val pages: List<String>, val imagePath: String, val allImagePaths: List<String>) : Screen()
}

class MainActivity : ComponentActivity() {
    private lateinit var authRepository: AuthRepository
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!OpenCVLoader.initDebug()) {
            Log.e("MainActivity", "OpenCV initialization failed.")
        } else {
            Log.d("MainActivity", "OpenCV initialized successfully.")
        }

        authRepository = AuthRepository()
        authRepository.signInAnonymously()

        enableEdgeToEdge()
        setContent {
            AndroidOCRTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()
                    val currentUser by authRepository.currentUser.collectAsState(initial = FirebaseAuth.getInstance().currentUser)

                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
                    var isProcessing by remember { mutableStateOf(false) }

                    val scanRepository = remember { ScanRepository.getInstance(context) }
                    val scans by scanRepository.scans.collectAsState(initial = emptyList())
                    val recentHistory = remember(scans) {
                        scans.sortedByDescending { it.timestamp }.map { scan ->
                            HistoryItemData(
                                id = scan.id,
                                title = scan.title.ifBlank { "Tệp không tên" },
                                timeString = formatRelativeTime(scan.timestamp),
                                imagePath = scan.imagePath.ifBlank { null },
                                timestamp = scan.timestamp
                            )
                        }
                    }
                    val workManager = remember { WorkManager.getInstance(context) }

                    val scannerViewModel: ScannerViewModel = viewModel()
                    val scannedPages by scannerViewModel.scannedPages.collectAsState()

                    val analyticsHelper = remember { AnalyticsHelper(context) }

                    LaunchedEffect(Unit) {
                        scannerViewModel.initAnalytics(analyticsHelper)
                        analyticsHelper.logUserInteraction("MainActivity", "app_start")
                    }

                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModelFactory(scanRepository, workManager)
                    )

                    LaunchedEffect(currentUser) {
                        if (currentUser != null && currentScreen == Screen.Loading) {
                            currentScreen = Screen.Home
                        }
                    }

                    val permissionsToRequest = mutableListOf(Manifest.permission.CAMERA)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
                        if (cameraGranted) {
                            currentScreen = Screen.Scanner
                        }
                    }

                    val openScannerWithPermissionCheck = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            currentScreen = Screen.Scanner
                        } else {
                            currentScreen = Screen.Permission
                        }
                    }

                    // ─── ĐÃ SỬA: PHÂN TÁCH RÕ RÀNG INDEX ĐỂ TẠO HIỆU ỨNG ĐẨY TỪ PHẢI SANG TRÁI ───
                    fun getScreenIndex(screen: Screen): Int = when (screen) {
                        Screen.Home -> 0       // Gốc Trang chủ bên trái nhất
                        Screen.History -> 1    // Nhấp "Xem tất cả" sẽ có index lớn hơn -> Đẩy từ phải sang trái chuẩn bài
                        Screen.Files -> 2
                        Screen.Settings -> 3
                        Screen.Profile -> 4
                        else -> -1
                    }

                    AnimatedContent(
                        targetState = currentScreen,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1A1D24)),
                        transitionSpec = {
                            val currentIndex = getScreenIndex(initialState)
                            val targetIndex = getScreenIndex(targetState)

                            val direction = when {
                                targetIndex == -1 -> "forward"
                                currentIndex == -1 -> "backward"
                                targetIndex > currentIndex -> "forward" // Index lớn hơn -> dịch chuyển từ phải sang trái
                                else -> "backward"
                            }

                            val animDuration = 350
                            when (direction) {
                                "forward" -> {
                                    slideInHorizontally(
                                        initialOffsetX = { it },
                                        animationSpec = tween(animDuration)
                                    ) with slideOutHorizontally(
                                        targetOffsetX = { -it },
                                        animationSpec = tween(animDuration)
                                    )
                                }
                                else -> {
                                    slideInHorizontally(
                                        initialOffsetX = { -it },
                                        animationSpec = tween(animDuration)
                                    ) with slideOutHorizontally(
                                        targetOffsetX = { it },
                                        animationSpec = tween(animDuration)
                                    )
                                }
                            }.using(
                                SizeTransform(clip = true)
                            )
                        }
                    ) { screen ->
                        when (screen) {
                            Screen.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator()
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(stringResource(R.string.common_initializing))
                                    }
                                }
                            }

                            Screen.Home -> {
                                HomeScreen(
                                    recentHistory = recentHistory,
                                    onNavigateToSection = { sectionName ->
                                        when (sectionName) {
                                            "Quét" -> openScannerWithPermissionCheck()
                                            "Tệp" -> currentScreen = Screen.Files
                                            "Xem tất cả" -> currentScreen = Screen.History
                                            "Hồ sơ" -> currentScreen = Screen.Profile
                                            "Cài đặt", "Công cụ" -> currentScreen = Screen.Settings
                                        }
                                    },
                                    onCenterFabClick = openScannerWithPermissionCheck,
                                    onRecentItemClick = { item ->
                                        currentScreen = Screen.Results(
                                            pages = listOf(item.title),
                                            imagePath = item.imagePath ?: "",
                                            allImagePaths = item.imagePath?.let { listOf(it) } ?: emptyList()
                                        )
                                    }
                                )
                            }

                            Screen.History -> {
                                HistoryScreen(
                                    historyList = recentHistory,
                                    onItemClick = { item ->
                                        currentScreen = Screen.Results(
                                            pages = listOf(item.title),
                                            imagePath = item.imagePath ?: "",
                                            allImagePaths = emptyList()
                                        )
                                    },
                                    onEditItemClick = { item ->
                                        Log.d("Navigation", "Sửa đổi item: ${item.id}")
                                    },
                                    onNavigateToSection = { sectionName ->
                                        when (sectionName) {
                                            "Trang chủ" -> currentScreen = Screen.Home
                                            "Tệp" -> currentScreen = Screen.Files
                                            "Hồ sơ" -> currentScreen = Screen.Profile
                                            "Công cụ", "Cài đặt" -> currentScreen = Screen.Settings
                                        }
                                    },
                                    onCenterFabClick = openScannerWithPermissionCheck
                                )
                            }

                            Screen.Files -> {
                                FilesScreen(
                                    scanRepository = scanRepository,
                                    onNavigateToSection = { sectionName ->
                                        when (sectionName) {
                                            "Trang chủ" -> currentScreen = Screen.Home
                                            "Tệp" -> { /* Đang ở chính nó */ }
                                            "Công cụ", "Cài đặt" -> currentScreen = Screen.Settings
                                            "Hồ sơ" -> currentScreen = Screen.Profile
                                        }
                                    },
                                    onCenterFabClick = openScannerWithPermissionCheck,
                                    onOpenScan = { scan ->
                                        currentScreen = Screen.Results(
                                            pages = listOf(scan.title),
                                            imagePath = scan.imagePath,
                                            allImagePaths = if (scan.imagePath.isNotBlank()) listOf(scan.imagePath) else emptyList()
                                        )
                                    },
                                    onDeleteScan = { scan ->
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                if (scan.imagePath.isNotBlank()) File(scan.imagePath).delete()
                                                if (scan.pdfPath.isNotBlank()) File(scan.pdfPath).delete()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                            scanRepository.deleteScan(scan.id)
                                        }
                                    }
                                )
                            }

                            Screen.Permission -> {
                                CameraPermissionRationale(
                                    onGrantClick = {
                                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                                    },
                                    onDismissClick = {
                                        currentScreen = Screen.Home
                                    }
                                )
                            }

                            Screen.Scanner -> {
                                ScannerScreen(
                                    viewModel = scannerViewModel,
                                    onDocumentCaptured = { path, points ->
                                        currentScreen = Screen.Crop(path, points)
                                    },
                                    onProfileClick = {
                                        currentScreen = Screen.Profile
                                    },
                                    onBackClick = {
                                        currentScreen = Screen.Home
                                    },
                                    onGalleryClick = {
                                        currentScreen = Screen.Gallery
                                    }
                                )
                            }

                            Screen.Profile -> {
                                ProfileScreen(
                                    authRepository = authRepository,
                                    onNavigateBack = { currentScreen = Screen.Home },
                                    onNavigateToSettings = { currentScreen = Screen.Settings },
                                    onNavigateToHistory = { currentScreen = Screen.History },
                                    onNavigateToHome = { currentScreen = Screen.Home },
                                    onLogoutSuccess = {
                                        currentScreen = Screen.Login
                                    },
                                    onNavigateToScanner = openScannerWithPermissionCheck
                                )
                            }

                            Screen.Settings -> {
                                val settingsViewModel: com.t2h.ocr.ui.settings.SettingsViewModel = viewModel(
                                    factory = com.t2h.ocr.ui.settings.SettingsViewModelFactory(
                                        com.t2h.ocr.data.local.UserPreferences(context)
                                    )
                                )
                                LaunchedEffect(Unit) {
                                    settingsViewModel.initAnalytics(analyticsHelper)
                                    analyticsHelper.logUserInteraction("SettingsScreen", "screen_view")
                                }
                                com.t2h.ocr.ui.settings.SettingsScreen(
                                    viewModel = settingsViewModel,
                                    onBack = { currentScreen = Screen.Profile },
                                    onNavigateToHome = { currentScreen = Screen.Home },
                                    onNavigateToHistory = { currentScreen = Screen.Files },
                                    onNavigateToProfile = { currentScreen = Screen.Profile },
                                    onNavigateToScanner = openScannerWithPermissionCheck
                                )
                            }

                            Screen.Login -> {
                                LoginScreen(
                                    onNavigateToRegister = { currentScreen = Screen.Register },
                                    onAuthSuccess = { currentScreen = Screen.Home }
                                )
                            }

                            Screen.Register -> {
                                RegisterScreen(
                                    onNavigateToLogin = { currentScreen = Screen.Login },
                                    onRegisterSuccess = { currentScreen = Screen.Home }
                                )
                            }

                            is Screen.Crop -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    CropScreen(
                                        imagePath = screen.imagePath,
                                        initialPoints = screen.points,
                                        onConfirm = { finalPoints ->
                                            isProcessing = true
                                            scope.launch {
                                                val startTime = System.currentTimeMillis()
                                                val result = processAndOcr(screen.imagePath, finalPoints)
                                                val latency = System.currentTimeMillis() - startTime
                                                isProcessing = false
                                                if (result != null) {
                                                    try { File(screen.imagePath).delete() } catch (e: Exception) {}

                                                    scannerViewModel.logOcrPerformance(latency, 1, "success")
                                                    scannerViewModel.addPage(ScannedPage(imagePath = result.second, text = result.first.text))
                                                    currentScreen = Screen.Gallery
                                                } else {
                                                    scannerViewModel.logOcrPerformance(latency, 1, "failure")
                                                }
                                            }
                                        },
                                        onCancel = {
                                            try { File(screen.imagePath).delete() } catch (e: Exception) {}
                                            currentScreen = Screen.Scanner
                                        }
                                    )

                                    if (isProcessing) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                }
                            }

                            Screen.Gallery -> {
                                GalleryScreen(
                                    pages = scannedPages,
                                    onAddMore = { currentScreen = Screen.Scanner },
                                    onRemovePage = { id -> scannerViewModel.removePage(id) },
                                    onFinish = {
                                        currentScreen = Screen.Results(
                                            pages = scannedPages.map { it.text },
                                            imagePath = scannedPages.firstOrNull()?.imagePath ?: "",
                                            allImagePaths = scannedPages.map { it.imagePath }
                                        )
                                    },
                                    onBackClick = { currentScreen = Screen.Scanner }
                                )
                            }

                            is Screen.Results -> {
                                val resultsViewModel: ResultsViewModel = viewModel(
                                    factory = ResultsViewModelFactory(scanRepository)
                                )
                                LaunchedEffect(Unit) {
                                    resultsViewModel.initAnalytics(analyticsHelper)
                                    analyticsHelper.logUserInteraction("ResultsScreen", "screen_view")
                                }
                                ResultsScreen(
                                    viewModel = resultsViewModel,
                                    pages = screen.pages,
                                    imagePath = screen.imagePath,
                                    allImagePaths = screen.allImagePaths,
                                    onSaveComplete = {
                                        scannerViewModel.clearPages()
                                        currentScreen = Screen.Home
                                    },
                                    onBackClick = {
                                        if (scannedPages.isNotEmpty()) {
                                            currentScreen = Screen.Gallery
                                        } else {
                                            currentScreen = Screen.Home
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun processAndOcr(imagePath: String, points: List<Point>): Pair<VisionText, String>? = withContext(Dispatchers.IO) {
        try {
            val src = Imgcodecs.imread(imagePath)
            if (src.empty()) return@withContext null

            val warped = ImageProcessor.warpPerspective(src, points)
            src.release()

            val warpedFile = File(cacheDir, "warped_${UUID.randomUUID()}.jpg")
            Imgcodecs.imwrite(warpedFile.absolutePath, warped)

            val bitmap = android.graphics.BitmapFactory.decodeFile(warpedFile.absolutePath)
            warped.release()

            if (bitmap == null) return@withContext null

            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val visionText = recognizer.process(inputImage).await()

            visionText to warpedFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        recognizer.close()
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        diff < 60_000 -> "Vừa xong"
        diff < 3_600_000 -> "${minutes} phút trước"
        diff < 86_400_000 -> "${hours} giờ trước"
        diff < 172_800_000 -> "Hôm qua"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}