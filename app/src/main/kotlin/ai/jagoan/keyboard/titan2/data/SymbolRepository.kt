/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.data to ai.jagoan.keyboard.titan2.data
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

package ai.jagoan.keyboard.titan2.data

import ai.jagoan.keyboard.titan2.util.LocaleUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents a category of symbols in the symbol picker
 */
data class SymbolCategoryItem(
    /** Unique identifier for this category */
    val id: String,

    /** Display name shown in UI */
    val displayName: String,

    /** List of symbol characters in this category */
    val symbols: List<String>,

    /** Order in which categories are displayed (0-based) */
    val order: Int
)

/**
 * Repository for symbol categories and symbol data
 * Provides symbols for the symbol picker, excluding symbols already available via Alt key
 *
 * Alt key provides: 1 2 3 4 5 6 7 8 9 0 @ ! ( ) - _ * # + " , . / ' ? :
 */
@Singleton
class SymbolRepository @Inject constructor() {

    /**
     * Get symbol categories with the specified currency symbol in Common category
     * @param preferredCurrency User's preferred currency symbol, or null to use locale default
     */
    fun getCategories(preferredCurrency: String?): List<SymbolCategoryItem> {
        // Use preferred currency, or auto-detect from locale
        val currencySymbol = preferredCurrency ?: LocaleUtils.getDefaultCurrencySymbol()

        return listOf(
            // Category 1: Common Symbols
            SymbolCategoryItem(
                id = "common",
                displayName = "Common Symbols",
                symbols = listOf(
                    currencySymbol,  // User's preferred/locale currency FIRST
                    "%", "&", "=", "[", "]",
                    "{", "}", "|", ";", "<", ">",
                    "~", "\\", "`", "^", "«", "»",
                    "°", "§", "¶", "•", "…"
                ),
                order = 0
            ),

            // Category 2: Math Symbols
            SymbolCategoryItem(
                id = "math",
                displayName = "Math",
                symbols = listOf(
                    "±", "×", "÷", "≠", "≈", "≤",
                    "≥", "√", "∞", "∑", "π", "∫",
                    "∂", "∆", "∏", "ƒ", "µ", "α",
                    "β", "γ", "δ", "θ", "λ", "σ"
                ),
                order = 1
            ),

            // Category 3: Currency Symbols
            SymbolCategoryItem(
                id = "currency",
                displayName = "Currency",
                symbols = listOf(
                    "$", "€", "£", "¥", "₹", "₽",
                    "₩", "¢", "₪", "₿", "CHF", "kr",
                    "zł", "Kč", "Ft", "lei", "лв", "₺",
                    "R$", "R", "HK$", "Rp"
                ),
                order = 2
            ),

            // Category 4: Arrows
            SymbolCategoryItem(
                id = "arrows",
                displayName = "Arrows",
                symbols = listOf(
                    "←", "→", "↑", "↓", "↔", "↕",
                    "⇐", "⇒", "⇑", "⇓", "⇔", "↖",
                    "↗", "↘", "↙", "⟵", "⟶", "⟷"
                ),
                order = 3
            ),

            // Category 5: Other Symbols
            SymbolCategoryItem(
                id = "other",
                displayName = "Other",
                symbols = listOf(
                    "©", "®", "™", "†", "‡", "‰",
                    "′", "″", "※", "℃", "℉", "№",
                    "℗", "℠", "⁂", "¡", "¿", "…",
                    "–", "—", "‹", "›", "‚", "„"
                ),
                order = 4
            )
        )
    }

    /**
     * Get category by index (wraps around if out of bounds)
     */
    fun getCategoryByIndex(index: Int, preferredCurrency: String?): SymbolCategoryItem {
        val categories = getCategories(preferredCurrency)
        return categories[index % categories.size]
    }

    /**
     * Get the next category after the current one
     */
    fun getNextCategory(currentId: String, preferredCurrency: String?): SymbolCategoryItem {
        val categories = getCategories(preferredCurrency)
        val currentIndex = categories.indexOfFirst { it.id == currentId }
        val nextIndex = (currentIndex + 1) % categories.size
        return categories[nextIndex]
    }

    /**
     * Get total number of categories
     */
    fun getCategoryCount(): Int = 5
}
