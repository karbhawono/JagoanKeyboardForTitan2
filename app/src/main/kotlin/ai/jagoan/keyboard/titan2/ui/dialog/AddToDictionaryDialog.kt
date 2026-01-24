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
 */

package ai.jagoan.keyboard.titan2.ui.dialog

import android.content.Context

/**
 * Simple language selection for adding words to dictionary.
 * Since popups don't work reliably from IME, we use the suggestion bar instead.
 */
object AddToDictionaryDialog {
    
    /**
     * For :atd command - shows options in suggestion bar.
     * Returns the word to be added. Language selection happens in the IME.
     */
    fun promptForLanguage(
        context: Context,
        word: String,
        onLanguageSelected: (language: String) -> Unit
    ) {
        // This is handled by the IME service showing language options in suggestion bar
        // The IME will call onLanguageSelected when user clicks a language chip
    }
}