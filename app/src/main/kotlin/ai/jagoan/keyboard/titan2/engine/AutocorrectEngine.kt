package ai.jagoan.keyboard.titan2.engine

import ai.jagoan.keyboard.titan2.domain.model.AutocorrectSuggestion
import ai.jagoan.keyboard.titan2.domain.model.SuggestionMetadata
import ai.jagoan.keyboard.titan2.domain.model.SuggestionSource
import ai.jagoan.keyboard.titan2.domain.repository.DictionaryRepository
import android.util.Log
import kotlin.math.min

/**
 * Core autocorrect engine that generates suggestions for misspelled words.
 * Uses multiple algorithms:
 * - Levenshtein edit distance
 * - Keyboard proximity analysis
 * - Smart contractions
 * - Context-aware language detection
 */
class AutocorrectEngine(
    private val dictionaryRepository: DictionaryRepository
) {
    
    companion object {
        private const val TAG = "AutocorrectEngine"
        
        // Thresholds
        private const val MAX_EDIT_DISTANCE = 2
        private const val HIGH_CONFIDENCE_THRESHOLD = 0.8f
        private const val MEDIUM_CONFIDENCE_THRESHOLD = 0.5f
        private const val MAX_SUGGESTIONS = 5
        
        // QWERTY keyboard layout for proximity calculations
        private val KEYBOARD_LAYOUT = mapOf(
            'q' to Pair(0, 0), 'w' to Pair(1, 0), 'e' to Pair(2, 0), 'r' to Pair(3, 0),
            't' to Pair(4, 0), 'y' to Pair(5, 0), 'u' to Pair(6, 0), 'i' to Pair(7, 0),
            'o' to Pair(8, 0), 'p' to Pair(9, 0),
            'a' to Pair(0, 1), 's' to Pair(1, 1), 'd' to Pair(2, 1), 'f' to Pair(3, 1),
            'g' to Pair(4, 1), 'h' to Pair(5, 1), 'j' to Pair(6, 1), 'k' to Pair(7, 1),
            'l' to Pair(8, 1),
            'z' to Pair(0, 2), 'x' to Pair(1, 2), 'c' to Pair(2, 2), 'v' to Pair(3, 2),
            'b' to Pair(4, 2), 'n' to Pair(5, 2), 'm' to Pair(6, 2)
        )
    }
    
    /**
     * Generate autocorrect suggestions for a given word.
     * Returns suggestions sorted by confidence (highest first).
     */
    fun getSuggestions(
        word: String,
        maxSuggestions: Int = MAX_SUGGESTIONS,
        contextWords: List<String> = emptyList()
    ): List<AutocorrectSuggestion> {
        if (word.isBlank()) return emptyList()
        
        val lowercaseWord = word.lowercase()
        val suggestions = mutableListOf<AutocorrectSuggestion>()
        
        // If word is already in dictionary, no correction needed
        if (dictionaryRepository.contains(lowercaseWord)) {
            Log.d(TAG, "Word '$word' found in dictionary, no correction needed")
            return emptyList()
        }
        
        // Check for smart contractions first (highest priority)
        val contraction = dictionaryRepository.getContraction(lowercaseWord)
        if (contraction != null) {
            suggestions.add(
                AutocorrectSuggestion(
                    original = word,
                    suggestion = preserveCase(word, contraction),
                    confidence = 0.95f,
                    source = SuggestionSource.CONTRACTION,
                    metadata = SuggestionMetadata(
                        editDistance = 1,
                        language = "en",
                        isContraction = true
                    )
                )
            )
            Log.d(TAG, "Contraction suggestion: $lowercaseWord -> $contraction")
        }
        
        // Generate candidates from dictionary
        val candidates = generateCandidates(lowercaseWord, contextWords)
        suggestions.addAll(candidates)
        
        // Sort by confidence and return top N
        return suggestions.sorted().take(maxSuggestions)
    }
    
    /**
     * Generate candidate suggestions from dictionary words.
     */
    private fun generateCandidates(
        word: String,
        contextWords: List<String>
    ): List<AutocorrectSuggestion> {
        val candidates = mutableListOf<AutocorrectSuggestion>()
        val allWords = dictionaryRepository.getAllWords()
        
        // Detect likely language from context
        val contextLanguage = detectContextLanguage(contextWords)
        
        for (dictWord in allWords) {
            // Skip if word lengths differ too much
            if (kotlin.math.abs(dictWord.length - word.length) > MAX_EDIT_DISTANCE) {
                continue
            }
            
            val editDistance = levenshteinDistance(word, dictWord)
            
            // Only consider words within edit distance threshold
            if (editDistance <= MAX_EDIT_DISTANCE && editDistance > 0) {
                val proximityScore = calculateKeyboardProximity(word, dictWord)
                val confidence = calculateConfidence(
                    editDistance = editDistance,
                    proximityScore = proximityScore,
                    wordLength = word.length,
                    contextLanguage = contextLanguage,
                    candidateLanguage = dictionaryRepository.detectLanguage(dictWord)
                )
                
                if (confidence >= MEDIUM_CONFIDENCE_THRESHOLD) {
                    candidates.add(
                        AutocorrectSuggestion(
                            original = word,
                            suggestion = dictWord,
                            confidence = confidence,
                            source = if (proximityScore > 0.5f) {
                                SuggestionSource.KEYBOARD_PROXIMITY
                            } else {
                                SuggestionSource.DICTIONARY
                            },
                            metadata = SuggestionMetadata(
                                editDistance = editDistance,
                                language = dictionaryRepository.detectLanguage(dictWord),
                                proximityScore = proximityScore
                            )
                        )
                    )
                }
            }
        }
        
        return candidates
    }
    
    /**
     * Calculate Levenshtein edit distance between two strings.
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        
        // Create a matrix to store distances
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        // Initialize first row and column
        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j
        
        // Fill the matrix
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(
                        dp[i - 1][j] + 1,      // deletion
                        dp[i][j - 1] + 1       // insertion
                    ),
                    dp[i - 1][j - 1] + cost    // substitution
                )
            }
        }
        
        return dp[len1][len2]
    }
    
    /**
     * Calculate keyboard proximity score between two strings.
     * Returns 0.0 to 1.0, where 1.0 means perfect proximity match.
     */
    private fun calculateKeyboardProximity(s1: String, s2: String): Float {
        if (s1.length != s2.length) return 0f
        
        var totalProximity = 0f
        var comparisons = 0
        
        for (i in s1.indices) {
            val c1 = s1[i]
            val c2 = s2[i]
            
            if (c1 != c2) {
                val pos1 = KEYBOARD_LAYOUT[c1]
                val pos2 = KEYBOARD_LAYOUT[c2]
                
                if (pos1 != null && pos2 != null) {
                    // Calculate Euclidean distance on keyboard
                    val dx = pos1.first - pos2.first
                    val dy = pos1.second - pos2.second
                    val distance = kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
                    
                    // Convert distance to proximity (closer = higher score)
                    // Adjacent keys (distance ~1) get high score
                    val proximity = when {
                        distance <= 1.5 -> 0.9f
                        distance <= 2.5 -> 0.6f
                        distance <= 3.5 -> 0.3f
                        else -> 0.1f
                    }
                    totalProximity += proximity
                }
                comparisons++
            }
        }
        
        return if (comparisons > 0) totalProximity / comparisons else 0f
    }
    
    /**
     * Calculate overall confidence score for a suggestion.
     */
    private fun calculateConfidence(
        editDistance: Int,
        proximityScore: Float,
        wordLength: Int,
        contextLanguage: String?,
        candidateLanguage: String?
    ): Float {
        // Base confidence from edit distance (lower distance = higher confidence)
        var confidence = when (editDistance) {
            0 -> 1.0f
            1 -> 0.8f
            2 -> 0.5f
            else -> 0.2f
        }
        
        // Boost confidence for keyboard proximity
        if (proximityScore > 0.5f) {
            confidence += 0.15f * proximityScore
        }
        
        // Boost confidence for longer words (more reliable corrections)
        if (wordLength >= 5) {
            confidence += 0.05f
        }
        
        // Boost confidence if candidate matches context language
        if (contextLanguage != null && contextLanguage == candidateLanguage) {
            confidence += 0.1f
        }
        
        // Clamp to [0, 1]
        return confidence.coerceIn(0f, 1f)
    }
    
    /**
     * Detect the likely language from context words.
     */
    private fun detectContextLanguage(contextWords: List<String>): String? {
        if (contextWords.isEmpty()) return null
        
        val languageCounts = mutableMapOf<String, Int>()
        
        for (word in contextWords.takeLast(5)) { // Look at last 5 words
            val lang = dictionaryRepository.detectLanguage(word.lowercase())
            if (lang != null) {
                languageCounts[lang] = languageCounts.getOrDefault(lang, 0) + 1
            }
        }
        
        return languageCounts.maxByOrNull { it.value }?.key
    }
    
    /**
     * Preserve the original case pattern when applying correction.
     * Examples:
     * - "hello" + "world" = "world"
     * - "Hello" + "world" = "World"
     * - "HELLO" + "world" = "WORLD"
     * - "HeLLo" + "world" = "WoRLd"
     */
    private fun preserveCase(original: String, corrected: String): String {
        if (original.isEmpty() || corrected.isEmpty()) return corrected
        
        // All uppercase
        if (original.all { it.isUpperCase() }) {
            return corrected.uppercase()
        }
        
        // First letter uppercase only
        if (original[0].isUpperCase() && original.substring(1).all { it.isLowerCase() }) {
            return corrected.replaceFirstChar { it.uppercase() }
        }
        
        // Mixed case - preserve pattern
        val result = StringBuilder()
        for (i in corrected.indices) {
            val char = corrected[i]
            result.append(
                if (i < original.length && original[i].isUpperCase()) {
                    char.uppercase()
                } else {
                    char.lowercase()
                }
            )
        }
        
        return result.toString()
    }
    
    /**
     * Check if autocorrect should be applied automatically (high confidence).
     * Only auto-applies contractions (e.g., dont -> don't) for better UX.
     * Other corrections should be shown in suggestion bar.
     */
    fun shouldAutoApply(suggestions: List<AutocorrectSuggestion>): Boolean {
        if (suggestions.isEmpty()) return false
        
        val topSuggestion = suggestions.first()
        
        // Only auto-apply contractions - they have very high confidence and are unambiguous
        // All other corrections should be shown in suggestion bar for user to choose
        return topSuggestion.source == SuggestionSource.CONTRACTION && topSuggestion.confidence >= 0.9f
    }
    
    /**
     * Check if a word should be ignored by autocorrect.
     */
    fun shouldIgnore(word: String): Boolean {
        // Ignore very short words (often acronyms or abbreviations)
        if (word.length <= 1) return true
        
        // Ignore all caps (likely acronym)
        if (word.all { it.isUpperCase() }) return true
        
        // Ignore numbers
        if (word.any { it.isDigit() }) return true
        
        // Ignore URLs, emails, etc.
        if (word.contains('@') || word.contains('.') || word.contains('/')) return true
        
        return false
    }
}