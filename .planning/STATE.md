# Project State: Android OCR

## Current Phase: Phase 4: Polish, Testing & Deployment
## Status: Planned

## Recent Milestones
- [x] Initial project mapping completed.
- [x] Project goals and tech stack defined.
- [x] Technical research on ML Kit, Firebase, and PDF generation completed.
- [x] Wave 1: Foundation & Branding (Deep Blue theme, permissions UI).
- [x] Wave 2: Real-time OCR Scanner (CameraX + ML Kit integration).
- [x] Wave 3: Searchable PDF & Local Persistence (PdfDocument + JsonStorage).
- [x] Wave 4: Final verification, UI polish, and performance audit.
- [x] Phase 1 UAT: Verified all core features with the user.
- [x] Phase 2 Context: Implementation decisions for Auth and Sync captured.
- [x] Phase 2 Planned: 4 plans verified and ready.
- [x] Phase 2 UAT & Execution: Google Drive sync implemented and verified.
- [x] Phase 3 Execution: Advanced Features (OpenCV, Bento Grid, Batch Scanning, PDF Rewrite) completed and verified.
- [x] Phase 4 Planned: 4 plans for testing, performance, resilience, and release readiness.

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
