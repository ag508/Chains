package com.chain.messaging.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.messaging.domain.model.NotificationSettings
import com.chain.messaging.presentation.components.NotificationSoundPicker

/**
 * Notification settings screen for managing notification preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    var messageNotifications by remember { mutableStateOf(true) }
    var callNotifications by remember { mutableStateOf(true) }
    var groupNotifications by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var ledEnabled by remember { mutableStateOf(true) }
    var notificationSound by remember { mutableStateOf("default") }
    var quietHoursEnabled by remember { mutableStateOf(false) }
    var quietHoursStart by remember { mutableStateOf("22:00") }
    var quietHoursEnd by remember { mutableStateOf("08:00") }
    var showPreview by remember { mutableStateOf(true) }
    var showSenderName by remember { mutableStateOf(true) }
    var showSoundPicker by remember { mutableStateOf(false) }
    
    // Initialize form with current settings
    LaunchedEffect(uiState.settings) {
        uiState.settings?.notifications?.let { notifications ->
            messageNotifications = notifications.messageNotifications
            callNotifications = notifications.callNotifications
            groupNotifications = notifications.groupNotifications
            soundEnabled = notifications.soundEnabled
            vibrationEnabled = notifications.vibrationEnabled
            ledEnabled = notifications.ledEnabled
            notificationSound = notifications.notificationSound
            quietHoursEnabled = notifications.quietHoursEnabled
            quietHoursStart = notifications.quietHoursStart
            quietHoursEnd = notifications.quietHoursEnd
            showPreview = notifications.showPreview
            showSenderName = notifications.showSenderName
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Notifications") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        val updatedNotifications = NotificationSettings(
                            messageNotifications = messageNotifications,
                            callNotifications = callNotifications,
                            groupNotifications = groupNotifications,
                            soundEnabled = soundEnabled,
                            vibrationEnabled = vibrationEnabled,
                            ledEnabled = ledEnabled,
                            notificationSound = notificationSound,
                            quietHoursEnabled = quietHoursEnabled,
                            quietHoursStart = quietHoursStart,
                            quietHoursEnd = quietHoursEnd,
                            showPreview = showPreview,
                            showSenderName = showSenderName
                        )
                        viewModel.updateNotificationSettings(updatedNotifications)
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
            // Notification Types
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Notification Types",
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
                            Text("Message Notifications")
                            Text(
                                "Get notified for new messages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = messageNotifications,
                            onCheckedChange = { messageNotifications = it }
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
                            Text("Call Notifications")
                            Text(
                                "Get notified for incoming calls",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = callNotifications,
                            onCheckedChange = { callNotifications = it }
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
                            Text("Group Notifications")
                            Text(
                                "Get notified for group messages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = groupNotifications,
                            onCheckedChange = { groupNotifications = it }
                        )
                    }
                }
            }
            
            // Notification Style
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Notification Style",
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
                            Text("Sound")
                            Text(
                                "Play notification sound",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it }
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
                            Text("Vibration")
                            Text(
                                "Vibrate on notifications",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = vibrationEnabled,
                            onCheckedChange = { vibrationEnabled = it }
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
                            Text("LED Light")
                            Text(
                                "Flash LED for notifications",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = ledEnabled,
                            onCheckedChange = { ledEnabled = it }
                        )
                    }
                    
                    OutlinedButton(
                        onClick = { showSoundPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = soundEnabled
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Choose Notification Sound")
                            Text(
                                text = getSoundDisplayName(notificationSound),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Quiet Hours
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Quiet Hours",
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
                            Text("Enable Quiet Hours")
                            Text(
                                "Silence notifications during specified hours",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = quietHoursEnabled,
                            onCheckedChange = { quietHoursEnabled = it }
                        )
                    }
                    
                    if (quietHoursEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = quietHoursStart,
                                onValueChange = { quietHoursStart = it },
                                label = { Text("Start Time") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("22:00") }
                            )
                            
                            OutlinedTextField(
                                value = quietHoursEnd,
                                onValueChange = { quietHoursEnd = it },
                                label = { Text("End Time") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                placeholder = { Text("08:00") }
                            )
                        }
                    }
                }
            }
            
            // Privacy Options
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Privacy",
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
                            Text("Show Message Preview")
                            Text(
                                "Display message content in notifications",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showPreview,
                            onCheckedChange = { showPreview = it }
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
                            Text("Show Sender Name")
                            Text(
                                "Display sender name in notifications",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showSenderName,
                            onCheckedChange = { showSenderName = it }
                        )
                    }
                }
            }
        }
    }
    
    // Sound picker dialog
    if (showSoundPicker) {
        NotificationSoundPicker(
            currentSound = notificationSound,
            onSoundSelected = { selectedSound ->
                notificationSound = selectedSound
            },
            onDismiss = { showSoundPicker = false }
        )
    }
}

/**
 * Get display name for notification sound
 */
private fun getSoundDisplayName(soundId: String): String {
    return when (soundId) {
        "default" -> "Default"
        "silent" -> "Silent"
        else -> "Custom Sound"
    }
}