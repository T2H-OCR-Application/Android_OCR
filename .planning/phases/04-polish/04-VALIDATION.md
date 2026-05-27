# Phase 4 Validation Architecture

## Overview
This document defines the validation strategy for Phase 4: Polish, Testing & Deployment. It ensures that the hardening, testing, performance, and release readiness requirements are met before final delivery.

## 1. Quality Gates & Testing
- **Goal:** Verify that the "Triple-Tier" testing strategy is fully implemented and passes.
- **Method:**
    - Run all unit tests: `./gradlew test`. Assert 100% pass rate and >80% coverage for core logic (`PdfGenerator`, `SyncWorker`).
    - Run E2E Compose UI tests: `./gradlew connectedDebugAndroidTest`. Verify the full "Capture → Sync" flow passes on a physical device or emulator.
    - Run Snapshot tests: `./gradlew verifyRoborazziDebug`. Verify that current UI matches established baselines for Bento Grid and Overlays.

## 2. Performance & Memory
- **Goal:** Confirm the application is free of OpenCV-related memory leaks and meets latency targets.
- **Method:**
    - **LeakCanary Audit:** Perform 10 consecutive scan sessions. Assert that LeakCanary reports zero leaks.
    - **Latency Check:** Use the Android Studio Profiler to measure the `DocumentAnalyzer` processing time. Assert that frame processing remains under 33ms (30fps) on mid-range hardware.
    - **APK Size:** Compare final release APK size against Phase 3 baseline. Assert that R8 optimization has reduced or maintained the size despite the addition of testing/localization libraries.

## 3. Resilience & Observability
- **Goal:** Verify that the app recovers from failures gracefully and reports telemetry correctly.
- **Method:**
    - **Backoff Test:** Force a network failure during sync. Assert that `WorkManager` schedules a retry with the configured exponential backoff.
    - **Error Visibility:** Trigger a mock camera permission denial or hardware failure. Assert that the "High-Visibility" error screen is displayed with a recovery button.
    - **Analytics Audit:** Verify that custom events (e.g., `ocr_success`, `sync_duration`) are logged correctly in the Firebase Analytics debug view.

## 4. Release Readiness
- **Goal:** Confirm the app is ready for global deployment.
- **Method:**
    - **Localization Check:** Switch device language to Spanish (ES) and Vietnamese (VI). Verify that all UI elements are translated and no hardcoded English strings remain.
    - **R8 Verification:** Run the release build: `./gradlew assembleRelease`. Verify that the app launches successfully and that ProGuard rules have not stripped essential ML Kit or OpenCV native components.
