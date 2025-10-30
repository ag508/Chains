package com.chain.messaging.domain.model

/**
 * Data class representing media content in messages
 */
data class MediaMessage(
    val uri: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val duration: Long? = null, // For audio/video files
    val width: Int? = null,     // For images/videos
    val height: Int? = null,    // For images/videos
    val thumbnailUri: String? = null,
    val isLocal: Boolean = true
)

/**
 * Supported media types for messages
 */
enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT;
    
    companion object {
        fun fromMimeType(mimeType: String): MediaType {
            return when {
                mimeType.startsWith("image/") -> IMAGE
                mimeType.startsWith("video/") -> VIDEO
                mimeType.startsWith("audio/") -> AUDIO
                else -> DOCUMENT
            }
        }
    }
}

/**
 * Extension to Message model to handle media content
 */
fun Message.getMediaContent(): MediaMessage? {
    return if (type in listOf(MessageType.IMAGE, MessageType.VIDEO, MessageType.AUDIO, MessageType.DOCUMENT)) {
        try {
            // Parse JSON content to MediaMessage
            parseMediaContent(content)
        } catch (e: Exception) {
            null
        }
    } else {
        null
    }
}

private fun parseMediaContent(content: String): MediaMessage? {
    return try {
        // Simple JSON parsing - in production, use Gson or kotlinx.serialization
        if (content.startsWith("{") && content.endsWith("}")) {
            // Extract values using regex (simplified approach)
            val uriRegex = "\"uri\":\\s*\"([^\"]+)\"".toRegex()
            val fileNameRegex = "\"fileName\":\\s*\"([^\"]+)\"".toRegex()
            val mimeTypeRegex = "\"mimeType\":\\s*\"([^\"]+)\"".toRegex()
            val fileSizeRegex = "\"fileSize\":\\s*(\\d+)".toRegex()
            val durationRegex = "\"duration\":\\s*(\\d+|null)".toRegex()
            val widthRegex = "\"width\":\\s*(\\d+|null)".toRegex()
            val heightRegex = "\"height\":\\s*(\\d+|null)".toRegex()
            val thumbnailUriRegex = "\"thumbnailUri\":\\s*(\"[^\"]+\"|null)".toRegex()
            val isLocalRegex = "\"isLocal\":\\s*(true|false)".toRegex()
            
            val uri = uriRegex.find(content)?.groupValues?.get(1) ?: return null
            val fileName = fileNameRegex.find(content)?.groupValues?.get(1) ?: return null
            val mimeType = mimeTypeRegex.find(content)?.groupValues?.get(1) ?: return null
            val fileSize = fileSizeRegex.find(content)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            val duration = durationRegex.find(content)?.groupValues?.get(1)?.let { 
                if (it == "null") null else it.toLongOrNull() 
            }
            val width = widthRegex.find(content)?.groupValues?.get(1)?.let { 
                if (it == "null") null else it.toIntOrNull() 
            }
            val height = heightRegex.find(content)?.groupValues?.get(1)?.let { 
                if (it == "null") null else it.toIntOrNull() 
            }
            val thumbnailUri = thumbnailUriRegex.find(content)?.groupValues?.get(1)?.let { 
                if (it == "null") null else it.removeSurrounding("\"")
            }
            val isLocal = isLocalRegex.find(content)?.groupValues?.get(1)?.toBoolean() ?: true
            
            MediaMessage(
                uri = uri,
                fileName = fileName,
                mimeType = mimeType,
                fileSize = fileSize,
                duration = duration,
                width = width,
                height = height,
                thumbnailUri = thumbnailUri,
                isLocal = isLocal
            )
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}