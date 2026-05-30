# Codebase Structure

**Analysis Date:** 2026-05-31

## Directory Layout

```
[project-root]/
├── app/                    # Main Android application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/t2h/ocr/
│   │   │   │   ├── data/   # Data layer (Repositories, Storage, Sync)
│   │   │   │   ├── domain/ # Domain layer (OCR, Image Processing, Logic)
│   │   │   │   ├── ui/     # UI layer (Screens, ViewModels, Components)
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/        # Android resources (drawables, values, etc.)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/           # Local unit tests
│   │   └── androidTest/    # Instrumental tests
│   ├── build.gradle.kts
│   └── google-services.json
├── gradle/                 # Gradle wrapper and version catalog
├── build.gradle.kts        # Root build config
├── settings.gradle.kts     # Project settings
└── .planning/              # Project documentation and roadmap
```

## Directory Purposes

**com.t2h.ocr.data:**
- Purpose: Data fetching, persistence, and synchronization.
- Contains: Repositories, Room entities/DAOs, DataStore preferences, Firebase integrations, WorkManager sync logic.
- Key files: `ScanRepository.kt`, `data/auth/AuthRepository.kt`, `data/sync/SyncWorker.kt`.

**com.t2h.ocr.domain:**
- Purpose: Business logic and pure processing logic.
- Contains: OCR engine wrappers, OpenCV image processing logic, PDF generation logic, Analytics helpers.
- Key files: `domain/ocr/ImageProcessor.kt`, `domain/ocr/PdfGenerator.kt`, `domain/observability/AnalyticsHelper.kt`.

**com.t2h.ocr.ui:**
- Purpose: User interface and UI state management.
- Contains: Jetpack Compose screens, ViewModels, shared components, and themes.
- Key files: `ui/home/HomeScreen.kt`, `ui/scanner/ScannerViewModel.kt`, `ui/theme/Theme.kt`.

**app/src/test:**
- Purpose: Unit testing for logic that doesn't require Android framework.
- Contains: ViewModel tests, Repository tests, Processor tests.

## Key File Locations

**Entry Points:**
- `app/src/main/java/com/t2h/ocr/MainActivity.kt`: Main entry point and navigation host.

**Configuration:**
- `gradle/libs.versions.toml`: Version catalog for all dependencies.
- `app/build.gradle.kts`: App-level build configuration.
- `app/src/main/res/values/strings.xml`: Localized strings.

**Core Logic:**
- `app/src/main/java/com/t2h/ocr/domain/ocr/ImageProcessor.kt`: OpenCV warping logic.
- `app/src/main/java/com/t2h/ocr/domain/ocr/PdfGenerator.kt`: Searchable PDF creation.

**Testing:**
- `app/src/test/java/com/t2h/ocr/`: Unit tests.
- `app/src/androidTest/java/com/t2h/ocr/`: Instrumental tests (Camera, Storage).

## Naming Conventions

**Files:**
- Composable Screens: `[Feature]Screen.kt` (e.g., `HomeScreen.kt`)
- ViewModels: `[Feature]ViewModel.kt` (e.g., `ScannerViewModel.kt`)
- Repositories: `[Data]Repository.kt` (e.g., `AuthRepository.kt`)

**Directories:**
- Feature-based packaging within `ui/` layer.
- Layer-based packaging at the top level (`data`, `domain`, `ui`).

## Where to Add New Code

**New Feature:**
- UI & ViewModel: `app/src/main/java/com/t2h/ocr/ui/[feature_name]/`
- Business Logic: `app/src/main/java/com/t2h/ocr/domain/[domain_area]/`
- Data/Persistence: `app/src/main/java/com/t2h/ocr/data/` (or sub-package)
- Tests: `app/src/test/java/com/t2h/ocr/ui/[feature_name]/`

**New Component/Module:**
- Implementation: `app/src/main/java/com/t2h/ocr/ui/components/`

**Utilities:**
- Shared helpers: `app/src/main/java/com/t2h/ocr/domain/` or specialized util package.

## Special Directories

**app/src/test/snapshots:**
- Purpose: Contains Roborazzi screenshot test outputs.
- Generated: Yes.
- Committed: Yes (for baseline comparison).

**app/build/ (ignored):**
- Purpose: Compiled artifacts and build outputs.
- Generated: Yes.
- Committed: No.

---

*Structure analysis: 2026-05-31*
