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
- [ ] **Task 2.1:** Integrate Firebase Authentication (Google/Email).
- [ ] **Task 2.2:** Implement Cloud Firestore for scan metadata and history.
- [ ] **Task 2.3:** Integrate Firebase Cloud Storage for PDF file hosting.
- [ ] **Task 2.4:** Implement `WorkManager` for robust background synchronization.
- [ ] **Task 2.5:** Configure Firebase Security Rules for data isolation.

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
