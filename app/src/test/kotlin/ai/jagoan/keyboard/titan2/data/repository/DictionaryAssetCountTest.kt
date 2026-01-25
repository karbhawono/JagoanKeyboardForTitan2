/**
 * Copyright (c) 2025 Aryo Karbhawono
 *
 * Unit tests to validate dictionary asset files
 * Verifies that 5k word dictionaries are properly formatted
 */

package ai.jagoan.keyboard.titan2.data.repository

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

@DisplayName("Dictionary Asset Count Tests")
class DictionaryAssetCountTest {

    // Path is relative to the module root (app/)
    private val assetsPath = "src/main/assets/dictionaries"
    
    @Test
    @DisplayName("English dictionary has exactly 5000 words")
    fun englishDictionaryHas5000Words() {
        val file = File(assetsPath, "en.txt")
        assertTrue(file.exists(), "English dictionary file should exist at ${file.absolutePath}")
        
        val lines = file.readLines().filter { it.isNotBlank() }
        assertEquals(5000, lines.size, "English dictionary should have exactly 5000 words")
    }
    
    @Test
    @DisplayName("Indonesian dictionary has exactly 5000 words")
    fun indonesianDictionaryHas5000Words() {
        val file = File(assetsPath, "id.txt")
        assertTrue(file.exists(), "Indonesian dictionary file should exist at ${file.absolutePath}")
        
        val lines = file.readLines().filter { it.isNotBlank() }
        assertEquals(5000, lines.size, "Indonesian dictionary should have exactly 5000 words")
    }
    
    @Test
    @DisplayName("English dictionary has no empty lines")
    fun englishDictionaryHasNoEmptyLines() {
        val file = File(assetsPath, "en.txt")
        val lines = file.readLines()
        val emptyLines = lines.count { it.isBlank() }
        
        assertEquals(0, emptyLines, "English dictionary should have no empty lines")
    }
    
    @Test
    @DisplayName("Indonesian dictionary has no empty lines")
    fun indonesianDictionaryHasNoEmptyLines() {
        val file = File(assetsPath, "id.txt")
        val lines = file.readLines()
        val emptyLines = lines.count { it.isBlank() }
        
        assertEquals(0, emptyLines, "Indonesian dictionary should have no empty lines")
    }
    
    @Test
    @DisplayName("English dictionary has no duplicate words")
    fun englishDictionaryHasNoDuplicates() {
        val file = File(assetsPath, "en.txt")
        val lines = file.readLines().filter { it.isNotBlank() }
        val uniqueLines = lines.toSet()
        
        assertEquals(lines.size, uniqueLines.size, "English dictionary should have no duplicates")
    }
    
    @Test
    @DisplayName("Indonesian dictionary has no duplicate words")
    fun indonesianDictionaryHasNoDuplicates() {
        val file = File(assetsPath, "id.txt")
        val lines = file.readLines().filter { it.isNotBlank() }
        val uniqueLines = lines.toSet()
        
        assertEquals(lines.size, uniqueLines.size, "Indonesian dictionary should have no duplicates")
    }
    
    @Test
    @DisplayName("English dictionary words are lowercase")
    fun englishDictionaryWordsAreLowercase() {
        val file = File(assetsPath, "en.txt")
        val lines = file.readLines().filter { it.isNotBlank() }
        val nonLowercase = lines.filter { it != it.lowercase() }
        
        assertTrue(nonLowercase.isEmpty(), 
            "English dictionary should be all lowercase, found: ${nonLowercase.take(5)}")
    }
    
    @Test
    @DisplayName("Indonesian dictionary words are lowercase")
    fun indonesianDictionaryWordsAreLowercase() {
        val file = File(assetsPath, "id.txt")
        val lines = file.readLines().filter { it.isNotBlank() }
        val nonLowercase = lines.filter { it != it.lowercase() }
        
        assertTrue(nonLowercase.isEmpty(), 
            "Indonesian dictionary should be all lowercase, found: ${nonLowercase.take(5)}")
    }
    
    @Test
    @DisplayName("English dictionary contains common words")
    fun englishDictionaryContainsCommonWords() {
        val file = File(assetsPath, "en.txt")
        val words = file.readLines().filter { it.isNotBlank() }.toSet()
        
        val commonWords = listOf("the", "a", "is", "and", "of", "to", "in")
        val missing = commonWords.filter { it !in words }
        
        assertTrue(missing.isEmpty(), 
            "English dictionary should contain common words, missing: $missing")
    }
    
    @Test
    @DisplayName("Indonesian dictionary contains common words")
    fun indonesianDictionaryContainsCommonWords() {
        val file = File(assetsPath, "id.txt")
        val words = file.readLines().filter { it.isNotBlank() }.toSet()
        
        val commonWords = listOf("ada", "adalah", "dan", "yang", "ini", "itu", "dengan")
        val missing = commonWords.filter { it !in words }
        
        assertTrue(missing.isEmpty(), 
            "Indonesian dictionary should contain common words, missing: $missing")
    }
    
    @Test
    @DisplayName("Contractions file exists and is not empty")
    fun contractionsFileExistsAndNotEmpty() {
        val file = File(assetsPath, "en_contractions.txt")
        assertTrue(file.exists(), "Contractions file should exist at ${file.absolutePath}")
        
        val lines = file.readLines().filter { it.isNotBlank() }
        assertTrue(lines.isNotEmpty(), "Contractions file should not be empty")
    }
    
    @Test
    @DisplayName("Total dictionary size is under 500KB for performance")
    fun totalDictionarySizeIsUnder500KB() {
        val enFile = File(assetsPath, "en.txt")
        val idFile = File(assetsPath, "id.txt")
        
        val totalSize = enFile.length() + idFile.length()
        val maxSize = 500 * 1024 // 500KB
        
        assertTrue(totalSize < maxSize, 
            "Total dictionary size should be under 500KB for performance, got ${totalSize / 1024}KB")
    }
}
