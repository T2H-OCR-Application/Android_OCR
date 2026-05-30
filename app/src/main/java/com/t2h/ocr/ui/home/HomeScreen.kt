package com.t2h.ocr.ui.home

import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.t2h.ocr.R
import java.io.File

@Composable
fun HomeScreen(
    onNavigateToSection: (String) -> Unit = {},
    onCenterFabClick: () -> Unit = {},
    recentHistory: List<HistoryItemData> = emptyList(),
    onRecentItemClick: (HistoryItemData) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentTab by remember { mutableStateOf("Trang chủ") }
    val filteredRecent = remember(searchQuery, recentHistory) {
        if (searchQuery.isBlank()) {
            recentHistory
        } else {
            recentHistory.filter { item ->
                item.title.contains(searchQuery, ignoreCase = true) ||
                        item.timeString.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        bottomBar = {
            HomeBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { tabName ->
                    currentTab = tabName
                    when (tabName) {
                        "Tệp" -> onNavigateToSection("Tệp")
                        "Công cụ" -> onNavigateToSection("Công cụ")
                        "Hồ sơ" -> onNavigateToSection("Hồ sơ")
                    }
                },
                onCenterClick = onCenterFabClick
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

            // 2. LƯỚI CHỨC NĂNG (QUÉT, VĂN BẢN, AI, TẬP TIN, PDF, ẢNH, TẤT CẢ)
            val categories = listOf(
                CategoryItem("Quét", R.drawable.streamline_scanner_solid),
                CategoryItem("Văn bản", R.drawable.f7_doc_text),
                CategoryItem("AI", R.drawable.mingcute_ai_line),
                CategoryItem("Tập tin", R.drawable.icon_add_file),
                CategoryItem("PDF", R.drawable.pdf_icon),
                CategoryItem("Ảnh", R.drawable.photo),
                CategoryItem("Tất cả", R.drawable.material_symbols_border_all_rounded)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                categories.take(4).forEach { category ->
                    CategoryButton(category = category, onClick = { onNavigateToSection(category.title) })
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                categories.drop(4).forEach { category ->
                    CategoryButton(
                        category = category,
                        onClick = {
                            if (category.title == "Tất cả") onNavigateToSection("Tệp")
                            else onNavigateToSection(category.title)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. VÙNG HIỂN THỊ DANH SÁCH "GẦN ĐÂY"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF252329))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header Gần đây
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
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
                            modifier = Modifier.clickable {
                                onNavigateToSection("Xem tất cả") // Chuyển sang danh sách lịch sử đầy đủ
                            }
                        )
                    }

                    if (filteredRecent.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.bi_file_text),
                                contentDescription = "No Data",
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isBlank()) "Không có dữ liệu" else "Không tìm thấy kết quả phù hợp",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                onClick = { onCenterFabClick() },
                                color = Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF14B8A6))
                            ) {
                                Text(
                                    text = "Thêm dữ liệu",
                                    color = Color(0xFF14B8A6),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredRecent.take(4), key = { it.id }) { item ->
                                RecentHistoryItem(
                                    item = item,
                                    onClick = { onRecentItemClick(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENT NÚT CHỨC NĂNG PHÂN LOẠI ---
data class CategoryItem(val title: String, val iconRes: Int)

@Composable
private fun CategoryButton(
    category: CategoryItem,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFF252329)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = category.iconRes),
                contentDescription = category.title,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.title,
            color = Color.LightGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun RecentHistoryItem(
    item: HistoryItemData,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (!item.imagePath.isNullOrBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(File(item.imagePath)),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.bi_file_text),
                    contentDescription = item.title,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.timeString,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

// --- COMPONENT THANH ĐIỀU HƯỚNG DƯỚI TRÀN CẠNH ---
@Composable
private fun HomeBottomNavigation(
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

            // NÚT CHÍNH GIỮA NỔI BẬT (CAMERA ACTION) - Giữ màu xanh ngọc thương hiệu
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
            // ─── ĐÃ KHÔI PHỤC: Trả lại màu vàng hổ phách (0xFFD4AF37) khi tab được chọn ───
            tint = if (isSelected) Color(0xFFD4AF37) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            // ─── ĐÃ KHÔI PHỤC: Trả lại màu chữ vàng khi active ───
            color = if (isSelected) Color(0xFFD4AF37) else Color.Gray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}