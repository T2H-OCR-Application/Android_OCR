# Phase 4: Polish, Testing & Deployment - Context

**Gathered:** 2026-05-23
**Status:** Ready for planning

<domain>
## Phase Boundary

Production Hardening & Release Readiness. This final phase transforms the feature-complete application into a stable, high-performance, and resilient product. It focuses on exhaustive testing coverage (unit, integration, and visual), rigorous performance optimization (memory and latency), robust error handling and observability, and preparing the app for internationalization and deployment.

</domain>

<decisions>
## Implementation Decisions

### Testing & Quality Assurance
- **D-01: Triple-Tier Testing.** 
    - **Logic:** Exhaustive unit tests for `PdfGenerator`, `SyncWorker`, and `AuthRepository`.
    - **E2E:** Compose UI tests covering the "Scan → Crop → Review → Sync" pipeline.
    - **Visual:** Snapshot testing for Bento Grid, Camera Overlays, and custom brand components to prevent regression.
- **D-02: Quality Gates.** Fail builds if test coverage for core business logic drops below 80% or if linting/detekt issues are found.

### Performance & Resource Management
- **D-03: Memory Hardening.** Implement strict monitoring of the `Mat` lifecycle in OpenCV and bitmap allocations in the scanner to prevent OOM errors.
- **D-04: Latency Polish.** Optimize the OCR/Analysis pipeline to ensure the green boundary overlay tracks at >30fps on mid-range devices.
- **D-05: Resource Toggles.** Provide user settings for sync behavior (e.g., "Upload only on Wi-Fi") and storage management (e.g., "Clear local cache after sync").

### Resilience & Observability
- **D-06: Robust Sync.** Implement exponential backoff for `WorkManager` sync failures and a manual "Retry Sync" trigger in the UI.
- **D-07: High-Viz Error States.** Use dedicated full-screen error components for fatal failures (e.g., Camera hardware failure, Auth lockdown) with clear recovery actions.
- **D-08: Custom Analytics.** Track OCR success rates, processing latency, and sync duration via Firebase Analytics to identify real-world bottlenecks.

### Deployment & Global Readiness
- **D-09: Localization-Ready.** Extract all remaining hardcoded strings into `strings.xml`. Structure resources to support multiple languages (starting with EN/VI/ES as candidates).
- **D-10: Release Optimization.** Enable R8/ProGuard obfuscation and resource shrinking. Prepare Play Store assets (icons, feature graphics, signing).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Foundation
- `.planning/ROADMAP.md` — Defines phase goals and high-level tasks.
- `.planning/REQUIREMENTS.md` — Final check against NREQs (Performance, Reliability).
- `.planning/phases/03-advanced/03-CONTEXT.md` — Details on OpenCV and Bento UI patterns.

### Configuration
- `app/build.gradle.kts` — Update for testing libraries and R8 configuration.
- `app/src/main/res/values/strings.xml` — Central source for localization.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `ScannerViewModel`: The primary state machine for performance and latency tuning.
- `SyncWorker`: The target for resilience and auto-retry logic.

### Established Patterns
- **Standardized UI Components:** Use the existing `ShimmerLoader` and Material 3 theme for new error/loading states.

### Integration Points
- **Firebase:** Connect Crashlytics and custom Analytics events.
- **Settings Screen:** New destination for resource toggles.

</code_context>

<deferred>
## Deferred Ideas
- **Advanced PDF Editing:** (e.g., merging/splitting) deferred to post-1.0.
- **Multi-user Collaboration:** Deferred to future roadmap.
</deferred>

---

*Phase: 4-Polish, Testing & Deployment*
*Context gathered: 2026-05-23*
