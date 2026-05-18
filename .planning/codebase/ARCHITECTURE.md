<!-- refreshed: 2025-01-16 -->
# Architecture

**Analysis Date:** 2025-01-16

## System Overview

```text
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                     │
├─────────────────────────────────────────────────────────────┤
│   UI Components (Compose)       │       Activity (Entry)    │
│  `app/src/main/java/.../ui`     │      `MainActivity.kt`    │
└────────┬───────────────────────────────────┬────────────────┘
         │                                   │
         ▼                                   ▼
┌─────────────────────────────────────────────────────────────┐
│                    External Services / SDKs                 │
├─────────────────────────────────────────────────────────────┤
│       ML Kit (OCR)         │          Firebase              │
│  (Google ML Kit SDK)       │  (Auth, Firestore, Storage)    │
└─────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| `MainActivity` | Main entry point, hosts the Jetpack Compose UI content. | `app/src/main/java/com/t2h/ocr/MainActivity.kt` |
| `FrontendAndroidOCRTheme` | Defines the Material 3 color scheme, typography, and theme for the app. | `app/src/main/java/com/t2h/ocr/ui/theme/Theme.kt` |
| `AndroidManifest` | App configuration, permissions, and component declaration. | `app/src/main/AndroidManifest.xml` |

## Pattern Overview

**Overall:** Standard Android Development with Jetpack Compose.

**Key Characteristics:**
- **Declarative UI:** Uses Jetpack Compose for building the user interface.
- **Service Integration:** Relies on Google ML Kit for OCR and Firebase for backend services.
- **Material 3:** Follows Material Design 3 guidelines for UI components.

## Layers

**Presentation Layer:**
- Purpose: Handles user interaction and displays information.
- Location: `app/src/main/java/com/t2h/ocr/ui/` and `MainActivity.kt`.
- Contains: Composable functions, Themes, and Activities.
- Depends on: Jetpack Compose libraries, Android Framework.
- Used by: End users.

**Data/External Layer:**
- Purpose: Provides specialized services like text recognition and cloud storage.
- Location: Integrated via SDKs in `app/build.gradle.kts`.
- Contains: ML Kit and Firebase SDKs.
- Depends on: Google Play Services.
- Used by: Presentation layer (intended).

## Data Flow

### Primary Request Path (Anticipated)

1. **User Action:** User captures or selects an image in the UI.
2. **Processing:** UI passes the image to ML Kit for text recognition.
3. **Result:** Recognized text is returned to the UI for display or further processing.
4. **Storage:** Text or image data is optionally saved to Firebase Firestore/Storage.

**State Management:**
- Currently managed within Composable functions using `remember` and `mutableStateOf` (minimal state exists currently).

## Key Abstractions

**Theme:**
- Purpose: Centralizes styling (colors, types) for the entire application.
- Examples: `app/src/main/java/com/t2h/ocr/ui/theme/`
- Pattern: Material 3 Theme wrapping.

## Entry Points

**MainActivity:**
- Location: `app/src/main/java/com/t2h/ocr/MainActivity.kt`
- Triggers: System launch.
- Responsibilities: Initializes the app, sets up edge-to-edge display, and provides the root Composable content.

## Architectural Constraints

- **Threading:** Android main thread for UI; ML Kit and Firebase operations are typically asynchronous/off-loaded to background threads.
- **Global state:** No significant global state implemented yet.
- **Permissions:** Requires Camera/Storage permissions (to be implemented in `AndroidManifest.xml`).

## Anti-Patterns

### Logic in Activity

**What happens:** Placing business logic or OCR processing directly in `MainActivity.kt`.
**Why it's wrong:** Makes the code hard to test and maintain; violates separation of concerns.
**Do this instead:** Use ViewModels and Repository patterns to encapsulate logic.

## Error Handling

**Strategy:** Not yet explicitly defined in the codebase.

**Patterns:**
- Standard Kotlin try-catch for synchronous operations.
- Firebase/ML Kit listeners/callbacks for asynchronous operations.

## Cross-Cutting Concerns

**Logging:** Uses standard Android `Log` (not explicitly seen in sample code yet).
**Authentication:** Firebase Auth is integrated but not yet implemented in UI.

---

*Architecture analysis: 2025-01-16*
