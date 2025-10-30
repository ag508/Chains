package com.chain.messaging.presentation.chat

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.presentation.media.MediaPickerType
import com.chain.messaging.presentation.media.MediaViewer
import kotlinx.coroutines.launch

/**
 * Chat Screen showing message conversation with bubble UI
 * Implements Requirements: 4.6, 4.7, 4.4
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    chatName: String,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showMediaViewer by remember { mutableStateOf(false) }
    var selectedMediaMessage by remember { mutableStateOf<Message?>(null) }
    
    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId)
    }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = chatName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        // Messages List
        Box(
            modifier = Modifier.weight(1f)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Error: ${uiState.error}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Button(onClick = { viewModel.loadMessages(chatId) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                uiState.messages.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No messages yet. Start the conversation!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.messages) { message ->
                            MessageBubble(
                                message = message,
                                isOwnMessage = message.senderId == (uiState.currentUserId ?: "unknown_user"),
                                onReactionClick = { emoji ->
                                    viewModel.addReaction(message.id, emoji)
                                },
                                onReplyClick = {
                                    viewModel.setReplyToMessage(message)
                                },
                                onMediaClick = { mediaMessage ->
                                    selectedMediaMessage = mediaMessage
                                    showMediaViewer = true
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Message Input
        MessageInput(
            replyToMessage = uiState.replyToMessage,
            onSendMessage = { content ->
                viewModel.sendMessage(chatId, content)
            },
            onSendMedia = { uri, type ->
                viewModel.sendMediaMessage(chatId, uri, type)
            },
            onSendVoiceMessage = { filePath ->
                viewModel.sendVoiceMessage(chatId, filePath)
            },
            onClearReply = {
                viewModel.clearReplyToMessage()
            }
        )
    }
    
    // Media Viewer
    if (showMediaViewer && selectedMediaMessage != null) {
        val mediaMessage = selectedMediaMessage!!.getMediaContent()
        if (mediaMessage != null) {
            MediaViewer(
                mediaMessages = listOf(mediaMessage),
                initialIndex = 0,
                onBackClick = {
                    showMediaViewer = false
                    selectedMediaMessage = null
                },
                onShareClick = { media ->
                    // Handle sharing - could be implemented later
                }
            )
        }
    }
}