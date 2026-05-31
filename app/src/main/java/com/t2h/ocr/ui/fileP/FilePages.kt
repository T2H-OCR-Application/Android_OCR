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
import androidx.compose.material.icons.filled.AutoAwesome
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
import com.t2h.ocr.data.SummaryRepository
import com.t2h.ocr.data.models.ScanMetadata
import com.t2h.ocr.data.models.SummaryMetadata
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Màu dùng chung ───
private val BrandTeal = Color(0xFF14B8A6)
private val BrandGold = Color(0xFFD4AF37)
private val SurfaceDark = Color(0xFF252329)
private val BackgroundDark = Color(0xFF1A1D24)

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
        pageCount = 1,
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

// ─── Tab enum ───
private enum class FilesTab { SCANS, SUMMARIES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    scanRepository: ScanRepository,
    summaryRepository: SummaryRepository? = null,
    onNavigateToSection: (String) -> Unit = {},
    onCenterFabClick: () -> Unit = {},
    onOpenScan: (ScanMetadata) -> Unit = {},
    onDeleteScan: (ScanMetadata) -> Unit = {},
    onOpenSummary: (SummaryMetadata) -> Unit = {},
    onDeleteSummary: (SummaryMetadata) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentTab by remember { mutableStateOf("Tệp") }
    var selectedFilesTab by remember { mutableStateOf(FilesTab.SCANS) }

    val scans by scanRepository.scans.collectAsState(initial = emptyList())
    val summaries by (summaryRepository?.summaries
        ?: kotlinx.coroutines.flow.MutableStateFlow(emptyList())).collectAsState(initial = emptyList())

    val filteredScans = remember(scans, searchQuery) {
        scans.filter { scan ->
            searchQuery.isBlank() ||
                    scan.title.contains(searchQuery, ignoreCase = true) ||
                    scan.ocrText.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredSummaries = remember(summaries, searchQuery) {
        summaries.filter { summary ->
            searchQuery.isBlank() ||
                    summary.title.contains(searchQuery, ignoreCase = true) ||
                    summary.summaryText.contains(searchQuery, ignoreCase = true)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        bottomBar = {
            HomeBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { tabName ->
                    currentTab = tabName
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
        containerColor = BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
            // ─── 1. THANH TÌM KIẾM ───
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
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
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

            // ─── 2. TAB CHỌN: SCAN vs SUMMARY ───
            if (summaryRepository != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilesTabChip(
                        label = "Tài liệu quét",
                        count = filteredScans.size,
                        isSelected = selectedFilesTab == FilesTab.SCANS,
                        color = BrandTeal,
                        onClick = { selectedFilesTab = FilesTab.SCANS },
                        modifier = Modifier.weight(1f)
                    )
                    FilesTabChip(
                        label = "Tóm tắt AI",
                        count = filteredSummaries.size,
                        isSelected = selectedFilesTab == FilesTab.SUMMARIES,
                        color = BrandGold,
                        onClick = { selectedFilesTab = FilesTab.SUMMARIES },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ─── 3. DANH SÁCH ───
            when {
                summaryRepository == null || selectedFilesTab == FilesTab.SCANS -> {
                    ScansListSection(
                        scans = filteredScans,
                        searchQuery = searchQuery,
                        onOpenScan = onOpenScan,
                        onDeleteScan = onDeleteScan
                    )
                }
                selectedFilesTab == FilesTab.SUMMARIES -> {
                    SummariesListSection(
                        summaries = filteredSummaries,
                        searchQuery = searchQuery,
                        onOpenSummary = onOpenSummary,
                        onDeleteSummary = onDeleteSummary
                    )
                }
            }
        }
    }
}

// ─── Tab chip component ───
@Composable
private fun FilesTabChip(
    label: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) color.copy(alpha = 0.15f) else SurfaceDark)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (isSelected) color else Color.Gray,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$count",
                    color = if (isSelected) color else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─── Danh sách scan ───
@Composable
private fun ScansListSection(
    scans: List<ScanMetadata>,
    searchQuery: String,
    onOpenScan: (ScanMetadata) -> Unit,
    onDeleteScan: (ScanMetadata) -> Unit
) {
    if (scans.isEmpty()) {
        EmptyState(
            text = if (searchQuery.isBlank()) "Thư mục lịch sử trống" else "Không tìm thấy tệp"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(scans, key = { it.id }) { scan ->
                FileItemRow(
                    file = scan.toDocumentFile(),
                    onClick = { onOpenScan(scan) },
                    onDelete = { onDeleteScan(scan) }
                )
            }
        }
    }
}

// ─── Danh sách summary ───
@Composable
private fun SummariesListSection(
    summaries: List<SummaryMetadata>,
    searchQuery: String,
    onOpenSummary: (SummaryMetadata) -> Unit,
    onDeleteSummary: (SummaryMetadata) -> Unit
) {
    if (summaries.isEmpty()) {
        EmptyState(
            text = if (searchQuery.isBlank()) "Chưa có bản tóm tắt nào" else "Không tìm thấy tóm tắt"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(summaries, key = { it.id }) { summary ->
                SummaryItemRow(
                    summary = summary,
                    onClick = { onOpenSummary(summary) },
                    onDelete = { onDeleteSummary(summary) }
                )
            }
        }
    }
}

// ─── Empty state chung ───
@Composable
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.Gray, fontSize = 14.sp)
    }
}

// ─── COMPONENT ITEM TÀI LIỆU QUÉT ───
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
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
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
                        else BrandTeal.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (file.isPdf) Icons.Default.PictureAsPdf else Icons.Default.TextSnippet,
                    contentDescription = null,
                    tint = if (file.isPdf) Color(0xFFEF4444) else BrandTeal,
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
                    contentDescription = "Xóa",
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── COMPONENT ITEM TÓM TẮT AI ───
@Composable
private fun SummaryItemRow(
    summary: SummaryMetadata,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(summary.timestamp) {
        if (summary.timestamp > 0)
            SimpleDateFormat("dd MMM, yyyy HH:mm", Locale.getDefault()).format(Date(summary.timestamp))
        else ""
    }
    val wordCount = remember(summary.summaryText) {
        summary.summaryText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon tóm tắt AI — màu vàng hổ phách
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandGold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = BrandGold,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = summary.title.ifBlank { "Tóm tắt không tên" },
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
                    Text(text = dateStr, color = Color.Gray, fontSize = 12.sp)
                    if (wordCount > 0) {
                        Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(Color.DarkGray))
                        Text(text = "$wordCount từ", color = BrandGold.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
                // Badge synced/local
                if (summary.isSynced) {
                    Text(
                        text = "☁ Đã đồng bộ Drive",
                        color = BrandTeal.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        text = "📱 Lưu cục bộ",
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Xóa",
                    tint = Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── COMPONENT BOTTOM NAVIGATION ───
@Composable
private fun HomeBottomNavigation(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onCenterClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(BackgroundDark)
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

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(BrandTeal)
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

        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
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
            tint = if (isSelected) BrandGold else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = if (isSelected) BrandGold else Color.Gray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}
