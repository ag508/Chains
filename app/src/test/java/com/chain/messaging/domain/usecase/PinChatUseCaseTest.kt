package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.model.Chat
import com.chain.messaging.domain.model.ChatSettings
import com.chain.messaging.domain.model.ChatType
import com.chain.messaging.domain.repository.ChatRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class PinChatUseCaseTest {
    
    private lateinit var chatRepository: ChatRepository
    private lateinit var pinChatUseCase: PinChatUseCase
    
    @Before
    fun setUp() {
        chatRepository = mockk()
        pinChatUseCase = PinChatUseCase(chatRepository)
    }
    
    @Test
    fun `execute should pin chat successfully`() = runTest {
        // Given
        val chatId = "chat123"
        val chat = Chat(
            id = chatId,
            type = ChatType.DIRECT,
            name = "Test Chat",
            participants = listOf("user1", "user2"),
            settings = ChatSettings(isPinned = false),
            createdAt = Date(),
            updatedAt = Date()
        )
        val expectedSettings = chat.settings.copy(isPinned = true)
        
        coEvery { chatRepository.getChatById(chatId) } returns chat
        coEvery { chatRepository.updateChatSettings(chatId, expectedSettings) } returns Result.success(Unit)
        
        // When
        val result = pinChatUseCase.execute(chatId, true)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { chatRepository.getChatById(chatId) }
        coVerify { chatRepository.updateChatSettings(chatId, expectedSettings) }
    }
    
    @Test
    fun `execute should unpin chat successfully`() = runTest {
        // Given
        val chatId = "chat123"
        val chat = Chat(
            id = chatId,
            type = ChatType.DIRECT,
            name = "Test Chat",
            participants = listOf("user1", "user2"),
            settings = ChatSettings(isPinned = true),
            createdAt = Date(),
            updatedAt = Date()
        )
        val expectedSettings = chat.settings.copy(isPinned = false)
        
        coEvery { chatRepository.getChatById(chatId) } returns chat
        coEvery { chatRepository.updateChatSettings(chatId, expectedSettings) } returns Result.success(Unit)
        
        // When
        val result = pinChatUseCase.execute(chatId, false)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { chatRepository.getChatById(chatId) }
        coVerify { chatRepository.updateChatSettings(chatId, expectedSettings) }
    }
    
    @Test
    fun `execute should return failure when chat not found`() = runTest {
        // Given
        val chatId = "nonexistent"
        
        coEvery { chatRepository.getChatById(chatId) } returns null
        
        // When
        val result = pinChatUseCase.execute(chatId, true)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Chat not found", result.exceptionOrNull()?.message)
        coVerify { chatRepository.getChatById(chatId) }
        coVerify(exactly = 0) { chatRepository.updateChatSettings(any(), any()) }
    }
    
    @Test
    fun `execute should return failure when repository throws exception`() = runTest {
        // Given
        val chatId = "chat123"
        val chat = Chat(
            id = chatId,
            type = ChatType.DIRECT,
            name = "Test Chat",
            participants = listOf("user1", "user2"),
            settings = ChatSettings(isPinned = false),
            createdAt = Date(),
            updatedAt = Date()
        )
        val exception = RuntimeException("Database error")
        
        coEvery { chatRepository.getChatById(chatId) } returns chat
        coEvery { chatRepository.updateChatSettings(any(), any()) } returns Result.failure(exception)
        
        // When
        val result = pinChatUseCase.execute(chatId, true)
        
        // Then
        assertTrue(result.isFailure)
        coVerify { chatRepository.getChatById(chatId) }
        coVerify { chatRepository.updateChatSettings(any(), any()) }
    }
    
    @Test
    fun `execute should handle repository getChatById exception`() = runTest {
        // Given
        val chatId = "chat123"
        val exception = RuntimeException("Network error")
        
        coEvery { chatRepository.getChatById(chatId) } throws exception
        
        // When
        val result = pinChatUseCase.execute(chatId, true)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { chatRepository.getChatById(chatId) }
        coVerify(exactly = 0) { chatRepository.updateChatSettings(any(), any()) }
    }
}