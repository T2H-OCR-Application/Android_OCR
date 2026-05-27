# Security Verification: Phase 1

## Threat Model & Mitigations

| Threat ID | Category | Component | Mitigation Plan | Status | Evidence |
|-----------|----------|-----------|-----------------|--------|----------|
| T-01-01 | Tampering | build.gradle.kts | Use specific versions from RESEARCH.md (Locking) | VERIFIED | `libs.versions.toml` pins all major versions. |
| T-01-02 | Information Disclosure | Camera | Only use Camera permission when the scanner is active | VERIFIED | `ScannerScreen.kt` uses `LifecycleCameraController` for lifecycle-aware camera management. |
| T-01-03 | Tampering | scans.json | Validate JSON structure on load | VERIFIED | `JsonStorage.kt` uses `try-catch` blocks and `kotlinx.serialization` for safe decoding. |
| T-01-04 | Information Disclosure | Local Storage | Store PDFs and JSON in app-private directory (`filesDir`) | VERIFIED | `JsonStorage.kt` and `ResultsScreen.kt` use `context.filesDir` for data persistence. |

## Audit Results

### T-01-01: Dependency Locking
- [x] Check `app/build.gradle.kts` for version ranges or dynamic versions.
- **Finding:** All dependencies are pinned in `libs.versions.toml` and referenced correctly.

### T-01-02: Camera Access
- [x] Verify `ScannerScreen` and `ScannerViewModel` lifecycle management.
- [x] Check for any background camera usage.
- **Finding:** Camera starts/stops with the UI lifecycle; no background analyzers detected.

### T-01-03: JSON Validation
- [x] Audit `JsonStorage.kt` for error handling during deserialization.
- **Finding:** Graceful failure (empty list) implemented for malformed JSON, preventing crashes.

### T-01-04: Data Isolation
- [x] Verify storage paths in `JsonStorage.kt` and `PdfGenerator.kt`.
- **Finding:** Images, PDFs, and `scans.json` are strictly contained within the app's internal storage (`/data/user/0/com.t2h.ocr/files`).
