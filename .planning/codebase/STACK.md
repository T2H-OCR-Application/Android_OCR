# Technology Stack

**Analysis Date:** 2025-02-14

## Languages

**Primary:**
- Kotlin 2.0.21 - Core application logic, UI, and background processing across all source files.

**Secondary:**
- SQL (SQLite) - Used via Room persistence library for local data storage.

## Runtime

**Environment:**
- Android SDK (minSdk 24, targetSdk 36)
- Java 17 (JVM target)

**Package Manager:**
- Gradle (Kotlin DSL)
- Version Catalog: `gradle/libs.versions.toml`
- Lockfile: missing (standard for Android projects)

## Frameworks

**Core:**
- Jetpack Compose (BOM 2024.10.01) - Modern UI toolkit for building all screens.
- Android Architecture Components (Lifecycle, ViewModel, Navigation-like sealed class pattern).

**Testing:**
- JUnit 4 - Unit testing.
- MockK - Mocking library for tests.
- Robolectric - Android unit testing framework.
- Roborazzi - Screenshot testing.
- Turbine - Flow testing.

**Build/Dev:**
- Android Gradle Plugin 8.9.1 - Build system.
- Kotlin Serialization Plugin - JSON parsing.
- Kapt - Annotation processing for Room.

## Key Dependencies

**Critical:**
- CameraX 1.4.0 - Camera implementation in `app/src/main/java/com/t2h/ocr/ui/scanner/ScannerScreen.kt`.
- ML Kit Text Recognition 16.0.1 - OCR processing in `app/src/main/java/com/t2h/ocr/MainActivity.kt`.
- OpenCV 4.5.3.0 - Image processing and document detection in `app/src/main/java/com/t2h/ocr/domain/ocr/DocumentAnalyzer.kt` and `app/src/main/java/com/t2h/ocr/domain/ocr/ImageProcessor.kt`.
- Room 2.7.0 - Local database management.

**Infrastructure:**
- WorkManager 2.10.0 - Background synchronization in `app/src/main/java/com/t2h/ocr/data/sync/SyncWorker.kt`.
- DataStore 1.1.1 - User preferences storage in `app/src/main/java/com/t2h/ocr/data/local/UserPreferences.kt`.
- Coil 2.7.0 - Image loading for Compose.

## Configuration

**Environment:**
- Configured via `google-services.json` for Firebase.
- `gradle.properties` for build-wide settings.

**Build:**
- `app/build.gradle.kts` - Main application module configuration.
- `build.gradle.kts` - Root project configuration.
- `gradle/libs.versions.toml` - Centralized dependency management.

## Platform Requirements

**Development:**
- Android Studio Ladybug or newer.
- JDK 17.

**Production:**
- Android devices running Android 7.0 (API 24) or higher.

---

*Stack analysis: 2025-02-14*
