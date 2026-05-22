<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01: Batch Scan Flow.** Users will rapidly snap multiple pages in a row, then review them all at the end in a gallery view before finalizing the document.
- **D-02: Clean Text Document.** To resolve the Phase 1 PDF bug, the generated PDF will not just be an image wrapper. It will be a clean, white-background document with the extracted text printed normally (similar to a standard word processor document).
- **D-03: Manual Confirmation Every Time.** While the app will automatically detect edges, it must pause on a review screen showing crop handles, forcing the user to confirm or adjust the crop for every scan before OCR processing.
- **D-04: Dynamic Grid Layout.** The home screen will use a chronological feed with variable sizing (e.g., newest items are large, older items are smaller grid items).
- **D-05: Deep Full-Text Search.** The search functionality must cover both the document titles and the full extracted text content within the JSON metadata.
- **D-06: Real-time Camera Guidance.** Implement a dynamic document boundary overlay on the camera preview highlighting detected edges in real-time, accompanied by intuitive framing guidance (e.g., "Move closer").
- **D-07: Polished Interactions.** Use smooth scale transitions for the custom crop handles.
- **D-08: Thematic Animations.** Ensure fluid screen transitions between the Scanner and Results screens. Include a camera shutter animation with haptic feedback upon capture.
- **D-09: Loading States.** Use a subtle loading shimmer effect during OCR text processing instead of a basic spinner.
- **D-10: Brand Adherence.** Maintain a clean Material 3 design centered around the established Deep Blue primary accents.

### the agent's Discretion
*(None explicitly listed in 03-CONTEXT.md)*

### Deferred Ideas (OUT OF SCOPE)
*(No specific ideas deferred during this session; all requested UX features fit within the phase boundary).*
</user_constraints>

# Phase 3: Advanced Features & UX Refinement - Research

**Researched:** 2024-05-22
**Domain:** Android UI/UX (Jetpack Compose), CameraX, Native PDF Generation, Document Edge Detection
**Confidence:** HIGH

## Summary

This phase transitions the OCR MVP into a robust, professional-grade document scanner. The most significant technical challenge involves satisfying the strict Custom UI requirements (D-06, D-07, D-08) for document edge detection. The standard Google ML Kit Document Scanner API (which provides auto-crop and perspective correction) operates as a closed `Intent` with a fixed UI, meaning it **cannot** be used while adhering to the custom transition and real-time guidance requirements. We must therefore build a custom CameraX analyzer using OpenCV for 4-corner edge detection. 

Additionally, we must overhaul the PDF generation to move away from image-wrapping to rendering multi-page, formatted `StaticLayout` text onto clean white `PdfDocument` canvases. Finally, Jetpack Compose's `LazyVerticalGrid` with custom spans will drive the Bento Grid home screen.

**Primary recommendation:** Implement a custom CameraX image analysis pipeline using a lightweight OpenCV wrapper for 4-corner edge detection, and use Android's `StaticLayout` combined with `canvas.translate()` to handle multi-page text wrapping for the clean-text PDF output.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| **Camera Viewfinder & Overlay** | UI (Compose) | CameraX Analyzer | UI renders the bounding box overlay, driven by real-time coordinates emitted from the CameraX analyzer flow. |
| **Edge Detection (4-corners)** | Domain/Native | OpenCV | OpenCV (`findContours`, `approxPolyDP`) running in an background analyzer thread handles mathematically isolating the document quadrilateral. |
| **Clean PDF Generation** | Domain | `PdfDocument` | A dedicated `PdfGenerator` class maps text lines into `StaticLayout` blocks, handling pagination and drawing to the `PdfDocument` canvas. |
| **Bento Grid & State** | UI (Compose) | ViewModel | The ViewModel transforms the flat scan history into span-aware UI models (e.g., highlighting the most recent scan) which Compose renders via `LazyVerticalGrid`. |
| **Full-Text Search** | Repository/Data | Memory/Kotlin | Since local metadata is stored in JSON, the Repository loads it into memory and exposes a `Flow` that filters both title and text content on-the-fly. |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `androidx.compose.foundation:foundation` | (BOM) | Grid Layouts | `LazyVerticalGrid` and `GridItemSpan` are the native Compose tools for building Bento grids. |
| `OpenCV-Android` (e.g., QuickBird wrapper or native) | `4.x` | Edge Detection | Standard for finding non-axis-aligned quadrilaterals (4 corners). ML Kit Object Detection only finds axis-aligned bounding boxes. |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `android.text.StaticLayout` | Native | Text Wrapping | Essential for drawing multiline text onto a native Android `Canvas` (used in PDF generation). |
| `android.graphics.pdf.PdfDocument` | Native | PDF Creation | Existing phase 1 library, but will be utilized to generate multi-page text-only files. |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Custom OpenCV Analyzer | `play-services-mlkit-document-scanner` | ML Kit's Document Scanner is heavily optimized and requires no camera permissions, **BUT** it launches a closed Activity. It fundamentally violates D-06 (real-time custom overlay), D-07 (custom handles), and D-08 (fluid transitions). We MUST use OpenCV to meet user constraints. |

## Package Legitimacy Audit

> *Note: Android dependencies are resolved via Maven/Google repositories, not npm/PyPI. slopcheck is not directly applicable, so Android packages are verified via Google/Maven Central documentation.*

| Package | Registry | Age | Downloads | Source Repo | slopcheck | Disposition |
|---------|----------|-----|-----------|-------------|-----------|-------------|
| `com.quickbirdstudios:opencv` | Maven Central | 3 yrs | N/A | github.com/quickbirdstudios/opencv-android | [ASSUMED] | Approved (Standard community wrapper for OpenCV 4.x) |

*All packages above are tagged `[ASSUMED]` as slopcheck is unavailable for Android Maven dependencies. The planner must gate each install behind a `checkpoint:human-verify` task.*

## Architecture Patterns

### System Architecture Diagram

```
[CameraX Preview] ---> (ImageProxy Flow) ---> [OpenCV Edge Detector]
       ^                                              |
       |                                              v
       +--- (Real-time Quad Coordinates) <--- [StateFlow in ViewModel]
                                                      |
                                                      v
[Capture Button] ---> (High-Res Image) ---> [Perspective Transform (Warp)]
                                                      |
                                                      v
[Manual Crop UI] <--- (Draggable 4 Corners) ----------+
       |
       v
[ML Kit OCR Analyzer] ---> (Extracted Text)
       |
       v
[PdfGenerator] ---> (StaticLayout Pagination) ---> [Clean White PDF File]
```

### Pattern 1: Pagination via Canvas Translation
**What:** Using `StaticLayout` combined with `canvas.translate` and `canvas.clipRect` to draw multi-page text blocks without rebuilding the layout.
**When to use:** When rendering variable-length text to a `PdfDocument`.
**Example:**
```kotlin
// [CITED: Android Developer Docs]
val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, pageWidth).build()
val totalHeight = staticLayout.height
var currentY = 0

while (currentY < totalHeight) {
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    canvas.save()
    // Move the canvas UP by the amount we've already printed
    canvas.translate(0f, -currentY.toFloat())
    // Clip the canvas to only draw the current page's height
    canvas.clipRect(0, currentY, pageWidth, currentY + pageHeight)
    staticLayout.draw(canvas)
    canvas.restore()

    pdfDocument.finishPage(page)
    currentY += pageHeight
}
```

### Anti-Patterns to Avoid
- **Re-measuring text for every page:** `StaticLayout` measurement is computationally expensive. Build the layout once for the entire text block, and use canvas transformations to draw the correct "window" onto each PDF page.
- **Using ML Kit Document Scanner API for Custom UI:** Given the locked decisions (D-06, D-07, D-08), using the built-in intent-based scanner will result in an immediate UX requirement failure. 

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Perspective Warp | Manual Matrix Math | OpenCV `getPerspectiveTransform` and `warpPerspective` | Matrix transformations for non-axis-aligned quads are mathematically error-prone and slow in pure Kotlin. |
| Text Wrapping | Custom String Splitters | `android.text.StaticLayout` | Handles complex unicode, word boundaries, and font metrics perfectly natively. |

## Common Pitfalls

### Pitfall 1: Memory Leaks with OpenCV Mat
**What goes wrong:** The app crashes with OutOfMemoryError after a few scans.
**Why it happens:** OpenCV `Mat` objects are allocated in native memory (C++). The JVM garbage collector does not know their true size and won't aggressively clean them up.
**How to avoid:** Always call `.release()` on every `Mat` object in a `finally` block or use Kotlin `use` blocks if wrapping them.

### Pitfall 2: Coordinate Space Mismatch
**What goes wrong:** The edge detection bounding box drawn on the UI does not align with the physical document in the camera preview.
**Why it happens:** The CameraX `ImageProxy` (e.g., 640x480) has a different resolution and aspect ratio than the Compose UI viewport. 
**How to avoid:** You must map the OpenCV output coordinates through a transformation matrix that accounts for scaling (e.g., `ContentScale.Crop` equivalent) and rotation (sensor orientation vs UI orientation).

### Pitfall 3: PDF Font Scaling
**What goes wrong:** The generated clean text PDF has microscopic or massive text.
**Why it happens:** `PdfDocument` uses PostScript points (1/72 inch). 1 unit = 1 point. If you use Android SP/DP values designed for screen density, they will be wrong on the PDF.
**How to avoid:** Hardcode `TextPaint.textSize` in points (e.g., `12f` for standard 12pt font), completely ignoring screen density metrics.

## Code Examples

### Compose Bento Grid with Spans
```kotlin
// [CITED: Compose Foundation documentation]
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    modifier = Modifier.fillMaxSize()
) {
    itemsIndexed(scanList) { index, scan ->
        // The newest item (index 0) spans 2 columns, others span 1
        val spanCount = if (index == 0) 2 else 1
        GridItem(
            scan = scan,
            modifier = Modifier.animateItemPlacement()
        )
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Image + Invisible Text PDF | `StaticLayout` White Background | Phase 3 | Fixes the "black box" / illegible PDF rendering bug and creates professional documents. |
| Single Page Processing | Batch Scan + Gallery Review | Phase 3 | Allows users to scan a 10-page document in 10 seconds, instead of pausing for OCR after every single page. |

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | OpenCV wrapper (`com.quickbirdstudios:opencv`) will be used | Standard Stack | If the dependency is broken, we may need to manually compile OpenCV C++ via NDK, significantly increasing implementation time. |
| A2 | Custom UI requirements block ML Kit Document Scanner API | Summary | If D-06/D-07 can be dropped, we could use the built-in ML Kit API and save days of OpenCV integration effort. (Planner must confirm this constraint is rigid). |

## Open Questions (RESOLVED)

1. **OpenCV Binary Size vs Project Constraints**
   - **RESOLVED:** The inclusion of native OpenCV increases the APK size by roughly 10-20MB. While this is acceptable for the technical requirement, it is recommended to implement Proguard/R8 and App Bundles prior to Play Store release to offset the size penalty. Decision: Accept the size increase as it is necessary for the advanced scanning requirements, and rely on standard Android build optimizations.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Android SDK | Core App | ✓ | API 35 | — |

*Step 2.6: Executed (Standard Android build environment; no external CLI dependencies required).*

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 / Espresso |
| Config file | app/build.gradle.kts |
| Quick run command | `./gradlew testDebugUnitTest` |
| Full suite command | `./gradlew connectedAndroidTest` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| REQ-1.2.2 | Multi-page PDF layout | unit | `./gradlew testDebugUnitTest --tests "com.t2h.ocr.PdfGeneratorTest"` | ❌ Wave 0 |
| REQ-1.4.1 | Bento Grid home layout | e2e/UI | `./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.t2h.ocr.HomeScreenTest` | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** `./gradlew testDebugUnitTest`
- **Per wave merge:** `./gradlew connectedAndroidTest`
- **Phase gate:** Full suite green before `/gsd:verify-work`

### Wave 0 Gaps
- [ ] `app/src/test/java/com/t2h/ocr/domain/ocr/PdfGeneratorTest.kt` — covers REQ-1.2.2
- [ ] `app/src/androidTest/java/com/t2h/ocr/ui/home/HomeScreenTest.kt` — covers REQ-1.4.1

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | (Handled in Phase 2) |
| V3 Session Management | no | (Handled in Phase 2) |
| V4 Access Control | no | (Handled in Phase 2) |
| V5 Input Validation | yes | Native Kotlin type safety |
| V6 Cryptography | no | — |

### Known Threat Patterns for Android SDK

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Unbounded PDF Generation | Denial of Service | Impose maximum page counts or file size limits before generation. |

## Sources

### Primary (HIGH confidence)
- [Android Developer Docs] - Canvas and PdfDocument
- [Compose Developer Docs] - LazyVerticalGrid layout APIs

### Secondary (MEDIUM confidence)
- WebSearch verified with official source (OpenCV Android vs ML Kit limitations)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - OpenCV is the unquestioned standard for native custom quad edge detection.
- Architecture: HIGH - StaticLayout via translation is standard for Android native pagination.
- Pitfalls: HIGH - Common issues well documented in the Android graphics and JNI space.

**Research date:** 2024-05-22
**Valid until:** 30 days
