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
import ai.jagoan.keyboard.titan2.domain.model.SuggestionBarMode
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
        setBackgroundColor(Color.parseColor("#000000"))
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dpToPx(25f)
        )

        container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            // Add extra left padding to avoid overlapping with down arrow (60dp left)
            // Add extra right padding to avoid overlapping with keyboard icon (60dp right)
            setPadding(dpToPx(60f), 0, dpToPx(60f), 0)
        }

        addView(container)
    }

    fun setSuggestions(currentWord: String, suggestions: List<AutocorrectSuggestion>, mode: SuggestionBarMode = SuggestionBarMode.AUTO) {
        container.removeAllViews()

        // In ALWAYS_SHOW mode, keep bar visible even when empty
        if (mode == SuggestionBarMode.ALWAYS_SHOW) {
            visibility = View.VISIBLE
            if (currentWord.isBlank() && suggestions.isEmpty()) {
                // Show placeholder with empty space to keep bar visible and maintain structure
                container.addView(createPlaceholderText())
                return
            }
        } else {
            // AUTO and OFF modes - hide when empty
            if (currentWord.isBlank() && suggestions.isEmpty()) {
                visibility = View.GONE
                return
            }
            visibility = View.VISIBLE
        }

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

    fun updateSuggestions(currentWord: String, suggestions: List<AutocorrectSuggestion>, mode: SuggestionBarMode = SuggestionBarMode.AUTO) {
        setSuggestions(currentWord, suggestions, mode)
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
            textSize = 10f
            setTextColor(Color.WHITE)
            typeface = if (isHighConfidence) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL

            val bgColor = when {
                isCurrentWord -> Color.parseColor("#404040")
                isHighConfidence -> Color.parseColor("#4A90E2")
                else -> Color.parseColor("#505050")
            }
            setBackgroundColor(bgColor)

            val horizontalPadding = dpToPx(10f)
            val verticalPadding = dpToPx(6f)
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                marginEnd = dpToPx(8f)
                gravity = Gravity.CENTER_VERTICAL
            }

            // Ensure single line to prevent text wrapping
            setSingleLine(true)

            setOnClickListener {
                onSuggestionClickListener?.invoke(text)
            }
        }
    }

    private fun createPlaceholderText(): View {
        return TextView(context).apply {
            text = " " // Empty space instead of text
            textSize = 10f
            setTextColor(Color.parseColor("#808080"))
            typeface = Typeface.DEFAULT
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL

            val horizontalPadding = dpToPx(10f)
            val verticalPadding = dpToPx(6f)
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }

            setSingleLine(true)
            isClickable = false
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
