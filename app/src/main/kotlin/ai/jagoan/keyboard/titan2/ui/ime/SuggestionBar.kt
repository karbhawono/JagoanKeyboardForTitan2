package ai.jagoan.keyboard.titan2.ui.ime

import ai.jagoan.keyboard.titan2.domain.model.AutocorrectSuggestion
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SuggestionBar(
    currentWord: String,
    suggestions: List<AutocorrectSuggestion>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displaySuggestions = buildList {
        if (currentWord.isNotBlank()) {
            add(currentWord)
        }
        suggestions.take(2).forEach { add(it.suggestion) }
        while (size < 3) {
            add("")
        }
    }.take(3)
    
    if (displaySuggestions.all { it.isBlank() }) {
        return
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF2C2C2C)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        displaySuggestions.forEachIndexed { index, text ->
            if (index > 0) {
                VerticalDivider(
                    color = Color(0xFF444444),
                    modifier = Modifier.fillMaxHeight(0.6f).width(1.dp)
                )
            }
            
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .clickable(enabled = text.isNotBlank()) { onSuggestionClick(text) }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (text.isNotBlank()) {
                    Text(
                        text = text,
                        color = if (index == 0) Color(0xFFE0E0E0) else Color(0xFFFFFFFF),
                        fontSize = 17.sp,
                        fontWeight = if (index == 1) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
