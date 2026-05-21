# Project State: Android OCR

## Current Phase: Phase 2: Firebase Integration & Synchronization
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

## Key Decisions
- **OCR Engine:** Google ML Kit (On-device) for speed and cost-effectiveness.
- **Sync Strategy:** Hybrid (Firestore for metadata, Storage for files) with `WorkManager`.
- **PDF API:** Native Android `PdfDocument` for a lightweight, searchable solution.
- **UI Direction:** Custom Brand Style using Material 3 foundation.
- **Coordinate Mapping:** Used `MlKitAnalyzer` to automatically map OCR bounding boxes to UI coordinates.
- **Searchable PDFs:** Layering transparent text over images to maintain visual fidelity while enabling search.

## Open Questions / Risks
- **PDF Size:** Need to monitor the size of generated PDFs for storage efficiency.
- **OOM Risks:** Native `PdfDocument` builds in-memory; multi-page documents need testing.
- **Firebase Costs:** Monitor Firestore and Storage usage as user base scales.
