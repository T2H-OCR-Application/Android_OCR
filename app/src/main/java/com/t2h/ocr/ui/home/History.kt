package com.t2h.ocr.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.t2h.ocr.R
import java.io.File

// Model dữ liệu mẫu phù hợp với cấu trúc OCR
data class HistoryItemData(
    val id: String,
    val title: String,
    val timeString: String, // Ví dụ: "Hôm qua", "2 giờ trước"
    val imagePath: String? = null
)

@Composable
fun HistoryScreen(
    historyList: List<HistoryItemData> = emptyList(),
    onItemClick: (HistoryItemData) -> Unit = {},
    onEditItemClick: (HistoryItemData) -> Unit = {},
    onNavigateToSection: (String) -> Unit = {}, // Callback điều hướng lọt về MainActivity
    onCenterFabClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    // Đóng đinh Tab hiện tại là "Tệp" (hoặc "Trang chủ" tùy cấu trúc phân nhánh app của bạn)
    // để sáng chuẩn màu vàng hổ phách 0xFFD4AF37
    val currentTab = "Tệp"

    // Tự động lọc danh sách dựa trên những gì người dùng gõ vào ô Tìm kiếm
    val filteredHistory = remember(searchQuery, historyList) {
        if (searchQuery.isBlank()) {
            historyList
        } else {
            historyList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.timeString.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        bottomBar = {
            HistoryBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { tabName ->
                    // ─── ĐÃ SỬA: Kích hoạt callback định tuyến đẩy ngược sự kiện về MainActivity ───
                    when (tabName) {
                        "Trang chủ" -> onNavigateToSection("Trang chủ")
                        "Tệp" -> { /* Đang ở chính màn hình lịch sử/tệp, không xử lý lại */ }
                        "Công cụ" -> onNavigateToSection("Công cụ")
                        "Hồ sơ" -> onNavigateToSection("Hồ sơ")
                    }
                },
                onCenterClick = onCenterFabClick // Nhấn nút giữa mở máy ảnh quét nhanh
            )
        },
        containerColor = Color(0xFF1A1D24)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFF1A1D24))
        ) {

            // ─── 1. THANH TÌM KIẾM ĐÃ ĐƯỢC KÍCH HOẠT ĐỂ GÕ CHỮ ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF252329))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Tìm kiếm kết quả OCR...",
                                color = Color.Gray.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            cursorBrush = SolidColor(Color(0xFF14B8A6)),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ─── 2. VÙNG KHUNG CHỨA DANH SÁCH KHỐI BENTO ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF252329))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Header tiêu đề vùng
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Gần Đây" else "Kết quả tìm kiếm (${filteredHistory.size})",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (searchQuery.isBlank()) {
                            Text(
                                text = "Xem tất cả",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.clickable { onNavigateToSection("Xem tất cả") }
                            )
                        }
                    }

                    // Danh sách cuộn LazyColumn
                    if (filteredHistory.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "Không có dữ liệu gần đây" else "Không tìm thấy kết quả phù hàng",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredHistory, key = { it.id }) { item ->
                                HistoryRowItem(
                                    item = item,
                                    onClick = { onItemClick(item) },
                                    onEditClick = { onEditItemClick(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRowItem(
    item: HistoryItemData,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Khối hiển thị ảnh đại diện bên trái
        if (!item.imagePath.isNullOrBlank()) {
            Image(
                painter = rememberAsyncImagePainter(File(item.imagePath)),
                contentDescription = "Scanned Image",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.15f))
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))

                // Icon Chỉnh sửa
                Icon(
                    painter = painterResource(id = R.drawable.codicon_new_file),
                    contentDescription = "Edit title",
                    tint = Color.LightGray.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onEditClick() }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Đã đổi sang icon đồng hồ/lịch phù hợp hơn cho thông số thời gian nếu có trong drawable của bạn
                Icon(
                    painter = painterResource(id = R.drawable.material_symbols_border_all_rounded),
                    contentDescription = "Time icon",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.timeString,
                    color = Color.Gray.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun HistoryBottomNavigation(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onCenterClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFF1A1D24))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.1f))
                .align(Alignment.TopCenter)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavigationItem(
                title = "Trang chủ",
                iconRes = R.drawable.material_symbols_home_outline_rounded,
                isSelected = currentTab == "Trang chủ",
                onClick = { onTabSelected("Trang chủ") }
            )

            NavigationItem(
                title = "Tệp",
                iconRes = R.drawable.mingcute_document_line,
                isSelected = currentTab == "Tệp",
                onClick = { onTabSelected("Tệp") }
            )

            // NÚT CHÍNH GIỮA TRÒN XANH NGỌC MỞ CAMERA CAMERA QUÉT NHANH
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF14B8A6))
                    .clickable { onCenterClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tabler_photo_plus),
                    contentDescription = "Center Action",
                    modifier = Modifier.size(24.dp)
                )
            }

            NavigationItem(
                title = "Công cụ",
                iconRes = R.drawable.tdesign_tools_circle,
                isSelected = currentTab == "Công cụ",
                onClick = { onTabSelected("Công cụ") }
            )

            NavigationItem(
                title = "Hồ sơ",
                iconRes = R.drawable.mingcute_user_4_line,
                isSelected = currentTab == "Hồ sơ",
                onClick = { onTabSelected("Hồ sơ") }
            )
        }
    }
}

@Composable
private fun NavigationItem(
    title: String,
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            tint = if (isSelected) Color(0xFFD4AF37) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (isSelected) Color(0xFFD4AF37) else Color.Gray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}