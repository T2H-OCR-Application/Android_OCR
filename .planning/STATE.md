# Project State: Android OCR

## Current Phase: Phase 1: Project Foundation & Core OCR (MVP)
## Status: Wave 3 complete, performing final verification (Wave 4)

## Recent Milestones
- [x] Initial project mapping completed.
- [x] Project goals and tech stack defined.
- [x] Technical research on ML Kit, Firebase, and PDF generation completed.
- [x] Wave 1: Foundation & Branding (Deep Blue theme, permissions UI).
- [x] Wave 2: Real-time OCR Scanner (CameraX + ML Kit integration).
- [x] Wave 3: Searchable PDF & Local Persistence (PdfDocument + JsonStorage).

## Active Tasks
- [ ] Task 1.4: Final verification, UI polish, and performance audit.

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
