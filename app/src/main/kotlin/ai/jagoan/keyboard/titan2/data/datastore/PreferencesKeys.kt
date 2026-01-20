/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.data.datastore to ai.jagoan.keyboard.titan2.data.datastore
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

package ai.jagoan.keyboard.titan2.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * DataStore preference keys for keyboard settings
 */
object PreferencesKeys {
    val AUTO_CAPITALIZE = booleanPreferencesKey("auto_capitalize")
    val KEY_REPEAT_ENABLED = booleanPreferencesKey("key_repeat_enabled")
    val LONG_PRESS_CAPITALIZE = booleanPreferencesKey("long_press_capitalize")
    val DOUBLE_SPACE_PERIOD = booleanPreferencesKey("double_space_period")
    val TEXT_SHORTCUTS_ENABLED = booleanPreferencesKey("text_shortcuts_enabled")
    val STICKY_SHIFT = booleanPreferencesKey("sticky_shift")
    val STICKY_ALT = booleanPreferencesKey("sticky_alt")
    val ALT_BACKSPACE_DELETE_LINE = booleanPreferencesKey("alt_backspace_delete_line")
    val KEY_REPEAT_DELAY = longPreferencesKey("key_repeat_delay")
    val KEY_REPEAT_RATE = longPreferencesKey("key_repeat_rate")
    val PREFERRED_CURRENCY = stringPreferencesKey("preferred_currency")
    val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    val LONG_PRESS_ACCENTS = booleanPreferencesKey("long_press_accents")
}
