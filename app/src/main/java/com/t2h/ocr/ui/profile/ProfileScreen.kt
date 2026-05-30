package com.t2h.ocr.ui.profile

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth
import com.t2h.ocr.R
import com.t2h.ocr.data.auth.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authRepository: AuthRepository,
    onNavigateBack: () -> Unit, // Giữ lại ở tham số đầu vào để tránh lỗi biên dịch hệ thống
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {}, // Callback định tuyến đá người dùng về trang Đăng nhập
    onNavigateToScanner: () -> Unit = {} // Hỗ trợ nút chụp ảnh nhanh ở BottomBar
) {
    val context = LocalContext.current
    val currentUser by authRepository.currentUser.collectAsState(initial = FirebaseAuth.getInstance().currentUser)
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Khóa trạng thái Tab hiện tại luôn là "Hồ sơ" để nút luôn có màu vàng hổ phách
    val currentTab = "Hồ sơ"

    // Trạng thái điều khiển ẩn/hiện hộp thoại xác nhận đăng xuất
    var showLogoutDialog by remember { mutableStateOf(false) }

    val accountLinkedMsg = stringResource(R.string.toast_account_linked)
    val linkFailedMsg = stringResource(R.string.toast_link_failed)
    val googleFailedTokenMsg = stringResource(R.string.toast_google_failed_token)
    val googleFailedStatusMsg = stringResource(R.string.toast_google_failed_status)
    val googleFailedResultMsg = stringResource(R.string.toast_google_failed_result)
    val signInCancelledMsg = stringResource(R.string.toast_sign_in_cancelled)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    isLoading = true
                    errorMessage = null
                    authRepository.linkWithGoogle(idToken) { success ->
                        isLoading = false
                        if (success) {
                            Toast.makeText(context, accountLinkedMsg, Toast.LENGTH_SHORT).show()
                        } else {
                            errorMessage = linkFailedMsg
                        }
                    }
                } else {
                    errorMessage = googleFailedTokenMsg
                }
            } catch (e: ApiException) {
                Log.e("ProfileScreen", "Google sign in failed", e)
                errorMessage = String.format(googleFailedStatusMsg, e.statusCode, e.message ?: "")
            }
        } else {
            if (result.resultCode != Activity.RESULT_CANCELED) {
                errorMessage = String.format(googleFailedResultMsg, result.resultCode)
            } else {
                Toast.makeText(context, signInCancelledMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── HỘP THOẠI XÁC NHẬN ĐĂNG XUẤT ───
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = Color(0xFF252329), // Nền Bento tối đồng bộ toàn app
            title = {
                Text(
                    text = "Đăng xuất tài khoản",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đăng xuất khỏi ứng dụng không?",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), // Màu đỏ cảnh báo nguy hiểm
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        showLogoutDialog = false // Đóng Dialog trước khi chuyển tiếp

                        // 1. Đăng xuất hoàn toàn khỏi Firebase Auth
                        FirebaseAuth.getInstance().signOut()

                        // 2. Đăng xuất khỏi Google SDK để không tự động điền tài khoản cũ vào lần sau
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        GoogleSignIn.getClient(context, gso).signOut()

                        Toast.makeText(context, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()

                        // 3. Kích hoạt callback điều hướng lọt về màn đăng nhập trong MainActivity
                        onLogoutSuccess()
                    }
                ) {
                    Text("Đăng xuất", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                // Đã xóa bỏ icon mũi tên quay lại tại đây để tránh xung đột trải nghiệm thanh TabBar chính dưới đáy
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1D24))
            )
        },
        bottomBar = {
            ProfileBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { tabName ->
                    when (tabName) {
                        "Trang chủ" -> onNavigateToHome()
                        "Tệp" -> onNavigateToHistory()
                        "Công cụ" -> onNavigateToSettings()
                        "Hồ sơ" -> { /* Đang ở chính màn này, không xử lý lại */ }
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar Người dùng
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF14B8A6)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.t2h_logo),
                    contentDescription = "User Large Avatar",
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hiển thị Email/Trạng thái ẩn danh
            Text(
                text = currentUser?.email ?: if (currentUser?.isAnonymous == true) "Tài khoản ẩn danh" else "Đã liên kết hệ thống",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Hiển thị UID rút gọn
            Text(
                text = stringResource(R.string.profile_uid, currentUser?.uid?.take(16) ?: ""),
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ─── KHỐI BENTO MENU CHỨC NĂNG ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF252329))
                    .padding(vertical = 4.dp)
            ) {
                Column {
                    ProfileMenuItem(
                        iconRes = R.drawable.material_symbols_border_all_rounded,
                        title = "Lịch sử quét",
                        subtitle = "Quản lý các tài liệu đã quét gần đây",
                        onClick = onNavigateToHistory
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.06f))

                    ProfileMenuItem(
                        iconRes = R.drawable.mingcute_document_line,
                        title = "Tập tin của tôi",
                        subtitle = "Danh sách văn bản và tệp PDF đã lưu",
                        onClick = onNavigateToHistory
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.06f))

                    ProfileMenuItem(
                        iconRes = R.drawable.tdesign_tools_circle,
                        title = "Công cụ ứng dụng",
                        subtitle = "Tùy chỉnh nhận diện, ngôn ngữ OCR",
                        onClick = onNavigateToSettings
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.06f))

                    // LIÊN KẾT TÀI KHOẢN GOOGLE
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = currentUser?.isAnonymous == true && !isLoading) {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(context.getString(R.string.default_web_client_id))
                                    .requestEmail()
                                    .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                launcher.launch(googleSignInClient.signInIntent)
                            }
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
                                painter = painterResource(id = R.drawable.google_icon),
                                contentDescription = "Google Link Icon",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kết nối tài khoản Google",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (currentUser?.isAnonymous == false) "Trạng thái: Đã đồng bộ tài khoản" else "Đồng bộ để không bị mất dữ liệu",
                                color = if (currentUser?.isAnonymous == false) Color(0xFF14B8A6) else Color.Gray,
                                fontSize = 11.sp
                            )
                        }

                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF14B8A6), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.codicon_new_file),
                                contentDescription = "Arrow right",
                                tint = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── NÚT KÍCH HOẠT ĐĂNG XUẤT MÀU ĐỎ MỜ TÌNH TẾ ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                    .clickable { showLogoutDialog = true } // Nhấp để hiển thị AlertDialog
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.mingcute_user_4_line),
                        contentDescription = "Logout",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Đăng xuất tài khoản",
                        color = Color(0xFFEF4444),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
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
            contentDescription = "Arrow right",
            tint = Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ProfileBottomNavigation(
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

            // NÚT CHÍNH GIỮA (QUÉT NHANH CAMERA)
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
            tint = if (isSelected) Color(0xFFD4AF37) else Color.Gray, // Chuẩn màu vàng hổ phách thương hiệu
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