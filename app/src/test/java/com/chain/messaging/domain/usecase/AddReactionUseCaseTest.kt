package com.chain.messaging.domain.usecase

import com.chain.messaging.domain.repository.MessageRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddReactionUseCaseTest {
    
    private lateinit var messageRepository: MessageRepository
    private lateinit var addReactionUseCase: AddReactionUseCase
    
    @Before
    fun setUp() {
        messageRepository = mockk()
        addReactionUseCase = AddReactionUseCase(messageRepository)
    }
    
    @Test
    fun `invoke should call repository addReaction and return success`() = runTest {
        // Given
        val messageId = "message123"
        val userId = "user456"
        val emoji = "👍"
        coEvery { messageRepository.addReaction(messageId, userId, emoji) } returns Result.success(Unit)
        
        // When
        val result = addReactionUseCase(messageId, userId, emoji)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { messageRepository.addReaction(messageId, userId, emoji) }
    }
    
    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Given
        val messageId = "message123"
        val userId = "user456"
        val emoji = "👍"
        val exception = RuntimeException("Database error")
        coEvery { messageRepository.addReaction(messageId, userId, emoji) } returns Result.failure(exception)
        
        // When
        val result = addReactionUseCase(messageId, userId, emoji)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { messageRepository.addReaction(messageId, userId, emoji) }
    }
    
    @Test
    fun `invoke should handle unexpected exceptions`() = runTest {
        // Given
        val messageId = "message123"
        val userId = "user456"
        val emoji = "👍"
        val exception = RuntimeException("Unexpected error")
        coEvery { messageRepository.addReaction(messageId, userId, emoji) } throws exception
        
        // When
        val result = addReactionUseCase(messageId, userId, emoji)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify { messageRepository.addReaction(messageId, userId, emoji) }
    }
}