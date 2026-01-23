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
import ai.jagoan.keyboard.titan2.domain.model.ViMode
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Non-Compose suggestion bar for autocorrect
 */
class SuggestionBarView(context: Context) : FrameLayout(context) {

    private val scrollView: HorizontalScrollView
    private val container: LinearLayout
    private val viModeIndicator: ViModeIndicatorView
    private var onSuggestionClickListener: ((String) -> Unit)? = null
    var fixedTextSize: Float = 16f

    init {
        setBackgroundColor(Color.parseColor("#000000"))
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dpToPx(25f)
        )

        // Create container for suggestions
        container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER
            // Add extra left padding to avoid overlapping with down arrow (60dp left)
            // Add extra right padding to avoid overlapping with keyboard icon (60dp right)
            setPadding(dpToPx(60f), 0, dpToPx(60f), 0)
        }

        // Create scroll view for suggestions
        scrollView = HorizontalScrollView(context).apply {
            layoutParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
            addView(container)
        }

        // Create Vi mode indicator
        viModeIndicator = ViModeIndicatorView(context).apply {
            layoutParams = LayoutParams(
                dpToPx(16f),
                dpToPx(16f)
            ).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
                marginEnd = dpToPx(8f)
            }
        }

        // Add views to the frame layout
        addView(scrollView)
        addView(viModeIndicator)
    }

    fun setSuggestions(currentWord: String, suggestions: List<AutocorrectSuggestion>, mode: SuggestionBarMode = SuggestionBarMode.AUTO) {
        container.removeAllViews()

        // In ALWAYS_SHOW mode, keep bar visible even when empty
        if (mode == SuggestionBarMode.ALWAYS_SHOW) {
            scrollView.visibility = View.VISIBLE
            if (currentWord.isBlank() && suggestions.isEmpty()) {
                // Show placeholder with empty space to keep bar visible and maintain structure
                container.addView(createPlaceholderText())
                return
            }
        } else {
            // AUTO and OFF modes - hide when empty
            if (currentWord.isBlank() && suggestions.isEmpty()) {
                scrollView.visibility = View.GONE
                return
            }
            scrollView.visibility = View.VISIBLE
        }

        // Sort suggestions: current word, high confidence (bold), normal
        val sortedSuggestions = mutableListOf<Pair<String, SuggestionType>>()

        // Add current word first
        if (currentWord.isNotBlank()) {
            sortedSuggestions.add(Pair(currentWord, SuggestionType.CURRENT))
        }

        // Separate high confidence and normal suggestions
        val highConfidence = suggestions.filter { it.isHighConfidence() }.take(3)
        val normal = suggestions.filterNot { it.isHighConfidence() }.take(3 - highConfidence.size)

        // Add high confidence suggestions
        highConfidence.forEach { suggestion ->
            sortedSuggestions.add(Pair(suggestion.suggestion, SuggestionType.HIGH_CONFIDENCE))
        }

        // Add normal suggestions
        normal.forEach { suggestion ->
            sortedSuggestions.add(Pair(suggestion.suggestion, SuggestionType.NORMAL))
        }

        // Create chips in sorted order
        sortedSuggestions.take(3).forEach { (text, type) ->
            container.addView(createSuggestionChip(
                text,
                isCurrentWord = type == SuggestionType.CURRENT,
                isHighConfidence = type == SuggestionType.HIGH_CONFIDENCE
            ))
        }
    }

    fun updateSuggestions(currentWord: String, suggestions: List<AutocorrectSuggestion>, mode: SuggestionBarMode = SuggestionBarMode.AUTO) {
        setSuggestions(currentWord, suggestions, mode)
    }

    fun setOnSuggestionClickListener(listener: (String) -> Unit) {
        onSuggestionClickListener = listener
    }

    /**
     * Update Vi mode indicator visibility
     */
    fun updateViMode(mode: ViMode) {
        viModeIndicator.updateViMode(mode)
    }

    private fun createSuggestionChip(
        text: String,
        isCurrentWord: Boolean,
        isHighConfidence: Boolean = false
    ): View {
        return TextView(context).apply {
            this.text = text
            textSize = fixedTextSize
            setTextColor(Color.WHITE)
            typeface = if (isHighConfidence) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL

            // All chips have black background
            setBackgroundColor(Color.BLACK)

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
            textSize = fixedTextSize
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

    private enum class SuggestionType {
        CURRENT,
        HIGH_CONFIDENCE,
        NORMAL
    }
}
