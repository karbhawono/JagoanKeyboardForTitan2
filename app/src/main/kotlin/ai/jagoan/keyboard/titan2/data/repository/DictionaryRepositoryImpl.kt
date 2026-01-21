package ai.jagoan.keyboard.titan2.data.repository

import ai.jagoan.keyboard.titan2.domain.repository.DictionaryRepository
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of DictionaryRepository that loads dictionaries from assets.
 */
class DictionaryRepositoryImpl(
    private val context: Context
) : DictionaryRepository {
    
    companion object {
        private const val TAG = "DictionaryRepository"
        private const val DICT_PATH = "dictionaries"
        private const val CONTRACTION_FILE = "en_contractions.txt"
    }
    
    // Thread-safe maps for dictionary storage
    private val dictionaries = ConcurrentHashMap<String, Set<String>>()
    private val contractions = ConcurrentHashMap<String, String>()
    private val personalDictionary = ConcurrentHashMap<String, String>()
    
    override suspend fun loadDictionaries(languages: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Starting to load dictionaries for languages: $languages")
            var success = true
            
            // Load each language dictionary
            for (lang in languages) {
                val filename = "$lang.txt"
                Log.i(TAG, "Loading dictionary file: $filename")
                val words = loadDictionaryFile(filename)
                if (words.isNotEmpty()) {
                    dictionaries[lang] = words
                    Log.i(TAG, "✓ Loaded $lang dictionary: ${words.size} words")
                } else {
                    Log.e(TAG, "✗ Failed to load $lang dictionary - file empty or not found")
                    success = false
                }
            }
            
            // Load contractions for English
            if (languages.contains("en")) {
                Log.i(TAG, "Loading contractions for English...")
                loadContractions()
            }
            
            Log.i(TAG, "Dictionary loading complete. Success: $success")
            success
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL ERROR loading dictionaries", e)
            false
        }
    }
    
    private fun loadDictionaryFile(filename: String): Set<String> {
        return try {
            val path = "$DICT_PATH/$filename"
            Log.i(TAG, "Opening asset file: $path")
            context.assets.open(path).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val words = reader.lineSequence()
                        .map { it.trim().lowercase() }
                        .filter { it.isNotEmpty() }
                        .toSet()
                    Log.i(TAG, "Loaded ${words.size} words from $filename")
                    words
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "FAILED to load dictionary file: $filename - ${e.message}", e)
            emptySet()
        }
    }
    
    private fun loadContractions() {
        try {
            val path = "$DICT_PATH/$CONTRACTION_FILE"
            Log.i(TAG, "Loading contractions from: $path")
            context.assets.open(path).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.lineSequence()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() && it.contains(':') }
                        .forEach { line ->
                            val parts = line.split(':')
                            if (parts.size == 2) {
                                contractions[parts[0].lowercase()] = parts[1]
                            }
                        }
                }
            }
            Log.i(TAG, "✓ Loaded ${contractions.size} contractions")
        } catch (e: Exception) {
            Log.e(TAG, "✗ FAILED to load contractions: ${e.message}", e)
        }
    }
    
    override fun contains(word: String): Boolean {
        val lowercaseWord = word.lowercase()
        
        // Check personal dictionary first
        if (personalDictionary.containsKey(lowercaseWord)) {
            Log.d(TAG, "Word '$word' found in personal dictionary")
            return true
        }
        
        // Check all loaded dictionaries
        val found = dictionaries.values.any { it.contains(lowercaseWord) }
        Log.d(TAG, "Word '$word' in dictionary: $found (checked ${dictionaries.size} dictionaries)")
        return found
    }
    
    override fun containsInLanguage(word: String, language: String): Boolean {
        val lowercaseWord = word.lowercase()
        return dictionaries[language]?.contains(lowercaseWord) ?: false
    }
    
    override fun getAllWords(): Set<String> {
        val allWords = mutableSetOf<String>()
        dictionaries.values.forEach { allWords.addAll(it) }
        allWords.addAll(personalDictionary.keys)
        return allWords
    }
    
    override fun getWordsForLanguage(language: String): Set<String> {
        return dictionaries[language] ?: emptySet()
    }
    
    override fun getContraction(word: String): String? {
        return contractions[word.lowercase()]
    }
    
    override suspend fun addToPersonalDictionary(word: String, language: String?) {
        withContext(Dispatchers.IO) {
            val lowercaseWord = word.lowercase()
            personalDictionary[lowercaseWord] = language ?: "unknown"
            Log.d(TAG, "Added '$word' to personal dictionary")
            // TODO: Persist to shared preferences or database
        }
    }
    
    override suspend fun removeFromPersonalDictionary(word: String) {
        withContext(Dispatchers.IO) {
            val lowercaseWord = word.lowercase()
            personalDictionary.remove(lowercaseWord)
            Log.d(TAG, "Removed '$word' from personal dictionary")
            // TODO: Persist to shared preferences or database
        }
    }
    
    override fun isInPersonalDictionary(word: String): Boolean {
        return personalDictionary.containsKey(word.lowercase())
    }
    
    override fun getLoadedLanguages(): Set<String> {
        return dictionaries.keys
    }
    
    override fun clearDictionaries() {
        dictionaries.clear()
        contractions.clear()
        Log.d(TAG, "Cleared all dictionaries")
    }
    
    override fun detectLanguage(word: String): String? {
        val lowercaseWord = word.lowercase()
        
        // Check each language dictionary
        for ((lang, words) in dictionaries) {
            if (words.contains(lowercaseWord)) {
                return lang
            }
        }
        
        return null
    }
}