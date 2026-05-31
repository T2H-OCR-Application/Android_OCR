# Codebase Concerns

**Analysis Date:** 2025-05-14

## Tech Debt

**God Object (MainActivity):**
- Issue: `MainActivity` manages too many responsibilities including navigation state, screen definitions, OCR logic coordination (`processAndOcr`), and repository instantiation.
- Files: `app/src/main/java/com/t2h/ocr/MainActivity.kt`
- Impact: Poor maintainability, difficult to unit test navigation and flow logic, high risk of regressions.
- Fix approach: Implement `navigation-compose` for routing and move business logic/coordination to `ViewModels` and `UseCases`.

**JSON-Based Metadata Storage:**
- Issue: Metadata for scans is stored in a single JSON file managed by `JsonStorage`. Every write operation requires reading/writing the entire file.
- Files: `app/src/main/java/com/t2h/ocr/data/local/JsonStorage.kt`
- Impact: Performance degrades as the number of scans grows (O(N) operations). Risk of data corruption if writes are interrupted.
- Fix approach: Replace `JsonStorage` with a Room database for efficient querying and atomic updates.

**Manual Resource Management:**
- Issue: Native OpenCV `Mat` and Android `Bitmap` resources are manually managed with `.release()` and `.recycle()`.
- Files: `app/src/main/java/com/t2h/ocr/domain/ocr/DocumentAnalyzer.kt`, `app/src/main/java/com/t2h/ocr/MainActivity.kt`
- Impact: High risk of memory leaks if a release call is missed in an early return or exception path.
- Fix approach: Use wrapper classes with `AutoCloseable` or `use` extension functions consistently; transition to more robust resource tracking.

**Weak Error Handling:**
- Issue: Extensive use of `e.printStackTrace()` instead of structured logging or crash reporting.
- Files: `app/src/main/java/com/t2h/ocr/data/local/JsonStorage.kt`, `app/src/main/java/com/t2h/ocr/ui/home/HomeViewModel.kt`, `app/src/main/java/com/t2h/ocr/ui/results/ResultsViewModel.kt`
- Impact: Difficult to debug issues in production; no visibility into silent failures.
- Fix approach: Integrate Timber for logging and Firebase Crashlytics for error reporting.

## Known Bugs

**Navigation State Loss:**
- Symptoms: The app may lose its current screen state or captured pages during configuration changes or process death because state is managed via `remember { mutableStateOf(...) }` in `MainActivity`.
- Files: `app/src/main/java/com/t2h/ocr/MainActivity.kt`
- Trigger: Device rotation or background process death.
- Workaround: Use `rememberSaveable` or move state to a `SavedStateHandle` in a `ViewModel`.

## Security Considerations

**File Permission Sandboxing:**
- Risk: While using internal storage (`filesDir`, `cacheDir`), sensitive scanned documents are stored as plain files.
- Files: `app/src/main/java/com/t2h/ocr/data/local/JsonStorage.kt`, `app/src/main/java/com/t2h/ocr/domain/ocr/PdfGenerator.kt`
- Current mitigation: Internal storage is private to the app.
- Recommendations: Consider EncryptedSharedPreferences for settings and Biometric prompt for accessing the history if privacy requirements increase.

## Performance Bottlenecks

**O(N) Storage Operations:**
- Problem: `JsonStorage` scales poorly. Adding or deleting a scan becomes slower as the history grows.
- Files: `app/src/main/java/com/t2h/ocr/data/local/JsonStorage.kt`
- Cause: Entire JSON list is serialized/deserialized for every change.
- Improvement path: Migrate to Room.

**In-Memory PDF Generation:**
- Problem: `PdfDocument` builds the entire document in memory.
- Files: `app/src/main/java/com/t2h/ocr/domain/ocr/PdfGenerator.kt`
- Cause: Native Android `PdfDocument` API does not support streaming pages to disk incrementally.
- Improvement path: For multi-page or high-res document support, consider a streaming PDF library or careful memory monitoring.

## Fragile Areas

**DocumentAnalyzer Pipeline:**
- Files: `app/src/main/java/com/t2h/ocr/domain/ocr/DocumentAnalyzer.kt`
- Why fragile: Tight timing requirements and manual `Mat` lifecycle. If the camera sends frames faster than processing, or if `release()` isn't called, it crashes or leaks.
- Safe modification: Ensure `ImageProxy.close()` is always called in `finally` and `Mat.use {}` is used for temporary matrices.
- Test coverage: Low. Difficult to test without real camera frames.

## Scaling Limits

**History Size:**
- Current capacity: Limited by internal storage and JSON parsing speed.
- Limit: ~1000 items before UI and storage lag becomes noticeable.
- Scaling path: Room DB with pagination (Paging 3).

## Dependencies at Risk

**OpenCV Native Library:**
- Risk: Native libraries increase APK size and complexity. Initialization can fail on certain architectures.
- Impact: App might crash on startup if native libs fail to load.
- Migration plan: Monitor crash reports for OpenCV init failures.

## Missing Critical Features

**Dependency Injection:**
- Problem: Manual instantiation of repositories and factories.
- Blocks: Clean testing and decoupled components.

**Robust Sync Recovery:**
- Problem: `SyncWorker` handles Drive uploads but lacks advanced retry logic for partial failures (e.g., metadata synced but file upload failed).
- Blocks: Reliable cloud synchronization.

## Test Coverage Gaps

**Image Processing Logic:**
- What's not tested: `DocumentAnalyzer` contour detection and `ImageProcessor` warping logic.
- Files: `app/src/main/java/com/t2h/ocr/domain/ocr/DocumentAnalyzer.kt`, `app/src/main/java/com/t2h/ocr/domain/ocr/ImageProcessor.kt`
- Risk: Regression in document detection accuracy or image quality.
- Priority: High

---

*Concerns audit: 2025-05-14*
