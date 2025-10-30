package com.chain.messaging.presentation.chat

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.chain.messaging.core.auth.AuthenticationService
import com.chain.messaging.core.media.MediaHandler
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.usecase.AddReactionUseCase
import com.chain.messaging.domain.usecase.GetMessagesUseCase
import com.chain.messaging.domain.usecase.SendMessageUseCase
import com.chain.messaging.presentation.base.BaseViewModel
import com.chain.messaging.presentation.base.UiState
import com.chain.messaging.presentation.media.MediaPickerType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for Chat screen
 * Handles message display, sending, reactions, and replies
 * Implements Requirements: 4.6, 4.7, 4.4
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val addReactionUseCase: AddReactionUseCase,
    private val mediaHandler: MediaHandler,
    private val authenticationService: AuthenticationService
) : BaseViewModel<ChatUiState>() {
    
    override val initialState = ChatUiState()
    
    fun loadMessages(chatId: String) {
        launchSafe {
            updateState { copy(isLoading = true, error = null) }
            
            // Set current user ID
            val userId = getCurrentUserId()
            updateState { copy(currentUserId = userId) }
            
            getMessagesUseCase(chatId).collect { messages ->
                updateState {
                    copy(
                        isLoading = false,
                        messages = messages,
                        error = null
                    )
                }
            }
        }
    }
    
    fun sendMessage(chatId: String, content: String) {
        val replyToMessageId = uiState.value.replyToMessage?.id
        
        launchSafe {
            sendMessageUseCase(
                chatId = chatId,
                content = content,
                replyToMessageId = replyToMessageId
            )
            
            // Clear reply after sending
            if (replyToMessageId != null) {
                clearReplyToMessage()
            }
        }
    }
    
    fun addReaction(messageId: String, emoji: String) {
        launchSafe {
            try {
                val currentUser = authenticationService.getCurrentUser()
                if (currentUser == null) {
                    updateState { copy(error = "No authenticated user found. Please log in again.") }
                    return@launchSafe
                }
                
                // Use the AddReactionUseCase to handle the reaction
                val result = addReactionUseCase(messageId, currentUser.userId, emoji)
                
                if (result.isFailure) {
                    updateState { copy(error = "Failed to add reaction: ${result.exceptionOrNull()?.message}") }
                }
                
                // The UI will be updated automatically through the observeMessages flow
                // which will include the updated reactions from the database
                
            } catch (e: Exception) {
                updateState { copy(error = "Failed to add reaction: ${e.message}") }
            }
        }
    }
    
    fun setReplyToMessage(message: Message) {
        updateState { copy(replyToMessage = message) }
    }
    
    fun clearReplyToMessage() {
        updateState { copy(replyToMessage = null) }
    }
    
    fun sendMediaMessage(chatId: String, uri: Uri, mediaPickerType: MediaPickerType) {
        val replyToMessageId = uiState.value.replyToMessage?.id
        
        launchSafe {
            updateState { copy(isLoading = true) }
            
            // Process the media file
            val mediaResult = mediaHandler.processMedia(uri)
            
            mediaResult.fold(
                onSuccess = { mediaMessage ->
                    // Convert MediaMessage to JSON string for message content
                    val mediaContent = """
                        {
                            "uri": "${mediaMessage.uri}",
                            "fileName": "${mediaMessage.fileName}",
                            "mimeType": "${mediaMessage.mimeType}",
                            "fileSize": ${mediaMessage.fileSize},
                            "duration": ${mediaMessage.duration},
                            "width": ${mediaMessage.width},
                            "height": ${mediaMessage.height},
                            "thumbnailUri": ${mediaMessage.thumbnailUri?.let { "\"$it\"" } ?: "null"},
                            "isLocal": ${mediaMessage.isLocal}
                        }
                    """.trimIndent()
                    
                    // Determine message type based on media picker type
                    val messageType = when (mediaPickerType) {
                        MediaPickerType.CAMERA_IMAGE, MediaPickerType.GALLERY_IMAGE -> MessageType.IMAGE
                        MediaPickerType.GALLERY_VIDEO -> MessageType.VIDEO
                        MediaPickerType.DOCUMENT -> MessageType.DOCUMENT
                    }
                    
                    // Send the media message
                    sendMessageUseCase(
                        chatId = chatId,
                        content = mediaContent,
                        messageType = messageType,
                        replyToMessageId = replyToMessageId
                    )
                    
                    // Clear reply after sending
                    if (replyToMessageId != null) {
                        clearReplyToMessage()
                    }
                    
                    updateState { copy(isLoading = false) }
                },
                onFailure = { error ->
                    updateState { 
                        copy(
                            isLoading = false,
                            error = "Failed to process media: ${error.message}"
                        )
                    }
                }
            )
        }
    }
    
    fun sendVoiceMessage(chatId: String, filePath: String) {
        val replyToMessageId = uiState.value.replyToMessage?.id
        
        launchSafe {
            updateState { copy(isLoading = true) }
            
            val voiceFile = File(filePath)
            if (!voiceFile.exists()) {
                updateState { 
                    copy(
                        isLoading = false,
                        error = "Voice file not found"
                    )
                }
                return@launchSafe
            }
            
            // Create MediaMessage for voice file
            val duration = extractAudioDuration(voiceFile)
            
            val mediaContent = """
                {
                    "uri": "${voiceFile.absolutePath}",
                    "fileName": "${voiceFile.name}",
                    "mimeType": "audio/mp4",
                    "fileSize": ${voiceFile.length()},
                    "duration": $duration,
                    "width": null,
                    "height": null,
                    "thumbnailUri": null,
                    "isLocal": true
                }
            """.trimIndent()
            
            // Send the voice message
            sendMessageUseCase(
                chatId = chatId,
                content = mediaContent,
                messageType = MessageType.AUDIO,
                replyToMessageId = replyToMessageId
            )
            
            // Clear reply after sending
            if (replyToMessageId != null) {
                clearReplyToMessage()
            }
            
            updateState { copy(isLoading = false) }
        }
    }
    
    private fun extractAudioDuration(file: File): Long {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()
            duration
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Get current authenticated user ID
     */
    suspend fun getCurrentUserId(): String? {
        return try {
            authenticationService.getCurrentUser()?.userId
        } catch (e: Exception) {
            updateState { copy(error = "Failed to get current user: ${e.message}") }
            null
        }
    }

    override fun handleError(error: Exception) {
        updateState {
            copy(
                isLoading = false,
                error = error.message ?: "Unknown error occurred"
            )
        }
    }
}

/**
 * UI State for Chat screen
 */
data class ChatUiState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val replyToMessage: Message? = null,
    val currentUserId: String? = null,
    val error: String? = null
) : UiState