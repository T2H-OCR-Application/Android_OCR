package com.t2h.ocr.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.t2h.ocr.R
import com.t2h.ocr.ui.components.AuthTextField
import com.t2h.ocr.ui.components.GoogleAuthObserver
import com.t2h.ocr.ui.components.GoogleButton
import com.t2h.ocr.ui.screens.validator.AuthValidator
import com.t2h.ocr.ui.viewmodel.RegisterViewModel

/**
 * Màn hình đăng ký.
 *
 * Screen không giữ NavController — mọi điều hướng đều qua callback,
 * để AppNavigation là nơi duy nhất quyết định luồng chuyển màn hình.
 *
 * @param onNavigateToLogin  Chuyển sang màn hình đăng nhập
 * @param onAuthSuccess      Đăng ký / Google Sign-In thành công → vào Main
 */
@Composable
fun RegisterScreen(
    onNavigateToLogin : () -> Unit,
    onAuthSuccess     : () -> Unit,
) {
    val viewModel: RegisterViewModel = viewModel()
    val googleAuthState by viewModel.googleAuthState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Google Sign-In thành công → gọi callback lên AppNavigation
    GoogleAuthObserver(
        state     = googleAuthState,
        onSuccess = {
            viewModel.resetState()
            onAuthSuccess()
        },
        onReset   = { viewModel.resetState() },
    )

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }
    var emailError      by remember { mutableStateOf("") }
    var passwordError   by remember { mutableStateOf("") }
    var confirmError    by remember { mutableStateOf("") }
    var isLoading       by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter            = painterResource(R.drawable.auth_bg),
            contentDescription = null,
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            Image(
                painter            = painterResource(R.drawable.t2h_logo),
                contentDescription = "App logo",
                modifier           = Modifier.size(100.dp),
            )

            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text       = "ĐĂNG KÝ",
                fontSize   = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
            )

            Spacer(modifier = Modifier.weight(3f))

            AuthTextField(
                value          = email,
                onValueChange  = { email = it; emailError = AuthValidator.validateEmail(it) },
                label          = "Email",
                placeholder    = "Nhập email của bạn",
                errorMessage   = emailError,
                leadingIconRes = R.drawable.email_icon,
                keyboardType   = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.weight(0.8f))

            AuthTextField(
                value                      = password,
                onValueChange              = { password = it; passwordError = AuthValidator.validatePassword(it) },
                label                      = "Mật khẩu",
                placeholder                = "Nhập mật khẩu của bạn",
                errorMessage               = passwordError,
                leadingIconRes             = R.drawable.lock_icon,
                keyboardType               = KeyboardType.Password,
                isPassword                 = true,
                passwordVisible            = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            )

            Spacer(modifier = Modifier.weight(0.8f))

            AuthTextField(
                value                      = confirmPassword,
                onValueChange              = { confirmPassword = it; confirmError = AuthValidator.validateConfirmPassword(it, password) },
                label                      = "Xác nhận mật khẩu",
                placeholder                = "Nhập lại mật khẩu của bạn",
                errorMessage               = confirmError,
                leadingIconRes             = R.drawable.lock_icon,
                keyboardType               = KeyboardType.Password,
                isPassword                 = true,
                passwordVisible            = confirmVisible,
                onTogglePasswordVisibility = { confirmVisible = !confirmVisible },
            )

            Spacer(modifier = Modifier.weight(0.8f))

            Button(
                onClick = {
                    isLoading     = true
                    emailError    = AuthValidator.validateEmail(email)
                    passwordError = AuthValidator.validatePassword(password)
                    confirmError  = AuthValidator.validateConfirmPassword(confirmPassword, password)
                    if (emailError.isEmpty() && passwordError.isEmpty() && confirmError.isEmpty()) {
                        // TODO: Firebase createUserWithEmailAndPassword → gọi onAuthSuccess trong callback
                        onAuthSuccess()
                    }
                    isLoading = false
                },
                enabled  = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(50.dp),
                shape  = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.Icon_cl)),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text(text = "ĐĂNG KÝ", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text      = "-----OR-----",
                fontSize  = 20.sp,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color     = colorResource(R.color.Unspecified),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text      = "Continue with",
                fontSize  = 16.sp,
                modifier  = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color     = colorResource(R.color.Unspecified),
            )

            Spacer(modifier = Modifier.height(10.dp))

            GoogleButton(
                isLoading = googleAuthState is AuthUiState.Loading,
                onClick   = { viewModel.signInWithGoogle(context) },
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text     = "Already have an account? Login",
                color    = Color.White,
                modifier = Modifier.clickable { onNavigateToLogin() },
            )

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
