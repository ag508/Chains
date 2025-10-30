package com.chain.messaging.domain.model

import org.junit.Test
import org.junit.Assert.*
import java.util.Date

/**
 * Unit tests for MediaMessage and related functionality
 */
class MediaMessageTest {
    
    @Test
    fun `MediaType fromMimeType should return correct type for images`() {
        assertEquals(MediaType.IMAGE, MediaType.fromMimeType("image/jpeg"))
        assertEquals(MediaType.IMAGE, MediaType.fromMimeType("image/png"))
        assertEquals(MediaType.IMAGE, MediaType.fromMimeType("image/gif"))
    }
    
    @Test
    fun `MediaType fromMimeType should return correct type for videos`() {
        assertEquals(MediaType.VIDEO, MediaType.fromMimeType("video/mp4"))
        assertEquals(MediaType.VIDEO, MediaType.fromMimeType("video/avi"))
        assertEquals(MediaType.VIDEO, MediaType.fromMimeType("video/quicktime"))
    }
    
    @Test
    fun `MediaType fromMimeType should return correct type for audio`() {
        assertEquals(MediaType.AUDIO, MediaType.fromMimeType("audio/mp3"))
        assertEquals(MediaType.AUDIO, MediaType.fromMimeType("audio/wav"))
        assertEquals(MediaType.AUDIO, MediaType.fromMimeType("audio/ogg"))
    }
    
    @Test
    fun `MediaType fromMimeType should return DOCUMENT for unknown types`() {
        assertEquals(MediaType.DOCUMENT, MediaType.fromMimeType("application/pdf"))
        assertEquals(MediaType.DOCUMENT, MediaType.fromMimeType("text/plain"))
        assertEquals(MediaType.DOCUMENT, MediaType.fromMimeType("unknown/type"))
    }
    
    @Test
    fun `getMediaContent should return null for text messages`() {
        val textMessage = Message(
            id = "1",
            chatId = "chat1",
            senderId = "user1",
            content = "Hello world",
            type = MessageType.TEXT,
            timestamp = Date(),
            status = MessageStatus.SENT
        )
        
        assertNull(textMessage.getMediaContent())
    }
    
    @Test
    fun `getMediaContent should parse valid JSON for image messages`() {
        val jsonContent = """
            {
                "uri": "/path/to/image.jpg",
                "fileName": "image.jpg",
                "mimeType": "image/jpeg",
                "fileSize": 1024,
                "duration": null,
                "width": 800,
                "height": 600,
                "thumbnailUri": null,
                "isLocal": true
            }
        """.trimIndent()
        
        val imageMessage = Message(
            id = "1",
            chatId = "chat1",
            senderId = "user1",
            content = jsonContent,
            type = MessageType.IMAGE,
            timestamp = Date(),
            status = MessageStatus.SENT
        )
        
        val mediaContent = imageMessage.getMediaContent()
        assertNotNull(mediaContent)
        assertEquals("/path/to/image.jpg", mediaContent?.uri)
        assertEquals("image.jpg", mediaContent?.fileName)
        assertEquals("image/jpeg", mediaContent?.mimeType)
        assertEquals(1024L, mediaContent?.fileSize)
        assertEquals(800, mediaContent?.width)
        assertEquals(600, mediaContent?.height)
        assertTrue(mediaContent?.isLocal == true)
    }
    
    @Test
    fun `getMediaContent should parse valid JSON for video messages with duration`() {
        val jsonContent = """
            {
                "uri": "/path/to/video.mp4",
                "fileName": "video.mp4",
                "mimeType": "video/mp4",
                "fileSize": 5120,
                "duration": 30000,
                "width": 1920,
                "height": 1080,
                "thumbnailUri": "/path/to/thumbnail.jpg",
                "isLocal": true
            }
        """.trimIndent()
        
        val videoMessage = Message(
            id = "1",
            chatId = "chat1",
            senderId = "user1",
            content = jsonContent,
            type = MessageType.VIDEO,
            timestamp = Date(),
            status = MessageStatus.SENT
        )
        
        val mediaContent = videoMessage.getMediaContent()
        assertNotNull(mediaContent)
        assertEquals("/path/to/video.mp4", mediaContent?.uri)
        assertEquals("video.mp4", mediaContent?.fileName)
        assertEquals("video/mp4", mediaContent?.mimeType)
        assertEquals(5120L, mediaContent?.fileSize)
        assertEquals(30000L, mediaContent?.duration)
        assertEquals(1920, mediaContent?.width)
        assertEquals(1080, mediaContent?.height)
        assertEquals("/path/to/thumbnail.jpg", mediaContent?.thumbnailUri)
    }
    
    @Test
    fun `getMediaContent should parse valid JSON for audio messages`() {
        val jsonContent = """
            {
                "uri": "/path/to/audio.mp3",
                "fileName": "audio.mp3",
                "mimeType": "audio/mp3",
                "fileSize": 2048,
                "duration": 15000,
                "width": null,
                "height": null,
                "thumbnailUri": null,
                "isLocal": true
            }
        """.trimIndent()
        
        val audioMessage = Message(
            id = "1",
            chatId = "chat1",
            senderId = "user1",
            content = jsonContent,
            type = MessageType.AUDIO,
            timestamp = Date(),
            status = MessageStatus.SENT
        )
        
        val mediaContent = audioMessage.getMediaContent()
        assertNotNull(mediaContent)
        assertEquals("/path/to/audio.mp3", mediaContent?.uri)
        assertEquals("audio.mp3", mediaContent?.fileName)
        assertEquals("audio/mp3", mediaContent?.mimeType)
        assertEquals(2048L, mediaContent?.fileSize)
        assertEquals(15000L, mediaContent?.duration)
        assertNull(mediaContent?.width)
        assertNull(mediaContent?.height)
    }
    
    @Test
    fun `getMediaContent should parse valid JSON for document messages`() {
        val jsonContent = """
            {
                "uri": "/path/to/document.pdf",
                "fileName": "document.pdf",
                "mimeType": "application/pdf",
                "fileSize": 4096,
                "duration": null,
                "width": null,
                "height": null,
                "thumbnailUri": null,
                "isLocal": true
            }
        """.trimIndent()
        
        val documentMessage = Message(
            id = "1",
            chatId = "chat1",
            senderId = "user1",
            content = jsonContent,
            type = MessageType.DOCUMENT,
            timestamp = Date(),
            status = MessageStatus.SENT
        )
        
        val mediaContent = documentMessage.getMediaContent()
        assertNotNull(mediaContent)
        assertEquals("/path/to/document.pdf", mediaContent?.uri)
        assertEquals("document.pdf", mediaContent?.fileName)
        assertEquals("application/pdf", mediaContent?.mimeType)
        assertEquals(4096L, mediaContent?.fileSize)
    }
    
    @Test
    fun `getMediaContent should return null for invalid JSON`() {
        val invalidJsonMessage = Message(
            id = "1",
            chatId = "chat1",
            senderId = "user1",
            content = "invalid json content",
            type = MessageType.IMAGE,
            timestamp = Date(),
            status = MessageStatus.SENT
        )
        
        assertNull(invalidJsonMessage.getMediaContent())
    }
    
    @Test
    fun `getMediaContent should return null for malformed JSON`() {
        val malformedJsonMessage = Message(
            id = "1",
            chatId = "chat1",
            senderId = "user1",
            content = """{"uri": "/path", "fileName":""",
            type = MessageType.IMAGE,
            timestamp = Date(),
            status = MessageStatus.SENT
        )
        
        assertNull(malformedJsonMessage.getMediaContent())
    }
    
    @Test
    fun `MediaMessage should handle all properties correctly`() {
        val mediaMessage = MediaMessage(
            uri = "/test/path",
            fileName = "test.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024L,
            duration = 5000L,
            width = 800,
            height = 600,
            thumbnailUri = "/test/thumb.jpg",
            isLocal = false
        )
        
        assertEquals("/test/path", mediaMessage.uri)
        assertEquals("test.jpg", mediaMessage.fileName)
        assertEquals("image/jpeg", mediaMessage.mimeType)
        assertEquals(1024L, mediaMessage.fileSize)
        assertEquals(5000L, mediaMessage.duration)
        assertEquals(800, mediaMessage.width)
        assertEquals(600, mediaMessage.height)
        assertEquals("/test/thumb.jpg", mediaMessage.thumbnailUri)
        assertFalse(mediaMessage.isLocal)
    }
}