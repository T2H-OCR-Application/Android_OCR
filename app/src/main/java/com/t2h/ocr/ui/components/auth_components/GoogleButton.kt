package com.t2h.ocr.ui.components.auth_components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.t2h.ocr.R

// hiển thị nút đăng nhập bằng google
@Composable
fun GoogleButton(
    onClick: () -> Unit,
    isLoading: Boolean = false
) {
    OutlinedButton(
        onClick = { if (!isLoading) onClick() },
        border = BorderStroke(
            0.dp,
            Color.Transparent
        ),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.google_icon),
                contentDescription = "",
                modifier = Modifier.size(40.dp),
                tint = Color.Unspecified
            )
        }
    }
}
