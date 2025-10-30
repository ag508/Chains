package com.chain.messaging.presentation.privacy

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chain.messaging.domain.model.Message
import java.util.Date
import kotlin.math.max

/**
 * Component that shows a countdown timer for disappearing messages.
 */
@Composable
fun DisappearingMessageIndicator(
    message: Message,
    modifier: Modifier = Modifier
) {
    if (!message.isDisappearing || message.expiresAt == null) return
    
    val currentTime by remember {
        mutableStateOf(System.currentTimeMillis())
    }
    
    // Update current time every second
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            // In a real implementation, you'd update the current time state
        }
    }
    
    val timeRemaining = max(0L, message.expiresAt.time - currentTime)
    val totalDuration = message.disappearingMessageTimer ?: 0L
    val progress = if (totalDuration > 0) {
        (timeRemaining.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    } else 0f
    
    if (timeRemaining > 0) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CircularProgressTimer(
                progress = progress,
                timeRemaining = timeRemaining,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = formatTimeRemaining(timeRemaining),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun CircularProgressTimer(
    progress: Float,
    timeRemaining: Long,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )
    
    val color = when {
        progress > 0.5f -> MaterialTheme.colorScheme.primary
        progress > 0.2f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        
        // Background circle
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = radius,
            style = Stroke(width = strokeWidth)
        )
        
        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

/**
 * Component that shows a warning when a message is about to expire.
 */
@Composable
fun ExpirationWarning(
    message: Message,
    modifier: Modifier = Modifier
) {
    if (!message.isDisappearing || message.expiresAt == null) return
    
    val timeRemaining = max(0L, message.expiresAt.time - System.currentTimeMillis())
    
    // Show warning when less than 1 minute remaining
    if (timeRemaining in 1..60000) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
            )
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = "Expiring soon",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(16.dp)
                )
                
                Text(
                    text = "This message will disappear in ${formatTimeRemaining(timeRemaining)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Component that shows a notification when a screenshot is detected.
 */
@Composable
fun ScreenshotDetectedNotification(
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
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📸",
                    fontSize = 16.sp
                )
                
                Text(
                    text = "Screenshot detected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Dismiss",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

private fun formatTimeRemaining(timeMs: Long): String {
    val seconds = timeMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "${days}d"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}