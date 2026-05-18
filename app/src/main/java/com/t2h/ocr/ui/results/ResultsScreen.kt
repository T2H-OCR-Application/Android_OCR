package com.t2h.ocr.ui.results

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.text.Text as VisionText
import com.t2h.ocr.data.local.JsonStorage
import com.t2h.ocr.data.models.ScanMetadata
import com.t2h.ocr.domain.ocr.PdfGenerator
import java.io.File
import java.io.FileOutputStream
import java.util.*

/**
 * Screen where users can review and edit recognized text before saving it as a PDF.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    recognizedText: VisionText,
    capturedBitmap: Bitmap,
    onSaveComplete: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var editedText by remember { mutableStateOf(recognizedText.text) }
    val jsonStorage = remember { JsonStorage(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Scan") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            TextField(
                value = editedText,
                onValueChange = { editedText = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = { Text("Recognized Text") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Implementation of saving logic
                    val id = UUID.randomUUID().toString()
                    val timestamp = System.currentTimeMillis()
                    
                    // 1. Save Image to app-private storage
                    val imageFile = File(context.filesDir, "image_$id.jpg")
                    FileOutputStream(imageFile).use { out ->
                        capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }

                    // 2. Generate searchable PDF
                    val pdfFile = File(context.filesDir, "doc_$id.pdf")
                    FileOutputStream(pdfFile).use { out ->
                        PdfGenerator.generateSearchablePdf(capturedBitmap, recognizedText, out)
                    }

                    // 3. Persist metadata locally
                    val metadata = ScanMetadata(
                        id = id,
                        title = "Scan ${Date(timestamp)}",
                        timestamp = timestamp,
                        ocrText = editedText,
                        imagePath = imageFile.absolutePath,
                        pdfPath = pdfFile.absolutePath,
                        language = "en"
                    )
                    jsonStorage.addScan(metadata)
                    
                    onSaveComplete()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save as PDF")
            }
        }
    }
}
