package ai.jagoan.keyboard.titan2.domain.repository

/**
 * Repository interface for dictionary operations.
 * Handles loading and querying words from various dictionary sources.
 */
interface DictionaryRepository {
    
    /**
     * Load dictionaries for the specified languages.
     * @param languages List of language codes (e.g., "en", "id")
     * @return true if dictionaries loaded successfully
     */
    suspend fun loadDictionaries(languages: List<String>): Boolean
    
    /**
     * Check if a word exists in any loaded dictionary.
     * @param word The word to check
     * @return true if word exists in dictionary
     */
    fun contains(word: String): Boolean
    
    /**
     * Check if a word exists in a specific language dictionary.
     * @param word The word to check
     * @param language Language code (e.g., "en", "id")
     * @return true if word exists in specified language dictionary
     */
    fun containsInLanguage(word: String, language: String): Boolean
    
    /**
     * Get all words from loaded dictionaries.
     * @return Set of all words
     */
    fun getAllWords(): Set<String>
    
    /**
     * Get words from a specific language dictionary.
     * @param language Language code
     * @return Set of words in specified language
     */
    fun getWordsForLanguage(language: String): Set<String>
    
    /**
     * Get contraction mapping (e.g., "didnt" -> "didn't").
     * @param word The word without apostrophe
     * @return The correct contraction or null if not found
     */
    fun getContraction(word: String): String?
    
    /**
     * Add a word to personal dictionary.
     * @param word The word to add
     * @param language Optional language hint
     */
    suspend fun addToPersonalDictionary(word: String, language: String? = null)
    
    /**
     * Remove a word from personal dictionary.
     * @param word The word to remove
     */
    suspend fun removeFromPersonalDictionary(word: String)
    
    /**
     * Check if a word is in personal dictionary.
     * @param word The word to check
     * @return true if in personal dictionary
     */
    fun isInPersonalDictionary(word: String): Boolean
    
    /**
     * Get all loaded language codes.
     * @return Set of language codes
     */
    fun getLoadedLanguages(): Set<String>
    
    /**
     * Clear all loaded dictionaries from memory.
     */
    fun clearDictionaries()
    
    /**
     * Detect the likely language of a word based on loaded dictionaries.
     * @param word The word to detect language for
     * @return Language code or null if not found
     */
    fun detectLanguage(word: String): String?
}