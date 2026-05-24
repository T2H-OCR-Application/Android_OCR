package com.t2h.ocr.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t2h.ocr.R

/**
 * Screen for managing user preferences and resource management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val syncWifiOnly by viewModel.syncWifiOnly.collectAsState()
    val clearCacheOnSync by viewModel.clearCacheOnSync.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_resource_mgmt),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Wi-Fi Only Sync Toggle
            SettingsToggleItem(
                title = stringResource(R.string.settings_wifi_sync_title),
                description = stringResource(R.string.settings_wifi_sync_desc),
                icon = Icons.Default.Wifi,
                checked = syncWifiOnly,
                onCheckedChange = { viewModel.setSyncWifiOnly(it) }
            )

            // Clear Cache on Sync Toggle
            SettingsToggleItem(
                title = stringResource(R.string.settings_clear_cache_sync_title),
                description = stringResource(R.string.settings_clear_cache_sync_desc),
                icon = Icons.Default.Cached,
                checked = clearCacheOnSync,
                onCheckedChange = { viewModel.setClearCacheOnSync(it) }
            )

            HorizontalDivider()

            Text(
                text = stringResource(R.string.settings_storage),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Clear Local Cache Button
            val cacheClearedMsg = stringResource(R.string.toast_cache_cleared)
            val cacheClearFailedMsg = stringResource(R.string.toast_cache_clear_failed)
            Button(
                onClick = {
                    val success = viewModel.clearLocalCache()
                    if (success) {
                        Toast.makeText(context, cacheClearedMsg, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, cacheClearFailedMsg, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(stringResource(R.string.settings_clear_cache_btn))
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
