# Codebase Structure

**Analysis Date:** 2025-01-16

## Directory Layout

```
[project-root]/
├── app/                  # Main application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/     # Kotlin source code
│   │   │   ├── res/      # Android resources (XML, drawables)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/         # Local unit tests
│   │   └── androidTest/  # Instrumented tests
│   ├── build.gradle.kts  # Module-level build config
│   └── google-services.json # Firebase configuration
├── gradle/               # Gradle wrapper and version catalog
│   └── libs.versions.toml # Dependency version management
├── build.gradle.kts      # Project-level build config
└── settings.gradle.kts   # Project settings and module inclusion
```

## Directory Purposes

**app/src/main/java/com/t2h/ocr:**
- Purpose: Primary package for the application.
- Contains: `MainActivity.kt` and sub-packages for UI, data, etc.
- Key files: `MainActivity.kt`

**app/src/main/java/com/t2h/ocr/ui/theme:**
- Purpose: Jetpack Compose theme definitions.
- Contains: Color, Type, and Theme Kotlin files.
- Key files: `Theme.kt`, `Color.kt`, `Type.kt`

**app/src/main/res:**
- Purpose: Static resources for the Android app.
- Contains: Layouts (though minimal due to Compose), drawables, mipmaps (icons), and strings.
- Key files: `values/strings.xml`, `values/themes.xml`

## Key File Locations

**Entry Points:**
- `app/src/main/java/com/t2h/ocr/MainActivity.kt`: Initial Activity launched by the OS.

**Configuration:**
- `app/build.gradle.kts`: Dependencies, SDK versions, and build features (Compose).
- `gradle/libs.versions.toml`: Centralized versioning for all dependencies.
- `app/google-services.json`: Firebase project configuration.

**Core Logic:**
- Currently centralized in `MainActivity.kt` (minimal).

**Testing:**
- `app/src/test/java/com/t2h/ocr/ExampleUnitTest.kt`: Unit tests for non-Android logic.
- `app/src/androidTest/java/com/t2h/ocr/ExampleInstrumentedTest.kt`: UI/Integration tests requiring an Android device.

## Naming Conventions

**Files:**
- Kotlin Classes: PascalCase (`MainActivity.kt`)
- Composable Functions: PascalCase (`Greeting`)
- Resource Files: snake_case (`ic_launcher_background.xml`)

**Directories:**
- Packages: Lowercase with dots (`com.t2h.ocr`)
- Resources: Lowercase with underscores (`drawable`, `values`)

## Where to Add New Code

**New Feature:**
- Primary UI: `app/src/main/java/com/t2h/ocr/ui/features/[feature_name]/`
- ViewModel: `app/src/main/java/com/t2h/ocr/ui/viewmodel/`
- Tests: `app/src/test/java/com/t2h/ocr/`

**New Component/Module:**
- Shared Composable: `app/src/main/java/com/t2h/ocr/ui/components/`
- OCR Logic: `app/src/main/java/com/t2h/ocr/data/ocr/`
- Firebase Service: `app/src/main/java/com/t2h/ocr/data/firebase/`

**Utilities:**
- Shared helpers: `app/src/main/java/com/t2h/ocr/util/`

## Special Directories

**app/src/main/res/xml:**
- Purpose: XML configuration files for backup and data extraction rules.
- Generated: No
- Committed: Yes

---

*Structure analysis: 2025-01-16*
