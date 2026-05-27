package com.t2h.ocr.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Kết quả nhận diện",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            // Badge hiển thị chế độ quét Đơn hoặc Hàng loạt (Batch)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF14B8A6).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (pages.size > 1) "Hàng loạt (${pages.size})" else "Đơn trang",
                                    color = Color(0xFF14B8A6),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1D24))
                )
            },
            containerColor = Color(0xFF1A1D24) // Nền tối đồng bộ toàn app
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFF1A1D24))
            ) {
                // ─── KHỐI BENTO 1: VÙNG SOẠN THẢO VĂN BẢN ───
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF252329)) // Màu hộp bento tối mượt
                ) {
                    TextField(
                        value = editedText,
                        onValueChange = { editedText = it },
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 22.sp // Tăng khoảng cách dòng đọc văn bản không bị díu mắt
                        ),
                        placeholder = {
                            Text(
                                "Văn bản nhận diện trống...",
                                color = Color.Gray.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        },
                        enabled = !isSaving,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent, // Ẩn gạch chân khi focus
                            unfocusedIndicatorColor = Color.Transparent, // Ẩn gạch chân khi bình thường
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFF14B8A6) // Con trỏ chuột màu xanh ngọc
                        )
                    )
                }

                // ─── KHỐI BENTO 2: THANH CHỨA NÚT LƯU Ở ĐÁY ───
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(Color(0xFF252329))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF14B8A6), // Xanh ngọc thương hiệu
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF14B8A6).copy(alpha = 0.3f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Đang khởi tạo tệp PDF...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.SaveAs,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.results_save_pdf),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Lớp phủ mờ toàn màn hình kèm vòng quay Loading lớn khi đang lưu
            if (isSaving) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.4f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF252329)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.size(100.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF14B8A6))
                            }
                        }
                    }
                }
            }
        }
    }
}