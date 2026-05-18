# Feature Landscape: Android OCR

**Domain:** Mobile Document Management / OCR
**Researched:** 2024-12-21

## Table Stakes

Features users expect. Missing = product feels incomplete.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| PDF Selection | Core entry point for OCR. | Low | Use System File Picker. |
| On-Device OCR | Instant feedback without network. | Medium | Use ML Kit Text Recognition. |
| Searchable Text | Finding info within documents. | Low | Store snippet in Firestore. |
| Multi-device Sync | Access scans on phone & tablet. | High | Requires Firebase Sync patterns. |
| User Auth | Data privacy and account linking. | Low | Firebase Google/Email Auth. |

## Differentiators

Features that set product apart. Not expected, but valued.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Offline-First Uploads | Works in poor connectivity. | High | WorkManager + Resumable Uploads. |
| OCR Search Highlights | Direct visual link to text location. | High | Requires Bounding Box data from ML Kit. |
| PDF Export with Text | Share "Searchable PDF" (PDF/A). | High | Requires iText or PDFBox-Android. |
| Document Categorization | Auto-tagging based on OCR text. | Medium | Simple keyword matching or Gemini API. |

## Anti-Features

Features to explicitly NOT build.

| Anti-Feature | Why Avoid | What to Do Instead |
|--------------|-----------|-------------------|
| Web-based OCR | Latency and data costs. | Keep OCR on-device with ML Kit. |
| Custom Auth | Security risk and dev overhead. | Use Firebase Auth. |
| Global Public Search | Privacy risk. | Only allow search within user's own docs. |

## Feature Dependencies

```
User Auth → Firestore Sync (Sync requires User ID)
PDF Selection → ML Kit OCR (OCR requires Input File)
ML Kit OCR → Searchable Snippets (Snippets come from OCR)
WorkManager → Reliable Storage Sync (Storage needs a manager)
```

## MVP Recommendation

Prioritize:
1.  **Auth & Local OCR:** Get users in and extracting text from PDFs immediately.
2.  **Basic Firestore Sync:** Sync document metadata (title, snippet) for a simple list view.
3.  **Reliable PDF Storage:** Upload the PDF file to Cloud Storage using WorkManager.

Defer:
-   **Bounding Box Search Highlights:** Complex UI, can be added later.
-   **PDF/A Export:** Heavy library dependency, can be a premium feature.

## Sources

- [Competitor analysis of Adobe Scan, CamScanner, Google Lens]
- [ML Kit Documentation]
