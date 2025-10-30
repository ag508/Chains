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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import com.chain.messaging.core.audio.PlaybackState
import com.chain.messaging.domain.model.MediaMessage
import java.io.File

/**
 * Bubble component for displaying voice messages with playback controls
 */
@Composable
fun VoiceMessageBubble(
    mediaMessage: MediaMessage,
    isFromCurrentUser: Boolean,
    modifier: Modifier = Modifier,
    viewModel: VoicePlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val waveformData by viewModel.waveformData.collectAsStateWithLifecycle()
    
    val isCurrentlyPlaying = viewModel.isPlayingFile(mediaMessage.uri)
    
    LaunchedEffect(mediaMessage.uri) {
        viewModel.loadWaveformData(mediaMessage.uri)
    }
    
    Card(
        modifier = modifier
            .widthIn(min = 200.dp, max = 300.dp),
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
            bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isFromCurrentUser) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/Pause button
            IconButton(
                onClick = {
                    if (isCurrentlyPlaying) {
                        when (playbackState) {
                            PlaybackState.PLAYING -> viewModel.pause()
                            PlaybackState.PAUSED -> viewModel.resume()
                            else -> viewModel.play(File(mediaMessage.uri))
                        }
                    } else {
                        viewModel.play(File(mediaMessage.uri))
                    }
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isFromCurrentUser) 
                            Color.White.copy(alpha = 0.2f)
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isCurrentlyPlaying && playbackState == PlaybackState.PLAYING) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = if (isCurrentlyPlaying && playbackState == PlaybackState.PLAYING) {
                        "Pause"
                    } else {
                        "Play"
                    },
                    tint = if (isFromCurrentUser) 
                        Color.White 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Waveform and progress
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Waveform visualization
                VoiceWaveform(
                    waveformData = waveformData,
                    progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                    isPlaying = isCurrentlyPlaying && playbackState == PlaybackState.PLAYING,
                    color = if (isFromCurrentUser) Color.White else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clickable {
                            // Allow seeking by clicking on waveform
                            // This would require calculating the click position
                        }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Duration and progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(if (isCurrentlyPlaying) currentPosition else 0L),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFromCurrentUser) 
                            Color.White.copy(alpha = 0.8f)
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    
                    Text(
                        text = formatDuration(mediaMessage.duration ?: duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isFromCurrentUser) 
                            Color.White.copy(alpha = 0.8f)
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Waveform visualization for voice messages
 */
@Composable
private fun VoiceWaveform(
    waveformData: List<Float>,
    progress: Float,
    isPlaying: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(100),
        label = "waveformProgress"
    )
    
    Canvas(modifier = modifier) {
        drawVoiceWaveform(
            waveformData = waveformData,
            progress = animatedProgress,
            isPlaying = isPlaying,
            color = color,
            drawScope = this
        )
    }
}

/**
 * Draw voice waveform on canvas
 */
private fun drawVoiceWaveform(
    waveformData: List<Float>,
    progress: Float,
    isPlaying: Boolean,
    color: Color,
    drawScope: DrawScope
) {
    val width = drawScope.size.width
    val height = drawScope.size.height
    val centerY = height / 2
    
    if (waveformData.isEmpty()) {
        // Draw placeholder bars
        val barCount = 30
        val barWidth = width / barCount
        val barSpacing = barWidth * 0.2f
        val actualBarWidth = barWidth - barSpacing
        
        for (i in 0 until barCount) {
            val x = i * barWidth + barSpacing / 2
            val barHeight = (0.3f + (i % 3) * 0.2f) * height * 0.6f
            
            val barColor = if (i.toFloat() / barCount <= progress) {
                color
            } else {
                color.copy(alpha = 0.3f)
            }
            
            drawScope.drawLine(
                color = barColor,
                start = Offset(x + actualBarWidth / 2, centerY - barHeight / 2),
                end = Offset(x + actualBarWidth / 2, centerY + barHeight / 2),
                strokeWidth = actualBarWidth
            )
        }
    } else {
        // Draw actual waveform data
        val barCount = waveformData.size
        val barWidth = width / barCount
        val barSpacing = barWidth * 0.1f
        val actualBarWidth = barWidth - barSpacing
        
        waveformData.forEachIndexed { index, amplitude ->
            val x = index * barWidth + barSpacing / 2
            val barHeight = amplitude * height * 0.8f
            
            val barColor = if (index.toFloat() / barCount <= progress) {
                color
            } else {
                color.copy(alpha = 0.3f)
            }
            
            drawScope.drawLine(
                color = barColor,
                start = Offset(x + actualBarWidth / 2, centerY - barHeight / 2),
                end = Offset(x + actualBarWidth / 2, centerY + barHeight / 2),
                strokeWidth = actualBarWidth
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
    return String.format("%01d:%02d", minutes, seconds)
}