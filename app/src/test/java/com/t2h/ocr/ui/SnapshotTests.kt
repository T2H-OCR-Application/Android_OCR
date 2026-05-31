package com.t2h.ocr.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.t2h.ocr.ui.home.HomeScreen
import com.t2h.ocr.ui.scanner.ScannerScreen
import com.t2h.ocr.ui.scanner.ScannerViewModel
import com.t2h.ocr.ui.results.ResultsScreen
import com.t2h.ocr.ui.results.ResultsViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "roboscreenshots")
class SnapshotTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun captureHomeScreen() {
        // HomeScreen signature đã thay đổi — test đơn giản bằng cách mock data
        composeTestRule.setContent {
            HomeScreen(
                recentHistory = emptyList(),
                onNavigateToSection = {},
                onCenterFabClick = {},
                onRecentItemClick = {},
                onRecentItemDelete = {}
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun captureScannerScreen() {
        val viewModel = mockk<ScannerViewModel>(relaxed = true)
        every { viewModel.scannedPages } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            ScannerScreen(
                viewModel = viewModel,
                onDocumentCaptured = { _, _ -> },
                onProfileClick = {},
                onGalleryClick = {},
                onBackClick = {}
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun captureResultsScreen() {
        val viewModel = mockk<ResultsViewModel>(relaxed = true)
        every { viewModel.isSaving } returns MutableStateFlow(false)
        every { viewModel.errorState } returns MutableStateFlow(null)

        composeTestRule.setContent {
            ResultsScreen(
                viewModel = viewModel,
                pages = listOf("Recognized test text."),
                imagePath = "fake/path",
                allImagePaths = listOf("fake/path"),
                onSaveComplete = {},
                onBackClick = {}
            )
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}
