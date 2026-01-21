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
import ai.jagoan.keyboard.titan2.domain.model.KeyEventResult
import ai.jagoan.keyboard.titan2.domain.model.ModifierState
import ai.jagoan.keyboard.titan2.domain.model.ModifiersState
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
    
    // Suggestion bar for autocorrect
    private var suggestionBarView: ComposeView? = null
    private var isSuggestionBarShowing = false
    private var currentWordState by mutableStateOf("")
    private var suggestionsState by mutableStateOf<List<ai.jagoan.keyboard.titan2.domain.model.AutocorrectSuggestion>>(emptyList())

    // Track whether we're in any input field to block capacitive touch
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
    
    // Input view and suggestion bar height tracking
    private var inputViewForSpacing: View? = null
    private var measuredSuggestionBarHeight: Int = 0

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

        // Initialize window manager for symbol picker
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
        // Create a transparent spacer view that will be resized based on actual suggestion bar height
        val view = View(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0  // Start with 0, will update when suggestion bar is measured
            )
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
        }
        inputViewForSpacing = view
        return view
    }
    
    private fun handleSuggestionClick(word: String) {
        val inputConnection = currentInputConnection ?: return
        val currentWord = autocorrectManager.getCurrentWord()
        
        if (currentWord.isNotEmpty()) {
            // Delete the current word
            inputConnection.deleteSurroundingText(currentWord.length, 0)
        }
        
        // Insert the selected word with a space
        inputConnection.commitText("$word ", 1)
        
        // Commit the word to context and clear suggestions
        autocorrectManager.commitWord(word)
        updateSuggestions("", emptyList())
        
        // Hide suggestion bar after selection
        hideSuggestionBar()
    }
    
    /**
     * Show suggestion bar as WindowManager overlay
     */
    private fun showSuggestionBar() = PerformanceMonitor.measure("suggestion_bar_show") {
        try {
            Log.d(TAG, "showSuggestionBar called")
            
            // Don't show if symbol picker is visible
            if (isSymbolPickerShowing) {
                Log.d(TAG, "Symbol picker is showing, not showing suggestion bar")
                return@measure
            }
            
            // Always remove existing overlay first to ensure clean state
            if (isSuggestionBarShowing) {
                try {
                    suggestionBarView?.let { view ->
                        windowManager?.removeView(view)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error removing old suggestion bar overlay", e)
                }
                suggestionBarView = null
                isSuggestionBarShowing = false
            }

            // Check window token before creating
            val token = window?.window?.decorView?.windowToken
            if (token == null) {
                Log.e(TAG, "No window token available for suggestion bar")
                return@measure
            }

            val composeView = ComposeView(this).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)

                setContent {
                    ai.jagoan.keyboard.titan2.ui.ime.SuggestionBar(
                        currentWord = currentWordState,
                        suggestions = suggestionsState,
                        onSuggestionClick = { selectedWord ->
                            Log.d(TAG, "Suggestion selected: $selectedWord")
                            handleSuggestionClick(selectedWord)
                        }
                    )
                }
            }
            
            // Measure the ComposeView after it's laid out to get actual height
            composeView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    composeView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    
                    val actualHeight = composeView.height
                    if (actualHeight > 0) {
                        measuredSuggestionBarHeight = actualHeight
                        Log.d(TAG, "Measured suggestion bar height: ${actualHeight}px")
                        
                        // Update input view height to match
                        inputViewForSpacing?.layoutParams?.height = actualHeight
                        inputViewForSpacing?.requestLayout()
                        
                        // Request showing input view to reserve space
                        requestShowSelf(0)
                        
                        // Trigger insets computation update
                        window?.window?.decorView?.post {
                            onComputeInsets(Insets())
                        }
                    }
                }
            })

            // Window parameters for overlay - position it within the reserved input view space
            // Don't add the view yet - wait for measurement first
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                y = 0  // Position at bottom with no offset - will be within reserved space
                this.token = window?.window?.decorView?.windowToken
            }

            windowManager?.addView(composeView, params)
            suggestionBarView = composeView
            isSuggestionBarShowing = true
            
            Log.d(TAG, "Suggestion bar shown")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing suggestion bar", e)
        }
    }
    
    /**
     * Hide suggestion bar overlay
     */
    private fun hideSuggestionBar() {
        try {
            if (isSuggestionBarShowing) {
                Log.d(TAG, "hideSuggestionBar called")
                suggestionBarView?.let { view ->
                    try {
                        windowManager?.removeView(view)
                    } catch (e: Exception) {
                        Log.w(TAG, "Error removing suggestion bar", e)
                    }
                }
                suggestionBarView = null
                isSuggestionBarShowing = false
                
                // Hide input view to remove reserved space
                requestHideSelf(0)
                
                Log.d(TAG, "Suggestion bar hidden")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding suggestion bar", e)
        }
    }
    
    /**
     * Remove suggestion bar overlay (cleanup)
     */
    private fun removeSuggestionBar() {
        try {
            if (!isSuggestionBarShowing && suggestionBarView == null) {
                return
            }
            
            Log.d(TAG, "Removing suggestion bar overlay")
            val view = suggestionBarView
            if (view != null) {
                try {
                    if (view is ComposeView) {
                        view.disposeComposition()
                    }
                    val parent = view.parent
                    if (parent != null) {
                        windowManager?.removeView(view)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error during suggestion bar removal", e)
                }
            }
            suggestionBarView = null
            isSuggestionBarShowing = false
            Log.d(TAG, "Suggestion bar removed")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing suggestion bar overlay", e)
        }
    }
    
    private fun updateSuggestions(word: String, suggestions: List<ai.jagoan.keyboard.titan2.domain.model.AutocorrectSuggestion>) {
        currentWordState = word
        suggestionsState = suggestions
        
        // Show or hide suggestion bar overlay based on whether we have content
        val shouldShow = word.isNotEmpty() || suggestions.isNotEmpty()
        Log.i(TAG, "updateSuggestions - word: '$word', suggestions: ${suggestions.size}, shouldShow: $shouldShow")
        
        if (shouldShow) {
            showSuggestionBar()
        } else {
            hideSuggestionBar()
        }
    }
    
    private fun updateSuggestionsFromAutocorrect() {
        val currentWord = autocorrectManager.getCurrentWord()
        
        if (currentWord.isEmpty()) {
            updateSuggestions("", emptyList())
            return
        }
        
        // Get suggestions from autocorrect manager
        val suggestions = autocorrectManager.getSuggestions(currentWord, maxSuggestions = 3)
        
        // Update UI
        updateSuggestions(currentWord, suggestions)
    }

    override fun onEvaluateInputViewShown(): Boolean {
        // Show input view when suggestion bar is visible to reserve space
        return isSuggestionBarShowing && measuredSuggestionBarHeight > 0
    }
    
    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        
        val inputView = window?.window?.decorView
        if (isSuggestionBarShowing && measuredSuggestionBarHeight > 0 && inputView != null) {
            // Calculate the top of the IME content area (from bottom of screen)
            val inputViewTop = inputView.height - measuredSuggestionBarHeight
            
            // Reserve space at bottom using the actual measured height
            outInsets.contentTopInsets = inputViewTop
            outInsets.visibleTopInsets = inputViewTop
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_VISIBLE
            
            // Set touchable region to the suggestion bar area only
            outInsets.touchableRegion.setEmpty()
            
            Log.d(TAG, "onComputeInsets: inputViewTop=${inputViewTop}px, height=${measuredSuggestionBarHeight}px")
        } else {
            // No space reserved
            outInsets.contentTopInsets = inputView?.height ?: 0
            outInsets.visibleTopInsets = inputView?.height ?: 0
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_VISIBLE
        }
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        // Never use fullscreen mode
        return false
    }

    override fun onShowInputRequested(flags: Int, configChange: Boolean): Boolean {
        // Always return true to ensure we're active for hardware keyboard
        // This ensures key events come to us even when there's no soft keyboard shown
        return true
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
        }

        // Block capacitive touch for ANY input field
        isInputActive = attribute != null
        LazyLog.d(TAG) { "isInputActive: $isInputActive" }

        // Update the key event handler with current editor info
        keyEventHandler.updateEditorInfo(attribute)

        // Reset autocorrect state for new input field
        autocorrectManager.reset()
        
        // Clear suggestions
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return super.onKeyDown(keyCode, event)

        // Track key event time for capacitive touch blocking
        lastKeyEventTime = System.currentTimeMillis()

        val result = keyEventHandler.handleKeyDown(event, currentInputConnection)
        
        // Update suggestions after key press
        Log.i(TAG, "onKeyDown - keyCode: $keyCode, updating suggestions...")
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
     */
    private fun showSymbolPicker() = PerformanceMonitor.measure("symbol_picker_show") {
        try {
            Log.d(TAG, "showSymbolPicker called")
            
            // Hide suggestion bar when showing symbol picker
            hideSuggestionBar()
            
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
        
        // Remove suggestion bar if attached
        removeSuggestionBar()

        // Remove symbol picker if attached
        removeSymbolPicker()

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
