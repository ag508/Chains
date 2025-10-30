package com.chain.messaging.data.local.storage

import android.content.Context
import android.content.ContentResolver
import android.net.Uri
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

class MediaCompressorTest {
    
    private lateinit var context: Context
    private lateinit var contentResolver: ContentResolver
    private lateinit var mediaCompressor: MediaCompressor
    
    private lateinit var mockSourceUri: Uri
    private lateinit var mockOutputDir: File
    private lateinit var mockInputStream: InputStream
    
    @Before
    fun setup() {
        context = mockk()
        contentResolver = mockk()
        mockSourceUri = mockk()
        mockOutputDir = mockk()
        mockInputStream = mockk()
        
        every { context.contentResolver } returns contentResolver
        every { context.cacheDir } returns mockk<File>().apply {
            every { absolutePath } returns "/cache"
        }
        
        // Mock output directory
        every { mockOutputDir.exists() } returns true
        every { mockOutputDir.mkdirs() } returns true
        
        mediaCompressor = MediaCompressor(context)
    }
    
    @Test
    fun `compressMedia should compress image files`() = runTest {
        // Given
        val mimeType = "image/jpeg"
        val outputFile = mockk<File>()
        
        every { contentResolver.openInputStream(mockSourceUri) } returns mockInputStream
        every { mockInputStream.available() } returns 1024
        every { mockInputStream.copyTo(any()) } just Runs
        every { mockInputStream.close() } just Runs
        
        // Mock File constructor and operations
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().length() } returns 512L
        
        mockkConstructor(FileOutputStream::class)
        every { anyConstructed<FileOutputStream>().close() } just Runs
        
        // Mock BitmapFactory operations (would need more detailed mocking in real test)
        mockkStatic("android.graphics.BitmapFactory")
        
        // When
        val result = mediaCompressor.compressMedia(mockSourceUri, mimeType, mockOutputDir)
        
        // Then
        assertNotNull(result)
        verify { contentResolver.openInputStream(mockSourceUri) }
    }
    
    @Test
    fun `compressMedia should handle video files`() = runTest {
        // Given
        val mimeType = "video/mp4"
        
        every { contentResolver.openInputStream(mockSourceUri) } returns mockInputStream
        every { mockInputStream.copyTo(any()) } just Runs
        every { mockInputStream.close() } just Runs
        
        // Mock File operations
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().delete() } returns true
        every { anyConstructed<File>().length() } returns 1024L
        
        mockkConstructor(FileOutputStream::class)
        every { anyConstructed<FileOutputStream>().close() } just Runs
        
        // When
        val result = mediaCompressor.compressMedia(mockSourceUri, mimeType, mockOutputDir)
        
        // Then
        assertNotNull(result)
        verify { contentResolver.openInputStream(mockSourceUri) }
    }
    
    @Test
    fun `compressMedia should copy non-media files without compression`() = runTest {
        // Given
        val mimeType = "application/pdf"
        
        every { contentResolver.openInputStream(mockSourceUri) } returns mockInputStream
        every { mockInputStream.copyTo(any()) } just Runs
        every { mockInputStream.close() } just Runs
        
        // Mock File operations
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        
        mockkConstructor(FileOutputStream::class)
        every { anyConstructed<FileOutputStream>().close() } just Runs
        
        // When
        val result = mediaCompressor.compressMedia(mockSourceUri, mimeType, mockOutputDir)
        
        // Then
        assertNotNull(result)
        verify { contentResolver.openInputStream(mockSourceUri) }
    }
    
    @Test
    fun `shouldCompress should return true for large images`() {
        // Given
        val mimeType = "image/jpeg"
        val largeFileSize = 2 * 1024 * 1024L // 2MB
        
        // When
        val result = mediaCompressor.shouldCompress(mimeType, largeFileSize)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `shouldCompress should return false for small images`() {
        // Given
        val mimeType = "image/jpeg"
        val smallFileSize = 500 * 1024L // 500KB
        
        // When
        val result = mediaCompressor.shouldCompress(mimeType, smallFileSize)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `shouldCompress should return true for large videos`() {
        // Given
        val mimeType = "video/mp4"
        val largeFileSize = 20 * 1024 * 1024L // 20MB
        
        // When
        val result = mediaCompressor.shouldCompress(mimeType, largeFileSize)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `shouldCompress should return false for small videos`() {
        // Given
        val mimeType = "video/mp4"
        val smallFileSize = 5 * 1024 * 1024L // 5MB
        
        // When
        val result = mediaCompressor.shouldCompress(mimeType, smallFileSize)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `shouldCompress should return false for non-media files`() {
        // Given
        val mimeType = "application/pdf"
        val largeFileSize = 50 * 1024 * 1024L // 50MB
        
        // When
        val result = mediaCompressor.shouldCompress(mimeType, largeFileSize)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `getEstimatedCompressedSize should return correct estimates`() {
        // Given
        val originalSize = 10 * 1024 * 1024L // 10MB
        
        // When & Then
        val imageEstimate = mediaCompressor.getEstimatedCompressedSize("image/jpeg", originalSize)
        assertEquals((originalSize * 0.3).toLong(), imageEstimate)
        
        val videoEstimate = mediaCompressor.getEstimatedCompressedSize("video/mp4", originalSize)
        assertEquals((originalSize * 0.4).toLong(), videoEstimate)
        
        val otherEstimate = mediaCompressor.getEstimatedCompressedSize("application/pdf", originalSize)
        assertEquals(originalSize, otherEstimate)
    }
    
    @Test
    fun `getCompressionRatio should calculate correct ratio`() = runTest {
        // Given
        val originalSize = 1000L
        val compressedFile = mockk<File>()
        
        every { contentResolver.openInputStream(mockSourceUri) } returns mockInputStream
        every { mockInputStream.available() } returns originalSize.toInt()
        every { mockInputStream.close() } just Runs
        every { compressedFile.length() } returns 300L
        
        // When
        val ratio = mediaCompressor.getCompressionRatio(mockSourceUri, compressedFile)
        
        // Then
        assertEquals(0.3f, ratio, 0.01f)
    }
    
    @Test
    fun `getCompressionRatio should handle errors gracefully`() = runTest {
        // Given
        val compressedFile = mockk<File>()
        
        every { contentResolver.openInputStream(mockSourceUri) } throws RuntimeException("IO Error")
        every { compressedFile.length() } returns 300L
        
        // When
        val ratio = mediaCompressor.getCompressionRatio(mockSourceUri, compressedFile)
        
        // Then
        assertEquals(1.0f, ratio, 0.01f)
    }
    
    @Test
    fun `calculateVideoSize should maintain aspect ratio for landscape videos`() {
        // Given
        val originalWidth = 1920
        val originalHeight = 1080
        
        // When
        val (newWidth, newHeight) = mediaCompressor.calculateVideoSize(originalWidth, originalHeight)
        
        // Then
        assertEquals(1280, newWidth)
        assertEquals(720, newHeight)
        
        // Check aspect ratio is maintained
        val originalRatio = originalWidth.toFloat() / originalHeight.toFloat()
        val newRatio = newWidth.toFloat() / newHeight.toFloat()
        assertEquals(originalRatio, newRatio, 0.01f)
    }
    
    @Test
    fun `calculateVideoSize should maintain aspect ratio for portrait videos`() {
        // Given
        val originalWidth = 1080
        val originalHeight = 1920
        
        // When
        val (newWidth, newHeight) = mediaCompressor.calculateVideoSize(originalWidth, originalHeight)
        
        // Then
        assertTrue(newWidth <= 1280)
        assertTrue(newHeight <= 720)
        
        // Check aspect ratio is maintained
        val originalRatio = originalWidth.toFloat() / originalHeight.toFloat()
        val newRatio = newWidth.toFloat() / newHeight.toFloat()
        assertEquals(originalRatio, newRatio, 0.01f)
    }
    
    @Test
    fun `calculateVideoSize should return original size if already within limits`() {
        // Given
        val originalWidth = 640
        val originalHeight = 480
        
        // When
        val (newWidth, newHeight) = mediaCompressor.calculateVideoSize(originalWidth, originalHeight)
        
        // Then
        assertEquals(originalWidth, newWidth)
        assertEquals(originalHeight, newHeight)
    }
    
    @Test
    fun `calculateVideoSize should ensure even dimensions`() {
        // Given
        val originalWidth = 1921 // Odd number
        val originalHeight = 1081 // Odd number
        
        // When
        val (newWidth, newHeight) = mediaCompressor.calculateVideoSize(originalWidth, originalHeight)
        
        // Then
        assertEquals(0, newWidth % 2) // Should be even
        assertEquals(0, newHeight % 2) // Should be even
    }
    
    @Test
    fun `getVideoMetadata should return null on error`() = runTest {
        // Given
        every { contentResolver.openFileDescriptor(mockSourceUri, "r") } throws RuntimeException("Error")
        
        // When
        val metadata = mediaCompressor.getVideoMetadata(mockSourceUri)
        
        // Then
        assertNull(metadata)
    }
}