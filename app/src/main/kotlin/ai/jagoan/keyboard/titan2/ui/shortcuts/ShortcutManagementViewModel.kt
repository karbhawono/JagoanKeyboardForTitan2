/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.ui.shortcuts to ai.jagoan.keyboard.titan2.ui.shortcuts
 * - Updated domain repository and model imports
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

package ai.jagoan.keyboard.titan2.ui.shortcuts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.jagoan.keyboard.titan2.domain.model.TextShortcut
import ai.jagoan.keyboard.titan2.domain.repository.ShortcutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for managing text shortcuts
 */
@HiltViewModel
class ShortcutManagementViewModel @Inject constructor(
    private val shortcutRepository: ShortcutRepository
) : ViewModel() {

    /**
     * UI state for the shortcuts screen
     */
    val uiState: StateFlow<ShortcutManagementUiState> = shortcutRepository.shortcutsFlow
        .map { shortcuts ->
            ShortcutManagementUiState.Success(shortcuts)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ShortcutManagementUiState.Loading
        )

    /**
     * Add a new shortcut
     */
    fun addShortcut(trigger: String, replacement: String, caseSensitive: Boolean = false) {
        viewModelScope.launch {
            val shortcut = TextShortcut(
                id = UUID.randomUUID().toString(),
                trigger = trigger,
                replacement = replacement,
                caseSensitive = caseSensitive,
                isDefault = false
            )
            shortcutRepository.addShortcut(shortcut)
        }
    }

    /**
     * Update an existing shortcut
     */
    fun updateShortcut(shortcut: TextShortcut) {
        viewModelScope.launch {
            shortcutRepository.updateShortcut(shortcut)
        }
    }

    /**
     * Delete a shortcut
     * Default shortcuts cannot be deleted
     */
    fun deleteShortcut(id: String) {
        viewModelScope.launch {
            shortcutRepository.deleteShortcut(id)
        }
    }
}

/**
 * UI state for the shortcuts management screen
 */
sealed interface ShortcutManagementUiState {
    data object Loading : ShortcutManagementUiState
    data class Success(val shortcuts: List<TextShortcut>) : ShortcutManagementUiState
}
