/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.util to ai.jagoan.keyboard.titan2.util
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

package ai.jagoan.keyboard.titan2.util

import java.util.Locale

/**
 * Utilities for locale-based functionality
 */
object LocaleUtils {

    /**
     * Get the default currency symbol based on device locale
     */
    fun getDefaultCurrencySymbol(): String {
        val locale = Locale.getDefault()
        val country = locale.country

        return when (country) {
            // US, Canada (English), Latin America
            "US", "CA", "MX", "AR", "CL", "CO", "PE", "VE", "EC", "GT",
            "CU", "BO", "DO", "HN", "PY", "SV", "NI", "CR", "PA", "UY" -> "$"

            // UK
            "GB" -> "£"

            // Eurozone countries
            "AT", "BE", "CY", "EE", "FI", "FR", "DE", "GR", "IE",
            "IT", "LV", "LT", "LU", "MT", "NL", "PT", "SK", "SI",
            "ES", "HR" -> "€"

            // Japan
            "JP" -> "¥"

            // China (Yuan uses same symbol as Yen)
            "CN" -> "¥"

            // India
            "IN" -> "₹"

            // Russia
            "RU" -> "₽"

            // South Korea
            "KR" -> "₩"

            // Israel
            "IL" -> "₪"

            // Switzerland
            "CH" -> "CHF"

            // Sweden
            "SE" -> "kr"

            // Norway
            "NO" -> "kr"

            // Denmark
            "DK" -> "kr"

            // Poland
            "PL" -> "zł"

            // Czech Republic
            "CZ" -> "Kč"

            // Hungary
            "HU" -> "Ft"

            // Romania
            "RO" -> "lei"

            // Bulgaria
            "BG" -> "лв"

            // Turkey
            "TR" -> "₺"

            // Brazil
            "BR" -> "R$"

            // South Africa
            "ZA" -> "R"

            // Australia, New Zealand
            "AU", "NZ" -> "$"

            // Singapore
            "SG" -> "$"

            // Hong Kong
            "HK" -> "HK$"

            // Indonesia
            "ID" -> "Rp"

            // Default to dollar if unknown
            else -> "$"
        }
    }

    /**
     * Get all available currency symbols for user selection
     * Returns list of (symbol, displayLabel) pairs
     */
    fun getAllCurrencySymbols(): List<Pair<String, String>> {
        return listOf(
            "$" to "Dollar ($)",
            "€" to "Euro (€)",
            "£" to "Pound (£)",
            "¥" to "Yen/Yuan (¥)",
            "₹" to "Rupee (₹)",
            "₽" to "Ruble (₽)",
            "₩" to "Won (₩)",
            "¢" to "Cent (¢)",
            "₪" to "Shekel (₪)",
            "₿" to "Bitcoin (₿)",
            "CHF" to "Swiss Franc (CHF)",
            "kr" to "Krona (kr)",
            "zł" to "Zloty (zł)",
            "Kč" to "Koruna (Kč)",
            "Ft" to "Forint (Ft)",
            "lei" to "Leu (lei)",
            "лв" to "Lev (лв)",
            "₺" to "Lira (₺)",
            "R$" to "Real (R$)",
            "R" to "Rand (R)",
            "HK$" to "Hong Kong Dollar (HK$)",
            "Rp" to "Rupiah (Rp)"
        )
    }
}
