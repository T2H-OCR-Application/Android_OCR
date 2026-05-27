package com.t2h.ocr.ui.screens.tools

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t2h.ocr.R

// ─────────────────────────────────────────────────────────────────────────────
//  Data model
// ─────────────────────────────────────────────────────────────────────────────

private data class Tool(
    val id      : String,
    val label   : String,
    val iconRes : Int,
)

private data class ToolCategory(
    val title : String,
    val tools : List<Tool>,
)

private val toolCategories = listOf(
    ToolCategory(
        title = "Nhập liệu",
        tools = listOf(
            Tool(id = "scan",  label = "Quét",      iconRes = R.drawable.streamline_scanner_solid),
            Tool(id = "image", label = "Chọn ảnh",  iconRes = R.drawable.tabler_photo),
        ),
    ),
    ToolCategory(
        title = "Kết quả",
        tools = listOf(
            Tool(id = "text", label = "Văn bản", iconRes = R.drawable.f7_doc_text),
            Tool(id = "pdf",  label = "PDF",     iconRes = R.drawable.fa7_regular_file_pdf),
            Tool(id = "file", label = "Tập tin", iconRes = R.drawable.codicon_new_file),
        ),
    ),
)

// ─────────────────────────────────────────────────────────────────────────────
//  Screen
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Màn hình Công cụ — hiển thị các công cụ chia theo category.
 * Mỗi hàng tối đa 3 icon.
 *
 * @param onToolClick callback khi bấm vào từng công cụ, truyền id của tool
 */
@Composable
fun ToolsScreen(
    onToolClick : (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        toolCategories.forEach { category ->
            ToolCategorySection(
                category    = category,
                onToolClick = onToolClick,
            )
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Category section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ToolCategorySection(
    category    : ToolCategory,
    onToolClick : (String) -> Unit,
) {
    Text(
        text       = category.title,
        color      = Color.White,
        fontSize   = 17.sp,
        fontWeight = FontWeight.Bold,
        modifier   = Modifier.padding(bottom = 16.dp),
    )

    val rows = category.tools.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        rows.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { tool ->
                    ToolIconItem(
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

// ─────────────────────────────────────────────────────────────────────────────
//  Tool icon item
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ToolIconItem(
    tool        : Tool,
    onToolClick : (String) -> Unit,
    modifier    : Modifier = Modifier,
) {
    Column(
        modifier            = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication        = null,
            onClick           = { onToolClick(tool.id) },
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.bg_color)),
        ) {
            Icon(
                painter            = painterResource(tool.iconRes),
                contentDescription = tool.label,
                modifier           = Modifier.size(30.dp),
                tint               = Color.Unspecified,
            )
        }

        Text(
            text      = tool.label,
            color     = Color.White,
            fontSize  = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}
