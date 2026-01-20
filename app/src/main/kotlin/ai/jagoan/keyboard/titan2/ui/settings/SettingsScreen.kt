/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.ui.settings to ai.jagoan.keyboard.titan2.ui.settings
 * - Complete UI redesign with modern Material Design 3 approach
 * - Added SettingsDesignTokens object for consistent design tokens (colors, spacing, corner radii)
 * - Added QuickAccessToggles component: 2x2 grid for instant access to frequently used settings
 *   (Sticky Shift, Sticky Alt, Auto-capitalize, Long-press Accents)
 * - Added emoji icons to all settings for better visual recognition and scannability
 * - Implemented grouped settings cards (single card per section instead of individual cards)
 * - Added SectionHeader component with icon, title, and underline separator
 * - Added SettingItemWithIcon and ClickableSettingItem components for consistent item styling
 * - Implemented QuickToggleItem with active/inactive states (green border + filled background when active)
 * - Enhanced ActivationBanner with simplified activation flow and improved visual feedback
 * - Condensed setting descriptions for better readability
 * - Improved button hierarchy: primary (filled green), secondary (outlined), danger (red)
 * - Consistent 16dp rounded corners and spacing system (8dp, 16dp, 24dp, 28dp)
 * - Added proper dividers between settings items with icon offset alignment
 * - Changed title text color to white (0xFFFFFFFF) for better visibility
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

package ai.jagoan.keyboard.titan2.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ai.jagoan.keyboard.titan2.R
import ai.jagoan.keyboard.titan2.util.LocaleUtils

// Design tokens from design-mockups/DESIGN_SPECIFICATIONS.md
object SettingsDesignTokens {
    val JagoanGreen = Color(0xFFCDFF85)
    val BackgroundDark = Color(0xFF121212)
    val SurfaceDark = Color(0xFF1A1A1A)
    val OnSurface = Color(0xFFFFFFFF)
    val OnSurfaceVariant = Color(0xFF888888)
    val DangerRed = Color(0xFFFF6464)
    
    val QuickToggleActive = Color(0x1ACDFF85)  // 10% green
    val CardBorder = Color(0x0DFFFFFF)  // 5% white
    val DividerColor = Color(0x08FFFFFF)  // 3% white
    val IconBackground = Color(0x1ACDFF85)  // 10% green
    
    val CornerRadiusMedium = 12.dp
    val CornerRadiusLarge = 16.dp
    val IconSize = 24.sp
    val IconContainerSize = 40.dp
}

object SettingIcons {
    const val AUTO_CAPITALIZE = "🔠"
    const val LONG_PRESS_CAPITALIZE = "aA"
    const val LONG_PRESS_ACCENTS = "é"
    const val DOUBLE_SPACE = "⌨️"
    const val TEXT_SHORTCUTS = "💬"
    const val CURRENCY = "💰"
    const val LANGUAGE = "🌐"
    const val KEY_REPEAT = "⟲"
    const val STICKY_SHIFT = "📌"
    const val STICKY_ALT = "⌥"
    const val ALT_BACKSPACE = "⌫"
}

// Helper function to check if keyboard is enabled
fun isKeyboardEnabled(context: Context): Boolean {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    val enabledIMEs = imm?.enabledInputMethodList ?: emptyList()
    val packageName = context.packageName
    return enabledIMEs.any { it.packageName == packageName }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToShortcuts: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboardEnabled = remember(context) { isKeyboardEnabled(context) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SettingsDesignTokens.SurfaceDark
                )
            )
        },
        containerColor = SettingsDesignTokens.BackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Activation Banner
            ActivationBanner(
                isEnabled = keyboardEnabled,
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                    context.startActivity(intent)
                }
            )

            when (val state = settingsState) {
                is SettingsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                is SettingsUiState.Success -> {
                    SettingsContent(
                        settings = state.settings,
                        onAutoCapitalizeChanged = viewModel::updateAutoCapitalize,
                        onKeyRepeatChanged = viewModel::updateKeyRepeat,
                        onLongPressCapitalizeChanged = viewModel::updateLongPressCapitalize,
                        onDoubleSpacePeriodChanged = viewModel::updateDoubleSpacePeriod,
                        onTextShortcutsChanged = viewModel::updateTextShortcuts,
                        onStickyShiftChanged = viewModel::updateStickyShift,
                        onStickyAltChanged = viewModel::updateStickyAlt,
                        onAltBackspaceDeleteLineChanged = viewModel::updateAltBackspaceDeleteLine,
                        onPreferredCurrencyChanged = viewModel::updatePreferredCurrency,
                        onSelectedLanguageChanged = viewModel::updateSelectedLanguage,
                        onLongPressAccentsChanged = viewModel::updateLongPressAccents,
                        onManageShortcuts = onNavigateToShortcuts,
                        onNavigateToAbout = onNavigateToAbout,
                        onResetToDefaults = viewModel::resetToDefaults
                    )
                }
                is SettingsUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = SettingsDesignTokens.DangerRed,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivationBanner(
    isEnabled: Boolean,
    onOpenSettings: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SettingsDesignTokens.SurfaceDark,
        shape = RoundedCornerShape(SettingsDesignTokens.CornerRadiusLarge),
        border = BorderStroke(1.dp, SettingsDesignTokens.CardBorder)
    ) {
        if (isEnabled) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SettingsDesignTokens.JagoanGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Keyboard is active",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = SettingsDesignTokens.JagoanGreen
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.enable_ime_title),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = SettingsDesignTokens.JagoanGreen
                )
                Text(
                    text = stringResource(R.string.enable_ime_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SettingsDesignTokens.OnSurfaceVariant
                )
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SettingsDesignTokens.JagoanGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.enable_ime_button),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsContent(
    settings: ai.jagoan.keyboard.titan2.domain.model.KeyboardSettings,
    onAutoCapitalizeChanged: (Boolean) -> Unit,
    onKeyRepeatChanged: (Boolean) -> Unit,
    onLongPressCapitalizeChanged: (Boolean) -> Unit,
    onDoubleSpacePeriodChanged: (Boolean) -> Unit,
    onTextShortcutsChanged: (Boolean) -> Unit,
    onStickyShiftChanged: (Boolean) -> Unit,
    onStickyAltChanged: (Boolean) -> Unit,
    onAltBackspaceDeleteLineChanged: (Boolean) -> Unit,
    onPreferredCurrencyChanged: (String?) -> Unit,
    onSelectedLanguageChanged: (String) -> Unit,
    onLongPressAccentsChanged: (Boolean) -> Unit,
    onManageShortcuts: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onResetToDefaults: () -> Unit
) {
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Access Toggles
        QuickAccessToggles(
            stickyShift = settings.stickyShift,
            stickyAlt = settings.stickyAlt,
            autoCapitalize = settings.autoCapitalize,
            longPressAccents = settings.longPressAccents,
            onStickyShiftChanged = onStickyShiftChanged,
            onStickyAltChanged = onStickyAltChanged,
            onAutoCapitalizeChanged = onAutoCapitalizeChanged,
            onLongPressAccentsChanged = onLongPressAccentsChanged
        )

        Spacer(modifier = Modifier.height(8.dp))

        // General Section
        SectionHeader(icon = "⚙️", title = stringResource(R.string.settings_general))
        
        SettingsGroupCard {
            SettingItemWithIcon(
                icon = SettingIcons.AUTO_CAPITALIZE,
                title = stringResource(R.string.setting_auto_capitalize),
                description = "Capitalize first letter",
                checked = settings.autoCapitalize,
                onCheckedChange = onAutoCapitalizeChanged,
                isLast = false
            )
            
            SettingItemWithIcon(
                icon = SettingIcons.LONG_PRESS_CAPITALIZE,
                title = stringResource(R.string.setting_long_press_capitalize),
                description = "Hold letter key to capitalize",
                checked = settings.longPressCapitalize,
                onCheckedChange = onLongPressCapitalizeChanged,
                isLast = false
            )
            
            SettingItemWithIcon(
                icon = SettingIcons.LONG_PRESS_ACCENTS,
                title = "Long-press accents",
                description = "Cycle accents on hold (e→é→è→ê)",
                checked = settings.longPressAccents,
                onCheckedChange = onLongPressAccentsChanged,
                isLast = false
            )
            
            SettingItemWithIcon(
                icon = SettingIcons.DOUBLE_SPACE,
                title = stringResource(R.string.setting_double_space_period),
                description = "Double-space adds period",
                checked = settings.doubleSpacePeriod,
                onCheckedChange = onDoubleSpacePeriodChanged,
                isLast = false
            )
            
            SettingItemWithIcon(
                icon = SettingIcons.TEXT_SHORTCUTS,
                title = stringResource(R.string.setting_text_shortcuts),
                description = "Expand abbreviations (lm→I'm)",
                checked = settings.textShortcutsEnabled,
                onCheckedChange = onTextShortcutsChanged,
                isLast = false
            )
            
            ClickableSettingItem(
                icon = SettingIcons.CURRENCY,
                title = "Currency preference",
                description = settings.preferredCurrency?.let { it } 
                    ?: "Auto (${LocaleUtils.getDefaultCurrencySymbol()})",
                onClick = { showCurrencyDialog = true },
                isLast = false
            )
            
            ClickableSettingItem(
                icon = SettingIcons.LANGUAGE,
                title = "Language",
                description = ai.jagoan.keyboard.titan2.data.AccentRepository()
                    .getSupportedLanguages()
                    .find { it.first == settings.selectedLanguage }?.second
                    ?.substringBefore(" (") ?: "English",
                onClick = { showLanguageDialog = true },
                isLast = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Manage Substitutions Button
        Button(
            onClick = onManageShortcuts,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(SettingsDesignTokens.CornerRadiusMedium),
            colors = ButtonDefaults.buttonColors(
                containerColor = SettingsDesignTokens.SurfaceDark,
                contentColor = SettingsDesignTokens.JagoanGreen
            ),
            border = BorderStroke(1.dp, SettingsDesignTokens.CardBorder)
        ) {
            Text(
                text = stringResource(R.string.manage_shortcuts),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Advanced Section
        SectionHeader(icon = "🚀", title = stringResource(R.string.settings_advanced))
        
        SettingsGroupCard {
            SettingItemWithIcon(
                icon = SettingIcons.KEY_REPEAT,
                title = stringResource(R.string.setting_key_repeat),
                description = "Repeat character on long press",
                checked = settings.keyRepeatEnabled,
                onCheckedChange = onKeyRepeatChanged,
                isLast = false
            )
            
            SettingItemWithIcon(
                icon = SettingIcons.STICKY_SHIFT,
                title = stringResource(R.string.setting_sticky_shift),
                description = "Tap=one-shot, hold/double=lock",
                checked = settings.stickyShift,
                onCheckedChange = onStickyShiftChanged,
                isLast = false
            )
            
            SettingItemWithIcon(
                icon = SettingIcons.STICKY_ALT,
                title = stringResource(R.string.setting_sticky_alt),
                description = "Tap=one-shot, hold/double=lock",
                checked = settings.stickyAlt,
                onCheckedChange = onStickyAltChanged,
                isLast = false
            )
            
            SettingItemWithIcon(
                icon = SettingIcons.ALT_BACKSPACE,
                title = stringResource(R.string.setting_alt_backspace_delete_line),
                description = "Delete entire line before cursor",
                checked = settings.altBackspaceDeleteLine,
                onCheckedChange = onAltBackspaceDeleteLineChanged,
                isLast = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Button
        OutlinedButton(
            onClick = onNavigateToAbout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(SettingsDesignTokens.CornerRadiusMedium),
            border = BorderStroke(1.dp, SettingsDesignTokens.CardBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SettingsDesignTokens.OnSurface
            )
        ) {
            Text(
                text = stringResource(R.string.about_title),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reset to Defaults
        OutlinedButton(
            onClick = onResetToDefaults,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(SettingsDesignTokens.CornerRadiusMedium),
            border = BorderStroke(1.dp, SettingsDesignTokens.DangerRed),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SettingsDesignTokens.DangerRed
            )
        ) {
            Text(
                text = "Reset to Defaults",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Dialogs
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = settings.preferredCurrency,
            onCurrencySelected = { currency ->
                onPreferredCurrencyChanged(currency)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = settings.selectedLanguage,
            onLanguageSelected = { language ->
                onSelectedLanguageChanged(language)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
private fun QuickAccessToggles(
    stickyShift: Boolean,
    stickyAlt: Boolean,
    autoCapitalize: Boolean,
    longPressAccents: Boolean,
    onStickyShiftChanged: (Boolean) -> Unit,
    onStickyAltChanged: (Boolean) -> Unit,
    onAutoCapitalizeChanged: (Boolean) -> Unit,
    onLongPressAccentsChanged: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SettingsDesignTokens.CornerRadiusLarge),
        color = SettingsDesignTokens.SurfaceDark,
        border = BorderStroke(1.dp, SettingsDesignTokens.QuickToggleActive)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "QUICK ACCESS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp
                ),
                color = SettingsDesignTokens.OnSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickToggleItem(
                    modifier = Modifier.weight(1f),
                    icon = "📌",
                    label = "Sticky Shift",
                    isActive = stickyShift,
                    onClick = { onStickyShiftChanged(!stickyShift) }
                )
                QuickToggleItem(
                    modifier = Modifier.weight(1f),
                    icon = "⌥",
                    label = "Sticky Alt",
                    isActive = stickyAlt,
                    onClick = { onStickyAltChanged(!stickyAlt) }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickToggleItem(
                    modifier = Modifier.weight(1f),
                    icon = "🔠",
                    label = "Auto-cap",
                    isActive = autoCapitalize,
                    onClick = { onAutoCapitalizeChanged(!autoCapitalize) }
                )
                QuickToggleItem(
                    modifier = Modifier.weight(1f),
                    icon = "é",
                    label = "Accents",
                    isActive = longPressAccents,
                    onClick = { onLongPressAccentsChanged(!longPressAccents) }
                )
            }
        }
    }
}

@Composable
private fun QuickToggleItem(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .heightIn(min = 60.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(SettingsDesignTokens.CornerRadiusMedium),
        color = if (isActive) {
            SettingsDesignTokens.QuickToggleActive
        } else {
            Color(0x08FFFFFF)
        },
        border = if (isActive) {
            BorderStroke(1.dp, SettingsDesignTokens.JagoanGreen)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp,
                color = SettingsDesignTokens.OnSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                fontSize = 13.sp,
                color = SettingsDesignTokens.OnSurface
            )
        }
    }
}

@Composable
private fun SectionHeader(
    icon: String,
    title: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 22.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                ),
                color = SettingsDesignTokens.JagoanGreen
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            thickness = 2.dp,
            color = Color(0x33CDFF85)  // 20% green
        )
    }
}

@Composable
private fun SettingsGroupCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SettingsDesignTokens.CornerRadiusLarge),
        color = SettingsDesignTokens.BackgroundDark,
        border = BorderStroke(1.dp, SettingsDesignTokens.CardBorder)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SettingItemWithIcon(
    icon: String,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clickable { onCheckedChange(!checked) }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(SettingsDesignTokens.IconContainerSize)
                    .background(
                        SettingsDesignTokens.IconBackground,
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = SettingsDesignTokens.IconSize
                )
            }
            
            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    fontSize = 15.sp,
                    color = SettingsDesignTokens.OnSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = SettingsDesignTokens.OnSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Toggle
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
        
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 70.dp),
                color = SettingsDesignTokens.DividerColor,
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun ClickableSettingItem(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit,
    isLast: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(SettingsDesignTokens.IconContainerSize)
                    .background(
                        SettingsDesignTokens.IconBackground,
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = SettingsDesignTokens.IconSize
                )
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    fontSize = 15.sp,
                    color = SettingsDesignTokens.OnSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = SettingsDesignTokens.JagoanGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 70.dp),
                color = SettingsDesignTokens.DividerColor,
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun CurrencySelectionDialog(
    currentCurrency: String?,
    onCurrencySelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val currencies = listOf(
        null to "Auto (${LocaleUtils.getDefaultCurrencySymbol()})",
        "$" to "Dollar ($)",
        "€" to "Euro (€)",
        "£" to "Pound (£)",
        "¥" to "Yen (¥)",
        "Rp" to "Rupiah (Rp)",
        "₹" to "Rupee (₹)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Currency preference") },
        text = {
            LazyColumn {
                items(currencies) { (symbol, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCurrencySelected(symbol) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label)
                        if (currentCurrency == symbol) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = SettingsDesignTokens.JagoanGreen
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = ai.jagoan.keyboard.titan2.data.AccentRepository().getSupportedLanguages()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Language") },
        text = {
            LazyColumn {
                items(languages) { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(code) }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name)
                        if (currentLanguage == code) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = SettingsDesignTokens.JagoanGreen
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
