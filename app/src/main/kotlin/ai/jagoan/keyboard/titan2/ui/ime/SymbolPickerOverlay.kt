/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.ui.ime to ai.jagoan.keyboard.titan2.ui.ime
 * - Updated data and UI imports
 * - Completely redesigned symbol picker from centered grid layout to bottom-aligned keyboard layout
 * - Changed from 6-column LazyVerticalGrid to 3-row physical keyboard mapping layout
 * - Added physical keyboard button mapping (Q-P, A-L, Z-M) matching Titan 2 QWERTY layout
 * - Added getSymbolIndexForKeyCode() function to map physical keys to symbol indices (26 keys)
 * - Changed buttons to full-width with equal weight distribution per row
 * - Added row offset support (startWeightFraction) for authentic QWERTY staggered layout
 * - Added key labels below symbols showing corresponding physical keys (Q, W, E, etc.)
 * - Changed animation from scaleIn/scaleOut to slideInVertically/slideOutVertically
 * - Changed from centered Card UI to bottom-aligned keyboard-style overlay
 * - Added visual keyboard key styling with borders and rounded corners
 * - Increased file from 188 lines to 317 lines (+129 lines for keyboard layout)
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


package ai.jagoan.keyboard.titan2.ui.ime

import android.util.Log
import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.jagoan.keyboard.titan2.domain.model.SymbolCategory
import ai.jagoan.keyboard.titan2.domain.model.SymbolData

/**
 * Bottom-aligned symbol picker overlay matching Titan 2 physical QWERTY keyboard layout.
 *
 * Displays symbols in 3 rows matching the physical keys (Q-P, A-L, Z-M) so users can
 * press the corresponding physical key to insert the symbol shown above it.
 */
@Composable
fun SymbolPickerOverlay(
    visible: Boolean,
    currentCategory: SymbolCategory,
    onSymbolSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            KeyboardStyleSymbolPicker(
                category = currentCategory,
                onSymbolSelected = onSymbolSelected,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consume clicks to prevent dismissal */ }
            )
        }
    }
}

@Composable
private fun KeyboardStyleSymbolPicker(
    category: SymbolCategory,
    onSymbolSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val symbols = remember(category) {
        SymbolData.getSymbolsForCategory(category)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Category header
        Text(
            text = category.displayName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE0E0E0),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Press physical key to insert • Press SYM for next categories",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF888888),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Row 1: Q W E R T Y U I O P (10 keys, indices 0-9)
        KeyboardRow(
            keys = listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
            symbols = symbols.take(10),
            onSymbolSelected = onSymbolSelected,
            startWeightFraction = 0f  // No offset for first row
        )

        // Row 2: A S D F G H J K L (9 keys, indices 10-18)
        if (symbols.size > 10) {
            Spacer(modifier = Modifier.height(6.dp))

            KeyboardRow(
                keys = listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
                symbols = symbols.drop(10).take(9),
                onSymbolSelected = onSymbolSelected,
                startWeightFraction = 0.5f  // Standard row 2 offset
            )
        }

        // Row 3: Z X C V B N M (7 keys, indices 19-25) - only show if needed
        if (category != SymbolCategory.PUNCTUATION && symbols.size > 19) {
            Spacer(modifier = Modifier.height(6.dp))

            KeyboardRow(
                keys = listOf("Z", "X", "C", "V", "B", "N", "M"),
                symbols = symbols.drop(19).take(7),
                onSymbolSelected = onSymbolSelected,
                startWeightFraction = 1.5f  // Larger offset (1.5 key widths)
            )
        }
    }
}

@Composable
private fun KeyboardRow(
    keys: List<String>,
    symbols: List<ai.jagoan.keyboard.titan2.domain.model.Symbol>,
    onSymbolSelected: (String) -> Unit,
    startWeightFraction: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Add starting spacer for row offset (proportional to key width)
        if (startWeightFraction > 0f) {
            Spacer(modifier = Modifier.weight(startWeightFraction))
        }

        // Render each key with equal weight
        keys.forEachIndexed { index, keyLetter ->
            if (index < symbols.size) {
                SymbolKeyButton(
                    symbol = symbols[index].character,
                    keyLabel = keyLetter,
                    onClick = { onSymbolSelected(symbols[index].character) },
                    modifier = Modifier.weight(1f)  // Equal weight for all buttons
                )

                // Small gap between buttons (fixed width, not weight-based)
                if (index < keys.size - 1) {
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }

        // Add ending spacer to fill remaining space (only if needed)
        val endWeight = 10f - keys.size - startWeightFraction
        if (endWeight > 0f) {
            Spacer(modifier = Modifier.weight(endWeight))
        }
    }
}

@Composable
private fun SymbolKeyButton(
    symbol: String,
    keyLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = Color(0xFF4A4A4A),
                shape = RoundedCornerShape(6.dp)
            ),
        shape = RoundedCornerShape(6.dp),
        color = Color.Black,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Symbol on top (larger)
            Text(
                text = symbol,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFFE0E0E0)
            )

            // Physical key label at bottom (smaller, dimmed)
            Text(
                text = keyLabel,
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = Color(0xFF888888)
            )
        }
    }
}

/**
 * Get the next category in sequence for cycling
 */
fun getNextCategory(current: SymbolCategory): SymbolCategory {
    val categories = SymbolData.categories
    val currentIndex = categories.indexOf(current)
    val nextIndex = (currentIndex + 1) % categories.size
    return categories[nextIndex]
}

/**
 * Map physical key code to symbol index (0-25 for A-Z letter keys)
 * Returns null if key is not a letter key
 */
fun getSymbolIndexForKeyCode(keyCode: Int): Int? {
    return when (keyCode) {
        // Row 1: Q=0, W=1, E=2, R=3, T=4, Y=5, U=6, I=7, O=8, P=9
        KeyEvent.KEYCODE_Q -> 0
        KeyEvent.KEYCODE_W -> 1
        KeyEvent.KEYCODE_E -> 2
        KeyEvent.KEYCODE_R -> 3
        KeyEvent.KEYCODE_T -> 4
        KeyEvent.KEYCODE_Y -> 5
        KeyEvent.KEYCODE_U -> 6
        KeyEvent.KEYCODE_I -> 7
        KeyEvent.KEYCODE_O -> 8
        KeyEvent.KEYCODE_P -> 9

        // Row 2: A=10, S=11, D=12, F=13, G=14, H=15, J=16, K=17, L=18
        KeyEvent.KEYCODE_A -> 10
        KeyEvent.KEYCODE_S -> 11
        KeyEvent.KEYCODE_D -> 12
        KeyEvent.KEYCODE_F -> 13
        KeyEvent.KEYCODE_G -> 14
        KeyEvent.KEYCODE_H -> 15
        KeyEvent.KEYCODE_J -> 16
        KeyEvent.KEYCODE_K -> 17
        KeyEvent.KEYCODE_L -> 18

        // Row 3: Z=19, X=20, C=21, V=22, B=23, N=24, M=25
        KeyEvent.KEYCODE_Z -> 19
        KeyEvent.KEYCODE_X -> 20
        KeyEvent.KEYCODE_C -> 21
        KeyEvent.KEYCODE_V -> 22
        KeyEvent.KEYCODE_B -> 23
        KeyEvent.KEYCODE_N -> 24
        KeyEvent.KEYCODE_M -> 25

        else -> null
    }
}
