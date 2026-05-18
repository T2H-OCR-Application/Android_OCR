# Domain Pitfalls: Android OCR & Firebase

**Domain:** Mobile Document Management / OCR
**Researched:** 2024-12-21

## Critical Pitfalls

Mistakes that cause rewrites or major issues.

### Pitfall 1: Firestore 1 MiB Document Limit
**What goes wrong:** OCR results for a 20-page document contain thousands of bounding boxes and text blocks. Storing this directly in a Firestore document will crash the write operation once it hits 1 MiB.
**Prevention:** Use the "Hybrid Pattern": store search snippets in Firestore and the full structured JSON in Cloud Storage.

### Pitfall 2: Orphaning Files in Storage
**What goes wrong:** A user deletes a record in Firestore, but the PDF file remains in Cloud Storage, leading to ghost storage costs and potential data privacy issues.
**Prevention:** Use a Cloud Function (Firebase Extension: "Delete User Data") or manually ensure that Storage deletions accompany Firestore deletions.

### Pitfall 3: Inefficient OCR Re-runs
**What goes wrong:** Re-running OCR on the same document every time it's opened, wasting CPU and battery.
**Prevention:** Cache the OCR result locally and in Cloud Storage; only re-run if the user explicitly requests a "re-scan".

## Moderate Pitfalls

### Pitfall 1: Large PDF Memory Crashes
**What goes wrong:** Attempting to render or scan a 100MB PDF in memory on a low-end Android device.
**Prevention:** Use `PdfRenderer` with page-by-page processing and downsample images before passing them to ML Kit.

### Pitfall 2: Token Expiration in Background Sync
**What goes wrong:** A long-running `WorkManager` upload fails because the Firebase Auth token expired.
**Prevention:** The Firebase SDK handles token refresh automatically, but you must ensure the user is logged in before starting the worker.

## Minor Pitfalls

### Pitfall 1: Search Snippet Indexing
**What goes wrong:** Firestore indexes the entire text snippet, which is unnecessary for simple "contains" searches and adds storage cost.
**Prevention:** Use single-field index exemptions for large text fields that don't need indexing.

## Phase-Specific Warnings

| Phase Topic | Likely Pitfall | Mitigation |
|-------------|---------------|------------|
| Firebase Sync | Conflict Resolution | Use Firestore's `set(data, SetOptions.merge())` or transactions. |
| Background Upload | WorkManager Kill | Persist `uploadSessionUri` to allow resumption. |
| ML Kit OCR | Low Confidence Results | Show "OCR Confidence" indicators or allow manual editing of text. |

## Sources

- [Firebase Storage Pitfalls](https://firebase.google.com/docs/storage/android/handle-errors)
- [Firestore Limits Documentation](https://firebase.google.com/docs/firestore/quotas)
