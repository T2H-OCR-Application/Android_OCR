---
phase: 01-foundation
reviewed: 2025-03-05T10:30:00Z
depth: standard
files_reviewed: 11
files_reviewed_list:
  - app/src/main/java/com/t2h/ocr/ui/components/PermissionRationale.kt
  - app/src/main/java/com/t2h/ocr/ui/theme/Color.kt
  - app/src/main/java/com/t2h/ocr/ui/theme/Theme.kt
  - app/src/main/java/com/t2h/ocr/MainActivity.kt
  - app/src/main/java/com/t2h/ocr/data/models/ScanMetadata.kt
  - app/src/main/java/com/t2h/ocr/ui/scanner/ScannerViewModel.kt
  - app/src/main/java/com/t2h/ocr/ui/scanner/ScannerScreen.kt
  - app/src/main/java/com/t2h/ocr/domain/ocr/PdfGenerator.kt
  - app/src/main/java/com/t2h/ocr/data/local/JsonStorage.kt
  - app/src/main/java/com/t2h/ocr/ui/results/ResultsScreen.kt
  - app/src/androidTest/java/com/t2h/ocr/ScanFlowTest.kt
findings:
  critical: 3
  warning: 6
  info: 2
  total: 11
status: issues_found
---

# Phase 01: Code Review Report

**Reviewed:** 2025-03-05
**Depth:** standard
**Files Reviewed:** 11
**Status:** issues_found

## Summary

The foundation for the Android OCR app is functional but contains significant architectural and resource management risks. The most critical issues involve data integrity (incorrect text saved to PDF), memory management (unprotected Bitmap handling in navigation state), and resource leaks (Firebase Auth listeners). The project also violates Clean Architecture principles by placing complex business logic (IO, PDF generation, WorkManager) directly inside Compose UI components.

## Critical Issues

### CR-01: User Edits Ignored in PDF Generation

**File:** `app/src/main/java/com/t2h/ocr/ui/results/ResultsScreen.kt:94`
**Issue:** The `PdfGenerator.generateSearchablePdf` function is called using the original `recognizedText` object from ML Kit instead of the `editedText` state variable. This means any corrections made by the user in the UI are discarded in the final PDF.
**Fix:**
```kotlin
// In ResultsScreen.kt, update the save logic:
// Note: You may need to create a helper to convert editedText back to a compatible ML Kit Text object 
// or modify PdfGenerator to accept a String/List of lines.
PdfGenerator.generateSearchablePdf(capturedBitmap, recognizedText, out) // currently uses raw ML Kit result
```
*Suggestion: Pass the `editedText` to the PDF generator, or better yet, update the text blocks in the `recognizedText` object before passing it.*

### CR-02: Memory Leak in AuthRepository

**File:** `app/src/main/java/com/t2h/ocr/data/auth/AuthRepository.kt:18`
**Issue:** `addAuthStateListener` is registered in the `init` block of `AuthRepository`, but it is never removed. Since the repository is likely scoped to the application or activity lifecycle, this will cause a leak of the listener and potentially the repository itself if it were ever re-instantiated.
**Fix:**
Implement `Disposable` or a `clear()` method, or use a `callbackFlow` in Kotlin Coroutines to manage the listener lifecycle properly.
```kotlin
fun getAuthStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
    auth.addAuthStateListener(listener)
    awaitClose { auth.removeAuthStateListener(listener) }
}
```

### CR-03: Risk of OOM and TransactionTooLargeException

**File:** `app/src/main/java/com/t2h/ocr/MainActivity.kt:33`
**Issue:** The `Screen.Results` state holds a raw `Bitmap` object. Passing Bitmaps in navigation state is dangerous. If the state is saved (e.g., via `rememberSaveable` or process death), this will cause a `TransactionTooLargeException`. Even with standard `remember`, holding multiple large bitmaps in the navigation stack can quickly lead to `OutOfMemoryError`.
**Fix:**
Save the bitmap to a temporary file in internal storage in the `ScannerScreen` and pass the **file path** or a **content URI** in the `Screen.Results` data class instead of the `Bitmap` object.

## Warnings

### WR-01: Inaccurate PDF Text Alignment

**File:** `app/src/main/java/com/t2h/ocr/domain/ocr/PdfGenerator.kt:47`
**Issue:** `canvas.drawText` uses the bottom of the bounding box as the baseline. This causes text with descenders (g, j, p, q, y) to be positioned incorrectly. Furthermore, it doesn't scale text width, so searchable text won't align with the underlying image elements if they are stretched or condensed.
**Fix:** Use `Paint.getTextBounds` or `FontMetrics` to calculate the correct baseline. Ideally, use `canvas.drawText` with a transform or `canvas.drawTextRun` to better match ML Kit's layout.

### WR-02: Violations of Clean Architecture

**File:** `app/src/main/java/com/t2h/ocr/ui/results/ResultsScreen.kt:76`
**Issue:** Massive logic block inside the `Button.onClick`. It handles file IO, image compression, PDF generation, database updates, and WorkManager scheduling. This makes the UI untestable and fragile.
**Fix:** Move this logic into a `ResultsViewModel` or a dedicated `SaveScanUseCase`. The Composable should only trigger a single function call like `viewModel.saveScan(text, bitmap)`.

### WR-03: Inefficient and Unsafe Local Storage

**File:** `app/src/main/java/com/t2h/ocr/data/local/JsonStorage.kt:53,62`
**Issue:** `addScan` and `updateScan` perform a full Read-Modify-Write cycle on the entire JSON file. As the number of scans grows, this becomes extremely slow ($O(N)$ complexity for every update). Additionally, there is no thread synchronization, so concurrent updates will result in data loss.
**Fix:** Use a local database like **Room**. For a simple JSON file, at least ensure file operations are synchronized and offloaded to `Dispatchers.IO`.

### WR-04: UI Overlap with System Bars

**File:** `app/src/main/java/com/t2h/ocr/ui/scanner/ScannerScreen.kt:101`
**Issue:** The Profile button uses hardcoded top padding (`48.dp`). Since `MainActivity` uses `enableEdgeToEdge()`, this button may overlap with status bar icons on different devices (e.g., devices with notches or different status bar heights).
**Fix:** Use `Modifier.statusBarsPadding()` or `WindowInsets.statusBars` to ensure the UI respects the system safe areas.

### WR-05: Missing Resource Disposal (Bitmap)

**File:** `app/src/main/java/com/t2h/ocr/ui/results/ResultsScreen.kt:31`
**Issue:** `capturedBitmap` is never explicitly recycled. While the JVM/ART GC will eventually reclaim it, Bitmaps are memory-heavy, and it is best practice to `recycle()` them as soon as they are no longer needed, especially after they are saved to disk.
**Fix:** Call `capturedBitmap.recycle()` after the IO operation is complete or when the `ResultsScreen` is disposed.

### WR-06: Incorrect Error Handling in SyncWorker

**File:** `app/src/main/java/com/t2h/ocr/data/sync/SyncWorker.kt:73`
**Issue:** The retry logic checks for `PERMISSION_DENIED` to handle LWW (Last-Writer-Wins) conflicts. `PERMISSION_DENIED` is a security rule failure, not a data conflict. Standard LWW conflicts in Firestore are usually handled via Transactions or simply by overwriting (which is what `set()` does).
**Fix:** Use Firestore Transactions if atomicity is required, or remove the retry-on-permission-denied logic as it is misleading.

## Info

### IN-01: Hardcoded Strings

**File:** `app/src/main/java/com/t2h/ocr/ui/components/PermissionRationale.kt:71`
**Issue:** User-facing strings are hardcoded in Composables. This prevents localization.
**Fix:** Move all strings to `res/values/strings.xml`.

### IN-02: Camera Implementation Mode

**File:** `app/src/main/java/com/t2h/ocr/ui/scanner/ScannerScreen.kt:70`
**Issue:** `PreviewView.ImplementationMode.COMPATIBLE` is used. This often results in lower performance (using `TextureView` instead of `SurfaceView`).
**Fix:** Use `PERFORMANCE` (default) unless there is a specific reason to use `COMPATIBLE`.

---

_Reviewed: 2025-03-05_
_Reviewer: gsd-code-reviewer_
_Depth: standard_
