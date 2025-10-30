package com.chain.messaging.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.messaging.domain.model.AppearanceSettings
import com.chain.messaging.domain.model.AppTheme
import com.chain.messaging.domain.model.FontSize
import com.chain.messaging.presentation.components.WallpaperPicker

/**
 * Appearance settings screen for customizing theme and display options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    var selectedTheme by remember { mutableStateOf(AppTheme.SYSTEM) }
    var selectedFontSize by remember { mutableStateOf(FontSize.MEDIUM) }
    var useSystemEmojis by remember { mutableStateOf(false) }
    var showAvatarsInGroups by remember { mutableStateOf(true) }
    var compactMode by remember { mutableStateOf(false) }
    var showWallpaperPicker by remember { mutableStateOf(false) }
    
    // Initialize form with current settings
    LaunchedEffect(uiState.settings) {
        uiState.settings?.appearance?.let { appearance ->
            selectedTheme = appearance.theme
            selectedFontSize = appearance.fontSize
            useSystemEmojis = appearance.useSystemEmojis
            showAvatarsInGroups = appearance.showAvatarsInGroups
            compactMode = appearance.compactMode
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Appearance") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        val updatedAppearance = AppearanceSettings(
                            theme = selectedTheme,
                            fontSize = selectedFontSize,
                            useSystemEmojis = useSystemEmojis,
                            showAvatarsInGroups = showAvatarsInGroups,
                            compactMode = compactMode
                        )
                        viewModel.updateAppearanceSettings(updatedAppearance)
                    }
                ) {
                    Text("Save")
                }
            }
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Selection
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    AppTheme.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedTheme == theme,
                                    onClick = { selectedTheme = theme },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTheme == theme,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = when (theme) {
                                        AppTheme.LIGHT -> "Light"
                                        AppTheme.DARK -> "Dark"
                                        AppTheme.SYSTEM -> "System Default"
                                    }
                                )
                                Text(
                                    text = when (theme) {
                                        AppTheme.LIGHT -> "Always use light theme"
                                        AppTheme.DARK -> "Always use dark theme"
                                        AppTheme.SYSTEM -> "Follow system setting"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            
            // Font Size Selection
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Font Size",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    FontSize.values().forEach { fontSize ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedFontSize == fontSize,
                                    onClick = { selectedFontSize = fontSize },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFontSize == fontSize,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (fontSize) {
                                    FontSize.SMALL -> "Small"
                                    FontSize.MEDIUM -> "Medium"
                                    FontSize.LARGE -> "Large"
                                    FontSize.EXTRA_LARGE -> "Extra Large"
                                }
                            )
                        }
                    }
                }
            }
            
            // Chat Wallpaper
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Chat Wallpaper",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedButton(
                        onClick = { showWallpaperPicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Wallpaper, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose Wallpaper")
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.resetWallpaper() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset to Default")
                    }
                    
                    // Show current wallpaper info if set
                    uiState.settings?.appearance?.chatWallpaper?.let { wallpaper ->
                        Text(
                            text = "Current: ${if (wallpaper.startsWith("content://")) "Custom Image" else wallpaper.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Display Options
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Display Options",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Use System Emojis")
                            Text(
                                "Use your device's emoji set instead of app emojis",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useSystemEmojis,
                            onCheckedChange = { useSystemEmojis = it }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Show Avatars in Groups")
                            Text(
                                "Display profile pictures in group chats",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showAvatarsInGroups,
                            onCheckedChange = { showAvatarsInGroups = it }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Compact Mode")
                            Text(
                                "Reduce spacing and padding for more content",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = compactMode,
                            onCheckedChange = { compactMode = it }
                        )
                    }
                }
            }
        }
    }
    
    // Wallpaper picker dialog
    if (showWallpaperPicker) {
        WallpaperPicker(
            currentWallpaper = uiState.settings?.appearance?.chatWallpaper,
            onWallpaperSelected = { wallpaper ->
                viewModel.updateWallpaper(wallpaper)
            },
            onDismiss = { showWallpaperPicker = false }
        )
    }
}