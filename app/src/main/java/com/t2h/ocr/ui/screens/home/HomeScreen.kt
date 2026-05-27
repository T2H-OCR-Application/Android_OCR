package com.t2h.ocr.ui.screens.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.t2h.ocr.R
import com.t2h.ocr.ui.viewmodel.HomeViewModel

private data class QuickTool(
    val id      : String,
    val label   : String,
    val iconRes : Int?,
)

@Composable
fun HomeScreen(
    onToolClick : (String) -> Unit = {},
) {
    val viewModel: HomeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadRecentItems() }

    val quickTools = listOf(
        QuickTool(id = "scan",  label = "Quét",    iconRes = R.drawable.streamline_scanner_solid),
        QuickTool(id = "text",  label = "Văn bản", iconRes = R.drawable.f7_doc_text),
        QuickTool(id = "file",  label = "Tập tin", iconRes = R.drawable.codicon_new_file),
        QuickTool(id = "pdf",   label = "PDF",     iconRes = R.drawable.fa7_regular_file_pdf),
        QuickTool(id = "image", label = "Ảnh",     iconRes = R.drawable.tabler_photo),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── Lưới công cụ nhanh ──────────────────────────────────────────
        QuickToolGrid(tools = quickTools, onToolClick = onToolClick)

        Spacer(modifier = Modifier.height(20.dp))

        // ── Section Gần Đây ──────────────────────────────────────────────
        RecentSection(
            uiState        = uiState,
            onAddDataClick = { /* TODO */ },
            onViewAllClick = { /* TODO */ },
            modifier       = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Lưới công cụ nhanh (3 cột)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickToolGrid(
    tools       : List<QuickTool>,
    onToolClick : (String) -> Unit,
) {
    val rows = tools.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        rows.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                row.forEach { tool ->
                    QuickToolItem(
                        tool        = tool,
                        onToolClick = onToolClick,
                        modifier    = Modifier.weight(1f),
                    )
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickToolItem(
    tool        : QuickTool,
    onToolClick : (String) -> Unit,
    modifier    : Modifier = Modifier,
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colorResource(R.color.bg_color))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = { onToolClick(tool.id) },
                ),
        ) {
            if (tool.iconRes != null) {
                Icon(
                    painter            = painterResource(tool.iconRes),
                    contentDescription = tool.label,
                    modifier           = Modifier.size(28.dp),
                    tint               = Color.Unspecified,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(colorResource(R.color.Unspecified), RoundedCornerShape(4.dp)),
                )
            }
        }
        Text(
            text      = tool.label,
            color     = Color.White,
            fontSize  = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Section "Gần Đây"
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecentSection(
    uiState        : HomeUiState,
    onAddDataClick : () -> Unit,
    onViewAllClick : () -> Unit,
    modifier       : Modifier = Modifier,
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
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text       = "Gần Đây",
                color      = Color.White,
                fontSize   = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text     = "Xem tất cả",
                color    = colorResource(R.color.Icon_cl),
                fontSize = 13.sp,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null,
                    onClick           = onViewAllClick,
                ),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (uiState) {
            is HomeUiState.Loading ->
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "Đang tải...", color = colorResource(R.color.Unspecified))
                }

            is HomeUiState.Success ->
                if (uiState.recentItems.isEmpty()) {
                    EmptyState(onAddDataClick = onAddDataClick)
                } else {
                    uiState.recentItems.forEach { item ->
                        Text(text = item.title, color = Color.White, fontSize = 13.sp)
                    }
                }

            is HomeUiState.Error ->
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = uiState.message, color = Color.Red, fontSize = 13.sp)
                }

            else -> EmptyState(onAddDataClick = onAddDataClick)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Trạng thái rỗng
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(onAddDataClick: () -> Unit) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter            = painterResource(R.drawable.bi_file_text),
            contentDescription = "Empty",
            modifier           = Modifier.size(56.dp),
            tint               = colorResource(R.color.Icon_cl),
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text     = "Chưa có tài liệu nào",
            color    = colorResource(R.color.Unspecified),
            fontSize = 14.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick  = onAddDataClick,
            shape    = RoundedCornerShape(20.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            modifier = Modifier.border(
                width  = 1.5.dp,
                color  = colorResource(R.color.Icon_cl),
                shape  = RoundedCornerShape(20.dp),
            ),
        ) {
            Text(
                text     = "Thêm dữ liệu",
                color    = colorResource(R.color.Icon_cl),
                fontSize = 13.sp,
            )
        }
    }
}
