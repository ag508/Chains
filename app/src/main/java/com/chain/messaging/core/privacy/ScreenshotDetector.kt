package com.chain.messaging.core.privacy

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
 * Interface for detecting screenshots and screen recordings.
 */
interface ScreenshotDetector {
    
    /**
     * Starts monitoring for screenshots.
     */
    fun startMonitoring()
    
    /**
     * Stops monitoring for screenshots.
     */
    fun stopMonitoring()
    
    /**
     * Observes screenshot events.
     * @return Flow of screenshot events with timestamp
     */
    fun observeScreenshots(): Flow<PrivacyScreenshotEvent>
    
    /**
     * Checks if screenshot detection is supported on this device.
     * @return true if supported, false otherwise
     */
    fun isSupported(): Boolean
}

/**
 * Data class representing a privacy screenshot event.
 */
data class PrivacyScreenshotEvent(
    val timestamp: Long,
    val filePath: String? = null,
    val type: ScreenshotType = ScreenshotType.SCREENSHOT
)

enum class ScreenshotType {
    SCREENSHOT,
    SCREEN_RECORDING
}

/**
 * Implementation of ScreenshotDetector using MediaStore content observer.
 */
@Singleton
class ScreenshotDetectorImpl @Inject constructor(
    private val context: Context
) : ScreenshotDetector {
    
    private val _screenshotEvents = MutableSharedFlow<PrivacyScreenshotEvent>()
    private var contentObserver: ContentObserver? = null
    private var isMonitoring = false
    
    private val handler = Handler(Looper.getMainLooper())
    
    companion object {
        private val SCREENSHOT_URIS = arrayOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Images.Media.INTERNAL_CONTENT_URI
        )
        
        private val SCREENSHOT_KEYWORDS = arrayOf(
            "screenshot", "screen_shot", "screencapture", "screen_capture",
            "screencap", "screen_cap", "scrnshot"
        )
    }
    
    override fun startMonitoring() {
        if (isMonitoring || !isSupported()) return
        
        contentObserver = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                uri?.let { handleMediaChange(it) }
            }
        }
        
        SCREENSHOT_URIS.forEach { uri ->
            context.contentResolver.registerContentObserver(
                uri, true, contentObserver!!
            )
        }
        
        isMonitoring = true
    }
    
    override fun stopMonitoring() {
        if (!isMonitoring) return
        
        contentObserver?.let { observer ->
            context.contentResolver.unregisterContentObserver(observer)
        }
        contentObserver = null
        isMonitoring = false
    }
    
    override fun observeScreenshots(): Flow<PrivacyScreenshotEvent> {
        return _screenshotEvents.asSharedFlow()
    }
    
    override fun isSupported(): Boolean {
        // Screenshot detection is available on most Android versions
        // but may not work reliably on all devices due to OEM customizations
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN
    }
    
    private fun handleMediaChange(uri: Uri) {
        try {
            val projection = arrayOf(
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DISPLAY_NAME
            )
            
            context.contentResolver.query(
                uri, projection, null, null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                    val dateIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                    val nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    
                    if (dataIndex >= 0 && dateIndex >= 0 && nameIndex >= 0) {
                        val filePath = cursor.getString(dataIndex)
                        val dateAdded = cursor.getLong(dateIndex)
                        val displayName = cursor.getString(nameIndex)
                        
                        // Check if this looks like a screenshot
                        if (isScreenshotFile(filePath, displayName)) {
                            val event = PrivacyScreenshotEvent(
                                timestamp = dateAdded * 1000, // Convert to milliseconds
                                filePath = filePath,
                                type = ScreenshotType.SCREENSHOT
                            )
                            
                            _screenshotEvents.tryEmit(event)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Handle security exceptions or other errors
            // Some devices may not allow access to MediaStore
        }
    }
    
    private fun isScreenshotFile(filePath: String?, displayName: String?): Boolean {
        val pathToCheck = (filePath ?: "").lowercase()
        val nameToCheck = (displayName ?: "").lowercase()
        
        return SCREENSHOT_KEYWORDS.any { keyword ->
            pathToCheck.contains(keyword) || nameToCheck.contains(keyword)
        }
    }
}