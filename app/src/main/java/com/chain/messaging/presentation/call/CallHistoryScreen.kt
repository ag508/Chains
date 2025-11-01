package com.chain.messaging.presentation.call

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.messaging.domain.model.CallNotification
import com.chain.messaging.domain.model.CallNotificationType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Call history screen showing call logs and missed calls
 * Implements requirement 6.3 for call quality indicators and network status
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryScreen(
    onCallUser: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CallHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.loadCallHistory()
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top app bar
        TopAppBar(
            title = {
                Text(
                    text = "Call History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            },
            actions = {
                IconButton(onClick = { viewModel.clearCallHistory() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear history"
                    )
                }
            }
        )
        
        when {
            uiState.isLoading -> {
                CallHistoryLoadingScreen()
            }
            uiState.callHistory.isEmpty() -> {
                CallHistoryEmptyScreen()
            }
            else -> {
                CallHistoryContent(
                    callHistory = uiState.callHistory,
                    onCallUser = onCallUser,
                    onDeleteCall = viewModel::deleteCallFromHistory
                )
            }
        }
    }
}

@Composable
private fun CallHistoryContent(
    callHistory: List<CallNotification>,
    onCallUser: (String, Boolean) -> Unit,
    onDeleteCall: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(
            items = callHistory,
            key = { it.callId }
        ) { call ->
            CallHistoryItem(
                call = call,
                onCallUser = onCallUser,
                onDeleteCall = onDeleteCall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallHistoryItem(
    call: CallNotification,
    onCallUser: (String, Boolean) -> Unit,
    onDeleteCall: (String) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = { onCallUser(call.callerName, call.isVideo) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Call type and direction icon
            CallTypeIcon(
                type = call.type,
                isVideo = call.isVideo
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Call details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = call.callerName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = getCallTypeText(call.type, call.isVideo),
                        style = MaterialTheme.typography.bodySmall,
                        color = getCallTypeColor(call.type)
                    )
                    
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = dateFormat.format(Date(call.timestamp)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Call back button
                IconButton(
                    onClick = { onCallUser(call.callerName, call.isVideo) }
                ) {
                    Icon(
                        imageVector = if (call.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = "Call back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Delete button
                IconButton(
                    onClick = { onDeleteCall(call.callId) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CallTypeIcon(
    type: CallNotificationType,
    isVideo: Boolean
) {
    val (icon, color) = when (type) {
        CallNotificationType.INCOMING -> {
            if (isVideo) Icons.Default.Videocam to Color.Green
            else Icons.Default.CallReceived to Color.Green
        }
        CallNotificationType.OUTGOING -> {
            if (isVideo) Icons.Default.Videocam to MaterialTheme.colorScheme.primary
            else Icons.Default.CallMade to MaterialTheme.colorScheme.primary
        }
        CallNotificationType.ONGOING -> {
            if (isVideo) Icons.Default.Videocam to MaterialTheme.colorScheme.primary
            else Icons.Default.Call to MaterialTheme.colorScheme.primary
        }
        CallNotificationType.MISSED -> {
            if (isVideo) Icons.Default.VideocamOff to Color.Red
            else Icons.Default.CallMissed to Color.Red
        }
        CallNotificationType.REJECTED -> {
            if (isVideo) Icons.Default.VideocamOff to Color.Red
            else Icons.Default.CallEnd to Color.Red
        }
        CallNotificationType.ENDED -> {
            if (isVideo) Icons.Default.Videocam to MaterialTheme.colorScheme.onSurfaceVariant
            else Icons.Default.Call to MaterialTheme.colorScheme.onSurfaceVariant
        }
    }
    
    Icon(
        imageVector = icon,
        contentDescription = "Call type",
        tint = color,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun getCallTypeText(type: CallNotificationType, isVideo: Boolean): String {
    return when (type) {
        CallNotificationType.INCOMING -> if (isVideo) "Incoming video call" else "Incoming call"
        CallNotificationType.OUTGOING -> if (isVideo) "Outgoing video call" else "Outgoing call"
        CallNotificationType.ONGOING -> if (isVideo) "Video call" else "Voice call"
        CallNotificationType.MISSED -> if (isVideo) "Missed video call" else "Missed call"
        CallNotificationType.REJECTED -> if (isVideo) "Rejected video call" else "Rejected call"
        CallNotificationType.ENDED -> if (isVideo) "Video call ended" else "Call ended"
    }
}

@Composable
private fun getCallTypeColor(type: CallNotificationType): Color {
    return when (type) {
        CallNotificationType.INCOMING -> Color.Green
        CallNotificationType.OUTGOING -> MaterialTheme.colorScheme.primary
        CallNotificationType.ONGOING -> MaterialTheme.colorScheme.primary
        CallNotificationType.MISSED -> Color.Red
        CallNotificationType.REJECTED -> Color.Red
        CallNotificationType.ENDED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun CallHistoryLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Loading call history...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CallHistoryEmptyScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "No calls",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Text(
                text = "No call history",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Your call history will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}