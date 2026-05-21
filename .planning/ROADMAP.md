# Roadmap: Android OCR

## Phase 1: Project Foundation & Core OCR (MVP)
**Goal:** Establish the project foundation, implement core OCR scanning with CameraX and ML Kit, and enable basic searchable PDF generation with local history.

**Requirements:** REQ-1.1.1, REQ-1.1.2, REQ-1.1.4, REQ-1.1.5, REQ-1.2.1, REQ-1.3.5, REQ-1.4.2, REQ-1.4.3, REQ-1.4.4

**Plans:** 4 plans
- [x] 01-01-PLAN.md — Foundation & Branding (Setup theme, dependencies, and permission rationale)
- [x] 01-02-PLAN.md — Real-time OCR Scanner (CameraX + ML Kit integration with visual feedback)
- [x] 01-03-PLAN.md — PDF Generation & Persistence (Searchable PDF logic and local JSON storage)
- [ ] 01-04-PLAN.md — Final Verification & UX Polish (Automated tests and theme refinement)

## Phase 2: Firebase Integration & Synchronization
**Goal:** Integrate Firebase Authentication, Cloud Firestore for metadata sync, Cloud Storage for PDF files, and use WorkManager for background sync.

**Requirements:** REQ-1.3.1, REQ-1.3.2, REQ-1.3.3, REQ-1.3.4

**Plans:** 4 plans

**Wave 1**
- [ ] 02-01-PLAN.md — Firebase Auth & Anonymous Identity (Silent sign-in on launch)

**Wave 2 (blocked on Wave 1 completion)**
- [ ] 02-02-PLAN.md — Profile Screen & Account Linking (Google Sign-In integration)
- [ ] 02-03-PLAN.md — WorkManager & File-First Sync Pipeline (Automatic background uploads)

**Wave 3 (blocked on Wave 2 completion)**
- [ ] 02-04-PLAN.md — Data Integrity & Rules (Security Rules and conflict resolution)

**Cross-cutting constraints:**
- "File-First" sync strategy (Storage then Firestore).
- Anonymous-to-Permanent account linking (D-01).
- Visible background progress via notifications (D-05).

## Phase 3: Advanced Features & UX Refinement
- [ ] **Task 3.1:** Implement Multi-page scan and PDF generation.
- [ ] **Task 3.2:** Develop the "Bento Grid" home screen for document management.
- [ ] **Task 3.3:** Add search and filter functionality for scan history.
- [ ] **Task 3.4:** Implement advanced scanner features (Auto-crop, perspective correction).
- [ ] **Task 3.5:** Refine UI with custom brand identity and animations.

## Phase 4: Polish, Testing & Deployment
- [ ] **Task 4.1:** Conduct thorough unit and instrumentation testing.
- [ ] **Task 4.2:** Perform performance optimization (memory leaks, OCR latency).
- [ ] **Task 4.3:** Implement comprehensive error handling and analytics.
- [ ] **Task 4.4:** Prepare for Play Store release (App icon, signing, metadata).
