package com.t2h.ocr.ui.summary

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.t2h.ocr.data.ScanRepository
import com.t2h.ocr.data.SummaryRepository
import com.t2h.ocr.data.models.ScanMetadata
import java.text.SimpleDateFormat
import java.util.*

// ─── Màu thương hiệu ───
private val BrandTeal = Color(0xFF14B8A6)
private val BrandGold = Color(0xFFD4AF37)
private val SurfaceDark = Color(0xFF252329)
private val BackgroundDark = Color(0xFF1A1D24)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    scanRepository: ScanRepository,
    summaryRepository: SummaryRepository,
    onNavigateBack: () -> Unit,
    context: Context = LocalContext.current
) {
    val viewModel: SummaryViewModel = viewModel(
        factory = SummaryViewModelFactory(scanRepository, summaryRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val scans by scanRepository.scans.collectAsState(initial = emptyList())

    // Lọc bỏ các scan không có nội dung OCR (không thể tóm tắt)
    val summaryableScans = remember(scans) {
        scans.filter { it.ocrText.isNotBlank() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BrandGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Tóm tắt AI",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "summary_state"
            ) { state ->
                when (state) {
                    // ─── BƯỚC 1: CHỌN TÀI LIỆU ───
                    is SummaryUiState.SelectingScan -> {
                        SelectingScanStep(
                            scans = summaryableScans.sortedByDescending { it.timestamp },
                            onScanSelected = { viewModel.startSummary(context, it) }
                        )
                    }

                    // ─── BƯỚC 2: ĐANG TÓM TẮT ───
                    is SummaryUiState.Summarizing -> {
                        SummarizingStep(progress = state.progress)
                    }

                    // ─── BƯỚC 3: CHỈNH SỬA & LƯU ───
                    is SummaryUiState.Editing -> {
                        EditingStep(
                            summaryText = state.summaryText,
                            sourceScan = state.sourceScan,
                            onSave = { editedText ->
                                viewModel.saveSummary(
                                    context,
                                    editedText,
                                    state.sourceScan,
                                    state.detectedLanguage
                                )
                            },
                            onDiscard = { viewModel.reset() }
                        )
                    }

                    // ─── ĐANG LƯU ───
                    is SummaryUiState.Saving -> {
                        SummarizingStep(progress = "Đang lưu PDF...")
                    }

                    // ─── LƯU THÀNH CÔNG ───
                    is SummaryUiState.Saved -> {
                        SavedStep(
                            title = state.summaryTitle,
                            onDone = { viewModel.reset() }
                        )
                    }

                    // ─── LỖI ───
                    is SummaryUiState.Error -> {
                        ErrorStep(
                            message = state.message,
                            onRetry = { viewModel.reset() }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// BƯỚC 1 — Danh sách chọn tài liệu
// ─────────────────────────────────────────────
@Composable
private fun SelectingScanStep(
    scans: List<ScanMetadata>,
    onScanSelected: (ScanMetadata) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header mô tả
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceDark)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Chọn tài liệu cần tóm tắt",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "AI sẽ rút gọn nội dung xuống còn ~30–40% độ dài gốc, giữ nguyên ngôn ngữ.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        if (scans.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Chưa có tài liệu nào",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Hãy quét tài liệu có văn bản trước rồi quay lại đây.",
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(scans, key = { it.id }) { scan ->
                    ScanItemCard(scan = scan, onClick = { onScanSelected(scan) })
                }
            }
        }
    }
}

@Composable
private fun ScanItemCard(scan: ScanMetadata, onClick: () -> Unit) {
    val dateStr = remember(scan.timestamp) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(scan.timestamp))
    }
    val wordCount = remember(scan.ocrText) {
        scan.ocrText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BrandTeal.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = BrandTeal,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = scan.title.ifBlank { "Tài liệu không tên" },
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = dateStr, color = Color.Gray, fontSize = 11.sp)
            Text(
                text = "$wordCount từ",
                color = BrandTeal.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }

        Text(text = "›", color = Color.Gray, fontSize = 22.sp)
    }
}

// ─────────────────────────────────────────────
// BƯỚC 2 — Loading spinner
// ─────────────────────────────────────────────
@Composable
private fun SummarizingStep(progress: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            CircularProgressIndicator(color = BrandTeal, strokeWidth = 3.dp)
            Text(text = progress, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = "Gemini đang xử lý văn bản...", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

// ─────────────────────────────────────────────
// BƯỚC 3 — Chỉnh sửa kết quả tóm tắt
// ─────────────────────────────────────────────
@Composable
private fun EditingStep(
    summaryText: String,
    sourceScan: ScanMetadata,
    onSave: (String) -> Unit,
    onDiscard: () -> Unit
) {
    // Local state để chỉnh sửa — init từ kết quả AI
    var editedText by remember(summaryText) { mutableStateOf(summaryText) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Nhãn tài liệu gốc
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = BrandGold,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Tóm tắt từ: ${sourceScan.title.ifBlank { "Tài liệu không tên" }}",
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Gợi ý chỉnh sửa
        Text(
            text = "Bạn có thể chỉnh sửa nội dung trước khi lưu.",
            color = BrandTeal.copy(alpha = 0.7f),
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // TextField có thể chỉnh sửa
        OutlinedTextField(
            value = editedText,
            onValueChange = { editedText = it },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            textStyle = LocalTextStyle.current.copy(
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 22.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = BrandTeal,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                cursorColor = BrandTeal
            ),
            shape = RoundedCornerShape(16.dp)
        )

        // Thanh nút ở đáy
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(SurfaceDark)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDiscard,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f))
                ) {
                    Text("Không lưu", fontSize = 14.sp)
                }

                Button(
                    onClick = { onSave(editedText) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTeal,
                        contentColor = Color.White
                    )
                ) {
                    Text("Lưu PDF", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// SAVED — Thông báo lưu thành công
// ─────────────────────────────────────────────
@Composable
private fun SavedStep(title: String, onDone: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = BrandTeal,
                modifier = Modifier.size(64.dp)
            )
            Text(text = "Đã lưu thành công!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = title, color = Color.Gray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDone,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandTeal,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Tóm tắt tài liệu khác", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────
// ERROR — Hiển thị lỗi + nút thử lại
// ─────────────────────────────────────────────
@Composable
private fun ErrorStep(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "⚠️", fontSize = 48.sp)
            Text(text = "Có lỗi xảy ra", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = message, color = Color.Gray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandTeal,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Thử lại", fontWeight = FontWeight.Bold)
            }
        }
    }
}
