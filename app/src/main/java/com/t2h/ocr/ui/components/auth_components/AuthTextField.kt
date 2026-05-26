package com.t2h.ocr.ui.components.auth_components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.colorResource
import com.t2h.ocr.R

/**
 * TextField dùng chung cho màn hình Auth (Login, Register).
 *
 * @param value         giá trị hiện tại
 * @param onValueChange callback khi người dùng nhập
 * @param label         nhãn hiển thị (hoặc thông báo lỗi nếu có)
 * @param placeholder   gợi ý nhập liệu
 * @param errorMessage  chuỗi lỗi — nếu không rỗng thì label đổi sang màu đỏ
 * @param leadingIconRes resource id của icon bên trái
 * @param keyboardType  loại bàn phím (Email, Password, ...)
 * @param isPassword    true nếu là ô mật khẩu → hiện icon ẩn/hiện
 * @param passwordVisible trạng thái hiện/ẩn mật khẩu
 * @param onTogglePasswordVisibility callback khi bấm icon mắt
 */
@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    errorMessage: String = "",
    leadingIconRes: Int,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        label = {
            Text(
                text = errorMessage.ifEmpty { label },
                color = if (errorMessage.isNotEmpty()) Red else Unspecified
            )
        },
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                painter = painterResource(leadingIconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = colorResource(R.color.Icon_cl)
            )
        },
        visualTransformation = if (isPassword && !passwordVisible)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        trailingIcon = if (isPassword && onTogglePasswordVisibility != null) {
            {
                val iconRes = if (passwordVisible)
                    R.drawable.visibility_24dp
                else
                    R.drawable.visibility_off_24dp
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                    modifier = Modifier
                        .clickable { onTogglePasswordVisibility() }
                        .size(20.dp)
                )
            }
        } else null,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    )
}
