package com.t2h.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text as VisionText
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.t2h.ocr.data.auth.AuthRepository
import com.t2h.ocr.domain.ocr.ImageProcessor
import com.t2h.ocr.ui.components.CameraPermissionRationale
import com.t2h.ocr.ui.profile.ProfileScreen
import com.t2h.ocr.ui.results.ResultsScreen
import com.t2h.ocr.ui.scanner.CropScreen
import com.t2h.ocr.ui.scanner.ScannerScreen
import com.t2h.ocr.ui.theme.AndroidOCRTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.core.Point
import java.io.File
import java.util.UUID

/**
 * Main entry point of the application. Manages high-level navigation between screens.
 */
sealed class Screen {
    object Loading : Screen()
    object Permission : Screen()
    object Scanner : Screen()
    object Profile : Screen()
    data class Crop(val imagePath: String, val points: List<Point>) : Screen()
    data class Results(val text: VisionText, val imagePath: String) : Screen()
}

class MainActivity : ComponentActivity() {
    private lateinit var authRepository: AuthRepository
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth anonymously
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
                    
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Loading) }
                    var isProcessing by remember { mutableStateOf(false) }

                    // Handle initial navigation and auth state
                    LaunchedEffect(currentUser) {
                        if (currentUser != null && currentScreen == Screen.Loading) {
                            currentScreen = if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                Screen.Scanner
                            } else {
                                Screen.Permission
                            }
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

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState is Screen.Results || targetState is Screen.Crop) {
                                slideInHorizontally { it } + fadeIn() with
                                        slideOutHorizontally { -it } + fadeOut()
                            } else {
                                slideInHorizontally { -it } + fadeIn() with
                                        slideOutHorizontally { it } + fadeOut()
                            }.using(
                                SizeTransform(clip = false)
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
                                        Text("Initializing session...")
                                    }
                                }
                            }

                            Screen.Permission -> {
                                CameraPermissionRationale(
                                    onGrantClick = {
                                        permissionLauncher.launch(permissionsToRequest.toTypedArray())
                                    },
                                    onDismissClick = {
                                        // In a real app, we might show a message or close the app
                                    }
                                )
                            }

                            Screen.Scanner -> {
                                ScannerScreen(
                                    onDocumentCaptured = { path, points ->
                                        currentScreen = Screen.Crop(path, points)
                                    },
                                    onProfileClick = {
                                        currentScreen = Screen.Profile
                                    }
                                )
                            }

                            Screen.Profile -> {
                                ProfileScreen(
                                    authRepository = authRepository,
                                    onNavigateBack = {
                                        currentScreen = Screen.Scanner
                                    }
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
                                                val result = processAndOcr(screen.imagePath, finalPoints)
                                                isProcessing = false
                                                if (result != null) {
                                                    currentScreen = Screen.Results(result.first, result.second)
                                                }
                                            }
                                        },
                                        onCancel = {
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

                            is Screen.Results -> {
                                ResultsScreen(
                                    recognizedText = screen.text,
                                    imagePath = screen.imagePath,
                                    onSaveComplete = {
                                        currentScreen = Screen.Scanner
                                    },
                                    onBackClick = {
                                        currentScreen = Screen.Scanner
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
            val visionText = kotlinx.coroutines.tasks.await(recognizer.process(inputImage))
            
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

