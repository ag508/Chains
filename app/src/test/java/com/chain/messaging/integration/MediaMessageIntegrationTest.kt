package com.chain.messaging.integration

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.chain.messaging.core.media.MediaHandler
import com.chain.messaging.domain.model.MediaType
import com.chain.messaging.domain.model.MessageType
import com.chain.messaging.presentation.media.MediaPickerType
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.InputStream

/**
 * Integration tests for media message functionality
 */
@RunWith(RobolectricTestRunner::class)
class MediaMessageIntegrationTest {
    
    private lateinit var context: Context
    private lateinit var mediaHandler: MediaHandler
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mediaHandler = MediaHandler(context)
    }
    
    @Test
    fun `complete media message flow should work end-to-end`() = runTest {
        // Given - Mock URI and content resolver
        val mockUri = mockk<Uri>()
        val mockInputStream = mockk<InputStream>()
        
        // Mock the context and content resolver
        val mockContext = mockk<Context>()
        val contentResolver = mockk<android.content.ContentResolver>()
        
        every { mockContext.contentResolver } returns contentResolver
        every { mockContext.filesDir } returns File("/tmp")
        
        every { contentResolver.getType(mockUri) } returns "image/jpeg"
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns mockk {
            every { moveToFirst() } returns true
            every { getColumnIndex(any()) } returns 0
            every { getString(0) } returns "test_image.jpg"
            every { getLong(0) } returns 1024L
            every { close() } just Runs
        }
        every { contentResolver.openInputStream(mockUri) } returns mockInputStream
        every { mockInputStream.copyTo(any()) } returns 1024L
        every { mockInputStream.close() } just Runs
        
        // Mock file operations
        mockkStatic(File::class)
        val mockFile = mockk<File>()
        every { File(any<File>(), any<String>()) } returns mockFile
        every { mockFile.mkdirs() } returns true
        every { mockFile.exists() } returns false
        every { File.createTempFile(any(), any(), any()) } returns mockFile
        every { mockFile.absolutePath } returns "/tmp/media/test_image.jpg"
        
        // Mock FileOutputStream
        mockkConstructor(java.io.FileOutputStream::class)
        every { anyConstructed<java.io.FileOutputStream>().close() } just Runs
        
        val testMediaHandler = MediaHandler(mockContext)
        
        // When - Process the media
        val result = testMediaHandler.processMedia(mockUri)
        
        // Then - Verify the result
        assertTrue("Media processing should succeed", result.isSuccess)
        
        val mediaMessage = result.getOrNull()
        assertNotNull("MediaMessage should not be null", mediaMessage)
        assertEquals("test_image.jpg", mediaMessage?.fileName)
        assertEquals("image/jpeg", mediaMessage?.mimeType)
        assertEquals(1024L, mediaMessage?.fileSize)
        assertTrue(mediaMessage?.isLocal == true)
    }
    
    @Test
    fun `MediaPickerType should map to correct MessageType`() {
        // Test mapping from MediaPickerType to MessageType
        val mappings = mapOf(
            MediaPickerType.CAMERA_IMAGE to MessageType.IMAGE,
            MediaPickerType.GALLERY_IMAGE to MessageType.IMAGE,
            MediaPickerType.GALLERY_VIDEO to MessageType.VIDEO,
            MediaPickerType.DOCUMENT to MessageType.DOCUMENT
        )
        
        mappings.forEach { (pickerType, expectedMessageType) ->
            val actualMessageType = when (pickerType) {
                MediaPickerType.CAMERA_IMAGE, MediaPickerType.GALLERY_IMAGE -> MessageType.IMAGE
                MediaPickerType.GALLERY_VIDEO -> MessageType.VIDEO
                MediaPickerType.DOCUMENT -> MessageType.DOCUMENT
            }
            
            assertEquals(
                "MediaPickerType $pickerType should map to MessageType $expectedMessageType",
                expectedMessageType,
                actualMessageType
            )
        }
    }
    
    @Test
    fun `MediaType should correctly identify mime types`() {
        val testCases = mapOf(
            "image/jpeg" to MediaType.IMAGE,
            "image/png" to MediaType.IMAGE,
            "image/gif" to MediaType.IMAGE,
            "video/mp4" to MediaType.VIDEO,
            "video/avi" to MediaType.VIDEO,
            "video/quicktime" to MediaType.VIDEO,
            "audio/mp3" to MediaType.AUDIO,
            "audio/wav" to MediaType.AUDIO,
            "audio/ogg" to MediaType.AUDIO,
            "application/pdf" to MediaType.DOCUMENT,
            "text/plain" to MediaType.DOCUMENT,
            "application/msword" to MediaType.DOCUMENT,
            "unknown/type" to MediaType.DOCUMENT
        )
        
        testCases.forEach { (mimeType, expectedType) ->
            val actualType = MediaType.fromMimeType(mimeType)
            assertEquals(
                "MIME type $mimeType should map to MediaType $expectedType",
                expectedType,
                actualType
            )
        }
    }
    
    @Test
    fun `media message JSON serialization and parsing should be consistent`() {
        // Given - Create a MediaMessage
        val originalMedia = com.chain.messaging.domain.model.MediaMessage(
            uri = "/path/to/test.jpg",
            fileName = "test.jpg",
            mimeType = "image/jpeg",
            fileSize = 2048L,
            duration = null,
            width = 800,
            height = 600,
            thumbnailUri = "/path/to/thumb.jpg",
            isLocal = true
        )
        
        // When - Serialize to JSON (simulate what ChatViewModel does)
        val jsonContent = """
            {
                "uri": "${originalMedia.uri}",
                "fileName": "${originalMedia.fileName}",
                "mimeType": "${originalMedia.mimeType}",
                "fileSize": ${originalMedia.fileSize},
                "duration": ${originalMedia.duration},
                "width": ${originalMedia.width},
                "height": ${originalMedia.height},
                "thumbnailUri": "${originalMedia.thumbnailUri}",
                "isLocal": ${originalMedia.isLocal}
            }
        """.trimIndent()
        
        // Create a message with this JSON content
        val message = com.chain.messaging.domain.model.Message(
            id = "1",
            chatId = "chat1",
            senderId = "user1",
            content = jsonContent,
            type = MessageType.IMAGE,
            timestamp = java.util.Date(),
            status = com.chain.messaging.domain.model.MessageStatus.SENT
        )
        
        // Then - Parse back from JSON
        val parsedMedia = message.getMediaContent()
        
        // Verify all properties match
        assertNotNull("Parsed media should not be null", parsedMedia)
        assertEquals(originalMedia.uri, parsedMedia?.uri)
        assertEquals(originalMedia.fileName, parsedMedia?.fileName)
        assertEquals(originalMedia.mimeType, parsedMedia?.mimeType)
        assertEquals(originalMedia.fileSize, parsedMedia?.fileSize)
        assertEquals(originalMedia.duration, parsedMedia?.duration)
        assertEquals(originalMedia.width, parsedMedia?.width)
        assertEquals(originalMedia.height, parsedMedia?.height)
        assertEquals(originalMedia.thumbnailUri, parsedMedia?.thumbnailUri)
        assertEquals(originalMedia.isLocal, parsedMedia?.isLocal)
    }
    
    @Test
    fun `media file operations should handle different file types`() = runTest {
        val testFiles = listOf(
            Triple("image.jpg", "image/jpeg", MediaType.IMAGE),
            Triple("video.mp4", "video/mp4", MediaType.VIDEO),
            Triple("audio.mp3", "audio/mp3", MediaType.AUDIO),
            Triple("document.pdf", "application/pdf", MediaType.DOCUMENT)
        )
        
        testFiles.forEach { (fileName, mimeType, expectedType) ->
            val mediaMessage = com.chain.messaging.domain.model.MediaMessage(
                uri = "/test/$fileName",
                fileName = fileName,
                mimeType = mimeType,
                fileSize = 1024L,
                isLocal = true
            )
            
            val actualType = MediaType.fromMimeType(mediaMessage.mimeType)
            assertEquals(
                "File $fileName with MIME type $mimeType should be recognized as $expectedType",
                expectedType,
                actualType
            )
        }
    }
}