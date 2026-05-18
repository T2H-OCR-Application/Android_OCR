# Firebase Synchronization Patterns for Android OCR

This document details robust patterns for synchronizing OCR data and PDF files across devices using Firebase Auth, Firestore, and Cloud Storage.

## 1. Offline-First Architecture

### Firestore (Text & Metadata)
Firestore handles offline persistence automatically. To ensure a robust experience:
-   **Enable Persistence:** `FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build()`. (Enabled by default on Android).
-   **Local-First UI:** Use Snapshot Listeners. The UI should update immediately from the local cache when a user saves OCR data, even without a network.
-   **Conflict Resolution:** Use `set(data, SetOptions.merge())` for updates to prevent overwriting metadata changed on another device.

### Cloud Storage (PDF Files)
Storage does NOT have built-in persistence across app restarts. We use **Android WorkManager** to bridge this gap.

## 2. Resumable Upload Pattern

To handle large PDF files efficiently:
1.  **Capture Session URI:** When starting an upload with `putFile()`, capture the `uploadSessionUri` from the `TaskSnapshot`.
2.  **Persist URI:** Save this URI in `SharedPreferences` or a local Database mapped to the file path.
3.  **Resume:** If the upload is interrupted (network loss, app kill), the next `WorkManager` run retrieves the URI and calls:
    ```kotlin
    val task = storageRef.putFile(localFileUri, metadata, sessionUri)
    ```
    This resumes the upload from the last byte successfully received by the server.

## 3. Data Security Rules

### Firestore Rules (Ownership)
Ensure users can only access their own OCR records.
```javascript
service cloud.firestore {
  match /databases/{database}/documents {
    match /documents/{docId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
    }
  }
}
```

### Cloud Storage Rules (Paths)
Isolate files into user-specific folders.
```javascript
service firebase.storage {
  match /b/{bucket}/o {
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## 4. Efficient File Management

### Hybrid Storage Pattern
-   **Firestore:** Stores `title`, `uploadDate`, `userId`, and a `searchSnippet` (first 1-2KB of text).
-   **Cloud Storage:** Stores the original `.pdf` and the full `.json` result from ML Kit (including bounding boxes).
-   **Linking:** Use the same UUID for the Firestore document ID and the Storage filename to simplify lookups.

### WorkManager Configuration
```kotlin
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .setRequiresBatteryNotLow(true)
    .build()

val uploadWork = OneTimeWorkRequestBuilder<UploadWorker>()
    .setConstraints(constraints)
    .setInputData(workDataOf("FILE_PATH" to fileUri.toString()))
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
    .build()
```

## 5. Security Best Practices
-   **App Check:** Enable Firebase App Check to prevent unauthorized API access from non-app clients.
-   **Storage Constraints:** Add file size and MIME type checks in Storage Rules:
    ```javascript
    allow write: if request.resource.size < 50 * 1024 * 1024 // 50MB limit
                 && request.resource.contentType == 'application/pdf';
    ```

## Sources
- [Firebase Documentation: Resumable Uploads](https://firebase.google.com/docs/storage/android/upload-files#monitor_upload_progress)
- [Android Developers: WorkManager Constraints](https://developer.android.com/topic/libraries/architecture/workmanager/how-to/constraints)
