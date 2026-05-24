package com.t2h.ocr.ui.profile

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
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
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by authRepository.currentUser.collectAsState(initial = FirebaseAuth.getInstance().currentUser)
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val accountLinkedMsg = stringResource(R.string.toast_account_linked)
    val linkFailedMsg = stringResource(R.string.toast_link_failed)
    val googleFailedTokenMsg = stringResource(R.string.toast_google_failed_token)
    val googleFailedStatusMsg = stringResource(R.string.toast_google_failed_status)
    val googleFailedResultMsg = stringResource(R.string.toast_google_failed_result)
    val signInCancelledMsg = stringResource(R.string.toast_sign_in_cancelled)

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
                            Toast.makeText(context, accountLinkedMsg, Toast.LENGTH_SHORT).show()
                        } else {
                            errorMessage = linkFailedMsg
                        }
                    }
                } else {
                    errorMessage = googleFailedTokenMsg
                }
            } catch (e: ApiException) {
                Log.e("ProfileScreen", "Google sign in failed", e)
                errorMessage = String.format(googleFailedStatusMsg, e.statusCode, e.message ?: "")
            }
        } else {
            Log.e("ProfileScreen", "Google sign in cancelled or failed: ${result.resultCode}")
            if (result.resultCode != Activity.RESULT_CANCELED) {
                errorMessage = String.format(googleFailedResultMsg, result.resultCode)
            } else {
                Toast.makeText(context, signInCancelledMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
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
                text = if (currentUser?.isAnonymous == true) stringResource(R.string.profile_anonymous) else stringResource(R.string.profile_linked),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.profile_uid, currentUser?.uid ?: "Not signed in"),
                style = MaterialTheme.typography.bodyMedium
            )

            if (currentUser?.email != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.profile_email, currentUser?.email ?: ""),
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
                        Text(stringResource(R.string.profile_link_google))
                    }
                } else {
                    Text(stringResource(R.string.profile_google_linked))
                }
            }
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
