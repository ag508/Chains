package com.chain.messaging.presentation.privacy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Screen for configuring disappearing message settings for a chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisappearingMessageSettingsScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    viewModel: DisappearingMessageSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(chatId) {
        viewModel.loadSettings(chatId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Disappearing Messages") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Auto-delete messages",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Messages will be automatically deleted from all devices after the selected time period.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item {
                TimerOption(
                    label = "Off",
                    isSelected = uiState.currentTimer == null,
                    onClick = { viewModel.setTimer(chatId, null) }
                )
            }
            
            items(uiState.availableTimers) { timer ->
                TimerOption(
                    label = formatTimerDuration(timer),
                    isSelected = uiState.currentTimer == timer,
                    onClick = { viewModel.setTimer(chatId, timer) }
                )
            }
            
            if (uiState.currentTimer != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "⚠️ Important",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Messages will be deleted from all devices\n" +
                                      "• Screenshots may be detected and reported\n" +
                                      "• This setting applies to new messages only",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

private fun formatTimerDuration(durationMs: Long): String {
    return when (durationMs) {
        5_000L -> "5 seconds"
        10_000L -> "10 seconds"
        30_000L -> "30 seconds"
        60_000L -> "1 minute"
        300_000L -> "5 minutes"
        600_000L -> "10 minutes"
        1_800_000L -> "30 minutes"
        3_600_000L -> "1 hour"
        21_600_000L -> "6 hours"
        43_200_000L -> "12 hours"
        86_400_000L -> "1 day"
        604_800_000L -> "1 week"
        else -> "${durationMs / 1000} seconds"
    }
}