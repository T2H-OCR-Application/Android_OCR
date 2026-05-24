# Roadmap: Android OCR

## Phase 1: Project Foundation & Core OCR (MVP)
**Goal:** Establish the project foundation, implement core OCR scanning with CameraX and ML Kit, and enable basic searchable PDF generation with local history.

**Requirements:** REQ-1.1.1, REQ-1.1.2, REQ-1.1.4, REQ-1.1.5, REQ-1.2.1, REQ-1.3.5, REQ-1.4.2, REQ-1.4.3, REQ-1.4.4

**Plans:** 4 plans
- [x] 01-01-PLAN.md — Foundation & Branding (Setup theme, dependencies, and permission rationale)
- [x] 01-02-PLAN.md — Real-time OCR Scanner (CameraX + ML Kit integration with visual feedback)
- [x] 01-03-PLAN.md — PDF Generation & Persistence (Searchable PDF logic and local JSON storage)
- [x] 01-04-PLAN.md — Final Verification & UX Polish (Automated tests and theme refinement)

## Phase 2: Firebase Integration & Synchronization
**Goal:** Integrate Firebase Authentication, Cloud Firestore for metadata sync, Cloud Storage for PDF files, and use WorkManager for background sync.

**Requirements:** REQ-1.3.1, REQ-1.3.2, REQ-1.3.3, REQ-1.3.4

**Plans:** 4 plans

**Wave 1**
- [x] 02-01-PLAN.md — Firebase Auth & Anonymous Identity (Silent sign-in on launch)

**Wave 2 (blocked on Wave 1 completion)**
- [x] 02-02-PLAN.md — Profile Screen & Account Linking (Google Sign-In integration)
- [x] 02-03-PLAN.md — WorkManager & File-First Sync Pipeline (Automatic background uploads)

**Wave 3 (blocked on Wave 2 completion)**
- [x] 02-04-PLAN.md — Data Integrity & Rules (Security Rules and conflict resolution)

**Cross-cutting constraints:**
- "File-First" sync strategy (Storage then Firestore).
- Anonymous-to-Permanent account linking (D-01).
- Visible background progress via notifications (D-05).

## Phase 3: Advanced Features & UX Refinement
**Goal:** Transition the OCR MVP into a professional-grade scanner with custom edge detection, bento-grid UI, and high-fidelity PDF generation.

**Requirements:** REQ-1.1.2, REQ-1.4.1, REQ-1.4.2, REQ-1.2.2, REQ-1.2.3

**Plans:** 4 plans
- [x] 03-01-PLAN.md — OpenCV Edge Detection (Custom CameraX analyzer for document boundary isolation)
- [x] 03-02-PLAN.md — Bento Grid & Deep Search (Dynamic home screen with full-text search and UI tests)
- [x] 03-03-PLAN.md — Real-time Overlay & Manual Crop (Live guidance and draggable corner refinement)
- [x] 03-04-PLAN.md — Batch Scanning & Clean PDF Rewrite (Multi-page processing and high-fidelity text-only PDFs)


## Phase 4: Polish, Testing & Deployment
**Goal:** Production hardening, testing coverage, performance optimization, and release readiness.

**Requirements:** NREQ-2.1.1, NREQ-2.1.2, NREQ-2.2.1

**Plans:** 4 plans
- [x] 04-01-PLAN.md — Testing Suite & Quality Gates (JUnit, Roborazzi setup, E2E pipeline)
- [x] 04-02-PLAN.md — Performance & Memory Hardening (OpenCV Mat management, latency optimization)
- [x] 04-03-PLAN.md — Resilience & Observability (WorkManager backoff, high-viz errors, custom Analytics)
- [x] 04-04-PLAN.md — Release Readiness & Localization (Strings extraction, R8/ProGuard, Play Store assets)
