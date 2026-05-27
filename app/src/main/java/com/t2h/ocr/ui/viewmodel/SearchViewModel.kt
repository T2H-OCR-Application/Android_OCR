package com.t2h.ocr.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.t2h.ocr.ui.screens.file.FileItem
import com.t2h.ocr.ui.screens.search.ToolItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class SearchViewModel : ViewModel() {

    // ── Query ────────────────────────────────────────────────────────────
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    // ── Dữ liệu gốc (sẽ được inject từ backend sau) ─────────────────────
    // TODO: thay bằng dữ liệu thực từ repository
    private val _allDocs = MutableStateFlow<List<FileItem>>(emptyList())

    // Danh sách công cụ cố định
    private val allTools = listOf(
        ToolItem(id = "scan",   label = "Quét",       iconRes = com.t2h.ocr.R.drawable.streamline_scanner_solid),
        ToolItem(id = "image",  label = "Chọn ảnh",   iconRes = com.t2h.ocr.R.drawable.tabler_photo),
        ToolItem(id = "text",   label = "Văn bản",    iconRes = com.t2h.ocr.R.drawable.f7_doc_text),
        ToolItem(id = "pdf",    label = "PDF",         iconRes = com.t2h.ocr.R.drawable.fa7_regular_file_pdf),
        ToolItem(id = "file",   label = "Tập tin",    iconRes = com.t2h.ocr.R.drawable.codicon_new_file),
    )

    // ── Kết quả tìm kiếm ─────────────────────────────────────────────────
    private val _filteredDocs = MutableStateFlow<List<FileItem>>(emptyList())
    val filteredDocs: StateFlow<List<FileItem>> = _filteredDocs

    private val _filteredTools = MutableStateFlow<List<ToolItem>>(allTools)
    val filteredTools: StateFlow<List<ToolItem>> = _filteredTools

    // ── Hàm cập nhật query ───────────────────────────────────────────────
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        val q = newQuery.trim().lowercase()

        _filteredDocs.value = if (q.isEmpty()) {
            emptyList()
        } else {
            _allDocs.value.filter { it.name.lowercase().contains(q) }
        }

        _filteredTools.value = if (q.isEmpty()) {
            allTools
        } else {
            allTools.filter { it.label.lowercase().contains(q) }
        }
    }

    // ── Inject docs từ bên ngoài (gọi từ Screen khi có data) ────────────
    fun setDocs(docs: List<FileItem>) {
        _allDocs.value = docs
        // Refresh lại filter theo query hiện tại
        onQueryChange(_query.value)
    }
}
