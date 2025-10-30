package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.repository.ChatRepository
import com.chain.messaging.domain.repository.MessageRepository
import javax.inject.Inject

/**
 * Use case for deleting chats.
 * Implements requirement 11.3 for chat management operations.
 */
class DeleteChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository
) {
    
    /**
     * Deletes a chat and all its associated messages.
     * 
     * @param chatId The ID of the chat to delete
     * @return Result indicating success or failure
     */
    suspend fun execute(chatId: String): Result<Unit> {
        return try {
            // First verify the chat exists
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("Chat not found"))
            
            // Delete all messages in the chat first
            val deleteMessagesResult = messageRepository.deleteMessagesByChat(chatId)
            if (deleteMessagesResult.isFailure) {
                return Result.failure(
                    Exception("Failed to delete chat messages: ${deleteMessagesResult.exceptionOrNull()?.message}")
                )
            }
            
            // Then delete the chat itself
            chatRepository.deleteChat(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}