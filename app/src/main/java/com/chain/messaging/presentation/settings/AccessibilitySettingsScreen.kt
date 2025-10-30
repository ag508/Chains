package com.chain.messaging.presentation.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.messaging.domain.model.AccessibilitySettings

/**
 * Accessibility settings screen for accessibility and ease of use options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    var highContrast by remember { mutableStateOf(false) }
    var largeText by remember { mutableStateOf(false) }
    var reduceMotion by remember { mutableStateOf(false) }
    var screenReaderOptimized by remember { mutableStateOf(false) }
    var hapticFeedback by remember { mutableStateOf(true) }
    var voiceOverEnabled by remember { mutableStateOf(false) }
    
    // Initialize form with current settings
    LaunchedEffect(uiState.settings) {
        uiState.settings?.accessibility?.let { accessibility ->
            highContrast = accessibility.highContrast
            largeText = accessibility.largeText
            reduceMotion = accessibility.reduceMotion
            screenReaderOptimized = accessibility.screenReaderOptimized
            hapticFeedback = accessibility.hapticFeedback
            voiceOverEnabled = accessibility.voiceOverEnabled
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Accessibility") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        val updatedAccessibility = AccessibilitySettings(
                            highContrast = highContrast,
                            largeText = largeText,
                            reduceMotion = reduceMotion,
                            screenReaderOptimized = screenReaderOptimized,
                            hapticFeedback = hapticFeedback,
                            voiceOverEnabled = voiceOverEnabled
                        )
                        viewModel.updateAccessibilitySettings(updatedAccessibility)
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
            // Visual Accessibility
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Visual Accessibility",
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
                            Text("High Contrast")
                            Text(
                                "Increase contrast for better visibility",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = highContrast,
                            onCheckedChange = { highContrast = it }
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
                            Text("Large Text")
                            Text(
                                "Use larger text sizes throughout the app",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = largeText,
                            onCheckedChange = { largeText = it }
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
                            Text("Reduce Motion")
                            Text(
                                "Minimize animations and transitions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = reduceMotion,
                            onCheckedChange = { reduceMotion = it }
                        )
                    }
                }
            }
            
            // Screen Reader Support
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Screen Reader Support",
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
                            Text("Screen Reader Optimized")
                            Text(
                                "Optimize interface for screen readers",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = screenReaderOptimized,
                            onCheckedChange = { screenReaderOptimized = it }
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
                            Text("Voice Over")
                            Text(
                                "Enable voice over for UI elements",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = voiceOverEnabled,
                            onCheckedChange = { voiceOverEnabled = it }
                        )
                    }
                }
            }
            
            // Interaction Accessibility
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Interaction",
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
                            Text("Haptic Feedback")
                            Text(
                                "Provide tactile feedback for interactions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = hapticFeedback,
                            onCheckedChange = { hapticFeedback = it }
                        )
                    }
                }
            }
            
            // Accessibility Information
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Additional Information",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = "Chain is designed to be accessible to all users. These settings help customize the app for your specific needs. For additional accessibility features, please check your device's system accessibility settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedButton(
                        onClick = { 
                            try {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback to general settings if accessibility settings are not available
                                try {
                                    val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                                    fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(fallbackIntent)
                                } catch (fallbackException: Exception) {
                                    // Log error or show user-friendly message
                                    // In a real app, you might want to show a toast or snackbar
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("System Accessibility Settings")
                    }
                }
            }
        }
    }
}