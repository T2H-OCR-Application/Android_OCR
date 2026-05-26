package com.t2h.ocr.ui.components.file_components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t2h.ocr.R

/**
 * Trạng thái rỗng — hiển thị khi chưa có tệp nào.
 */
@Composable
fun FileEmptyState(
    onAddClick: () -> Unit,
    modifier  : Modifier = Modifier,
) {
    Column(
        modifier            = modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter            = painterResource(R.drawable.bi_file_text),
            contentDescription = null,
            modifier           = Modifier.size(56.dp),
            tint               = colorResource(R.color.Icon_cl),
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text     = "Chưa có tệp nào",
            color    = colorResource(R.color.Unspecified),
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick  = onAddClick,
            shape    = RoundedCornerShape(20.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.border(
                width  = 1.5.dp,
                color  = colorResource(R.color.Icon_cl),
                shape  = RoundedCornerShape(20.dp),
            ),
        ) {
            Text(
                text     = "Thêm tệp",
                color    = colorResource(R.color.Icon_cl),
                fontSize = 13.sp,
            )
        }
    }
}
