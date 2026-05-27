package com.t2h.ocr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.t2h.ocr.R

/**
 * High-visibility error component for fatal application failures.
 */
@Composable
fun FullScreenError(
    title: String,
    message: String,
    icon: ImageVector,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAction,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text(actionLabel)
        }
    }
}

/**
 * Specifically for camera hardware or unrecoverable permission errors.
 */
@Composable
fun CameraErrorState(onRetry: () -> Unit) {
    FullScreenError(
        title = stringResource(R.string.error_camera_title),
        message = stringResource(R.string.error_camera_message),
        icon = Icons.Default.CameraAlt,
        actionLabel = stringResource(R.string.error_camera_action),
        onAction = onRetry
    )
}

/**
 * Specifically for authentication lockdown or expired sessions.
 */
@Composable
fun AuthErrorState(onSignIn: () -> Unit) {
    FullScreenError(
        title = stringResource(R.string.error_account_title),
        message = stringResource(R.string.error_account_message),
        icon = Icons.Default.Lock,
        actionLabel = stringResource(R.string.error_account_action),
        onAction = onSignIn
    )
}

/**
 * Specifically for catastrophic network or disk failures.
 */
@Composable
fun SyncErrorState(onRetry: () -> Unit) {
    FullScreenError(
        title = stringResource(R.string.error_sync_title),
        message = stringResource(R.string.error_sync_message),
        icon = Icons.Default.SignalWifiOff,
        actionLabel = stringResource(R.string.error_sync_action),
        onAction = onRetry
    )
}

