package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * Use case for pinning/unpinning chats.
 * Implements requirement 11.3 for chat management operations.
 */
class PinChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    
    /**
     * Pins or unpins a chat by updating its settings.
     * 
     * @param chatId The ID of the chat to pin/unpin
     * @param isPinned True to pin, false to unpin
     * @return Result indicating success or failure
     */
    suspend fun execute(chatId: String, isPinned: Boolean): Result<Unit> {
        return try {
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("Chat not found"))
            
            val updatedSettings = chat.settings.copy(isPinned = isPinned)
            chatRepository.updateChatSettings(chatId, updatedSettings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}