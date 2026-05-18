# Coding Conventions

**Analysis Date:** 2025-01-24

## Naming Patterns

**Files:**
- Kotlin files use PascalCase: `MainActivity.kt`, `Theme.kt`, `Color.kt`.

**Functions:**
- Composable functions use PascalCase: `Greeting`, `FrontendAndroidOCRTheme`.
- Standard functions use camelCase: `onCreate`.

**Variables:**
- Local variables and parameters use camelCase: `savedInstanceState`, `innerPadding`, `darkTheme`.
- Constants (top-level private) use PascalCase in theme files: `DarkColorScheme`, `LightColorScheme`, `Typography`.

**Types:**
- Classes and Interfaces use PascalCase: `MainActivity`.

## Code Style

**Formatting:**
- Follows standard Kotlin coding conventions (JetBrains/Google style).
- Uses 4 spaces for indentation.

**Linting:**
- No explicit linting configuration (like `detekt` or `ktlint`) detected in the project root or app module.
- Relies on default Android Studio / Kotlin IDE inspections.

## Import Organization

**Order:**
1. Standard library imports (e.g., `android.*`)
2. Framework imports (e.g., `androidx.*`)
3. Project-specific imports (e.g., `com.t2h.ocr.*`)

**Path Aliases:**
- Not detected.

## Error Handling

**Patterns:**
- Standard Kotlin try-catch usage (none observed in scaffold code).
- Compose-specific state handling for UI errors.

## Logging

**Framework:** `android.util.Log` (standard Android logging).

**Patterns:**
- Not explicitly used in the entry point `MainActivity.kt`.

## Comments

**When to Comment:**
- Minimal commenting in scaffolded code.
- KDoc style for classes/complex methods (observed in `ExampleUnitTest.kt`).

**JSDoc/TSDoc:**
- N/A (Project is Kotlin-based).

## Function Design

**Size:** Standard small functions (e.g., `Greeting`).

**Parameters:** Uses named parameters where clarity is needed, common in Compose.

**Return Values:** Usually `Unit` for Composables.

## Module Design

**Exports:** Standard Kotlin visibility (default is public).

**Barrel Files:** Not used (Kotlin uses package-level imports).

---

*Convention analysis: 2025-01-24*
