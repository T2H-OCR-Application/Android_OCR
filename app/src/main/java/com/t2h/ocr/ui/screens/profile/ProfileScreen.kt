package com.t2h.ocr.ui.screens.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Màn hình Hồ sơ.
 * TODO: Implement thông tin tài khoản, cài đặt, đăng xuất...
 */
@Composable
fun ProfileScreen() {
    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Màn hình Hồ sơ", color = Color.White)
    }
}
