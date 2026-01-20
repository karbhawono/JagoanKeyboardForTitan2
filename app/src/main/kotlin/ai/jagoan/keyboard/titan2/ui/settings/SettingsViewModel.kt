/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.ui.settings to ai.jagoan.keyboard.titan2.ui.settings
 * - Updated domain repository and model imports
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

package ai.jagoan.keyboard.titan2.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.jagoan.keyboard.titan2.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settingsState: StateFlow<SettingsUiState> = settingsRepository
        .settingsFlow
        .map<ai.jagoan.keyboard.titan2.domain.model.KeyboardSettings, SettingsUiState> { settings ->
            SettingsUiState.Success(settings)
        }
        .catch { exception ->
            emit(SettingsUiState.Error(exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState.Loading
        )

    fun updateAutoCapitalize(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSetting("autoCapitalize", enabled)
        }
    }

    fun updateKeyRepeat(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSetting("keyRepeatEnabled", enabled)
        }
    }

    fun updateLongPressCapitalize(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSetting("longPressCapitalize", enabled)
        }
    }

    fun updateDoubleSpacePeriod(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSetting("doubleSpacePeriod", enabled)
        }
    }

    fun updateTextShortcuts(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSetting("textShortcutsEnabled", enabled)
        }
    }

    fun updateStickyShift(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSetting("stickyShift", enabled)
        }
    }

    fun updateStickyAlt(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSetting("stickyAlt", enabled)
        }
    }

    fun updateAltBackspaceDeleteLine(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSetting("altBackspaceDeleteLine", enabled)
        }
    }

    fun updatePreferredCurrency(currency: String?) {
        viewModelScope.launch {
            settingsRepository.updateSetting("preferredCurrency", currency)
        }
    }

    fun updateSelectedLanguage(language: String) {
        viewModelScope.launch {
            settingsRepository.updateSetting("selectedLanguage", language)
        }
    }

    fun updateLongPressAccents(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSetting("longPressAccents", enabled)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            settingsRepository.resetToDefaults()
        }
    }
}
