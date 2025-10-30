package com.chain.messaging.presentation.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.chain.messaging.core.sync.SyncPhase
import com.chain.messaging.core.sync.SyncProgress

/**
 * Dialog showing synchronization progress
 */
@Composable
fun SyncProgressDialog(
    syncProgress: SyncProgress,
    onDismiss: () -> Unit,
    isDismissible: Boolean = false
) {
    Dialog(
        onDismissRequest = if (isDismissible) onDismiss else { {} },
        properties = DialogProperties(
            dismissOnBackPress = isDismissible,
            dismissOnClickOutside = isDismissible
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Synchronizing",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress indicator
                LinearProgressIndicator(
                    progress = syncProgress.progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Progress percentage
                Text(
                    text = "${(syncProgress.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Current phase
                Text(
                    text = getSyncPhaseDisplayName(syncProgress.phase),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Current operation
                Text(
                    text = syncProgress.currentOperation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                // Estimated time remaining
                syncProgress.estimatedTimeRemaining?.let { timeRemaining ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Estimated time: ${formatTimeRemaining(timeRemaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Show dismiss button only if sync is completed or failed
                if (isDismissible && (syncProgress.phase == SyncPhase.COMPLETED || syncProgress.phase == SyncPhase.ERROR)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

/**
 * Compact sync progress indicator for status bars
 */
@Composable
fun SyncProgressIndicator(
    syncProgress: SyncProgress,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (syncProgress.phase != SyncPhase.COMPLETED && syncProgress.phase != SyncPhase.ERROR) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = when (syncProgress.phase) {
                SyncPhase.COMPLETED -> "Synced"
                SyncPhase.ERROR -> "Sync failed"
                else -> "Syncing..."
            },
            style = MaterialTheme.typography.bodySmall,
            color = when (syncProgress.phase) {
                SyncPhase.ERROR -> MaterialTheme.colorScheme.error
                SyncPhase.COMPLETED -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun getSyncPhaseDisplayName(phase: SyncPhase): String {
    return when (phase) {
        SyncPhase.INITIALIZING -> "Initializing"
        SyncPhase.DISCOVERING_DEVICES -> "Discovering devices"
        SyncPhase.SYNCING_KEYS -> "Syncing encryption keys"
        SyncPhase.SYNCING_MESSAGES -> "Syncing messages"
        SyncPhase.SYNCING_SETTINGS -> "Syncing settings"
        SyncPhase.FINALIZING -> "Finalizing"
        SyncPhase.COMPLETED -> "Completed"
        SyncPhase.ERROR -> "Error occurred"
    }
}

private fun formatTimeRemaining(milliseconds: Long): String {
    val seconds = milliseconds / 1000
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}