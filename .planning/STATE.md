# Project State: Android OCR

## Current Phase: Initialized
## Status: Ready for Phase 1

## Recent Milestones
- [x] Initial project mapping completed.
- [x] Project goals and tech stack defined.
- [x] Technical research on ML Kit, Firebase, and PDF generation completed.
- [x] Requirements and Roadmap established.

## Active Tasks
- [ ] Task 1.1: Initialize project structure and set up dependencies.

## Key Decisions
- **OCR Engine:** Google ML Kit (On-device) for speed and cost-effectiveness.
- **Sync Strategy:** Hybrid (Firestore for metadata, Storage for files) with `WorkManager`.
- **PDF API:** Native Android `PdfDocument` for a lightweight, searchable solution.
- **UI Direction:** Custom Brand Style using Material 3 foundation.

## Open Questions / Risks
- **PDF Size:** Need to monitor the size of generated PDFs for storage efficiency.
- **OOM Risks:** Native `PdfDocument` builds in-memory; multi-page documents need testing.
- **Firebase Costs:** Monitor Firestore and Storage usage as user base scales.
