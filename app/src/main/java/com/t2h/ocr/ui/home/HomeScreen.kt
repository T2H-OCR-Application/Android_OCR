package com.t2h.ocr.ui.home

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.t2h.ocr.R
import com.t2h.ocr.data.models.ScanMetadata
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onScanClick: (ScanMetadata) -> Unit,
    onNewScanClick: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val scans by viewModel.filteredScans.collectAsState()
    val syncStatuses by viewModel.syncStatuses.collectAsState()

    LaunchedEffect(Unit) {
        Log.e("DEBUG_OCR", "HomeScreen: Composable entered")
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewScanClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.home_new_scan))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("search_bar"),
                placeholder = { Text(stringResource(R.string.home_search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            if (scans.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) stringResource(R.string.home_no_scans) else stringResource(R.string.home_no_results),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("bento_grid")
                ) {
                    itemsIndexed(
                        items = scans,
                        key = { _, scan -> scan.id },
                        span = { index, _ ->
                            if (index == 0) GridItemSpan(2) else GridItemSpan(1)
                        }
                    ) { index, scan ->
                        val workerStatus = syncStatuses[scan.id]
                        val finalStatus = if (scan.isSynced) SyncStatus.SYNCED else (workerStatus ?: SyncStatus.IDLE)
                        
                        Log.e("DEBUG_OCR", "Scan ${scan.id}: isSynced=${scan.isSynced}, workerStatus=$workerStatus -> finalStatus=$finalStatus")
                        
                        ScanItem(
                            scan = scan,
                            isLarge = index == 0,
                            syncStatus = finalStatus,
                            onClick = { onScanClick(scan) },
                            onDelete = { viewModel.deleteScan(scan) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScanItem(
    scan: ScanMetadata,
    isLarge: Boolean,
    syncStatus: SyncStatus,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val height = if (isLarge) 200.dp else 160.dp
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image (if exists)
            if (scan.imagePath.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(File(scan.imagePath)),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.4f
                )
            }

            // Sync Status Indicator
            SyncIndicator(
                status = syncStatus,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = scan.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isLarge) 20.sp else 16.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(scan.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (isLarge && scan.ocrText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = scan.ocrText,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SyncIndicator(status: SyncStatus, modifier: Modifier = Modifier) {
    Log.e("DEBUG_OCR", "SyncIndicator: status=$status")
    if (status == SyncStatus.IDLE) return

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val (color, icon, label) = when (status) {
        SyncStatus.SYNCING -> Triple(MaterialTheme.colorScheme.primary, Icons.Default.Sync, stringResource(R.string.sync_status_syncing))
        SyncStatus.WAITING_FOR_NETWORK -> Triple(Color.Gray, Icons.Default.CloudOff, stringResource(R.string.sync_status_waiting))
        SyncStatus.FAILED -> Triple(MaterialTheme.colorScheme.error, Icons.Default.SyncProblem, stringResource(R.string.sync_status_failed))
        SyncStatus.SYNCED -> Triple(Color(0xFF4CAF50), Icons.Default.CheckCircle, stringResource(R.string.sync_status_success))
        else -> Triple(Color.Transparent, Icons.Default.Sync, "")
    }

    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(12.dp)
                    .then(if (status == SyncStatus.SYNCING) Modifier.rotate(rotation) else Modifier),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
            )
        }
    }
}
