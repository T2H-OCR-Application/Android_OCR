# Phase 3 Validation Architecture

## Overview
This document defines the validation strategy for Phase 3: Advanced Features & UX Refinement. It ensures that the multi-page scanning, custom OpenCV edge detection, clean text PDF rendering, Bento Grid UI, and UX polish meet the project requirements and user expectations.

## 1. Feature Validation

### 1.1 Multi-page Scanning (Batch Flow)
- **Goal:** Verify that users can sequentially scan multiple pages and review them in a gallery before finalization.
- **Method:**
  - Mock CameraX input to feed multiple images.
  - Assert that the `ScannerViewModel` maintains an ordered list of captured images.
  - Verify that the `GalleryScreen` displays the correct number of thumbnails in the correct order.

### 1.2 Document Edge Detection (OpenCV)
- **Goal:** Verify that the custom analyzer correctly identifies document quadrilaterals in varied lighting.
- **Method:**
  - Provide reference images (documents on contrasting backgrounds) to the `DocumentAnalyzer`.
  - Assert that the returned 4-point coordinates fall within an acceptable margin of error compared to known ground truths.
  - Verify no memory leaks occur when processing a high volume of `Mat` objects (stress test).

### 1.3 Clean Text PDF Rendering
- **Goal:** Verify that the output PDF contains selectable, clean text on a white background instead of just a raw image.
- **Method:**
  - Parse the generated PDF using a basic PDF text extractor in unit tests.
  - Assert that the extracted string matches the input `VisionText`.
  - Verify that the file size is significantly smaller than the image-based PDFs from Phase 1.
  - Check for correct pagination if the text exceeds a single page.

### 1.4 Bento Grid Home Screen & Deep Search
- **Goal:** Verify that the dynamic grid displays correctly and search queries match both titles and content.
- **Method:**
  - Pre-populate `JsonStorage` with mock scans containing specific keywords in the OCR text.
  - Dispatch a search query from the `HomeViewModel`.
  - Assert that the filtered results include scans where the keyword exists *only* in the content, not just the title.

### 1.5 UX Polish & Brand Identity
- **Goal:** Verify animations, haptics, and thematic constraints (D-06 to D-10).
- **Method:**
  - Use Compose UI testing rules to assert the existence of the `ShimmerLoader` during processing states.
  - Manually verify (UAT) the camera shutter animation, scale transitions on crop handles, and Deep Blue color usage.

## 2. Integration Validation
- **End-to-End Flow:** Run an Espresso UI test simulating a full capture -> crop -> review -> save flow.
- **Sync Compatibility:** Verify that the output of the new PDF generator is still correctly picked up by the Phase 2 Google Drive `SyncWorker`.