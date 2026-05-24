package com.t2h.ocr.ui.home

import androidx.work.WorkManager
import com.t2h.ocr.data.ScanRepository
import com.t2h.ocr.data.models.ScanMetadata
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeScanRepository: ScanRepository
    private val workManager = mockk<WorkManager>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // ScanRepository has a private constructor, we need to mock it or use a spy if possible,
        // but since we want to test HomeViewModel, let's mock ScanRepository.
        fakeScanRepository = mockk(relaxed = true)
        
        viewModel = HomeViewModel(fakeScanRepository, workManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filteredScans returns all scans when query is empty`() = runTest {
        val scans = listOf(
            ScanMetadata(id = "1", title = "Scan 1", ocrText = "Text 1"),
            ScanMetadata(id = "2", title = "Scan 2", ocrText = "Text 2")
        )
        every { fakeScanRepository.scans } returns flowOf(scans)

        // Re-instantiate to pick up the flow
        viewModel = HomeViewModel(fakeScanRepository, workManager)
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.filteredScans.first()
        assertEquals(2, filtered.size)
    }

    @Test
    fun `filteredScans filters by title`() = runTest {
        val scans = listOf(
            ScanMetadata(id = "1", title = "Apple", ocrText = "Text 1"),
            ScanMetadata(id = "2", title = "Banana", ocrText = "Text 2")
        )
        every { fakeScanRepository.scans } returns flowOf(scans)

        viewModel = HomeViewModel(fakeScanRepository, workManager)
        viewModel.onSearchQueryChanged("app")
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.filteredScans.value
        assertEquals(1, filtered.size)
        assertEquals("Apple", filtered[0].title)
    }

    @Test
    fun `filteredScans filters by ocrText`() = runTest {
        val scans = listOf(
            ScanMetadata(id = "1", title = "Scan 1", ocrText = "Hello World"),
            ScanMetadata(id = "2", title = "Scan 2", ocrText = "Goodbye")
        )
        every { fakeScanRepository.scans } returns flowOf(scans)

        viewModel = HomeViewModel(fakeScanRepository, workManager)
        viewModel.onSearchQueryChanged("world")
        testDispatcher.scheduler.advanceUntilIdle()

        val filtered = viewModel.filteredScans.value
        assertEquals(1, filtered.size)
        assertEquals("Scan 1", filtered[0].title)
    }
}
