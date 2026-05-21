---
phase: 02-firebase
reviewed: 2025-03-05T10:15:00Z
depth: standard
files_reviewed: 11
files_reviewed_list:
  - app/src/main/java/com/t2h/ocr/data/auth/AuthRepository.kt
  - app/src/main/java/com/t2h/ocr/MainActivity.kt
  - app/src/main/java/com/t2h/ocr/ui/profile/ProfileScreen.kt
  - app/src/main/java/com/t2h/ocr/ui/scanner/ScannerScreen.kt
  - app/src/main/java/com/t2h/ocr/data/models/ScanMetadata.kt
  - app/src/main/java/com/t2h/ocr/data/local/JsonStorage.kt
  - app/src/main/java/com/t2h/ocr/data/sync/SyncWorker.kt
  - app/src/main/java/com/t2h/ocr/ui/results/ResultsScreen.kt
  - app/src/main/AndroidManifest.xml
  - firestore.rules
  - storage.rules
findings:
  critical: 0
  warning: 5
  info: 3
  total: 8
status: issues_found
---

# Phase 02: Code Review Report

**Reviewed:** 2025-03-05
**Depth:** standard
**Files Reviewed:** 11
**Status:** issues_found

## Summary

The Firebase integration phase implements anonymous authentication, account linking with Google, local JSON persistence, and background synchronization using WorkManager. Security rules for Firestore and Storage are well-defined with per-user isolation and "Last Writer Wins" (LWW) logic.

However, several issues were identified regarding UI responsiveness, data integrity, and Android 14 compatibility. The local storage implementation is non-atomic and runs on the main thread, posing a risk of data corruption and UI hangs. Authentication state management is also loose, which may lead to synchronization failures.

## Warnings

### WR-01: UI-Blocking I/O in ResultsScreen

**File:** `app/src/main/java/com/t2h/ocr/ui/results/ResultsScreen.kt:111`
**Issue:** `JsonStorage.addScan` performs file I/O operations (`readText`, `writeText`) directly on the main thread inside the button's `onClick` handler. As the scan history grows, this will cause noticeable UI stutters or ANR (Application Not Responding) errors.
**Fix:**
Wrap the storage call in a coroutine using `viewModelScope` or `LaunchedEffect`'s scope, switching to `Dispatchers.IO`.

```kotlin
// In ResultsScreen.kt
val scope = rememberCoroutineScope()
// ...
Button(
    onClick = {
        scope.launch(Dispatchers.IO) {
            // ... saving logic ...
            jsonStorage.addScan(metadata)
            withContext(Dispatchers.Main) {
                onSaveComplete()
            }
        }
    }
)
```

### WR-02: Non-Atomic File Storage

**File:** `app/src/main/java/com/t2h/ocr/data/local/JsonStorage.kt:30`
**Issue:** `JsonStorage` uses `File.writeText` which overwrites the file directly. If the app crashes or the device loses power during this operation, the `scans.json` file may be truncated or corrupted, resulting in the loss of all scan history.
**Fix:**
Implement atomic writes by writing to a temporary file and then renaming it to the target file, or use `AtomicFile` from the AndroidX core library.

```kotlin
fun saveScans(scans: List<ScanMetadata>) {
    val atomicFile = AtomicFile(file)
    var stream: FileOutputStream? = null
    try {
        val jsonString = json.encodeToString(scans)
        stream = atomicFile.startWrite()
        stream.write(jsonString.toByteArray())
        atomicFile.finishWrite(stream)
    } catch (e: Exception) {
        atomicFile.failWrite(stream)
        e.printStackTrace()
    }
}
```

### WR-03: Missing Foreground Service Type for Android 14

**File:** `app/src/main/java/com/t2h/ocr/data/sync/SyncWorker.kt:125`
**Issue:** Android 14 (API 34) requires foreground services to specify their type at runtime. `SyncWorker` triggers a foreground notification but doesn't pass the `DATA_SYNC` type to `ForegroundInfo`. While it is declared in the manifest, missing it at runtime can cause issues.
**Fix:**
Update `createForegroundInfo` to include the service type.

```kotlin
return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    ForegroundInfo(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
} else {
    ForegroundInfo(notificationId, notification)
}
```

### WR-04: Lack of Auth State Handling in MainActivity

**File:** `app/src/main/java/com/t2h/ocr/MainActivity.kt:49`
**Issue:** The app calls `signInAnonymously()` but does not observe the result or the auth state before allowing the user to reach the scanner. If sign-in fails or takes too long, `SyncWorker` will immediately fail because `auth.currentUser` is null.
**Fix:**
Observe the `authRepository.currentUser` StateFlow in `MainActivity` and show a loading state or error message until the user is authenticated.

### WR-05: Coordinate Inconsistency in ScannerScreen

**File:** `app/src/main/java/com/t2h/ocr/ui/scanner/ScannerScreen.kt:82`
**Issue:** The `Canvas` draws bounding boxes using coordinates from ML Kit. While `MlKitAnalyzer` is configured for `COORDINATE_SYSTEM_VIEW_REFERENCED`, any scaling or letterboxing performed by `PreviewView` (due to aspect ratio mismatch) may cause the boxes to be offset relative to the actual text in the preview.
**Fix:**
Ensure the `Canvas` matches the exact visible area of the `PreviewView` or use a transformation matrix provided by CameraX's `PreviewView.getOutputTransform()`.

## Info

### IN-01: Hardcoded Padding for System Bars

**File:** `app/src/main/java/com/t2h/ocr/ui/scanner/ScannerScreen.kt:101`
**Issue:** Top padding for the profile icon is hardcoded to `48.dp`. This may overlap with the status bar or leave too much gap on different devices.
**Fix:**
Use `Modifier.windowInsetsPadding(WindowInsets.statusBars)`.

### IN-02: Generic Error Handling

**File:** `app/src/main/java/com/t2h/ocr/data/local/JsonStorage.kt:33`
**Issue:** Widespread use of `e.printStackTrace()` makes it difficult to debug in production and provides no feedback to the user when critical operations (like saving) fail.
**Fix:**
Use a proper logging library (e.g., Timber) and propagate errors to the UI via Results or State objects.

### IN-03: Non-User-Friendly Titles

**File:** `app/src/main/java/com/t2h/ocr/ui/results/ResultsScreen.kt:97`
**Issue:** Default scan titles use `Date(timestamp).toString()`, which is verbose and not localized (e.g., "Scan Wed Mar 05 10:15:00 GMT 2025").
**Fix:**
Use `java.text.DateFormat` or `kotlinx-datetime` to format a friendly localized date/time string.

---

_Reviewed: 2025-03-05_
_Reviewer: gsd-code-reviewer_
_Depth: standard_
