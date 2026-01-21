/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.domain.model to ai.jagoan.keyboard.titan2.domain.model
 * - Added isCurrencySymbol() helper function to check if a symbol is a currency symbol
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
 * Categories of symbols in the BB OS 7-style picker
 */
enum class SymbolCategory(val displayName: String) {
    PUNCTUATION("Punctuation"),
    CURRENCY("Currency"),
    MATH("Math"),
    ARROWS("Arrows"),
    EMOJI("Emoji"),
    MISC("Misc")
}

/**
 * Symbol data for display in the picker
 */
data class Symbol(
    val character: String,
    val description: String = ""
)

/**
 * Symbols organized by category - BB OS 7 style layout
 */
object SymbolData {

    val categories = SymbolCategory.entries.toList()

    private val punctuationSymbols = listOf(
        // Row 1 (Q-P): 10 symbols
        Symbol(";", "Semicolon"),
        Symbol("[", "Open bracket"),
        Symbol("]", "Close bracket"),
        Symbol("{", "Open brace"),
        Symbol("}", "Close brace"),
        Symbol("<", "Less than"),
        Symbol(">", "Greater than"),
        Symbol("|", "Pipe"),
        Symbol("\\", "Backslash"),
        Symbol("&", "Ampersand"),
        // Row 2 (A-L): 9 symbols
        Symbol("^", "Caret"),
        Symbol("%", "Percent"),
        Symbol("~", "Tilde"),
        Symbol("`", "Backtick"),
        Symbol("...", "Ellipsis"),
        Symbol("Rp", "Rupiah"),
        Symbol("°", "Degree"),
        Symbol("§", "Section"),
        Symbol("•", "Bullet")
    )

    private val currencySymbols = listOf(
        Symbol("$", "Dollar"),
        Symbol("€", "Euro"),
        Symbol("£", "Pound"),
        Symbol("¥", "Yen"),
        Symbol("₹", "Rupee"),
        Symbol("₽", "Ruble"),
        Symbol("₿", "Bitcoin"),
        Symbol("¢", "Cent"),
        Symbol("₩", "Won"),
        Symbol("₪", "Shekel"),
        Symbol("₫", "Dong"),
        Symbol("₱", "Peso"),
        Symbol("฿", "Baht"),
        Symbol("₴", "Hryvnia"),
        Symbol("₦", "Naira"),
        Symbol("₲", "Guarani"),
        Symbol("₵", "Cedi"),
        Symbol("₡", "Colon"),
        Symbol("₸", "Tenge"),
        Symbol("₺", "Lira"),
        Symbol("Rp", "Rupiah")

    )

    private val mathSymbols = listOf(
        Symbol("+", "Plus"),
        Symbol("-", "Minus"),
        Symbol("×", "Multiply"),
        Symbol("÷", "Divide"),
        Symbol("=", "Equals"),
        Symbol("≠", "Not equal"),
        Symbol("≈", "Approx"),
        Symbol("<", "Less than"),
        Symbol(">", "Greater than"),
        Symbol("≤", "Less or equal"),
        Symbol("≥", "Greater or equal"),
        Symbol("±", "Plus minus"),
        Symbol("∞", "Infinity"),
        Symbol("√", "Square root"),
        Symbol("∑", "Sum"),
        Symbol("∏", "Product"),
        Symbol("∫", "Integral"),
        Symbol("π", "Pi"),
        Symbol("°", "Degree"),
        Symbol("′", "Prime"),
        Symbol("″", "Double prime"),
        Symbol("‰", "Per mille"),
        Symbol("∂", "Partial"),
        Symbol("∆", "Delta"),
        Symbol("∇", "Nabla"),
        Symbol("µ", "Micro"),
        Symbol("∈", "Element of"),
        Symbol("∉", "Not element"),
        Symbol("⊂", "Subset"),
        Symbol("∪", "Union")
    )

    private val arrowSymbols = listOf(
        Symbol("←", "Left"),
        Symbol("→", "Right"),
        Symbol("↑", "Up"),
        Symbol("↓", "Down"),
        Symbol("↔", "Left right"),
        Symbol("↕", "Up down"),
        Symbol("↖", "Upper left"),
        Symbol("↗", "Upper right"),
        Symbol("↘", "Lower right"),
        Symbol("↙", "Lower left"),
        Symbol("⇐", "Double left"),
        Symbol("⇒", "Double right"),
        Symbol("⇑", "Double up"),
        Symbol("⇓", "Double down"),
        Symbol("⇔", "Double horiz"),
        Symbol("⇕", "Double vert"),
        Symbol("↵", "Return"),
        Symbol("↩", "Return left"),
        Symbol("↪", "Return right"),
        Symbol("⟲", "Anticlockwise"),
        Symbol("⟳", "Clockwise"),
        Symbol("↻", "Clockwise arrow"),
        Symbol("↺", "Counter arrow"),
        Symbol("⤴", "Right up"),
        Symbol("⤵", "Right down"),
        Symbol("➔", "Heavy right"),
        Symbol("➜", "Heavy right 2"),
        Symbol("➤", "Triangle right"),
        Symbol("►", "Play"),
        Symbol("◄", "Play left")
    )

    private val emojiSymbols = listOf(
        Symbol("😀", "Smile"),
        Symbol("😃", "Grin"),
        Symbol("😄", "Happy"),
        Symbol("😊", "Blush"),
        Symbol("🙂", "Slight smile"),
        Symbol("😉", "Wink"),
        Symbol("😍", "Heart eyes"),
        Symbol("😘", "Kiss"),
        Symbol("😎", "Cool"),
        Symbol("🤔", "Think"),
        Symbol("😢", "Cry"),
        Symbol("😭", "Sob"),
        Symbol("😤", "Huff"),
        Symbol("😠", "Angry"),
        Symbol("🤯", "Mind blown"),
        Symbol("👍", "Thumbs up"),
        Symbol("👎", "Thumbs down"),
        Symbol("👋", "Wave"),
        Symbol("🙏", "Pray"),
        Symbol("💪", "Strong"),
        Symbol("❤️", "Heart"),
        Symbol("💔", "Broken heart"),
        Symbol("⭐", "Star"),
        Symbol("🔥", "Fire"),
        Symbol("✨", "Sparkle"),
        Symbol("💯", "100"),
        Symbol("✅", "Check"),
        Symbol("❌", "Cross"),
        Symbol("⚠️", "Warning"),
        Symbol("🎉", "Party")
    )

    private val miscSymbols = listOf(
        Symbol("©", "Copyright"),
        Symbol("®", "Registered"),
        Symbol("™", "Trademark"),
        Symbol("§", "Section"),
        Symbol("¶", "Paragraph"),
        Symbol("†", "Dagger"),
        Symbol("‡", "Double dag"),
        Symbol("•", "Bullet"),
        Symbol("·", "Middle dot"),
        Symbol("…", "Ellipsis"),
        Symbol("—", "Em dash"),
        Symbol("–", "En dash"),
        Symbol("‹", "Single left"),
        Symbol("›", "Single right"),
        Symbol("«", "Double left"),
        Symbol("»", "Double right"),
        Symbol("№", "Numero"),
        Symbol("℃", "Celsius"),
        Symbol("℉", "Fahrenheit"),
        Symbol("♠", "Spade"),
        Symbol("♥", "Heart"),
        Symbol("♦", "Diamond"),
        Symbol("♣", "Club"),
        Symbol("♪", "Note"),
        Symbol("♫", "Notes"),
        Symbol("☀", "Sun"),
        Symbol("☁", "Cloud"),
        Symbol("☂", "Umbrella"),
        Symbol("★", "Black star"),
        Symbol("☆", "White star")
    )

    /**
     * Get symbols for a given category
     */
    fun getSymbolsForCategory(category: SymbolCategory): List<Symbol> {
        return when (category) {
            SymbolCategory.PUNCTUATION -> punctuationSymbols
            SymbolCategory.CURRENCY -> currencySymbols
            SymbolCategory.MATH -> mathSymbols
            SymbolCategory.ARROWS -> arrowSymbols
            SymbolCategory.EMOJI -> emojiSymbols
            SymbolCategory.MISC -> miscSymbols
        }
    }

    /**
     * Get the default category to show
     */
    fun getDefaultCategory(): SymbolCategory = SymbolCategory.PUNCTUATION

    /**
     * Check if a given symbol is a currency symbol
     * Checks both the currency category and punctuation layer
     */
    fun isCurrencySymbol(symbol: String): Boolean {
        return currencySymbols.any { it.character == symbol } ||
               punctuationSymbols.any { it.character == symbol && 
                   // Known currency symbols in punctuation layer
                   symbol in listOf("Rp", "$", "€", "£", "¥", "₹", "₽", "₩", "¢", 
                                   "₪", "₿", "CHF", "kr", "zł", "Kč", "Ft", 
                                   "lei", "лв", "₺", "R$", "R", "HK$")
               }
    }
}
