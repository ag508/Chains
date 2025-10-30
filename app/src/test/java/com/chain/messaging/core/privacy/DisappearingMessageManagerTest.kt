package com.chain.messaging.core.privacy

import android.content.Context
import android.content.SharedPreferences
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.repository.MessageRepository
import com.chain.messaging.core.blockchain.BlockchainManager
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.util.Date

@ExperimentalCoroutinesApi
class DisappearingMessageManagerTest {
    
    private lateinit var disappearingMessageManager: DisappearingMessageManagerImpl
    private val mockContext = mockk<Context>()
    private val mockMessageRepository = mockk<MessageRepository>()
    private val mockBlockchainManager = mockk<BlockchainManager>()
    private val mockScreenshotDetector = mockk<ScreenshotDetector>()
    private val mockSharedPreferences = mockk<SharedPreferences>()
    private val mockEditor = mockk<SharedPreferences.Editor>()
    
    @Before
    fun setup() {
        every { mockContext.getSharedPreferences(any(), any()) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putLong(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        
        disappearingMessageManager = DisappearingMessageManagerImpl(
            mockContext,
            mockMessageRepository,
            mockBlockchainManager,
            mockScreenshotDetector
        )
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `setDisappearingMessageTimer should save timer to preferences`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val timerDuration = DisappearingMessageTimers.ONE_MINUTE
        
        // When
        disappearingMessageManager.setDisappearingMessageTimer(chatId, timerDuration)
        
        // Then
        verify { mockEditor.putLong("timer_$chatId", timerDuration) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `setDisappearingMessageTimer with null should remove timer from preferences`() = runTest {
        // Given
        val chatId = "test-chat-id"
        
        // When
        disappearingMessageManager.setDisappearingMessageTimer(chatId, null)
        
        // Then
        verify { mockEditor.remove("timer_$chatId") }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `getDisappearingMessageTimer should return timer from preferences`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val expectedTimer = DisappearingMessageTimers.FIVE_MINUTES
        every { mockSharedPreferences.getLong("timer_$chatId", -1L) } returns expectedTimer
        
        // When
        val result = disappearingMessageManager.getDisappearingMessageTimer(chatId)
        
        // Then
        assertEquals(expectedTimer, result)
    }
    
    @Test
    fun `getDisappearingMessageTimer should return null when no timer set`() = runTest {
        // Given
        val chatId = "test-chat-id"
        every { mockSharedPreferences.getLong("timer_$chatId", -1L) } returns -1L
        
        // When
        val result = disappearingMessageManager.getDisappearingMessageTimer(chatId)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `processMessageForExpiration should add expiration when timer is set`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val timerDuration = DisappearingMessageTimers.ONE_MINUTE
        val message = createTestMessage(chatId)
        
        every { mockSharedPreferences.getLong("timer_$chatId", -1L) } returns timerDuration
        
        // When
        val result = disappearingMessageManager.processMessageForExpiration(message, chatId)
        
        // Then
        assertTrue(result.isDisappearing)
        assertEquals(timerDuration, result.disappearingMessageTimer)
        assertNotNull(result.expiresAt)
        assertTrue(result.expiresAt!!.time > System.currentTimeMillis())
    }
    
    @Test
    fun `processMessageForExpiration should not modify message when no timer set`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val message = createTestMessage(chatId)
        
        every { mockSharedPreferences.getLong("timer_$chatId", -1L) } returns -1L
        
        // When
        val result = disappearingMessageManager.processMessageForExpiration(message, chatId)
        
        // Then
        assertEquals(message, result)
    }
    
    @Test
    fun `cleanupExpiredMessages should delete expired messages and return count`() = runTest {
        // Given
        val expiredMessages = listOf(
            createTestMessage("chat1", isExpired = true),
            createTestMessage("chat2", isExpired = true)
        )
        
        coEvery { mockMessageRepository.getExpiredMessages(any()) } returns expiredMessages
        coEvery { mockMessageRepository.deleteMessage(any()) } returns Result.success(Unit)
        coEvery { mockBlockchainManager.sendDeletionTransaction(any()) } just Runs
        
        // When
        val result = disappearingMessageManager.cleanupExpiredMessages()
        
        // Then
        assertEquals(2, result)
        coVerify(exactly = 2) { mockMessageRepository.deleteMessage(any()) }
        coVerify(exactly = 2) { mockBlockchainManager.sendDeletionTransaction(any()) }
    }
    
    @Test
    fun `cleanupExpiredMessages should handle deletion errors gracefully`() = runTest {
        // Given
        val expiredMessages = listOf(
            createTestMessage("chat1", isExpired = true),
            createTestMessage("chat2", isExpired = true)
        )
        
        coEvery { mockMessageRepository.getExpiredMessages(any()) } returns expiredMessages
        coEvery { mockMessageRepository.deleteMessage(expiredMessages[0].id) } returns Result.failure(Exception("Delete failed"))
        coEvery { mockMessageRepository.deleteMessage(expiredMessages[1].id) } returns Result.success(Unit)
        coEvery { mockBlockchainManager.sendDeletionTransaction(any()) } just Runs
        
        // When
        val result = disappearingMessageManager.cleanupExpiredMessages()
        
        // Then
        assertEquals(1, result) // Only one successful deletion
    }
    
    @Test
    fun `getAvailableTimerOptions should return all predefined options`() {
        // When
        val options = disappearingMessageManager.getAvailableTimerOptions()
        
        // Then
        assertEquals(DisappearingMessageTimers.ALL_OPTIONS, options)
        assertTrue(options.contains(DisappearingMessageTimers.FIVE_SECONDS))
        assertTrue(options.contains(DisappearingMessageTimers.ONE_WEEK))
    }
    
    @Test
    fun `startCleanupService should start periodic cleanup`() = runTest {
        // Given
        coEvery { mockMessageRepository.getExpiredMessages(any()) } returns emptyList()
        coEvery { mockMessageRepository.getMessagesExpiringBefore(any()) } returns emptyList()
        
        // When
        disappearingMessageManager.startCleanupService()
        
        // Then
        // Verify that cleanup service is running (this is a simplified test)
        // In a real implementation, you might want to test the periodic behavior
        coVerify(timeout = 1000) { mockMessageRepository.getExpiredMessages(any()) }
    }
    
    private fun createTestMessage(
        chatId: String,
        isExpired: Boolean = false
    ): Message {
        val expiresAt = if (isExpired) {
            Date(System.currentTimeMillis() - 1000) // Expired 1 second ago
        } else {
            Date(System.currentTimeMillis() + 60000) // Expires in 1 minute
        }
        
        return Message(
            id = "test-message-${System.currentTimeMillis()}",
            chatId = chatId,
            senderId = "test-sender",
            content = "Test message",
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENT,
            isDisappearing = true,
            disappearingMessageTimer = DisappearingMessageTimers.ONE_MINUTE,
            expiresAt = expiresAt
        )
    }
}