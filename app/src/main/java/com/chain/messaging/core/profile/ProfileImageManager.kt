package com.chain.messaging.core.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages profile image processing, storage, and retrieval
 */
@Singleton
class ProfileImageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val PROFILE_IMAGES_DIR = "profile_images"
        private const val MAX_IMAGE_SIZE = 512 // Maximum width/height in pixels
        private const val JPEG_QUALITY = 85 // JPEG compression quality
    }
    
    private val profileImagesDir: File by lazy {
        File(context.filesDir, PROFILE_IMAGES_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    /**
     * Processes and saves a profile image from the given URI
     * @param imageUri The URI of the selected image
     * @param userId The user ID to associate with the image
     * @return The file path of the saved image, or null if processing failed
     */
    suspend fun saveProfileImage(imageUri: Uri, userId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Read the image from URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext Result.failure(IOException("Could not open image"))
            
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                ?: return@withContext Result.failure(IOException("Could not decode image"))
            
            inputStream.close()
            
            // Process the image (resize, rotate if needed)
            val processedBitmap = processImage(imageUri, originalBitmap)
            
            // Generate unique filename
            val filename = "profile_${userId}_${UUID.randomUUID()}.jpg"
            val outputFile = File(profileImagesDir, filename)
            
            // Save the processed image
            FileOutputStream(outputFile).use { outputStream ->
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
            }
            
            // Clean up
            if (processedBitmap != originalBitmap) {
                processedBitmap.recycle()
            }
            originalBitmap.recycle()
            
            // Delete old profile image if exists
            deleteOldProfileImage(userId, filename)
            
            Result.success(outputFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Processes the image by resizing and correcting orientation
     */
    private suspend fun processImage(imageUri: Uri, bitmap: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        var processedBitmap = bitmap
        
        // Correct orientation based on EXIF data
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            inputStream?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                
                val rotationDegrees = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
                
                if (rotationDegrees != 0f) {
                    val matrix = Matrix().apply { postRotate(rotationDegrees) }
                    processedBitmap = Bitmap.createBitmap(
                        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                    )
                }
            }
        } catch (e: Exception) {
            // If EXIF reading fails, continue with original bitmap
        }
        
        // Resize if necessary
        val width = processedBitmap.width
        val height = processedBitmap.height
        
        if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
            val scaleFactor = if (width > height) {
                MAX_IMAGE_SIZE.toFloat() / width
            } else {
                MAX_IMAGE_SIZE.toFloat() / height
            }
            
            val newWidth = (width * scaleFactor).toInt()
            val newHeight = (height * scaleFactor).toInt()
            
            val resizedBitmap = Bitmap.createScaledBitmap(processedBitmap, newWidth, newHeight, true)
            
            if (resizedBitmap != processedBitmap && processedBitmap != bitmap) {
                processedBitmap.recycle()
            }
            
            processedBitmap = resizedBitmap
        }
        
        processedBitmap
    }
    
    /**
     * Deletes old profile images for the user (keeps only the latest)
     */
    private fun deleteOldProfileImage(userId: String, currentFilename: String) {
        try {
            profileImagesDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("profile_${userId}_") && file.name != currentFilename) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Log error but don't fail the operation
        }
    }
    
    /**
     * Gets the profile image file for a user
     * @param imagePath The stored image path
     * @return The File object, or null if not found
     */
    fun getProfileImageFile(imagePath: String): File? {
        return try {
            val file = File(imagePath)
            if (file.exists()) file else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Deletes a profile image
     * @param imagePath The path of the image to delete
     */
    suspend fun deleteProfileImage(imagePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets the URI for a profile image file
     */
    fun getProfileImageUri(imagePath: String): Uri? {
        return try {
            val file = File(imagePath)
            if (file.exists()) {
                Uri.fromFile(file)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}