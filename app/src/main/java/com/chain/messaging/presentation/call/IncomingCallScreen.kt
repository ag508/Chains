package com.chain.messaging.presentation.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Incoming call screen with answer/decline UI
 * Implements requirement 6.3 for incoming call notification and answer/decline UI
 */
@Composable
fun IncomingCallScreen(
    callId: String,
    onCallAccepted: (String) -> Unit,
    onCallDeclined: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: IncomingCallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(callId) {
        viewModel.loadIncomingCall(callId)
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        when {
            uiState.isLoading -> {
                IncomingCallLoadingScreen()
            }
            uiState.pendingCall != null -> {
                IncomingCallContent(
                    uiState = uiState,
                    onAccept = {
                        viewModel.acceptCall()
                        onCallAccepted(callId)
                    },
                    onDecline = {
                        viewModel.declineCall()
                        onCallDeclined()
                    }
                )
            }
            uiState.error != null -> {
                val error = uiState.error!!
                IncomingCallErrorScreen(
                    error = error,
                    onClose = onCallDeclined
                )
            }
        }
    }
}

@Composable
private fun IncomingCallContent(
    uiState: IncomingCallUiState,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        // Incoming call info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Call type indicator
            Text(
                text = if (uiState.pendingCall?.isVideo == true) "Incoming video call" else "Incoming call",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // Caller avatar
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Caller avatar",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // Caller name
            Text(
                text = uiState.pendingCall?.peerId ?: "Unknown",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // Call status
            Text(
                text = "Ringing...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            // Video call indicator
            uiState.pendingCall?.let { pendingCall ->
                if (pendingCall.isVideo) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video call",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Video call",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        // Answer/Decline controls
        IncomingCallControls(
            onAccept = onAccept,
            onDecline = onDecline
        )
    }
}

@Composable
private fun IncomingCallControls(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Decline button
        FloatingActionButton(
            onClick = onDecline,
            modifier = Modifier.size(72.dp),
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "Decline call",
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Accept button
        FloatingActionButton(
            onClick = onAccept,
            modifier = Modifier.size(72.dp),
            containerColor = Color.Green,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Accept call",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun IncomingCallLoadingScreen() {
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
                text = "Loading incoming call...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun IncomingCallErrorScreen(
    error: String,
    onClose: () -> Unit
) {
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
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = "Call Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Button(onClick = onClose) {
                Text("Close")
            }
        }
    }
}