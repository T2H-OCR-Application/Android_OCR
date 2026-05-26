package com.t2h.ocr.ui.components.auth_components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.t2h.ocr.ui.screens.auth.AuthUiState

/**
 * Composable dùng chung để lắng nghe kết quả Google Auth.
 * Dùng cho cả LoginScreen và RegisterScreen.
 *
 * @param state     trạng thái hiện tại từ AuthViewModel
 * @param onSuccess callback khi đăng nhập thành công (navigate sang màn hình tiếp theo)
 * @param onReset   callback để reset state về Idle sau khi xử lý xong
 */
@Composable
fun GoogleAuthObserver(
    state: AuthUiState,
    onSuccess: () -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(state) {
        when (state) {
            is AuthUiState.Success -> {
                onSuccess()
                onReset()
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                onReset()
            }
            else -> Unit
        }
    }
}
