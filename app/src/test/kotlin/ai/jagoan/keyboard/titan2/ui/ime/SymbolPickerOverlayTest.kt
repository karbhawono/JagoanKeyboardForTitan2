/**
 * Copyright (c) 2025 Aryo Karbhawono
 *
 * Unit tests for SymbolPickerOverlay component
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
 */

package ai.jagoan.keyboard.titan2.ui.ime

import android.view.KeyEvent
import ai.jagoan.keyboard.titan2.domain.model.SymbolCategory
import ai.jagoan.keyboard.titan2.domain.model.SymbolData
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Comprehensive unit tests for SymbolPickerOverlay functionality
 *
 * Tests cover:
 * - Physical key mapping (Q-P, A-L, Z-M to indices 0-25)
 * - Category cycling logic
 * - Symbol retrieval for different categories
 * - Edge cases and boundary conditions
 */
@DisplayName("SymbolPickerOverlay Tests")
class SymbolPickerOverlayTest {

    @Nested
    @DisplayName("Physical Key Mapping Tests")
    inner class KeyMappingTests {

        @Test
        @DisplayName("Row 1 keys (Q-P) map to indices 0-9")
        fun `row 1 keys map correctly`() {
            // Row 1: Q=0, W=1, E=2, R=3, T=4, Y=5, U=6, I=7, O=8, P=9
            assertEquals(0, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_Q))
            assertEquals(1, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_W))
            assertEquals(2, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_E))
            assertEquals(3, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_R))
            assertEquals(4, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_T))
            assertEquals(5, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_Y))
            assertEquals(6, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_U))
            assertEquals(7, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_I))
            assertEquals(8, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_O))
            assertEquals(9, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_P))
        }

        @Test
        @DisplayName("Row 2 keys (A-L) map to indices 10-18")
        fun `row 2 keys map correctly`() {
            // Row 2: A=10, S=11, D=12, F=13, G=14, H=15, J=16, K=17, L=18
            assertEquals(10, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_A))
            assertEquals(11, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_S))
            assertEquals(12, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_D))
            assertEquals(13, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_F))
            assertEquals(14, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_G))
            assertEquals(15, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_H))
            assertEquals(16, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_J))
            assertEquals(17, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_K))
            assertEquals(18, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_L))
        }

        @Test
        @DisplayName("Row 3 keys (Z-M) map to indices 19-25")
        fun `row 3 keys map correctly`() {
            // Row 3: Z=19, X=20, C=21, V=22, B=23, N=24, M=25
            assertEquals(19, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_Z))
            assertEquals(20, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_X))
            assertEquals(21, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_C))
            assertEquals(22, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_V))
            assertEquals(23, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_B))
            assertEquals(24, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_N))
            assertEquals(25, getSymbolIndexForKeyCode(KeyEvent.KEYCODE_M))
        }

        @Test
        @DisplayName("Non-letter keys return null")
        fun `non letter keys return null`() {
            // Test various non-letter keys
            assertNull(getSymbolIndexForKeyCode(KeyEvent.KEYCODE_0))
            assertNull(getSymbolIndexForKeyCode(KeyEvent.KEYCODE_1))
            assertNull(getSymbolIndexForKeyCode(KeyEvent.KEYCODE_SPACE))
            assertNull(getSymbolIndexForKeyCode(KeyEvent.KEYCODE_ENTER))
            assertNull(getSymbolIndexForKeyCode(KeyEvent.KEYCODE_DEL))
            assertNull(getSymbolIndexForKeyCode(KeyEvent.KEYCODE_SHIFT_LEFT))
            assertNull(getSymbolIndexForKeyCode(KeyEvent.KEYCODE_ALT_LEFT))
            assertNull(getSymbolIndexForKeyCode(KeyEvent.KEYCODE_SYM))
        }

        @Test
        @DisplayName("All 26 letter keys are mapped uniquely")
        fun `all letter keys have unique indices`() {
            val indices = mutableSetOf<Int>()
            val keyCodes = listOf(
                // Row 1
                KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_E,
                KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_T, KeyEvent.KEYCODE_Y,
                KeyEvent.KEYCODE_U, KeyEvent.KEYCODE_I, KeyEvent.KEYCODE_O,
                KeyEvent.KEYCODE_P,
                // Row 2
                KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_D,
                KeyEvent.KEYCODE_F, KeyEvent.KEYCODE_G, KeyEvent.KEYCODE_H,
                KeyEvent.KEYCODE_J, KeyEvent.KEYCODE_K, KeyEvent.KEYCODE_L,
                // Row 3
                KeyEvent.KEYCODE_Z, KeyEvent.KEYCODE_X, KeyEvent.KEYCODE_C,
                KeyEvent.KEYCODE_V, KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_N,
                KeyEvent.KEYCODE_M
            )

            keyCodes.forEach { keyCode ->
                val index = getSymbolIndexForKeyCode(keyCode)
                assertThat(index).isNotNull()
                assertThat(indices.add(index!!)).isTrue() // Ensure uniqueness
            }

            // Verify we have exactly 26 unique indices
            assertThat(indices).hasSize(26)
            // Verify indices are in range 0-25
            assertThat(indices).containsExactlyElementsIn(0..25)
        }
    }

    @Nested
    @DisplayName("Category Cycling Tests")
    inner class CategoryCyclingTests {

        @Test
        @DisplayName("Cycling from PUNCTUATION goes to CURRENCY")
        fun `next category after punctuation is currency`() {
            val next = getNextCategory(SymbolCategory.PUNCTUATION)
            assertEquals(SymbolCategory.CURRENCY, next)
        }

        @Test
        @DisplayName("Cycling from CURRENCY goes to MATH")
        fun `next category after currency is math`() {
            val next = getNextCategory(SymbolCategory.CURRENCY)
            assertEquals(SymbolCategory.MATH, next)
        }

        @Test
        @DisplayName("Cycling from MATH goes to ARROWS")
        fun `next category after math is arrows`() {
            val next = getNextCategory(SymbolCategory.MATH)
            assertEquals(SymbolCategory.ARROWS, next)
        }

        @Test
        @DisplayName("Cycling from ARROWS goes to EMOJI")
        fun `next category after arrows is emoji`() {
            val next = getNextCategory(SymbolCategory.ARROWS)
            assertEquals(SymbolCategory.EMOJI, next)
        }

        @Test
        @DisplayName("Cycling from EMOJI goes to MISC")
        fun `next category after emoji is misc`() {
            val next = getNextCategory(SymbolCategory.EMOJI)
            assertEquals(SymbolCategory.MISC, next)
        }

        @Test
        @DisplayName("Cycling from MISC wraps back to PUNCTUATION")
        fun `next category after misc wraps to punctuation`() {
            val next = getNextCategory(SymbolCategory.MISC)
            assertEquals(SymbolCategory.PUNCTUATION, next)
        }

        @Test
        @DisplayName("Category cycling is circular")
        fun `cycling through all categories returns to start`() {
            var category = SymbolCategory.PUNCTUATION
            val visited = mutableListOf<SymbolCategory>()

            // Cycle through all categories
            SymbolCategory.entries.forEach { _ ->
                visited.add(category)
                category = getNextCategory(category)
            }

            // Should return to PUNCTUATION after a full cycle
            assertEquals(SymbolCategory.PUNCTUATION, category)
            // Should have visited all categories exactly once
            assertThat(visited).containsExactlyElementsIn(SymbolCategory.entries)
        }
    }

    @Nested
    @DisplayName("Symbol Data Tests")
    inner class SymbolDataTests {

        @Test
        @DisplayName("PUNCTUATION category has exactly 19 symbols")
        fun `punctuation has 19 symbols`() {
            val symbols = SymbolData.getSymbolsForCategory(SymbolCategory.PUNCTUATION)
            assertThat(symbols).hasSize(19)
        }

        @Test
        @DisplayName("PUNCTUATION symbols match expected layout")
        fun `punctuation symbols are in correct order`() {
            val symbols = SymbolData.getSymbolsForCategory(SymbolCategory.PUNCTUATION)
            
            // Row 1 (Q-P): 10 symbols
            assertEquals(";", symbols[0].character)
            assertEquals("[", symbols[1].character)
            assertEquals("]", symbols[2].character)
            assertEquals("{", symbols[3].character)
            assertEquals("}", symbols[4].character)
            assertEquals("<", symbols[5].character)
            assertEquals(">", symbols[6].character)
            assertEquals("|", symbols[7].character)
            assertEquals("\\", symbols[8].character)
            assertEquals("&", symbols[9].character)

            // Row 2 (A-L): 9 symbols
            assertEquals("^", symbols[10].character)
            assertEquals("%", symbols[11].character)
            assertEquals("~", symbols[12].character)
            assertEquals("`", symbols[13].character)
            assertEquals("...", symbols[14].character)
            assertEquals("Rp", symbols[15].character)
            assertEquals("°", symbols[16].character)
            assertEquals("§", symbols[17].character)
            assertEquals("•", symbols[18].character)
        }

        @Test
        @DisplayName("CURRENCY category has multiple symbols")
        fun `currency has symbols`() {
            val symbols = SymbolData.getSymbolsForCategory(SymbolCategory.CURRENCY)
            assertThat(symbols.size).isAtLeast(10)
            
            // Verify some common currencies
            val characters = symbols.map { it.character }
            assertThat(characters).contains("$")
            assertThat(characters).contains("€")
            assertThat(characters).contains("£")
            assertThat(characters).contains("¥")
            assertThat(characters).contains("Rp")
        }

        @Test
        @DisplayName("MATH category has mathematical symbols")
        fun `math has mathematical symbols`() {
            val symbols = SymbolData.getSymbolsForCategory(SymbolCategory.MATH)
            assertThat(symbols.size).isAtLeast(10)
            
            // Verify some common math symbols
            val characters = symbols.map { it.character }
            assertThat(characters).contains("+")
            assertThat(characters).contains("×")
            assertThat(characters).contains("÷")
            assertThat(characters).contains("=")
            assertThat(characters).contains("π")
        }

        @Test
        @DisplayName("ARROWS category has directional symbols")
        fun `arrows has directional symbols`() {
            val symbols = SymbolData.getSymbolsForCategory(SymbolCategory.ARROWS)
            assertThat(symbols.size).isAtLeast(10)
            
            // Verify some common arrows
            val characters = symbols.map { it.character }
            assertThat(characters).contains("←")
            assertThat(characters).contains("→")
            assertThat(characters).contains("↑")
            assertThat(characters).contains("↓")
        }

        @Test
        @DisplayName("EMOJI category has emoji symbols")
        fun `emoji has emoji symbols`() {
            val symbols = SymbolData.getSymbolsForCategory(SymbolCategory.EMOJI)
            assertThat(symbols.size).isAtLeast(10)
            
            // Verify some common emojis
            val characters = symbols.map { it.character }
            assertThat(characters).contains("😀")
            assertThat(characters).contains("👍")
            assertThat(characters).contains("❤️")
        }

        @Test
        @DisplayName("MISC category has miscellaneous symbols")
        fun `misc has miscellaneous symbols`() {
            val symbols = SymbolData.getSymbolsForCategory(SymbolCategory.MISC)
            assertThat(symbols.size).isAtLeast(10)
            
            // Verify some common misc symbols
            val characters = symbols.map { it.character }
            assertThat(characters).contains("©")
            assertThat(characters).contains("®")
            assertThat(characters).contains("™")
        }

        @Test
        @DisplayName("All categories return non-empty symbol lists")
        fun `all categories have symbols`() {
            SymbolCategory.entries.forEach { category ->
                val symbols = SymbolData.getSymbolsForCategory(category)
                assertThat(symbols).isNotEmpty()
            }
        }

        @Test
        @DisplayName("Default category is PUNCTUATION")
        fun `default category is punctuation`() {
            val defaultCategory = SymbolData.getDefaultCategory()
            assertEquals(SymbolCategory.PUNCTUATION, defaultCategory)
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    inner class IntegrationTests {

        @Test
        @DisplayName("Key mapping works with symbol retrieval for PUNCTUATION")
        fun `can retrieve punctuation symbols via key codes`() {
            val symbols = SymbolData.getSymbolsForCategory(SymbolCategory.PUNCTUATION)

            // Test Q key (index 0) -> semicolon
            val qIndex = getSymbolIndexForKeyCode(KeyEvent.KEYCODE_Q)
            assertThat(qIndex).isNotNull()
            assertEquals(";", symbols[qIndex!!].character)

            // Test P key (index 9) -> ampersand
            val pIndex = getSymbolIndexForKeyCode(KeyEvent.KEYCODE_P)
            assertThat(pIndex).isNotNull()
            assertEquals("&", symbols[pIndex!!].character)

            // Test A key (index 10) -> caret
            val aIndex = getSymbolIndexForKeyCode(KeyEvent.KEYCODE_A)
            assertThat(aIndex).isNotNull()
            assertEquals("^", symbols[aIndex!!].character)

            // Test L key (index 18) -> bullet
            val lIndex = getSymbolIndexForKeyCode(KeyEvent.KEYCODE_L)
            assertThat(lIndex).isNotNull()
            assertEquals("•", symbols[lIndex!!].character)
        }

        @Test
        @DisplayName("Key mapping works with symbol retrieval for other categories")
        fun `can retrieve symbols from all categories via key codes`() {
            // For each category, test that key mapping works
            SymbolCategory.entries.forEach { category ->
                val symbols = SymbolData.getSymbolsForCategory(category)

                // Test first key (Q = index 0)
                val qIndex = getSymbolIndexForKeyCode(KeyEvent.KEYCODE_Q)
                assertThat(qIndex).isNotNull()
                if (symbols.size > qIndex!!) {
                    assertThat(symbols[qIndex].character).isNotEmpty()
                }

                // Test last available key based on symbol count
                if (symbols.size > 18) {
                    val lIndex = getSymbolIndexForKeyCode(KeyEvent.KEYCODE_L)
                    assertThat(lIndex).isNotNull()
                    assertThat(symbols[lIndex!!].character).isNotEmpty()
                }
            }
        }

        @Test
        @DisplayName("Complete cycle through categories and keys")
        fun `complete workflow simulation`() {
            var currentCategory = SymbolCategory.PUNCTUATION

            // Simulate cycling through all categories
            repeat(SymbolCategory.entries.size) { _ ->
                val symbols = SymbolData.getSymbolsForCategory(currentCategory)
                assertThat(symbols).isNotEmpty()

                // Simulate pressing some keys in this category
                val testKeyCodes = listOf(
                    KeyEvent.KEYCODE_Q, // Index 0
                    KeyEvent.KEYCODE_W, // Index 1
                    KeyEvent.KEYCODE_A, // Index 10
                    KeyEvent.KEYCODE_S  // Index 11
                )

                testKeyCodes.forEach { keyCode ->
                    val index = getSymbolIndexForKeyCode(keyCode)
                    assertThat(index).isNotNull()
                    if (symbols.size > index!!) {
                        val symbol = symbols[index]
                        assertThat(symbol.character).isNotEmpty()
                    }
                }

                // Move to next category
                currentCategory = getNextCategory(currentCategory)
            }

            // Should be back to PUNCTUATION after full cycle
            assertEquals(SymbolCategory.PUNCTUATION, currentCategory)
        }

        @Test
        @DisplayName("Boundary test - accessing symbol at max index")
        fun `accessing symbols at boundary indices works correctly`() {
            val punctuationSymbols = SymbolData.getSymbolsForCategory(SymbolCategory.PUNCTUATION)
            
            // PUNCTUATION has 19 symbols (indices 0-18)
            // Index 18 should be accessible (L key)
            val lIndex = getSymbolIndexForKeyCode(KeyEvent.KEYCODE_L)
            assertThat(lIndex).isEqualTo(18)
            assertThat(punctuationSymbols.size).isGreaterThan(18)
            assertThat(punctuationSymbols[18].character).isEqualTo("•")

            // Index 19 (Z key) should be out of bounds for PUNCTUATION
            val zIndex = getSymbolIndexForKeyCode(KeyEvent.KEYCODE_Z)
            assertThat(zIndex).isEqualTo(19)
            assertThat(punctuationSymbols.size).isLessThan(20)
        }

        @Test
        @DisplayName("Verify Rupiah symbol at correct position")
        fun `rupiah symbol is at index 15 in punctuation`() {
            val symbols = SymbolData.getSymbolsForCategory(SymbolCategory.PUNCTUATION)
            
            // Rp should be at index 15 (H key in row 2)
            val hIndex = getSymbolIndexForKeyCode(KeyEvent.KEYCODE_H)
            assertThat(hIndex).isEqualTo(15)
            assertEquals("Rp", symbols[15].character)
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCaseTests {

        @Test
        @DisplayName("Invalid keycode returns null")
        fun `invalid keycode returns null`() {
            assertNull(getSymbolIndexForKeyCode(-1))
            assertNull(getSymbolIndexForKeyCode(999))
            assertNull(getSymbolIndexForKeyCode(Int.MAX_VALUE))
            assertNull(getSymbolIndexForKeyCode(Int.MIN_VALUE))
        }

        @Test
        @DisplayName("Categories list is not empty")
        fun `categories list is populated`() {
            val categories = SymbolData.categories
            assertThat(categories).isNotEmpty()
            assertThat(categories).hasSize(6)
        }

        @Test
        @DisplayName("All category names are unique")
        fun `category names are unique`() {
            val names = SymbolCategory.entries.map { it.displayName }
            assertThat(names.toSet()).hasSize(names.size)
        }

        @Test
        @DisplayName("Symbol characters are not blank")
        fun `no blank symbol characters`() {
            SymbolCategory.entries.forEach { category ->
                val symbols = SymbolData.getSymbolsForCategory(category)
                symbols.forEach { symbol ->
                    assertThat(symbol.character).isNotEmpty()
                    assertThat(symbol.character.isBlank()).isFalse()
                }
            }
        }
    }
}