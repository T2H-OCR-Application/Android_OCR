# Phase 2 Validation Architecture

This document defines the test cases and mapping for validating the implementation of Phase 2: Firebase Integration & Synchronization.

## Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 + AndroidX Test |
| Config file | `app/build.gradle.kts` |
| Full suite command | `./gradlew connectedAndroidTest` |

## Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | Target File |
|--------|----------|-----------|-------------------|-------------|
| REQ-1.3.1 | Auth Linking | Instrument | `./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.t2h.ocr.ScanFlowTest` | `app/src/androidTest/java/com/t2h/ocr/ScanFlowTest.kt` |
| REQ-1.3.2 | Firestore Sync | Instrument | `./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.t2h.ocr.SyncWorkerTest` | `app/src/androidTest/java/com/t2h/ocr/SyncWorkerTest.kt` |
| REQ-1.3.3 | Storage Upload | Instrument | `./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.t2h.ocr.SyncWorkerTest` | `app/src/androidTest/java/com/t2h/ocr/SyncWorkerTest.kt` |

## Validation Criteria
The execution of the tests listed in the automated command column must pass successfully to confirm that Phase 2 requirements have been fully satisfied.