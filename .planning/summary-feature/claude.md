# Tính năng Tóm tắt PDF bằng AI

## Tổng quan
Cho phép người dùng chọn một PDF đã scan trong app, gửi nội dung lên Gemini 2.5 Flash để tóm tắt, xem kết quả, sau đó chọn lưu (tạo PDF mới + metadata) hoặc huỷ.

---

## Luồng hoạt động

```
HomeScreen (nút "AI")
    └─> Screen.Summary → SummaryScreen
            ├─ Bước 1: Chọn PDF từ danh sách scan (ScanRepository)
            ├─ Bước 2: Đang tóm tắt (loading, gọi Gemini)
            └─ Bước 3: Kết quả
                    ├─ "Không lưu" → quay lại Bước 1
                    └─ "Lưu" → tạo PDF + lưu SummaryMetadata → màn hình Done
```

---

## Tasks

- [x] **Task 1 — Data layer**
  - `SummaryMetadata.kt` — data class
  - `SummaryStorage.kt` — interface
  - `SummaryJsonStorage.kt` — lưu `summaries.json`
  - `SummaryRepository.kt` — singleton

- [ ] **Task 2 — Service: `PdfSummaryService`**
  - Nhận `ScanMetadata`, dùng `ocrText` làm input
  - Gọi Gemini 2.5 Flash với key rotation (clone pattern `GeminiTranslationClient`)
  - Emit `SummaryState`: `Summarizing` → `Done` / `Error`

- [ ] **Task 3 — ViewModel: `SummaryViewModel`**
  - `SummaryUiState`: `SelectingScan` / `Summarizing` / `Result` / `Saving` / `Saved` / `Error`
  - `startSummary(scan)`, `saveSummary()`, `uploadToDrive()`, `reset()`
  - Lưu PDF bằng `PdfGenerator.generateTextPdf()`, lưu metadata qua `SummaryRepository`
  - Upload Drive vào folder "Android OCR Scans" dùng `DriveUploader` (cùng folder PDF gốc)

- [ ] **Task 4 — UI: `SummaryScreen`**
  - Bước 1: danh sách scan chọn PDF
  - Bước 2: loading spinner
  - Bước 3: hiển thị kết quả + nút Lưu / Không lưu
  - Done: thông báo thành công

- [ ] **Task 5 — Tích hợp `MainActivity`**
  - Thêm `Screen.Summary` vào sealed class
  - Khởi tạo `SummaryRepository`
  - Route `"AI"` → `Screen.Summary`
  - Thêm case `Screen.Summary` vào `AnimatedContent`

---

## Data Model

### `SummaryMetadata`
```kotlin
@Serializable
data class SummaryMetadata(
    val id: String = "",
    val title: String = "",            // "[tên_gốc]_summary"
    val sourceScanId: String = "",
    val sourcePdfPath: String = "",
    val summaryPdfPath: String = "",
    val summaryText: String = "",
    val timestamp: Long = 0L,
    val language: String = ""
)
```

---

## Service: `PdfSummaryService`

### State
```kotlin
sealed class SummaryState {
    object Idle : SummaryState()
    data class Summarizing(val progress: String) : SummaryState()
    data class Done(val summaryText: String, val sourceScanId: String) : SummaryState()
    data class Error(val message: String) : SummaryState()
}
```

### Prompt
```
Hãy tóm tắt văn bản sau ở độ dài trung bình (khoảng 30-40% độ dài gốc).
Giữ nguyên ngôn ngữ của văn bản gốc.
Chỉ trả về nội dung tóm tắt, không thêm tiêu đề hay chú thích.

Văn bản:
{ocrText}
```

---

## Lưu ý kỹ thuật

1. **Không parse PDF** — dùng `ScanMetadata.ocrText` làm input cho Gemini.
2. **API key** — lấy key đầu tiên từ `ApiKeyPreferences`, không rotation.
3. ** upload Drive** ~~spec không yêu cầu~~ — upload vào folder "Android OCR Scans" (cùng folder PDF gốc) dùng `DriveUploader` hiện có.
4. **PDF output** — dùng `PdfGenerator.generateTextPdf()` hiện có.
5. **`SummaryJsonStorage`** — clone `JsonStorage`, đổi file thành `summaries.json`, type thành `SummaryMetadata`.
