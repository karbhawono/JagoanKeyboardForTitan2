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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ai.jagoan.keyboard.titan2.domain.model.SymbolCategory

/**
 * Main IME input view that hosts the symbol picker.
 * 
 * This is the proper way to display IME UI components - as part of the InputMethodService's
 * input view. This ensures the component receives both touch events and keyboard events
 * properly, without the complexity and fragility of WindowManager overlays.
 * 
 * The view is always present but the symbol picker content is only shown when visible=true.
 */
@Composable
fun ImeInputView(
    symbolPickerVisible: State<Boolean>,
    symbolPickerCategory: State<SymbolCategory>,
    onSymbolSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // Symbol picker overlay - only shows when visible
        SymbolPickerOverlay(
            visible = symbolPickerVisible.value,
            currentCategory = symbolPickerCategory.value,
            onSymbolSelected = onSymbolSelected,
            onDismiss = onDismiss
        )
    }
}