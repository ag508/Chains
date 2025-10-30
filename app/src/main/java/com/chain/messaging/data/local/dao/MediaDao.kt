package com.chain.messaging.data.local.dao

import androidx.room.*
import com.chain.messaging.data.local.entity.MediaEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Media operations
 */
@Dao
interface MediaDao {
    
    /**
     * Get media by ID
     */
    @Query("SELECT * FROM media WHERE id = :mediaId")
    suspend fun getMediaById(mediaId: String): MediaEntity?
    
    /**
     * Get all media for a message
     */
    @Query("SELECT * FROM media WHERE messageId = :messageId ORDER BY createdAt ASC")
    suspend fun getMediaByMessageId(messageId: String): List<MediaEntity>
    
    /**
     * Get all media for a message as Flow
     */
    @Query("SELECT * FROM media WHERE messageId = :messageId ORDER BY createdAt ASC")
    fun observeMediaByMessageId(messageId: String): Flow<List<MediaEntity>>
    
    /**
     * Get all media files
     */
    @Query("SELECT * FROM media ORDER BY createdAt DESC")
    suspend fun getAllMedia(): List<MediaEntity>
    
    /**
     * Get media files by type (using MIME type pattern)
     */
    @Query("SELECT * FROM media WHERE mimeType LIKE :mimeTypePattern ORDER BY createdAt DESC")
    suspend fun getMediaByType(mimeTypePattern: String): List<MediaEntity>
    
    /**
     * Get media files that need to be uploaded
     */
    @Query("SELECT * FROM media WHERE isUploaded = 0 AND cloudUrl IS NULL ORDER BY createdAt ASC")
    suspend fun getPendingUploads(): List<MediaEntity>
    
    /**
     * Get media files by cloud service
     */
    @Query("SELECT * FROM media WHERE cloudService = :service ORDER BY createdAt DESC")
    suspend fun getMediaByCloudService(service: String): List<MediaEntity>
    
    /**
     * Get total storage used by media files
     */
    @Query("SELECT SUM(fileSize) FROM media")
    suspend fun getTotalStorageUsed(): Long?
    
    /**
     * Get storage used by cloud service
     */
    @Query("SELECT SUM(fileSize) FROM media WHERE cloudService = :service")
    suspend fun getStorageUsedByService(service: String): Long?
    
    /**
     * Get media files larger than specified size
     */
    @Query("SELECT * FROM media WHERE fileSize > :sizeBytes ORDER BY fileSize DESC")
    suspend fun getLargeMediaFiles(sizeBytes: Long): List<MediaEntity>
    
    /**
     * Get media files older than specified timestamp
     */
    @Query("SELECT * FROM media WHERE createdAt < :timestamp ORDER BY createdAt ASC")
    suspend fun getOldMediaFiles(timestamp: Long): List<MediaEntity>
    
    /**
     * Insert media
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity)
    
    /**
     * Insert multiple media files
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: List<MediaEntity>)
    
    /**
     * Update media
     */
    @Update
    suspend fun updateMedia(media: MediaEntity)
    
    /**
     * Update upload progress
     */
    @Query("UPDATE media SET uploadProgress = :progress, updatedAt = :updatedAt WHERE id = :mediaId")
    suspend fun updateUploadProgress(mediaId: String, progress: Int, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Mark media as uploaded
     */
    @Query("UPDATE media SET isUploaded = 1, cloudUrl = :cloudUrl, cloudService = :service, uploadProgress = 100, updatedAt = :updatedAt WHERE id = :mediaId")
    suspend fun markAsUploaded(mediaId: String, cloudUrl: String, service: String, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Update cloud information
     */
    @Query("UPDATE media SET cloudUrl = :cloudUrl, cloudService = :service, updatedAt = :updatedAt WHERE id = :mediaId")
    suspend fun updateCloudInfo(mediaId: String, cloudUrl: String?, service: String?, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * Delete media
     */
    @Delete
    suspend fun deleteMedia(media: MediaEntity)
    
    /**
     * Delete media by ID
     */
    @Query("DELETE FROM media WHERE id = :mediaId")
    suspend fun deleteMediaById(mediaId: String)
    
    /**
     * Delete media by message ID
     */
    @Query("DELETE FROM media WHERE messageId = :messageId")
    suspend fun deleteMediaByMessageId(messageId: String)
    
    /**
     * Delete media files older than specified timestamp
     */
    @Query("DELETE FROM media WHERE createdAt < :timestamp")
    suspend fun deleteOldMedia(timestamp: Long): Int
    
    /**
     * Delete media files by cloud service
     */
    @Query("DELETE FROM media WHERE cloudService = :service")
    suspend fun deleteMediaByCloudService(service: String): Int
    
    /**
     * Get media count
     */
    @Query("SELECT COUNT(*) FROM media")
    suspend fun getMediaCount(): Int
    
    /**
     * Get media count by type
     */
    @Query("SELECT COUNT(*) FROM media WHERE mimeType LIKE :mimeTypePattern")
    suspend fun getMediaCountByType(mimeTypePattern: String): Int
    
    /**
     * Check if media exists
     */
    @Query("SELECT COUNT(*) > 0 FROM media WHERE id = :mediaId")
    suspend fun mediaExists(mediaId: String): Boolean
}