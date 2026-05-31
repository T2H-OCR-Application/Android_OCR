package com.t2h.ocr.ui.fileP

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t2h.ocr.R
import com.t2h.ocr.data.ScanRepository
import com.t2h.ocr.data.models.ScanMetadata
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DocumentFile(
    val id: String,
    val name: String,
    val date: String,
    val size: String,
    val pageCount: Int,
    val isPdf: Boolean
)

private fun ScanMetadata.toDocumentFile(): DocumentFile {
    val path = if (pdfPath.isNotBlank()) pdfPath else imagePath
    val file = if (path.isNotBlank()) File(path) else null
    val displayName = title.ifBlank { file?.name ?: "Tệp không tên" }
    val dateText = if (timestamp > 0) formatTimestamp(timestamp) else ""
    val sizeText = if (pdfPath.isNotBlank()) "" else file?.let { formatFileSize(it.length()) } ?: ""
    return DocumentFile(
        id = id,
        name = displayName,
        date = dateText,
        size = sizeText,
        pageCount = if (pdfPath.isNotBlank()) 1 else 1,
        isPdf = pdfPath.isNotBlank()
    )
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.getDefault(), "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    scanRepository: ScanRepository,
    onNavigateToSection: (String) -> Unit = {},
    onCenterFabClick: () -> Unit = {},
    onOpenScan: (ScanMetadata) -> Unit = {},
    onDeleteScan: (ScanMetadata) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    // Thiết lập trạng thái tab hiện tại mặc định là "Tệp" khi vào màn hình này
    var currentTab by remember { mutableStateOf("Tệp") }

    val scans by scanRepository.scans.collectAsState(initial = emptyList())
    val filteredScans = remember(scans, searchQuery) {
        scans.filter { scan ->
            searchQuery.isBlank() ||
                    scan.title.contains(searchQuery, ignoreCase = true) ||
                    scan.ocrText.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quản lý tệp tin",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1D24))
            )
        },
        bottomBar = {
            HomeBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { tabName ->
                    currentTab = tabName
                    // ─── ĐÃ SỬA: Nhấn tab nào điều hướng chuẩn sang màn đó dựa theo MainActivity ───
                    when (tabName) {
                        "Trang chủ" -> onNavigateToSection("Trang chủ")
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
            // ─── 1. THANH TÌM KIẾM TẬP TIN ───
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(56.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                },
                placeholder = {
                    Text(
                        text = "Tìm kiếm tên tệp, văn bản...",
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF252329),
                    unfocusedContainerColor = Color(0xFF252329),
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLeadingIconColor = Color.Gray,
                    unfocusedLeadingIconColor = Color.Gray,
                    focusedPlaceholderColor = Color.Gray.copy(alpha = 0.6f),
                    unfocusedPlaceholderColor = Color.Gray.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // ─── 2. DANH SÁCH FILE BENTO LIST ───
            if (filteredScans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "Thư mục lịch sử trống" else "Không tìm thấy tệp",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredScans, key = { it.id }) { scan ->
                        FileItemRow(
                            file = scan.toDocumentFile(),
                            onClick = { onOpenScan(scan) },
                            onDelete = { onDeleteScan(scan) }
                        )
                    }
                }
            }
        }
    }
}

// ─── COMPONENT ITEM TỪNG TỆP TIN ───
@Composable
fun FileItemRow(
    file: DocumentFile,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252329))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (file.isPdf) Color(0xFFEF4444).copy(alpha = 0.15f)
                        else Color(0xFF14B8A6).copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (file.isPdf) Icons.Default.PictureAsPdf else Icons.Default.TextSnippet,
                    contentDescription = null,
                    tint = if (file.isPdf) Color(0xFFEF4444) else Color(0xFF14B8A6),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = file.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = file.date, color = Color.Gray, fontSize = 12.sp)
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(Color.DarkGray))
                    Text(
                        text = if (file.isPdf) "${file.pageCount} trang" else file.size,
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete File",
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── COMPONENT BOTTOM NAVIGATION TRÀN CẠNH ───
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

            // NÚT CHÍNH GIỮA (CAMERA ACTION) - Giữ màu xanh ngọc làm điểm nhấn nổi bật riêng biệt
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
            // ─── ĐÃ KHÔI PHỤC: Chuyển lại màu vàng hổ phách (0xFFD4AF37) chuẩn xác khi tab được chọn ───
            tint = if (isSelected) Color(0xFFD4AF37) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            // ─── ĐÃ KHÔI PHỤC: Màu chữ vàng khi active giống hệt icon ───
            color = if (isSelected) Color(0xFFD4AF37) else Color.Gray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}