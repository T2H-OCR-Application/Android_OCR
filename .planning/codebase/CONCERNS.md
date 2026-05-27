# Codebase Concerns

**Analysis Date:** 2025-01-24

## Tech Debt

**Missing Architecture:**
- Issue: The project currently lacks a defined architectural pattern (e.g., MVVM, MVI). All logic (if any) is expected to be placed in `MainActivity.kt`.
- Files: `app/src/main/java/com/t2h/ocr/MainActivity.kt`
- Impact: As features are added, the codebase will become difficult to maintain and test.
- Fix approach: Implement MVVM pattern with `ViewModel` and `Repository` layers.

**Boilerplate Code:**
- Issue: `MainActivity.kt` and tests are still in their initial boilerplate state from the Android Studio template.
- Files: `app/src/main/java/com/t2h/ocr/MainActivity.kt`, `app/src/test/java/com/t2h/ocr/ExampleUnitTest.kt`
- Impact: The project doesn't perform any of its intended OCR functionality.
- Fix approach: Replace boilerplate with actual business logic and meaningful tests.

## Known Bugs

**None detected:**
- Symptoms: N/A
- Files: N/A
- Trigger: N/A
- Workaround: N/A

## Security Considerations

**Firebase Configuration:**
- Risk: `google-services.json` is present in the repository. While this file is generally required for the app to function and doesn't contain "secrets" in the traditional sense, it does identify the project and can be used to interact with Firebase services if they are not properly restricted via Firebase Console rules.
- Files: `app/google-services.json`
- Current mitigation: None detected.
- Recommendations: Ensure Firebase Security Rules (Firestore, Storage) and App Check are configured to prevent unauthorized access.

## Performance Bottlenecks

**None detected:**
- Problem: The current codebase is minimal and has no heavy processing.
- Files: N/A
- Cause: N/A
- Improvement path: Monitor OCR processing time once ML Kit is implemented.

## Fragile Areas

**MainActivity.kt:**
- Files: `app/src/main/java/com/t2h/ocr/MainActivity.kt`
- Why fragile: It is currently the only entry point and logic container. It is at risk of becoming a "God Object."
- Safe modification: Refactor logic into separate components before adding more functionality.
- Test coverage: 0% real coverage.

## Scaling Limits

**Local Storage:**
- Current capacity: No local database (e.g., Room) is implemented.
- Limit: All data depends on Firebase or memory.
- Scaling path: Implement Room persistence if offline support or caching is needed.

## Dependencies at Risk

**ML Kit & Firebase Integration:**
- Risk: The project depends on specific versions of ML Kit and Firebase without a clear abstraction layer.
- Impact: Future updates to these SDKs might require extensive refactoring of UI-coupled code.
- Migration plan: Create an interface for OCR services to decouple implementation details from the UI.

## Missing Critical Features

**OCR Logic:**
- Problem: No implementation of ML Kit `TextRecognition`.
- Blocks: Core functionality of the app.

**Camera and Gallery Integration:**
- Problem: No UI or logic to capture images or select them from the gallery.
- Blocks: User ability to provide input for OCR.

**Authentication and Storage Implementation:**
- Problem: Firebase dependencies are added but not used.
- Blocks: Saving results or syncing data across devices.

## Test Coverage Gaps

**Business Logic:**
- What's not tested: There is no business logic to test.
- Files: `app/src/main/java/com/t2h/ocr/MainActivity.kt`
- Risk: New logic will likely be introduced without tests.
- Priority: High

**UI Testing:**
- What's not tested: Compose UI components.
- Files: `app/src/main/java/com/t2h/ocr/MainActivity.kt`
- Risk: UI regressions as the app grows.
- Priority: Medium

---

*Concerns audit: 2025-01-24*
