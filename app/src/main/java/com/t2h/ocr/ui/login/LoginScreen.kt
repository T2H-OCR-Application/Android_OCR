package com.t2h.ocr.ui.login

import android.app.Activity
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale // Đã thêm import để scale ảnh nền
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.t2h.ocr.R
import com.t2h.ocr.data.auth.AuthRepository

@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onNavigateToRegister: () -> Unit,
    onAuthSuccess: () -> Unit,
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val googleFailedTokenMsg = "Google Sign-In failed: No ID Token"
    val googleFailedStatusMsg = "Google Sign-In failed: status=%d, message=%s"
    val googleFailedResultMsg = "Google Sign-In failed: result=%d"
    val signInCancelledMsg = "Sign-In cancelled"

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
                    authRepository.signInWithGoogle(idToken) { success ->
                        isLoading = false
                        if (success) {
                            onAuthSuccess()
                        } else {
                            errorMessage = "Firebase authentication with Google failed"
                        }
                    }
                } else {
                    errorMessage = googleFailedTokenMsg
                }
            } catch (e: ApiException) {
                Log.e("LoginScreen", "Google sign in failed", e)
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

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader(title = "LOGIN")

            // Khoảng cách đẩy các ô nhập liệu xuống vùng nền tối để không đè lên ảnh màu xanh
            Spacer(modifier = Modifier.height(120.dp))

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
            }

            AuthTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = validateEmail(it)
                },
                placeholder = "Email",
                errorMessage = emailError,
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = validatePassword(it)
                },
                placeholder = "Enter password",
                errorMessage = passwordError,
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Forgot password?",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: xử lý quên mật khẩu */ }
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.End,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            AuthActionButton(
                label = "LOGIN",
                isLoading = isLoading,
                onClick = {
                    emailError = validateEmail(email)
                    passwordError = validatePassword(password)
                    if (emailError.isEmpty() && passwordError.isEmpty()) {
                        isLoading = true
                        onAuthSuccess()
                        isLoading = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "------------------------OR------------------------",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Continue with",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Nút đăng nhập Google tròn lấy ảnh từ thư mục drawable
            SocialButton(
                iconResId = R.drawable.google_icon,
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    
                    // Call signOut before signing in to prevent cached cancelled states
                    googleSignInClient.signOut().addOnCompleteListener {
                        launcher.launch(googleSignInClient.signInIntent)
                    }
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            AuthFooter(
                message = "Bạn chưa có tài khoản?",
                actionText = "Đăng ký",
                onActionClick = onNavigateToRegister
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RegisterScreenContent(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthHeader(title = "REGISTER")

            Spacer(modifier = Modifier.height(120.dp))

            AuthTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = validateEmail(it)
                },
                placeholder = "Email",
                errorMessage = emailError,
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email,
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = validatePassword(it)
                },
                placeholder = "Password",
                errorMessage = passwordError,
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible }
            )

            Spacer(modifier = Modifier.height(20.dp))

            AuthTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = validateConfirmPassword(password, it)
                },
                placeholder = "Confirm Password",
                errorMessage = confirmPasswordError,
                leadingIcon = Icons.Default.Lock,
                keyboardType = KeyboardType.Password,
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onTogglePasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible }
            )

            Spacer(modifier = Modifier.height(30.dp))

            AuthActionButton(
                label = "REGISTER",
                isLoading = isLoading,
                onClick = {
                    emailError = validateEmail(email)
                    passwordError = validatePassword(password)
                    confirmPasswordError = validateConfirmPassword(password, confirmPassword)
                    if (emailError.isEmpty() && passwordError.isEmpty() && confirmPasswordError.isEmpty()) {
                        isLoading = true
                        onRegisterSuccess()
                        isLoading = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            AuthFooter(
                message = "Bạn đã có tài khoản?",
                actionText = "Đăng nhập",
                onActionClick = onNavigateToLogin
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- CÁC COMPONENT GIAO DIỆN TÙY CHỈNH THEO MẪU ---

@Composable
private fun AuthBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF252329))
    ) {
        // ĐÃ SỬA: Ép ảnh nền bao bao phủ toàn bộ màn hình điện thoại
        Image(
            painter = painterResource(id = R.drawable.auth_bg),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Tự động crop phủ kín không để lại khoảng trống
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            content()
        }
    }
}

@Composable
private fun AuthHeader(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(105.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A1D24)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.t2h_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(90.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AuthFooter(
    message: String,
    actionText: String,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = actionText,
            color = Color(0xFF14B8A6),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
private fun AuthActionButton(
    label: String,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B8A6))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Text(
                text = label,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SocialButton(
    iconResId: Int,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = "Google Icon",
            modifier = Modifier.size(28.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorMessage: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            placeholder = { Text(text = placeholder, color = Color.LightGray.copy(alpha = 0.8f)) },
            leadingIcon = { Icon(imageVector = leadingIcon, contentDescription = null, tint = Color(0xFF14B8A6)) },
            trailingIcon = {
                if (isPassword && onTogglePasswordVisibility != null) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle visibility",
                        tint = Color.Gray,
                        modifier = Modifier.clickable { onTogglePasswordVisibility() }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        if (errorMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = Color(0xFFFFB4AB),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

// --- KHU VỰC CÁC HÀM VALIDATE ---

private fun validateEmail(email: String): String {
    return when {
        email.isBlank() -> "Email không được bỏ trống"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email không đúng định dạng"
        else -> ""
    }
}

private fun validatePassword(password: String): String {
    return when {
        password.isBlank() -> "Mật khẩu không được bỏ trống"
        password.length < 6 -> "Mật khẩu phải dài ít nhất 6 ký tự"
        else -> ""
    }
}

private fun validateConfirmPassword(password: String, confirmPassword: String): String {
    return when {
        confirmPassword.isBlank() -> "Xác nhận mật khẩu không được bỏ trống"
        confirmPassword != password -> "Mật khẩu xác nhận không khớp"
        else -> ""
    }
}