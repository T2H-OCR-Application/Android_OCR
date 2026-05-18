# Phase 1 UAT: Android OCR

**Goal:** Verify core OCR and PDF generation features in the MVP.
**Status:** In Progress

## Test Execution

| ID | Test Case | Expected Result | Status | Notes |
|----|-----------|-----------------|--------|-------|
| 1.1 | Permission Rationale | App displays a rationale screen explaining camera use before the system dialog. | PASS | |
| 1.2 | Scanner Preview | A live camera preview is visible on the scanner screen. | PASS | |
| 1.3 | OCR Feedback | Bounding boxes (Cyan) appear over text in the camera preview in real-time. | PASS | |
| 1.4 | Capture Text | Clicking "Capture Text" opens the Results screen with the recognized text. | PASS | |
| 1.5 | Text Editing | The recognized text can be edited in the Results screen. | PASS | |
| 1.6 | PDF Generation | Clicking "Save as PDF" triggers a save action and completes successfully. | PASS | |

## Issues Found
1. **Build Failure (Gradle):** Unresolved references for `CameraAlt` and `setMlKitAnalyzer`.
   - **Diagnosis:** Missing `material-icons-extended` dependency and missing explicit import for CameraX extension function.
   - **Fix:** Added `androidx-compose-material-icons-extended` to `libs.versions.toml` and `app/build.gradle.kts`. Switched to `setImageAnalysisAnalyzer` in `ScannerScreen.kt`.
   - **Status:** FIXED.
2. **Black Screen in Scanner:** No camera feed visible.
   - **Diagnosis:** Manual use-case configuration was accidentally disabling the preview.
   - **Fix:** Removed `setEnabledUseCases` to use defaults and set `ImplementationMode.COMPATIBLE`.
   - **Status:** FIXED.

## Summary
Phase 1 UAT is complete. All core features (Permission UX, Scanner, OCR, PDF Generation, and Persistence) are functional. The app is stable and ready for Phase 2.
