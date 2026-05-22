# Project State: Android OCR

## Current Phase: Phase 3: Advanced Features & UX Refinement
## Status: Ready to execute (4 plans created).

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
- [x] Phase 3 Planned: 4 plans verified for advanced features and UX polish.

## Key Decisions
- **OCR Engine:** Google ML Kit (On-device) for speed and cost-effectiveness.
- **Sync Strategy:** Direct Google Drive API upload for PDFs to avoid Firebase billing constraints, with metadata in Firestore.
- **PDF API:** Native Android `PdfDocument` for a lightweight, searchable solution.
- **UI Direction:** Custom Brand Style using Material 3 foundation.
- **Coordinate Mapping:** Used `MlKitAnalyzer` to automatically map OCR bounding boxes to UI coordinates.
- **Searchable PDFs:** Layering transparent text over images to maintain visual fidelity while enabling search.

## Open Questions / Risks
- **PDF Generation Bug:** The current `PdfGenerator` outputs raw images without text overlay. This MUST be fixed in Phase 3.
- **PDF Size:** Need to monitor the size of generated PDFs for storage efficiency.
- **OOM Risks:** Native `PdfDocument` builds in-memory; multi-page documents need testing.
