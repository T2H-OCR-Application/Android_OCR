package com.t2h.ocr.ui.settings

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    onNavigateToScanner: () -> Unit = {}
) {
    val syncWifiOnly by viewModel.syncWifiOnly.collectAsState()
    val clearCacheOnSync by viewModel.clearCacheOnSync.collectAsState()
    val savedApiKey by viewModel.geminiApiKey.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var apiKeyInput by remember(savedApiKey) { mutableStateOf(savedApiKey) }
    var showKey by remember { mutableStateOf(false) }

    val currentTab = "Cài đặt"

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1D24))
            )
        },
        bottomBar = {
            SettingsBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { tabName ->
                    when (tabName) {
                        "Trang chủ" -> onNavigateToHome()
                        "Tệp" -> onNavigateToHistory()
                        "Công cụ" -> {}
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
                    .padding(vertical = 4.dp)
            ) {
                Column {
                    ToolsMenuItem(
                        iconRes = R.drawable.material_symbols_border_all_rounded,
                        title = "Quét tài liệu nhanh",
                        subtitle = "Nhận diện chữ tự động qua Camera AI",
                        onClick = onNavigateToScanner
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.06f))

                    ToolsMenuItem(
                        iconRes = R.drawable.tabler_photo_plus,
                        title = "Chụp & Nhập hình ảnh",
                        subtitle = "Trích xuất văn bản từ ảnh chụp sẵn có",
                        onClick = onNavigateToScanner
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.06f))

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

            // ─── PHẦN 2: GEMINI API KEY ───
            Text(
                text = "Cấu hình AI",
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
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Gemini API Key",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Key được lưu trên thiết bị, không gửi đi đâu ngoài Gemini API",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Nhập Gemini API Key...", color = Color.Gray, fontSize = 13.sp)
                        },
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showKey = !showKey }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (showKey) R.drawable.mingcute_user_4_line
                                        else R.drawable.mingcute_document_line
                                    ),
                                    contentDescription = if (showKey) "Ẩn key" else "Hiện key",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF14B8A6),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF14B8A6),
                            focusedContainerColor = Color(0xFF1A1D24),
                            unfocusedContainerColor = Color(0xFF1A1D24)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (apiKeyInput.isBlank()) {
                                Toast.makeText(context, "Vui lòng nhập API Key", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.saveGeminiApiKey(apiKeyInput)
                                focusManager.clearFocus()
                                Toast.makeText(context, "Đã lưu API Key", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B8A6)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Lưu API Key", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ─── PHẦN 3: KHỐI BENTO CÀI ĐẶT HỆ THỐNG ───
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
                    SettingsToggleItem(
                        title = stringResource(R.string.settings_wifi_sync_title),
                        description = stringResource(R.string.settings_wifi_sync_desc),
                        iconRes = R.drawable.tdesign_tools_circle,
                        checked = syncWifiOnly,
                        onCheckedChange = { viewModel.setSyncWifiOnly(it) }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.06f))

                    SettingsToggleItem(
                        title = stringResource(R.string.settings_clear_cache_sync_title),
                        description = stringResource(R.string.settings_clear_cache_sync_desc),
                        iconRes = R.drawable.material_symbols_border_all_rounded,
                        checked = clearCacheOnSync,
                        onCheckedChange = { viewModel.setClearCacheOnSync(it) }
                    )
                }
            }

            // ─── PHẦN 4: DỌN DẸP BỘ NHỚ ───
            val cacheClearedMsg = stringResource(R.string.toast_cache_cleared)
            val cacheClearFailedMsg = stringResource(R.string.toast_cache_clear_failed)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.1f))
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
            Text(text = description, color = Color.Gray, fontSize = 11.sp)
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

@Composable
private fun SettingsBottomNavigation(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onCenterClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                    title = "Cài đặt",
                    iconRes = R.drawable.tdesign_tools_circle,
                    isSelected = currentTab == "Cài đặt",
                    onClick = { onTabSelected("Cài đặt") }
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
