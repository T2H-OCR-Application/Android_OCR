# Architecture Patterns: Android OCR Firebase Sync

**Domain:** Mobile Document Management / OCR
**Researched:** 2024-12-21

## Recommended Architecture

The system follows a **Clean Architecture** approach with a **Hybrid Sync Strategy**.

### Component Boundaries

| Component | Responsibility | Communicates With |
|-----------|---------------|-------------------|
| `UI (Compose)` | Renders document lists and OCR views. | `ViewModel` |
| `OCR Engine` | Extracts text from PDF using ML Kit. | `Local Repository` |
| `Sync Manager` | Coordinates Firestore and Storage sync using WorkManager. | `Firebase Firestore`, `Cloud Storage` |
| `Auth Service` | Manages user session. | `Firebase Auth` |

### Data Flow

1.  **Input:** User selects a PDF.
2.  **Process:** ML Kit runs locally; OCR text and metadata are saved to **local Room DB** or directly to **Firestore** (which has local persistence).
3.  **Sync (Firestore):** Metadata and text snippets sync automatically via the Firebase SDK.
4.  **Sync (Storage):** `WorkManager` triggers a background task to upload the PDF file and full OCR JSON to Cloud Storage using **Resumable Uploads**.
5.  **Recovery:** If the upload fails, `WorkManager` retries with exponential backoff. If the process is killed, the `uploadSessionUri` (stored locally) allows the next worker run to resume the upload.

## Patterns to Follow

### Pattern 1: Hybrid Metadata/File Storage
**What:** Store searchable fields in Firestore and heavy data (PDF, full OCR JSON) in Storage.
**When:** Always, to avoid Firestore's 1 MiB limit and optimize costs.
**Example:**
```kotlin
// Firestore Document: documents/{docId}
val documentMetadata = hashMapOf(
    "userId" to currentUserId,
    "title" to "Tax Invoice 2024.pdf",
    "searchSnippet" to "Total Amount: $1,200...", // First 1000 chars
    "storagePath" to "users/$currentUserId/pdfs/$docId.pdf",
    "ocrDataPath" to "users/$currentUserId/ocr/$docId.json"
)
```

### Pattern 2: Resumable Background Uploads
**What:** Use `WorkManager` with `StorageReference.putFile(uri, metadata, sessionUri)`.
**When:** Uploading PDFs or large OCR results.

## Anti-Patterns to Avoid

### Anti-Pattern 1: Storing Full OCR in Firestore
**What:** Putting the entire ML Kit `Text` object (including bounding boxes) into a Firestore field.
**Why bad:** Quickly exceeds 1 MiB for multi-page documents, causing write failures.
**Instead:** Save as a JSON file in Cloud Storage.

### Anti-Pattern 2: Immediate UI Block on Upload
**What:** Showing a blocking spinner while a 50MB PDF uploads.
**Why bad:** Poor UX in mobile environments.
**Instead:** Use background sync with `WorkManager` and show a non-blocking progress indicator in the list view.

## Scalability Considerations

| Concern | At 100 users | At 10K users | At 1M users |
|---------|--------------|--------------|-------------|
| Firestore Reads | Negligible | Optimize with indexing | Use Firestore Bundle for caching |
| Storage Costs | Free tier sufficient | Manage lifecycle rules (cleanup) | Implement data deduplication |
| OCR Accuracy | ML Kit standard | Fine-tune with Gemini API | Custom OCR models (Cloud) |

## Sources

- [Google Cloud Architecture Framework](https://cloud.google.com/architecture/framework)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
