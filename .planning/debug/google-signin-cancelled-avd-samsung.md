---
status: resolved
trigger: "Google Sign-In shows 'Sign-in cancelled' on AVD and some Samsung devices; works on other devices"
created: 2026-05-31T00:00:00Z
updated: 2026-05-31T00:00:00Z
symptoms_prefilled: true
---

## Current Focus

hypothesis: CONFIRMED — Three bugs: (1) ProfileScreen missing signOut() before sign-in causes cached-cancel RESULT_CANCELED; (2) requestScopes(DRIVE_FILE) during login adds a consent step that triggers RESULT_CANCELED on AVD/Samsung; (3) default_web_client_id is hardcoded to Android client ID (type 1) instead of web client ID (type 3), breaking idToken for Firebase auth.
test: Full codebase read completed.
expecting: N/A — root causes confirmed.
next_action: Apply fixes.

reasoning_checkpoint:
  hypothesis: "Three independent bugs compound to produce RESULT_CANCELED or sign-in failure on AVD/Samsung. The ProfileScreen fix was applied to LoginScreen (signOut before launch) but never propagated to ProfileScreen. Additionally, requesting DRIVE_FILE scope during the login flow creates an extra consent dialog that fails silently on AVD and some Samsung devices. The wrong web client ID means even 'successful' sign-ins return null idToken."
  confirming_evidence:
    - "ProfileScreen.kt line 294: launcher.launch(googleSignInClient.signInIntent) — NO signOut() before launch (same bug already fixed in LoginScreen.kt)"
    - "LoginScreen.kt lines 225-230: .requestScopes(Scope(DriveScopes.DRIVE_FILE)) in sign-in GSO — extra consent step not needed for basic Firebase auth, causes RESULT_CANCELED on devices where consent flow fails"
    - "strings.xml line 3: default_web_client_id = '640867640781-et954voeu0u4hg2us8s9lv75lubiskpp' which is client_type:1 (Android client) — requestIdToken() requires client_type:3 (web client = '640867640781-boe7f3v256nbs0v5cm6gbmo5d1l78qt3')"
    - "google-services.json lines 15-17: Android client has certificate_hash only for ONE SHA-1 — debug vs release signing differences can cause DEVELOPER_ERROR on some builds"
  falsification_test: "If signOut() is added to ProfileScreen, Drive scope removed from LoginScreen, and web client ID fixed → RESULT_CANCELED will stop appearing on previously-failing devices."
  blind_spots: "AVD with no Google account configured will still return RESULT_CANCELED — this is a device setup issue, not a code bug. Users must add a Google account in AVD Settings > Accounts."

## Symptoms

expected: Google Sign-In succeeds on all devices
actual: RESULT_CANCELED / 'Sign-in cancelled' toast on AVD and some Samsung devices
errors: RESULT_CANCELED (status 12501 SIGN_IN_CANCELLED or device-kill)
reproduction: Click Google sign-in button on LoginScreen or Profile 'Connect Google' on affected device
started: unknown

## Eliminated

- hypothesis: "SHA-1 mismatch causes DEVELOPER_ERROR"
  evidence: "DEVELOPER_ERROR returns as ApiException (caught in try/catch), NOT as RESULT_CANCELED. Would show 'status=10' error, not 'Sign-in cancelled'. Not the cause."
  timestamp: 2026-05-31T00:00:00Z

## Evidence

- timestamp: 2026-05-31T00:00:00Z
  checked: ProfileScreen.kt lines 292-295
  found: "GoogleSignIn.getClient(context, gso); launcher.launch(googleSignInClient.signInIntent) — no signOut() call"
  implication: "Cached failed/cancelled state persists across launches on Samsung and some devices. Causes immediate RESULT_CANCELED."

- timestamp: 2026-05-31T00:00:00Z
  checked: LoginScreen.kt lines 225-230
  found: ".requestScopes(Scope(DriveScopes.DRIVE_FILE)) added to sign-in GSO"
  implication: "Login flow shows extra Drive consent dialog. On AVD (no account) and some Samsung devices, this extra step causes RESULT_CANCELED. Drive scope only needed in ProfileScreen for account linking."

- timestamp: 2026-05-31T00:00:00Z
  checked: strings.xml line 3 vs google-services.json
  found: "default_web_client_id = 640867640781-et954voeu0u4hg2us8s9lv75lubiskpp (client_type:1 Android) but requestIdToken() needs client_type:3 web client = 640867640781-boe7f3v256nbs0v5cm6gbmo5d1l78qt3"
  implication: "Even on successful sign-in, idToken may be null or invalid for Firebase, causing 'No ID Token' failure. Must be web client ID."

## Resolution

root_cause: |
  Three bugs compound:
  1. ProfileScreen.kt (line 294): Missing signOut() before launching signInIntent. LoginScreen was fixed but ProfileScreen was not. Cached GoogleSignInClient state causes RESULT_CANCELED on Samsung and other devices with stale sign-in state.
  2. LoginScreen.kt (lines 225-230): requestScopes(DRIVE_FILE) in the login GSO adds an extra OAuth consent screen. On AVD (no Google account) and some Samsung devices, this extra consent step fails, returning RESULT_CANCELED. Drive scope is only needed in ProfileScreen.
  3. strings.xml (line 3): default_web_client_id is set to the Android OAuth client (type 1) instead of the web OAuth client (type 3). requestIdToken() requires the web client ID to produce a Firebase-valid token.

fix: |
  1. ProfileScreen.kt: Add signOut().addOnCompleteListener { launcher.launch(...) } before launching signInIntent
  2. LoginScreen.kt: Remove .requestScopes(Scope(DriveScopes.DRIVE_FILE)) and unused Scope/DriveScopes imports
  3. strings.xml: Change default_web_client_id to 640867640781-boe7f3v256nbs0v5cm6gbmo5d1l78qt3.apps.googleusercontent.com (web client type 3)

verification: Applied — code changes made
files_changed:
  - app/src/main/java/com/t2h/ocr/ui/profile/ProfileScreen.kt
  - app/src/main/java/com/t2h/ocr/ui/login/LoginScreen.kt
  - app/src/main/res/values/strings.xml
