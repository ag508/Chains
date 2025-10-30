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
import com.chain.messaging.domain.model.*

/**
 * Privacy settings screen for controlling privacy and security settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    
    var readReceipts by remember { mutableStateOf(true) }
    var typingIndicators by remember { mutableStateOf(true) }
    var profilePhotoVisibility by remember { mutableStateOf(ProfileVisibility.EVERYONE) }
    var lastSeenVisibility by remember { mutableStateOf(ProfileVisibility.EVERYONE) }
    var onlineStatusVisibility by remember { mutableStateOf(ProfileVisibility.EVERYONE) }
    var groupInvitePermission by remember { mutableStateOf(GroupInvitePermission.EVERYONE) }
    var disappearingMessagesDefault by remember { mutableStateOf(DisappearingMessageTimer.OFF) }
    var screenshotNotifications by remember { mutableStateOf(true) }
    var forwardingRestriction by remember { mutableStateOf(false) }
    
    // Initialize form with current settings
    LaunchedEffect(uiState.settings) {
        uiState.settings?.privacy?.let { privacy ->
            readReceipts = privacy.readReceipts
            typingIndicators = privacy.typingIndicators
            profilePhotoVisibility = privacy.profilePhotoVisibility
            lastSeenVisibility = privacy.lastSeenVisibility
            onlineStatusVisibility = privacy.onlineStatusVisibility
            groupInvitePermission = privacy.groupInvitePermission
            disappearingMessagesDefault = privacy.disappearingMessagesDefault
            screenshotNotifications = privacy.screenshotNotifications
            forwardingRestriction = privacy.forwardingRestriction
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Privacy & Security") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        val updatedPrivacy = PrivacySettings(
                            readReceipts = readReceipts,
                            typingIndicators = typingIndicators,
                            profilePhotoVisibility = profilePhotoVisibility,
                            lastSeenVisibility = lastSeenVisibility,
                            onlineStatusVisibility = onlineStatusVisibility,
                            groupInvitePermission = groupInvitePermission,
                            disappearingMessagesDefault = disappearingMessagesDefault,
                            screenshotNotifications = screenshotNotifications,
                            forwardingRestriction = forwardingRestriction
                        )
                        viewModel.updatePrivacySettings(updatedPrivacy)
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
            // Message Privacy
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Message Privacy",
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
                            Text("Read Receipts")
                            Text(
                                "Let others know when you've read their messages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = readReceipts,
                            onCheckedChange = { readReceipts = it }
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
                            Text("Typing Indicators")
                            Text(
                                "Show when you're typing a message",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = typingIndicators,
                            onCheckedChange = { typingIndicators = it }
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
                            Text("Screenshot Notifications")
                            Text(
                                "Notify when someone takes a screenshot",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = screenshotNotifications,
                            onCheckedChange = { screenshotNotifications = it }
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
                            Text("Forwarding Restriction")
                            Text(
                                "Prevent others from forwarding your messages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = forwardingRestriction,
                            onCheckedChange = { forwardingRestriction = it }
                        )
                    }
                }
            }
            
            // Profile Visibility
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Profile Visibility",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    VisibilitySettingItem(
                        title = "Profile Photo",
                        description = "Who can see your profile photo",
                        selectedVisibility = profilePhotoVisibility,
                        onVisibilityChange = { profilePhotoVisibility = it }
                    )
                    
                    VisibilitySettingItem(
                        title = "Last Seen",
                        description = "Who can see when you were last online",
                        selectedVisibility = lastSeenVisibility,
                        onVisibilityChange = { lastSeenVisibility = it }
                    )
                    
                    VisibilitySettingItem(
                        title = "Online Status",
                        description = "Who can see when you're online",
                        selectedVisibility = onlineStatusVisibility,
                        onVisibilityChange = { onlineStatusVisibility = it }
                    )
                }
            }
            
            // Group Settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Group Settings",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Column {
                        Text(
                            text = "Group Invites",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Who can add you to groups",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        GroupInvitePermission.values().forEach { permission ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = groupInvitePermission == permission,
                                        onClick = { groupInvitePermission = permission },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = groupInvitePermission == permission,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (permission) {
                                        GroupInvitePermission.EVERYONE -> "Everyone"
                                        GroupInvitePermission.CONTACTS -> "My Contacts"
                                        GroupInvitePermission.NOBODY -> "Nobody"
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Disappearing Messages
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Disappearing Messages",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = "Default timer for new chats",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    OutlinedButton(
                        onClick = { /* TODO: Show timer picker */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (disappearingMessagesDefault) {
                                DisappearingMessageTimer.OFF -> "Off"
                                DisappearingMessageTimer.FIVE_SECONDS -> "5 seconds"
                                DisappearingMessageTimer.TEN_SECONDS -> "10 seconds"
                                DisappearingMessageTimer.THIRTY_SECONDS -> "30 seconds"
                                DisappearingMessageTimer.ONE_MINUTE -> "1 minute"
                                DisappearingMessageTimer.FIVE_MINUTES -> "5 minutes"
                                DisappearingMessageTimer.TEN_MINUTES -> "10 minutes"
                                DisappearingMessageTimer.THIRTY_MINUTES -> "30 minutes"
                                DisappearingMessageTimer.ONE_HOUR -> "1 hour"
                                DisappearingMessageTimer.SIX_HOURS -> "6 hours"
                                DisappearingMessageTimer.TWELVE_HOURS -> "12 hours"
                                DisappearingMessageTimer.ONE_DAY -> "1 day"
                                DisappearingMessageTimer.ONE_WEEK -> "1 week"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VisibilitySettingItem(
    title: String,
    description: String,
    selectedVisibility: ProfileVisibility,
    onVisibilityChange: (ProfileVisibility) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        ProfileVisibility.values().forEach { visibility ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedVisibility == visibility,
                        onClick = { onVisibilityChange(visibility) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedVisibility == visibility,
                    onClick = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (visibility) {
                        ProfileVisibility.EVERYONE -> "Everyone"
                        ProfileVisibility.CONTACTS -> "My Contacts"
                        ProfileVisibility.NOBODY -> "Nobody"
                    }
                )
            }
        }
    }
}