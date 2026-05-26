package com.t2h.ocr.ui.components.file_components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t2h.ocr.R
import com.t2h.ocr.ui.screens.file.FileItem

/**
 * Một dòng item trong danh sách tệp.
 */
@Composable
fun FileItemRow(
    item       : FileItem,
    isSelected : Boolean,
    onToggle   : (String) -> Unit,
    onClick    : (FileItem) -> Unit,
    modifier   : Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = colorResource(R.color.Icon_cl).copy(alpha = 0.35f),
                shape = RoundedCornerShape(10.dp),
            )
            .background(colorResource(R.color.main_act))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = { onClick(item) },
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── Thumbnail placeholder (56×56) ───────────────────────────────
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colorResource(R.color.Unspecified).copy(alpha = 0.15f)),
        )

        // ── Tên file + thời gian ────────────────────────────────────────
        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(
                            color = colorResource(R.color.Unspecified).copy(alpha = 0.3f),
                            shape = RoundedCornerShape(3.dp),
                        ),
                )

                Text(
                    text       = item.name,
                    color      = Color.White,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
            }

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    painter            = painterResource(R.drawable.bi_file_text),
                    contentDescription = null,
                    modifier           = Modifier.size(12.dp),
                    tint               = colorResource(R.color.Icon_cl),
                )
                Text(
                    text     = item.timeLabel,
                    color    = colorResource(R.color.Unspecified),
                    fontSize = 11.sp,
                )
            }
        }

        // ── Checkbox ────────────────────────────────────────────────────
        Checkbox(
            checked         = isSelected,
            onCheckedChange = { onToggle(item.id) },
            colors          = CheckboxDefaults.colors(
                checkedColor   = colorResource(R.color.Icon_cl),
                uncheckedColor = colorResource(R.color.Unspecified),
                checkmarkColor = Color.White,
            ),
            modifier = Modifier.size(20.dp),
        )
    }
}
