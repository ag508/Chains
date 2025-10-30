package com.chain.messaging.core.webrtc

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Screenshot detector for detecting when screenshots are taken during calls
 * Implements requirement 6.4 for screenshot detection where possible
 */
@Singleton
class ScreenshotDetector @Inject constructor(
    private val context: Context
) {
    
    private val _screenshotEvents = MutableSharedFlow<ScreenshotEvent>()
    val screenshotEvents: Flow<ScreenshotEvent> = _screenshotEvents.asSharedFlow()
    
    private var contentObserver: ContentObserver? = null
    private var isMonitoring = false
    private var currentCallId: String? = null
    
    /**
     * Start monitoring for screenshots during a call
     */
    fun startScreenshotDetection(callId: String) {
        if (isMonitoring) return
        
        currentCallId = callId
        isMonitoring = true
        
        // Create content observer for media store changes
        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                handleMediaStoreChange(uri)
            }
        }
        
        // Register observer for external storage images
        try {
            context.contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver!!
            )
        } catch (e: Exception) {
            // Failed to register observer
            isMonitoring = false
        }
    }
    
    /**
     * Stop monitoring for screenshots
     */
    fun stopScreenshotDetection() {
        if (!isMonitoring) return
        
        contentObserver?.let { observer ->
            try {
                context.contentResolver.unregisterContentObserver(observer)
            } catch (e: Exception) {
                // Failed to unregister observer
            }
        }
        
        contentObserver = null
        isMonitoring = false
        currentCallId = null
    }
    
    /**
     * Check if screenshot detection is currently active
     */
    fun isDetectionActive(): Boolean {
        return isMonitoring
    }
    
    private fun handleMediaStoreChange(uri: Uri?) {
        if (!isMonitoring || currentCallId == null) return
        
        uri?.let { mediaUri ->
            // Check if this is likely a screenshot
            if (isLikelyScreenshot(mediaUri)) {
                val event = ScreenshotEvent(
                    callId = currentCallId!!,
                    timestamp = System.currentTimeMillis(),
                    confidence = getScreenshotConfidence(mediaUri),
                    details = "Screenshot detected during call"
                )
                
                _screenshotEvents.tryEmit(event)
            }
        }
    }
    
    private fun isLikelyScreenshot(uri: Uri): Boolean {
        try {
            // Query the media store for file details
            val projection = arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT
            )
            
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                    val dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED))
                    val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                    val width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH))
                    val height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT))
                    
                    // Check if this looks like a screenshot
                    return isScreenshotBasedOnMetadata(displayName, dateAdded, size, width, height)
                }
            }
        } catch (e: Exception) {
            // Failed to query media store
            return false
        }
        
        return false
    }
    
    private fun isScreenshotBasedOnMetadata(
        displayName: String,
        dateAdded: Long,
        size: Long,
        width: Int,
        height: Int
    ): Boolean {
        // Check if filename suggests screenshot
        val screenshotKeywords = listOf("screenshot", "screen", "capture", "shot")
        val lowerDisplayName = displayName.lowercase()
        val hasScreenshotKeyword = screenshotKeywords.any { lowerDisplayName.contains(it) }
        
        // Check if image was created very recently (within last 5 seconds)
        val currentTime = System.currentTimeMillis() / 1000
        val isRecent = (currentTime - dateAdded) <= 5
        
        // Check if dimensions match common screen sizes
        val isScreenDimensions = isCommonScreenSize(width, height)
        
        // Check if file size is reasonable for a screenshot
        val isReasonableSize = size > 10000 && size < 5000000 // 10KB to 5MB
        
        // Screenshot is likely if it has keyword OR (is recent AND has screen dimensions AND reasonable size)
        return hasScreenshotKeyword || (isRecent && isScreenDimensions && isReasonableSize)
    }
    
    private fun isCommonScreenSize(width: Int, height: Int): Boolean {
        // Common mobile screen resolutions
        val commonResolutions = listOf(
            Pair(1080, 1920), // Full HD
            Pair(1080, 2340), // 19.5:9
            Pair(1440, 2960), // QHD+
            Pair(1440, 3200), // QHD+ tall
            Pair(720, 1280),  // HD
            Pair(720, 1520),  // HD+ tall
            Pair(1080, 2400), // Full HD+ tall
            Pair(1170, 2532), // iPhone 12/13
            Pair(1284, 2778), // iPhone 12/13 Pro Max
            Pair(828, 1792),  // iPhone XR/11
        )
        
        // Check both orientations
        return commonResolutions.any { (w, h) ->
            (width == w && height == h) || (width == h && height == w)
        }
    }
    
    private fun getScreenshotConfidence(uri: Uri): ScreenshotConfidence {
        // This is a simplified confidence calculation
        // In practice, you'd use more sophisticated heuristics
        return try {
            val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                    val lowerDisplayName = displayName.lowercase()
                    
                    when {
                        lowerDisplayName.contains("screenshot") -> ScreenshotConfidence.HIGH
                        lowerDisplayName.contains("screen") || lowerDisplayName.contains("capture") -> ScreenshotConfidence.MEDIUM
                        else -> ScreenshotConfidence.LOW
                    }
                } else {
                    ScreenshotConfidence.LOW
                }
            } ?: ScreenshotConfidence.LOW
        } catch (e: Exception) {
            ScreenshotConfidence.LOW
        }
    }
}

/**
 * Screenshot event data class
 */
data class ScreenshotEvent(
    val callId: String,
    val timestamp: Long,
    val confidence: ScreenshotConfidence,
    val details: String
)

/**
 * Confidence level for screenshot detection
 */
enum class ScreenshotConfidence {
    LOW,
    MEDIUM,
    HIGH
}