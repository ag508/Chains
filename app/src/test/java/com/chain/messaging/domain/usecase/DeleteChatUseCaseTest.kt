package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.model.ChatSettings
import com.chain.messaging.domain.model.ChatType
import com.chain.messaging.domain.repository.ChatRepository
import com.chain.messaging.domain.repository.MessageRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

/**
 * Unit tests for DeleteChatUseCase
 */
class DeleteChatUseCaseTest {
    
    private lateinit var chatRepository: ChatRepository
    private lateinit var messageRepository: MessageRepository
    private lateinit var deleteChatUseCase: DeleteChatUseCase
    
    @Before
    fun setUp() {
        chatRepository = mockk()
        messageRepository = mockk()
        deleteChatUseCase = DeleteChatUseCase(chatRepository, messageRepository)
    }
    
    @Test
    fun `execute should delete chat and messages successfully`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val chat = Chat(
            id = chatId,
            type = ChatType.DIRECT,
            name = "Test Chat",
            participants = listOf("user1", "user2"),
            admins = emptyList(),
            settings = ChatSettings(),
            lastMessage = null,
            unreadCount = 0,
            createdAt = Date(),
            updatedAt = Date()
        )
        
        coEvery { chatRepository.getChatById(chatId) } returns chat
        coEvery { messageRepository.deleteMessagesByChat(chatId) } returns Result.success(Unit)
        coEvery { chatRepository.deleteChat(chatId) } returns Result.success(Unit)
        
        // When
        val result = deleteChatUseCase.execute(chatId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { chatRepository.getChatById(chatId) }
        coVerify { messageRepository.deleteMessagesByChat(chatId) }
        coVerify { chatRepository.deleteChat(chatId) }
    }
    
    @Test
    fun `execute should fail when chat not found`() = runTest {
        // Given
        val chatId = "non-existent-chat-id"
        
        coEvery { chatRepository.getChatById(chatId) } returns null
        
        // When
        val result = deleteChatUseCase.execute(chatId)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Chat not found", result.exceptionOrNull()?.message)
        coVerify { chatRepository.getChatById(chatId) }
        coVerify(exactly = 0) { messageRepository.deleteMessagesByChat(any()) }
        coVerify(exactly = 0) { chatRepository.deleteChat(any()) }
    }
    
    @Test
    fun `execute should fail when message deletion fails`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val chat = Chat(
            id = chatId,
            type = ChatType.DIRECT,
            name = "Test Chat",
            participants = listOf("user1", "user2"),
            admins = emptyList(),
            settings = ChatSettings(),
            lastMessage = null,
            unreadCount = 0,
            createdAt = Date(),
            updatedAt = Date()
        )
        val messageError = Exception("Failed to delete messages")
        
        coEvery { chatRepository.getChatById(chatId) } returns chat
        coEvery { messageRepository.deleteMessagesByChat(chatId) } returns Result.failure(messageError)
        
        // When
        val result = deleteChatUseCase.execute(chatId)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to delete chat messages") == true)
        coVerify { chatRepository.getChatById(chatId) }
        coVerify { messageRepository.deleteMessagesByChat(chatId) }
        coVerify(exactly = 0) { chatRepository.deleteChat(any()) }
    }
    
    @Test
    fun `execute should fail when chat deletion fails`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val chat = Chat(
            id = chatId,
            type = ChatType.DIRECT,
            name = "Test Chat",
            participants = listOf("user1", "user2"),
            admins = emptyList(),
            settings = ChatSettings(),
            lastMessage = null,
            unreadCount = 0,
            createdAt = Date(),
            updatedAt = Date()
        )
        val chatError = Exception("Failed to delete chat")
        
        coEvery { chatRepository.getChatById(chatId) } returns chat
        coEvery { messageRepository.deleteMessagesByChat(chatId) } returns Result.success(Unit)
        coEvery { chatRepository.deleteChat(chatId) } returns Result.failure(chatError)
        
        // When
        val result = deleteChatUseCase.execute(chatId)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Failed to delete chat", result.exceptionOrNull()?.message)
        coVerify { chatRepository.getChatById(chatId) }
        coVerify { messageRepository.deleteMessagesByChat(chatId) }
        coVerify { chatRepository.deleteChat(chatId) }
    }
    
    @Test
    fun `execute should handle unexpected exceptions`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val unexpectedException = RuntimeException("Unexpected error")
        
        coEvery { chatRepository.getChatById(chatId) } throws unexpectedException
        
        // When
        val result = deleteChatUseCase.execute(chatId)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Unexpected error", result.exceptionOrNull()?.message)
        coVerify { chatRepository.getChatById(chatId) }
    }
}