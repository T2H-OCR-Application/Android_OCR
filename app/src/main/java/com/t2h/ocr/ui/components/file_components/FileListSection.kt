package com.t2h.ocr.ui.components.file_components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t2h.ocr.R
import com.t2h.ocr.ui.screens.file.FileItem
import com.t2h.ocr.ui.screens.file.FileUiState

/**
 * Container chính của danh sách tệp.
 */
@Composable
fun FileListSection(
    uiState      : FileUiState,
    selectedIds  : Set<String>,
    onToggleItem : (String) -> Unit,
    onClickItem  : (FileItem) -> Unit,
    onDeleteAll  : () -> Unit,
    onAddClick   : () -> Unit,
    modifier     : Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.5.dp,
                color = colorResource(R.color.Icon_cl),
                shape = RoundedCornerShape(14.dp),
            )
            .background(colorResource(R.color.bg_color))
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        // ── Header ──────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            val count = if (uiState is FileUiState.Success) uiState.items.size else 0
            Text(
                text       = "Tất cả ($count)",
                color      = Color.White,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Icon(
                painter            = painterResource(R.drawable.bi_file_text),
                contentDescription = "Xóa tất cả",
                modifier           = Modifier
                    .size(20.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onDeleteAll,
                    ),
                tint = colorResource(R.color.Unspecified),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Body ─────────────────────────────────────────────────────────
        when (uiState) {
            is FileUiState.Loading -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color       = colorResource(R.color.Icon_cl),
                        modifier    = Modifier.size(32.dp),
                        strokeWidth = 2.dp,
                    )
                }
            }

            is FileUiState.Empty -> {
                FileEmptyState(onAddClick = onAddClick)
            }

            is FileUiState.Success -> {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = uiState.items,
                        key   = { it.id },
                    ) { item ->
                        FileItemRow(
                            item       = item,
                            isSelected = item.id in selectedIds,
                            onToggle   = onToggleItem,
                            onClick    = onClickItem,
                        )
                    }
                }
            }

            is FileUiState.Error -> {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text     = uiState.message,
                        color    = Color.Red,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}
