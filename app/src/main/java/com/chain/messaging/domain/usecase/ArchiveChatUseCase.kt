package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.repository.ChatRepository
import javax.inject.Inject

/**
 * Use case for archiving/unarchiving chats.
 * Implements requirement 11.3 for chat management operations.
 */
class ArchiveChatUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    
    /**
     * Archives or unarchives a chat by updating its settings.
     * 
     * @param chatId The ID of the chat to archive/unarchive
     * @param isArchived True to archive, false to unarchive
     * @return Result indicating success or failure
     */
    suspend fun execute(chatId: String, isArchived: Boolean): Result<Unit> {
        return try {
            val chat = chatRepository.getChatById(chatId)
                ?: return Result.failure(Exception("Chat not found"))
            
            val updatedSettings = chat.settings.copy(isArchived = isArchived)
            chatRepository.updateChatSettings(chatId, updatedSettings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}