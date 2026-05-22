---
status: completed
phase: 02-firebase
source: [.planning/phases/02-firebase/2-VERIFICATION.md]
started: 2026-05-21T17:50:00Z
updated: 2026-05-22T08:45:00Z
---

## Current Test
None. UAT Session concluded.

## Tests

### 1. Firebase Anonymous Sign-In
expected: |
  1. Launch the app on a device/emulator.
  2. Open the Firebase Console -> Authentication tab.
  3. A new user with the "Anonymous" provider should be created and visible.
result: passed

### 2. Google Account Linking
expected: |
  1. Navigate to the Profile screen (Profile icon in Scanner top bar).
  2. Click "Link Google Account" and complete the authentication flow.
  3. The status in the app should change to "Linked Account".
  4. In the Firebase Console, the user's provider should change from "Anonymous" to "Google".
result: passed

### 3. End-to-End Sync Flow
expected: |
  1. Perform a document scan and click "Save as PDF".
  2. Wait for the "Syncing Scan" notification to disappear.
  3. Open the Firebase Storage tab; the PDF should be at `users/{uid}/scans/{id}.pdf`.
  4. Open the Firestore tab; a document at `users/{uid}/scans/{id}` should exist with `isSynced: true` and the correct `remotePdfUrl`.
result: failed
issue: Blocked by environment constraint (Firebase Storage requires billing).

### 4. Sync Notification
expected: |
  1. Save a scan in the Results screen.
  2. A persistent system notification titled "Syncing Scan" should briefly appear in the notification tray.
result: failed
issue: Blocked by failure of SyncWorker caused by Firebase Storage constraint.

## Summary

total: 4
passed: 2
issues: 2
pending: 0
skipped: 0

## Gaps

- **Test 3 & 4 Failure (Sync and Notification):**
    - **Diagnosis:** The user's Firebase project is on a free tier that does not allow enabling Cloud Storage without a billing account. This causes the `SyncWorker` to fail when calling `putFile`, which blocks both the PDF upload and the subsequent Firestore metadata update.
    - **Fix Plan:** We have designed an alternative architecture to bypass Firebase Storage entirely. The app will now upload PDFs directly to the user's personal Google Drive using the Google Drive REST API. 
    - **Status:** The fix plan is documented and formally approved at `plans/google-drive-sync.md` and is ready for `/gsd:execute-phase`.
