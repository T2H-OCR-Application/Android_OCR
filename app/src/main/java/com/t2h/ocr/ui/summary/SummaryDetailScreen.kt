package com.t2h.ocr.ui.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t2h.ocr.data.models.SummaryMetadata
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BrandGold  = Color(0xFFD4AF37)
private val BrandTeal  = Color(0xFF14B8A6)
private val SurfaceDark     = Color(0xFF252329)
private val BackgroundDark  = Color(0xFF1A1D24)
private val TextPrimary     = Color(0xFFF1F5F9)
private val TextSecondary   = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryDetailScreen(
    summary: SummaryMetadata,
    onBack: () -> Unit
) {
    val dateStr = remember(summary.timestamp) {
        if (summary.timestamp > 0)
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(summary.timestamp))
        else ""
    }
    val wordCount = remember(summary.summaryText) {
        summary.summaryText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = summary.title.ifBlank { "Tóm tắt" },
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── Header card ───
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Icon AI
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandGold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BrandGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (dateStr.isNotBlank()) {
                            Text(
                                text = dateStr,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (wordCount > 0) {
                                MetaBadge(
                                    text = "$wordCount từ",
                                    color = BrandGold
                                )
                            }
                            if (summary.language.isNotBlank()) {
                                MetaBadge(
                                    text = summary.language.uppercase(),
                                    color = BrandTeal
                                )
                            }
                            MetaBadge(
                                text = if (summary.isSynced) "☁ Drive" else "📱 Cục bộ",
                                color = if (summary.isSynced) BrandTeal else TextSecondary
                            )
                        }
                    }
                }
            }

            // ─── Nội dung tóm tắt ───
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Nội dung tóm tắt",
                        color = BrandGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Divider(color = Color.White.copy(alpha = 0.08f))

                    if (summary.summaryText.isBlank()) {
                        Text(
                            text = "Không có nội dung.",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = summary.summaryText,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            // ─── Nguồn tài liệu gốc ───
            if (summary.sourcePdfPath.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Tài liệu gốc",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = summary.sourcePdfPath.substringAfterLast("/"),
                            color = TextPrimary.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MetaBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
