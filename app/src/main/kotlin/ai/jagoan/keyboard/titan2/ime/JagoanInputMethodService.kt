/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.ime to ai.jagoan.keyboard.titan2.ime
 * - Renamed class from Titan2InputMethodService to JagoanInputMethodService
 * - Added LazyLog utility integration for improved logging
 * - Added PerformanceMonitor utility integration
 * - Added Debouncer utility integration for battery optimization
 * - Replaced standard Log calls with LazyLog for better performance
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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import ai.jagoan.keyboard.titan2.R
import ai.jagoan.keyboard.titan2.domain.model.KeyboardSettings
import ai.jagoan.keyboard.titan2.domain.model.KeyEventResult
import ai.jagoan.keyboard.titan2.domain.model.ModifierState
import ai.jagoan.keyboard.titan2.domain.model.ModifiersState
import ai.jagoan.keyboard.titan2.domain.model.SuggestionBarMode
import ai.jagoan.keyboard.titan2.domain.model.SymbolCategory
import ai.jagoan.keyboard.titan2.domain.repository.SettingsRepository
import ai.jagoan.keyboard.titan2.ui.ime.SymbolPickerOverlay
import ai.jagoan.keyboard.titan2.util.Debouncer
import ai.jagoan.keyboard.titan2.util.LazyLog
import ai.jagoan.keyboard.titan2.util.PerformanceMonitor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Input Method Service for Jagoan Keyboard for Titan 2
 * Handles physical keyboard input events
 */
@AndroidEntryPoint
class JagoanInputMethodService : InputMethodService(), ModifierStateListener {

    @Inject
    lateinit var keyEventHandler: KeyEventHandler

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var autocorrectManager: ai.jagoan.keyboard.titan2.engine.AutocorrectManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Suggestion bar as IME input view
    private var suggestionBarView: ai.jagoan.keyboard.titan2.ui.ime.SuggestionBarView? = null
    private var currentSuggestionBarMode = SuggestionBarMode.AUTO
    private var currentSettingsAutocorrectEnabled = true

    // Input state tracking
    private var isInputActive = false
    private var lastKeyEventTime = 0L

    // Notification for status bar indicator
    private lateinit var notificationManager: NotificationManager
    
    // Debouncer for notification updates to reduce battery drain
    private var notificationDebouncer: Debouncer? = null

    // Symbol picker window management
    private var windowManager: WindowManager? = null
    private var symbolPickerView: ComposeView? = null
    private var isSymbolPickerShowing = false
    
    // Compose state for symbol picker
    private var symbolPickerVisible by mutableStateOf(false)
    private var symbolPickerCategory by mutableStateOf(SymbolCategory.PUNCTUATION)

    // Lifecycle owner for Compose in service
    private val lifecycleOwner = ServiceLifecycleOwner()

    companion object {
        private const val TAG = "Titan2IME"
        private const val CAPACITIVE_BLOCK_TIME_MS = 1000L // Block capacitive for 1s after keystroke
        private const val NOTIFICATION_CHANNEL_ID = "modifier_keys_status_v2"
        private const val NOTIFICATION_ID = 1001
    }

    /**
     * Custom lifecycle owner for running Compose in a Service
     */
    private class ServiceLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle get() = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }

        fun performRestore() {
            savedStateRegistryController.performRestore(null)
        }
    }

    override fun onCreate() {
        super.onCreate()
        LazyLog.d(TAG) { "IME Service created" }

        // Initialize lifecycle for Compose
        lifecycleOwner.performRestore()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        // Set up notification manager and channel for status bar indicators
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        // Set up Vi mode listener
        keyEventHandler.setViModeListener { viMode ->
            suggestionBarView?.updateViMode(viMode)
        }

        // Initialize window manager for symbol picker and suggestion bar
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // Initialize notification debouncer (100ms delay to batch rapid updates)
        notificationDebouncer = Debouncer(serviceScope, delayMs = 100L)

        // Set up modifier state listener
        keyEventHandler.setModifierStateListener(this)

        // Set up Sym key callback to show/cycle symbol picker
        keyEventHandler.setSymKeyPressedCallback {
            handleSymKeyPressed()
        }

        // Set up Sym picker dismiss callback
        keyEventHandler.setSymPickerDismissCallback {
            hideSymbolPicker()
        }

        // Set up Sym picker show callback (for category shortcuts)
        keyEventHandler.setSymPickerShowCallback { category ->
            showSymbolPicker(category)
        }

        // Initialize autocorrect
        serviceScope.launch {
            autocorrectManager.initialize(listOf("en", "id"))
        }

        // Observe settings changes
        serviceScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                LazyLog.d(TAG) { "Settings updated: $settings" }
                LazyLog.d(TAG) { "  stickyShift=${settings.stickyShift}, stickyAlt=${settings.stickyAlt}" }
                keyEventHandler.updateSettings(settings)
                
                // Update autocorrect settings
                autocorrectManager.setEnabled(settings.autocorrectEnabled)
                if (settings.autocorrectEnabled && settings.autocorrectLanguages.isNotEmpty()) {
                    autocorrectManager.initialize(settings.autocorrectLanguages)
                }
                
                // Update suggestion bar settings
                currentSuggestionBarMode = settings.suggestionBarMode
                currentSettingsAutocorrectEnabled = settings.autocorrectEnabled
                
                // Update suggestion bar based on mode
                when (settings.suggestionBarMode) {
                    SuggestionBarMode.ALWAYS_SHOW -> {
                        // Always show, even if empty
                        if (isInputActive) {
                            updateSuggestionsFromAutocorrect()
                        } else {
                            updateSuggestions("", emptyList())
                        }
                    }
                    SuggestionBarMode.AUTO -> {
                        // Show when typing
                        if (settings.autocorrectEnabled && isInputActive) {
                            updateSuggestionsFromAutocorrect()
                        } else {
                            updateSuggestions("", emptyList())
                        }
                    }
                    SuggestionBarMode.OFF -> {
                        // Never show
                        updateSuggestions("", emptyList())
                    }
                }
            }
        }

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Modifier Keys Status",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Shows Shift/Alt key status in status bar"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                // Silent notification - no sound
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreateInputView(): View {
        // Create suggestion bar as IME input view
        suggestionBarView = ai.jagoan.keyboard.titan2.ui.ime.SuggestionBarView(this).apply {
            setOnSuggestionClickListener { word ->
                handleSuggestionClick(word)
            }
            // Initialize with empty state
            updateSuggestions("", emptyList(), currentSuggestionBarMode)
        }
        return suggestionBarView!!
    }
    

    override fun onCreateExtractTextView(): View? {
        // Don't show extract text view - prevents system UI overlays
        return null
    }
    
    override fun onCreateCandidatesView(): View? {
        // Don't use candidates view - using WindowManager overlay instead
        return null
    }
    
    override fun isExtractViewShown(): Boolean {
        // Never show extract view - prevents minimize/keyboard switcher buttons
        return false
    }
    
    override fun isShowInputRequested(): Boolean {
        // Don't show standard input - prevents system UI controls
        return false
    }
    
    private fun handleSuggestionClick(word: String) {
        val inputConnection = currentInputConnection ?: return
        val currentWord = autocorrectManager.getCurrentWord()
        
        LazyLog.d(TAG) { "Suggestion clicked: $word, current word: $currentWord" }
        
        if (currentWord.isNotEmpty()) {
            // Delete the current word
            inputConnection.deleteSurroundingText(currentWord.length, 0)
        }
        
        // Insert the selected word with a space
        inputConnection.commitText("$word ", 1)
        
        // Commit the word to context and clear suggestions
        autocorrectManager.commitWord(word)
        updateSuggestions("", emptyList())
    }

    private fun updateSuggestions(word: String, suggestions: List<ai.jagoan.keyboard.titan2.domain.model.AutocorrectSuggestion>) {
        LazyLog.d(TAG) { "updateSuggestions - word: '$word', suggestions: ${suggestions.size}" }
        
        // Update the suggestion bar view with current mode
        suggestionBarView?.updateSuggestions(word, suggestions, currentSuggestionBarMode)
        
        // Update whether to show input view based on mode
        val shouldShow = when (currentSuggestionBarMode) {
            SuggestionBarMode.ALWAYS_SHOW -> isInputActive && (word.isNotEmpty() || suggestions.isNotEmpty())
            SuggestionBarMode.AUTO -> isInputActive && (word.length >= 2 || suggestions.isNotEmpty())
            SuggestionBarMode.OFF -> false
        }
        
        // Tell system to show/hide input view
        updateInputViewShown()
        LazyLog.d(TAG) { "updateInputViewShown called, shouldShow=$shouldShow, mode=$currentSuggestionBarMode" }
    }
    
    private fun updateSuggestionsFromAutocorrect() {
        Log.d(TAG, "updateSuggestionsFromAutocorrect called")
        val currentWord = autocorrectManager.getCurrentWord()
        Log.d(TAG, "Current word from autocorrect: '$currentWord'")
        
        if (currentWord.isEmpty()) {
            Log.d(TAG, "Current word is empty, clearing suggestions")
            updateSuggestions("", emptyList())
            return
        }
        
        // Get suggestions from autocorrect manager
        val suggestions = autocorrectManager.getSuggestions(currentWord, maxSuggestions = 3)
        Log.d(TAG, "Got ${suggestions.size} suggestions for '$currentWord': ${suggestions.map { it.suggestion }}")
        
        // Update UI
        updateSuggestions(currentWord, suggestions)
    }

    override fun onEvaluateInputViewShown(): Boolean {
        // Show IME input view based on suggestion bar mode
        val shouldShow = when (currentSuggestionBarMode) {
            SuggestionBarMode.ALWAYS_SHOW -> isInputActive
            SuggestionBarMode.AUTO -> isInputActive
            SuggestionBarMode.OFF -> false
        }
        LazyLog.d(TAG) { "onEvaluateInputViewShown: $shouldShow (mode=$currentSuggestionBarMode, active=$isInputActive)" }
        return shouldShow
    }
    
    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        // Let system handle insets for IME input view
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        // Never use fullscreen mode - prevents system IME UI overlays
        return false
    }
    
    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean {
        // Show input view for hardware keyboard to reserve space
        return true
    }
    
    override fun isInputViewShown(): Boolean {
        // Never report input view as shown - prevents system IME controls
        return false
    }


    override fun onModifierStateChanged(modifiersState: ModifiersState) {
        LazyLog.d(TAG) { "Modifier state changed: shift=${modifiersState.shift}, alt=${modifiersState.alt}, symPicker=${modifiersState.symPickerVisible}" }

        // Update status bar notification (debounced to reduce battery drain)
        LazyLog.d(TAG) { "Scheduling debounced updateStatusBarNotification" }
        try {
            notificationDebouncer?.debounce {
                updateStatusBarNotification(modifiersState)
            }
        } catch (e: Exception) {
            LazyLog.e(TAG, e) { "Error scheduling updateStatusBarNotification" }
        }

        // Handle symbol picker visibility
        if (modifiersState.symPickerVisible != symbolPickerVisible) {
            symbolPickerVisible = modifiersState.symPickerVisible
            if (symbolPickerVisible) {
                showSymbolPicker()
            } else {
                hideSymbolPicker()
            }
        }

        // Update symbol picker category
        if (modifiersState.symCategory != symbolPickerCategory) {
            symbolPickerCategory = modifiersState.symCategory
            keyEventHandler.setSymbolCategory(symbolPickerCategory)
        }
    }

    private fun updateStatusBarNotification(modifiersState: ModifiersState) {
        val shouldShow = modifiersState.isShiftActive() || modifiersState.isAltActive()
        LazyLog.d(TAG) { "updateStatusBarNotification: shouldShow=$shouldShow" }

        if (shouldShow) {
            // Choose icon based on which modifiers are active
            val iconRes = when {
                modifiersState.isShiftActive() && modifiersState.isAltActive() -> R.drawable.ic_shift_alt
                modifiersState.isShiftActive() -> R.drawable.ic_shift
                modifiersState.isAltActive() -> R.drawable.ic_alt
                else -> R.drawable.ic_shift // Fallback
            }
            LazyLog.d(TAG) { "Using icon resource: $iconRes" }

            // Build notification text based on active modifiers
            val notificationText = buildString {
                if (modifiersState.isShiftActive()) {
                    append("SHIFT")
                    if (modifiersState.shift == ModifierState.LOCKED) {
                        append(" 🔒")
                    }
                }
                if (modifiersState.isAltActive()) {
                    if (isNotEmpty()) append(" + ")
                    append("ALT")
                    if (modifiersState.alt == ModifierState.LOCKED) {
                        append(" 🔒")
                    }
                }
            }
            LazyLog.d(TAG) { "Notification text: $notificationText" }

            try {
                val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(iconRes)
                    .setContentTitle("Modifier Keys Active")
                    .setContentText(notificationText)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOngoing(true)
                    .setShowWhen(false)
                    .build()

                notificationManager.notify(NOTIFICATION_ID, notification)
                LazyLog.d(TAG) { "Notification posted successfully" }
            } catch (e: Exception) {
                LazyLog.e(TAG, e) { "Error posting notification" }
            }
        } else {
            // Cancel notification when no modifiers are active
            LazyLog.d(TAG) { "Canceling notification" }
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        LazyLog.d(TAG) { 
            "Input started - inputType: ${attribute?.inputType}, " +
                "packageName: ${attribute?.packageName}, " +
                "fieldId: ${attribute?.fieldId}, " +
                "restarting: $restarting"
        }

        // Log input type details for debugging
        attribute?.let { info ->
            val typeClass = info.inputType and android.text.InputType.TYPE_MASK_CLASS
            val typeVariation = info.inputType and android.text.InputType.TYPE_MASK_VARIATION
            val hasNoSuggestions = (info.inputType and android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) != 0
            val hasAutoCorrectionDisabled = (info.inputType and android.text.InputType.TYPE_TEXT_FLAG_AUTO_CORRECT) == 0
            Log.i(TAG, "Input type - class: $typeClass, variation: $typeVariation, " +
                    "noSuggestions: $hasNoSuggestions, autoCorrectionDisabled: $hasAutoCorrectionDisabled")
            
            // For physical keyboards, always respect the suggestion bar mode setting
            // Physical keyboard typing behavior is controlled by suggestionBarMode
            LazyLog.d(TAG) { "Suggestion bar mode: $currentSuggestionBarMode, autocorrect enabled: $currentSettingsAutocorrectEnabled" }
        }

        // Block capacitive touch for ANY input field
        isInputActive = attribute != null
        LazyLog.d(TAG) { "isInputActive: $isInputActive" }

        // Update the key event handler with current editor info
        keyEventHandler.updateEditorInfo(attribute)
        
        // Set up suggestion update callback
        keyEventHandler.onSuggestionUpdateNeeded = {
            updateSuggestionsFromAutocorrect()
        }

        // Reset autocorrect state for new input field
        autocorrectManager.reset()
        
        // Clear suggestions - don't show IME until there's actual typing
        updateSuggestions("", emptyList())

        // Check if we should activate auto-cap shift at start of input
        keyEventHandler.onInputStarted(currentInputConnection)
    }

    override fun onFinishInput() {
        super.onFinishInput()
        LazyLog.d(TAG) { "Input finished" }
        isInputActive = false
        
        // Clear suggestions and hide suggestion bar
        updateSuggestions("", emptyList())
    }
    
    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        
        // Track text changes for suggestion updates
        if (currentSuggestionBarMode == SuggestionBarMode.OFF || !isInputActive) {
            return
        }
        
        // Get current text before cursor to extract the current word
        val ic = currentInputConnection ?: return
        val textBeforeCursor = ic.getTextBeforeCursor(100, 0)?.toString() ?: ""
        
        // Extract the current word (everything after last space/punctuation)
        val currentWord = textBeforeCursor.takeLastWhile { !it.isWhitespace() && it.isLetterOrDigit() }
        
        Log.d(TAG, "onUpdateSelection: currentWord='$currentWord'")
        
        // Update autocorrect manager with current word
        if (currentWord != autocorrectManager.getCurrentWord()) {
            autocorrectManager.reset()
            currentWord.forEach { char ->
                if (char.isLetter() || char == '\'') {
                    autocorrectManager.addCharacter(char)
                }
            }
        }
        
        // Update suggestions
        updateSuggestionsFromAutocorrect()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return super.onKeyDown(keyCode, event)
        
        Log.d(TAG, "onKeyDown: keyCode=$keyCode, event=$event")

        // Track key event time for capacitive touch blocking
        lastKeyEventTime = System.currentTimeMillis()

        Log.d(TAG, "Calling keyEventHandler.handleKeyDown")
        val result = keyEventHandler.handleKeyDown(event, currentInputConnection)
        Log.d(TAG, "handleKeyDown returned: $result")
        
        // Autocorrect runs in background (no UI)
        serviceScope.launch {
            updateSuggestionsFromAutocorrect()
        }
        
        return when (result) {
            KeyEventResult.Handled -> {
                LazyLog.d(TAG) { "Key handled: $keyCode" }
                true
            }
            KeyEventResult.NotHandled -> {
                super.onKeyDown(keyCode, event)
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return super.onKeyUp(keyCode, event)

        val result = keyEventHandler.handleKeyUp(event, currentInputConnection)
        return when (result) {
            KeyEventResult.Handled -> true
            KeyEventResult.NotHandled -> super.onKeyUp(keyCode, event)
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        // Block capacitive touch events (trackpad/scroll gestures) when any input is active
        if (isInputActive) {
            val timeSinceLastKey = System.currentTimeMillis() - lastKeyEventTime

            // Block if we recently typed (within 1 second)
            if (lastKeyEventTime > 0 && timeSinceLastKey < CAPACITIVE_BLOCK_TIME_MS) {
                LazyLog.d(TAG) { "Blocking capacitive touch event (${timeSinceLastKey}ms since last key)" }
                return true // Consume the event
            }

            // Also block all capacitive touch while any input field is active
            LazyLog.d(TAG) { "Blocking capacitive touch event (input active)" }
            return true // Consume the event
        }

        // No input active, allow capacitive touch
        return super.onGenericMotionEvent(event)
    }

    /**
     * Handle Sym key press - toggle symbol picker
     */
    private fun handleSymKeyPressed() {
        Log.d(TAG, "handleSymKeyPressed called! symbolPickerVisible=$symbolPickerVisible")
        if (symbolPickerVisible) {
            // Already visible - cycle to next category
            Log.d(TAG, "Cycling to next category from $symbolPickerCategory")
            symbolPickerCategory = getNextSymbolCategory(symbolPickerCategory)
            // Update KeyEventHandler so it knows the current category
            keyEventHandler.setSymbolCategory(symbolPickerCategory)
            // No need to recreate the view - Compose will automatically recompose when symbolPickerCategory changes
            Log.d(TAG, "Category updated to $symbolPickerCategory")
        } else {
            // Show symbol picker
            Log.d(TAG, "Showing symbol picker")
            symbolPickerCategory = SymbolCategory.PUNCTUATION
            keyEventHandler.setSymbolCategory(symbolPickerCategory)
            showSymbolPicker()
        }
    }

    private fun getNextSymbolCategory(current: SymbolCategory): SymbolCategory {
        val categories = SymbolCategory.entries
        val currentIndex = categories.indexOf(current)
        val nextIndex = (currentIndex + 1) % categories.size
        return categories[nextIndex]
    }

    /**
     * Show the symbol picker overlay
     * @param category Optional category to show directly. If null, shows current category.
     */
    private fun showSymbolPicker(category: SymbolCategory? = null) = PerformanceMonitor.measure("symbol_picker_show") {
        try {
            Log.d(TAG, "showSymbolPicker called with category: $category")
            
            // Set category if provided
            if (category != null) {
                symbolPickerCategory = category
                keyEventHandler.setSymbolCategory(category)
                Log.d(TAG, "Category set to: $category")
            }
            
            // Hide candidates view when showing symbol picker
            setCandidatesViewShown(false)
            
            // Get WindowManager for symbol picker overlay
            val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            
            // Always remove existing overlay first to ensure clean state
            if (isSymbolPickerShowing) {
                try {
                    symbolPickerView?.let { view ->
                        windowManager?.removeView(view)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error removing old overlay", e)
                }
                symbolPickerView = null
                isSymbolPickerShowing = false
            }

            // Check window token before creating
            val token = window?.window?.decorView?.windowToken
            if (token == null) {
                Log.e(TAG, "No window token available for symbol picker")
                return@measure
            }

            val composeView = ComposeView(this).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)

                setContent {
                    SymbolPickerOverlay(
                        visible = true,  // Always visible when this ComposeView exists
                        currentCategory = symbolPickerCategory,
                        onSymbolSelected = { symbol ->
                            Log.d(TAG, "Symbol selected: $symbol")
                            keyEventHandler.insertSymbol(symbol, currentInputConnection)
                            hideSymbolPicker()
                        },
                        onDismiss = {
                            Log.d(TAG, "Dismiss called")
                            hideSymbolPicker()
                        }
                    )
                }
            }

            // Window parameters for overlay
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                this.token = window?.window?.decorView?.windowToken
            }

            windowManager?.addView(composeView, params)
            symbolPickerView = composeView
            isSymbolPickerShowing = true
            symbolPickerVisible = true
            keyEventHandler.setSymPickerVisible(true)
            Log.d(TAG, "Symbol picker shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing symbol picker", e)
        }
    }

    /**
     * Hide the symbol picker
     */
    private fun hideSymbolPicker() {
        Log.d(TAG, "hideSymbolPicker called")
        
        symbolPickerVisible = false
        keyEventHandler.setSymPickerVisible(false)

        if (isSymbolPickerShowing) {
            try {
                symbolPickerView?.let { view ->
                    Log.d(TAG, "Removing view from WindowManager")
                    windowManager?.removeView(view)
                }
                isSymbolPickerShowing = false
            } catch (e: Exception) {
                LazyLog.w(TAG, e) { "Error removing view" }
            }
        }
    }

    /**
     * Remove the symbol picker completely
     */
    private fun removeSymbolPicker() {
        LazyLog.d(TAG) { "Removing symbol picker" }

        try {
            if (isSymbolPickerShowing) {
                symbolPickerView?.let { view ->
                    windowManager?.removeView(view)
                }
            }
            symbolPickerView = null
            isSymbolPickerShowing = false
            symbolPickerVisible = false
        } catch (e: Exception) {
            LazyLog.e(TAG, e) { "Error removing symbol picker" }
        }
    }

    override fun onDestroy() {
        LazyLog.d(TAG) { "IME Service destroyed" }

        // Cancel any pending debounced notifications
        notificationDebouncer?.cancel()
        notificationDebouncer = null

        // Clean up KeyEventHandler resources (accent handlers, etc.)
        keyEventHandler.cleanup()

        // Clean up autocorrect manager
        autocorrectManager.cleanup()

        // Remove symbol picker if attached
        removeSymbolPicker()
        
        // Clean up suggestion bar reference
        suggestionBarView = null

        // Update lifecycle
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        // Cancel any active notifications
        notificationManager.cancel(NOTIFICATION_ID)
        serviceScope.cancel()
        super.onDestroy()
    }
}
