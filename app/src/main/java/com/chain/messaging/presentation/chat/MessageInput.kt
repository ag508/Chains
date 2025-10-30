package com.chain.messaging.presentation.chat

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chain.messaging.domain.model.Message
import com.chain.messaging.presentation.media.MediaPicker
import com.chain.messaging.presentation.media.MediaPickerType
import com.chain.messaging.presentation.voice.VoiceRecorderWidget

/**
 * Message Input component with reply functionality
 * Implements Requirements: 4.6, 4.7, 4.4
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(
    replyToMessage: Message?,
    onSendMessage: (String) -> Unit,
    onSendMedia: (Uri, MediaPickerType) -> Unit,
    onSendVoiceMessage: (String) -> Unit,
    onClearReply: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var showMediaPicker by remember { mutableStateOf(false) }
    var showVoiceRecorder by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Reply preview
        replyToMessage?.let { message ->
            ReplyPreview(
                message = message,
                onClearReply = onClearReply
            )
        }
        
        // Message input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Attachment button
            IconButton(
                onClick = { showMediaPicker = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach media",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Text input field
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = if (replyToMessage != null) "Reply..." else "Type a message...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                maxLines = 4
            )
            
            // Send/Voice button
            FloatingActionButton(
                onClick = {
                    if (messageText.trim().isNotEmpty()) {
                        onSendMessage(messageText.trim())
                        messageText = ""
                    } else {
                        showVoiceRecorder = true
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = if (messageText.trim().isNotEmpty()) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
                contentColor = if (messageText.trim().isNotEmpty()) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSecondary
                }
            ) {
                Icon(
                    imageVector = if (messageText.trim().isNotEmpty()) {
                        Icons.Default.Send
                    } else {
                        Icons.Default.Mic
                    },
                    contentDescription = if (messageText.trim().isNotEmpty()) {
                        "Send message"
                    } else {
                        "Record voice message"
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Media picker
        if (showMediaPicker) {
            MediaPicker(
                onMediaSelected = { uri, type ->
                    onSendMedia(uri, type)
                    showMediaPicker = false
                },
                onDismiss = { showMediaPicker = false }
            )
        }
        
        // Voice recorder
        if (showVoiceRecorder) {
            VoiceRecorderWidget(
                onVoiceMessageRecorded = { filePath ->
                    onSendVoiceMessage(filePath)
                    showVoiceRecorder = false
                },
                onCancel = {
                    showVoiceRecorder = false
                }
            )
        }
    }
}

@Composable
private fun ReplyPreview(
    message: Message,
    onClearReply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reply indicator line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(2.dp)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Reply content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Replying to",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = getReplyPreviewText(message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Close button
            IconButton(
                onClick = onClearReply,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear reply",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun getReplyPreviewText(message: Message): String {
    return when (message.type) {
        com.chain.messaging.domain.model.MessageType.TEXT -> message.content
        com.chain.messaging.domain.model.MessageType.IMAGE -> "📷 Image"
        com.chain.messaging.domain.model.MessageType.VIDEO -> "🎥 Video"
        com.chain.messaging.domain.model.MessageType.AUDIO -> "🎵 Voice message"
        com.chain.messaging.domain.model.MessageType.DOCUMENT -> "📄 Document"
        com.chain.messaging.domain.model.MessageType.LOCATION -> "📍 Location"
        com.chain.messaging.domain.model.MessageType.CONTACT -> "👤 Contact"
        com.chain.messaging.domain.model.MessageType.POLL -> "📊 Poll"
        com.chain.messaging.domain.model.MessageType.SYSTEM -> message.content
    }
}