# Project State: Android OCR

## Current Phase: Phase 4: Polish, Testing & Deployment
## Status: Phase 4 Complete. All milestones achieved.

## Recent Milestones
- [x] Phase 4, Plan 01: Testing Suite & Quality Gates completed.
- [x] Phase 4, Plan 02: Performance & Memory Hardening completed.
- [x] Phase 4, Plan 03: Resilience & Observability completed.
- [x] Phase 4, Plan 04: Release Readiness & Localization completed.

## Key Decisions
- **OCR Engine:** Google ML Kit (On-device) for speed and cost-effectiveness.
- **Sync Strategy:** Direct Google Drive API upload for PDFs to avoid Firebase billing constraints, with metadata in Firestore.
- **PDF API:** Native Android `PdfDocument` for a lightweight, searchable solution.
- **UI Direction:** Custom Brand Style using Material 3 foundation.
- **Coordinate Mapping:** Used `MlKitAnalyzer` to automatically map OCR bounding boxes to UI coordinates.
- **Searchable PDFs:** Layering transparent text over images to maintain visual fidelity while enabling search.
- **Post-Crop OCR:** OCR is now performed after manual crop confirmation to ensure high accuracy and only process the intended document area (D-03).
- **Centralized Recognition:** Moved `TextRecognizer` lifecycle management to `MainActivity` for better resource control during the scan-crop-result flow.
- **StaticLayout Rendering:** Adopted `StaticLayout` for PDF generation to support clean text wrapping and automatic pagination for multi-page documents (D-02).

## Open Questions / Risks
- **PDF Size:** Need to monitor the size of generated PDFs for storage efficiency.
- **OOM Risks:** Native `PdfDocument` builds in-memory; multi-page documents need testing.
- **Edge Case OCR:** Need to verify performance on low-contrast or distorted documents during Phase 4 testing.
