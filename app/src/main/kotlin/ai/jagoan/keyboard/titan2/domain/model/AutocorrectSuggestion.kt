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
 * - Created autocorrect suggestion data model with confidence scoring
 * - Added SuggestionSource enum for tracking suggestion origin
 * - Added SuggestionMetadata for additional suggestion information
 */

package ai.jagoan.keyboard.titan2.domain.model

/**
 * Represents an autocorrect suggestion with confidence score and metadata.
 */
data class AutocorrectSuggestion(
    val original: String,
    val suggestion: String,
    val confidence: Float, // 0.0 to 1.0
    val source: SuggestionSource,
    val metadata: SuggestionMetadata = SuggestionMetadata()
) : Comparable<AutocorrectSuggestion> {
    
    override fun compareTo(other: AutocorrectSuggestion): Int {
        // Higher confidence first
        return other.confidence.compareTo(this.confidence)
    }
    
    fun isHighConfidence(): Boolean = confidence >= 0.8f
    fun isMediumConfidence(): Boolean = confidence in 0.5f..0.79f
    fun isLowConfidence(): Boolean = confidence < 0.5f
}

/**
 * Source of the suggestion
 */
enum class SuggestionSource {
    DICTIONARY,      // From dictionary lookup
    CONTRACTION,     // Smart contraction (e.g., didnt -> didn't)
    KEYBOARD_PROXIMITY, // Keyboard proximity correction
    FREQUENCY,       // Based on word frequency
    PERSONAL,        // From personal dictionary/learned words
    CONTEXT          // Context-aware suggestion
}

/**
 * Metadata about the suggestion
 */
data class SuggestionMetadata(
    val editDistance: Int = 0,
    val language: String? = null,
    val isContraction: Boolean = false,
    val proximityScore: Float = 0f,
    val frequencyRank: Int = Int.MAX_VALUE
)