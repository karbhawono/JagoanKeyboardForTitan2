/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.domain.repository to ai.jagoan.keyboard.titan2.domain.repository
 * - Updated domain model imports
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

package ai.jagoan.keyboard.titan2.domain.repository

import ai.jagoan.keyboard.titan2.domain.model.TextShortcut
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing text shortcuts
 */
interface ShortcutRepository {
    /**
     * Flow of all available shortcuts
     */
    val shortcutsFlow: Flow<List<TextShortcut>>

    /**
     * Get all available shortcuts (snapshot)
     */
    suspend fun getShortcuts(): List<TextShortcut>

    /**
     * Find a replacement for the given text
     * Synchronous lookup using in-memory cache for real-time typing performance
     * @param text The text to look up
     * @return The replacement text, or null if no shortcut matches
     */
    fun findReplacement(text: String): String?

    /**
     * Add a new shortcut
     */
    suspend fun addShortcut(shortcut: TextShortcut)

    /**
     * Update an existing shortcut
     */
    suspend fun updateShortcut(shortcut: TextShortcut)

    /**
     * Delete a shortcut by ID
     */
    suspend fun deleteShortcut(id: String)

    /**
     * Initialize with default shortcuts if none exist
     */
    suspend fun initializeDefaults()
}
