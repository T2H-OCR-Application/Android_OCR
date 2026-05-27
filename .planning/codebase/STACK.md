# Technology Stack

**Analysis Date:** 2025-01-24

## Languages

**Primary:**
- Kotlin 2.0.21 - Used for all Android application logic and UI components.

**Secondary:**
- Kotlin DSL - Used for Gradle build configuration files (`build.gradle.kts`, `settings.gradle.kts`).
- XML - Used for Android Manifest, resources, and configuration (`AndroidManifest.xml`, `res/values/*.xml`).

## Runtime

**Environment:**
- Android SDK (Target 35, Compile 35, Min 24)
- Java 11 (Target and Source compatibility)

**Package Manager:**
- Gradle 9.3.1
- Lockfile: Not detected (using `libs.versions.toml` for version management).

## Frameworks

**Core:**
- Jetpack Compose (BOM 2024.10.01) - Primary UI framework.
- Material 3 - UI component library.

**Testing:**
- JUnit 4.13.2 - Unit testing.
- AndroidX JUnit 1.2.1 - Instrumentation testing.
- Espresso 3.6.1 - UI testing.
- Compose UI Test - Testing Compose layouts.

**Build/Dev:**
- Android Gradle Plugin (AGP) 8.7.3 - Build system.
- Kotlin Compose Compiler (integrated in Kotlin 2.0.21).

## Key Dependencies

**Critical:**
- `androidx.core:core-ktx:1.15.0` - Standard Android KTX libraries.
- `androidx.activity:activity-compose:1.9.3` - Integration between Activity and Compose.
- `androidx.lifecycle:lifecycle-runtime-ktx:2.8.7` - Lifecycle-aware components.

**Infrastructure:**
- `com.google.mlkit:text-recognition:16.0.0` - On-device OCR capabilities.
- `com.google.firebase:firebase-bom:33.7.0` - Firebase platform orchestration.

## Configuration

**Environment:**
- `gradle.properties` - Gradle build properties.
- `app/google-services.json` - Firebase configuration.

**Build:**
- `build.gradle.kts` (root) - `C:\Users\testu\OneDrive\Máy tính\New folder (2)\Android_OCR\build.gradle.kts`
- `app/build.gradle.kts` - `C:\Users\testu\OneDrive\Máy tính\New folder (2)\Android_OCR\app\build.gradle.kts`
- `settings.gradle.kts` - `C:\Users\testu\OneDrive\Máy tính\New folder (2)\Android_OCR\settings.gradle.kts`
- `gradle/libs.versions.toml` - `C:\Users\testu\OneDrive\Máy tính\New folder (2)\Android_OCR\gradle\libs.versions.toml`

## Platform Requirements

**Development:**
- Android Studio Ladybug or newer.
- JDK 11+.

**Production:**
- Android Device (API 24 or higher).

---

*Stack analysis: 2025-01-24*
