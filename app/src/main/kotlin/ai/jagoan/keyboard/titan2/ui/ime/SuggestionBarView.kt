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
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Non-Compose suggestion bar for autocorrect
 */
class SuggestionBarView(context: Context) : HorizontalScrollView(context) {
    
    private val container: LinearLayout
    private var onSuggestionClickListener: ((String) -> Unit)? = null
    
    init {
        setBackgroundColor(Color.parseColor("#2C2C2C"))
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dpToPx(48f)
        )
        
        container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setPadding(dpToPx(8f), 0, dpToPx(8f), 0)
        }
        
        addView(container)
    }
    
    fun setSuggestions(currentWord: String, suggestions: List<AutocorrectSuggestion>) {
        container.removeAllViews()
        
        if (currentWord.isBlank() && suggestions.isEmpty()) {
            visibility = View.GONE
            return
        }
        
        visibility = View.VISIBLE
        
        // Add current word chip
        if (currentWord.isNotBlank()) {
            container.addView(createSuggestionChip(currentWord, isCurrentWord = true))
        }
        
        // Add suggestion chips
        suggestions.take(3).forEach { suggestion ->
            container.addView(createSuggestionChip(
                suggestion.suggestion,
                isCurrentWord = false,
                isHighConfidence = suggestion.isHighConfidence()
            ))
        }
    }
    
    fun setOnSuggestionClickListener(listener: (String) -> Unit) {
        onSuggestionClickListener = listener
    }
    
    private fun createSuggestionChip(
        text: String,
        isCurrentWord: Boolean,
        isHighConfidence: Boolean = false
    ): View {
        return TextView(context).apply {
            this.text = text
            textSize = 16f
            setTextColor(Color.WHITE)
            typeface = if (isHighConfidence) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            gravity = Gravity.CENTER
            
            val bgColor = when {
                isCurrentWord -> Color.parseColor("#404040")
                isHighConfidence -> Color.parseColor("#4A90E2")
                else -> Color.parseColor("#505050")
            }
            setBackgroundColor(bgColor)
            
            val padding = dpToPx(12f)
            setPadding(padding, dpToPx(8f), padding, dpToPx(8f))
            
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                marginEnd = dpToPx(8f)
            }
            
            setOnClickListener {
                onSuggestionClickListener?.invoke(text)
            }
        }
    }
    
    private fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }
}
