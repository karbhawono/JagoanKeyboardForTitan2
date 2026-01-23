/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.ime to ai.jagoan.keyboard.titan2.ime
 * - Updated domain model imports
 * - Added SYM+C currency shortcut feature for quick currency symbol insertion
 * - Added getCurrencyForCountryCode() function supporting 50+ country codes
 * - Added currency shortcut mode tracking (currencyShortcutMode, currencyCountryCode, currencyModeStartTime)
 * - Added handleCurrencyShortcut() function for processing 2-letter country codes
 * - Added clearCurrencyShortcutMode() function for mode cleanup
 * - Modified insertSymbol() to automatically add space after currency symbols
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

package ai.jagoan.keyboard.titan2.ime

import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import ai.jagoan.keyboard.titan2.domain.model.KeyEventResult
import ai.jagoan.keyboard.titan2.domain.model.KeyboardSettings
import ai.jagoan.keyboard.titan2.domain.model.ModifierState
import ai.jagoan.keyboard.titan2.domain.model.ModifiersState
import ai.jagoan.keyboard.titan2.domain.model.SymbolCategory
import ai.jagoan.keyboard.titan2.domain.model.SymbolData
import ai.jagoan.keyboard.titan2.domain.model.ViMode
import ai.jagoan.keyboard.titan2.domain.repository.ShortcutRepository
import ai.jagoan.keyboard.titan2.ui.ime.getSymbolIndexForKeyCode
import ai.jagoan.keyboard.titan2.util.LazyLog
import ai.jagoan.keyboard.titan2.util.NumberFormatter
import ai.jagoan.keyboard.titan2.util.PerformanceMonitor
import ai.jagoan.keyboard.titan2.util.StringBuilderPool
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Listener for modifier state changes
 */
interface ModifierStateListener {
    fun onModifierStateChanged(modifiersState: ModifiersState)
}

/**
 * Handles physical keyboard key events
 */
@Singleton
class KeyEventHandler @Inject constructor(
    private val shortcutRepository: ShortcutRepository,
    private val accentRepository: ai.jagoan.keyboard.titan2.data.AccentRepository,
    private val autocorrectManager: ai.jagoan.keyboard.titan2.engine.AutocorrectManager
) {

    private var currentSettings: KeyboardSettings = KeyboardSettings()
    private var currentEditorInfo: EditorInfo? = null
    private var lastSpaceTime: Long = 0L
    private var currentWord: StringBuilder = StringBuilder(64) // Pre-allocate with reasonable capacity

    // Track last replacement for undo on backspace
    private data class ReplacementInfo(
        val original: String,
        val replacement: String,
        val hadTrailingSpace: Boolean
    )
    private var lastReplacement: ReplacementInfo? = null
    private var skipNextShortcut: Boolean = false

    // Modifier state tracking
    private var modifiersState = ModifiersState()
    private var shiftKeyDownTime: Long = 0L
    private var altKeyDownTime: Long = 0L
    private var lastShiftTapTime: Long = 0L
    private var lastAltTapTime: Long = 0L
    private var modifierStateListener: ModifierStateListener? = null

    // Sym key tracking
    private var symKeyDownTime: Long = 0L
    private var lastSymTapTime: Long = 0L
    private var onSymKeyPressed: (() -> Unit)? = null
    private var onSymPickerDismiss: (() -> Unit)? = null
    private var isSymPickerVisible: Boolean = false
    private var currentSymbolCategory: SymbolCategory = SymbolCategory.PUNCTUATION

    // Suggestion update callback
    var onSuggestionUpdateNeeded: (() -> Unit)? = null

    // Vi mode state tracking
    private var viMode: ViMode = ViMode.DISABLED
    private var viModeListener: ((ViMode) -> Unit)? = null
    private var pendingViToggle = false
    private var pendingVKey = false
    private var pendingGKey = false
    private var pendingDKey = false
    private var pendingYKey = false
    private var pendingRKey = false
    private var viClipboard: String = ""
    private var viModeHandler: android.os.Handler = android.os.Handler(android.os.Looper.getMainLooper())

    // Currency shortcut tracking (SYM+C+CountryCode)
    private var currencyShortcutMode = false
    private var currencyCountryCode = StringBuilder()
    private var currencyModeStartTime = 0L

    // Accent cycling tracking
    private var accentKeyDownTime: Long = 0L
    private var accentBaseChar: Char? = null
    private var accentCycleList: List<String> = emptyList()
    private var accentCycleIndex: Int = 0
    private var accentCycleHandler: android.os.Handler? = null
    private var accentCycleRunnable: Runnable? = null

    companion object {
        private const val TAG = "KeyEventHandler"
        private const val DOUBLE_SPACE_THRESHOLD_MS = 500L
        private const val LONG_PRESS_THRESHOLD_MS = 500L
        private const val DOUBLE_TAP_THRESHOLD_MS = 300L // Max time between taps for double-tap
        private const val ACCENT_CYCLE_INTERVAL_MS = 450L // Time between accent cycles while holding key
        private const val ACCENT_START_DELAY_MS = 500L // Delay before starting accent cycling
        private const val CURRENCY_MODE_TIMEOUT_MS = 3000L // 3 seconds to complete country code
    }

    /**
     * Set the listener for modifier state changes
     */
    fun setModifierStateListener(listener: ModifierStateListener?) {
        modifierStateListener = listener
    }

    /**
     * Set the listener for Vi mode state changes
     */
    fun setViModeListener(listener: ((ViMode) -> Unit)?) {
        viModeListener = listener
    }

    /**
     * Get current Vi mode state
     */
    fun getViMode(): ViMode = viMode

    /**
     * Toggle Vi mode on/off
     */
    private fun toggleViMode() {
        viMode = when (viMode) {
            ViMode.DISABLED -> ViMode.ENABLED
            ViMode.ENABLED -> ViMode.DISABLED
        }
        viModeListener?.invoke(viMode)
        Log.d(TAG, "Vi mode toggled to: $viMode")
    }

    /**
     * Set the callback for Sym key presses
     */
    fun setSymKeyPressedCallback(callback: () -> Unit) {
        onSymKeyPressed = callback
    }

    /**
     * Set the callback for dismissing the symbol picker
     */
    fun setSymPickerDismissCallback(callback: () -> Unit) {
        onSymPickerDismiss = callback
    }

    /**
     * Update the symbol picker visibility state
     */
    fun setSymPickerVisible(visible: Boolean) {
        LazyLog.d(TAG) { "setSymPickerVisible called: $visible (was: $isSymPickerVisible)" }
        isSymPickerVisible = visible
    }

    /**
     * Update the current symbol category
     */
    fun setSymbolCategory(category: SymbolCategory) {
        LazyLog.d(TAG) { "setSymbolCategory called: $category (was: $currentSymbolCategory)" }
        currentSymbolCategory = category
    }

    /**
     * Insert a symbol from the symbol picker
     * Automatically adds a space after currency symbols for better UX
     */
    fun insertSymbol(symbol: String, inputConnection: InputConnection?) {
        // Detailed debug logging to diagnose currency detection
        Log.d(TAG, "insertSymbol: CHECKING symbol='$symbol' (length=${symbol.length}, bytes=${symbol.toByteArray().contentToString()})")
        
        // Check if this is a currency symbol and add space after it
        val isCurrency = ai.jagoan.keyboard.titan2.domain.model.SymbolData.isCurrencySymbol(symbol)
        
        Log.d(TAG, "insertSymbol: isCurrencySymbol result=$isCurrency")
        
        val textToInsert = if (isCurrency) {
            "$symbol "
        } else {
            symbol
        }
        Log.d(TAG, "insertSymbol: final text='$textToInsert' (will insert '${textToInsert.replace(" ", "<SPACE>")}')")
        inputConnection?.commitText(textToInsert, 1)
        
        // Reset autocorrect state after inserting symbol
        autocorrectManager.reset()
        
        // Clear one-shot modifiers after inserting
        clearOneShotModifiers()
    }

    /**
     * Dismiss the symbol picker overlay
     */
    fun dismissSymbolPicker() {
        // Update modifier state to hide symbol picker
        updateModifierState(modifiersState.copy(symPickerVisible = false))
    }

    /**
     * Get the current modifier state
     */
    fun getModifiersState(): ModifiersState = modifiersState

    /**
     * Update the current settings
     */
    fun updateSettings(settings: KeyboardSettings) {
        currentSettings = settings
    }

    /**
     * Clean up resources when service is destroyed
     */
    fun cleanup() {
        // Cancel any pending accent cycling
        accentCycleRunnable?.let { runnable ->
            accentCycleHandler?.removeCallbacks(runnable)
        }
        accentCycleHandler = null
        accentCycleRunnable = null
        
        // Clear state
        accentBaseChar = null
        accentCycleList = emptyList()
        accentCycleIndex = 0
        
        LazyLog.d(TAG) { "KeyEventHandler cleanup completed" }
    }

    /**
     * Update the current editor info
     */
    fun updateEditorInfo(editorInfo: EditorInfo?) {
        currentEditorInfo = editorInfo
    }

    /**
     * Called when input is started - check if we should activate auto-cap shift
     */
    fun onInputStarted(inputConnection: InputConnection?) {
        inputConnection ?: return
        // Reset modifier state when starting new input
        updateModifierState(ModifiersState())
        // Don't auto-activate shift on input start - wait for first key press
        // This prevents the shift indicator from showing on home screen or non-text fields
    }

    /**
     * Handle a key event from the physical keyboard
     * @param event The key event to handle
     * @param inputConnection The current input connection
     * @return KeyEventResult indicating whether the event was handled
     */
    fun handleKeyDown(event: KeyEvent, inputConnection: InputConnection?): KeyEventResult = PerformanceMonitor.measure("key_down_event") {
        // Log all key events with full metaState for debugging
        Log.d(TAG, "handleKeyDown: keyCode=${event.keyCode}, char=${event.unicodeChar.toChar()}, metaState=0x${event.metaState.toString(16)}, " +
                "META_FUNCTION=${(event.metaState and KeyEvent.META_FUNCTION_ON) != 0}, " +
                "META_ALT=${(event.metaState and KeyEvent.META_ALT_ON) != 0}, " +
                "META_SHIFT=${(event.metaState and KeyEvent.META_SHIFT_ON) != 0}")
        
        // Log SPACE key at the very start
        if (event.keyCode == KeyEvent.KEYCODE_SPACE) {
            Log.d(TAG, ">>> SPACE KEY RECEIVED at handleKeyDown entry <<<")
        }
        
        LazyLog.d(TAG) { "handleKeyDown: keyCode=${event.keyCode}, modifiers=shift:${modifiersState.shift}/alt:${modifiersState.alt}" }
        inputConnection ?: return@measure KeyEventResult.NotHandled

        // Check if currency shortcut mode timed out
        if (currencyShortcutMode && (event.eventTime - currencyModeStartTime) > CURRENCY_MODE_TIMEOUT_MS) {
            LazyLog.d(TAG) { "Currency shortcut mode timed out" }
            clearCurrencyShortcutMode()
        }

        // Handle currency shortcut mode (SYM+C+CountryCode)
        if (currencyShortcutMode) {
            return handleCurrencyShortcut(event, inputConnection)
        }

        // Handle symbol picker interaction
        LazyLog.d(TAG) { "handleKeyDown: isSymPickerVisible=$isSymPickerVisible, keyCode=${event.keyCode}" }
        
        if (isSymPickerVisible && event.keyCode != KeyEvent.KEYCODE_SYM) {
            LazyLog.d(TAG) { "Symbol picker is visible, checking if key is letter key" }
            
            // Check if this is a letter key (A-Z) for symbol selection
            val symbolIndex = getSymbolIndexForKeyCode(event.keyCode)
            LazyLog.d(TAG) { "Symbol index for keyCode ${event.keyCode}: $symbolIndex" }
            
            if (symbolIndex != null) {
                // Get the symbol at this index from the current category
                val symbols = SymbolData.getSymbolsForCategory(currentSymbolCategory)
                LazyLog.d(TAG) { "Current category: $currentSymbolCategory, symbols count: ${symbols.size}" }
                
                if (symbolIndex < symbols.size) {
                    val symbol = symbols[symbolIndex]
                    
                    LazyLog.d(TAG) { "Symbol selected from physical key: ${symbol.character}" }
                    
                    // Insert the symbol (use insertSymbol for currency detection)
                    insertSymbol(symbol.character, inputConnection)
                    LazyLog.d(TAG) { "Symbol inserted, dismissing picker" }
                    
                    // Dismiss the picker after inserting
                    onSymPickerDismiss?.invoke()
                    
                    return@measure KeyEventResult.Handled
                } else {
                    LazyLog.w(TAG) { "Symbol index $symbolIndex out of bounds for category $currentSymbolCategory (size: ${symbols.size})" }
                }
            } else {
                LazyLog.d(TAG) { "Key ${event.keyCode} is not a letter key, not handling" }
            }
            
            // Check if this is SYM+C to enter currency mode
            if (event.keyCode == KeyEvent.KEYCODE_C) {
                currencyShortcutMode = true
                currencyModeStartTime = event.eventTime
                currencyCountryCode.clear()
                
                Log.d(TAG, "Currency shortcut mode activated: SYM+C")
                
                onSymPickerDismiss?.invoke()
                return KeyEventResult.Handled
            }
            
            // Dismiss picker on other keys (space, enter, back, etc.)
            onSymPickerDismiss?.invoke()

            // Consume back key to prevent Android from also handling it
            if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                return KeyEventResult.Handled
            }
        }

        // Handle key repeats (repeatCount > 0)
        if (event.repeatCount > 0) {
            // Always allow backspace to repeat
            if (event.keyCode == KeyEvent.KEYCODE_DEL) {
                return KeyEventResult.NotHandled
            }

            // Handle accent cycling for letter keys if enabled
            if (currentSettings.longPressAccents && isLetterKey(event.keyCode) && accentKeyDownTime > 0) {
                val pressDuration = event.eventTime - accentKeyDownTime

                // Start cycling after the initial delay
                if (pressDuration >= ACCENT_START_DELAY_MS && accentCycleList.isNotEmpty()) {
                    // Calculate which accent to show based on press duration
                    val cyclesSinceStart = ((pressDuration - ACCENT_START_DELAY_MS) / ACCENT_CYCLE_INTERVAL_MS).toInt()
                    val newIndex = (cyclesSinceStart + 1) % accentCycleList.size  // +1 because we start at index 0

                    if (newIndex != accentCycleIndex) {
                        // Cycle to next accent
                        accentCycleIndex = newIndex

                        // Delete previous character and insert new one
                        inputConnection.deleteSurroundingText(1, 0)
                        inputConnection.commitText(accentCycleList[accentCycleIndex], 1)

                        Log.d(TAG, "Accent cycling: ${accentCycleList[accentCycleIndex]} (index $accentCycleIndex/${accentCycleList.size})")
                    }
                }
                return KeyEventResult.Handled
            }

            // Handle long-press capitalization for letter keys (legacy behavior)
            if (currentSettings.longPressCapitalize && isLetterKey(event.keyCode)) {
                val char = getCharForKeyCode(event.keyCode)
                if (char != null) {
                    if (currentSettings.keyRepeatEnabled) {
                        // Key repeat is enabled: output uppercase on all repeats (appends)
                        inputConnection.commitText(char.uppercase(), 1)
                        return KeyEventResult.Handled
                    } else {
                        // Key repeat is disabled: replace lowercase with uppercase on first repeat
                        if (event.repeatCount == 1) {
                            // Delete the lowercase letter we just typed
                            inputConnection.deleteSurroundingText(1, 0)
                            // Replace it with uppercase
                            inputConnection.commitText(char.uppercase(), 1)
                        }
                        // Block this and all subsequent repeats
                        return KeyEventResult.Handled
                    }
                }
            }

            // Block all other key repeats if key repeat is disabled
            if (!currentSettings.keyRepeatEnabled) {
                return KeyEventResult.Handled
            }

            // If key repeat is enabled, let system handle the repeat
            return KeyEventResult.NotHandled
        }

        // Handle first key press (repeatCount == 0)

        // Check if we should activate auto-cap shift before processing this key
        checkAndActivateAutoCapShift(inputConnection)

        // Handle Vi mode toggle (TAB+V+I sequence)
        // TAB key is keyCode 61 on Titan 2
        
        // Log key press for debugging Vi mode toggle
        if (event.keyCode == KeyEvent.KEYCODE_TAB || event.keyCode == KeyEvent.KEYCODE_V || 
            event.keyCode == KeyEvent.KEYCODE_I || pendingVKey) {
            Log.d(TAG, "Vi toggle check: keyCode=${event.keyCode}, pendingVKey=$pendingVKey, viMode=$viMode")
        }
        
        // Detect TAB+V (TAB key is keyCode 61)
        if (event.keyCode == KeyEvent.KEYCODE_TAB) {
            pendingVKey = false // Reset on TAB press
            pendingViToggle = true
            Log.d(TAG, "TAB key detected, waiting for V")
            return KeyEventResult.Handled
        }
        
        // Detect V after TAB
        if (pendingViToggle && event.keyCode == KeyEvent.KEYCODE_V) {
            pendingViToggle = false
            pendingVKey = true
            Log.d(TAG, "TAB+V detected, waiting for I")
            return KeyEventResult.Handled
        }
        
        // Detect I after TAB+V
        if (pendingVKey && event.keyCode == KeyEvent.KEYCODE_I) {
            pendingVKey = false
            toggleViMode()
            Log.d(TAG, "Vi mode toggled via TAB+V+I to: $viMode")
            return KeyEventResult.Handled
        }
        
        // Reset pending keys if other key is pressed
        if ((pendingViToggle || pendingVKey) && 
            event.keyCode != KeyEvent.KEYCODE_V && 
            event.keyCode != KeyEvent.KEYCODE_I) {
            Log.d(TAG, "Resetting Vi toggle sequence, got keyCode=${event.keyCode}")
            pendingViToggle = false
            pendingVKey = false
        }

        // Handle Vi mode commands
        if (viMode == ViMode.ENABLED) {
            val viResult = handleViModeCommand(event, inputConnection)
            if (viResult == KeyEventResult.Handled) {
                return KeyEventResult.Handled
            }
        }

        // Handle modifier keys (Shift/Alt/Sym)
        when (event.keyCode) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                shiftKeyDownTime = event.eventTime
                return KeyEventResult.Handled
            }
            KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT -> {
                if (currentSettings.stickyAlt) {
                    altKeyDownTime = event.eventTime
                    return KeyEventResult.Handled
                }
            }
            KeyEvent.KEYCODE_SYM, 253 -> { // 253 is AGUI_SYM on Titan 2 hardware
                Log.d(TAG, "SYM key DOWN detected, keyCode=${event.keyCode}")
                symKeyDownTime = event.eventTime
                return KeyEventResult.Handled
            }
        }

        // Handle Alt+Backspace behavior
        if (event.keyCode == KeyEvent.KEYCODE_DEL && modifiersState.isAltActive()) {
            if (currentSettings.altBackspaceDeleteLine) {
                // Delete entire line before cursor (everything from last newline or start of text to cursor)
                val textBeforeCursor = inputConnection.getTextBeforeCursor(1000, 0)
                if (textBeforeCursor != null && textBeforeCursor.isNotEmpty()) {
                    val lastNewlineIndex = textBeforeCursor.lastIndexOf('\n')
                    val deleteCount = if (lastNewlineIndex >= 0) {
                        // Delete everything after the last newline
                        textBeforeCursor.length - lastNewlineIndex - 1
                    } else {
                        // No newline found, delete everything
                        textBeforeCursor.length
                    }

                    if (deleteCount > 0) {
                        inputConnection.deleteSurroundingText(deleteCount, 0)
                    }
                }

                // Clear one-shot modifiers after use
                clearOneShotModifiers()
                return KeyEventResult.Handled
            } else {
                // Setting is OFF: treat Alt+Backspace as regular backspace (ignore Alt modifier)
                // Clear one-shot modifiers and let the regular backspace handler process it
                clearOneShotModifiers()
                // Fall through to let system handle regular backspace (don't send Alt modifier)
                return KeyEventResult.NotHandled
            }
        }

        // Handle backspace - check if we should undo last replacement
        if (event.keyCode == KeyEvent.KEYCODE_DEL && lastReplacement != null) {
            val replacement = lastReplacement!!
            // Check if the text before cursor matches our replacement
            val textBefore = inputConnection.getTextBeforeCursor(replacement.replacement.length + 1, 0)
            val expectedText = if (replacement.hadTrailingSpace) {
                replacement.replacement + " "
            } else {
                replacement.replacement
            }

            if (textBefore?.toString() == expectedText) {
                // Undo the replacement - restore original word
                val deleteCount = if (replacement.hadTrailingSpace) {
                    replacement.replacement.length + 1
                } else {
                    replacement.replacement.length
                }
                inputConnection.deleteSurroundingText(deleteCount, 0)
                inputConnection.commitText(replacement.original, 1)
                lastReplacement = null
                // Skip next shortcut check to prevent re-triggering on the next space
                skipNextShortcut = true
                return KeyEventResult.Handled
            }
            // Text doesn't match, clear the replacement tracking
            lastReplacement = null
        }

        // Handle backspace - check if we should undo autocorrect
        if (event.keyCode == KeyEvent.KEYCODE_DEL && currentSettings.autocorrectEnabled) {
            if (autocorrectManager.handleBackspace()) {
                // Autocorrect undo available
                val undoWord = autocorrectManager.getUndoWord()
                if (undoWord != null) {
                    // Delete the corrected word and space, restore original
                    val correctedLength = inputConnection.getTextBeforeCursor(50, 0)?.toString()
                        ?.trimEnd()?.takeLastWhile { !it.isWhitespace() }?.length ?: 0
                    if (correctedLength > 0) {
                        inputConnection.deleteSurroundingText(correctedLength + 1, 0)  // +1 for space
                        inputConnection.commitText(undoWord, 1)
                        autocorrectManager.clearUndo()
                        LazyLog.d(TAG) { "Autocorrect undone, restored: $undoWord" }
                        return KeyEventResult.Handled
                    }
                }
            } else {
                // Not undoing, just handle backspace for autocorrect tracking
                autocorrectManager.handleBackspace()
                // Update suggestions after backspace
                Log.d(TAG, "Backspace pressed, invoking suggestion update callback")
                onSuggestionUpdateNeeded?.invoke()
            }
        }

        // Track characters for autocorrect
        Log.d(TAG, "handleKeyDown: Processing key ${event.keyCode}, callback null? ${onSuggestionUpdateNeeded == null}")
        if (event.keyCode >= KeyEvent.KEYCODE_A && event.keyCode <= KeyEvent.KEYCODE_Z) {
            // If we have a current word that wasn't committed (user kept typing after space showed suggestions)
            // commit it now as they're starting a new word
            if (autocorrectManager.getCurrentWord().isNotEmpty() && 
                inputConnection.getTextBeforeCursor(1, 0)?.toString()?.lastOrNull()?.isWhitespace() == true) {
                val previousWord = autocorrectManager.getCurrentWord()
                autocorrectManager.commitWord(previousWord)
            }
            
            val char = ('a' + (event.keyCode - KeyEvent.KEYCODE_A))
            Log.d(TAG, "Adding character to autocorrect: $char")
            autocorrectManager.addCharacter(char)
            // Trigger suggestion update after adding character
            Log.d(TAG, "Letter typed: $char, callback is null? ${onSuggestionUpdateNeeded == null}, invoking...")
            onSuggestionUpdateNeeded?.invoke()
            Log.d(TAG, "Callback invoked (if not null)")
        } else if (event.keyCode == KeyEvent.KEYCODE_APOSTROPHE) {
            // Track apostrophes for contractions (e.g., don't, can't)
            autocorrectManager.addCharacter('\'')
            // Trigger suggestion update after adding character
            Log.d(TAG, "Apostrophe typed, invoking suggestion update callback")
            onSuggestionUpdateNeeded?.invoke()
        }

        // Clear tracking on any non-backspace key
        if (event.keyCode != KeyEvent.KEYCODE_DEL) {
            lastReplacement = null
        }

        // Block ALT+Space to prevent keyboard switcher UI and symbol picker (must come before shortcuts!)
        // Check both sticky modifier state AND event meta state (for non-sticky ALT)
        val hasAltMeta = (event.metaState and KeyEvent.META_ALT_ON) != 0 ||
                         (event.metaState and KeyEvent.META_ALT_LEFT_ON) != 0 ||
                         (event.metaState and KeyEvent.META_ALT_RIGHT_ON) != 0
        if (event.keyCode == KeyEvent.KEYCODE_SPACE && (modifiersState.isAltActive() || hasAltMeta)) {
            Log.d(TAG, "ALT+Space blocked (sticky=${modifiersState.isAltActive()}, meta=$hasAltMeta)")
            
            // Auto-unlock ALT since the combination is blocked
            if (modifiersState.isAltActive()) {
                Log.d(TAG, "Auto-unlocking ALT after blocking ALT+Space")
                clearOneShotModifiers()
            }
            
            return KeyEventResult.Handled
        }

        // Check for text shortcuts on word boundary keys (space, enter, punctuation)
        if (currentSettings.textShortcutsEnabled && isWordBoundary(event.keyCode)) {
            // If we just undid a replacement, skip this shortcut check
            if (skipNextShortcut) {
                skipNextShortcut = false
                // Let the boundary character be inserted normally
                return KeyEventResult.NotHandled
            }

            val replacementResult = checkAndReplaceShortcut(inputConnection, event.keyCode)
            if (replacementResult != null) {
                // Shortcut was replaced, handle double-space for space key
                if (event.keyCode == KeyEvent.KEYCODE_SPACE && currentSettings.doubleSpacePeriod) {
                    val currentTime = System.currentTimeMillis()
                    if (lastSpaceTime > 0 && (currentTime - lastSpaceTime) <= DOUBLE_SPACE_THRESHOLD_MS) {
                        // Double space after replacement - replace the trailing space with period+space
                        inputConnection.deleteSurroundingText(1, 0)
                        inputConnection.commitText(". ", 1)
                        // Update lastReplacement to reflect the space is gone
                        lastReplacement = lastReplacement?.copy(hadTrailingSpace = false)
                        lastSpaceTime = 0L
                        return KeyEventResult.Handled
                    } else {
                        lastSpaceTime = currentTime
                    }
                }
                // Shortcut was replaced and space/punctuation was added
                return KeyEventResult.Handled
            }
        }

        // Handle auto-format numbers on space key
        if (event.keyCode == KeyEvent.KEYCODE_SPACE) {
            Log.d(TAG, "=== SPACE KEY PRESSED ===")
            Log.d(TAG, "autoFormatNumbers=${currentSettings.autoFormatNumbers}")
            Log.d(TAG, "inputConnection=$inputConnection")
            
            if (currentSettings.autoFormatNumbers && inputConnection != null) {
                // Log input type details
                val editorInfo = currentEditorInfo
                if (editorInfo != null) {
                    val inputType = editorInfo.inputType
                    val typeClass = inputType and android.text.InputType.TYPE_MASK_CLASS
                    val typeClassName = when (typeClass) {
                        android.text.InputType.TYPE_CLASS_TEXT -> "TEXT"
                        android.text.InputType.TYPE_CLASS_NUMBER -> "NUMBER"
                        android.text.InputType.TYPE_CLASS_PHONE -> "PHONE"
                        android.text.InputType.TYPE_CLASS_DATETIME -> "DATETIME"
                        else -> "UNKNOWN($typeClass)"
                    }
                    Log.d(TAG, "InputType: 0x${inputType.toString(16)}, class=$typeClassName")
                    
                    // Check if this is a pure NUMBER field (which blocks commas)
                    if (typeClass == android.text.InputType.TYPE_CLASS_NUMBER) {
                        Log.d(TAG, "⚠️ BLOCKING: This is a NUMBER field - commas not allowed!")
                        Log.d(TAG, "💡 TIP: Number formatting only works in TEXT fields (Notes, Messages, etc)")
                    }
                }
                
                val shouldFormat = NumberFormatter.shouldFormatInField(editorInfo)
                Log.d(TAG, "shouldFormatInField=$shouldFormat, editorInfo=$editorInfo")
                
                if (shouldFormat) {
                    // Get raw text before cursor for debugging
                    val rawText = inputConnection.getTextBeforeCursor(50, 0)
                    Log.d(TAG, "Raw text before cursor (50 chars): '$rawText'")
                    
                    val wordBefore = NumberFormatter.getWordBeforeCursor(inputConnection)
                    val textBefore = NumberFormatter.getTextBeforeCursor(inputConnection, 100)
                    
                    Log.d(TAG, "wordBefore='$wordBefore' (length=${wordBefore?.length})")
                    Log.d(TAG, "textBefore (last 30): '${textBefore.takeLast(30)}'")
                    
                    if (wordBefore != null) {
                        // Check each condition individually
                        val isPlain = NumberFormatter.isPlainNumber(wordBefore)
                        val isPhone = NumberFormatter.isPhoneNumber(wordBefore, textBefore)
                        val isCard = NumberFormatter.isCreditCardNumber(wordBefore)
                        val isTracking = NumberFormatter.isTrackingNumber(wordBefore, textBefore)
                        val digitsOnly = wordBefore.replace(".", "")
                        val lengthOk = digitsOnly.length >= 3 && digitsOnly.length <= 12
                        
                        Log.d(TAG, "Checks: isPlain=$isPlain, isPhone=$isPhone, isCard=$isCard, isTracking=$isTracking")
                        Log.d(TAG, "Length: digitsOnly='$digitsOnly' (${digitsOnly.length} chars), lengthOk=$lengthOk")
                        
                        val shouldFormatAsAmount = NumberFormatter.shouldFormatAsAmount(wordBefore, textBefore, editorInfo)
                        Log.d(TAG, "shouldFormatAsAmount=$shouldFormatAsAmount for '$wordBefore'")
                        
                        if (shouldFormatAsAmount) {
                            val formatted = NumberFormatter.formatNumber(wordBefore)
                            Log.d(TAG, "formatNumber: '$wordBefore' → '$formatted' (changed=${formatted != wordBefore})")
                            
                            if (formatted != wordBefore) {
                                // Delete the plain number
                                Log.d(TAG, "Deleting $wordBefore.length chars and inserting '$formatted '")
                                inputConnection.deleteSurroundingText(wordBefore.length, 0)
                                
                                // Insert formatted number with space
                                inputConnection.commitText("$formatted ", 1)
                                
                                Log.d(TAG, "✅ SUCCESS: Auto-formatted number: $wordBefore → $formatted")
                                
                                // Return Handled to prevent double space
                                return KeyEventResult.Handled
                            } else {
                                Log.d(TAG, "Number already formatted or doesn't need formatting")
                            }
                        } else {
                            Log.d(TAG, "❌ shouldFormatAsAmount=false, reasons:")
                            if (!isPlain) Log.d(TAG, "  - not a plain number")
                            if (isPhone) Log.d(TAG, "  - detected as phone number")
                            if (isCard) Log.d(TAG, "  - detected as credit card")
                            if (isTracking) Log.d(TAG, "  - detected as tracking number")
                            if (!lengthOk) Log.d(TAG, "  - length check failed (${digitsOnly.length} not in 3..12)")
                        }
                    } else {
                        Log.d(TAG, "❌ wordBefore is null - nothing to format before cursor")
                    }
                } else {
                    Log.d(TAG, "❌ shouldFormatInField=false for this field type")
                }
            } else {
                if (!currentSettings.autoFormatNumbers) {
                    Log.d(TAG, "❌ autoFormatNumbers is disabled in settings")
                }
                if (inputConnection == null) {
                    Log.d(TAG, "❌ inputConnection is null!")
                }
            }
            Log.d(TAG, "=== END SPACE KEY ===")
        }

        // Handle autocorrect on space key
        if (event.keyCode == KeyEvent.KEYCODE_SPACE && currentSettings.autocorrectEnabled) {
            val correctedWord = autocorrectManager.handleSpace()
            if (correctedWord != null) {
                // Auto-correction applied (contraction)
                val undoWord = autocorrectManager.getUndoWord()
                if (undoWord != null) {
                    // Delete the original word and insert corrected word with space
                    inputConnection.deleteSurroundingText(undoWord.length, 0)
                    inputConnection.commitText("$correctedWord ", 1)
                    LazyLog.d(TAG) { "Autocorrect applied: $undoWord -> $correctedWord" }
                    // Return Handled to prevent double space insertion
                    return KeyEventResult.Handled
                }
            }
            // If no auto-correction, word is either correct or has suggestions showing
            // Let space be inserted normally, suggestions will be shown in bar
        }

        // Handle double-space period (for non-shortcut cases)
        if (event.keyCode == KeyEvent.KEYCODE_SPACE && currentSettings.doubleSpacePeriod) {
            val currentTime = System.currentTimeMillis()
            if (lastSpaceTime > 0 && (currentTime - lastSpaceTime) <= DOUBLE_SPACE_THRESHOLD_MS) {
                // Double space detected - replace last space with period and space
                inputConnection.deleteSurroundingText(1, 0)
                inputConnection.commitText(". ", 1)
                lastSpaceTime = 0L  // Reset to prevent triple-space issues
                return KeyEventResult.Handled
            } else {
                // Single space - record the time and let system handle it
                lastSpaceTime = currentTime
                return KeyEventResult.NotHandled
            }
        }

        // Reset space timer if any other key is pressed
        if (event.keyCode != KeyEvent.KEYCODE_SPACE) {
            lastSpaceTime = 0L
        }

        // Handle word boundaries for autocorrect
        if (isWordBoundary(event.keyCode) && event.keyCode != KeyEvent.KEYCODE_SPACE) {
            autocorrectManager.handleWordBoundary()
        }

        // Clear skip flag when typing a letter or other non-boundary key
        if (!isWordBoundary(event.keyCode)) {
            skipNextShortcut = false
        }

        // Handle Alt modifier - send key event with Alt meta state
        if (modifiersState.isAltActive()) {
            Log.d(TAG, "Sending key with Alt modifier: keyCode=${event.keyCode}")

            // Create key events with Alt modifier
            val downTime = event.downTime
            val eventTime = event.eventTime

            val altDownEvent = KeyEvent(
                downTime,
                eventTime,
                KeyEvent.ACTION_DOWN,
                event.keyCode,
                0, // repeat
                KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON,
                event.deviceId,
                event.scanCode
            )

            val altUpEvent = KeyEvent(
                downTime,
                eventTime,
                KeyEvent.ACTION_UP,
                event.keyCode,
                0, // repeat
                KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON,
                event.deviceId,
                event.scanCode
            )

            // Send the key events with Alt modifier
            inputConnection.sendKeyEvent(altDownEvent)
            inputConnection.sendKeyEvent(altUpEvent)

            // Clear one-shot modifiers after use
            clearOneShotModifiers()
            return KeyEventResult.Handled
        }

        // Handle letter keys with shift modifier
        if (isLetterKey(event.keyCode)) {
            val char = getCharForKeyCode(event.keyCode)
            if (char != null) {
                // Check if we should apply shift modifier
                if (modifiersState.isShiftActive()) {
                    // Commit the capitalized character
                    inputConnection.commitText(char.uppercase(), 1)

                    // Clear one-shot modifiers after use
                    clearOneShotModifiers()
                    return KeyEventResult.Handled
                }

                // Start tracking for accent cycling if enabled and no modifiers active
                if (currentSettings.longPressAccents && !modifiersState.isShiftActive() && !modifiersState.isAltActive()) {
                    val baseChar = if (modifiersState.shift == ModifierState.ONE_SHOT) char[0].uppercaseChar() else char[0]
                    val accentCycle = accentRepository.getAccentCycle(currentSettings.selectedLanguage, baseChar)

                    if (accentCycle.isNotEmpty()) {
                        // This letter has accents, track it for cycling
                        accentKeyDownTime = event.eventTime
                        accentBaseChar = baseChar
                        accentCycleList = accentCycle
                        accentCycleIndex = 0  // Start with base character
                    }
                }
            }
        }

        // Let system handle all other first presses normally
        if (event.keyCode == KeyEvent.KEYCODE_SPACE) {
            Log.d(TAG, ">>> SPACE KEY: Returning NotHandled (reached end of function) <<<")
        }
        return KeyEventResult.NotHandled
    }

    /**
     * Get the character for a letter key code
     */
    private fun getCharForKeyCode(keyCode: Int): String? {
        return when (keyCode) {
            KeyEvent.KEYCODE_A -> "a"
            KeyEvent.KEYCODE_B -> "b"
            KeyEvent.KEYCODE_C -> "c"
            KeyEvent.KEYCODE_D -> "d"
            KeyEvent.KEYCODE_E -> "e"
            KeyEvent.KEYCODE_F -> "f"
            KeyEvent.KEYCODE_G -> "g"
            KeyEvent.KEYCODE_H -> "h"
            KeyEvent.KEYCODE_I -> "i"
            KeyEvent.KEYCODE_J -> "j"
            KeyEvent.KEYCODE_K -> "k"
            KeyEvent.KEYCODE_L -> "l"
            KeyEvent.KEYCODE_M -> "m"
            KeyEvent.KEYCODE_N -> "n"
            KeyEvent.KEYCODE_O -> "o"
            KeyEvent.KEYCODE_P -> "p"
            KeyEvent.KEYCODE_Q -> "q"
            KeyEvent.KEYCODE_R -> "r"
            KeyEvent.KEYCODE_S -> "s"
            KeyEvent.KEYCODE_T -> "t"
            KeyEvent.KEYCODE_U -> "u"
            KeyEvent.KEYCODE_V -> "v"
            KeyEvent.KEYCODE_W -> "w"
            KeyEvent.KEYCODE_X -> "x"
            KeyEvent.KEYCODE_Y -> "y"
            KeyEvent.KEYCODE_Z -> "z"
            else -> null
        }
    }

    /**
     * Handle key up event
     */
    fun handleKeyUp(event: KeyEvent, inputConnection: InputConnection?): KeyEventResult = PerformanceMonitor.measure("key_up_event") {
        inputConnection ?: return KeyEventResult.NotHandled

        // Handle modifier key release
        when (event.keyCode) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                if (shiftKeyDownTime > 0) {
                    val pressDuration = event.eventTime - shiftKeyDownTime
                    val isLongPress = pressDuration >= LONG_PRESS_THRESHOLD_MS

                    // Check for double-tap (only on short presses)
                    val isDoubleTap = if (!isLongPress) {
                        val timeSinceLastTap = event.eventTime - lastShiftTapTime
                        timeSinceLastTap < DOUBLE_TAP_THRESHOLD_MS
                    } else {
                        false
                    }

                    // Double-tap acts like long-press (locks modifier)
                    val shouldLock = isLongPress || isDoubleTap

                    if (shouldLock) {
                        Log.d(TAG, "Shift: ${if (isDoubleTap) "double-tap" else "long-press"} detected, locking")
                    }

                    toggleShiftModifier(shouldLock)
                    shiftKeyDownTime = 0L

                    // Update last tap time for double-tap detection (only for short presses)
                    if (!isLongPress) {
                        lastShiftTapTime = event.eventTime
                    }

                    return KeyEventResult.Handled
                }
            }
            KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_ALT_RIGHT -> {
                if (currentSettings.stickyAlt && altKeyDownTime > 0) {
                    val pressDuration = event.eventTime - altKeyDownTime
                    val isLongPress = pressDuration >= LONG_PRESS_THRESHOLD_MS

                    // Check for double-tap (only on short presses)
                    val isDoubleTap = if (!isLongPress) {
                        val timeSinceLastTap = event.eventTime - lastAltTapTime
                        timeSinceLastTap < DOUBLE_TAP_THRESHOLD_MS
                    } else {
                        false
                    }

                    // Double-tap acts like long-press (locks modifier)
                    val shouldLock = isLongPress || isDoubleTap

                    if (shouldLock) {
                        Log.d(TAG, "Alt: ${if (isDoubleTap) "double-tap" else "long-press"} detected, locking")
                    }

                    toggleAltModifier(shouldLock)
                    altKeyDownTime = 0L

                    // Update last tap time for double-tap detection (only for short presses)
                    if (!isLongPress) {
                        lastAltTapTime = event.eventTime
                    }

                    return KeyEventResult.Handled
                }
            }
            KeyEvent.KEYCODE_SYM, 253 -> { // 253 is AGUI_SYM on Titan 2 hardware
                Log.d(TAG, "SYM key UP detected, keyCode=${event.keyCode}")
                // If in currency mode and Sym is released, cancel it
                if (currencyShortcutMode) {
                    Log.d(TAG, "Sym released during currency mode, cancelling")
                    clearCurrencyShortcutMode()
                    symKeyDownTime = 0L
                    return KeyEventResult.Handled
                }
                
                if (symKeyDownTime > 0) {
                    val pressDuration = event.eventTime - symKeyDownTime
                    val isLongPress = pressDuration >= LONG_PRESS_THRESHOLD_MS

                    // Check for double-tap (only on short presses)
                    val timeSinceLastTap = event.eventTime - lastSymTapTime
                    val isDoubleTap = !isLongPress && timeSinceLastTap < DOUBLE_TAP_THRESHOLD_MS

                    Log.d(TAG, "SYM UP: pressDuration=$pressDuration, isLongPress=$isLongPress, isDoubleTap=$isDoubleTap, callback=${onSymKeyPressed != null}")

                    if (isDoubleTap || isLongPress) {
                        // Double-tap or long-press: insert preferred currency symbol
                        Log.d(TAG, "SYM: Double-tap or long-press, inserting currency")
                        val currency = currentSettings.preferredCurrency?.takeIf { it.isNotEmpty() }
                            ?: ai.jagoan.keyboard.titan2.util.LocaleUtils.getDefaultCurrencySymbol()
                        inputConnection.commitText("$currency ", 1)
                        
                        // Reset autocorrect state after inserting currency
                        autocorrectManager.reset()

                        // Dismiss the symbol picker if it's visible (from first tap)
                        if (isSymPickerVisible) {
                            onSymPickerDismiss?.invoke()
                        }

                        // Reset tap time to prevent triple-tap issues
                        lastSymTapTime = 0L
                    } else {
                        // Short press: show picker or cycle category
                        Log.d(TAG, "SYM: Short press, invoking callback")
                        onSymKeyPressed?.invoke()
                        Log.d(TAG, "SYM: Callback invoked")
                        // Update last tap time for double-tap detection
                        lastSymTapTime = event.eventTime
                    }
                    symKeyDownTime = 0L
                    return KeyEventResult.Handled
                }
            }
        }

        // Reset accent tracking when letter key is released
        if (isLetterKey(event.keyCode) && accentKeyDownTime > 0) {
            accentKeyDownTime = 0L
            accentBaseChar = null
            accentCycleList = emptyList()
            accentCycleIndex = 0
            Log.d(TAG, "Accent tracking reset for keyCode=${event.keyCode}")
        }

        // Block key up for repeats if key repeat is disabled
        // (but always allow backspace and long-press capitalize)
        if (event.repeatCount > 0 && !currentSettings.keyRepeatEnabled &&
            event.keyCode != KeyEvent.KEYCODE_DEL &&
            !(currentSettings.longPressCapitalize && isLetterKey(event.keyCode))) {
            return KeyEventResult.Handled
        }

        return KeyEventResult.NotHandled
    }

    /**
     * Handle Vi mode commands
     */
    private fun handleViModeCommand(event: KeyEvent, ic: InputConnection): KeyEventResult {
        // Handle pending R key (replace character)
        if (pendingRKey) {
            val char = event.unicodeChar
            if (char != 0) {
                ic.beginBatchEdit()
                ic.deleteSurroundingText(0, 1)
                ic.commitText(char.toChar().toString(), 1)
                ic.endBatchEdit()
                pendingRKey = false
                Log.d(TAG, "Vi: Replace character with ${char.toChar()}")
            }
            return KeyEventResult.Handled
        }

        when (event.keyCode) {
            // Navigation: h, j, k, l
            KeyEvent.KEYCODE_H -> {
                moveCursorLeft(ic)
                return KeyEventResult.Handled
            }
            KeyEvent.KEYCODE_J -> {
                moveCursorDown(ic)
                return KeyEventResult.Handled
            }
            KeyEvent.KEYCODE_K -> {
                moveCursorUp(ic)
                return KeyEventResult.Handled
            }
            KeyEvent.KEYCODE_L -> {
                moveCursorRight(ic)
                return KeyEventResult.Handled
            }

            // Word navigation: w, b
            KeyEvent.KEYCODE_W -> {
                moveForwardWord(ic)
                return KeyEventResult.Handled
            }
            KeyEvent.KEYCODE_B -> {
                moveBackwardWord(ic)
                return KeyEventResult.Handled
            }

            // Line navigation: 0, $
            KeyEvent.KEYCODE_0 -> {
                moveToLineStart(ic)
                return KeyEventResult.Handled
            }
            KeyEvent.KEYCODE_4 -> { // $ key requires shift
                if (event.isShiftPressed) {
                    moveToLineEnd(ic)
                    return KeyEventResult.Handled
                }
                return KeyEventResult.NotHandled
            }

            // Document navigation: gg, G
            KeyEvent.KEYCODE_G -> {
                if (pendingGKey) {
                    moveToDocStart(ic)
                    pendingGKey = false
                    Log.d(TAG, "Vi: gg - move to document start")
                } else {
                    pendingGKey = true
                    viModeHandler.postDelayed({
                        if (pendingGKey) {
                            moveToDocEnd(ic)
                            pendingGKey = false
                            Log.d(TAG, "Vi: G - move to document end")
                        }
                    }, 500)
                }
                return KeyEventResult.Handled
            }

            // Editing: dd, yy
            KeyEvent.KEYCODE_D -> {
                if (pendingDKey) {
                    deleteCurrentLine(ic)
                    pendingDKey = false
                    Log.d(TAG, "Vi: dd - delete line")
                } else {
                    pendingDKey = true
                    viModeHandler.postDelayed({ pendingDKey = false }, 500)
                }
                return KeyEventResult.Handled
            }

            KeyEvent.KEYCODE_Y -> {
                if (pendingYKey) {
                    yankCurrentLine(ic)
                    pendingYKey = false
                    Log.d(TAG, "Vi: yy - yank line")
                } else {
                    pendingYKey = true
                    viModeHandler.postDelayed({ pendingYKey = false }, 500)
                }
                return KeyEventResult.Handled
            }

            // Paste: p
            KeyEvent.KEYCODE_P -> {
                paste(ic)
                Log.d(TAG, "Vi: p - paste")
                return KeyEventResult.Handled
            }

            // Undo: u
            KeyEvent.KEYCODE_U -> {
                undo(ic)
                Log.d(TAG, "Vi: u - undo")
                return KeyEventResult.Handled
            }

            // Delete character: x
            KeyEvent.KEYCODE_X -> {
                deleteCharUnderCursor(ic)
                Log.d(TAG, "Vi: x - delete character")
                return KeyEventResult.Handled
            }

            // Replace character: r
            KeyEvent.KEYCODE_R -> {
                pendingRKey = true
                Log.d(TAG, "Vi: r - waiting for replacement character")
                return KeyEventResult.Handled
            }

            else -> {
                // Reset pending states for unhandled keys
                pendingGKey = false
                pendingDKey = false
                pendingYKey = false
                return KeyEventResult.NotHandled
            }
        }
    }

    // Vi mode cursor movement functions

    private fun moveCursorLeft(ic: InputConnection) {
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT))
    }

    private fun moveCursorDown(ic: InputConnection) {
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN))
    }

    private fun moveCursorUp(ic: InputConnection) {
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP))
    }

    private fun moveCursorRight(ic: InputConnection) {
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
    }

    private fun moveForwardWord(ic: InputConnection) {
        val textAfter = ic.getTextAfterCursor(1000, 0)?.toString() ?: return
        
        // Skip current word's remaining characters
        var offset = 0
        while (offset < textAfter.length && textAfter[offset].isLetterOrDigit()) {
            offset++
        }
        
        // Skip whitespace
        while (offset < textAfter.length && textAfter[offset].isWhitespace()) {
            offset++
        }
        
        if (offset > 0) {
            // Move cursor forward
            for (i in 0 until offset) {
                moveCursorRight(ic)
            }
        }
    }

    private fun moveBackwardWord(ic: InputConnection) {
        val textBefore = ic.getTextBeforeCursor(1000, 0)?.toString() ?: return
        
        if (textBefore.isEmpty()) return
        
        var offset = 0
        var index = textBefore.length - 1
        
        // Skip whitespace
        while (index >= 0 && textBefore[index].isWhitespace()) {
            offset++
            index--
        }
        
        // Skip word characters
        while (index >= 0 && textBefore[index].isLetterOrDigit()) {
            offset++
            index--
        }
        
        if (offset > 0) {
            // Move cursor backward
            for (i in 0 until offset) {
                moveCursorLeft(ic)
            }
        }
    }

    private fun moveToLineStart(ic: InputConnection) {
        val textBefore = ic.getTextBeforeCursor(10000, 0)?.toString() ?: return
        val lastNewline = textBefore.lastIndexOf('\n')
        val offset = textBefore.length - lastNewline - 1
        
        if (offset > 0) {
            for (i in 0 until offset) {
                moveCursorLeft(ic)
            }
        }
    }

    private fun moveToLineEnd(ic: InputConnection) {
        val textAfter = ic.getTextAfterCursor(10000, 0)?.toString() ?: return
        val nextNewline = textAfter.indexOf('\n')
        val offset = if (nextNewline == -1) textAfter.length else nextNewline
        
        if (offset > 0) {
            for (i in 0 until offset) {
                moveCursorRight(ic)
            }
        }
    }

    private fun moveToDocStart(ic: InputConnection) {
        // Use Ctrl+Home to move to start
        val downTime = android.os.SystemClock.uptimeMillis()
        ic.sendKeyEvent(KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_HOME, 0, KeyEvent.META_CTRL_ON))
        ic.sendKeyEvent(KeyEvent(downTime, downTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MOVE_HOME, 0, KeyEvent.META_CTRL_ON))
    }

    private fun moveToDocEnd(ic: InputConnection) {
        // Use Ctrl+End to move to end
        val downTime = android.os.SystemClock.uptimeMillis()
        ic.sendKeyEvent(KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MOVE_END, 0, KeyEvent.META_CTRL_ON))
        ic.sendKeyEvent(KeyEvent(downTime, downTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MOVE_END, 0, KeyEvent.META_CTRL_ON))
    }

    // Vi mode editing functions

    private fun deleteCurrentLine(ic: InputConnection) {
        val textBefore = ic.getTextBeforeCursor(10000, 0)?.toString() ?: ""
        val textAfter = ic.getTextAfterCursor(10000, 0)?.toString() ?: ""
        
        // Find line boundaries
        val lineStart = textBefore.lastIndexOf('\n')
        val lineStartOffset = textBefore.length - lineStart - 1
        
        val lineEnd = textAfter.indexOf('\n')
        val lineEndOffset = if (lineEnd == -1) textAfter.length else lineEnd + 1 // Include newline
        
        // Extract current line
        val beforeLine = if (lineStart >= 0) textBefore.substring(lineStart + 1) else textBefore
        val afterLine = if (lineEnd >= 0) textAfter.substring(0, lineEnd) else textAfter
        val currentLine = beforeLine + afterLine
        
        // Store in clipboard
        viClipboard = currentLine
        
        // Delete the line
        ic.beginBatchEdit()
        ic.deleteSurroundingText(lineStartOffset, lineEndOffset)
        ic.endBatchEdit()
        
        Log.d(TAG, "Vi: Deleted line (${currentLine.length} chars)")
    }

    private fun yankCurrentLine(ic: InputConnection) {
        val textBefore = ic.getTextBeforeCursor(10000, 0)?.toString() ?: ""
        val textAfter = ic.getTextAfterCursor(10000, 0)?.toString() ?: ""
        
        // Find line boundaries
        val lineStart = textBefore.lastIndexOf('\n')
        val lineEnd = textAfter.indexOf('\n')
        
        // Extract current line
        val beforeLine = if (lineStart >= 0) textBefore.substring(lineStart + 1) else textBefore
        val afterLine = if (lineEnd >= 0) textAfter.substring(0, lineEnd) else textAfter
        val currentLine = beforeLine + afterLine
        
        // Store in clipboard
        viClipboard = currentLine
        
        Log.d(TAG, "Vi: Yanked line (${currentLine.length} chars)")
    }

    private fun paste(ic: InputConnection) {
        if (viClipboard.isNotEmpty()) {
            ic.commitText(viClipboard, 1)
            Log.d(TAG, "Vi: Pasted ${viClipboard.length} chars")
        }
    }

    private fun undo(ic: InputConnection) {
        // Send Ctrl+Z for undo
        val downTime = android.os.SystemClock.uptimeMillis()
        ic.sendKeyEvent(KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Z, 0, KeyEvent.META_CTRL_ON))
        ic.sendKeyEvent(KeyEvent(downTime, downTime, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_Z, 0, KeyEvent.META_CTRL_ON))
    }

    private fun deleteCharUnderCursor(ic: InputConnection) {
        ic.deleteSurroundingText(0, 1)
    }

    /**
     * Check if the given key code is a letter key (A-Z)
     */
    private fun isLetterKey(keyCode: Int): Boolean {
        return keyCode in KeyEvent.KEYCODE_A..KeyEvent.KEYCODE_Z
    }

    /**
     * Check if auto-capitalization conditions are met, and if so, activate shift in ONE_SHOT mode
     */
    private fun checkAndActivateAutoCapShift(inputConnection: InputConnection?) = PerformanceMonitor.measure("auto_cap_check") {
        if (!currentSettings.autoCapitalize) return@measure
        inputConnection ?: return@measure

        // Don't override existing modifier states
        if (modifiersState.isShiftActive() || modifiersState.isAltActive()) return@measure

        // Must have valid editor info
        val info = currentEditorInfo ?: return@measure

        val inputType = info.inputType
        val typeClass = inputType and InputType.TYPE_MASK_CLASS
        val typeVariation = inputType and InputType.TYPE_MASK_VARIATION

        // Only auto-capitalize for text input types
        if (typeClass != InputType.TYPE_CLASS_TEXT) {
            return@measure
        }

        // Don't auto-capitalize for specific text variations
        when (typeVariation) {
            InputType.TYPE_TEXT_VARIATION_URI,
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
            InputType.TYPE_TEXT_VARIATION_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
            InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD -> return@measure
        }

        // Check for NO_SUGGESTIONS flag (used by our shortcut dialog)
        if (inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS != 0) {
            return@measure
        }

        // Check text before cursor to determine if we should capitalize
        val textBeforeCursor = inputConnection.getTextBeforeCursor(100, 0)

        var shouldActivate = false

        if (textBeforeCursor.isNullOrEmpty()) {
            // Start of text, activate shift
            shouldActivate = true
        } else {
            // Get the last character
            val lastChar = textBeforeCursor.last()

            // Activate after newline
            if (lastChar == '\n') {
                shouldActivate = true
            }

            // Check for sentence-ending punctuation followed by space
            // This prevents auto-cap in URLs (www.github.com) and other period uses
            if (textBeforeCursor.length >= 2) {
                val secondToLast = textBeforeCursor[textBeforeCursor.length - 2]
                if (lastChar in listOf(' ', '\t') && secondToLast in listOf('.', '!', '?')) {
                    shouldActivate = true
                }
            }
        }

        if (shouldActivate) {
            // Activate shift in ONE_SHOT mode for auto-cap
            updateModifierState(ModifiersState(shift = ModifierState.ONE_SHOT, alt = ModifierState.NONE))
        }
    }

    /**
     * Check if a key code represents a word boundary
     */
    private fun isWordBoundary(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_SPACE,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_TAB,
            KeyEvent.KEYCODE_PERIOD,
            KeyEvent.KEYCODE_COMMA -> true
            else -> false
        }
    }

    /**
     * Check if the last word matches a shortcut and replace it
     * @param inputConnection The input connection
     * @param boundaryKeyCode The key code of the word boundary character (space, period, etc.)
     * @return The replacement text if a shortcut was found, null otherwise
     */
    private fun checkAndReplaceShortcut(inputConnection: InputConnection, boundaryKeyCode: Int): String? {
        // Get text before cursor to extract the last word
        val textBeforeCursor = inputConnection.getTextBeforeCursor(100, 0) ?: return null
        if (textBeforeCursor.isEmpty()) return null

        // Extract the last word (everything after the last word boundary)
        val lastWord = extractLastWord(textBeforeCursor)
        Log.d(TAG, "checkAndReplaceShortcut: lastWord='$lastWord'")
        if (lastWord.isEmpty()) return null

        // Check if this word has a shortcut replacement
        val replacement = shortcutRepository.findReplacement(lastWord)
        Log.d(TAG, "checkAndReplaceShortcut: lastWord='$lastWord' -> replacement='$replacement'")
        if (replacement == null) return null

        // Delete the original word
        inputConnection.deleteSurroundingText(lastWord.length, 0)

        // Get the trailing character for this boundary key
        val trailingChar = when (boundaryKeyCode) {
            KeyEvent.KEYCODE_SPACE -> " "
            KeyEvent.KEYCODE_ENTER -> "\n"
            KeyEvent.KEYCODE_TAB -> "\t"
            KeyEvent.KEYCODE_PERIOD -> "."
            KeyEvent.KEYCODE_COMMA -> ","
            else -> " " // Default to space
        }

        // Commit the replacement with the trailing character
        inputConnection.commitText(replacement + trailingChar, 1)

        // Track this replacement for potential undo
        lastReplacement = ReplacementInfo(
            original = lastWord,
            replacement = replacement,
            hadTrailingSpace = trailingChar == " "
        )

        return replacement
    }

    /**
     * Extract the last word from text (everything after the last word boundary)
     */
    private fun extractLastWord(text: CharSequence): String {
        val str = text.toString()
        var startIndex = str.length - 1

        // Find the start of the last word by going backwards until we hit a word boundary
        while (startIndex >= 0) {
            val char = str[startIndex]
            if (char.isWhitespace() || char in ".!?,;:\"'()[]{}") {
                break
            }
            startIndex--
        }

        // Extract the word (startIndex is now at the boundary, so add 1)
        return str.substring(startIndex + 1)
    }

    /**
     * Update modifier state and notify listener
     */
    private fun updateModifierState(newState: ModifiersState) {
        LazyLog.d(TAG) { "updateModifierState called: old=${modifiersState}, new=${newState}" }
        modifiersState = newState
        modifierStateListener?.onModifierStateChanged(newState)
    }

    /**
     * Toggle shift modifier state
     * @param isLongPress true if this is a long press (locked), false for one-shot
     */
    private fun toggleShiftModifier(isLongPress: Boolean) {
        val newShiftState = when {
            // If already locked, turn off
            modifiersState.shift == ModifierState.LOCKED -> ModifierState.NONE
            // If already one-shot (from auto-cap or manual)
            modifiersState.shift == ModifierState.ONE_SHOT -> {
                if (isLongPress) {
                    // Long press upgrades to locked
                    ModifierState.LOCKED
                } else {
                    // Short press toggles off
                    ModifierState.NONE
                }
            }
            // Currently none
            else -> {
                if (isLongPress) {
                    // Long press = locked
                    ModifierState.LOCKED
                } else {
                    // Short press = one-shot (only if sticky shift enabled or will be handled by system)
                    if (currentSettings.stickyShift) ModifierState.ONE_SHOT else modifiersState.shift
                }
            }
        }

        // Clear alt if activating shift (mutual exclusivity)
        val newAltState = if (newShiftState != ModifierState.NONE) ModifierState.NONE else modifiersState.alt

        updateModifierState(ModifiersState(shift = newShiftState, alt = newAltState))
    }

    /**
     * Toggle alt modifier state
     * @param isLongPress true if this is a long press (locked), false for one-shot
     */
    private fun toggleAltModifier(isLongPress: Boolean) {
        if (!currentSettings.stickyAlt) return

        val newAltState = when {
            // If already locked, turn off
            modifiersState.alt == ModifierState.LOCKED -> ModifierState.NONE
            // If already one-shot
            modifiersState.alt == ModifierState.ONE_SHOT -> {
                if (isLongPress) {
                    // Long press upgrades to locked
                    ModifierState.LOCKED
                } else {
                    // Short press toggles off
                    ModifierState.NONE
                }
            }
            // Currently none
            else -> {
                if (isLongPress) {
                    // Long press = locked
                    ModifierState.LOCKED
                } else {
                    // Short press = one-shot
                    ModifierState.ONE_SHOT
                }
            }
        }

        // Clear shift if activating alt (mutual exclusivity)
        val newShiftState = if (newAltState != ModifierState.NONE) ModifierState.NONE else modifiersState.shift

        updateModifierState(ModifiersState(shift = newShiftState, alt = newAltState))
    }

    /**
     * Clear one-shot modifiers after use
     */
    private fun clearOneShotModifiers() {
        Log.d(TAG, "clearOneShotModifiers called: current modifiers=shift:${modifiersState.shift}/alt:${modifiersState.alt}")
        if (!modifiersState.hasOneShotModifier()) return

        val newShift = if (modifiersState.shift == ModifierState.ONE_SHOT) ModifierState.NONE else modifiersState.shift
        val newAlt = if (modifiersState.alt == ModifierState.ONE_SHOT) ModifierState.NONE else modifiersState.alt

        if (newShift != modifiersState.shift || newAlt != modifiersState.alt) {
            updateModifierState(ModifiersState(shift = newShift, alt = newAlt))
        }
    }

    /**
     * Clear currency shortcut mode
     */
    private fun clearCurrencyShortcutMode() {
        currencyShortcutMode = false
        currencyCountryCode.clear()
        currencyModeStartTime = 0L
        Log.d(TAG, "Currency shortcut mode cleared")
    }

    /**
     * Handle currency shortcut key sequence: SYM+C+[2-letter country code]
     * Example: SYM+C+I+D = Rp (Indonesia)
     */
    private fun handleCurrencyShortcut(event: KeyEvent, inputConnection: InputConnection?): KeyEventResult {
        // Allow backspace to delete last character
        if (event.keyCode == KeyEvent.KEYCODE_DEL) {
            if (currencyCountryCode.isNotEmpty()) {
                currencyCountryCode.deleteCharAt(currencyCountryCode.length - 1)
                Log.d(TAG, "Currency shortcut: backspace, buffer = '${currencyCountryCode}'")
                return KeyEventResult.Handled
            } else {
                // Empty buffer, exit currency mode
                clearCurrencyShortcutMode()
                return KeyEventResult.NotHandled
            }
        }

        // Only process letter keys
        if (!isLetterKey(event.keyCode)) {
            // Non-letter key cancels currency mode
            Log.d(TAG, "Currency shortcut: non-letter key, cancelling mode")
            clearCurrencyShortcutMode()
            return KeyEventResult.NotHandled
        }

        val char = getCharForKeyCode(event.keyCode)?.uppercase() ?: return KeyEventResult.NotHandled
        
        currencyCountryCode.append(char)
        
        Log.d(TAG, "Currency shortcut: accumulated = '${currencyCountryCode}'")

        // After 2 letters, look up the currency
        if (currencyCountryCode.length == 2) {
            val countryCode = currencyCountryCode.toString()
            val currency = getCurrencyForCountryCode(countryCode)
            
            if (currency != null) {
                // Insert the currency symbol with space
                inputConnection?.commitText("$currency ", 1)
                Log.d(TAG, "Currency shortcut: $countryCode → $currency inserted")
                
                // Reset autocorrect state after inserting currency
                autocorrectManager.reset()
            } else {
                // Unknown country code
                Log.w(TAG, "Currency shortcut: Unknown country code '$countryCode'")
            }
            
            clearCurrencyShortcutMode()
            return KeyEventResult.Handled
        }

        // First letter entered, waiting for second
        return KeyEventResult.Handled
    }

    /**
     * Get currency symbol for a country code
     * Uses ISO 3166-1 alpha-2 country codes
     */
    private fun getCurrencyForCountryCode(countryCode: String): String? {
        return when (countryCode.uppercase()) {
            // Dollar (various countries)
            "US", "CA", "MX", "AR", "CL", "CO", "PE", "VE", "EC", "GT",
            "CU", "BO", "DO", "HN", "PY", "SV", "NI", "CR", "PA", "UY",
            "AU", "NZ", "SG" -> "$"
            
            // British Pound
            "GB" -> "£"
            
            // Euro (Eurozone)
            "AT", "BE", "CY", "EE", "FI", "FR", "DE", "GR", "IE",
            "IT", "LV", "LT", "LU", "MT", "NL", "PT", "SK", "SI",
            "ES", "HR" -> "€"
            
            // Yen/Yuan
            "JP" -> "¥"
            "CN" -> "¥"
            
            // Indian Rupee
            "IN" -> "₹"
            
            // Russian Ruble
            "RU" -> "₽"
            
            // South Korean Won
            "KR" -> "₩"
            
            // Israeli Shekel
            "IL" -> "₪"
            
            // Swiss Franc
            "CH" -> "CHF"
            
            // Krona (Nordic countries)
            "SE", "NO", "DK" -> "kr"
            
            // Polish Zloty
            "PL" -> "zł"
            
            // Czech Koruna
            "CZ" -> "Kč"
            
            // Hungarian Forint
            "HU" -> "Ft"
            
            // Romanian Leu
            "RO" -> "lei"
            
            // Bulgarian Lev
            "BG" -> "лв"
            
            // Turkish Lira
            "TR" -> "₺"
            
            // Brazilian Real
            "BR" -> "R$"
            
            // South African Rand
            "ZA" -> "R"
            
            // Hong Kong Dollar
            "HK" -> "HK$"
            
            // Indonesian Rupiah
            "ID" -> "Rp"
            
            else -> null
        }
    }
}
