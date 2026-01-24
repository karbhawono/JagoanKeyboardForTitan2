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
 * - Created complete dictionary management UI with Material Design 3
 * - View all custom words grouped by language
 * - Export custom words to ZIP backup with share functionality
 * - Import custom words with Merge/Replace mode selection
 * - Delete individual words with confirmation dialogs
 * - Clear all custom words with warning dialogs
 */

package ai.jagoan.keyboard.titan2.ui.dictionary

import ai.jagoan.keyboard.titan2.domain.repository.ImportMode
import ai.jagoan.keyboard.titan2.ui.settings.SettingsDesignTokens
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: DictionaryManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    
    var showClearDialog by remember { mutableStateOf(false) }
    var showImportModeDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    
    val shareLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.resetExportState()
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            pendingImportUri = it
            showImportModeDialog = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dictionary Management") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SettingsDesignTokens.SurfaceDark
                )
            )
        },
        containerColor = SettingsDesignTokens.BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (uiState) {
                is DictionaryManagementUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DictionaryManagementUiState.Success -> {
                    val state = uiState as DictionaryManagementUiState.Success
                    SummaryCard(totalWords = state.totalWords)
                    ActionButtons(
                        hasWords = state.totalWords > 0,
                        onExport = { viewModel.exportCustomWords() },
                        onImport = { importLauncher.launch("application/zip") },
                        onClear = { showClearDialog = true }
                    )
                    if (state.totalWords > 0) {
                        Text(
                            text = "Custom Words",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SettingsDesignTokens.OnSurface
                            )
                        )
                        CustomWordsList(
                            wordsByLanguage = state.wordsByLanguage,
                            onDeleteWord = { word, language ->
                                viewModel.deleteCustomWord(word, language)
                            }
                        )
                    } else {
                        EmptyState()
                    }
                }
                is DictionaryManagementUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = (uiState as DictionaryManagementUiState.Error).message,
                            color = SettingsDesignTokens.DangerRed
                        )
                    }
                }
            }
        }
    }
    
    LaunchedEffect(exportState) {
        when (val state = exportState) {
            is ExportState.Success -> {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, state.fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                shareLauncher.launch(Intent.createChooser(shareIntent, "Export Custom Words"))
            }
            else -> {}
        }
    }
    
    if (showImportModeDialog && pendingImportUri != null) {
        ImportModeDialog(
            onModeSelected = { mode ->
                viewModel.importCustomWords(pendingImportUri!!, mode)
                showImportModeDialog = false
                pendingImportUri = null
            },
            onDismiss = {
                showImportModeDialog = false
                pendingImportUri = null
            }
        )
    }
    
    when (val state = importState) {
        is ImportState.Success -> {
            ImportResultDialog(
                totalWords = state.totalWords,
                addedWords = state.addedWords,
                skippedWords = state.skippedWords,
                errorWords = state.errorWords,
                languageBreakdown = state.languageBreakdown,
                onDismiss = { viewModel.resetImportState() }
            )
        }
        is ImportState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetImportState() },
                title = { Text("Import Error") },
                text = { Text(state.message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetImportState() }) {
                        Text("OK")
                    }
                }
            )
        }
        else -> {}
    }
    
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Custom Words?") },
            text = { Text("This will permanently delete all your custom words. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCustomWords()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear", color = SettingsDesignTokens.DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SummaryCard(totalWords: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SettingsDesignTokens.SurfaceDark),
        border = BorderStroke(1.dp, SettingsDesignTokens.CardBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Custom Words",
                style = MaterialTheme.typography.bodyLarge.copy(color = SettingsDesignTokens.OnSurface)
            )
            Text(
                text = totalWords.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SettingsDesignTokens.JagoanGreen
                )
            )
        }
    }
}

@Composable
private fun ActionButtons(
    hasWords: Boolean,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onClear: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onExport,
            enabled = hasWords,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(SettingsDesignTokens.CornerRadiusMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = SettingsDesignTokens.JagoanGreen,
                disabledContainerColor = SettingsDesignTokens.SurfaceDark
            )
        ) {
            Text("📤 Export Backup", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
        }
        
        OutlinedButton(
            onClick = onImport,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(SettingsDesignTokens.CornerRadiusMedium),
            border = BorderStroke(1.dp, SettingsDesignTokens.CardBorder)
        ) {
            Text("📥 Import Backup", style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                color = SettingsDesignTokens.OnSurface
            ))
        }
        
        OutlinedButton(
            onClick = onClear,
            enabled = hasWords,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(SettingsDesignTokens.CornerRadiusMedium),
            border = BorderStroke(1.dp, SettingsDesignTokens.DangerRed),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SettingsDesignTokens.DangerRed)
        ) {
            Text("🗑️ Clear All Words", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun CustomWordsList(
    wordsByLanguage: Map<String, List<String>>,
    onDeleteWord: (String, String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        wordsByLanguage.forEach { (language, words) ->
            item {
                Text(
                    text = getLanguageName(language),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = SettingsDesignTokens.JagoanGreen
                    ),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            
            items(words) { word ->
                WordItem(word = word, language = language, onDelete = { onDeleteWord(word, language) })
            }
        }
    }
}

@Composable
private fun WordItem(word: String, language: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SettingsDesignTokens.SurfaceDark),
        border = BorderStroke(0.5.dp, SettingsDesignTokens.CardBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = word, style = MaterialTheme.typography.bodyLarge.copy(color = SettingsDesignTokens.OnSurface))
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete word", tint = SettingsDesignTokens.DangerRed)
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📖", style = MaterialTheme.typography.displayLarge)
            Text(
                "No Custom Words Yet",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SettingsDesignTokens.OnSurface
                )
            )
            Text(
                "Add words to your dictionary using Vi-mode commands\n(:atd, :atdi, :atde) or by long-pressing suggestions",
                style = MaterialTheme.typography.bodyMedium.copy(color = SettingsDesignTokens.OnSurfaceVariant),
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ImportModeDialog(onModeSelected: (ImportMode) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Mode") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("How would you like to handle existing words?")
                
                TextButton(onClick = { onModeSelected(ImportMode.MERGE) }, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Merge", fontWeight = FontWeight.Bold)
                        Text(
                            "Keep existing words and add new ones (skip duplicates)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                TextButton(onClick = { onModeSelected(ImportMode.REPLACE) }, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Replace", fontWeight = FontWeight.Bold)
                        Text(
                            "Delete all existing words and import from backup",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ImportResultDialog(
    totalWords: Int,
    addedWords: Int,
    skippedWords: Int,
    errorWords: Int,
    languageBreakdown: Map<String, Int>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Complete") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Import Summary:", fontWeight = FontWeight.Bold)
                Text("• Total words processed: $totalWords")
                Text("• Added: $addedWords", color = SettingsDesignTokens.JagoanGreen)
                Text("• Skipped (duplicates): $skippedWords")
                if (errorWords > 0) {
                    Text("• Errors: $errorWords", color = SettingsDesignTokens.DangerRed)
                }
                
                if (languageBreakdown.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("By Language:", fontWeight = FontWeight.Bold)
                    languageBreakdown.forEach { (lang, count) ->
                        Text("• ${getLanguageName(lang)}: $count words")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

private fun getLanguageName(code: String): String {
    return when (code) {
        "id" -> "Indonesian"
        "en" -> "English"
        else -> code
    }
}