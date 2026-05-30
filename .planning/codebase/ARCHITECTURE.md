<!-- refreshed: 2026-05-31 -->
# Architecture

**Analysis Date:** 2026-05-31

## System Overview

```text
┌─────────────────────────────────────────────────────────────┐
│                      UI Layer (Compose)                     │
├──────────────────┬──────────────────┬───────────────────────┤
│   Screens        │   ViewModels     │    Components         │
│  `ui/*.kt`       │  `ui/**/*VM.kt`  │   `ui/components/`    │
└────────┬─────────┴────────┬─────────┴──────────┬────────────┘
         │                  │                     │
         ▼                  ▼                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer (Logic)                      │
│         `domain/ocr/`, `domain/observability/`               │
└─────────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────┐
│  Data Layer (Persistence & Sync)                             │
│  `data/local/`, `data/auth/`, `data/sync/`                   │
└─────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| MainActivity | Entry point, Navigation, Global State | `MainActivity.kt` |
| ScanRepository | Single source of truth for scan data | `data/ScanRepository.kt` |
| AuthRepository | Manages Firebase Authentication | `data/auth/AuthRepository.kt` |
| ImageProcessor | OpenCV-based image manipulation (warping) | `domain/ocr/ImageProcessor.kt` |
| DocumentAnalyzer | Orchestrates OCR via ML Kit | `domain/ocr/DocumentAnalyzer.kt` |
| PdfGenerator | Creates searchable PDFs from scans | `domain/ocr/PdfGenerator.kt` |
| ScannerViewModel | Manages camera state and capture flow | `ui/scanner/ScannerViewModel.kt` |

## Pattern Overview

**Overall:** MVVM (Model-View-ViewModel) with Clean Architecture principles.

**Key Characteristics:**
- **Layered Architecture:** Clear separation between UI, Domain logic, and Data persistence.
- **Unidirectional Data Flow:** State flows down from ViewModels to Composables; events flow up.
- **Single Activity:** `MainActivity` acts as the host and navigation controller using manual state management.

## Layers

**UI Layer:**
- Purpose: Renders the user interface and handles user interactions.
- Location: `app/src/main/java/com/t2h/ocr/ui`
- Contains: Jetpack Compose screens, ViewModels, and reusable UI components.
- Depends on: Domain Layer, Data Layer (via Repositories).
- Used by: Android System.

**Domain Layer:**
- Purpose: Contains core business logic and pure processing logic.
- Location: `app/src/main/java/com/t2h/ocr/domain`
- Contains: OCR logic, Image processing (OpenCV), PDF generation, Analytics helpers.
- Depends on: None (ideally) or Android-specific APIs for processing.
- Used by: UI Layer (ViewModels).

**Data Layer:**
- Purpose: Handles data persistence and remote synchronization.
- Location: `app/src/main/java/com/t2h/ocr/data`
- Contains: Repositories, Room database (implied), DataStore, Firebase integration, Google Drive sync logic.
- Depends on: External libraries (Firebase, Google API).
- Used by: UI Layer (ViewModels), MainActivity.

## Data Flow

### Primary Request Path (Scanning Flow)

1. **Capture:** `ScannerScreen` triggers capture in `ScannerViewModel` (`ui/scanner/ScannerScreen.kt`).
2. **Crop:** User adjusts boundaries in `CropScreen`; `MainActivity` calls `processAndOcr` (`MainActivity.kt:387`).
3. **Processing:** `ImageProcessor.warpPerspective` applies OpenCV transformations (`domain/ocr/ImageProcessor.kt`).
4. **OCR:** `TextRecognition` extracts text from the warped image (`MainActivity.kt:403`).
5. **Results:** `ResultsViewModel` saves metadata via `ScanRepository` (`ui/results/ResultsViewModel.kt`).
6. **Sync:** `SyncWorker` (WorkManager) uploads files to Google Drive/Firestore in the background (`data/sync/SyncWorker.kt`).

### Authentication Flow

1. **Auto-Login:** `MainActivity` calls `authRepository.signInAnonymously()` on startup.
2. **Link Account:** `ProfileScreen` triggers Google Sign-In via `AuthRepository`.
3. **State Updates:** UI reacts to `currentUser` Flow from `AuthRepository`.

**State Management:**
- State is managed within ViewModels using `MutableStateFlow`.
- `MainActivity` holds the top-level `currentScreen` state for navigation.
- Persistent state is stored in `ScanRepository` (backed by `ScanStorage`/Room).

## Key Abstractions

**Repository:**
- Purpose: Abstracts data sources for the rest of the app.
- Examples: `data/ScanRepository.kt`, `data/auth/AuthRepository.kt`
- Pattern: Repository Pattern.

**ViewModel:**
- Purpose: Bridges the UI and Data/Domain layers, maintaining UI state across configuration changes.
- Examples: `ui/home/HomeViewModel.kt`, `ui/scanner/ScannerViewModel.kt`
- Pattern: MVVM ViewModel.

## Entry Points

**MainActivity:**
- Location: `app/src/main/java/com/t2h/ocr/MainActivity.kt`
- Triggers: App Launch.
- Responsibilities: Initializes OpenCV, Firebase; sets up Compose content; manages navigation and permissions.

## Architectural Constraints

- **Threading:** Heavy operations (OCR, Image Processing, PDF Gen) MUST run on `Dispatchers.IO` or `Dispatchers.Default` to avoid blocking the UI thread.
- **Global state:** `ScanRepository` is accessed as a singleton via `getInstance(context)`.
- **Manual DI:** Dependency injection is handled manually via `ViewModelFactory` classes or direct instantiation in `MainActivity`.

## Anti-Patterns

### Logic in MainActivity

**What happens:** Business logic or navigation logic growing too large in `MainActivity.kt`.
**Why it's wrong:** Violates Single Responsibility Principle; makes testing difficult.
**Do this instead:** Move logic to dedicated ViewModels or UseCases in the Domain layer.

### Manual Navigation State

**What happens:** Using a `sealed class Screen` and `mutableStateOf` for navigation instead of Jetpack Navigation Component.
**Why it's wrong:** Harder to manage deep links and complex backstack behaviors.
**Do this instead:** Consider migrating to `androidx.navigation:navigation-compose` if complexity increases.

## Error Handling

**Strategy:** Result-based handling and localized UI feedback.

**Patterns:**
- Try-catch blocks in Coroutines with logging to `AnalyticsHelper`.
- State-based error messages (e.g., `isError` flag in UI state).

## Cross-Cutting Concerns

**Logging:** Handled by `AnalyticsHelper` and `Log` class.
**Validation:** Local validation in ViewModels before repository calls.
**Authentication:** Centralized in `AuthRepository` using Firebase Auth.

---

*Architecture analysis: 2026-05-31*
