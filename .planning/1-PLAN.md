# Phase 1 Execution Plan: Project Foundation & Core OCR (MVP)

This phase is decomposed into 4 execution plans to be executed sequentially or in parallel where waves allow.

## Wave 1
- **[01-01-PLAN.md](phases/01-foundation/01-01-PLAN.md): Foundation & Branding**
  - Setup Material 3 Theme with "Deep Blue" identity.
  - Implement core dependencies.
  - Implement Permission Rationale screen.

## Wave 2
- **[01-02-PLAN.md](phases/01-foundation/01-02-PLAN.md): Real-time OCR Scanner**
  - CameraX integration with `LifecycleCameraController`.
  - ML Kit Text Recognition integration.
  - Real-time bounding box feedback with Coordinate Transform.

## Wave 3
- **[01-03-PLAN.md](phases/01-foundation/01-03-PLAN.md): PDF Generation & Persistence**
  - Searchable PDF generation with transparent text layer.
  - Local JSON metadata storage for scan history.
  - Results Screen for text editing and saving.

## Wave 4
- **[01-04-PLAN.md](phases/01-foundation/01-04-PLAN.md): Final Verification & UX Polish**
  - Automated UI smoke tests for the scan flow.
  - UX refinement and dark/light mode audit.
  - Performance and memory leak checks.

## Next Steps
Execute each plan using `/gsd:execute-phase 01-foundation`.
