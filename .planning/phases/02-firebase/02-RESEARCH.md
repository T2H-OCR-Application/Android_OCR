# Phase 2: Firebase Integration & Synchronization - Research

**Researched:** 2026-05-21
**Domain:** Cloud Synchronization & User Identity
**Confidence:** HIGH

## Summary

This phase focuses on transitioning the Android OCR app from a local-only utility to a cloud-synced platform. The core technical challenges involve managing "Anonymous-to-Permanent" user accounts without disrupting the user experience, implementing a robust "File-First" synchronization pipeline (Storage then Firestore), and ensuring reliable background execution via WorkManager.

**Primary recommendation:** Use Firebase Authentication's anonymous sign-in by default to enable immediate app use, and leverage `CoroutineWorker` with `setForeground` to provide a reliable, transparent background sync experience.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01: Anonymous/Deferred Auth.** Users can start scanning immediately without a mandatory login screen.
- **D-02: In-App Trigger.** A profile or sync button in the UI will trigger the sign-in/up flow (Google/Email) when the user wants to persist data to the cloud.
- **D-03: Auto (Immediate) Sync.** The app will attempt to sync data to Firebase as soon as a scan is finalized and the PDF is generated.
- **D-04: Any Network Connectivity.** Synchronization is allowed over any available internet connection (not restricted to Wi-Fi) by default.
- **D-05: Visible Progress.** Users will see active upload progress via notifications or in-app status bars to maintain transparency of background work.
- **D-06: File-First Sync Flow.** To ensure data integrity, the PDF will be uploaded to Cloud Storage first. Once successful, the Firestore metadata document will be created/updated with the file's download URL.
- **D-07: Last Writer Wins.** Conflicts between local and cloud data will be resolved by letting the most recent update (based on timestamp) overwrite previous versions.
- **D-08: Per-User Data Isolation.** Robust Firebase Security Rules will be used to ensure users can only read and write their own documents and files (`/users/{uid}/*`).

### the agent's Discretion
- No specific UI designs provided; open to standard Material 3 patterns for login and profile management.

### Deferred Ideas (OUT OF SCOPE)
- **Multi-page scans:** Scheduled for Phase 3.
- **Bento Grid Home:** Scheduled for Phase 3.
- **Search and Filter:** Scheduled for Phase 3.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| REQ-1.3.1 | User authentication via Firebase (Email/Password or Social). | Verified `linkWithCredential` flow for anonymous-to-permanent upgrade. |
| REQ-1.3.2 | Real-time synchronization of scan history and metadata using Cloud Firestore. | Defined LWW strategy using `updatedAt` timestamps and Security Rules. |
| REQ-1.3.3 | Secure storage of PDF documents in Firebase Cloud Storage. | Documented "File-First" sync pattern via `WorkManager`. |
| REQ-1.3.4 | Data isolation: users can only access their own documents. | Designed per-user Firestore and Storage paths with Security Rules. |
| NREQ-2.2.1| Background sync should be resilient to network failures. | WorkManager retry policies and resumable uploads researched. |
</phase_requirements>

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Authentication | API (Firebase Auth) | Client (Android) | Firebase manages the identity provider and token lifecycle. |
| Metadata Sync | Database (Firestore)| Client (Android) | Firestore provides real-time listeners and offline persistence. |
| File Storage | Storage (Firebase)  | Client (Android) | Cloud Storage handles large binary objects (PDFs). |
| Background Sync| Client (OS)         | Client (App)    | WorkManager handles OS-level scheduling and lifecycle persistence. |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `firebase-auth-ktx` | 23.1.0 | User Identity | Standard for Android Auth; supports account linking. [VERIFIED: official docs] |
| `firebase-firestore-ktx`| 25.1.1 | Metadata Store | Real-time sync and easy scaling for mobile metadata. [VERIFIED: official docs] |
| `firebase-storage-ktx` | 21.0.1 | File Hosting | Native integration with Firebase Auth/Firestore. [VERIFIED: official docs] |
| `androidx-work-runtime-ktx`| 2.11.2 | Background Tasks| Android's standard for reliable background work. [VERIFIED: Maven Central] |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `play-services-auth`| 21.3.0 | Google Sign-In | Required for social authentication. [ASSUMED] |
| `kotlinx-serialization`| 1.7.3 | Data Conversion | Already used for local JSON; helpful for Firestore mapping. [VERIFIED: libs.versions.toml] |

**Installation:**
```kotlin
// In app/build.gradle.kts (Add missing versions)
implementation("androidx.work:work-runtime-ktx:2.11.2")
implementation("com.google.android.gms:play-services-auth:21.3.0")
```

## Package Legitimacy Audit

| Package | Registry | Age | Downloads | Source Repo | slopcheck | Disposition |
|---------|----------|-----|-----------|-------------|-----------|-------------|
| com.google.firebase:firebase-auth | Maven | 10y+ | High | firebase/firebase-android-sdk | [OK] | Approved |
| com.google.firebase:firebase-firestore | Maven | 7y+ | High | firebase/firebase-android-sdk | [OK] | Approved |
| com.google.firebase:firebase-storage | Maven | 10y+ | High | firebase/firebase-android-sdk | [OK] | Approved |
| androidx.work:work-runtime-ktx | Maven | 6y+ | High | androidx/androidx | [OK] | Approved |

## Architecture Patterns

### Recommended Project Structure
```
app/src/main/java/com/t2h/ocr/
├── data/
│   ├── auth/            # Firebase Auth implementations
│   ├── sync/            # WorkManager SyncWorkers
│   └── repository/      # SyncRepository (Coordinates Storage/Firestore)
├── domain/
│   └── models/          # Firestore-compatible Data Classes
└── ui/
    ├── auth/            # Profile & Login Screen
    └── scanner/
        └── ScannerViewModel.kt # Updated to trigger SyncWorker
```

### Pattern 1: File-First Sync Pipeline
**What:** Upload binary files to Storage first, then update metadata in Firestore.
**When to use:** Every time a new scan is finalized.
**Example:**
```kotlin
// SyncWorker.kt
override suspend fun doWork(): Result {
    val localUri = inputData.getString("file_uri") ?: return Result.failure()
    
    // 1. Upload to Storage
    val storageRef = storage.reference.child("users/$uid/scans/$scanId.pdf")
    val uploadTask = storageRef.putFile(Uri.parse(localUri)).await()
    val downloadUrl = storageRef.downloadUrl.await()

    // 2. Create Firestore Metadata
    val metadata = ScanMetadata(..., pdfUrl = downloadUrl.toString())
    firestore.collection("users").document(uid).collection("scans")
        .document(scanId).set(metadata).await()

    return Result.success()
}
```

### Pattern 2: Last Writer Wins (LWW) via Security Rules
**What:** Enforce that only newer updates (based on client-side timestamps) can overwrite existing data.
**When to use:** To handle concurrent edits or late offline syncs.
**Implementation:**
```javascript
// Firestore Security Rules
match /users/{userId}/scans/{scanId} {
  allow update: if request.resource.data.updatedAt > resource.data.updatedAt;
}
```

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Sync Reliability | Custom Service/Thread | WorkManager | Handles process death, network retries, and OS constraints automatically. |
| Progress Notifications| Custom Notification Mgr| `setForeground()` | Native integration with WorkManager for persistent status updates. |
| User Session Mgmt | Custom SQLite Table | Firebase Auth | Handles secure token refreshes and multiple providers natively. |

## Common Pitfalls

### Pitfall 1: Notification Channel Missing
**What goes wrong:** `setForeground()` fails or doesn't show the notification on API 26+.
**How to avoid:** Create the notification channel in the `Application` class or at the start of the `Worker`.

### Pitfall 2: Firestore Offline Latency
**What goes wrong:** Local updates appear instantly, but listeners might fire multiple times (once for local, once for server).
**How to avoid:** Check `metadata.hasPendingWrites()` in the UI to distinguish between local-optimistic and server-confirmed state.

### Pitfall 3: Large PDF Uploads
**What goes wrong:** Uploads on slow connections time out or drain battery.
**How to avoid:** Use WorkManager's `setRequiredNetworkType(NetworkType.CONNECTED)` and resumable upload support in Firebase Storage.

## Code Examples

### Anonymous to Google Account Linking
```kotlin
// Source: https://firebase.google.com/docs/auth/android/account-linking
fun linkAnonymousWithGoogle(googleIdToken: String) {
    val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
    Firebase.auth.currentUser?.linkWithCredential(credential)
        ?.addOnSuccessListener { result ->
            // Account is now permanent
        }
}
```

### Worker Progress in UI
```kotlin
// ViewModel
val syncStatus = WorkManager.getInstance(context)
    .getWorkInfoByIdLiveData(workerId)
    .asFlow()
    .map { workInfo ->
        val progress = workInfo?.progress?.getInt("progress", 0) ?: 0
        SyncState(progress, workInfo?.state)
    }
```

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| `google-services.json` | Firebase Init | ✓ | — | — |
| Firebase Project | Auth/Storage/DB | ✓ | — | — |
| Google Play Services | Auth/Scanner | ✓ | — | — |

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 + AndroidX Test |
| Config file | `app/build.gradle.kts` |
| Full suite command | `./gradlew connectedAndroidTest` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| REQ-1.3.1 | Auth Linking | Instrument | `ScanFlowTest` (extended) | ✅ |
| REQ-1.3.2 | Firestore Sync | Instrument | `SyncWorkerTest` | ❌ Wave 0 |
| REQ-1.3.3 | Storage Upload | Instrument | `SyncWorkerTest` | ❌ Wave 0 |

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | yes | Firebase Auth (Anonymous & Google) |
| V4 Access Control | yes | Firebase Security Rules (per-user paths) |
| V5 Input Validation | yes | Firestore Field Validation (rules) |

### Known Threat Patterns

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Unauthorized Data Access | Information Disclosure | Firebase Rules: `request.auth.uid == userId` |
| Malicious File Upload | Tampering | Limit file size and types in Storage Rules |

## Sources

### Primary (HIGH confidence)
- Firebase Official Documentation - [Authentication](https://firebase.google.com/docs/auth/android/account-linking), [Firestore](https://firebase.google.com/docs/firestore), [Storage](https://firebase.google.com/docs/storage)
- Android Developers - [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)

### Tertiary (LOW confidence)
- WebSearch for Google Auth versioning - [ASSUMED]

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `play-services-auth:21.3.0` is the latest stable. | Standard Stack | Build error / minor version mismatch. |

**Research date:** 2026-05-21
**Valid until:** 2026-06-21
