package com.chain.messaging.core.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for ProfileImageManager
 */
@RunWith(RobolectricTestRunner::class)
class ProfileImageManagerTest {
    
    private lateinit var context: Context
    private lateinit var profileImageManager: ProfileImageManager
    private lateinit var testImageFile: File
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        profileImageManager = ProfileImageManager(context)
        
        // Create a test image file
        testImageFile = File(context.cacheDir, "test_image.jpg")
        createTestImage(testImageFile)
    }
    
    private fun createTestImage(file: File) {
        // Create a simple test bitmap
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.BLUE)
        
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        }
        bitmap.recycle()
    }
    
    @Test
    fun `saveProfileImage should process and save image successfully`() = runTest {
        // Given
        val userId = "test_user_123"
        val imageUri = Uri.fromFile(testImageFile)
        
        // When
        val result = profileImageManager.saveProfileImage(imageUri, userId)
        
        // Then
        assertTrue(result.isSuccess)
        val savedPath = result.getOrNull()
        assertNotNull(savedPath)
        
        val savedFile = File(savedPath)
        assertTrue(savedFile.exists())
        assertTrue(savedFile.name.startsWith("profile_${userId}_"))
        assertTrue(savedFile.name.endsWith(".jpg"))
    }
    
    @Test
    fun `getProfileImageFile should return existing file`() = runTest {
        // Given
        val userId = "test_user_456"
        val imageUri = Uri.fromFile(testImageFile)
        val saveResult = profileImageManager.saveProfileImage(imageUri, userId)
        val savedPath = saveResult.getOrNull()!!
        
        // When
        val retrievedFile = profileImageManager.getProfileImageFile(savedPath)
        
        // Then
        assertNotNull(retrievedFile)
        assertTrue(retrievedFile.exists())
        assertEquals(savedPath, retrievedFile.absolutePath)
    }
    
    @Test
    fun `getProfileImageFile should return null for non-existent file`() {
        // Given
        val nonExistentPath = "/non/existent/path/image.jpg"
        
        // When
        val result = profileImageManager.getProfileImageFile(nonExistentPath)
        
        // Then
        assertEquals(null, result)
    }
    
    @Test
    fun `deleteProfileImage should remove file successfully`() = runTest {
        // Given
        val userId = "test_user_789"
        val imageUri = Uri.fromFile(testImageFile)
        val saveResult = profileImageManager.saveProfileImage(imageUri, userId)
        val savedPath = saveResult.getOrNull()!!
        val savedFile = File(savedPath)
        assertTrue(savedFile.exists())
        
        // When
        val deleteResult = profileImageManager.deleteProfileImage(savedPath)
        
        // Then
        assertTrue(deleteResult.isSuccess)
        assertTrue(!savedFile.exists())
    }
    
    @Test
    fun `getProfileImageUri should return valid URI for existing file`() = runTest {
        // Given
        val userId = "test_user_uri"
        val imageUri = Uri.fromFile(testImageFile)
        val saveResult = profileImageManager.saveProfileImage(imageUri, userId)
        val savedPath = saveResult.getOrNull()!!
        
        // When
        val uri = profileImageManager.getProfileImageUri(savedPath)
        
        // Then
        assertNotNull(uri)
        assertEquals("file", uri.scheme)
        assertTrue(uri.path?.endsWith(".jpg") == true)
    }
}