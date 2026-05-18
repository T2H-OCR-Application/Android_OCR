# Phase 1 Context: Android OCR

## 1. Phase Overview
**Goal:** Establish the project foundation, implement core OCR scanning with CameraX and ML Kit, and enable basic searchable PDF generation with local history.

## 2. Implementation Decisions

### 2.1 Visual Identity
- **Primary Color:** Deep Blue.
- **Foundation:** Material 3 with custom brand elements.
- **UI Patterns:** Prepare for "Bento Grid" layout in subsequent phases.

### 2.2 Local Storage Strategy
- **Mechanism:** File-based storage (JSON for metadata, local file system for PDFs).
- **Rationale:** Simplicity for Phase 1 MVP; allows for rapid iteration before introducing full Room database or Firebase sync.

### 2.3 PDF Export Formatting (Interim)
- **Status:** Basic searchable text output.
- **Assumed Defaults:** A4 page size, standard margins (approx. 20dp), system default font. (Refinement deferred to Phase 3).

### 2.4 Permission UX Flow
- **Pattern:** Pre-permission Rationale Screens.
- **Workflow:** Display a clear explanation of *why* the permission is needed before triggering the system dialog (for Camera and Storage).

## 3. Technology Locks
- **Camera:** CameraX with `ImageAnalysis` for ML Kit.
- **OCR:** Google ML Kit (Text Recognition v2).
- **PDF:** Native Android `PdfDocument` API.
- **UI:** Jetpack Compose (Material 3).

## 4. Scope Boundaries
- **In-Scope:** Camera preview, real-time OCR feedback, text editing, single-page PDF generation, local JSON history.
- **Deferred:** Multi-page PDFs, Firebase integration, advanced image processing (auto-crop), cloud sync.
