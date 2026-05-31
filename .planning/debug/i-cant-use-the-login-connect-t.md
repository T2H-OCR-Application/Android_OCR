---
status: resolved
trigger: |
  DATA_START
  I cant use the login - connect to gmail function on some device, it just said sign in cancelled when i clicked on it
  DATA_END
---
# Symptoms
- Expected behavior: Connect to gmail successfully.
- Actual behavior: It says "sign in cancelled" when clicked on it on some devices.
- Error messages: "sign in cancelled"
- Timeline: Not provided.
- Reproduction: Click on "connect to gmail" button.

# Current Focus
- hypothesis: The Google Sign-In client returns a cached cancellation state on some devices, instantly returning RESULT_CANCELED.
- next_action: Apply the fix.

# Resolution
- root_cause: GoogleSignInClient retains previous cancellation or partial sign-in state on certain devices, causing subsequent sign-in intents to immediately fail with RESULT_CANCELED.
- fix: Called googleSignInClient.signOut() before launching the signInIntent to clear any cached states.