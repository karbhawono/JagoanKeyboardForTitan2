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

/**
 * State of a modifier key (Shift or Alt)
 */
enum class ModifierState {
    /** Modifier is not active */
    NONE,

    /** Modifier is active for one character (single press) */
    ONE_SHOT,

    /** Modifier is locked on (long press) until pressed again */
    LOCKED
}

/**
 * Tracks the state of all modifiers
 */
data class ModifiersState(
    val shift: ModifierState = ModifierState.NONE,
    val alt: ModifierState = ModifierState.NONE,
    val symPickerVisible: Boolean = false,
    val symCategory: SymbolCategory = SymbolCategory.PUNCTUATION
) {
    /**
     * Check if shift is active (either one-shot or locked)
     */
    fun isShiftActive(): Boolean = shift != ModifierState.NONE

    /**
     * Check if alt is active (either one-shot or locked)
     */
    fun isAltActive(): Boolean = alt != ModifierState.NONE

    /**
     * Check if any modifier is in one-shot mode
     */
    fun hasOneShotModifier(): Boolean = shift == ModifierState.ONE_SHOT || alt == ModifierState.ONE_SHOT

    /**
     * Check if symbol picker is showing
     */
    fun isSymPickerActive(): Boolean = symPickerVisible
}
