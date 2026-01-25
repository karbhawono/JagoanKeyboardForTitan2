/**
 * Copyright (c) 2025 Aryo Karbhawono
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.jagoan.keyboard.titan2.ui.dictionary

import ai.jagoan.keyboard.titan2.domain.repository.DictionaryRepository
import android.util.Log
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Basic tests for DictionaryManagementViewModel.
 * 
 * Note: Full state testing is challenging with the current architecture because
 * the ViewModel uses viewModelScope which is difficult to control in unit tests.
 * For comprehensive testing, consider:
 * - Injecting a CoroutineDispatcher into the ViewModel
 * - Using integration/UI tests instead of unit tests
 * - Testing the repository layer thoroughly (which we do)
 */
@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("DictionaryManagementViewModel Basic Tests")
class DictionaryManagementViewModelTest {

    private lateinit var repository: DictionaryRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        // Mock Android Log class
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.i(any(), any<String>()) } returns 0
        every { Log.v(any(), any<String>()) } returns 0
        
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        coEvery { repository.getAllCustomWordsByLanguage() } returns emptyMap()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("ViewModel can be instantiated")
    fun viewModelCanBeInstantiated() = runTest {
        val viewModel = DictionaryManagementViewModel(repository)
        
        assertNotNull(viewModel)
        assertNotNull(viewModel.uiState)
        assertNotNull(viewModel.exportState)
        assertNotNull(viewModel.importState)
    }

    @Test
    @DisplayName("ViewModel methods can be called without exceptions")
    fun viewModelMethodsDoNotThrowExceptions() = runTest {
        val viewModel = DictionaryManagementViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // These should not throw exceptions
        viewModel.loadCustomWords()
        viewModel.exportCustomWords()
        viewModel.resetExportState()
        viewModel.resetImportState()
        
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
