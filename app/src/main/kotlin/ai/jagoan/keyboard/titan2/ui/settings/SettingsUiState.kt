/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.ui.settings to ai.jagoan.keyboard.titan2.ui.settings
 * - Updated domain model imports
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

import ai.jagoan.keyboard.titan2.domain.model.KeyboardSettings

/**
 * UI state for settings screen
 */
sealed class SettingsUiState {
    /**
     * Loading state
     */
    data object Loading : SettingsUiState()

    /**
     * Success state with settings data
     */
    data class Success(val settings: KeyboardSettings) : SettingsUiState()

    /**
     * Error state
     */
    data class Error(val message: String) : SettingsUiState()
}
