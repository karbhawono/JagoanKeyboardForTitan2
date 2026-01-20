/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.domain.repository to ai.jagoan.keyboard.titan2.domain.repository
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

package ai.jagoan.keyboard.titan2.domain.repository

import ai.jagoan.keyboard.titan2.domain.model.KeyboardSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for keyboard settings
 */
interface SettingsRepository {
    /**
     * Flow of keyboard settings that emits whenever settings change
     */
    val settingsFlow: Flow<KeyboardSettings>

    /**
     * Update a specific setting
     * @param key The setting key
     * @param value The new value
     */
    suspend fun updateSetting(key: String, value: Any?)

    /**
     * Reset all settings to defaults
     */
    suspend fun resetToDefaults()
}
