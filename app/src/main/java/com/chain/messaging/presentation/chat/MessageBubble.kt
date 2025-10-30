package com.chain.messaging.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.model.Reaction
import com.chain.messaging.domain.model.getMediaContent
import com.chain.messaging.presentation.media.MediaPreview
import com.chain.messaging.presentation.voice.VoiceMessageBubble
import java.text.SimpleDateFormat
import java.util.*

/**
 * Message Bubble component with reactions and reply functionality
 * Implements Requirements: 4.6, 4.7, 4.4
 */
@Composable
fun MessageBubble(
    message: Message,
    isOwnMessage: Boolean,
    onReactionClick: (String) -> Unit,
    onReplyClick: () -> Unit,
    onMediaClick: (Message) -> Unit = {}
) {
    var showReactionPicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start
    ) {
        // Reply indicator if this message is a reply
        message.replyTo?.let {
            ReplyIndicator(
                isOwnMessage = isOwnMessage,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        // Main message bubble
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clickable { showReactionPicker = !showReactionPicker },
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isOwnMessage) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Message content
                MessageContent(
                    message = message,
                    isOwnMessage = isOwnMessage,
                    onMediaClick = { onMediaClick(message) }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Message metadata (time and status)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOwnMessage) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        },
                        fontSize = 11.sp
                    )
                    
                    if (isOwnMessage) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIcon(
                            status = message.status,
                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        // Reactions
        if (message.reactions.isNotEmpty()) {
            MessageReactions(
                reactions = message.reactions,
                onReactionClick = onReactionClick,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        // Reaction picker
        if (showReactionPicker) {
            ReactionPicker(
                onReactionSelected = { emoji ->
                    onReactionClick(emoji)
                    showReactionPicker = false
                },
                onReplyClick = {
                    onReplyClick()
                    showReactionPicker = false
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun MessageContent(
    message: Message,
    isOwnMessage: Boolean,
    onMediaClick: () -> Unit
) {
    when (message.type) {
        MessageType.TEXT -> {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOwnMessage) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        
        MessageType.AUDIO -> {
            // Handle voice messages specially
            val mediaMessage = message.getMediaContent()
            
            if (mediaMessage != null) {
                VoiceMessageBubble(
                    mediaMessage = mediaMessage,
                    isFromCurrentUser = isOwnMessage
                )
            } else {
                // Fallback to simple display
                MediaPlaceholder(
                    messageType = message.type,
                    content = message.content,
                    isOwnMessage = isOwnMessage,
                    onClick = onMediaClick
                )
            }
        }
        
        MessageType.IMAGE, MessageType.VIDEO, MessageType.DOCUMENT -> {
            // Try to get media content, fallback to placeholder if not available
            val mediaMessage = message.getMediaContent()
            
            if (mediaMessage != null) {
                MediaPreview(
                    mediaMessage = mediaMessage,
                    onClick = onMediaClick
                )
            } else {
                // Fallback to simple display
                MediaPlaceholder(
                    messageType = message.type,
                    content = message.content,
                    isOwnMessage = isOwnMessage,
                    onClick = onMediaClick
                )
            }
        }
        
        else -> {
            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOwnMessage) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun MediaPlaceholder(
    messageType: MessageType,
    content: String,
    isOwnMessage: Boolean,
    onClick: () -> Unit
) {
    val (emoji, label) = when (messageType) {
        MessageType.IMAGE -> "📷" to "Image"
        MessageType.VIDEO -> "🎥" to "Video"
        MessageType.AUDIO -> "🎵" to "Voice message"
        MessageType.DOCUMENT -> "📄" to "Document"
        else -> "📎" to "Media"
    }
    
    Column(
        modifier = Modifier.clickable { onClick() }
    ) {
        if (messageType in listOf(MessageType.IMAGE, MessageType.VIDEO)) {
            Box(
                modifier = Modifier
                    .size(200.dp, 150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$emoji $label",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = content.ifEmpty { label },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOwnMessage) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        
        if (content.isNotEmpty() && messageType in listOf(MessageType.IMAGE, MessageType.VIDEO)) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOwnMessage) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun MessageStatusIcon(
    status: MessageStatus,
    tint: Color
) {
    when (status) {
        MessageStatus.SENDING -> {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Sending",
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        MessageStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Delivered",
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
        }
        MessageStatus.READ -> {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Read",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp)
            )
        }
        MessageStatus.FAILED -> {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Failed",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MessageReactions(
    reactions: List<Reaction>,
    onReactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Group reactions by emoji and count them
    val groupedReactions = reactions.groupBy { it.emoji }
        .mapValues { it.value.size }
    
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(groupedReactions.toList()) { (emoji, count) ->
            ReactionChip(
                emoji = emoji,
                count = count,
                onClick = { onReactionClick(emoji) }
            )
        }
    }
}

@Composable
private fun ReactionChip(
    emoji: String,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 14.sp
            )
            if (count > 1) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ReactionPicker(
    onReactionSelected: (String) -> Unit,
    onReplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val commonEmojis = listOf("👍", "❤️", "😂", "😮", "😢", "😡")
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reply button
            IconButton(
                onClick = onReplyClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    )
            ) {
                Text(
                    text = "↩️",
                    fontSize = 16.sp
                )
            }
            
            // Reaction emojis
            commonEmojis.forEach { emoji ->
                IconButton(
                    onClick = { onReactionSelected(emoji) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Text(
                        text = emoji,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplyIndicator(
    isOwnMessage: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(2.dp)
                )
        )
        Text(
            text = "Replying to message",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}