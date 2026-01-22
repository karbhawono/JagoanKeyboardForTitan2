/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.data.repository to ai.jagoan.keyboard.titan2.data.repository
 * - Updated datastore and domain package imports
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
import androidx.datastore.preferences.core.edit
import ai.jagoan.keyboard.titan2.data.datastore.PreferencesKeys
import ai.jagoan.keyboard.titan2.domain.model.KeyboardSettings
import ai.jagoan.keyboard.titan2.domain.model.SuggestionBarMode
import ai.jagoan.keyboard.titan2.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository using DataStore
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val settingsFlow: Flow<KeyboardSettings> = dataStore.data
        .map { preferences ->
            // Migrate from old Boolean setting to new enum
            val suggestionBarMode = when {
                preferences.contains(PreferencesKeys.SUGGESTION_BAR_MODE) -> {
                    when (preferences[PreferencesKeys.SUGGESTION_BAR_MODE]) {
                        "ALWAYS_SHOW" -> SuggestionBarMode.ALWAYS_SHOW
                        "OFF" -> SuggestionBarMode.OFF
                        else -> SuggestionBarMode.AUTO
                    }
                }
                preferences.contains(PreferencesKeys.SHOW_SUGGESTIONS) -> {
                    // Migrate from old Boolean to new enum - default to ALWAYS_SHOW for better UX
                    if (preferences[PreferencesKeys.SHOW_SUGGESTIONS] == true) {
                        SuggestionBarMode.ALWAYS_SHOW
                    } else {
                        SuggestionBarMode.OFF
                    }
                }
                else -> SuggestionBarMode.ALWAYS_SHOW
            }
            
            KeyboardSettings(
                autoCapitalize = preferences[PreferencesKeys.AUTO_CAPITALIZE] ?: false,
                keyRepeatEnabled = preferences[PreferencesKeys.KEY_REPEAT_ENABLED] ?: false,
                longPressCapitalize = preferences[PreferencesKeys.LONG_PRESS_CAPITALIZE] ?: true,
                doubleSpacePeriod = preferences[PreferencesKeys.DOUBLE_SPACE_PERIOD] ?: true,
                textShortcutsEnabled = preferences[PreferencesKeys.TEXT_SHORTCUTS_ENABLED] ?: true,
                stickyShift = preferences[PreferencesKeys.STICKY_SHIFT] ?: true,
                stickyAlt = preferences[PreferencesKeys.STICKY_ALT] ?: false,
                altBackspaceDeleteLine = preferences[PreferencesKeys.ALT_BACKSPACE_DELETE_LINE] ?: true,
                keyRepeatDelay = preferences[PreferencesKeys.KEY_REPEAT_DELAY] ?: 400L,
                keyRepeatRate = preferences[PreferencesKeys.KEY_REPEAT_RATE] ?: 50L,
                preferredCurrency = preferences[PreferencesKeys.PREFERRED_CURRENCY] ?: "Rp",
                selectedLanguage = preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: "en",
                longPressAccents = preferences[PreferencesKeys.LONG_PRESS_ACCENTS] ?: false,
                autocorrectEnabled = preferences[PreferencesKeys.AUTOCORRECT_ENABLED] ?: true,
                autocorrectLanguages = preferences[PreferencesKeys.AUTOCORRECT_LANGUAGES]?.split(",") ?: listOf("en", "id"),
                suggestionBarMode = suggestionBarMode,
                autoFormatNumbers = preferences[PreferencesKeys.AUTO_FORMAT_NUMBERS] ?: true
            )
        }
        .distinctUntilChanged() // Only emit when settings actually change
        .conflate() // Keep only latest value if collector is slow

    override suspend fun updateSetting(key: String, value: Any?) {
        dataStore.edit { preferences ->
            when (key) {
                "autoCapitalize" -> preferences[PreferencesKeys.AUTO_CAPITALIZE] = value as Boolean
                "keyRepeatEnabled" -> preferences[PreferencesKeys.KEY_REPEAT_ENABLED] = value as Boolean
                "longPressCapitalize" -> preferences[PreferencesKeys.LONG_PRESS_CAPITALIZE] = value as Boolean
                "doubleSpacePeriod" -> preferences[PreferencesKeys.DOUBLE_SPACE_PERIOD] = value as Boolean
                "textShortcutsEnabled" -> preferences[PreferencesKeys.TEXT_SHORTCUTS_ENABLED] = value as Boolean
                "stickyShift" -> preferences[PreferencesKeys.STICKY_SHIFT] = value as Boolean
                "stickyAlt" -> preferences[PreferencesKeys.STICKY_ALT] = value as Boolean
                "altBackspaceDeleteLine" -> preferences[PreferencesKeys.ALT_BACKSPACE_DELETE_LINE] = value as Boolean
                "keyRepeatDelay" -> preferences[PreferencesKeys.KEY_REPEAT_DELAY] = value as Long
                "keyRepeatRate" -> preferences[PreferencesKeys.KEY_REPEAT_RATE] = value as Long
                "preferredCurrency" -> {
                    if (value == null) {
                        preferences.remove(PreferencesKeys.PREFERRED_CURRENCY)
                    } else {
                        preferences[PreferencesKeys.PREFERRED_CURRENCY] = value as String
                    }
                }
                "selectedLanguage" -> preferences[PreferencesKeys.SELECTED_LANGUAGE] = value as String
                "longPressAccents" -> preferences[PreferencesKeys.LONG_PRESS_ACCENTS] = value as Boolean
                "autocorrectEnabled" -> preferences[PreferencesKeys.AUTOCORRECT_ENABLED] = value as Boolean
                "autocorrectLanguages" -> {
                    @Suppress("UNCHECKED_CAST")
                    val languages = value as List<String>
                    preferences[PreferencesKeys.AUTOCORRECT_LANGUAGES] = languages.joinToString(",")
                }
                "suggestionBarMode" -> {
                    val mode = value as SuggestionBarMode
                    preferences[PreferencesKeys.SUGGESTION_BAR_MODE] = mode.name
                    // Remove old key if it exists
                    preferences.remove(PreferencesKeys.SHOW_SUGGESTIONS)
                }
                "autoFormatNumbers" -> preferences[PreferencesKeys.AUTO_FORMAT_NUMBERS] = value as Boolean
            }
        }
    }

    override suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
