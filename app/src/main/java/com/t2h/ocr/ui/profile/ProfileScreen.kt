package com.t2h.ocr.ui.profile

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.t2h.ocr.R
import com.t2h.ocr.data.auth.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authRepository.currentUser.collectAsState(initial = FirebaseAuth.getInstance().currentUser)
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("ProfileScreen", "Result received: ${result.resultCode}")
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                Log.d("ProfileScreen", "Google Sign-In successful, idToken present: ${idToken != null}")
                if (idToken != null) {
                    isLoading = true
                    errorMessage = null
                    authRepository.linkWithGoogle(idToken) { success ->
                        isLoading = false
                        if (success) {
                            Toast.makeText(context, "Account linked successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            errorMessage = "Failed to link account with Firebase. Check your network or Firebase Console."
                        }
                    }
                } else {
                    errorMessage = "Google Sign-In failed: No ID Token returned. This usually means the Web Client ID is incorrect or not for this project."
                }
            } catch (e: ApiException) {
                Log.e("ProfileScreen", "Google sign in failed", e)
                errorMessage = "Google Sign-In failed (Status Code: ${e.statusCode}). Details: ${e.message}"
            }
        } else {
            Log.e("ProfileScreen", "Google sign in cancelled or failed: ${result.resultCode}")
            if (result.resultCode != Activity.RESULT_CANCELED) {
                errorMessage = "Google Sign-In failed with result code: ${result.resultCode}"
            } else {
                Toast.makeText(context, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (currentUser?.isAnonymous == true) "Anonymous Account" else "Linked Account",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "UID: ${currentUser?.uid ?: "Not signed in"}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (currentUser?.email != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Email: ${currentUser?.email}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                if (currentUser?.isAnonymous == true) {
                    Button(
                        onClick = {
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(context.getString(R.string.default_web_client_id))
                                .requestEmail()
                                .requestScopes(com.google.android.gms.common.api.Scope(com.google.api.services.drive.DriveScopes.DRIVE_FILE))
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            launcher.launch(googleSignInClient.signInIntent)
                        }
                    ) {
                        Text("Link Google Account")
                    }
                } else {
                    Text("Your account is linked to Google.")
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
