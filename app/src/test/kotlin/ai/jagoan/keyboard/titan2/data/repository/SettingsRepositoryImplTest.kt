/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.data.repository to ai.jagoan.keyboard.titan2.data.repository
 * - Updated all package imports for data, domain, and datastore layers
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
 *
 */

package ai.jagoan.keyboard.titan2.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import ai.jagoan.keyboard.titan2.domain.model.KeyboardSettings
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import app.cash.turbine.test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Example unit test for SettingsRepository
 * Demonstrates modern testing with JUnit 5, MockK, and Coroutines Test
 */
class SettingsRepositoryImplTest {

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepositoryImpl

    @BeforeEach
    fun setup() {
        dataStore = mockk(relaxed = true)
        // Set up the mock before creating the repository since settingsFlow is initialized eagerly
        every { dataStore.data } returns flowOf(emptyPreferences())
        repository = SettingsRepositoryImpl(dataStore)
    }

    @Test
    fun `settingsFlow emits default settings when preferences are empty`() = runTest {
        // Given - mock is already set up in @BeforeEach

        // When & Then
        repository.settingsFlow.test {
            val settings = awaitItem()
            
            val expected = KeyboardSettings(
                autoCapitalize = false,
                keyRepeatEnabled = false,
                longPressCapitalize = true,
                doubleSpacePeriod = true,
                textShortcutsEnabled = true,
                stickyShift = true,
                stickyAlt = true,
                altBackspaceDeleteLine = true,
                keyRepeatDelay = 400L,
                keyRepeatRate = 50L,
                preferredCurrency = "Rp",
                selectedLanguage = "en",
                longPressAccents = false
            )
            assertEquals(expected, settings)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
