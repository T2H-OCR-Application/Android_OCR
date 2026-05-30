# Testing Patterns

**Analysis Date:** 2026-05-31

## Test Framework

**Runner:**
- Unit Tests: JUnit 4, Robolectric.
- Instrumented Tests: `AndroidJUnitRunner` (configured in `app/build.gradle.kts`).

**Assertion Library:**
- JUnit Assert (e.g., `assertEquals`, `assertTrue`).
- Compose UI Test assertions (e.g., `assertIsDisplayed()`).

**Run Commands:**
```bash
./gradlew test         # Run all unit tests
./gradlew connectedCheck # Run all instrumented tests
./gradlew recordRoborazziDebug # Record snapshots
./gradlew verifyRoborazziDebug # Verify snapshots
```

## Test File Organization

**Location:**
- Unit tests: `app/src/test/java/com/t2h/ocr/`.
- Instrumented tests: `app/src/androidTest/java/com/t2h/ocr/`.
- Snapshots: `app/src/test/snapshots/` (configured via `roborazzi` plugin).

**Naming:**
- PascalCase with `Test` suffix (e.g., `HomeViewModelTest.kt`, `HomeScreenTest.kt`).

**Structure:**
```
app/src/test/
├── java/com/t2h/ocr/
│   ├── data/
│   ├── domain/
│   └── ui/
└── snapshots/
app/src/androidTest/
└── java/com/t2h/ocr/
```

## Test Structure

**Suite Organization:**
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Setup mocks and ViewModel
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `descriptive test name using backticks`() = runTest {
        // Given
        // When
        // Then
    }
}
```

**Patterns:**
- **Setup pattern:** `@Before` method to initialize mocks and the class under test.
- **Teardown pattern:** `@After` method to reset main dispatcher for coroutines.
- **Assertion pattern:** Use of `runTest` for coroutine testing and `testDispatcher.scheduler.advanceUntilIdle()` for timing control.

## Mocking

**Framework:** MockK (`io.mockk`)

**Patterns:**
```kotlin
// Creating a mock
val fakeRepository = mockk<ScanRepository>(relaxed = true)

// Stubbing behavior
every { fakeRepository.scans } returns flowOf(listOf(...))

// Verification
verify { fakeRepository.deleteScan(any()) }
```

**What to Mock:**
- External dependencies (Repositories, WorkManager, Firebase).
- Android components that require a context (unless using Robolectric).

**What NOT to Mock:**
- Plain data models (`ScanMetadata`, `ScannedPage`).
- Utility functions or objects that don't have side effects.

## Fixtures and Factories

**Test Data:**
- Manual instantiation in tests.
- Example:
```kotlin
val scans = listOf(
    ScanMetadata(id = "1", title = "Scan 1", ocrText = "Text 1"),
    ScanMetadata(id = "2", title = "Scan 2", ocrText = "Text 2")
)
```

**Location:**
- Often defined within the test class or as helper functions in the same package.

## Coverage

**Requirements:** None explicitly enforced in `build.gradle.kts`.

**View Coverage:**
```bash
./gradlew testDebugUnitTestCoverage # (if jacoco is configured, not observed here)
```

## Test Types

**Unit Tests:**
- Test ViewModels, Repositories, and domain logic.
- Located in `app/src/test`.

**Integration Tests:**
- Test flows like `ScanFlowTest` in `app/src/androidTest`.
- WorkManager tests using `work-testing`.

**E2E Tests:**
- Not explicitly labeled as E2E, but `ScanFlowTest` covers significant parts of the user flow.

**Snapshot Tests:**
- Roborazzi is used for UI snapshot testing.
- Located in `app/src/test/java/com/t2h/ocr/ui/SnapshotTests.kt`.
- Uses Robolectric to run in the unit test environment.

## Common Patterns

**Async Testing:**
```kotlin
@Test
fun testAsyncOperation() = runTest {
    // ...
    testDispatcher.scheduler.advanceUntilIdle()
    // assertions
}
```

**Error Testing:**
```kotlin
@Test(expected = Exception::class)
fun testError() {
    // operation that should throw
}
// OR
@Test
fun testErrorState() = runTest {
    every { repository.doSomething() } throws Exception()
    viewModel.action()
    assertEquals(ExpectedError, viewModel.uiState.value.error)
}
```

---

*Testing analysis: 2026-05-31*
