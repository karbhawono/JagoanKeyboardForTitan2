/**
 * Copyright (c) 2025 Aryo Karbhawono
 *
 * This file displays open source license information for all third-party
 * libraries and frameworks used in Jagoan Keyboard for Titan 2.
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

package ai.jagoan.keyboard.titan2.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.jagoan.keyboard.titan2.data.LibraryInfo
import ai.jagoan.keyboard.titan2.data.LicenseDataProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val categories = LicenseDataProvider.getAllCategories()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Open Source Licenses",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SettingsDesignTokens.SurfaceDark
                )
            )
        },
        containerColor = SettingsDesignTokens.BackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Header
            item {
                Text(
                    text = "This app uses the following open source libraries and frameworks:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SettingsDesignTokens.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Categories
            categories.forEach { (categoryName, libraries) ->
                item {
                    LicenseCategory(
                        title = categoryName,
                        libraries = libraries,
                        onUrlClick = { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }
            }
            
            // Footer spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LicenseCategory(
    title: String,
    libraries: List<LibraryInfo>,
    onUrlClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Category Header
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = SettingsDesignTokens.JagoanGreen,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        
        // Libraries in this category
        libraries.forEach { library ->
            LibraryCard(
                library = library,
                onUrlClick = onUrlClick
            )
        }
    }
}

@Composable
private fun LibraryCard(
    library: LibraryInfo,
    onUrlClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SettingsDesignTokens.CornerRadiusLarge),
        color = SettingsDesignTokens.SurfaceDark,
        border = BorderStroke(1.dp, SettingsDesignTokens.CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Library Name
            Text(
                text = library.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = SettingsDesignTokens.OnSurface
            )
            
            // Group ID (if available)
            library.groupId?.let { groupId ->
                Text(
                    text = groupId,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = SettingsDesignTokens.OnSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Version (if available)
            library.version?.let { version ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Version:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SettingsDesignTokens.OnSurfaceVariant
                    )
                    Text(
                        text = version,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = SettingsDesignTokens.OnSurface
                    )
                }
            }
            
            // Purpose
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Purpose:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SettingsDesignTokens.OnSurfaceVariant
                )
                Text(
                    text = library.purpose,
                    style = MaterialTheme.typography.bodySmall,
                    color = SettingsDesignTokens.OnSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // License
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "License:",
                    style = MaterialTheme.typography.bodySmall,
                    color = SettingsDesignTokens.OnSurfaceVariant
                )
                Text(
                    text = library.license,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = SettingsDesignTokens.JagoanGreen
                )
            }
            
            // URL (clickable if available)
            library.url?.let { url ->
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = SettingsDesignTokens.DividerColor
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUrlClick(url) }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View Source",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = SettingsDesignTokens.JagoanGreen
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open URL",
                        tint = SettingsDesignTokens.JagoanGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
