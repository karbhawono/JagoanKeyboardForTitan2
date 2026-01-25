/**
 * Copyright (c) 2025 Aryo Karbhawono
 */

package ai.jagoan.keyboard.titan2.util

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NumberFormatterTest {

    @Before
    fun setup() {
        // Mock Android Log class
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any<String>()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.i(any(), any<String>()) } returns 0
        every { Log.v(any(), any<String>()) } returns 0
    }

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
