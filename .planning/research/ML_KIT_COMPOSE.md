# Best Practices: Google ML Kit (OCR) + Jetpack Compose

This document outlines the specific implementation details for real-time document scanning using ML Kit and Jetpack Compose.

## 1. Camera Performance & Efficiency

To achieve smooth real-time scanning:

### Backpressure Strategy
Always use `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST`. This prevents a backlog of images if the OCR engine takes longer to process a frame than the camera takes to produce one.

### Resolution Selection
Target **1280x720 (720p)**. High resolutions (like 4K) significantly increase processing time without a proportional gain in OCR accuracy for most documents.

### Throttling OCR
Running OCR on every frame (30+ times per second) is unnecessary and drains battery. Aim for an update frequency of **3-5 times per second** (every 200-300ms).

```kotlin
// Example of throttling in an analyzer
private var lastAnalysisTimestamp = 0L

override fun analyze(imageProxy: ImageProxy) {
    val currentTimestamp = System.currentTimeMillis()
    if (currentTimestamp - lastAnalysisTimestamp >= 300L) {
        processImage(imageProxy)
        lastAnalysisTimestamp = currentTimestamp
    } else {
        imageProxy.close()
    }
}
```

### Resource Management
*   **Close Images:** Ensure `imageProxy.close()` is called in the `onCompleteListener` of the ML Kit task.
*   **Reuse Clients:** Initialize `TextRecognition.getClient()` once and reuse it across frames.

## 2. UI Feedback: Bounding Boxes

Drawing bounding boxes accurately requires mapping from the camera sensor coordinate system to the Compose UI coordinate system.

### Coordinate Transformation
Use `MlKitAnalyzer` from `androidx.camera:camera-mlkit-vision`. It provides built-in support for `COORDINATE_SYSTEM_VIEW_REFERENCED`, which automatically scales and rotates the detection coordinates to match your `PreviewView`.

```kotlin
val cameraController = LifecycleCameraController(context)
cameraController.setImageAnalysisAnalyzer(
    executor,
    MlKitAnalyzer(
        listOf(textRecognizer),
        COORDINATE_SYSTEM_VIEW_REFERENCED,
        executor
    ) { result ->
        val visionText = result.getValue(textRecognizer)
        // visionText.textBlocks[0].boundingBox is now mapped to your view!
    }
)
```

### Drawing in Compose
Use `Canvas` to draw overlays. This is more efficient than creating individual `Box` or `Border` components for each text block, which would cause excessive recompositions.

```kotlin
Canvas(modifier = Modifier.fillMaxSize()) {
    detectedTextBlocks.forEach { block ->
        block.boundingBox?.let { rect ->
            drawRect(
                color = Color.Green,
                topLeft = Offset(rect.left.toFloat(), rect.top.toFloat()),
                size = Size(rect.width().toFloat(), rect.height().toFloat()),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
```

## 3. Error Handling

### ML Kit Specific Errors
Handle `MlKitException` in your `onFailureListener`:

*   **Error 14 (UNAVAILABLE):** The OCR model is not yet downloaded. 
    *   *Solution:* Inform the user that the scanner is "initializing" and ensure `meta-data` is in `AndroidManifest.xml`.
*   **Error 13 (INTERNAL):** Often means the input image format or rotation is invalid.
    *   *Solution:* Verify `InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)`.

### Camera Permissions
Always check for `Manifest.permission.CAMERA` before attempting to bind the camera lifecycle. Use Compose's `rememberLauncherForActivityResult` with `ActivityResultContracts.RequestPermission()`.

### Low-Light & Focus
*   Monitor `CameraInfo.getSensorRotationDegrees()` and lighting conditions if accuracy is low.
*   Enable **Auto-focus** via `cameraController.enableTorch(true)` if needed for dark environments.

## 4. Required Configuration

### AndroidManifest.xml
Add this to ensure the OCR model is downloaded when the app is installed:

```xml
<meta-data
    android:name="com.google.mlkit.vision.DEPENDENCIES"
    android:value="ocr" />
```

### Dependencies
```kotlin
implementation("androidx.camera:camera-mlkit-vision:1.3.0")
implementation("com.google.mlkit:text-recognition:16.0.0")
```
