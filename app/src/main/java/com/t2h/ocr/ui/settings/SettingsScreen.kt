package com.t2h.ocr.ui.settings

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t2h.ocr.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {} // Gọi khi bấm quét nhanh/chụp ảnh
) {
    val syncWifiOnly by viewModel.syncWifiOnly.collectAsState()
    val clearCacheOnSync by viewModel.clearCacheOnSync.collectAsState()
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("Công cụ") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Công cụ ứng dụng",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        bottomBar = {
            SettingsBottomNavigation(
                currentTab = currentTab,
                onTabSelected = {
                    currentTab = it
                    when (it) {
                        "Trang chủ" -> onNavigateToHome()
                        "Tệp" -> onNavigateToHistory()
                        "Hồ sơ" -> onNavigateToProfile()
                    }
                },
                onCenterClick = onNavigateToScanner
            )
        },
        containerColor = Color(0xFF1A1D24)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF1A1D24))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ─── PHẦN 1: KHỐI BENTO CÔNG CỤ XỬ LÝ ───
            Text(
                text = "Công cụ OCR & Tiện ích",
                color = Color(0xFF14B8A6), // Màu xanh ngọc thương hiệu
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF252329))
                    .padding(vertical = 4.dp)
            ) {
                Column {
                    // 1. Công cụ Quét văn bản nhanh
                    ToolsMenuItem(
                        iconRes = R.drawable.material_symbols_border_all_rounded, // Tận dụng icon ô vuông/quét
                        title = "Quét tài liệu nhanh",
                        subtitle = "Nhận diện chữ tự động qua Camera AI",
                        onClick = onNavigateToScanner
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.06f))

                    // 2. Công cụ Chụp ảnh / Nhập từ thư viện
                    ToolsMenuItem(
                        iconRes = R.drawable.tabler_photo_plus,
                        title = "Chụp & Nhập hình ảnh",
                        subtitle = "Trích xuất văn bản từ ảnh chụp sẵn có",
                        onClick = onNavigateToScanner
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.06f))

                    // 3. Công cụ chuyển đổi định dạng tài liệu (PDF, Word, Docx)
                    ToolsMenuItem(
                        iconRes = R.drawable.mingcute_document_line,
                        title = "Chuyển đổi sang định dạng khác",
                        subtitle = "Xuất kết quả OCR sang tệp PDF, DOCX, TXT",
                        onClick = {
                            Toast.makeText(context, "Chọn tệp từ mục 'Tập tin của tôi' hoặc 'Lịch sử' để tiến hành xuất PDF", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }

            // ─── PHẦN 2: KHỐI BENTO CÀI ĐẶT HỆ THỐNG ───
            Text(
                text = stringResource(R.string.settings_resource_mgmt),
                color = Color(0xFF14B8A6),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF252329))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column {
                    // Wi-Fi Only Sync Toggle
                    SettingsToggleItem(
                        title = stringResource(R.string.settings_wifi_sync_title),
                        description = stringResource(R.string.settings_wifi_sync_desc),
                        iconRes = R.drawable.tdesign_tools_circle, // Hoặc thay bằng icon Wifi nếu bạn có
                        checked = syncWifiOnly,
                        onCheckedChange = { viewModel.setSyncWifiOnly(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.06f))

                    // Clear Cache on Sync Toggle
                    SettingsToggleItem(
                        title = stringResource(R.string.settings_clear_cache_sync_title),
                        description = stringResource(R.string.settings_clear_cache_sync_desc),
                        iconRes = R.drawable.material_symbols_border_all_rounded,
                        checked = clearCacheOnSync,
                        onCheckedChange = { viewModel.setClearCacheOnSync(it) }
                    )
                }
            }

            // ─── PHẦN 3: DỌN DẸP BỘ NHỚ ───
            val cacheClearedMsg = stringResource(R.string.toast_cache_cleared)
            val cacheClearFailedMsg = stringResource(R.string.toast_cache_clear_failed)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.1f)) // Màu đỏ mờ tinh tế
                    .clickable {
                        val success = viewModel.clearLocalCache()
                        if (success) {
                            Toast.makeText(context, cacheClearedMsg, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, cacheClearFailedMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.settings_clear_cache_btn),
                    color = Color(0xFFEF4444),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// --- DÒNG ITEM CHO MỤC CÔNG CỤ (MENU CLICK) ---
@Composable
private fun ToolsMenuItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1D24)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                tint = Color(0xFF14B8A6),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = Color.Gray, fontSize = 11.sp)
        }

        Icon(
            painter = painterResource(id = R.drawable.codicon_new_file),
            contentDescription = "Arrow Right",
            tint = Color.Gray.copy(alpha = 0.4f),
            modifier = Modifier.size(14.dp)
        )
    }
}

// --- DÒNG ITEM BẬT TẮT ĐỒNG BỘ (TOGGLE SWITCH) ---
@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    iconRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1D24)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color(0xFF14B8A6),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1.0f)) {
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(
                text = description,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF14B8A6),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF1A1D24)
            )
        )
    }
}

// --- COMPONENT THANH ĐIỀU HƯỚNG DƯỚI (BOTTOM NAVIGATION) ---
@Composable
private fun SettingsBottomNavigation(
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

            // NÚT CHÍNH GIỮA TRÒN XANH NGỌC
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF14B8A6))
                    .clickable { onCenterClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon_add_file),
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