package com.t2h.ocr.ui.screens.file

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.t2h.ocr.ui.components.file_components.FileListSection
import com.t2h.ocr.ui.components.main_components.MainSearchBar
import com.t2h.ocr.ui.viewmodel.FileViewModel

/**
 * Màn hình Tệp — hiển thị danh sách tệp đã scan / convert PDF.
 *
 * Layout:
 *  ┌─────────────────────────┐
 *  │  SearchBar              │
 *  ├─────────────────────────┤
 *  │  FileListSection        │  ← fill phần còn lại (weight 1f)
 *  │   ├ Header "Tất cả (N)" │
 *  │   └ LazyColumn items    │
 *  └─────────────────────────┘
 *
 * Dữ liệu hiện để trống (Empty state).
 * TODO: kết nối Google Drive API trong FileViewModel.
 */
@Composable
fun FileScreen() {
    val viewModel : FileViewModel  = viewModel()
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedIds by remember { mutableStateOf(emptySet<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(20.dp))





        // ── Danh sách tệp ───────────────────────────────────────────────
        FileListSection(
            uiState      = uiState,
            selectedIds  = selectedIds,
            onToggleItem = { id ->
                selectedIds = if (id in selectedIds)
                    selectedIds - id
                else
                    selectedIds + id
            },
            onClickItem  = { item ->
                // TODO: mở màn hình chi tiết tệp
            },
            onDeleteAll  = {
                // TODO: xóa các item đang được chọn
                viewModel.deleteSelected(selectedIds)
                selectedIds = emptySet()
            },
            onAddClick   = {
                // TODO: trigger scan / chọn ảnh
            },
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
