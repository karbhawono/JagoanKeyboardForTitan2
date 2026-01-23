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

import ai.jagoan.keyboard.titan2.R
import ai.jagoan.keyboard.titan2.domain.model.ViMode
import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView

/**
 * Visual indicator for Vi mode state
 * Shows a green bullet when Vi mode is enabled
 */
class ViModeIndicatorView(context: Context) : FrameLayout(context) {

    private val indicatorIcon: ImageView

    init {
        // Create the indicator icon
        indicatorIcon = ImageView(context).apply {
            setImageResource(R.drawable.ic_vi_mode_enabled)
            layoutParams = LayoutParams(
                dpToPx(8f),
                dpToPx(8f)
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        addView(indicatorIcon)

        // Initially hidden
        visibility = View.GONE

        // Set layout params for the container
        layoutParams = LayoutParams(
            dpToPx(16f),
            dpToPx(16f)
        )
    }

    /**
     * Update the indicator based on Vi mode state
     */
    fun updateViMode(mode: ViMode) {
        visibility = when (mode) {
            ViMode.ENABLED -> View.VISIBLE
            ViMode.DISABLED -> View.GONE
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