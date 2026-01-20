/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.domain.model to ai.jagoan.keyboard.titan2.domain.model
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

package ai.jagoan.keyboard.titan2.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a text shortcut/auto-correction rule
 * @param id Unique identifier for the shortcut
 * @param trigger The text pattern to match (e.g., "Im")
 * @param replacement The text to replace it with (e.g., "I'm")
 * @param caseSensitive Whether the trigger should be case-sensitive
 * @param isDefault Whether this is a default shortcut (cannot be deleted, only disabled)
 */
@Serializable
data class TextShortcut(
    val id: String,
    val trigger: String,
    val replacement: String,
    val caseSensitive: Boolean = false,
    val isDefault: Boolean = false
)
