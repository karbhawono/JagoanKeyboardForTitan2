/**
 * Copyright (c) 2025 Aryo Karbhawono
 */

package ai.jagoan.keyboard.titan2.util

import org.junit.Assert.*
import org.junit.Test

class NumberFormatterTest {

    @Test
    fun formatNumberShouldFormat50000() {
        val result = NumberFormatter.formatNumber("50000")
        assertEquals("50,000", result)
    }

    @Test
    fun formatNumberShouldFormat50000WithDecimals() {
        val result = NumberFormatter.formatNumber("50000.00")
        assertEquals("50,000.00", result)
    }

    @Test
    fun formatNumberShouldFormat1234567() {
        val result = NumberFormatter.formatNumber("1234567")
        assertEquals("1,234,567", result)
    }

    @Test
    fun isPlainNumberShouldReturnTrueForIntegers() {
        assertTrue(NumberFormatter.isPlainNumber("50000"))
        assertTrue(NumberFormatter.isPlainNumber("123"))
    }

    @Test
    fun isPlainNumberShouldReturnFalseForFormattedNumbers() {
        assertFalse(NumberFormatter.isPlainNumber("50,000"))
    }

    @Test
    fun isPhoneNumberShouldDetectIndonesianNumbers() {
        assertTrue(NumberFormatter.isPhoneNumber("081234567890", ""))
    }

    @Test
    fun isPhoneNumberShouldNotDetectRegularAmounts() {
        assertFalse(NumberFormatter.isPhoneNumber("50000", ""))
    }

    @Test
    fun shouldFormatAsAmountShouldReturnTrueForRegularAmounts() {
        assertTrue(NumberFormatter.shouldFormatAsAmount("50000", "", null))
        assertTrue(NumberFormatter.shouldFormatAsAmount("123456", "", null))
    }

    @Test
    fun shouldFormatAsAmountShouldReturnFalseForPhoneNumbers() {
        assertFalse(NumberFormatter.shouldFormatAsAmount("081234567890", "", null))
    }
}
