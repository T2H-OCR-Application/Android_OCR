# External Integrations

**Analysis Date:** 2025-01-24

## APIs & External Services

**Machine Learning:**
- Google ML Kit - Used for on-device text recognition (OCR).
  - SDK/Client: `com.google.mlkit:text-recognition:16.0.0`
  - Auth: Handled by Google Services and on-device processing.

**Analytics:**
- Firebase Analytics - Used for tracking app usage and user behavior.
  - SDK/Client: `com.google.firebase:firebase-analytics`
  - Config: `app/google-services.json`

## Data Storage

**Databases:**
- Cloud Firestore - NoSQL cloud database for data synchronization.
  - Client: `com.google.firebase:firebase-firestore`
  - Config: `app/google-services.json`

**File Storage:**
- Firebase Storage - For storing image files uploaded for OCR processing.
  - Client: `com.google.firebase:firebase-storage`
  - Config: `app/google-services.json`

**Caching:**
- Not detected (likely using default Firebase and ML Kit caching mechanisms).

## Authentication & Identity

**Auth Provider:**
- Firebase Authentication - Handles user login and identity.
  - Implementation: `com.google.firebase:firebase-auth`
  - Config: `app/google-services.json`

## Monitoring & Observability

**Error Tracking:**
- Not detected (standard Logcat used for development).

**Logs:**
- Android Logcat (standard approach).

## CI/CD & Deployment

**Hosting:**
- Google Play Store (intended distribution platform).

**CI Pipeline:**
- Not detected.

## Environment Configuration

**Required env vars:**
- None detected (uses `google-services.json` for service configuration).

**Secrets location:**
- `app/google-services.json` - Contains API keys and project identifiers for Firebase and Google services.

## Webhooks & Callbacks

**Incoming:**
- None detected.

**Outgoing:**
- None detected.

---

*Integration audit: 2025-01-24*
