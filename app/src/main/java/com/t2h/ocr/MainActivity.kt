package com.t2h.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.text.Text as VisionText
import com.t2h.ocr.data.auth.AuthRepository
import com.t2h.ocr.ui.components.CameraPermissionRationale
import com.t2h.ocr.ui.results.ResultsScreen
import com.t2h.ocr.ui.scanner.ScannerScreen
import com.t2h.ocr.ui.theme.AndroidOCRTheme

/**
 * Main entry point of the application. Manages high-level navigation between screens.
 */
sealed class Screen {
    object Permission : Screen()
    object Scanner : Screen()
    data class Results(val text: VisionText, val bitmap: Bitmap) : Screen()
}

class MainActivity : ComponentActivity() {
    private lateinit var authRepository: AuthRepository

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
                    var currentScreen by remember {
                        mutableStateOf(
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                Screen.Scanner
                            } else {
                                Screen.Permission
                            }
                        )
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            currentScreen = Screen.Scanner
                        }
                    }

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn() with fadeOut()
                        }
                    ) { screen ->
                        when (screen) {
                            Screen.Permission -> {
                                CameraPermissionRationale(
                                    onGrantClick = {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    },
                                    onDismissClick = {
                                        // In a real app, we might show a message or close the app
                                    }
                                )
                            }

                            Screen.Scanner -> {
                                ScannerScreen(
                                    onTextCaptured = { text, bitmap ->
                                        currentScreen = Screen.Results(text, bitmap)
                                    }
                                )
                            }

                            is Screen.Results -> {
                                ResultsScreen(
                                    recognizedText = screen.text,
                                    capturedBitmap = screen.bitmap,
                                    onSaveComplete = {
                                        // After saving, return to the scanner
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
}

