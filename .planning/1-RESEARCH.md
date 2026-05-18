# Phase 1: Project Foundation & Core OCR (MVP) - Research

**Researched:** 2025-01-24
**Domain:** Android / CameraX / ML Kit / PDF Generation
**Confidence:** HIGH

## Summary

This phase establishes the bedrock of the Android OCR application. We will implement a real-time OCR scanner using CameraX and Google ML Kit (Text Recognition v2), integrated into a Jetpack Compose UI. Metadata will be persisted in local JSON files, and a basic searchable PDF generation engine will be built using the native `PdfDocument` API.

**Primary recommendation:** Use `LifecycleCameraController` for a more "Compose-friendly" CameraX integration and `CoordinateTransform` for accurate bounding box mapping.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Camera Preview | Client (Android) | — | Hardware-bound; requires `PreviewView` |
| Image Analysis | Client (Android) | — | Real-time on-device ML processing (ML Kit) |
| OCR Logic | Client (Android) | — | ML Kit Text Recognition v2 client |
| PDF Generation | Client (Android) | — | Native `PdfDocument` API |
| Metadata Storage | Client (Android) | — | Local file-based JSON storage |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `androidx.camera:camera-camera2` | `1.4.0` | Camera hardware interface | Industry standard for Android camera dev |
| `androidx.camera:camera-lifecycle` | `1.4.0` | Lifecycle binding | Prevents leaks; handles lifecycle automatically |
| `androidx.camera:camera-view` | `1.4.0` | PreviewView & Transforms | Required for UI integration and coordinate mapping |
| `com.google.mlkit:text-recognition` | `16.0.1` | OCR Engine (Latin) | High accuracy, on-device, multi-script support |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | `1.7.3` | JSON Serialization | Native Kotlin support, type-safe, performant |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|--------------|
| `androidx.activity:activity-compose` | `1.9.3` | Compose Entry Point | Standard for modern Compose activities |
| `androidx.compose.material3:material3` | `1.3.1` | UI Components | Material 3 is the project's foundation |
| `androidx.camera:camera-mlkit-vision` | `1.4.0` | ML Kit Helper | Simplifies `ImageAnalysis` integration |

**Installation:**
```kotlin
// build.gradle.kts
dependencies {
    val camerax_version = "1.4.0"
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.camera:camera-mlkit-vision:$camerax_version")
    
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
```

## Package Legitimacy Audit

| Package | Registry | Age | Downloads | Source Repo | slopcheck | Disposition |
|---------|----------|-----|-----------|-------------|-----------|-------------|
| `androidx.camera:camera-core` | Maven | ~8 yrs | Millions/wk | Google | [OK] | Approved |
| `com.google.mlkit:text-recognition` | Maven | ~4 yrs | Millions/wk | Google | [OK] | Approved |
| `kotlinx-serialization-json` | Maven | ~6 yrs | Millions/wk | JetBrains | [OK] | Approved |

## Architecture Patterns

### Recommended Project Structure
```
app/src/main/java/com/t2h/ocr/
├── data/
│   ├── local/
│   │   ├── JsonStorage.kt       # Metadata persistence
│   │   └── FileUtils.kt         # Image/PDF file management
│   └── models/
│       └── ScanMetadata.kt      # @Serializable Scan entity
├── domain/
│   └── ocr/
│       └── PdfGenerator.kt      # PdfDocument logic
├── ui/
│   ├── components/
│   │   └── PermissionRationale.kt
│   ├── scanner/
│   │   ├── ScannerScreen.kt     # CameraX + Compose
│   │   └── ScannerViewModel.kt
│   └── theme/
│       └── Color.kt             # Deep Blue: Color(0xFF003366)
```

### Pattern 1: CameraX + ML Kit Integration
**What:** Using `LifecycleCameraController` with `setImageAnalysisAnalyzer` and `CoordinateTransform`.
**When to use:** For real-time OCR with visual feedback (bounding boxes).
**Example:**
```kotlin
// Source: [Verified via Android Developer Docs & Search]
val controller = remember {
    LifecycleCameraController(context).apply {
        setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
    }
}

LaunchedEffect(Unit) {
    controller.setImageAnalysisAnalyzer(executor) { imageProxy ->
        val inputImage = InputImage.fromMediaImage(
            imageProxy.image!!, 
            imageProxy.imageInfo.rotationDegrees
        )
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                // Use CoordinateTransform to map visionText.boundingBox 
                // to PreviewView coordinates
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
```

### Pattern 2: Searchable PDF Generation
**What:** Drawing an image followed by a transparent text layer in `PdfDocument`.
**When to use:** Creating PDFs where text can be selected/searched.
**Example:**
```kotlin
fun createSearchablePdf(bitmap: Bitmap, textBlocks: List<TextBlock>, outputStream: OutputStream) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    // 1. Draw Image Layer
    canvas.drawBitmap(bitmap, 0f, 0f, null)

    // 2. Draw Text Layer (Invisible but Searchable)
    val paint = Paint().apply {
        color = Color.TRANSPARENT
        textSize = 12f // Adjusted per block
    }
    
    for (block in textBlocks) {
        val rect = block.boundingBox ?: continue
        // Optional: Scale text size to fit rect
        canvas.drawText(block.text, rect.left.toFloat(), rect.bottom.toFloat(), paint)
    }

    pdfDocument.finishPage(page)
    pdfDocument.writeTo(outputStream)
    pdfDocument.close()
}
```

## JSON Schema: Local Scan Metadata

```json
{
  "id": "uuid-v4",
  "title": "Scan 2024-05-20",
  "timestamp": 1716192000000,
  "ocrText": "Full recognized text content...",
  "imagePath": "/storage/emulated/0/Android/data/.../scans/image_123.jpg",
  "pdfPath": "/storage/emulated/0/Android/data/.../scans/doc_123.pdf",
  "language": "en"
}
```

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| OCR Engine | Custom Tesseract build | Google ML Kit | Tesseract is heavy and hard to tune; ML Kit is optimized and reliable on Android. |
| Camera UI | Camera2 API from scratch | CameraX | CameraX handles device quirks, rotation, and aspect ratios automatically. |
| Permission Handling | Custom dialog logic | Material 3 + ActivityResult | Standardized UX; easier to maintain. |

## Common Pitfalls

### Pitfall 1: ImageProxy Leak
**What goes wrong:** The camera preview freezes or the analyzer stops receiving frames.
**Why it happens:** Failing to call `imageProxy.close()` in the `onComplete` or `onFailure` listener of ML Kit.
**How to avoid:** Always wrap processing in a `try-finally` or use `addOnCompleteListener { imageProxy.close() }`.

### Pitfall 2: Coordinate Mismatch
**What goes wrong:** OCR bounding boxes appear shifted or rotated on the screen.
**Why it happens:** ML Kit works in the image's coordinate system (often rotated 90 deg), while the UI is in screen coordinates.
**How to avoid:** Use `androidx.camera.view.transform.CoordinateTransform`.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `PdfDocument` text layer is searchable in most viewers | Pattern 2 | Search functionality might be limited in some "lite" viewers. |
| A2 | Bundled ML Kit is better than Unbundled for Phase 1 | Standard Stack | APK size will be larger (~30MB increase). |

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Android SDK 34 | Core Dev | ✓ | 35 | — |
| Gradle | Build | ✓ | 8.7.3 | — |
| Google Play Services | Unbundled ML Kit | ✓ | — | Use Bundled ML Kit |

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 + Espresso + Compose UI Test |
| Config file | `app/build.gradle.kts` |
| Quick run command | `./gradlew test` |
| Full suite command | `./gradlew connectedAndroidTest` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command |
|--------|----------|-----------|-------------------|
| OCR-01 | Recognize text from bitmap | Unit | `./gradlew test` |
| CAM-01 | Show camera preview | UI | `./gradlew connectedCheck` |
| PER-01 | Show rationale before dialog | UI | `./gradlew connectedCheck` |

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V5 Input Validation | Yes | Validate JSON metadata on load |
| V12 File System | Yes | Use internal storage for sensitive scans; Scoped Storage for exports |

### Known Threat Patterns for Android
| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Insecure File Storage | Information Disclosure | Use `Context.getFilesDir()` or `getExternalFilesDir()` |
| Excessive Permissions | Elevation of Privilege | Only request `CAMERA`; use `Media/Storage` access appropriately |

## Sources

### Primary (HIGH confidence)
- [Android CameraX Docs](https://developer.android.com/training/camerax)
- [Google ML Kit Text Recognition Docs](https://developers.google.com/ml-kit/vision/text-recognition/android)
- [Context7] - `/google/jetpack-camera-app` integration patterns.

### Secondary (MEDIUM confidence)
- [Maven Central] - Version verification for CameraX and ML Kit.
