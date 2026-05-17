package com.t2h.ocr.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.t2h.ocr.R

@Composable
fun GoogleButton(
    onClick: () -> Unit
) {

    OutlinedButton(
        onClick = onClick,



        border = BorderStroke(
            0.dp,
            Color.Transparent
        ),

        shape = RoundedCornerShape(10.dp),

        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent
        )

    ) {

        Icon(
            painter = painterResource(R.drawable.google_icon),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = Color.Unspecified
        )


    }
}