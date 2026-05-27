package com.t2h.ocr.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

/**
 * Nội dung màn hình tìm kiếm — chỉ phần kết quả, không có SearchBar.
 * SearchBar được quản lý bởi MainScreen (dùng chung thanh trên cùng).
 *
 * @param query         query hiện tại (từ MainScreen)
 * @param filteredTools danh sách công cụ đã lọc
 * @param filteredDocs  danh sách docs đã lọc
 * @param onToolClick   callback khi chọn công cụ
 * @param onDocClick    callback khi chọn tài liệu
 */
@Composable
fun SearchScreen(
    query         : String,
    filteredTools : List<ToolItem>,
    filteredDocs  : List<FileItem>,
    onToolClick   : (ToolItem) -> Unit = {},
    onDocClick    : (FileItem) -> Unit = {},
) {
    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        // ── Section: Công cụ ────────────────────────────────────────────
        if (filteredTools.isNotEmpty()) {
            item {
                SectionHeader(title = "Công cụ")
                LazyRow(
                    contentPadding        = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filteredTools, key = { it.id }) { tool ->
                        ToolIconItem(
                            tool    = tool,
                            onClick = { onToolClick(tool) },
                        )
                    }
                }
            }
        }

        // ── Section: Docs ───────────────────────────────────────────────
        if (filteredDocs.isNotEmpty()) {
            item { SectionHeader(title = "Docs") }
            items(filteredDocs, key = { it.id }) { doc ->
                DocResultRow(
                    item    = doc,
                    onClick = { onDocClick(doc) },
                )
            }
        }

        // ── Không có kết quả ────────────────────────────────────────────
        if (query.isNotEmpty() && filteredTools.isEmpty() && filteredDocs.isEmpty()) {
            item {
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text     = "Không tìm thấy kết quả cho \"$query\"",
                        color    = colorResource(R.color.Unspecified),
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text       = title,
        color      = colorResource(R.color.Unspecified),
        fontSize   = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier   = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun ToolIconItem(tool: ToolItem, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier            = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(bottom = 8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colorResource(R.color.bg_color)),
        ) {
            Icon(
                painter            = painterResource(tool.iconRes),
                contentDescription = tool.label,
                modifier           = Modifier.size(28.dp),
                tint               = Color.Unspecified,
            )
        }
        Text(
            text     = tool.label,
            color    = Color.White,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun DocResultRow(item: FileItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colorResource(R.color.bg_color)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter            = painterResource(R.drawable.bi_file_text),
                contentDescription = null,
                modifier           = Modifier.size(22.dp),
                tint               = colorResource(R.color.Icon_cl),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text       = item.name,
                color      = Color.White,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text     = item.timeLabel,
                color    = colorResource(R.color.Unspecified),
                fontSize = 12.sp,
            )
        }
    }
}
