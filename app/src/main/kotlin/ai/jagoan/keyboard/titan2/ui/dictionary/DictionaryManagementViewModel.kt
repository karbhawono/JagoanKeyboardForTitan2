package ai.jagoan.keyboard.titan2.ui.dictionary

import ai.jagoan.keyboard.titan2.domain.repository.DictionaryRepository
import ai.jagoan.keyboard.titan2.domain.repository.ExportResult
import ai.jagoan.keyboard.titan2.domain.repository.ImportMode
import ai.jagoan.keyboard.titan2.domain.repository.ImportResult
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for dictionary management operations.
 */
@HiltViewModel
class DictionaryManagementViewModel @Inject constructor(
    private val dictionaryRepository: DictionaryRepository
) : ViewModel() {

    companion object {
        private const val TAG = "DictMgmtViewModel"
    }

    private val _uiState = MutableStateFlow<DictionaryManagementUiState>(DictionaryManagementUiState.Loading)
    val uiState: StateFlow<DictionaryManagementUiState> = _uiState.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    init {
        loadCustomWords()
    }

    /**
     * Load all custom words grouped by language.
     */
    fun loadCustomWords() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading custom words...")
                _uiState.value = DictionaryManagementUiState.Loading

                val wordsByLanguage = dictionaryRepository.getAllCustomWordsByLanguage()
                val totalWords = wordsByLanguage.values.sumOf { it.size }

                Log.d(TAG, "Loaded $totalWords custom words across ${wordsByLanguage.size} languages")

                _uiState.value = DictionaryManagementUiState.Success(
                    wordsByLanguage = wordsByLanguage,
                    totalWords = totalWords
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading custom words", e)
                _uiState.value = DictionaryManagementUiState.Error(
                    e.message ?: "Failed to load custom words"
                )
            }
        }
    }

    /**
     * Export all custom words to a backup file.
     */
    fun exportCustomWords() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting export...")
                _exportState.value = ExportState.Exporting

                when (val result = dictionaryRepository.exportCustomWords()) {
                    is ExportResult.Success -> {
                        Log.d(TAG, "Export successful: ${result.wordCount} words, URI: ${result.fileUri}")
                        _exportState.value = ExportState.Success(result.fileUri, result.wordCount)
                    }
                    is ExportResult.NoWordsToExport -> {
                        Log.d(TAG, "No words to export")
                        _exportState.value = ExportState.Error("No custom words to export")
                    }
                    is ExportResult.Error -> {
                        Log.e(TAG, "Export error: ${result.message}")
                        _exportState.value = ExportState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during export", e)
                _exportState.value = ExportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Import custom words from a backup file.
     */
    fun importCustomWords(fileUri: Uri, mode: ImportMode) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting import from $fileUri with mode $mode")
                _importState.value = ImportState.Importing

                when (val result = dictionaryRepository.importCustomWords(fileUri, mode)) {
                    is ImportResult.Success -> {
                        Log.d(TAG, "Import successful: ${result.addedWords} added, ${result.skippedWords} skipped, ${result.errorWords} errors")
                        _importState.value = ImportState.Success(
                            totalWords = result.totalWords,
                            addedWords = result.addedWords,
                            skippedWords = result.skippedWords,
                            errorWords = result.errorWords,
                            languageBreakdown = result.languageBreakdown
                        )
                        // Reload custom words after import
                        loadCustomWords()
                    }
                    is ImportResult.InvalidFormat -> {
                        Log.e(TAG, "Invalid backup file format")
                        _importState.value = ImportState.Error("Invalid backup file format")
                    }
                    is ImportResult.IncompatibleVersion -> {
                        Log.e(TAG, "Incompatible backup version: ${result.backupVersion} vs ${result.currentVersion}")
                        _importState.value = ImportState.Error(
                            "Incompatible backup version (${result.backupVersion}). Current version: ${result.currentVersion}"
                        )
                    }
                    is ImportResult.Error -> {
                        Log.e(TAG, "Import error: ${result.message}")
                        _importState.value = ImportState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during import", e)
                _importState.value = ImportState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Delete a specific custom word.
     */
    fun deleteCustomWord(word: String, language: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Deleting word '$word' from language '$language'")
                val success = dictionaryRepository.removeWordFromDictionary(word, language)

                if (success) {
                    Log.d(TAG, "Word deleted successfully")
                    // Reload custom words after deletion
                    loadCustomWords()
                } else {
                    Log.w(TAG, "Failed to delete word")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting word", e)
            }
        }
    }

    /**
     * Clear all custom words or words for a specific language.
     */
    fun clearCustomWords(language: String? = null) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Clearing custom words" + if (language != null) " for language '$language'" else " (all languages)")
                val success = dictionaryRepository.clearCustomWords(language)

                if (success) {
                    Log.d(TAG, "Custom words cleared successfully")
                    // Reload custom words after clearing
                    loadCustomWords()
                } else {
                    Log.w(TAG, "Failed to clear custom words")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing custom words", e)
            }
        }
    }

    /**
     * Reset export state to idle.
     */
    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }

    /**
     * Reset import state to idle.
     */
    fun resetImportState() {
        _importState.value = ImportState.Idle
    }
}

/**
 * UI state for dictionary management screen.
 */
sealed class DictionaryManagementUiState {
    object Loading : DictionaryManagementUiState()

    data class Success(
        val wordsByLanguage: Map<String, List<String>>,
        val totalWords: Int
    ) : DictionaryManagementUiState()

    data class Error(val message: String) : DictionaryManagementUiState()
}

/**
 * State for export operation.
 */
sealed class ExportState {
    object Idle : ExportState()
    object Exporting : ExportState()
    data class Success(val fileUri: Uri, val wordCount: Int) : ExportState()
    data class Error(val message: String) : ExportState()
}

/**
 * State for import operation.
 */
sealed class ImportState {
    object Idle : ImportState()
    object Importing : ImportState()
    data class Success(
        val totalWords: Int,
        val addedWords: Int,
        val skippedWords: Int,
        val errorWords: Int,
        val languageBreakdown: Map<String, Int>
    ) : ImportState()
    data class Error(val message: String) : ImportState()
}