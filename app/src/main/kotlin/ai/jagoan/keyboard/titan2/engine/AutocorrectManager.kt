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
 * - Created autocorrect manager for coordinating dictionary loading and suggestions
 * - Asynchronous dictionary initialization with coroutines
 * - Integration with AutocorrectEngine for suggestion generation
 */

package ai.jagoan.keyboard.titan2.engine

import ai.jagoan.keyboard.titan2.domain.model.AutocorrectSuggestion
import ai.jagoan.keyboard.titan2.domain.repository.DictionaryRepository
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for autocorrect functionality.
 * Coordinates between AutocorrectEngine, DictionaryRepository, and IME.
 */
@Singleton
class AutocorrectManager @Inject constructor(
    private val dictionaryRepository: DictionaryRepository,
    private val autocorrectEngine: AutocorrectEngine
) {
    
    companion object {
        private const val TAG = "AutocorrectManager"
    }
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private var isEnabled = false
    private var isInitialized = false
    
    // Track current word being typed
    private val currentWord = StringBuilder()
    private val contextWords = mutableListOf<String>()
    private val maxContextWords = 10
    
    // Track last autocorrection for undo
    private var lastAutocorrection: AutocorrectInfo? = null
    
    data class AutocorrectInfo(
        val original: String,
        val corrected: String,
        val wasAutoApplied: Boolean
    )
    
    /**
     * Initialize autocorrect with specified languages.
     */
    fun initialize(languages: List<String> = listOf("en", "id")) {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return
        }
        
        scope.launch {
            Log.d(TAG, "Initializing autocorrect with languages: $languages")
            val success = dictionaryRepository.loadDictionaries(languages)
            if (success) {
                isInitialized = true
                Log.d(TAG, "Autocorrect initialized successfully")
            } else {
                Log.e(TAG, "Failed to initialize autocorrect")
            }
        }
    }
    
    /**
     * Enable or disable autocorrect.
     */
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        Log.d(TAG, "Autocorrect ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Check if autocorrect is enabled and ready.
     */
    fun isReady(): Boolean = isEnabled && isInitialized
    
    /**
     * Add a character to the current word being typed.
     */
    fun addCharacter(char: Char) {
        if (!isReady()) return
        
        if (char.isLetter() || char == '\'') {
            currentWord.append(char)
        }
    }
    
    /**
     * Handle space key press - trigger autocorrect if needed.
     * Returns the corrected word if autocorrection was applied, null otherwise.
     * Does NOT clear currentWord - that happens when user commits a choice.
     */
    fun handleSpace(): String? {
        Log.d(TAG, "handleSpace called, ready: ${isReady()}, currentWord: '${currentWord}'")
        if (!isReady() || currentWord.isEmpty()) return null
        
        val word = currentWord.toString()
        Log.d(TAG, "Processing word: '$word'")
        
        // Check if word should be ignored
        if (autocorrectEngine.shouldIgnore(word)) {
            Log.d(TAG, "Ignoring word: $word")
            // Add to context and clear for ignored words
            addToContext(word)
            currentWord.clear()
            return null
        }
        
        // Get suggestions
        val suggestions = autocorrectEngine.getSuggestions(
            word = word,
            maxSuggestions = 1,
            contextWords = contextWords
        )
        
        if (suggestions.isEmpty()) {
            Log.d(TAG, "No suggestions for: $word")
            // Word is correct, add to context and clear
            addToContext(word)
            currentWord.clear()
            return null
        }
        
        val topSuggestion = suggestions.first()
        Log.d(TAG, "Top suggestion for '$word': '${topSuggestion.suggestion}' (source: ${topSuggestion.source}, confidence: ${topSuggestion.confidence})")
        
        // Auto-apply only contractions (high confidence, unambiguous)
        if (autocorrectEngine.shouldAutoApply(suggestions)) {
            val corrected = topSuggestion.suggestion
            Log.d(TAG, "Auto-applying autocorrect: $word -> $corrected (confidence: ${topSuggestion.confidence})")
            
            lastAutocorrection = AutocorrectInfo(
                original = word,
                corrected = corrected,
                wasAutoApplied = true
            )
            
            // Add corrected word to context and clear
            addToContext(corrected)
            currentWord.clear()
            
            return corrected
        }
        
        // Don't auto-apply, keep currentWord for suggestion bar
        // currentWord will be cleared when user selects a suggestion or types next word
        Log.d(TAG, "Not auto-applying, showing suggestions for: $word")
        return null
    }
    
    /**
     * Handle backspace - potentially undo last autocorrection.
     * Returns true if undo was performed, false otherwise.
     */
    fun handleBackspace(): Boolean {
        if (!isReady()) return false
        
        // If currently typing a word, remove last character
        if (currentWord.isNotEmpty()) {
            currentWord.deleteCharAt(currentWord.length - 1)
            return false
        }
        
        // Otherwise, check if we can undo last autocorrection
        val lastCorrection = lastAutocorrection
        if (lastCorrection != null && lastCorrection.wasAutoApplied) {
            Log.d(TAG, "Undo available for: ${lastCorrection.corrected} -> ${lastCorrection.original}")
            return true
        }
        
        return false
    }
    
    /**
     * Get the original word that was auto-corrected for undo.
     */
    fun getUndoWord(): String? {
        return lastAutocorrection?.original
    }
    
    /**
     * Clear undo information.
     */
    fun clearUndo() {
        lastAutocorrection = null
    }
    
    /**
     * Clear the current word being typed.
     */
    fun clearCurrentWord() {
        currentWord.clear()
    }
    
    /**
     * Commit a word (user selected from suggestions or typed correctly).
     * @param word The word to commit to context
     */
    fun commitWord(word: String) {
        addToContext(word)
        currentWord.clear()
        lastAutocorrection = null
    }
    
    /**
     * Reset all autocorrect state.
     */
    fun reset() {
        currentWord.clear()
        contextWords.clear()
        lastAutocorrection = null
    }
    
    /**
     * Handle word boundary (space, punctuation, etc).
     */
    fun handleWordBoundary() {
        if (currentWord.isNotEmpty()) {
            addToContext(currentWord.toString())
            currentWord.clear()
        }
    }
    
    /**
     * Add word to personal dictionary.
     */
    fun addToPersonalDictionary(word: String) {
        scope.launch {
            dictionaryRepository.addToPersonalDictionary(word)
            Log.d(TAG, "Added to personal dictionary: $word")
        }
    }
    
    /**
     * Get suggestions for a word (for suggestion bar).
     */
    fun getSuggestions(word: String, maxSuggestions: Int = 5): List<AutocorrectSuggestion> {
        if (!isReady()) return emptyList()
        if (autocorrectEngine.shouldIgnore(word)) return emptyList()
        
        return autocorrectEngine.getSuggestions(
            word = word,
            maxSuggestions = maxSuggestions,
            contextWords = contextWords
        )
    }
    
    /**
     * Get current word being typed.
     */
    fun getCurrentWord(): String = currentWord.toString()
    
    /**
     * Add word to context history.
     */
    private fun addToContext(word: String) {
        if (word.isBlank()) return
        
        contextWords.add(word.lowercase())
        
        // Keep only recent context
        if (contextWords.size > maxContextWords) {
            contextWords.removeAt(0)
        }
    }
    
    /**
     * Cleanup resources.
     */
    fun cleanup() {
        reset()
        dictionaryRepository.clearDictionaries()
        isInitialized = false
        Log.d(TAG, "AutocorrectManager cleaned up")
    }
}