# UAT Session: Post-Phase 4 Fixes & Features

**Status:** COMPLETED
**Conducted:** 2026-05-31

## Authentication & Authorization
| ID | Requirement | Test Case | Status | Notes |
|----|-------------|-----------|--------|-------|
| U-BUG-01 | Google Sign-In | Verify Google button on LoginScreen initiates authentication and navigates to Home. | PASS | User confirmed: "its working" |
| U-BUG-02 | Drive Scope | Verify that signing in or linking account prompts for "Google Drive" permission (DRIVE_FILE scope). | PASS | User confirmed seeing the prompt. |
| U-BUG-03 | Auth Recovery | Trigger a missing-scope error (e.g., by revoking Drive permission) and verify recovery notification in SyncWorker. | PASS | User confirmed notification appeared and launched dialog. |

## Sync & Error Handling
| ID | Requirement | Test Case | Status | Notes |
|----|-------------|-----------|--------|-------|
| U-BUG-04 | Sealed Result | Verify SyncWorker correctly handles UploadResult.Success and updates Firestore. | PASS | User confirmed file is on Drive and sync finished. |
| U-BUG-05 | Localized Strings| Verify sync-related notification strings are correctly translated (EN/VI/ES). | PASS | Verified in strings.xml and via user confirmation of recovery notification. |

## Search Functionality
| ID | Requirement | Test Case | Status | Notes |
|----|-------------|-----------|--------|-------|
| U-FEAT-01| Home Search | Verify Home search bar is interactive and filters by OCR text. | PASS | User confirmed search bar is interactive and filters by OCR text correctly. |
| U-FEAT-02| History Search| Verify History search bar filters by OCR text. | PASS | User confirmed History search filters correctly by OCR text. |

