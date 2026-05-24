package com.t2h.ocr.ui.results

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.t2h.ocr.R

/**
 * Screen where users can review and edit recognized text before saving it as a PDF.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    viewModel: ResultsViewModel = viewModel(),
    pages: List<String>,
    imagePath: String,
    allImagePaths: List<String>,
    onSaveComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var editedText by remember { mutableStateOf(pages.joinToString("\n\n")) }
    val isSaving by viewModel.isSaving.collectAsState()
    val errorState by viewModel.errorState.collectAsState()

    if (errorState == "critical_sync_error") {
        com.t2h.ocr.ui.components.SyncErrorState(onRetry = { viewModel.clearError() })
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (pages.size > 1) stringResource(R.string.results_title_batch) else stringResource(R.string.results_title_scan)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    TextField(
                        value = editedText,
                        onValueChange = { editedText = it },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        label = { Text(stringResource(R.string.results_label_text)) },
                        enabled = !isSaving
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            // If it's a batch, we might want to preserve page breaks.
                            // For now, we split by double newline as a heuristic if it was a batch.
                            val pagesToSave = if (pages.size > 1) {
                                editedText.split("\n\n").filter { it.isNotBlank() }
                            } else {
                                listOf(editedText)
                            }

                            viewModel.saveBatchScan(
                                context = context,
                                pages = pagesToSave,
                                firstImagePath = imagePath,
                                allImagePaths = allImagePaths,
                                onComplete = onSaveComplete
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.results_save_pdf))
                        }
                    }
                }
                
                if (isSaving) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Black.copy(alpha = 0.3f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
