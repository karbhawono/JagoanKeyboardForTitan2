/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.domain.model to ai.jagoan.keyboard.titan2.domain.model
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

package ai.jagoan.keyboard.titan2.domain.model

/**
 * Domain model representing keyboard settings
 */
data class KeyboardSettings(
    val autoCapitalize: Boolean = false,
    val keyRepeatEnabled: Boolean = false,
    val longPressCapitalize: Boolean = true,
    val doubleSpacePeriod: Boolean = true,
    val textShortcutsEnabled: Boolean = true,
    val stickyShift: Boolean = true,
    val stickyAlt: Boolean = true,
    val altBackspaceDeleteLine: Boolean = true, // Alt+Backspace deletes entire line
    val keyRepeatDelay: Long = 400L, // milliseconds
    val keyRepeatRate: Long = 50L,   // milliseconds
    val preferredCurrency: String? = "Rp", // Preferred currency symbol (defaults to Rupiah, null = use locale default)
    val selectedLanguage: String = "en", // Primary language for accents (en, fr, de, es, pt, it, etc.)
    val longPressAccents: Boolean = false, // Long-press shows accent variants instead of uppercase
    val autocorrectEnabled: Boolean = true, // Enable autocorrect on space
    val autocorrectLanguages: List<String> = listOf("en", "id"), // Languages for autocorrect dictionaries
    val showSuggestions: Boolean = false // Show suggestion bar (not implemented yet)
)
