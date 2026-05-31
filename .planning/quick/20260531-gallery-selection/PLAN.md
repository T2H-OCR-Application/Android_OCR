---
phase: quick/20260531-gallery-selection
plan: 01
type: execute
wave: 1
depends_on: []
files_modified:
  - app/src/main/java/com/t2h/ocr/domain/ocr/DetectionUtils.kt
  - app/src/main/java/com/t2h/ocr/domain/ocr/DocumentAnalyzer.kt
  - app/src/main/java/com/t2h/ocr/domain/ocr/ImageProcessor.kt
  - app/src/main/java/com/t2h/ocr/ui/scanner/ScannerScreen.kt
  - app/src/main/java/com/t2h/ocr/ui/scanner/ScannerViewModel.kt
  - app/src/main/java/com/t2h/ocr/MainActivity.kt
  - app/src/main/java/com/t2h/ocr/ui/home/HomeScreen.kt
autonomous: true
requirements:
  - REQ-1.1.3

must_haves:
  truths:
    - "Scanner screen shows an 'Import from Gallery' button in the bottom bar."
    - "Clicking Import opens the Android Photo Picker (PickVisualMedia)."
    - "Selected image is copied to app cache to ensure persistence and compatibility."
    - "Document corners are automatically detected on the imported image."
    - "User is navigated to CropScreen with the imported image and detected corners."
    - "The full OCR pipeline (Crop -> OCR -> Results) works for gallery-imported images."
  artifacts:
    - path: "app/src/main/java/com/t2h/ocr/domain/ocr/DetectionUtils.kt"
      provides: "Reusable document detection logic extracted from DocumentAnalyzer"
    - path: "app/src/main/java/com/t2h/ocr/ui/scanner/ScannerScreen.kt"
      contains: "rememberLauncherForActivityResult(PickVisualMedia())"
  key_links:
    - from: "ScannerScreen"
      to: "Photo Picker"
      via: "ActivityResultContracts.PickVisualMedia"
    - from: "ScannerScreen"
      to: "CropScreen"
      via: "MainActivity navigation with detected corners"
---

<objective>
Implement REQ-1.1.3 to allow users to select images from their device gallery for OCR processing. 
This involves integrating the modern Android Photo Picker, copying the selected media to cache, 
running edge detection, and funneling the result into the existing document scanning pipeline.

Purpose: Increase app utility by supporting legacy documents or photos taken outside the app.
Output: Working gallery import flow in Scanner and Home screens.
</objective>

<execution_context>
@$HOME/.gemini/get-shit-done/workflows/execute-plan.md
</execution_context>

<context>
@.planning/ROADMAP.md
@.planning/STATE.md
@.planning/phases/01-foundation/01-05-RESEARCH.md
@app/src/main/java/com/t2h/ocr/ui/scanner/ScannerScreen.kt
@app/src/main/java/com/t2h/ocr/domain/ocr/DocumentAnalyzer.kt
@app/src/main/java/com/t2h/ocr/MainActivity.kt
</context>

<tasks>

<task type="auto">
  <name>Task 1: Extract Detection Logic & Add Uri Utilities</name>
  <files>
    app/src/main/java/com/t2h/ocr/domain/ocr/DetectionUtils.kt,
    app/src/main/java/com/t2h/ocr/domain/ocr/DocumentAnalyzer.kt,
    app/src/main/java/com/t2h/ocr/domain/ocr/ImageProcessor.kt
  </files>
  <action>
    1. Create `DetectionUtils.kt` and move the `performDetection` and `sortPoints` logic from `DocumentAnalyzer.kt` into it. Ensure it can take a `Mat` and return normalized `List<Point>`.
    2. Update `DocumentAnalyzer.kt` to delegate to `DetectionUtils`.
    3. In `ImageProcessor.kt`, add `copyUriToCache(context, uri)` which copies a Gallery Uri to a unique temp file in `context.cacheDir`.
    4. In `ImageProcessor.kt`, add `detectCornersInFile(path)` which loads a file via `Imgcodecs.imread` and uses `DetectionUtils` to find corners.
  </action>
  <verify>
    <automated>./gradlew test --tests DocumentAnalyzerTest</automated>
  </verify>
  <done>Detection logic is decoupled from CameraX analyzer and Uri utilities are available in ImageProcessor.</done>
</task>

<task type="auto">
  <name>Task 2: Implement Photo Picker in ScannerScreen</name>
  <files>
    app/src/main/java/com/t2h/ocr/ui/scanner/ScannerScreen.kt,
    app/src/main/java/com/t2h/ocr/MainActivity.kt
  </files>
  <action>
    1. In `ScannerScreen.kt`, define `val pickerLauncher = rememberLauncherForActivityResult(PickVisualMedia())`.
    2. In the `onResult` callback, launch a coroutine to:
       - Copy Uri to cache via `ImageProcessor.copyUriToCache`.
       - Run detection via `ImageProcessor.detectCornersInFile`.
       - Call `onDocumentCaptured(path, points)`.
    3. Add a new `IconButton` (using `Icons.Default.PhotoLibrary` or a similar 'Import' icon) to the left of the capture button in the bottom bar.
    4. Update `MainActivity.kt` navigation: ensure `onNavigateToSection("Ảnh")` triggers the same `PickVisualMedia` flow (either by navigating to Scanner and triggering it, or handling it directly). *Preference: Add "Import" button to HomeScreen "Ảnh" category that opens the picker and then navigates straight to Crop.*
  </action>
  <verify>
    <automated>Check ScannerScreen bottom bar for new button; verify Picker launches on click.</automated>
  </verify>
  <done>Photo Picker is integrated into the ScannerScreen UI and linked to the capture flow.</done>
</task>

<task type="checkpoint:human-verify">
  <name>Task 3: Verify End-to-End Gallery Flow</name>
  <files>app/src/main/java/com/t2h/ocr/MainActivity.kt</files>
  <action>
    1. Run the app on a device/emulator with images in the gallery.
    2. Go to Scanner screen -> Click Import button -> Select an image.
    3. Verify navigation to CropScreen with the selected image.
    4. Verify that corners are automatically suggested (if document visible).
    5. Confirm crop and verify OCR results are generated correctly.
    6. Repeat from Home screen "Ảnh" button.
  </action>
  <verify>Manual verification of the UI flow and data persistence.</verify>
  <done>Gallery selection is fully functional and matches the camera capture UX.</done>
</task>

</tasks>

<threat_model>
## Trust Boundaries
| Boundary | Description |
|----------|-------------|
| System Gallery → App | External Uris from the system Photo Picker enter the app. |

## STRIDE Threat Register
| Threat ID | Category | Component | Disposition | Mitigation Plan |
|-----------|----------|-----------|-------------|-----------------|
| T-GALLERY-01 | Tampering | Uri Handling | mitigate | Copy Uri to app-private cache directory immediately using ContentResolver; do not trust file extensions. |
| T-GALLERY-02 | Information Disclosure | Image Cache | mitigate | Ensure temp files in cacheDir use unique UUIDs and are deleted after OCR processing or on cancel. |
| T-GALLERY-03 | Denial of Service | Image Decoding | mitigate | Load images with appropriate downscaling if resolution is extremely high (pitfall identified in research). |
</threat_model>

<verification>
- [ ] User can select image via PickVisualMedia.
- [ ] Image is successfully copied to cache.
- [ ] Edge detection runs on the imported image.
- [ ] Flow continues to CropScreen and then ResultsScreen.
</verification>

<success_criteria>
REQ-1.1.3 is satisfied: Users can import and process images from the device gallery.
</success_criteria>

<output>
Update `.planning/STATE.md` to mark REQ-1.1.3 as complete and record the implementation details.
</output>
