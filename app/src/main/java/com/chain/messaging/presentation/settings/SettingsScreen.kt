package com.chain.messaging.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Main settings screen with navigation to different settings categories
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Load settings for current authenticated user
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Profile Section
                item {
                    uiState.settings?.let { settings ->
                        ProfileCard(
                            profile = settings.profile,
                            onClick = onNavigateToProfile
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
                
                // Settings Categories
                items(settingsCategories) { category ->
                    SettingsCategoryItem(
                        category = category,
                        onClick = {
                            when (category.type) {
                                SettingsCategoryType.PROFILE -> onNavigateToProfile()
                                SettingsCategoryType.PRIVACY -> onNavigateToPrivacy()
                                SettingsCategoryType.NOTIFICATIONS -> onNavigateToNotifications()
                                SettingsCategoryType.APPEARANCE -> onNavigateToAppearance()
                                SettingsCategoryType.ACCESSIBILITY -> onNavigateToAccessibility()
                            }
                        }
                    )
                }
            }
        }
        
        // Error handling
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show snackbar or error dialog
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: com.chain.messaging.domain.model.UserProfile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                profile.bio?.let { bio ->
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate"
            )
        }
    }
}

@Composable
private fun SettingsCategoryItem(
    category: SettingsCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                category.icon,
                contentDescription = category.title,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Navigate"
            )
        }
    }
}

/**
 * Settings category data class
 */
data class SettingsCategory(
    val type: SettingsCategoryType,
    val title: String,
    val description: String,
    val icon: ImageVector
)

/**
 * Settings category types
 */
enum class SettingsCategoryType {
    PROFILE,
    PRIVACY,
    NOTIFICATIONS,
    APPEARANCE,
    ACCESSIBILITY
}

/**
 * List of settings categories
 */
private val settingsCategories = listOf(
    SettingsCategory(
        type = SettingsCategoryType.PROFILE,
        title = "Profile",
        description = "Manage your profile information",
        icon = Icons.Default.Person
    ),
    SettingsCategory(
        type = SettingsCategoryType.PRIVACY,
        title = "Privacy & Security",
        description = "Control your privacy and security settings",
        icon = Icons.Default.Security
    ),
    SettingsCategory(
        type = SettingsCategoryType.NOTIFICATIONS,
        title = "Notifications",
        description = "Manage notification preferences",
        icon = Icons.Default.Notifications
    ),
    SettingsCategory(
        type = SettingsCategoryType.APPEARANCE,
        title = "Appearance",
        description = "Customize theme and display options",
        icon = Icons.Default.Palette
    ),
    SettingsCategory(
        type = SettingsCategoryType.ACCESSIBILITY,
        title = "Accessibility",
        description = "Accessibility and ease of use options",
        icon = Icons.Default.Accessibility
    )
)