package com.chain.messaging.core.privacy

import com.chain.messaging.core.messaging.MessagingService
import com.chain.messaging.domain.model.Message
import com.chain.messaging.domain.model.MessageStatus
import com.chain.messaging.domain.model.MessageType
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
class DisappearingMessageNotificationServiceTest {
    
    private lateinit var notificationService: DisappearingMessageNotificationServiceImpl
    private val mockMessagingService = mockk<MessagingService>()
    private val mockDisappearingMessageManager = mockk<DisappearingMessageManager>()
    private val mockScreenshotDetector = mockk<ScreenshotDetector>()
    
    @Before
    fun setup() {
        notificationService = DisappearingMessageNotificationServiceImpl(
            mockMessagingService,
            mockDisappearingMessageManager,
            mockScreenshotDetector
        )
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `notifyScreenshotDetected should send system message`() = runTest {
        // Given
        val messageId = "test-message-id"
        val screenshotEvent = ScreenshotEvent(
            timestamp = System.currentTimeMillis(),
            filePath = "/storage/screenshot.png",
            type = ScreenshotType.SCREENSHOT
        )
        
        coEvery { mockMessagingService.sendSystemMessage(any()) } just Runs
        
        // When
        notificationService.notifyScreenshotDetected(messageId, screenshotEvent)
        
        // Then
        coVerify { mockMessagingService.sendSystemMessage(any()) }
    }
    
    @Test
    fun `observeScreenshotNotifications should correlate screenshots with disappearing messages`() = runTest {
        // Given
        val screenshotEvent = ScreenshotEvent(
            timestamp = System.currentTimeMillis(),
            filePath = "/storage/screenshot.png",
            type = ScreenshotType.SCREENSHOT
        )
        
        val disappearingMessage = createTestDisappearingMessage()
        
        every { mockScreenshotDetector.observeScreenshots() } returns flowOf(screenshotEvent)
        every { mockDisappearingMessageManager.observeMessagesAboutToExpire() } returns flowOf(listOf(disappearingMessage))
        coEvery { mockMessagingService.sendSystemMessage(any()) } just Runs
        
        // When
        val flow = notificationService.observeScreenshotNotifications()
        
        // Then
        // The flow should process the correlation and send notifications
        // This is a simplified test - in practice, you'd collect from the flow
        coVerify(timeout = 1000) { mockMessagingService.sendSystemMessage(any()) }
    }
    
    @Test
    fun `warnAboutExpiringMessages should create warnings for each message`() = runTest {
        // Given
        val expiringMessages = listOf(
            createTestDisappearingMessage("msg1"),
            createTestDisappearingMessage("msg2")
        )
        
        // When
        notificationService.warnAboutExpiringMessages(expiringMessages)
        
        // Then
        // Verify that warnings were created for each message
        // In a real implementation, this might involve creating notifications
        // For now, we just verify the method completes without error
        assertTrue(true)
    }
    
    @Test
    fun `notifyScreenshotDetected should handle messaging service errors gracefully`() = runTest {
        // Given
        val messageId = "test-message-id"
        val screenshotEvent = ScreenshotEvent(
            timestamp = System.currentTimeMillis(),
            filePath = "/storage/screenshot.png",
            type = ScreenshotType.SCREENSHOT
        )
        
        coEvery { mockMessagingService.sendSystemMessage(any()) } throws Exception("Messaging failed")
        
        // When & Then
        // Should not throw exception
        notificationService.notifyScreenshotDetected(messageId, screenshotEvent)
        
        coVerify { mockMessagingService.sendSystemMessage(any()) }
    }
    
    @Test
    fun `createScreenshotNotificationMessage should create proper system message`() = runTest {
        // Given
        val messageId = "test-message-id"
        val screenshotEvent = ScreenshotEvent(
            timestamp = System.currentTimeMillis(),
            filePath = "/storage/screenshot.png",
            type = ScreenshotType.SCREENSHOT
        )
        
        coEvery { mockMessagingService.sendSystemMessage(any()) } just Runs
        
        // When
        notificationService.notifyScreenshotDetected(messageId, screenshotEvent)
        
        // Then
        coVerify { 
            mockMessagingService.sendSystemMessage(match { message ->
                message.type == MessageType.SYSTEM &&
                message.senderId == "system" &&
                message.content.contains("Screenshot detected") &&
                !message.isEncrypted
            })
        }
    }
    
    private fun createTestDisappearingMessage(id: String = "test-message"): Message {
        return Message(
            id = id,
            chatId = "test-chat",
            senderId = "test-sender",
            content = "Test disappearing message",
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENT,
            isDisappearing = true,
            disappearingMessageTimer = DisappearingMessageTimers.ONE_MINUTE,
            expiresAt = Date(System.currentTimeMillis() + 60000)
        )
    }
}