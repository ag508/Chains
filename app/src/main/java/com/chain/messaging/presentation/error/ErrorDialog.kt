package com.chain.messaging.presentation.error

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.chain.messaging.core.error.*

/**
 * Dialog for displaying errors with recovery options
 */
@Composable
fun ErrorDialog(
    errorEvent: ErrorEvent,
    onDismiss: () -> Unit,
    onRecoveryAction: (RecoveryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error icon and title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = getErrorIcon(errorEvent.error),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Text(
                        text = getErrorTitle(errorEvent.error),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Error message
                Text(
                    text = errorEvent.userMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
                
                // Recovery actions
                if (errorEvent.recoveryActions.isNotEmpty()) {
                    Text(
                        text = "What would you like to do?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(errorEvent.recoveryActions) { action ->
                            OutlinedButton(
                                onClick = { onRecoveryAction(action) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = getActionIcon(action),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(action.label)
                            }
                        }
                    }
                }
                
                // Dismiss button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

/**
 * Inline error message component for non-blocking errors
 */
@Composable
fun InlineErrorMessage(
    errorEvent: ErrorEvent,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = getErrorTitle(errorEvent.error),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = errorEvent.userMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onRetry != null) {
                    IconButton(onClick = onRetry) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

/**
 * Snackbar for quick error notifications
 */
@Composable
fun ErrorSnackbar(
    errorEvent: ErrorEvent,
    onActionClick: (() -> Unit)? = null,
    actionLabel: String? = null,
    modifier: Modifier = Modifier
) {
    Snackbar(
        modifier = modifier,
        action = if (onActionClick != null && actionLabel != null) {
            {
                TextButton(onClick = onActionClick) {
                    Text(actionLabel)
                }
            }
        } else null,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        Text(
            text = errorEvent.userMessage,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Get appropriate icon for error type
 */
private fun getErrorIcon(error: ChainError): ImageVector {
    return when (error) {
        is ChainError.NetworkError -> Icons.Default.WifiOff
        is ChainError.BlockchainError -> Icons.Default.Link
        is ChainError.EncryptionError -> Icons.Default.Security
        is ChainError.StorageError -> Icons.Default.Storage
        is ChainError.AuthError -> Icons.Default.AccountCircle
        is ChainError.CallError -> Icons.Default.Call
        is ChainError.P2PError -> Icons.Default.DeviceHub
        is ChainError.UIError -> Icons.Default.Error
        is ChainError.SystemError -> Icons.Default.Warning
    }
}

/**
 * Get appropriate title for error type
 */
private fun getErrorTitle(error: ChainError): String {
    return when (error) {
        is ChainError.NetworkError -> "Network Error"
        is ChainError.BlockchainError -> "Blockchain Error"
        is ChainError.EncryptionError -> "Security Error"
        is ChainError.StorageError -> "Storage Error"
        is ChainError.AuthError -> "Authentication Error"
        is ChainError.CallError -> "Call Error"
        is ChainError.P2PError -> "Connection Error"
        is ChainError.UIError -> "Interface Error"
        is ChainError.SystemError -> "System Error"
    }
}

/**
 * Get appropriate icon for recovery action
 */
private fun getActionIcon(action: RecoveryAction): ImageVector {
    return when (action) {
        is RecoveryAction.Retry -> Icons.Default.Refresh
        is RecoveryAction.CheckNetworkSettings -> Icons.Default.Settings
        is RecoveryAction.UseOfflineMode -> Icons.Default.CloudOff
        is RecoveryAction.QueueForLater -> Icons.Default.Schedule
        is RecoveryAction.SwitchNode -> Icons.Default.SwapHoriz
        is RecoveryAction.VerifyContact -> Icons.Default.VerifiedUser
        is RecoveryAction.ResetSession -> Icons.Default.RestartAlt
        is RecoveryAction.CleanupStorage -> Icons.Default.CleaningServices
        is RecoveryAction.ChangeStorageProvider -> Icons.Default.CloudSync
        is RecoveryAction.ReAuthenticate -> Icons.Default.Login
        is RecoveryAction.CheckPermissions -> Icons.Default.Security
        is RecoveryAction.SwitchToAudio -> Icons.Default.VolumeUp
        is RecoveryAction.Custom -> Icons.Default.Build
    }
}