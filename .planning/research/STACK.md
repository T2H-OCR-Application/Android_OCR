# Technology Stack: Android OCR

**Project:** Android OCR with Firebase Sync
**Researched:** 2024-12-21

## Recommended Stack

### Core Framework
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Kotlin | 1.9+ | Language | Modern, safe, and official Android language. |
| Jetpack Compose | BOM 2024+ | UI Framework | Declarative UI, faster development, better testing. |
| Android SDK | 35 (Target) | Platform | Latest features and security patches. |

### Backend / Sync
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| Firebase Auth | 33.7.0+ | Identity | Secure, supports Google/Email, handles session persistence. |
| Cloud Firestore | 33.7.0+ | Metadata | NoSQL, real-time sync, offline-first by default. |
| Cloud Storage | 33.7.0+ | File Hosting | Reliable hosting for PDFs and large OCR JSONs. |

### AI / OCR
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| ML Kit (Text) | 16.0.0 | Local OCR | Fast, runs on-device, free, no network required for core functionality. |

### Infrastructure / Local
| Technology | Version | Purpose | Why |
|------------|---------|---------|-----|
| WorkManager | 2.9.0+ | Background Sync | Guaranteed execution, handles constraints (WiFi, Battery). |
| Room | 2.6.0+ | Local Cache | Optional, but useful for complex local-only state before Firebase sync. |
| DataStore | 1.1.0+ | Preferences | Modern replacement for SharedPreferences; safe for UI settings. |

## Alternatives Considered

| Category | Recommended | Alternative | Why Not |
|----------|-------------|-------------|---------|
| OCR | ML Kit | Google Cloud Vision | Cloud Vision is paid and requires network; ML Kit is free and local. |
| Database | Firestore | Realtime DB | Firestore has better querying and scalability for document-based data. |
| Background | WorkManager | Service | WorkManager handles system-level rescheduling better than raw Services. |

## Installation

```kotlin
// In app/build.gradle.kts
dependencies {
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // ML Kit
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
```

## Sources

- [Firebase Official Documentation](https://firebase.google.com/docs/android/setup)
- [Android WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)
- [ML Kit Text Recognition](https://developers.google.com/ml-kit/vision/text-recognition/android)
