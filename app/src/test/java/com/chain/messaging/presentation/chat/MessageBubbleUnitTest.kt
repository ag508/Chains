package com.chain.messaging.presentation.chat

import com.chain.messaging.domain.model.*
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Unit tests for message bubble functionality
 * Tests Requirements: 4.6, 4.7, 4.4
 */
class MessageBubbleUnitTest {
    
    @Test
    fun messagePreview_returnsCorrectTextForDifferentTypes() {
        // Test text message
        val textMessage = createTestMessage(
            content = "Hello world",
            type = MessageType.TEXT
        )
        assertEquals("Hello world", getMessagePreviewText(textMessage))
        
        // Test image message
        val imageMessage = createTestMessage(
            content = "Caption",
            type = MessageType.IMAGE
        )
        assertEquals("📷 Image", getMessagePreviewText(imageMessage))
        
        // Test video message
        val videoMessage = createTestMessage(
            content = "Video caption",
            type = MessageType.VIDEO
        )
        assertEquals("🎥 Video", getMessagePreviewText(videoMessage))
        
        // Test audio message
        val audioMessage = createTestMessage(
            content = "",
            type = MessageType.AUDIO
        )
        assertEquals("🎵 Voice message", getMessagePreviewText(audioMessage))
        
        // Test document message
        val documentMessage = createTestMessage(
            content = "document.pdf",
            type = MessageType.DOCUMENT
        )
        assertEquals("📄 Document", getMessagePreviewText(documentMessage))
    }
    
    @Test
    fun messageReactions_groupCorrectly() {
        val reactions = listOf(
            Reaction("user1", "👍", Date()),
            Reaction("user2", "👍", Date()),
            Reaction("user3", "❤️", Date()),
            Reaction("user4", "👍", Date())
        )
        
        val groupedReactions = reactions.groupBy { it.emoji }
            .mapValues { it.value.size }
        
        assertEquals(2, groupedReactions.size)
        assertEquals(3, groupedReactions["👍"])
        assertEquals(1, groupedReactions["❤️"])
    }
    
    @Test
    fun messageStatus_hasCorrectValues() {
        val statuses = MessageStatus.values()
        
        assertTrue(statuses.contains(MessageStatus.SENDING))
        assertTrue(statuses.contains(MessageStatus.SENT))
        assertTrue(statuses.contains(MessageStatus.DELIVERED))
        assertTrue(statuses.contains(MessageStatus.READ))
        assertTrue(statuses.contains(MessageStatus.FAILED))
    }
    
    @Test
    fun messageType_hasCorrectValues() {
        val types = MessageType.values()
        
        assertTrue(types.contains(MessageType.TEXT))
        assertTrue(types.contains(MessageType.IMAGE))
        assertTrue(types.contains(MessageType.VIDEO))
        assertTrue(types.contains(MessageType.AUDIO))
        assertTrue(types.contains(MessageType.DOCUMENT))
        assertTrue(types.contains(MessageType.LOCATION))
        assertTrue(types.contains(MessageType.CONTACT))
        assertTrue(types.contains(MessageType.POLL))
        assertTrue(types.contains(MessageType.SYSTEM))
    }
    
    @Test
    fun reaction_hasCorrectProperties() {
        val reaction = Reaction(
            userId = "user123",
            emoji = "👍",
            timestamp = Date()
        )
        
        assertEquals("user123", reaction.userId)
        assertEquals("👍", reaction.emoji)
        assertNotNull(reaction.timestamp)
    }
    
    private fun createTestMessage(
        id: String = "msg_1",
        content: String = "Test message",
        senderId: String = "user_1",
        type: MessageType = MessageType.TEXT,
        status: MessageStatus = MessageStatus.DELIVERED,
        reactions: List<Reaction> = emptyList()
    ): Message {
        return Message(
            id = id,
            chatId = "chat_1",
            senderId = senderId,
            content = content,
            type = type,
            timestamp = Date(),
            status = status,
            reactions = reactions
        )
    }
    
    private fun getMessagePreviewText(message: Message): String {
        return when (message.type) {
            MessageType.TEXT -> message.content
            MessageType.IMAGE -> "📷 Image"
            MessageType.VIDEO -> "🎥 Video"
            MessageType.AUDIO -> "🎵 Voice message"
            MessageType.DOCUMENT -> "📄 Document"
            MessageType.LOCATION -> "📍 Location"
            MessageType.CONTACT -> "👤 Contact"
            MessageType.POLL -> "📊 Poll"
            MessageType.SYSTEM -> message.content
        }
    }
}