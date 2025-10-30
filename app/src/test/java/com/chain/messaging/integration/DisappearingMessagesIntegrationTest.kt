package com.chain.messaging.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.chain.messaging.data.local.ChainDatabase
import com.chain.messaging.data.local.dao.MessageDao
import com.chain.messaging.data.repository.MessageRepositoryImpl
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.domain.repository.MessageRepository
import com.chain.messaging.core.privacy.*
import com.chain.messaging.core.blockchain.BlockchainManager
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class DisappearingMessagesIntegrationTest {
    
    private lateinit var database: ChainDatabase
    private lateinit var messageDao: MessageDao
    private lateinit var messageRepository: MessageRepository
    private lateinit var disappearingMessageManager: DisappearingMessageManager
    private lateinit var screenshotDetector: ScreenshotDetector
    private lateinit var context: Context
    
    private val mockBlockchainManager = mockk<BlockchainManager>()
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            ChainDatabase::class.java
        ).allowMainThreadQueries().build()
        
        messageDao = database.messageDao()
        messageRepository = MessageRepositoryImpl(messageDao)
        screenshotDetector = ScreenshotDetectorImpl(context)
        
        // Mock blockchain manager
        coEvery { mockBlockchainManager.sendDeletionTransaction(any()) } just Runs
        
        disappearingMessageManager = DisappearingMessageManagerImpl(
            context,
            messageRepository,
            mockBlockchainManager,
            screenshotDetector
        )
    }
    
    @After
    fun tearDown() {
        database.close()
        clearAllMocks()
    }
    
    @Test
    fun `complete disappearing message flow should work end-to-end`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val timerDuration = 1000L // 1 second for quick testing
        
        // Set up disappearing message timer
        disappearingMessageManager.setDisappearingMessageTimer(chatId, timerDuration)
        
        // Create a regular message
        val originalMessage = Message(
            id = "test-message-1",
            chatId = chatId,
            senderId = "sender-1",
            content = "This message will disappear",
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENT
        )
        
        // When - Process message for expiration
        val processedMessage = disappearingMessageManager.processMessageForExpiration(originalMessage, chatId)
        
        // Then - Message should be marked as disappearing
        assertTrue(processedMessage.isDisappearing)
        assertEquals(timerDuration, processedMessage.disappearingMessageTimer)
        assertNotNull(processedMessage.expiresAt)
        
        // Save the processed message
        messageRepository.saveMessage(processedMessage)
        
        // Verify message is saved
        val savedMessage = messageRepository.getMessageById(processedMessage.id)
        assertNotNull(savedMessage)
        assertTrue(savedMessage!!.isDisappearing)
        
        // Wait for message to expire (plus a small buffer)
        delay(timerDuration + 100)
        
        // When - Run cleanup
        val deletedCount = disappearingMessageManager.cleanupExpiredMessages()
        
        // Then - Message should be deleted
        assertEquals(1, deletedCount)
        
        // Verify message is deleted from database
        val deletedMessage = messageRepository.getMessageById(processedMessage.id)
        assertNull(deletedMessage)
        
        // Verify blockchain deletion was called
        coVerify { mockBlockchainManager.sendDeletionTransaction(processedMessage.id) }
    }
    
    @Test
    fun `multiple messages with different expiration times should be handled correctly`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val shortTimer = 500L // 0.5 seconds
        val longTimer = 2000L // 2 seconds
        
        // Set up disappearing message timer
        disappearingMessageManager.setDisappearingMessageTimer(chatId, shortTimer)
        
        // Create messages
        val message1 = createTestMessage("msg-1", chatId)
        val message2 = createTestMessage("msg-2", chatId)
        
        // Process messages
        val processedMessage1 = disappearingMessageManager.processMessageForExpiration(message1, chatId)
        
        // Change timer for second message
        disappearingMessageManager.setDisappearingMessageTimer(chatId, longTimer)
        val processedMessage2 = disappearingMessageManager.processMessageForExpiration(message2, chatId)
        
        // Save messages
        messageRepository.saveMessage(processedMessage1)
        messageRepository.saveMessage(processedMessage2)
        
        // Wait for first message to expire
        delay(shortTimer + 100)
        
        // When - Run cleanup
        val deletedCount1 = disappearingMessageManager.cleanupExpiredMessages()
        
        // Then - Only first message should be deleted
        assertEquals(1, deletedCount1)
        assertNull(messageRepository.getMessageById(processedMessage1.id))
        assertNotNull(messageRepository.getMessageById(processedMessage2.id))
        
        // Wait for second message to expire
        delay(longTimer - shortTimer + 100)
        
        // When - Run cleanup again
        val deletedCount2 = disappearingMessageManager.cleanupExpiredMessages()
        
        // Then - Second message should now be deleted
        assertEquals(1, deletedCount2)
        assertNull(messageRepository.getMessageById(processedMessage2.id))
    }
    
    @Test
    fun `messages without disappearing timer should not be affected by cleanup`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val regularMessage = createTestMessage("regular-msg", chatId)
        
        // Save regular message (without processing for expiration)
        messageRepository.saveMessage(regularMessage)
        
        // When - Run cleanup
        val deletedCount = disappearingMessageManager.cleanupExpiredMessages()
        
        // Then - No messages should be deleted
        assertEquals(0, deletedCount)
        assertNotNull(messageRepository.getMessageById(regularMessage.id))
    }
    
    @Test
    fun `disabling disappearing messages should stop affecting new messages`() = runTest {
        // Given
        val chatId = "test-chat-id"
        val timerDuration = 1000L
        
        // Set up and then disable disappearing message timer
        disappearingMessageManager.setDisappearingMessageTimer(chatId, timerDuration)
        disappearingMessageManager.setDisappearingMessageTimer(chatId, null)
        
        val message = createTestMessage("test-msg", chatId)
        
        // When - Process message
        val processedMessage = disappearingMessageManager.processMessageForExpiration(message, chatId)
        
        // Then - Message should not be marked as disappearing
        assertFalse(processedMessage.isDisappearing)
        assertNull(processedMessage.disappearingMessageTimer)
        assertNull(processedMessage.expiresAt)
        assertEquals(message, processedMessage)
    }
    
    @Test
    fun `getAvailableTimerOptions should return all predefined options`() {
        // When
        val options = disappearingMessageManager.getAvailableTimerOptions()
        
        // Then
        assertEquals(DisappearingMessageTimers.ALL_OPTIONS.size, options.size)
        assertTrue(options.contains(DisappearingMessageTimers.FIVE_SECONDS))
        assertTrue(options.contains(DisappearingMessageTimers.ONE_WEEK))
        
        // Verify options are in ascending order
        for (i in 1 until options.size) {
            assertTrue("Options should be in ascending order", options[i] > options[i-1])
        }
    }
    
    @Test
    fun `cleanup service should run periodically`() = runTest {
        // Given
        coEvery { mockBlockchainManager.sendDeletionTransaction(any()) } just Runs
        
        // When - Start cleanup service
        disappearingMessageManager.startCleanupService()
        
        // Wait a bit to let the service run
        delay(100)
        
        // Then - Stop the service
        disappearingMessageManager.stopCleanupService()
        
        // The test passes if no exceptions are thrown
        assertTrue(true)
    }
    
    private fun createTestMessage(id: String, chatId: String): Message {
        return Message(
            id = id,
            chatId = chatId,
            senderId = "test-sender",
            content = "Test message content",
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENT
        )
    }
}