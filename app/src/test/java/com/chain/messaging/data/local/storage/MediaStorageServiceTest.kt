package com.chain.messaging.data.local.storage

import android.content.Context
import android.net.Uri
import com.chain.messaging.data.local.dao.MediaDao
import com.chain.messaging.data.local.entity.MediaEntity
import com.chain.messaging.core.security.FileEncryption
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class MediaStorageServiceTest {
    
    private lateinit var context: Context
    private lateinit var mediaDao: MediaDao
    private lateinit var fileEncryption: FileEncryption
    private lateinit var mediaCompressor: MediaCompressor
    private lateinit var thumbnailGenerator: ThumbnailGenerator
    private lateinit var mediaStorageService: MediaStorageService
    
    private lateinit var mockSourceUri: Uri
    private lateinit var mockMediaFile: File
    private lateinit var mockTempDir: File
    
    @Before
    fun setup() {
        context = mockk()
        mediaDao = mockk()
        fileEncryption = mockk()
        mediaCompressor = mockk()
        thumbnailGenerator = mockk()
        
        mockSourceUri = mockk()
        mockMediaFile = mockk()
        mockTempDir = mockk()
        
        // Mock context.filesDir
        val filesDir = mockk<File>()
        every { context.filesDir } returns filesDir
        every { filesDir.freeSpace } returns 1000000L
        
        // Mock cache dir
        val cacheDir = mockk<File>()
        every { context.cacheDir } returns cacheDir
        
        // Mock directory creation
        every { any<File>().mkdirs() } returns true
        every { any<File>().exists() } returns true
        every { any<File>().delete() } returns true
        every { any<File>().length() } returns 1000L
        every { any<File>().absolutePath } returns "/mock/path"
        
        mediaStorageService = MediaStorageService(
            context,
            mediaDao,
            fileEncryption,
            mediaCompressor,
            thumbnailGenerator
        )
    }
    
    @Test
    fun `storeMedia should compress, encrypt and store media file`() = runTest {
        // Given
        val messageId = "msg1"
        val fileName = "test.jpg"
        val mimeType = "image/jpeg"
        val encryptionKey = "test_key"
        val thumbnailPath = "/mock/thumbnail/path"
        
        val mockInputStream = mockk<InputStream>()
        val mockContentResolver = mockk<android.content.ContentResolver>()
        
        every { context.contentResolver } returns mockContentResolver
        every { mockContentResolver.openInputStream(mockSourceUri) } returns mockInputStream
        every { mockInputStream.copyTo(any()) } just Runs
        every { mockInputStream.close() } just Runs
        
        coEvery { mediaCompressor.compressMedia(mockSourceUri, mimeType, any()) } returns mockMediaFile
        every { fileEncryption.encryptFile(mockMediaFile, any()) } returns encryptionKey
        coEvery { thumbnailGenerator.generateThumbnail(mockMediaFile, any(), mimeType) } returns true
        coEvery { mediaDao.insertMedia(any()) } just Runs
        
        // When
        val result = mediaStorageService.storeMedia(messageId, mockSourceUri, fileName, mimeType)
        
        // Then
        assertTrue(result.isSuccess)
        val media = result.getOrNull()!!
        assertEquals(messageId, media.messageId)
        assertEquals(fileName, media.fileName)
        assertEquals(mimeType, media.mimeType)
        assertEquals(encryptionKey, media.encryptionKey)
        assertTrue(media.isEncrypted)
        
        coVerify { mediaCompressor.compressMedia(mockSourceUri, mimeType, any()) }
        verify { fileEncryption.encryptFile(mockMediaFile, any()) }
        coVerify { thumbnailGenerator.generateThumbnail(mockMediaFile, any(), mimeType) }
        coVerify { mediaDao.insertMedia(any()) }
    }
    
    @Test
    fun `storeMedia should skip compression when compress is false`() = runTest {
        // Given
        val messageId = "msg1"
        val fileName = "test.pdf"
        val mimeType = "application/pdf"
        val encryptionKey = "test_key"
        
        val mockInputStream = mockk<InputStream>()
        val mockContentResolver = mockk<android.content.ContentResolver>()
        
        every { context.contentResolver } returns mockContentResolver
        every { mockContentResolver.openInputStream(mockSourceUri) } returns mockInputStream
        every { mockInputStream.copyTo(any()) } just Runs
        every { mockInputStream.close() } just Runs
        
        every { fileEncryption.encryptFile(any(), any()) } returns encryptionKey
        coEvery { mediaDao.insertMedia(any()) } just Runs
        
        // When
        val result = mediaStorageService.storeMedia(messageId, mockSourceUri, fileName, mimeType, compress = false)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { mediaCompressor.compressMedia(any(), any(), any()) }
        coVerify(exactly = 0) { thumbnailGenerator.generateThumbnail(any(), any(), any()) }
    }
    
    @Test
    fun `getMedia should decrypt and return file`() = runTest {
        // Given
        val mediaId = "media1"
        val encryptionKey = "test_key"
        val mediaEntity = MediaEntity(
            id = mediaId,
            messageId = "msg1",
            fileName = "test.jpg",
            filePath = "/encrypted/path",
            mimeType = "image/jpeg",
            fileSize = 1000L,
            isEncrypted = true,
            encryptionKey = encryptionKey
        )
        
        val encryptedFile = mockk<File>()
        val decryptedFile = mockk<File>()
        
        coEvery { mediaDao.getMediaById(mediaId) } returns mediaEntity
        every { encryptedFile.exists() } returns true
        every { fileEncryption.decryptFile(encryptedFile, any(), encryptionKey) } returns true
        
        // Mock File constructor
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        
        // When
        val result = mediaStorageService.getMedia(mediaId)
        
        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        
        coVerify { mediaDao.getMediaById(mediaId) }
        verify { fileEncryption.decryptFile(any(), any(), encryptionKey) }
    }
    
    @Test
    fun `getMedia should return null for non-existent media`() = runTest {
        // Given
        val mediaId = "non_existent"
        coEvery { mediaDao.getMediaById(mediaId) } returns null
        
        // When
        val result = mediaStorageService.getMedia(mediaId)
        
        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
        
        coVerify { mediaDao.getMediaById(mediaId) }
        verify(exactly = 0) { fileEncryption.decryptFile(any(), any(), any()) }
    }
    
    @Test
    fun `deleteMedia should remove file, thumbnail and database entry`() = runTest {
        // Given
        val mediaId = "media1"
        val thumbnailPath = "/thumbnail/path"
        val mediaEntity = MediaEntity(
            id = mediaId,
            messageId = "msg1",
            fileName = "test.jpg",
            filePath = "/media/path",
            mimeType = "image/jpeg",
            fileSize = 1000L,
            thumbnailPath = thumbnailPath
        )
        
        val mediaFile = mockk<File>()
        val thumbnailFile = mockk<File>()
        
        coEvery { mediaDao.getMediaById(mediaId) } returns mediaEntity
        every { mediaFile.exists() } returns true
        every { mediaFile.delete() } returns true
        every { thumbnailFile.exists() } returns true
        every { thumbnailFile.delete() } returns true
        coEvery { mediaDao.deleteMediaById(mediaId) } just Runs
        
        // Mock File constructor
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().delete() } returns true
        
        // When
        val result = mediaStorageService.deleteMedia(mediaId)
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { mediaDao.getMediaById(mediaId) }
        coVerify { mediaDao.deleteMediaById(mediaId) }
    }
    
    @Test
    fun `getStorageStats should return correct statistics`() = runTest {
        // Given
        val totalSize = 50000L
        val mediaCount = 10
        
        coEvery { mediaDao.getTotalStorageUsed() } returns totalSize
        coEvery { mediaDao.getMediaCount() } returns mediaCount
        
        // When
        val result = mediaStorageService.getStorageStats()
        
        // Then
        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertEquals(totalSize, stats.totalSizeBytes)
        assertEquals(mediaCount, stats.totalFiles)
        assertTrue(stats.availableSpaceBytes > 0)
        assertTrue(stats.maxStorageSizeBytes > 0)
        
        coVerify { mediaDao.getTotalStorageUsed() }
        coVerify { mediaDao.getMediaCount() }
    }
    
    @Test
    fun `cleanupOldMedia should delete old files and return cleanup result`() = runTest {
        // Given
        val olderThanDays = 30
        val oldMediaEntity = MediaEntity(
            id = "old_media",
            messageId = "msg1",
            fileName = "old.jpg",
            filePath = "/old/path",
            mimeType = "image/jpeg",
            fileSize = 1000L,
            thumbnailPath = "/old/thumbnail"
        )
        
        coEvery { mediaDao.getOldMediaFiles(any()) } returns listOf(oldMediaEntity)
        coEvery { mediaDao.deleteOldMedia(any()) } returns 1
        
        // Mock File operations
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { anyConstructed<File>().length() } returns 1000L
        every { anyConstructed<File>().delete() } returns true
        
        // When
        val result = mediaStorageService.cleanupOldMedia(olderThanDays)
        
        // Then
        assertTrue(result.isSuccess)
        val cleanupResult = result.getOrNull()!!
        assertEquals(1, cleanupResult.deletedFiles)
        assertEquals(1000L, cleanupResult.freedSpaceBytes)
        assertEquals(1, cleanupResult.deletedFromDatabase)
        
        coVerify { mediaDao.getOldMediaFiles(any()) }
        coVerify { mediaDao.deleteOldMedia(any()) }
    }
    
    @Test
    fun `getMediaForMessage should return all media for message`() = runTest {
        // Given
        val messageId = "msg1"
        val mediaEntity1 = MediaEntity(
            id = "media1",
            messageId = messageId,
            fileName = "test1.jpg",
            filePath = "/path1",
            mimeType = "image/jpeg",
            fileSize = 1000L
        )
        val mediaEntity2 = MediaEntity(
            id = "media2",
            messageId = messageId,
            fileName = "test2.jpg",
            filePath = "/path2",
            mimeType = "image/jpeg",
            fileSize = 2000L
        )
        
        coEvery { mediaDao.getMediaByMessageId(messageId) } returns listOf(mediaEntity1, mediaEntity2)
        
        // When
        val result = mediaStorageService.getMediaForMessage(messageId)
        
        // Then
        assertTrue(result.isSuccess)
        val mediaList = result.getOrNull()!!
        assertEquals(2, mediaList.size)
        assertEquals("media1", mediaList[0].id)
        assertEquals("media2", mediaList[1].id)
        
        coVerify { mediaDao.getMediaByMessageId(messageId) }
    }
    
    @Test
    fun `storeMedia should handle encryption failure`() = runTest {
        // Given
        val messageId = "msg1"
        val fileName = "test.jpg"
        val mimeType = "image/jpeg"
        
        val mockInputStream = mockk<InputStream>()
        val mockContentResolver = mockk<android.content.ContentResolver>()
        
        every { context.contentResolver } returns mockContentResolver
        every { mockContentResolver.openInputStream(mockSourceUri) } returns mockInputStream
        every { mockInputStream.copyTo(any()) } just Runs
        every { mockInputStream.close() } just Runs
        
        coEvery { mediaCompressor.compressMedia(mockSourceUri, mimeType, any()) } returns mockMediaFile
        every { fileEncryption.encryptFile(mockMediaFile, any()) } throws RuntimeException("Encryption failed")
        
        // When
        val result = mediaStorageService.storeMedia(messageId, mockSourceUri, fileName, mimeType)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RuntimeException)
    }
}