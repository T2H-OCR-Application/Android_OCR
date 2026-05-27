# Phase 2: Firebase Integration & Synchronization - Context

**Gathered:** 2026-05-21
**Status:** Ready for planning

<domain>
## Phase Boundary

Cloud Synchronization & User Data Management. This phase connects the local MVP to a cloud backend, enabling cross-device access and secure backup. It delivers user authentication, Firestore metadata synchronization, and Cloud Storage for PDF files, orchestrated by WorkManager for robust background processing.

</domain>

<decisions>
## Implementation Decisions

### Authentication Flow & Navigation
- **D-01: Anonymous/Deferred Auth.** Users can start scanning immediately without a mandatory login screen.
- **D-02: In-App Trigger.** A profile or sync button in the UI will trigger the sign-in/up flow (Google/Email) when the user wants to persist data to the cloud.

### Cloud Sync Strategy
- **D-03: Auto (Immediate) Sync.** The app will attempt to sync data to Firebase as soon as a scan is finalized and the PDF is generated.
- **D-04: Any Network Connectivity.** Synchronization is allowed over any available internet connection (not restricted to Wi-Fi) by default.
- **D-05: Visible Progress.** Users will see active upload progress via notifications or in-app status bars to maintain transparency of background work.

### Data Linking & Conflicts
- **D-06: File-First Sync Flow.** To ensure data integrity, the PDF will be uploaded to Cloud Storage first. Once successful, the Firestore metadata document will be created/updated with the file's download URL.
- **D-07: Last Writer Wins.** Conflicts between local and cloud data will be resolved by letting the most recent update (based on timestamp) overwrite previous versions.

### Security & Isolation
- **D-08: Per-User Data Isolation.** Robust Firebase Security Rules will be used to ensure users can only read and write their own documents and files (`/users/{uid}/*`).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Foundation
- `.planning/ROADMAP.md` — Defines phase goals and high-level tasks.
- `.planning/REQUIREMENTS.md` — Lists functional requirements for sync (REQ-1.3.x).
- `.planning/PROJECT.md` — Core tech stack and vision.

### Configuration
- `app/build.gradle.kts` — Existing Firebase dependencies (Auth, Firestore, Storage).
- `app/google-services.json` — Firebase project configuration.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `JsonStorage.kt`: Serves as the local source of truth for the offline-first experience before syncing.
- `PdfGenerator.kt`: The point where PDF generation completes and sync should be triggered.

### Established Patterns
- **MVVM:** Use `ViewModel` to observe Auth state and trigger sync operations via a Repository or UseCase.
- **State-based Navigation:** `MainActivity` already uses a sealed class for screen state, making it easy to add an Auth/Profile destination.

### Integration Points
- `ScannerViewModel`: Needs to be updated to initiate background sync via `WorkManager` after PDF generation.
- `MainActivity`: Add the profile/sync trigger to the UI.

</code_context>

<specifics>
## Specific Ideas
- No specific UI designs provided; open to standard Material 3 patterns for login and profile management.
</specifics>

<deferred>
## Deferred Ideas
- **Multi-page scans:** Scheduled for Phase 3.
- **Bento Grid Home:** Scheduled for Phase 3.
- **Search and Filter:** Scheduled for Phase 3.

</deferred>

---

*Phase: 2-Firebase Integration & Synchronization*
*Context gathered: 2026-05-21*
