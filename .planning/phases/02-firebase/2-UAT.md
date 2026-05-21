---
status: testing
phase: 02-firebase
source: [.planning/phases/02-firebase/2-VERIFICATION.md]
started: 2026-05-21T17:50:00Z
updated: 2026-05-21T17:50:00Z
---

## Current Test
number: 1
name: Firebase Anonymous Sign-In
expected: |
  1. Launch the app on a device/emulator.
  2. Open the Firebase Console -> Authentication tab.
  3. A new user with the "Anonymous" provider should be created and visible.
awaiting: user response

## Tests

### 1. Firebase Anonymous Sign-In
expected: |
  1. Launch the app on a device/emulator.
  2. Open the Firebase Console -> Authentication tab.
  3. A new user with the "Anonymous" provider should be created and visible.
result: pending

### 2. Google Account Linking
expected: |
  1. Navigate to the Profile screen (Profile icon in Scanner top bar).
  2. Click "Link Google Account" and complete the authentication flow.
  3. The status in the app should change to "Linked Account".
  4. In the Firebase Console, the user's provider should change from "Anonymous" to "Google".
result: pending

### 3. End-to-End Sync Flow
expected: |
  1. Perform a document scan and click "Save as PDF".
  2. Wait for the "Syncing Scan" notification to disappear.
  3. Open the Firebase Storage tab; the PDF should be at `users/{uid}/scans/{id}.pdf`.
  4. Open the Firestore tab; a document at `users/{uid}/scans/{id}` should exist with `isSynced: true` and the correct `remotePdfUrl`.
result: pending

### 4. Sync Notification
expected: |
  1. Save a scan in the Results screen.
  2. A persistent system notification titled "Syncing Scan" should briefly appear in the notification tray.
result: pending

## Summary

total: 4
passed: 0
issues: 0
pending: 4
skipped: 0

## Gaps

[none yet]
