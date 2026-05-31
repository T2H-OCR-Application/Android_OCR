# External Integrations

**Analysis Date:** 2025-02-14

## APIs & External Services

**Cloud Services:**
- Firebase Analytics - Tracking user interactions and OCR performance.
  - SDK: `com.google.firebase:firebase-analytics`
- Firebase Firestore - Synchronizing scan metadata and results.
  - SDK: `com.google.firebase:firebase-firestore`
- Google Drive API - Uploading generated PDFs to user's Google Drive.
  - SDK: `com.google.apis:google-api-services-drive:v3`
  - Auth: OAuth2 via `play-services-auth`

**Local ML & Vision:**
- ML Kit Text Recognition - On-device OCR for text extraction.
  - SDK: `com.google.mlkit:text-recognition`
  - Implementation: `app/src/main/java/com/t2h/ocr/MainActivity.kt`
- OpenCV - On-device image processing and document boundary detection.
  - SDK: `com.quickbirdstudios:opencv`
  - Implementation: `app/src/main/java/com/t2h/ocr/domain/ocr/DocumentAnalyzer.kt`

## Data Storage

**Databases:**
- SQLite (via Room) - Local storage for scans and history.
  - Client: Room Persistence Library
  - Files: `app/src/main/java/com/t2h/ocr/data/ScanRepository.kt`

**File Storage:**
- Local filesystem (Internal Storage) - Storing captured images and generated PDFs.
- Google Drive - Cloud backup for exported PDFs.

**Caching:**
- Local cache directory - Used for temporary image processing files in `app/src/main/java/com/t2h/ocr/MainActivity.kt`.

## Authentication & Identity

**Auth Provider:**
- Firebase Authentication - Handles user identity.
  - Implementation: `app/src/main/java/com/t2h/ocr/data/auth/AuthRepository.kt`
  - Methods: Anonymous sign-in, Email/Password, and Google Sign-In support.

## Monitoring & Observability

**Error Tracking:**
- Firebase Crashlytics (indicated by `google-services` plugin, though not explicitly in dependencies, it is common in this stack).
- Custom logging via `AnalyticsHelper` in `app/src/main/java/com/t2h/ocr/domain/observability/AnalyticsHelper.kt`.

**Logs:**
- Android `Log` - Standard console logging.
- Android `Tracing` - Performance profiling in `app/src/main/java/com/t2h/ocr/domain/ocr/DocumentAnalyzer.kt`.

## CI/CD & Deployment

**Hosting:**
- Google Play Store (Production target)

**CI Pipeline:**
- Not detected (likely GitHub Actions or similar if hosted on GitHub).

## Environment Configuration

**Required env vars:**
- None (Configuration is baked into `google-services.json`).

**Secrets location:**
- `app/google-services.json` - Firebase and Google Play Services configuration.
- Proguard rules in `app/proguard-rules.pro`.

## Webhooks & Callbacks

**Incoming:**
- None

**Outgoing:**
- None

---

*Integration audit: 2025-02-14*
