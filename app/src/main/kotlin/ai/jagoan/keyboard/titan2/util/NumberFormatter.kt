/**
 * Copyright (c) 2025 Aryo Karbhawono
 *
 * Smart number formatter with detection for phone numbers, credit cards, and tracking numbers.
 * Automatically formats monetary amounts with thousand separators while avoiding formatting
 * of numbers that should remain unformatted.
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

package ai.jagoan.keyboard.titan2.util

import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

/**
 * Smart number formatter with intelligent detection of:
 * - Phone numbers (international and local formats)
 * - Credit card numbers (with Luhn validation)
 * - Tracking numbers (courier/shipment codes)
 * - Reference codes and IDs
 * 
 * Only formats regular monetary amounts with thousand separators.
 */
object NumberFormatter {
    
    // ========== PHONE NUMBER DETECTION ==========
    
    /**
     * Detect if a number is likely a phone number
     * Supports multiple country formats
     */
    fun isPhoneNumber(number: String, textBefore: String = ""): Boolean {
        val digitsOnly = number.replace(Regex("[^0-9]"), "")
        val length = digitsOnly.length
        
        // Phone numbers are typically 10-15 digits (sometimes up to 17 with country codes)
        if (length in 10..17) {
            // Indonesia: 08xx-xxxx-xxxx or +62-8xx-xxxx-xxxx
            if (number.startsWith("08") || number.startsWith("628") || number.startsWith("+628")) {
                return true
            }
            
            // Malaysia: 01x-xxx-xxxx or +60-1x-xxx-xxxx
            if (number.startsWith("01") && length >= 10) {
                return true
            }
            if (number.startsWith("60") && length >= 10) {
                return true
            }
            
            // Singapore: +65-xxxx-xxxx (8 digits after code)
            if (number.startsWith("+65") || number.startsWith("65")) {
                return true
            }
            
            // Thailand: 0x-xxxx-xxxx or +66-x-xxxx-xxxx
            if (number.startsWith("+66") || (number.startsWith("0") && length >= 9)) {
                return true
            }
            
            // Philippines: 09xx-xxx-xxxx or +63-9xx-xxx-xxxx
            if (number.startsWith("09") || number.startsWith("639") || number.startsWith("+639")) {
                return true
            }
            
            // Vietnam: 0xxx-xxx-xxx or +84-xxx-xxx-xxx
            if (number.startsWith("+84") || number.startsWith("84")) {
                return true
            }
            
            // US/Canada: +1-xxx-xxx-xxxx or 1-xxx-xxx-xxxx
            if (number.startsWith("+1") || number.startsWith("1")) {
                return true
            }
            
            // UK: +44-xxxx-xxxxxx
            if (number.startsWith("+44") || number.startsWith("44")) {
                return true
            }
            
            // Australia: +61-xxx-xxx-xxx
            if (number.startsWith("+61") || number.startsWith("61")) {
                return true
            }
            
            // International format: starts with + or 00
            if (number.startsWith("+") || number.startsWith("00")) {
                return true
            }
            
            // Generic: starts with 0 (local format in many countries)
            if (number.startsWith("0") && length >= 10) {
                return true
            }
            
            // Context check: previous text has phone separators
            if (textBefore.takeLast(20).contains(Regex("[-()\\s]")) && length >= 10) {
                return true
            }
        }
        
        // Very long numbers (>15 digits) are likely phone/ID numbers
        if (length > 15) {
            return true
        }
        
        return false
    }
    
    // ========== CREDIT CARD DETECTION ==========
    
    /**
     * Detect if a number is likely a credit card number
     * Uses length, prefix, and Luhn algorithm
     */
    fun isCreditCardNumber(number: String): Boolean {
        val digitsOnly = number.replace(Regex("[^0-9]"), "")
        val length = digitsOnly.length
        
        // Credit cards are 13-19 digits (most commonly 16)
        if (length !in 13..19) {
            return false
        }
        
        if (digitsOnly.isEmpty()) return false
        
        val firstDigit = digitsOnly[0].toString().toIntOrNull() ?: return false
        val firstTwoDigits = if (digitsOnly.length >= 2) {
            digitsOnly.substring(0, 2).toIntOrNull() ?: return false
        } else {
            return false
        }
        val firstFourDigits = if (digitsOnly.length >= 4) {
            digitsOnly.substring(0, 4).toIntOrNull() ?: return false
        } else {
            return false
        }
        
        // Visa: starts with 4, 16 digits
        if (firstDigit == 4 && length == 16) {
            return passesLuhnCheck(digitsOnly)
        }
        
        // Mastercard: starts with 51-55 or 2221-2720, 16 digits
        if ((firstTwoDigits in 51..55 || firstFourDigits in 2221..2720) && length == 16) {
            return passesLuhnCheck(digitsOnly)
        }
        
        // Amex: starts with 34 or 37, 15 digits
        if ((firstTwoDigits == 34 || firstTwoDigits == 37) && length == 15) {
            return passesLuhnCheck(digitsOnly)
        }
        
        // Discover: starts with 6011, 622126-622925, 644-649, or 65, 16-19 digits
        if ((firstFourDigits == 6011 || firstFourDigits in 6221..6229 || 
             firstTwoDigits in 64..65) && length in 16..19) {
            return passesLuhnCheck(digitsOnly)
        }
        
        // JCB: starts with 3528-3589, 16-19 digits
        if (firstFourDigits in 3528..3589 && length in 16..19) {
            return passesLuhnCheck(digitsOnly)
        }
        
        // Diners Club: starts with 36 or 38, 14 digits
        if ((firstTwoDigits == 36 || firstTwoDigits == 38) && length == 14) {
            return passesLuhnCheck(digitsOnly)
        }
        
        // UnionPay: starts with 62, 16-19 digits
        if (firstTwoDigits == 62 && length in 16..19) {
            return passesLuhnCheck(digitsOnly)
        }
        
        // If exactly 16 digits and starts with common prefix, likely a card
        if (length == 16 && firstDigit in listOf(3, 4, 5, 6)) {
            return passesLuhnCheck(digitsOnly)
        }
        
        return false
    }
    
    /**
     * Luhn algorithm (mod 10) for credit card validation
     * https://en.wikipedia.org/wiki/Luhn_algorithm
     */
    private fun passesLuhnCheck(cardNumber: String): Boolean {
        if (cardNumber.isEmpty()) return false
        
        var sum = 0
        var alternate = false
        
        // Process digits from right to left
        for (i in cardNumber.length - 1 downTo 0) {
            var digit = cardNumber[i].toString().toIntOrNull() ?: return false
            
            if (alternate) {
                digit *= 2
                if (digit > 9) {
                    digit -= 9
                }
            }
            
            sum += digit
            alternate = !alternate
        }
        
        return sum % 10 == 0
    }
    
    // ========== TRACKING NUMBER DETECTION ==========
    
    /**
     * Detect if a number is likely a tracking/reference number
     * Common patterns:
     * - Courier tracking: 10-20 alphanumeric characters
     * - Order numbers: mix of letters and numbers
     * - Reference codes: specific patterns
     */
    fun isTrackingNumber(text: String, textBefore: String = ""): Boolean {
        // Pure numbers only - check length
        val digitsOnly = text.replace(Regex("[^0-9]"), "")
        
        // Very long pure numbers (13-20 digits) are likely tracking numbers
        if (text == digitsOnly && digitsOnly.length in 13..20) {
            return true
        }
        
        // Contains mix of letters and numbers (tracking codes)
        if (text.contains(Regex("[A-Z]")) && text.contains(Regex("[0-9]"))) {
            return true
        }
        
        // Context: words like "tracking", "order", "ref", "id" nearby
        val context = textBefore.takeLast(50).lowercase()
        if (context.contains(Regex("(tracking|order|ref|reference|id|number|no|#)\\s*:?\\s*$"))) {
            if (digitsOnly.length >= 10) {
                return true
            }
        }
        
        return false
    }
    
    // ========== GENERAL NUMBER CHECKS ==========
    
    /**
     * Check if a string is a plain number (no commas, just digits and optional decimal)
     */
    fun isPlainNumber(text: String): Boolean {
        if (text.isBlank()) return false
        
        // Already has commas = already formatted
        if (text.contains(',')) return false
        
        // Check if it matches number pattern (digits with optional decimal)
        val pattern = """^\d+(\.\d+)?$""".toRegex()
        return pattern.matches(text)
    }
    
    /**
     * Smart check: Should this number be formatted?
     * Returns true only for regular amounts (not phone, card, tracking, etc.)
     */
    fun shouldFormatAsAmount(
        number: String, 
        textBefore: String = "",
        editorInfo: EditorInfo? = null
    ): Boolean {
        if (!isPlainNumber(number)) return false
        
        // Don't format phone numbers
        if (isPhoneNumber(number, textBefore)) {
            return false
        }
        
        // Don't format credit card numbers
        if (isCreditCardNumber(number)) {
            return false
        }
        
        // Don't format tracking numbers
        if (isTrackingNumber(number, textBefore)) {
            return false
        }
        
        // Don't format in inappropriate field types
        if (!shouldFormatInField(editorInfo)) {
            return false
        }
        
        // Length check: typical amounts are 3-12 digits
        val digitsOnly = number.replace(".", "")
        if (digitsOnly.length < 3 || digitsOnly.length > 12) {
            return false
        }
        
        return true
    }
    
    /**
     * Format number with thousand separators
     * Examples: 
     * - "50000" → "50,000"
     * - "50000.00" → "50,000.00"
     * - "1234567" → "1,234,567"
     * - "1234567.89" → "1,234,567.89"
     */
    fun formatNumber(number: String): String {
        if (!isPlainNumber(number)) return number
        
        // Split by decimal point
        val parts = number.split('.')
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else null
        
        // Add commas to integer part (from right to left, every 3 digits)
        val formatted = StringBuilder()
        val reversed = integerPart.reversed()
        
        reversed.forEachIndexed { index, char ->
            if (index > 0 && index % 3 == 0) {
                formatted.append(',')
            }
            formatted.append(char)
        }
        
        // Reverse back and add decimal part if exists
        val result = formatted.reverse().toString()
        return if (decimalPart != null) {
            "$result.$decimalPart"
        } else {
            result
        }
    }
    
    /**
     * Get the word before cursor (up to space or punctuation)
     */
    fun getWordBeforeCursor(inputConnection: InputConnection?, maxLength: Int = 50): String? {
        if (inputConnection == null) return null
        
        val textBefore = inputConnection.getTextBeforeCursor(maxLength, 0) ?: return null
        
        // Find last word (after last space/punctuation that typically ends words)
        val lastWord = textBefore.toString()
            .split(' ', '\n', '\t', ';', ':', '!', '?', '(', ')')
            .lastOrNull()
            
        return lastWord?.takeIf { it.isNotBlank() }
    }
    
    /**
     * Get extended context before cursor (for pattern detection)
     */
    fun getTextBeforeCursor(inputConnection: InputConnection?, length: Int = 100): String {
        return inputConnection?.getTextBeforeCursor(length, 0)?.toString() ?: ""
    }
    
    /**
     * Check if number formatting should be applied based on input type
     */
    fun shouldFormatInField(editorInfo: EditorInfo?): Boolean {
        android.util.Log.d("NumberFormatter", "=== shouldFormatInField CALLED (Build: 2025-01-22 10:40) ===")
        
        if (editorInfo == null) {
            android.util.Log.d("NumberFormatter", "editorInfo is null, returning TRUE")
            return true
        }
        
        val inputType = editorInfo.inputType
        val typeClass = inputType and InputType.TYPE_MASK_CLASS
        
        android.util.Log.d("NumberFormatter", "inputType=0x${inputType.toString(16)}, typeClass=0x${typeClass.toString(16)}")
        
        // Only block these specific cases
        // Don't format in password fields
        if (inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD != 0) {
            android.util.Log.d("NumberFormatter", "BLOCKED: password field")
            return false
        }
        if (inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD != 0) {
            android.util.Log.d("NumberFormatter", "BLOCKED: visible password field")
            return false
        }
        if (inputType and InputType.TYPE_NUMBER_VARIATION_PASSWORD != 0) {
            android.util.Log.d("NumberFormatter", "BLOCKED: number password field")
            return false
        }
        
        // Don't format in email fields
        if (inputType and InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS != 0) {
            android.util.Log.d("NumberFormatter", "BLOCKED: email field")
            return false
        }
        
        // Don't format in URI fields
        if (inputType and InputType.TYPE_TEXT_VARIATION_URI != 0) {
            android.util.Log.d("NumberFormatter", "BLOCKED: URI field")
            return false
        }
        
        // Don't format in phone number fields
        if (typeClass == InputType.TYPE_CLASS_PHONE) {
            android.util.Log.d("NumberFormatter", "BLOCKED: phone field")
            return false
        }
        
        // Don't format in pure NUMBER fields (they don't allow commas)
        if (typeClass == InputType.TYPE_CLASS_NUMBER) {
            android.util.Log.d("NumberFormatter", "BLOCKED: NUMBER class (doesn't support commas)")
            return false
        }
        
        // Allow all other TEXT fields
        android.util.Log.d("NumberFormatter", "ALLOWED: TEXT field, returning TRUE")
        return true
    }
    
    private fun getTypeClassName(typeClass: Int): String {
        return when (typeClass) {
            InputType.TYPE_CLASS_TEXT -> "TEXT"
            InputType.TYPE_CLASS_NUMBER -> "NUMBER"
            InputType.TYPE_CLASS_PHONE -> "PHONE"
            InputType.TYPE_CLASS_DATETIME -> "DATETIME"
            else -> "UNKNOWN"
        }
    }
}
