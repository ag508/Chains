package com.chain.messaging.integration

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.chain.messaging.core.profile.ProfileImageManager
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.FileOutputStream
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test for profile image picker functionality
 */
@RunWith(RobolectricTestRunner::class)
class ProfileImagePickerIntegrationTest {
    
    private lateinit var context: Context
    private lateinit var profileImageManager: ProfileImageManager
    private lateinit var testImageFile: File
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        profileImageManager = ProfileImageManager(context)
        
        // Create a test image file
        testImageFile = File(context.cacheDir, "test_profile_image.jpg")
        createTestImage(testImageFile)
    }
    
    private fun createTestImage(file: File) {
        // Create a simple test bitmap
        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.BLUE)
        
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        }
        bitmap.recycle()
    }
    
    @Test
    fun `complete image picker workflow should work end to end`() = runTest {
        // Given
        val userId = "test_user_profile_picker"
        val imageUri = Uri.fromFile(testImageFile)
        
        // When - Save profile image (simulating image picker selection)
        val saveResult = profileImageManager.saveProfileImage(imageUri, userId)
        
        // Then - Image should be saved successfully
        assertTrue(saveResult.isSuccess)
        val savedPath = saveResult.getOrNull()
        assertNotNull(savedPath)
        
        // And - Saved image file should exist
        val savedFile = profileImageManager.getProfileImageFile(savedPath)
        assertNotNull(savedFile)
        assertTrue(savedFile.exists())
        
        // And - Should be able to get URI for the saved image
        val savedUri = profileImageManager.getProfileImageUri(savedPath)
        assertNotNull(savedUri)
        
        // And - Should be able to delete the image
        val deleteResult = profileImageManager.deleteProfileImage(savedPath)
        assertTrue(deleteResult.isSuccess)
        assertTrue(!savedFile.exists())
    }
    
    @Test
    fun `image processing should handle large images correctly`() = runTest {
        // Given - Create a large test image
        val largeImageFile = File(context.cacheDir, "large_test_image.jpg")
        val largeBitmap = Bitmap.createBitmap(2000, 2000, Bitmap.Config.ARGB_8888)
        largeBitmap.eraseColor(android.graphics.Color.RED)
        
        FileOutputStream(largeImageFile).use { outputStream ->
            largeBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        }
        largeBitmap.recycle()
        
        val userId = "test_user_large_image"
        val imageUri = Uri.fromFile(largeImageFile)
        
        // When - Save the large image
        val saveResult = profileImageManager.saveProfileImage(imageUri, userId)
        
        // Then - Should process and save successfully
        assertTrue(saveResult.isSuccess)
        val savedPath = saveResult.getOrNull()
        assertNotNull(savedPath)
        
        // And - Saved file should exist and be smaller than original
        val savedFile = profileImageManager.getProfileImageFile(savedPath)
        assertNotNull(savedFile)
        assertTrue(savedFile.exists())
        assertTrue(savedFile.length() < largeImageFile.length())
        
        // Cleanup
        largeImageFile.delete()
        profileImageManager.deleteProfileImage(savedPath)
    }
    
    @Test
    fun `should handle multiple profile images for same user correctly`() = runTest {
        // Given
        val userId = "test_user_multiple_images"
        val imageUri1 = Uri.fromFile(testImageFile)
        
        // When - Save first image
        val saveResult1 = profileImageManager.saveProfileImage(imageUri1, userId)
        assertTrue(saveResult1.isSuccess)
        val savedPath1 = saveResult1.getOrNull()!!
        val savedFile1 = profileImageManager.getProfileImageFile(savedPath1)!!
        assertTrue(savedFile1.exists())
        
        // And - Save second image for same user
        val saveResult2 = profileImageManager.saveProfileImage(imageUri1, userId)
        assertTrue(saveResult2.isSuccess)
        val savedPath2 = saveResult2.getOrNull()!!
        val savedFile2 = profileImageManager.getProfileImageFile(savedPath2)!!
        
        // Then - Second image should exist
        assertTrue(savedFile2.exists())
        
        // And - First image should be deleted (old image cleanup)
        assertTrue(!savedFile1.exists())
        
        // Cleanup
        profileImageManager.deleteProfileImage(savedPath2)
    }
}