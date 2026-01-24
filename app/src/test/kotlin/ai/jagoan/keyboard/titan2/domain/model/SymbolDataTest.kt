/**
 * Copyright (c) 2025 Aryo Karbhawono
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
 * Modifications:
 * - Created unit tests for symbol data validation

@DisplayName("SymbolData Tests")
class SymbolDataTest {

    @Test
    @DisplayName("Rp from punctuation should be detected as currency")
    fun rpFromPunctuationIsCurrency() {
        val result = SymbolData.isCurrencySymbol("Rp")
        assertThat(result).isTrue()
    }

    @Test
    @DisplayName("Dollar sign should be detected as currency")
    fun dollarIsCurrency() {
        val result = SymbolData.isCurrencySymbol("$")
        assertThat(result).isTrue()
    }

    @Test
    @DisplayName("Euro should be detected as currency")
    fun euroIsCurrency() {
        val result = SymbolData.isCurrencySymbol("€")
        assertThat(result).isTrue()
    }

    @Test
    @DisplayName("Semicolon should NOT be detected as currency")
    fun semicolonIsNotCurrency() {
        val result = SymbolData.isCurrencySymbol(";")
        assertThat(result).isFalse()
    }

    @Test
    @DisplayName("Ellipsis should NOT be detected as currency")
    fun ellipsisIsNotCurrency() {
        val result = SymbolData.isCurrencySymbol("...")
        assertThat(result).isFalse()
    }
}
