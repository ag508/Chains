package com.chain.messaging.core.media

import android.content.Context
import android.net.Uri
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * Unit tests for MediaHandler
 */
class MediaHandlerTest {
    
    private lateinit var mediaHandler: MediaHandler
    private val mockContext = mockk<Context>()
    
    @Before
    fun setup() {
        mediaHandler = MediaHandler(mockContext)
        
        // Mock context methods
        every { mockContext.filesDir } returns File("/mock/files")
        every { mockContext.contentResolver } returns mockk()
    }
    
    @Test
    fun `processMedia should handle image URI successfully`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val mockInputStream = mockk<InputStream>()
        
        // Mock content resolver behavior
        val contentResolver = mockContext.contentResolver
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
        every { mockFile.absolutePath } returns "/mock/path/test_image.jpg"
        
        // Mock FileOutputStream
        mockkConstructor(java.io.FileOutputStream::class)
        every { anyConstructed<java.io.FileOutputStream>().close() } just Runs
        
        // When
        val result = mediaHandler.processMedia(mockUri)
        
        // Then
        assertTrue(result.isSuccess)
        val mediaMessage = result.getOrNull()
        assertNotNull(mediaMessage)
        assertEquals("test_image.jpg", mediaMessage?.fileName)
        assertEquals("image/jpeg", mediaMessage?.mimeType)
        assertEquals(1024L, mediaMessage?.fileSize)
        assertTrue(mediaMessage?.isLocal == true)
    }
    
    @Test
    fun `processMedia should handle video URI successfully`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val mockInputStream = mockk<InputStream>()
        
        // Mock content resolver behavior
        val contentResolver = mockContext.contentResolver
        every { contentResolver.getType(mockUri) } returns "video/mp4"
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns mockk {
            every { moveToFirst() } returns true
            every { getColumnIndex(any()) } returns 0
            every { getString(0) } returns "test_video.mp4"
            every { getLong(0) } returns 5120L
            every { close() } just Runs
        }
        every { contentResolver.openInputStream(mockUri) } returns mockInputStream
        every { mockInputStream.copyTo(any()) } returns 5120L
        every { mockInputStream.close() } just Runs
        
        // Mock file operations
        mockkStatic(File::class)
        val mockFile = mockk<File>()
        every { File(any<File>(), any<String>()) } returns mockFile
        every { mockFile.mkdirs() } returns true
        every { mockFile.exists() } returns false
        every { File.createTempFile(any(), any(), any()) } returns mockFile
        every { mockFile.absolutePath } returns "/mock/path/test_video.mp4"
        
        // Mock FileOutputStream
        mockkConstructor(java.io.FileOutputStream::class)
        every { anyConstructed<java.io.FileOutputStream>().close() } just Runs
        
        // When
        val result = mediaHandler.processMedia(mockUri)
        
        // Then
        assertTrue(result.isSuccess)
        val mediaMessage = result.getOrNull()
        assertNotNull(mediaMessage)
        assertEquals("test_video.mp4", mediaMessage?.fileName)
        assertEquals("video/mp4", mediaMessage?.mimeType)
        assertEquals(5120L, mediaMessage?.fileSize)
    }
    
    @Test
    fun `processMedia should handle document URI successfully`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        val mockInputStream = mockk<InputStream>()
        
        // Mock content resolver behavior
        val contentResolver = mockContext.contentResolver
        every { contentResolver.getType(mockUri) } returns "application/pdf"
        every { contentResolver.query(any(), any(), any(), any(), any()) } returns mockk {
            every { moveToFirst() } returns true
            every { getColumnIndex(any()) } returns 0
            every { getString(0) } returns "document.pdf"
            every { getLong(0) } returns 2048L
            every { close() } just Runs
        }
        every { contentResolver.openInputStream(mockUri) } returns mockInputStream
        every { mockInputStream.copyTo(any()) } returns 2048L
        every { mockInputStream.close() } just Runs
        
        // Mock file operations
        mockkStatic(File::class)
        val mockFile = mockk<File>()
        every { File(any<File>(), any<String>()) } returns mockFile
        every { mockFile.mkdirs() } returns true
        every { mockFile.exists() } returns false
        every { File.createTempFile(any(), any(), any()) } returns mockFile
        every { mockFile.absolutePath } returns "/mock/path/document.pdf"
        
        // Mock FileOutputStream
        mockkConstructor(java.io.FileOutputStream::class)
        every { anyConstructed<java.io.FileOutputStream>().close() } just Runs
        
        // When
        val result = mediaHandler.processMedia(mockUri)
        
        // Then
        assertTrue(result.isSuccess)
        val mediaMessage = result.getOrNull()
        assertNotNull(mediaMessage)
        assertEquals("document.pdf", mediaMessage?.fileName)
        assertEquals("application/pdf", mediaMessage?.mimeType)
        assertEquals(2048L, mediaMessage?.fileSize)
    }
    
    @Test
    fun `processMedia should handle failure gracefully`() = runTest {
        // Given
        val mockUri = mockk<Uri>()
        
        // Mock content resolver to throw exception
        val contentResolver = mockContext.contentResolver
        every { contentResolver.getType(mockUri) } throws RuntimeException("Network error")
        
        // When
        val result = mediaHandler.processMedia(mockUri)
        
        // Then
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }
    
    @Test
    fun `deleteMedia should delete local file successfully`() = runTest {
        // Given
        val mediaMessage = com.chain.messaging.domain.model.MediaMessage(
            uri = "/mock/path/test.jpg",
            fileName = "test.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024L,
            isLocal = true
        )
        
        // Mock file operations
        mockkStatic(File::class)
        val mockFile = mockk<File>()
        every { File(mediaMessage.uri) } returns mockFile
        every { mockFile.exists() } returns true
        every { mockFile.delete() } returns true
        
        // When
        val result = mediaHandler.deleteMedia(mediaMessage)
        
        // Then
        assertTrue(result.isSuccess)
        verify { mockFile.delete() }
    }
    
    @Test
    fun `getFile should return file for local media`() {
        // Given
        val mediaMessage = com.chain.messaging.domain.model.MediaMessage(
            uri = "/mock/path/test.jpg",
            fileName = "test.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024L,
            isLocal = true
        )
        
        // Mock file operations
        mockkStatic(File::class)
        val mockFile = mockk<File>()
        every { File(mediaMessage.uri) } returns mockFile
        every { mockFile.exists() } returns true
        
        // When
        val result = mediaHandler.getFile(mediaMessage)
        
        // Then
        assertNotNull(result)
        assertEquals(mockFile, result)
    }
    
    @Test
    fun `getFile should return null for non-local media`() {
        // Given
        val mediaMessage = com.chain.messaging.domain.model.MediaMessage(
            uri = "https://example.com/image.jpg",
            fileName = "image.jpg",
            mimeType = "image/jpeg",
            fileSize = 1024L,
            isLocal = false
        )
        
        // When
        val result = mediaHandler.getFile(mediaMessage)
        
        // Then
        assertNull(result)
    }
}