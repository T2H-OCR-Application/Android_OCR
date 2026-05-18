# Testing Patterns

**Analysis Date:** 2025-01-24

## Test Framework

**Runner:**
- JUnit 4
- `androidx.test.runner.AndroidJUnitRunner` for instrumented tests.

**Assertion Library:**
- `org.junit.Assert`

**Run Commands:**
```bash
./gradlew test         # Run local unit tests
./gradlew connectedAndroidTest # Run instrumented tests on device
```

## Test File Organization

**Location:**
- Local unit tests: `app/src/test/java/com/t2h/ocr/`
- Instrumented tests: `app/src/androidTest/java/com/t2h/ocr/`

**Naming:**
- Unit tests: `*UnitTest.kt` (e.g., `ExampleUnitTest.kt`)
- Instrumented tests: `*InstrumentedTest.kt` (e.g., `ExampleInstrumentedTest.kt`)

## Test Structure

**Suite Organization:**
```kotlin
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
```

**Patterns:**
- Standard `@Test` annotation.
- `@RunWith(AndroidJUnit4::class)` for instrumented tests in `app/src/androidTest/java/com/t2h/ocr/ExampleInstrumentedTest.kt`.

## Mocking

**Framework:** Not explicitly configured (e.g., Mockito or MockK are not in `libs.versions.toml`).

**What to Mock:**
- N/A

**What NOT to Mock:**
- N/A

## Fixtures and Factories

**Test Data:**
- No complex fixtures detected. Simple assertions in `ExampleUnitTest.kt`.

**Location:**
- Co-located in test classes.

## Coverage

**Requirements:** None enforced in `build.gradle.kts`.

**View Coverage:**
- Use Android Studio's "Run with Coverage" or add Jacoco plugin.

## Test Types

**Unit Tests:**
- Located in `app/src/test`.
- Execute on JVM.
- Used for logic that doesn't depend on Android framework.

**Integration Tests:**
- Located in `app/src/androidTest`.
- Execute on an Android device or emulator.
- Used for UI tests and Android framework integration.

**E2E Tests:**
- Likely handled by `androidTest` using Espresso/Compose UI Test.

## Common Patterns

**Async Testing:**
- Not observed in current tests.

**Error Testing:**
- Not observed in current tests.

---

*Testing analysis: 2025-01-24*
