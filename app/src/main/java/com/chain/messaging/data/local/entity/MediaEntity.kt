package com.chain.messaging.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ForeignKey
import com.chain.messaging.core.util.TimeUtils

/**
 * Room entity for storing media file information locally.
 */
@Entity(
    tableName = "media",
    indices = [
        Index(value = ["messageId"]),
        Index(value = ["filePath"]),
        Index(value = ["createdAt"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MediaEntity(
    @PrimaryKey
    val id: String,
    val messageId: String,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val fileSize: Long,
    val width: Int? = null, // For images and videos
    val height: Int? = null, // For images and videos
    val duration: Long? = null, // For audio and video in milliseconds
    val thumbnailPath: String? = null,
    val isEncrypted: Boolean = true,
    val encryptionKey: String? = null,
    val cloudUrl: String? = null, // URL if stored in cloud
    val cloudService: String? = null, // Which cloud service (Google Drive, OneDrive, etc.)
    val isUploaded: Boolean = false,
    val uploadProgress: Int = 0, // 0-100
    val createdAt: Long = TimeUtils.getCurrentTimestamp(),
    val updatedAt: Long = TimeUtils.getCurrentTimestamp()
)

/**
 * Domain model for media files
 */
data class Media(
    val id: String,
    val messageId: String,
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val fileSize: Long,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Long? = null,
    val thumbnailPath: String? = null,
    val isEncrypted: Boolean = true,
    val encryptionKey: String? = null,
    val cloudUrl: String? = null,
    val cloudService: String? = null,
    val isUploaded: Boolean = false,
    val uploadProgress: Int = 0
)

/**
 * Extension function to convert MediaEntity to domain Media model
 */
fun MediaEntity.toDomain(): Media {
    return Media(
        id = id,
        messageId = messageId,
        fileName = fileName,
        filePath = filePath,
        mimeType = mimeType,
        fileSize = fileSize,
        width = width,
        height = height,
        duration = duration,
        thumbnailPath = thumbnailPath,
        isEncrypted = isEncrypted,
        encryptionKey = encryptionKey,
        cloudUrl = cloudUrl,
        cloudService = cloudService,
        isUploaded = isUploaded,
        uploadProgress = uploadProgress
    )
}

/**
 * Extension function to convert domain Media model to MediaEntity
 */
fun Media.toEntity(): MediaEntity {
    return MediaEntity(
        id = id,
        messageId = messageId,
        fileName = fileName,
        filePath = filePath,
        mimeType = mimeType,
        fileSize = fileSize,
        width = width,
        height = height,
        duration = duration,
        thumbnailPath = thumbnailPath,
        isEncrypted = isEncrypted,
        encryptionKey = encryptionKey,
        cloudUrl = cloudUrl,
        cloudService = cloudService,
        isUploaded = isUploaded,
        uploadProgress = uploadProgress,
        updatedAt = TimeUtils.getCurrentTimestamp()
    )
}