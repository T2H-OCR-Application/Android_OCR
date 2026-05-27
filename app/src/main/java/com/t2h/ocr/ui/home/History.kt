package com.t2h.ocr.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.t2h.ocr.R
import java.io.File

// Model dữ liệu mẫu phù hợp với cấu trúc OCR của bạn
data class HistoryItemData(
    val id: String,
    val title: String,
    val timeString: String, // Ví dụ: "Hôm qua", "2 giờ trước"
    val imagePath: String? = null
)

@Composable
fun HistoryScreen(
    historyList: List<HistoryItemData> = emptyList(), // Truyền danh sách từ ViewModel vào đây
    onItemClick: (HistoryItemData) -> Unit = {},
    onEditItemClick: (HistoryItemData) -> Unit = {},
    onNavigateToSection: (String) -> Unit = {},
    onCenterFabClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentTab by remember { mutableStateOf("Trang chủ") }

    Scaffold(
        bottomBar = {
            HistoryBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                onCenterClick = onCenterFabClick
            )
        },
        containerColor = Color(0xFF1A1D24) // Nền tối ngoài cùng hệ thống
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFF1A1D24))
        ) {
            // 1. THANH TÌM KIẾM (SEARCH BAR)
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
                    Text(
                        text = "Tìm kiếm ....",
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 2. VÙNG KHUNG CHỨA DANH SÁCH "GẦN ĐÂY" ĐỔ DỌC
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF252329))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Gần đây
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gần Đây",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Xem tất cả",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { onNavigateToSection("Xem tất cả") }
                        )
                    }

                    // Danh sách cuộn mượt bằng LazyColumn
                    if (historyList.isEmpty()) {
                        // Trạng thái dự phòng nếu danh sách trống dữ liệu
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Không có dữ liệu gần đây", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(historyList, key = { it.id }) { item ->
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

// --- COMPONENT CON CHO MỖI DÒNG LỊCH SỬ QUÉT ---
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
            .background(Color(0xFF1E293B)) // Nền xanh đen tối của thẻ item theo ảnh mẫu
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
            // Nếu không có ảnh -> Hiển thị ô vuông màu trắng nhạt chuẩn theo yêu cầu của bạn
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.15f))
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Cột chứa thông tin văn bản tiêu đề và thời gian
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            // Hàng tiêu đề + Nút sửa đổi nhỏ bên cạnh
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
                // Icon chỉnh sửa nhỏ cạnh tiêu đề (Giống hình cái bảng viết vẽ trong ảnh của bạn)
                Icon(
                    painter = painterResource(id = R.drawable.codicon_new_file), // Hoặc map icon edit tùy ý trong drawable của bạn
                    contentDescription = "Edit title",
                    tint = Color.LightGray.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { onEditClick() }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Hàng hiển thị thời gian quét kèm icon đồng hồ
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.tabler_photo_plus), // Dùng icon thời gian/đồng hồ phù hợp
                    contentDescription = "Time icon",
                    tint = Color(0xFF3B82F6), // Màu xanh lam dịu nhẹ đổ bóng icon thời gian theo mẫu
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

// --- COMPONENT THANH ĐIỀU HƯỚNG DƯỚI (BOTTOM NAVIGATION) ---
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

            // NÚT CHÍNH GIỮA TRÒN XANH NGỌC CÓ ICON THÊM ẢNH (+)
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