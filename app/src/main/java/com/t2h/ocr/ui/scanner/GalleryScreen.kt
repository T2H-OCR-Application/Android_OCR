package com.t2h.ocr.ui.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import com.t2h.ocr.R
import com.t2h.ocr.data.models.ScannedPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    pages: List<ScannedPage>,
    onAddMore: () -> Unit,
    onRemovePage: (String) -> Unit,
    onFinish: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Xem trước trang quét",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        // Badge hiển thị số lượng trang nhỏ gọn, tinh tế
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF14B8A6).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${pages.size} trang",
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
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1D24))
            )
        },
        floatingActionButton = {
            // Nút Thêm trang được nhuộm màu xanh ngọc đồng bộ hệ thống
            FloatingActionButton(
                onClick = onAddMore,
                containerColor = Color(0xFF14B8A6),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 80.dp) // Tránh đè lên thanh bento nút nhấn ở đáy
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.gallery_add_page))
            }
        },
        containerColor = Color(0xFF1A1D24)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1A1D24))
        ) {
            // ─── TRẠNG THÁI TRỐNG ───
            if (pages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.gallery_no_pages),
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                // ─── DANH SÁCH CÁC TRANG (BENTO ITEM) ───
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(pages, key = { _, page -> page.id }) { index, page ->
                        PageItem(
                            index = index + 1, // Hiển thị số thứ tự Trang 1, Trang 2...
                            page = page,
                            onRemove = { onRemovePage(page.id) }
                        )
                    }
                }
            }

            // ─── KHỐI BENTO CHO NÚT HOÀN THÀNH Ở ĐÁY ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(Color(0xFF252329))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Button(
                    onClick = onFinish,
                    enabled = pages.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF14B8A6),
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                        disabledContentColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.gallery_generate_pdf),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PageItem(
    index: Int,
    page: ScannedPage,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252329)) // Màu hộp bento tối mượt mà
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .height(96.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ảnh Thumbnail trang quét (Được bo góc gọn gàng)
            AsyncImage(
                model = page.imagePath,
                contentDescription = null,
                modifier = Modifier
                    .width(72.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1A1D24)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Nội dung văn bản OCR nhận diện được
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Trang $index",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    Text(
                        text = page.text.trim().replace("\n", " ").ifEmpty { "Không nhận diện được ký tự nào..." },
                        color = if (page.text.isEmpty()) Color.Gray.copy(alpha = 0.6f) else Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Nút xóa trang được làm mờ tinh tế bằng màu đỏ nhạt, tránh gây rối mắt
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = stringResource(R.string.gallery_remove_page),
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}