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

class ArchiveChatUseCaseTest {
    
    private lateinit var chatRepository: ChatRepository
    private lateinit var archiveChatUseCase: ArchiveChatUseCase
    
    @Before
    fun setup() {
        chatRepository = mockk()
        archiveChatUseCase = ArchiveChatUseCase(chatRepository)
    }
    
    @Test
    fun `execute should archive chat successfully`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val chat = Chat(
            id = chatId,
            type = ChatType.DIRECT,
            name = "Test Chat",
            participants = listOf("user1", "user2"),
            settings = ChatSettings(isArchived = false),
            createdAt = Date(),
            updatedAt = Date()
        )
        val expectedSettings = chat.settings.copy(isArchived = true)
        
        coEvery { chatRepository.getChatById(chatId) } returns chat
        coEvery { chatRepository.updateChatSettings(chatId, expectedSettings) } returns Result.success(Unit)
        
        // When
        val result = archiveChatUseCase.execute(chatId, true)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { chatRepository.getChatById(chatId) }
        coVerify { chatRepository.updateChatSettings(chatId, expectedSettings) }
    }
    
    @Test
    fun `execute should unarchive chat successfully`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val chat = Chat(
            id = chatId,
            type = ChatType.DIRECT,
            name = "Test Chat",
            participants = listOf("user1", "user2"),
            settings = ChatSettings(isArchived = true),
            createdAt = Date(),
            updatedAt = Date()
        )
        val expectedSettings = chat.settings.copy(isArchived = false)
        
        coEvery { chatRepository.getChatById(chatId) } returns chat
        coEvery { chatRepository.updateChatSettings(chatId, expectedSettings) } returns Result.success(Unit)
        
        // When
        val result = archiveChatUseCase.execute(chatId, false)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { chatRepository.getChatById(chatId) }
        coVerify { chatRepository.updateChatSettings(chatId, expectedSettings) }
    }
    
    @Test
    fun `execute should return failure when chat not found`() = runTest {
        // Given
        val chatId = "non-existent-chat-id"
        
        coEvery { chatRepository.getChatById(chatId) } returns null
        
        // When
        val result = archiveChatUseCase.execute(chatId, true)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Chat not found", result.exceptionOrNull()?.message)
        coVerify { chatRepository.getChatById(chatId) }
        coVerify(exactly = 0) { chatRepository.updateChatSettings(any(), any()) }
    }
    
    @Test
    fun `execute should return failure when repository throws exception`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val chat = Chat(
            id = chatId,
            type = ChatType.DIRECT,
            name = "Test Chat",
            participants = listOf("user1", "user2"),
            settings = ChatSettings(isArchived = false),
            createdAt = Date(),
            updatedAt = Date()
        )
        val expectedSettings = chat.settings.copy(isArchived = true)
        val exception = Exception("Database error")
        
        coEvery { chatRepository.getChatById(chatId) } returns chat
        coEvery { chatRepository.updateChatSettings(chatId, expectedSettings) } returns Result.failure(exception)
        
        // When
        val result = archiveChatUseCase.execute(chatId, true)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { chatRepository.getChatById(chatId) }
        coVerify { chatRepository.updateChatSettings(chatId, expectedSettings) }
    }
}