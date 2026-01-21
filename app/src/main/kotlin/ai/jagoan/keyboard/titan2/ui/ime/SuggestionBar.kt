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

package ai.jagoan.keyboard.titan2.ui.ime

import ai.jagoan.keyboard.titan2.domain.model.AutocorrectSuggestion
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Suggestion bar showing autocorrect suggestions
 */
@Composable
fun SuggestionBar(
    currentWord: String,
    suggestions: List<AutocorrectSuggestion>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty() && currentWord.isBlank()) {
        // Don't show empty bar
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF1A1A1A))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show current word (user can tap to keep it)
        if (currentWord.isNotBlank()) {
            SuggestionChip(
                text = currentWord,
                isCurrentWord = true,
                onClick = { onSuggestionClick(currentWord) },
                modifier = Modifier
            )
        }

        // Show suggestions
        suggestions.take(3).forEach { suggestion ->
            Spacer(modifier = Modifier.width(12.dp))
            
            SuggestionChip(
                text = suggestion.suggestion,
                isCurrentWord = false,
                isHighConfidence = suggestion.isHighConfidence(),
                onClick = { onSuggestionClick(suggestion.suggestion) },
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun SuggestionChip(
    text: String,
    isCurrentWord: Boolean,
    isHighConfidence: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(
                color = when {
                    isCurrentWord -> Color(0xFF3A3A3A)
                    isHighConfidence -> Color(0xFF5BA3F5)
                    else -> Color(0xFF4A90E2)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = if (isHighConfidence || !isCurrentWord) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
