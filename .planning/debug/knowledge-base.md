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

## ocr-text-search-fix — Implement search functionality for OCR text in Home and History screens.
- **Date:** 2026-05-31
- **Error patterns:** Search Bar non-functional, Missing OCR Text Filter
- **Root cause:** Search bar in HomeScreen.kt was a static UI component, and filtering logic in both HomeScreen.kt and History.kt omitted the `ocrText` field.
- **Fix:** Replaced static search bar in HomeScreen.kt with an interactive `BasicTextField` and updated the filtering logic in both files to include `ocrText`.
- **Files changed:** app/src/main/java/com/t2h/ocr/ui/home/HomeScreen.kt, app/src/main/java/com/t2h/ocr/ui/home/History.kt
---
