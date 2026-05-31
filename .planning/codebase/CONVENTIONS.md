# Coding Conventions

**Analysis Date:** 2026-05-31

## Naming Patterns

**Files:**
- Kotlin source files: PascalCase (e.g., `MainActivity.kt`, `HomeViewModel.kt`).
- Layout files (if any): snake_case (e.g., `activity_main.xml`).
- Resource files: snake_case (e.g., `ic_launcher_background.xml`).

**Functions:**
- Standard functions: camelCase (e.g., `processAndOcr()`, `formatRelativeTime()`).
- Composable functions: PascalCase (e.g., `HomeScreen()`, `ResultsScreen()`).

**Variables:**
- Local variables and properties: camelCase (e.g., `authRepository`, `currentScreen`, `isProcessing`).
- Constants: SCREAMING_SNAKE_CASE (e.g., `SYNC_WIFI_ONLY`).

**Types:**
- Classes and Interfaces: PascalCase (e.g., `MainActivity`, `AuthRepository`).
- Sealed classes and objects: PascalCase (e.g., `Screen`, `PdfGenerator`).
- Enums: PascalCase (e.g., `SyncStatus`).

## Code Style

**Formatting:**
- Kotlin official code style: `kotlin.code.style=official` in `gradle.properties`.
- Indentation: 4 spaces.

**Linting:**
- Default Android Studio inspections.
- Compose-specific inspections enabled in `.idea/inspectionProfiles/Project_Default.xml`.

## Import Organization

**Order:**
1. Android/AndroidX imports.
2. Google/Third-party libraries.
3. Project-specific imports (`com.t2h.ocr.*`).
4. Kotlin/Java standard libraries.

**Path Aliases:**
- Not observed. Standard package imports are used.

## Error Handling

**Patterns:**
- `try-catch` blocks are used for operations that might fail (I/O, network, OCR).
- `e.printStackTrace()` or `Log.e()` for logging errors.
- Examples:
  - `app/src/main/java/com/t2h/ocr/MainActivity.kt`: `try { File(imagePath).delete() } catch (e: Exception) {}`
  - `app/src/main/java/com/t2h/ocr/data/local/UserPreferences.kt`: `try { ... } catch (e: Exception) { e.printStackTrace() }`

## Logging

**Framework:** `android.util.Log`

**Patterns:**
- `Log.d` for debug information (e.g., initialization success).
- `Log.e` for errors (e.g., initialization failure).
- Tagging: Usually uses the class name as the tag.

## Comments

**When to Comment:**
- Classes and major functions should have KDoc descriptions.
- Complex logic steps (e.g., OCR processing, PDF generation) are commented inline.

**JSDoc/TSDoc:**
- KDoc is used for Kotlin files.
- Example: `app/src/main/java/com/t2h/ocr/domain/ocr/PdfGenerator.kt`
  ```kotlin
  /**
   * Generates PDFs from OCR results.
   */
  object PdfGenerator { ... }
  ```

## Function Design

**Size:** Functions are generally concise, but some lifecycle or setup methods in Activities/Composables can be larger.

**Parameters:** Standard parameter passing. Use of default values where appropriate.

**Return Values:** Standard return values or `Pair`/`Triple` for multiple values in internal methods.

## Module Design

**Exports:** Public classes and functions are exported normally.

**Barrel Files:** Not applicable in Kotlin/Android. Package structure is used for organization.

**Design Patterns:**
- **MVVM**: ViewModels (`HomeViewModel`, `ScannerViewModel`) handle UI state and logic.
- **Repository Pattern**: Data access is abstracted through repositories (`AuthRepository`, `ScanRepository`).
- **Singleton**: Observed in `ScanRepository.getInstance(context)` and `object` declarations like `PdfGenerator`.

---

*Convention analysis: 2026-05-31*
