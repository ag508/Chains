package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.repository.MessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving messages from a chat
 */
class GetMessagesUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    
    /**
     * Observes messages for a chat in real-time
     */
    operator fun invoke(chatId: String): Flow<List<Message>> {
        return messageRepository.observeMessages(chatId)
    }
    
    /**
     * Gets messages for a chat with pagination
     */
    suspend fun execute(chatId: String, limit: Int = 50, offset: Int = 0): List<Message> {
        return messageRepository.getMessages(chatId, limit, offset)
    }
}