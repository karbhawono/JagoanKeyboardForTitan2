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
     * Get words that start with the specified prefix.
     * Used for optimized autocorrect candidate lookup.
     * @param prefix The prefix to match (2 characters recommended)
     * @return Set of words starting with the prefix
     */
    fun getWordsByPrefix(prefix: String): Set<String>
    
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
     * Add a custom word permanently to dictionary file.
     * @param word The word to add (will be cleaned and validated)
     * @param language "id" for Indonesian, "en" for English
     * @return AddWordResult indicating success or reason for failure
     */
    suspend fun addWordToDictionary(word: String, language: String): AddWordResult
    
    /**
     * Remove a custom word from dictionary file.
     * @param word The word to remove
     * @param language "id" for Indonesian, "en" for English
     * @return true if removed successfully
     */
    suspend fun removeWordFromDictionary(word: String, language: String): Boolean
    
    /**
     * Get all custom words added by the user for a language.
     * @param language "id" for Indonesian, "en" for English
     * @return Set of custom words
     */
    suspend fun getCustomWords(language: String): Set<String>
    
    /**
     * Validate if a word is acceptable for the dictionary.
     * @param word The word to validate
     * @return true if valid (alphabetic, 2+ chars, allows apostrophes and hyphens)
     */
    fun isValidDictionaryWord(word: String): Boolean
    
    /**
     * Rebuild the prefix index after adding/removing words.
     * Should be called after modifying dictionaries.
     */
    fun rebuildPrefixIndex()
    
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
    
    /**
     * Export all custom words to a backup file.
     * Creates a ZIP file containing a JSON manifest and per-language text files.
     * @return ExportResult with file URI on success or error information
     */
    suspend fun exportCustomWords(): ExportResult
    
    /**
     * Import custom words from a backup file.
     * @param fileUri URI of the backup file to import
     * @param mode ImportMode.MERGE (skip duplicates) or ImportMode.REPLACE (clear existing)
     * @return ImportResult with import summary or error information
     */
    suspend fun importCustomWords(fileUri: android.net.Uri, mode: ImportMode): ImportResult
    
    /**
     * Get all custom words for a specific language.
     * @param language Language code (e.g., "en", "id")
     * @return List of custom words for the language
     */
    suspend fun getCustomWordsList(language: String): List<String>
    
    /**
     * Get all custom words grouped by language.
     * @return Map of language code to list of words
     */
    suspend fun getAllCustomWordsByLanguage(): Map<String, List<String>>
    
    /**
     * Clear all custom words for a specific language.
     * @param language Language code (e.g., "en", "id"), or null to clear all languages
     * @return true if cleared successfully
     */
    suspend fun clearCustomWords(language: String? = null): Boolean
}

/**
 * Result of adding a word to the dictionary.
 */
sealed class AddWordResult {
    /** Word was successfully added */
    object Success : AddWordResult()
    
    /** Word already exists in the dictionary */
    object AlreadyExists : AddWordResult()
    
    /** Word format is invalid (too short, contains invalid characters, etc.) */
    object InvalidFormat : AddWordResult()
    
    /** An error occurred while saving the word */
    data class Error(val message: String) : AddWordResult()
}

/**
 * Result of exporting custom words.
 */
sealed class ExportResult {
    /** Export completed successfully */
    data class Success(val fileUri: android.net.Uri, val wordCount: Int) : ExportResult()
    
    /** No custom words to export */
    object NoWordsToExport : ExportResult()
    
    /** An error occurred during export */
    data class Error(val message: String) : ExportResult()
}

/**
 * Result of importing custom words.
 */
sealed class ImportResult {
    /** Import completed successfully */
    data class Success(
        val totalWords: Int,
        val addedWords: Int,
        val skippedWords: Int,
        val errorWords: Int,
        val languageBreakdown: Map<String, Int>
    ) : ImportResult()
    
    /** Invalid backup file format */
    object InvalidFormat : ImportResult()
    
    /** Incompatible backup version */
    data class IncompatibleVersion(val backupVersion: Int, val currentVersion: Int) : ImportResult()
    
    /** An error occurred during import */
    data class Error(val message: String) : ImportResult()
}

/**
 * Import mode for handling existing words.
 */
enum class ImportMode {
    /** Merge with existing words (skip duplicates) */
    MERGE,
    
    /** Replace all existing words with imported words */
    REPLACE
}

/**
 * Backup manifest structure for custom dictionary export.
 */
data class BackupManifest(
    val version: Int = 1,
    val timestamp: Long,
    val appVersion: String,
    val languages: List<LanguageBackup>
)

/**
 * Per-language backup data.
 */
data class LanguageBackup(
    val languageCode: String,
    val wordCount: Int,
    val words: List<String>
)