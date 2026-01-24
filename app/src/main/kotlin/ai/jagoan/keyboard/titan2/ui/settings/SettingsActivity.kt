/**
 * Copyright (c) 2024-2025 Divefire
 * Original source: https://github.com/Divefire/titan2keyboard
 *
 * Modifications Copyright (c) 2025 Aryo Karbhawono
 *
 * Modifications:
 * - Renamed package from com.titan2keyboard.ui.settings to ai.jagoan.keyboard.titan2.ui.settings
 * - Added "licenses" route for displaying open source licenses
 * - Added navigation to LicensesScreen from AboutScreen
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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ai.jagoan.keyboard.titan2.ui.dictionary.DictionaryManagementScreen
import ai.jagoan.keyboard.titan2.ui.shortcuts.ShortcutManagementScreen
import ai.jagoan.keyboard.titan2.ui.theme.Titan2KeyboardTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main settings activity for the keyboard
 */
@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Titan2KeyboardTheme {
                SettingsNavigation()
            }
        }
    }
}

@Composable
private fun SettingsNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "settings"
    ) {
        composable("settings") {
            SettingsScreen(
                onNavigateToShortcuts = {
                    navController.navigate("shortcuts")
                },
                onNavigateToDictionary = {
                    navController.navigate("dictionary")
                },
                onNavigateToAbout = {
                    navController.navigate("about")
                }
            )
        }
        composable("shortcuts") {
            ShortcutManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("dictionary") {
            DictionaryManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("about") {
            AboutScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLicenses = {
                    navController.navigate("licenses")
                }
            )
        }
        composable("licenses") {
            LicensesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
