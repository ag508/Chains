package com.chain.messaging.presentation.voice

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chain.messaging.core.audio.RecordingState
import kotlin.math.sin

/**
 * Widget for recording voice messages with waveform visualization
 */
@Composable
fun VoiceRecorderWidget(
    onVoiceMessageRecorded: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VoiceRecorderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val amplitude by viewModel.amplitude.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val waveformData by viewModel.waveformData.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Recording status and duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (recordingState) {
                        RecordingState.IDLE -> "Tap to record"
                        RecordingState.RECORDING -> "Recording..."
                        RecordingState.PAUSED -> "Paused"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                if (recordingState != RecordingState.IDLE) {
                    Text(
                        text = formatDuration(duration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Waveform visualization
            if (recordingState == RecordingState.RECORDING) {
                WaveformVisualization(
                    amplitude = amplitude,
                    waveformData = waveformData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Voice waveform will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel button
                if (recordingState != RecordingState.IDLE) {
                    IconButton(
                        onClick = {
                            viewModel.cancelRecording()
                            onCancel()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                // Record/Stop button
                val recordButtonScale by animateFloatAsState(
                    targetValue = if (recordingState == RecordingState.RECORDING) 1.2f else 1.0f,
                    animationSpec = tween(300),
                    label = "recordButtonScale"
                )
                
                IconButton(
                    onClick = {
                        when (recordingState) {
                            RecordingState.IDLE -> viewModel.startRecording()
                            RecordingState.RECORDING -> viewModel.stopRecording { filePath ->
                                onVoiceMessageRecorded(filePath)
                            }
                            RecordingState.PAUSED -> viewModel.startRecording()
                        }
                    },
                    modifier = Modifier
                        .size((48 * recordButtonScale).dp)
                        .background(
                            if (recordingState == RecordingState.RECORDING) 
                                MaterialTheme.colorScheme.error
                            else 
                                MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                ) {
                    Icon(
                        when (recordingState) {
                            RecordingState.IDLE -> Icons.Default.Mic
                            RecordingState.RECORDING -> Icons.Default.Stop
                            RecordingState.PAUSED -> Icons.Default.Mic
                        },
                        contentDescription = when (recordingState) {
                            RecordingState.IDLE -> "Start recording"
                            RecordingState.RECORDING -> "Stop recording"
                            RecordingState.PAUSED -> "Resume recording"
                        },
                        tint = Color.White
                    )
                }
                
                // Send button (only when recording is complete)
                if (recordingState != RecordingState.IDLE && duration > 1000) { // At least 1 second
                    IconButton(
                        onClick = {
                            viewModel.stopRecording { filePath ->
                                onVoiceMessageRecorded(filePath)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Waveform visualization component
 */
@Composable
private fun WaveformVisualization(
    amplitude: Int,
    waveformData: List<Float>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Canvas(modifier = modifier) {
        drawWaveform(
            waveformData = waveformData,
            amplitude = amplitude,
            color = primaryColor,
            drawScope = this
        )
    }
}

/**
 * Draw waveform on canvas
 */
private fun drawWaveform(
    waveformData: List<Float>,
    amplitude: Int,
    color: Color,
    drawScope: DrawScope
) {
    val width = drawScope.size.width
    val height = drawScope.size.height
    val centerY = height / 2
    
    if (waveformData.isEmpty()) {
        // Draw animated bars when no data
        val barCount = 20
        val barWidth = width / barCount
        
        for (i in 0 until barCount) {
            val x = i * barWidth + barWidth / 2
            val animatedHeight = (sin(System.currentTimeMillis() / 100.0 + i) * 0.5 + 0.5) * height * 0.3
            
            drawScope.drawLine(
                color = color,
                start = Offset(x, centerY - animatedHeight.toFloat()),
                end = Offset(x, centerY + animatedHeight.toFloat()),
                strokeWidth = barWidth * 0.6f
            )
        }
    } else {
        // Draw actual waveform data
        val barWidth = width / waveformData.size
        
        waveformData.forEachIndexed { index, value ->
            val x = index * barWidth + barWidth / 2
            val barHeight = value * height * 0.4f
            
            drawScope.drawLine(
                color = color,
                start = Offset(x, centerY - barHeight),
                end = Offset(x, centerY + barHeight),
                strokeWidth = barWidth * 0.8f
            )
        }
    }
}

/**
 * Format duration in MM:SS format
 */
private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}