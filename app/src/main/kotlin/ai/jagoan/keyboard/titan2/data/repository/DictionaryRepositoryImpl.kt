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
 * - Complete dictionary repository implementation with multi-language support
 * - English dictionary with 53,902+ words from multiple sources
 * - Indonesian dictionary support
 * - Custom words management with separate storage files (en_custom.txt, id_custom.txt)
 * - Export custom words to ZIP backup with JSON manifest
 * - Import custom words with Merge/Replace modes
 */

package ai.jagoan.keyboard.titan2.data.repository

import ai.jagoan.keyboard.titan2.domain.repository.AddWordResult
import ai.jagoan.keyboard.titan2.domain.repository.BackupManifest
import ai.jagoan.keyboard.titan2.domain.repository.DictionaryRepository
import ai.jagoan.keyboard.titan2.domain.repository.ExportResult
import ai.jagoan.keyboard.titan2.domain.repository.ImportMode
import ai.jagoan.keyboard.titan2.domain.repository.ImportResult
import ai.jagoan.keyboard.titan2.domain.repository.LanguageBackup
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

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
        private const val CUSTOM_WORDS_SUFFIX = "_custom.txt"
        
        // Backup/Export constants
        private const val BACKUP_VERSION = 1
        private const val BACKUP_DIR = "backups"
        private const val BACKUP_MANIFEST_FILE = "manifest.json"
        
        // Single dictionary files only - specialty dicts removed for performance
    }
    
    // Thread-safe maps for dictionary storage
    private val dictionaries = ConcurrentHashMap<String, MutableSet<String>>()
    private val contractions = ConcurrentHashMap<String, String>()
    private val personalDictionary = ConcurrentHashMap<String, String>()
    
    // Track custom words separately for efficient management
    private val customWords = ConcurrentHashMap<String, MutableSet<String>>()
    
    // Prefix index for fast lookups (built on-demand)
    private var prefixIndex: Map<String, Set<String>>? = null
    private val prefixIndexLock = Any()
    
    override suspend fun loadDictionaries(languages: List<String>): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Starting to load dictionaries for languages: $languages")
            var success = true
            
            // Load each language dictionary (single file only for performance)
            for (lang in languages) {
                // First, try to load from internal storage (which may have custom words)
                val internalFile = java.io.File(context.filesDir, "dictionaries/$lang.txt")
                val words = if (internalFile.exists()) {
                    Log.i(TAG, "Loading dictionary from internal storage: $lang.txt")
                    loadDictionaryFromFile(internalFile)
                } else {
                    // Copy from assets to internal storage on first run
                    Log.i(TAG, "Copying dictionary from assets to internal storage: $lang.txt")
                    copyDictionaryFromAssets(lang)
                    loadDictionaryFromFile(internalFile)
                }
                
                if (words.isNotEmpty()) {
                    dictionaries[lang] = words.toMutableSet()
                    Log.i(TAG, "✓ Loaded $lang dictionary: ${words.size} words")
                } else {
                    Log.e(TAG, "✗ Failed to load $lang dictionary - file empty or not found")
                    success = false
                }
                
                // Load custom words from separate file
                val customFile = java.io.File(context.filesDir, "dictionaries/$lang$CUSTOM_WORDS_SUFFIX")
                if (customFile.exists()) {
                    val customWordsList = loadDictionaryFromFile(customFile)
                    if (customWordsList.isNotEmpty()) {
                        customWords[lang] = customWordsList.toMutableSet()
                        // Also add custom words to main dictionary
                        dictionaries[lang]?.addAll(customWordsList)
                        Log.i(TAG, "✓ Loaded $lang custom words: ${customWordsList.size} words")
                    }
                }
            }
            
            // Load contractions for English
            if (languages.contains("en")) {
                Log.i(TAG, "Loading contractions for English...")
                loadContractions()
            }
            
            // Clear prefix index so it gets rebuilt on next use
            synchronized(prefixIndexLock) {
                prefixIndex = null
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
    
    private fun loadDictionaryFromFile(file: java.io.File): Set<String> {
        return try {
            if (!file.exists()) {
                Log.w(TAG, "Dictionary file does not exist: ${file.path}")
                return emptySet()
            }
            
            file.bufferedReader().use { reader ->
                val words = reader.lineSequence()
                    .map { it.trim().lowercase() }
                    .filter { it.isNotEmpty() }
                    .toSet()
                Log.i(TAG, "Loaded ${words.size} words from ${file.name}")
                words
            }
        } catch (e: Exception) {
            Log.e(TAG, "FAILED to load dictionary file: ${file.path} - ${e.message}", e)
            emptySet()
        }
    }
    
    private suspend fun copyDictionaryFromAssets(language: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val dictDir = java.io.File(context.filesDir, "dictionaries")
            if (!dictDir.exists()) {
                dictDir.mkdirs()
            }
            
            val assetPath = "$DICT_PATH/$language.txt"
            val targetFile = java.io.File(dictDir, "$language.txt")
            
            context.assets.open(assetPath).use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            Log.i(TAG, "✓ Copied $language.txt from assets to internal storage")
            true
        } catch (e: Exception) {
            Log.e(TAG, "✗ Failed to copy dictionary from assets: $language.txt - ${e.message}", e)
            false
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
    
    override fun getWordsByPrefix(prefix: String): Set<String> {
        if (prefix.isEmpty()) return emptySet()
        
        // Build index if not already built
        ensurePrefixIndexBuilt()
        
        val index = prefixIndex ?: return emptySet()
        return index[prefix.lowercase()] ?: emptySet()
    }
    
    /**
     * Build prefix index for O(1) prefix lookups.
     * Called lazily on first use.
     */
    private fun ensurePrefixIndexBuilt() {
        // Double-checked locking for thread safety
        if (prefixIndex != null) return
        
        synchronized(prefixIndexLock) {
            if (prefixIndex != null) return
            
            val startTime = System.currentTimeMillis()
            val index = mutableMapOf<String, MutableSet<String>>()
            val allWords = getAllWords()
            
            Log.i(TAG, "Building prefix index for ${allWords.size} words...")
            
            for (word in allWords) {
                // Index by first 2 characters for optimal performance
                if (word.length >= 2) {
                    val prefix = word.substring(0, 2)
                    index.getOrPut(prefix) { mutableSetOf() }.add(word)
                } else if (word.length == 1) {
                    index.getOrPut(word) { mutableSetOf() }.add(word)
                }
            }
            
            prefixIndex = index
            val elapsedMs = System.currentTimeMillis() - startTime
            Log.i(TAG, "✓ Built prefix index in ${elapsedMs}ms: ${index.size} prefixes")
        }
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
        synchronized(prefixIndexLock) {
            prefixIndex = null
        }
        Log.d(TAG, "Cleared all dictionaries and prefix index")
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
    
    override suspend fun addWordToDictionary(word: String, language: String): AddWordResult = withContext(Dispatchers.IO) {
        val cleanWord = word.trim().lowercase()
        
        // Validate word format
        if (!isValidDictionaryWord(cleanWord)) {
            Log.w(TAG, "Invalid word format: $cleanWord")
            return@withContext AddWordResult.InvalidFormat
        }
        
        // Check if already exists in loaded dictionaries (includes both built-in and custom words)
        val existingWords = dictionaries[language]
        if (existingWords?.contains(cleanWord) == true) {
            Log.i(TAG, "Duplicate word rejected - already exists in dictionary: $cleanWord")
            return@withContext AddWordResult.AlreadyExists
        }
        
        val dictDir = java.io.File(context.filesDir, DICT_PATH)
        
        try {
            // Add to in-memory dictionaries
            dictionaries.getOrPut(language) { mutableSetOf() }.add(cleanWord)
            
            // Track as custom word
            customWords.getOrPut(language) { mutableSetOf() }.add(cleanWord)
            
            // Persist to CUSTOM words file (separate from main dictionary)
            if (!dictDir.exists()) {
                dictDir.mkdirs()
            }
            
            val customFile = java.io.File(dictDir, "$language$CUSTOM_WORDS_SUFFIX")
            customFile.appendText("$cleanWord\n")
            
            // Mark prefix index as dirty
            synchronized(prefixIndexLock) {
                prefixIndex = null
            }
            
            Log.i(TAG, "✓ Added word '$cleanWord' to $language custom dictionary")
            AddWordResult.Success
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save word to custom dictionary", e)
            AddWordResult.Error(e.message ?: "Unknown error")
        }
    }
    
    override suspend fun removeWordFromDictionary(word: String, language: String): Boolean = withContext(Dispatchers.IO) {
        val cleanWord = word.trim().lowercase()
        
        try {
            // Remove from in-memory dictionary
            val wordSet = dictionaries[language]
            wordSet?.remove(cleanWord)
            
            // Remove from custom words tracking
            customWords[language]?.remove(cleanWord)
            
            // Remove from CUSTOM words file
            val dictDir = java.io.File(context.filesDir, DICT_PATH)
            val customFile = java.io.File(dictDir, "$language$CUSTOM_WORDS_SUFFIX")
            
            if (customFile.exists()) {
                val allWords = customFile.readLines()
                    .map { it.trim().lowercase() }
                    .filter { it.isNotEmpty() && it != cleanWord }
                
                if (allWords.isEmpty()) {
                    customFile.delete()
                    Log.d(TAG, "Deleted empty custom file for language '$language'")
                } else {
                    customFile.writeText(allWords.joinToString("\n") + "\n")
                }
            }
            
            // Mark prefix index as dirty
            synchronized(prefixIndexLock) {
                prefixIndex = null
            }
            
            Log.i(TAG, "✓ Removed word '$cleanWord' from $language custom dictionary")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove word from custom dictionary", e)
            false
        }
    }
    
    override suspend fun getCustomWords(language: String): Set<String> {
        return customWords[language]?.toSet() ?: emptySet()
    }
    
    override fun isValidDictionaryWord(word: String): Boolean {
        val cleanWord = word.trim().lowercase()
        return cleanWord.isNotBlank() &&
               cleanWord.length >= 2 &&
               cleanWord.all { it.isLetter() || it == '\'' || it == '-' }
    }
    
    override fun rebuildPrefixIndex() {
        synchronized(prefixIndexLock) {
            prefixIndex = null
        }
        // Will rebuild on next access via ensurePrefixIndexBuilt()
    }
    
    // ========== Export/Import/Management Methods ==========
    
    override suspend fun exportCustomWords(): ExportResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting custom words export")
            
            // Gather all custom words by language
            val allCustomWords = mutableMapOf<String, List<String>>()
            var totalWordCount = 0
            
            for (language in listOf("id", "en")) {
                val words = getCustomWordsList(language)
                if (words.isNotEmpty()) {
                    allCustomWords[language] = words
                    totalWordCount += words.size
                    Log.d(TAG, "Export: Found ${words.size} custom words for language '$language'")
                }
            }
            
            if (totalWordCount == 0) {
                Log.d(TAG, "Export: No custom words to export")
                return@withContext ExportResult.NoWordsToExport
            }
            
            // Create backup directory
            val backupDir = File(context.filesDir, BACKUP_DIR)
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            // Create timestamped backup file
            val timestamp = System.currentTimeMillis()
            val backupFileName = "custom_words_backup_$timestamp.zip"
            val backupFile = File(backupDir, backupFileName)
            
            // Create ZIP with manifest and per-language files
            ZipOutputStream(FileOutputStream(backupFile)).use { zipOut ->
                // Create manifest
                val manifest = BackupManifest(
                    version = BACKUP_VERSION,
                    timestamp = timestamp,
                    appVersion = getAppVersion(),
                    languages = allCustomWords.map { (lang, words) ->
                        LanguageBackup(
                            languageCode = lang,
                            wordCount = words.size,
                            words = words
                        )
                    }
                )
                
                // Write manifest.json
                val manifestJson = Gson().toJson(manifest)
                val manifestEntry = ZipEntry(BACKUP_MANIFEST_FILE)
                zipOut.putNextEntry(manifestEntry)
                zipOut.write(manifestJson.toByteArray())
                zipOut.closeEntry()
                Log.d(TAG, "Export: Written manifest.json")
                
                // Write per-language text files
                for ((language, words) in allCustomWords) {
                    val fileName = "${language}_custom.txt"
                    val entry = ZipEntry(fileName)
                    zipOut.putNextEntry(entry)
                    
                    val content = words.joinToString("\n")
                    zipOut.write(content.toByteArray())
                    zipOut.closeEntry()
                    Log.d(TAG, "Export: Written $fileName with ${words.size} words")
                }
            }
            
            Log.d(TAG, "Export: Successfully created backup at ${backupFile.absolutePath}")
            
            // Return URI for sharing using FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )
            Log.d(TAG, "Export: Created FileProvider URI: $uri")
            ExportResult.Success(uri, totalWordCount)
            
        } catch (e: Exception) {
            Log.e(TAG, "Export: Error exporting custom words", e)
            ExportResult.Error(e.message ?: "Unknown error during export")
        }
    }
    
    override suspend fun importCustomWords(fileUri: Uri, mode: ImportMode): ImportResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Import: Starting import from $fileUri with mode $mode")
            
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return@withContext ImportResult.Error("Cannot open file")
            
            var manifest: BackupManifest? = null
            val importedWords = mutableMapOf<String, MutableList<String>>()
            
            // Read ZIP file
            ZipInputStream(inputStream).use { zipIn ->
                var entry = zipIn.nextEntry
                
                while (entry != null) {
                    val entryName = entry.name
                    Log.d(TAG, "Import: Reading entry $entryName")
                    
                    when {
                        entryName == BACKUP_MANIFEST_FILE -> {
                            // Read manifest
                            val content = zipIn.readBytes().toString(Charsets.UTF_8)
                            manifest = Gson().fromJson(content, BackupManifest::class.java)
                            Log.d(TAG, "Import: Read manifest version ${manifest?.version}")
                        }
                        entryName.endsWith("_custom.txt") -> {
                            // Read language file
                            val language = entryName.substringBefore("_custom.txt")
                            val content = zipIn.readBytes().toString(Charsets.UTF_8)
                            val words = content.lines()
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                            
                            importedWords[language] = words.toMutableList()
                            Log.d(TAG, "Import: Read ${words.size} words for language '$language'")
                        }
                    }
                    
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }
            
            // Validate manifest
            if (manifest == null) {
                Log.e(TAG, "Import: No manifest found in backup file")
                return@withContext ImportResult.InvalidFormat
            }
            
            if (manifest!!.version > BACKUP_VERSION) {
                Log.e(TAG, "Import: Incompatible backup version ${manifest!!.version}")
                return@withContext ImportResult.IncompatibleVersion(manifest!!.version, BACKUP_VERSION)
            }
            
            // Handle replace mode - clear existing custom words
            if (mode == ImportMode.REPLACE) {
                Log.d(TAG, "Import: REPLACE mode - clearing existing custom words")
                clearCustomWords(null)
            }
            
            // Import words
            var totalWords = 0
            var addedWords = 0
            var skippedWords = 0
            var errorWords = 0
            val languageBreakdown = mutableMapOf<String, Int>()
            
            for ((language, words) in importedWords) {
                var langAdded = 0
                
                for (word in words) {
                    totalWords++
                    
                    val result = addWordToDictionary(word, language)
                    when (result) {
                        is AddWordResult.Success -> {
                            addedWords++
                            langAdded++
                        }
                        is AddWordResult.AlreadyExists -> {
                            skippedWords++
                            Log.d(TAG, "Import: Skipped duplicate word '$word' for language '$language'")
                        }
                        is AddWordResult.InvalidFormat -> {
                            errorWords++
                            Log.w(TAG, "Import: Invalid word format '$word' for language '$language'")
                        }
                        is AddWordResult.Error -> {
                            errorWords++
                            Log.e(TAG, "Import: Error adding word '$word': ${result.message}")
                        }
                    }
                }
                
                languageBreakdown[language] = langAdded
                Log.d(TAG, "Import: Added $langAdded words for language '$language'")
            }
            
            Log.d(TAG, "Import: Complete - Total: $totalWords, Added: $addedWords, Skipped: $skippedWords, Errors: $errorWords")
            
            ImportResult.Success(
                totalWords = totalWords,
                addedWords = addedWords,
                skippedWords = skippedWords,
                errorWords = errorWords,
                languageBreakdown = languageBreakdown
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Import: Error importing custom words", e)
            ImportResult.Error(e.message ?: "Unknown error during import")
        }
    }
    
    override suspend fun getCustomWordsList(language: String): List<String> = withContext(Dispatchers.IO) {
        try {
            // Get custom words from in-memory map (not from file which contains all words)
            val words = customWords[language]?.toList() ?: emptyList()
            val sortedWords = words.sorted()
            
            Log.d(TAG, "GetCustomWordsList: Found ${sortedWords.size} custom words for language '$language'")
            sortedWords
            
        } catch (e: Exception) {
            Log.e(TAG, "GetCustomWordsList: Error reading custom words for language '$language'", e)
            emptyList()
        }
    }
    
    override suspend fun getAllCustomWordsByLanguage(): Map<String, List<String>> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<String, List<String>>()
        
        for (language in listOf("id", "en")) {
            val words = getCustomWordsList(language)
            if (words.isNotEmpty()) {
                result[language] = words
            }
        }
        
        Log.d(TAG, "GetAllCustomWordsByLanguage: Found custom words for ${result.keys.size} languages")
        result
    }
    
    override suspend fun clearCustomWords(language: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val dictDir = File(context.filesDir, DICT_PATH)
            
            if (language != null) {
                // Clear specific language
                val customFile = File(dictDir, "$language$CUSTOM_WORDS_SUFFIX")
                
                if (customFile.exists()) {
                    customFile.delete()
                    Log.d(TAG, "ClearCustomWords: Deleted custom file for language '$language'")
                }
                
                // Remove custom words from main dictionary
                dictionaries[language]?.let { dict ->
                    val wordsToRemove = customWords[language] ?: emptySet()
                    dict.removeAll(wordsToRemove)
                }
                
                // Clear from memory
                customWords[language]?.clear()
                
                Log.d(TAG, "ClearCustomWords: Cleared custom words for language '$language'")
                
            } else {
                // Clear all languages
                for (lang in listOf("id", "en")) {
                    val customFile = File(dictDir, "$lang$CUSTOM_WORDS_SUFFIX")
                    if (customFile.exists()) {
                        customFile.delete()
                        Log.d(TAG, "ClearCustomWords: Deleted custom file for language '$lang'")
                    }
                    
                    // Remove custom words from main dictionary
                    dictionaries[lang]?.let { dict ->
                        val wordsToRemove = customWords[lang] ?: emptySet()
                        dict.removeAll(wordsToRemove)
                    }
                    
                    // Clear from memory
                    customWords[lang]?.clear()
                }
                
                customWords.clear()
                Log.d(TAG, "ClearCustomWords: Cleared all custom words")
            }
            
            // Rebuild prefix index since dictionary changed
            rebuildPrefixIndex()
            
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "ClearCustomWords: Error clearing custom words", e)
            false
        }
    }
    
    /**
     * Get app version for backup metadata.
     */
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
}