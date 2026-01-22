package ai.jagoan.keyboard.titan2.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

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
