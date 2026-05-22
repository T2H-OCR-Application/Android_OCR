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
import com.google.mlkit.vision.text.Text as VisionText
import com.t2h.ocr.data.auth.AuthRepository
import com.t2h.ocr.ui.components.CameraPermissionRationale
import com.t2h.ocr.ui.profile.ProfileScreen
import com.t2h.ocr.ui.results.ResultsScreen
import com.t2h.ocr.ui.scanner.ScannerScreen
import com.t2h.ocr.ui.theme.AndroidOCRTheme

/**
 * Main entry point of the application. Manages high-level navigation between screens.
 */
sealed class Screen {
    object Loading : Screen()
    object Permission : Screen()
    object Scanner : Screen()
    object Profile : Screen()
    data class Results(val text: VisionText, val imagePath: String) : Screen()
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
                    val currentUser by authRepository.currentUser.collectAsState(initial = FirebaseAuth.getInstance().currentUser)
                    
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Loading) }

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
                            fadeIn() with fadeOut()
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
                                    onTextCaptured = { text, path ->
                                        currentScreen = Screen.Results(text, path)
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

                            is Screen.Results -> {
                                ResultsScreen(
                                    recognizedText = screen.text,
                                    imagePath = screen.imagePath,
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

