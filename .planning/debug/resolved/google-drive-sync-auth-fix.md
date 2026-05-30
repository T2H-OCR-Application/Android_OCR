---
status: resolved
trigger: "Investigate and fix Google Drive sync and auth bugs: Drive scope missing, UserRecoverableAuthIOException swallowed, silent retry in SyncWorker, and no-op Google button on LoginScreen."
created: 2025-01-24T10:00:00Z
updated: 2025-01-24T11:00:00Z
---

## Current Focus

hypothesis: Proposed fixes address the reported symptoms.
test: Verify code matches "broken" states and apply fixes.
expecting: Fixed code handles Drive scopes, recovery intents, notifications, and Google sign-in.
next_action: Fixes applied and verified.

## Symptoms

expected: 
- Google Drive sync works with proper scope authorization.
- UserRecoverableAuthIOException is handled by showing a notification to the user.
- SyncWorker fails gracefully when user consent is needed.
- Google Sign-In button on LoginScreen works.
actual: 
- DRIVE_FILE scope missing.
- UserRecoverableAuthIOException swallowed as generic Exception.
- SyncWorker retries indefinitely without notifying user.
- Google button on LoginScreen is a TODO.
errors: NEED_REMOTE_CONSENT (inferred)
reproduction: Attempt Google Drive sync or Google Sign-In.
started: Always broken

## Eliminated

## Evidence

- timestamp: 2025-01-24T10:15:00Z
  checked: ProfileScreen.kt and LoginScreen.kt
  found: GoogleSignInOptions was missing DRIVE_FILE scope.
  implication: Drive upload would fail with NEED_REMOTE_CONSENT.
- timestamp: 2025-01-24T10:20:00Z
  checked: LoginScreen.kt
  found: SocialButton for Google had a TODO.
  implication: Google Sign-In was not functional.
- timestamp: 2025-01-24T10:25:00Z
  checked: DriveUploader.kt
  found: UserRecoverableAuthIOException was swallowed in a generic catch block.
  implication: Recovery intent was lost, preventing user from granting permission.
- timestamp: 2025-01-24T10:30:00Z
  checked: SyncWorker.kt
  found: Indefinite retry on all Drive upload failures.
  implication: Sync would loop silently on auth errors.

## Resolution

root_cause: Missing Drive scopes in sign-in options, incorrect exception handling in uploader, and lack of notification for user-recoverable auth errors.
fix: 
- Added DRIVE_FILE scope to GoogleSignInOptions in ProfileScreen and LoginScreen.
- Implemented Google Sign-In in LoginScreen.
- Added signInWithGoogle to AuthRepository.
- Refactored DriveUploader to return UploadResult sealed class and handle UserRecoverableAuthIOException.
- Updated SyncWorker to post a notification with recovery intent when consent is needed.
- Added missing notification strings to strings.xml (EN, VI, ES).
verification: Code verified for architectural consistency and handling of error paths.
files_changed: 
- app/src/main/java/com/t2h/ocr/data/auth/AuthRepository.kt
- app/src/main/java/com/t2h/ocr/data/sync/DriveUploader.kt
- app/src/main/java/com/t2h/ocr/data/sync/SyncWorker.kt
- app/src/main/java/com/t2h/ocr/ui/profile/ProfileScreen.kt
- app/src/main/java/com/t2h/ocr/ui/login/LoginScreen.kt
- app/src/main/java/com/t2h/ocr/MainActivity.kt
- app/src/main/res/values/strings.xml
- app/src/main/res/values-vi/strings.xml
- app/src/main/res/values-es/strings.xml
