# Research Summary: Android OCR Firebase Sync

**Domain:** Mobile Document Management / OCR
**Researched:** 2024-12-21
**Overall confidence:** HIGH

## Executive Summary

This research focuses on robust patterns for synchronizing OCR data and PDF files in an Android application using Firebase (Auth, Firestore, Storage). The primary challenge is maintaining a seamless "offline-first" experience while handling potentially large PDF files and heavy OCR metadata (bounding boxes, confidence scores) that can exceed Firestore's 1MB limit.

The recommended architecture uses a **Hybrid Sync Pattern**: Firestore handles lightweight, searchable metadata and text snippets, while Cloud Storage stores the heavy PDF files and full OCR JSON results. This ensures scalability, keeps costs low, and avoids document size limits.

Reliable synchronization is achieved by combining the Firebase SDK's built-in Firestore persistence with Android's `WorkManager` for Cloud Storage uploads. By capturing and persisting the `uploadSessionUri`, the application can resume interrupted uploads even after app process death or device restarts.

## Key Findings

**Stack:** Kotlin, Jetpack Compose, Firebase (Auth, Firestore, Storage), ML Kit OCR, and WorkManager for background sync.
**Architecture:** Hybrid approach splitting data between Firestore (metadata/search) and Storage (files/full OCR JSON).
**Critical pitfall:** Firestore's 1 MiB document limit makes it unsuitable for full OCR results with bounding boxes for multi-page documents.

## Implications for Roadmap

Based on research, suggested phase structure:

1.  **Foundation & Local OCR** - Build the core OCR engine using ML Kit and local PDF handling.
    -   Addresses: PDF selection, Text extraction, Local storage.
2.  **Firebase Integration & Auth** - Implement user identity and basic metadata sync.
    -   Addresses: Google/Email Auth, Firestore metadata schema.
3.  **Robust Background Sync** - Implement the `WorkManager` + `UploadTask` pattern for reliable file uploads.
    -   Addresses: Offline-first file sync, Resumable uploads.
4.  **Advanced OCR & Search** - Move full OCR data to Storage and implement cross-document search.
    -   Addresses: Hybrid data pattern, search snippets in Firestore.

**Phase ordering rationale:**
-   Starting with local OCR ensures the core value prop works immediately.
-   Reliable sync is a "hard" technical piece that should be solved before scaling the feature set.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Standard Google/Firebase stack for Android. |
| Features | HIGH | Well-understood document management patterns. |
| Architecture | HIGH | Proven "Hybrid" pattern for large metadata. |
| Pitfalls | MEDIUM | Most pitfalls are documented, but edge cases in WorkManager + Firebase UI sync need care. |

## Gaps to Address

-   **PDF Compression:** Research into Android-native PDF compression libraries to reduce storage costs and upload times.
-   **Full-Text Search:** While snippets in Firestore enable basic search, true full-text search across thousands of documents might eventually require Algolia or Firestore Vector Search (new feature).
