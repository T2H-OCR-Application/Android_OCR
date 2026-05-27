package com.t2h.ocr.ui.screens.textfile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.t2h.ocr.R
import com.t2h.ocr.ui.components.file_components.FileListSection
import com.t2h.ocr.ui.viewmodel.TextFileViewModel

/**
 * Màn hình Văn bản — hiển thị danh sách văn bản đã OCR.
 * Giao diện giống FileScreen, có nút Back để quay lại ToolsScreen.
 *
 * @param onBack callback để quay lại màn hình trước
 */
@Composable
fun TextFileScreen(
    onBack : () -> Unit = {},
) {
    val viewModel : TextFileViewModel = viewModel()
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedIds by remember { mutableStateOf(emptySet<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ── Header với nút Back ──────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter            = painterResource(R.drawable.arrow_forward_profile),
                    contentDescription = "Quay lại",
                    tint               = colorResource(R.color.Icon_cl),
                    modifier           = Modifier
                        .size(20.dp)
                        .graphicsLayer(scaleX = -1f), // lật ngang → mũi tên trái
                )
            }

            Text(
                text       = "Văn bản",
                color      = Color.White,
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Danh sách văn bản ────────────────────────────────────────────
        FileListSection(
            uiState      = uiState,
            selectedIds  = selectedIds,
            onToggleItem = { id ->
                selectedIds = if (id in selectedIds)
                    selectedIds - id
                else
                    selectedIds + id
            },
            onClickItem  = { _ ->
                // TODO: mở màn hình chi tiết văn bản
            },
            onDeleteAll  = {
                viewModel.deleteSelected(selectedIds)
                selectedIds = emptySet()
            },
            onAddClick   = {
                // TODO: trigger scan / chọn ảnh để tạo văn bản mới
            },
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
