package com.chain.messaging.presentation.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.messaging.core.webrtc.CallStatus
import com.chain.messaging.core.security.NetworkQuality
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

/**
 * Call screen for voice and video calls
 * Implements requirements 6.3, 6.4 for call interface and controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    callId: String,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    LaunchedEffect(callId) {
        viewModel.loadCall(callId)
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        when {
            uiState.isLoading -> {
                CallLoadingScreen()
            }
            uiState.callSession != null -> {
                val callSession = uiState.callSession!!
                if (callSession.isVideo) {
                    VideoCallContent(
                        uiState = uiState,
                        onMuteToggle = viewModel::toggleMute,
                        onVideoToggle = viewModel::toggleVideo,
                        onSpeakerToggle = viewModel::toggleSpeaker,
                        onSwitchCamera = viewModel::switchCamera,
                        onEndCall = {
                            viewModel.endCall()
                            onEndCall()
                        }
                    )
                } else {
                    AudioCallContent(
                        uiState = uiState,
                        onMuteToggle = viewModel::toggleMute,
                        onSpeakerToggle = viewModel::toggleSpeaker,
                        onEndCall = {
                            viewModel.endCall()
                            onEndCall()
                        }
                    )
                }
            }
            uiState.error != null -> {
                val error = uiState.error!!
                CallErrorScreen(
                    error = error,
                    onRetry = { viewModel.loadCall(callId) },
                    onClose = onEndCall
                )
            }
        }
        
        // Call quality indicator
        uiState.callSession?.let { callSession ->
            uiState.networkQuality?.let { networkQuality ->
                CallQualityIndicator(
                    quality = networkQuality,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun VideoCallContent(
    uiState: CallUiState,
    onMuteToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Remote video (full screen)
        if (uiState.remoteVideoTrack != null) {
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        init(uiState.eglBaseContext, null)
                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                        setMirror(false)
                        uiState.remoteVideoTrack.addSink(this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Show placeholder when no remote video
            VideoPlaceholder(
                name = uiState.callSession?.peerId ?: "Unknown",
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Local video (picture-in-picture)
        if (uiState.localVideoTrack != null && uiState.isVideoEnabled) {
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        init(uiState.eglBaseContext, null)
                        setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
                        setMirror(true)
                        uiState.localVideoTrack.addSink(this)
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(120.dp, 160.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        
        // Call info overlay
        CallInfoOverlay(
            callerName = uiState.callSession?.peerId ?: "Unknown",
            callDuration = uiState.callDuration,
            callStatus = uiState.callSession?.status ?: CallStatus.INITIATING,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
        
        // Video call controls
        VideoCallControls(
            isMuted = uiState.isMuted,
            isVideoEnabled = uiState.isVideoEnabled,
            isSpeakerOn = uiState.isSpeakerOn,
            onMuteToggle = onMuteToggle,
            onVideoToggle = onVideoToggle,
            onSpeakerToggle = onSpeakerToggle,
            onSwitchCamera = onSwitchCamera,
            onEndCall = onEndCall,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
        )
    }
}

@Composable
private fun AudioCallContent(
    uiState: CallUiState,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onEndCall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        
        // Caller info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Caller avatar",
                    modifier = Modifier.size(60.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Text(
                text = uiState.callSession?.peerId ?: "Unknown",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = when (uiState.callSession?.status) {
                    CallStatus.INITIATING -> "Calling..."
                    CallStatus.RINGING -> "Ringing..."
                    CallStatus.CONNECTING -> "Connecting..."
                    CallStatus.CONNECTED -> uiState.callDuration
                    CallStatus.ENDED -> "Call ended"
                    CallStatus.FAILED -> "Call failed"
                    else -> "Unknown status"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Audio call controls
        AudioCallControls(
            isMuted = uiState.isMuted,
            isSpeakerOn = uiState.isSpeakerOn,
            onMuteToggle = onMuteToggle,
            onSpeakerToggle = onSpeakerToggle,
            onEndCall = onEndCall
        )
    }
}

@Composable
private fun VideoCallControls(
    isMuted: Boolean,
    isVideoEnabled: Boolean,
    isSpeakerOn: Boolean,
    onMuteToggle: () -> Unit,
    onVideoToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mute button
        CallControlButton(
            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
            isActive = !isMuted,
            onClick = onMuteToggle,
            contentDescription = if (isMuted) "Unmute" else "Mute"
        )
        
        // Video toggle button
        CallControlButton(
            icon = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
            isActive = isVideoEnabled,
            onClick = onVideoToggle,
            contentDescription = if (isVideoEnabled) "Turn off video" else "Turn on video"
        )
        
        // Speaker button
        CallControlButton(
            icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
            isActive = isSpeakerOn,
            onClick = onSpeakerToggle,
            contentDescription = if (isSpeakerOn) "Turn off speaker" else "Turn on speaker"
        )
        
        // Switch camera button
        CallControlButton(
            icon = Icons.Default.Cameraswitch,
            isActive = true,
            onClick = onSwitchCamera,
            contentDescription = "Switch camera"
        )
        
        // End call button
        CallControlButton(
            icon = Icons.Default.CallEnd,
            isActive = false,
            onClick = onEndCall,
            contentDescription = "End call",
            backgroundColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun AudioCallControls(
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    onMuteToggle: () -> Unit,
    onSpeakerToggle: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mute button
        CallControlButton(
            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
            isActive = !isMuted,
            onClick = onMuteToggle,
            contentDescription = if (isMuted) "Unmute" else "Mute",
            size = 64.dp
        )
        
        // End call button
        CallControlButton(
            icon = Icons.Default.CallEnd,
            isActive = false,
            onClick = onEndCall,
            contentDescription = "End call",
            backgroundColor = MaterialTheme.colorScheme.error,
            size = 64.dp
        )
        
        // Speaker button
        CallControlButton(
            icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
            isActive = isSpeakerOn,
            onClick = onSpeakerToggle,
            contentDescription = if (isSpeakerOn) "Turn off speaker" else "Turn on speaker",
            size = 64.dp
        )
    }
}

@Composable
private fun CallControlButton(
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
    size: androidx.compose.ui.unit.Dp = 56.dp
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(size),
        containerColor = backgroundColor,
        contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun CallInfoOverlay(
    callerName: String,
    callDuration: String,
    callStatus: CallStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = callerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = when (callStatus) {
                    CallStatus.CONNECTED -> callDuration
                    CallStatus.CONNECTING -> "Connecting..."
                    CallStatus.RINGING -> "Ringing..."
                    else -> callStatus.name.lowercase().replaceFirstChar { it.uppercase() }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CallQualityIndicator(
    quality: NetworkQuality,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when (quality) {
        NetworkQuality.EXCELLENT -> Triple(Icons.Default.SignalWifi4Bar, Color(0xFF4CAF50), "Excellent")
        NetworkQuality.GOOD -> Triple(Icons.Default.Wifi, Color(0xFFFFEB3B), "Good")
        NetworkQuality.POOR -> Triple(Icons.Default.SignalWifiOff, Color(0xFFFF9800), "Poor")
        NetworkQuality.BAD -> Triple(Icons.Default.SignalWifiConnectedNoInternet4, Color(0xFFF44336), "Bad")
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Network quality: $text",
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun VideoPlaceholder(
    name: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Caller avatar",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Camera is off",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CallLoadingScreen() {
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
                text = "Loading call...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CallErrorScreen(
    error: String,
    onRetry: () -> Unit,
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
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(onClick = onClose) {
                    Text("Close")
                }
                
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

