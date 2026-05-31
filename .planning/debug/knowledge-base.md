# GSD Debug Knowledge Base

Resolved debug sessions. Used by `gsd-debugger` to surface known-pattern hypotheses at the start of new investigations.

---

## google-drive-sync-auth-fix — Fix Google Drive sync authorization and sign-in
- **Date:** 2025-01-24
- **Error patterns:** NEED_REMOTE_CONSENT, UserRecoverableAuthIOException, sync silent retry, Google button TODO
- **Root cause:** Missing Drive scopes in sign-in options, incorrect exception handling in uploader, and lack of notification for user-recoverable auth errors.
- **Fix:** Added DRIVE_FILE scope to GoogleSignInOptions, implemented Google Sign-In, refactored DriveUploader to handle UserRecoverableAuthIOException, and updated SyncWorker to post recovery notifications.
- **Files changed:** AuthRepository.kt, DriveUploader.kt, SyncWorker.kt, ProfileScreen.kt, LoginScreen.kt, MainActivity.kt, strings.xml
---

## google-signin-cancelled-avd-samsung — Fix Sign-in cancelled on AVD/Samsung
- **Date:** 2026-05-31
- **Error patterns:** RESULT_CANCELED, "Sign-in cancelled" toast, works on some devices not others
- **Root cause:** (1) ProfileScreen missing signOut() before launching signInIntent — cached cancel state triggers RESULT_CANCELED; (2) LoginScreen requested DRIVE_FILE scope during sign-in — extra consent step fails on AVD/Samsung; (3) default_web_client_id was Android client (type 1) not web client (type 3) — wrong client causes null idToken.
- **Fix (round 1):** Added signOut() before sign-in in ProfileScreen, removed Drive scope from LoginScreen GSO, fixed default_web_client_id to web client ID (type 3).
- **Fix (round 2):** Migrated from deprecated GoogleSignInClient.signInIntent to Credential Manager API (androidx.credentials 1.3.0 + googleid 1.1.1). Old API returns RESULT_CANCELED on Samsung One UI + newer AVDs after account selection with play-services-auth 21.3.0.
- **Files changed (round 2):** LoginScreen.kt, ProfileScreen.kt, build.gradle.kts, libs.versions.toml
- **Note for AVD:** If still cancelled on AVD after fixes, the device has no Google account configured — must add one in Settings > Accounts > Add Account > Google.
---

## ocr-text-search-fix — Implement search functionality for OCR text in Home and History screens.
- **Date:** 2026-05-31
- **Error patterns:** Search Bar non-functional, Missing OCR Text Filter
- **Root cause:** Search bar in HomeScreen.kt was a static UI component, and filtering logic in both HomeScreen.kt and History.kt omitted the `ocrText` field.
- **Fix:** Replaced static search bar in HomeScreen.kt with an interactive `BasicTextField` and updated the filtering logic in both files to include `ocrText`.
- **Files changed:** app/src/main/java/com/t2h/ocr/ui/home/HomeScreen.kt, app/src/main/java/com/t2h/ocr/ui/home/History.kt
---
