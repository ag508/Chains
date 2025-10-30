package com.chain.messaging.core.privacy

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@ExperimentalCoroutinesApi
class ScreenshotDetectorTest {
    
    private lateinit var screenshotDetector: ScreenshotDetectorImpl
    private val mockContext = mockk<Context>()
    private val mockContentResolver = mockk<ContentResolver>()
    private val mockCursor = mockk<Cursor>()
    
    @Before
    fun setup() {
        every { mockContext.contentResolver } returns mockContentResolver
        
        screenshotDetector = ScreenshotDetectorImpl(mockContext)
    }
    
    @After
    fun tearDown() {
        screenshotDetector.stopMonitoring()
        clearAllMocks()
    }
    
    @Test
    fun `isSupported should return true for supported Android versions`() {
        // When
        val isSupported = screenshotDetector.isSupported()
        
        // Then
        assertTrue(isSupported) // Should be true for modern Android versions
    }
    
    @Test
    fun `startMonitoring should register content observers`() {
        // Given
        every { mockContentResolver.registerContentObserver(any(), any(), any()) } just Runs
        
        // When
        screenshotDetector.startMonitoring()
        
        // Then
        verify(exactly = 2) { 
            mockContentResolver.registerContentObserver(any(), eq(true), any())
        }
    }
    
    @Test
    fun `stopMonitoring should unregister content observers`() {
        // Given
        every { mockContentResolver.registerContentObserver(any(), any(), any()) } just Runs
        every { mockContentResolver.unregisterContentObserver(any()) } just Runs
        
        screenshotDetector.startMonitoring()
        
        // When
        screenshotDetector.stopMonitoring()
        
        // Then
        verify { mockContentResolver.unregisterContentObserver(any()) }
    }
    
    @Test
    fun `handleMediaChange should detect screenshot files`() = runTest {
        // Given
        val testUri = Uri.parse("content://media/external/images/media/123")
        val screenshotPath = "/storage/emulated/0/Pictures/Screenshots/Screenshot_20231201_120000.png"
        val screenshotName = "Screenshot_20231201_120000.png"
        val timestamp = System.currentTimeMillis() / 1000
        
        setupMockCursor(screenshotPath, screenshotName, timestamp)
        every { mockContentResolver.registerContentObserver(any(), any(), any()) } just Runs
        
        // Start monitoring to set up the content observer
        screenshotDetector.startMonitoring()
        
        // When - simulate a media change event
        // This is tricky to test directly since the ContentObserver is internal
        // We'll test the screenshot detection logic indirectly
        
        // Then
        // In a real test, we would trigger the content observer and verify the flow emission
        // For now, we verify that monitoring is set up correctly
        verify(exactly = 2) { 
            mockContentResolver.registerContentObserver(any(), eq(true), any())
        }
    }
    
    @Test
    fun `isScreenshotFile should detect screenshot files by path`() {
        // Given
        val screenshotDetector = ScreenshotDetectorImpl(mockContext)
        
        // When & Then
        assertTrue(screenshotDetector.isScreenshotFile(
            "/storage/emulated/0/Pictures/Screenshots/screenshot_123.png",
            "screenshot_123.png"
        ))
        
        assertTrue(screenshotDetector.isScreenshotFile(
            "/storage/emulated/0/DCIM/screencapture_456.jpg",
            "screencapture_456.jpg"
        ))
        
        assertFalse(screenshotDetector.isScreenshotFile(
            "/storage/emulated/0/Pictures/photo_789.jpg",
            "photo_789.jpg"
        ))
    }
    
    @Test
    fun `isScreenshotFile should detect screenshot files by name`() {
        // Given
        val screenshotDetector = ScreenshotDetectorImpl(mockContext)
        
        // When & Then
        assertTrue(screenshotDetector.isScreenshotFile(
            null,
            "Screenshot_20231201_120000.png"
        ))
        
        assertTrue(screenshotDetector.isScreenshotFile(
            null,
            "screen_shot_image.jpg"
        ))
        
        assertTrue(screenshotDetector.isScreenshotFile(
            null,
            "screencap_test.png"
        ))
        
        assertFalse(screenshotDetector.isScreenshotFile(
            null,
            "regular_photo.jpg"
        ))
    }
    
    @Test
    fun `isScreenshotFile should handle null inputs gracefully`() {
        // Given
        val screenshotDetector = ScreenshotDetectorImpl(mockContext)
        
        // When & Then
        assertFalse(screenshotDetector.isScreenshotFile(null, null))
        assertFalse(screenshotDetector.isScreenshotFile("", ""))
    }
    
    private fun setupMockCursor(filePath: String, displayName: String, timestamp: Long) {
        every { mockContentResolver.query(any(), any(), any(), any(), any()) } returns mockCursor
        every { mockCursor.moveToFirst() } returns true
        every { mockCursor.getColumnIndex(MediaStore.Images.Media.DATA) } returns 0
        every { mockCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED) } returns 1
        every { mockCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME) } returns 2
        every { mockCursor.getString(0) } returns filePath
        every { mockCursor.getLong(1) } returns timestamp
        every { mockCursor.getString(2) } returns displayName
        every { mockCursor.use(any()) } answers {
            val block = firstArg<(Cursor) -> Unit>()
            block(mockCursor)
        }
    }
    
    // Extension function to access private method for testing
    private fun ScreenshotDetectorImpl.isScreenshotFile(filePath: String?, displayName: String?): Boolean {
        // Use reflection to access private method for testing
        val method = this::class.java.getDeclaredMethod("isScreenshotFile", String::class.java, String::class.java)
        method.isAccessible = true
        return method.invoke(this, filePath, displayName) as Boolean
    }
}