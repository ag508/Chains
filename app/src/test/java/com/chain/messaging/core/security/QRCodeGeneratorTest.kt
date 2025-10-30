package com.chain.messaging.core.security

import android.graphics.Bitmap
import android.graphics.Color
import io.mockk.*
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class QRCodeGeneratorTest {
    
    private lateinit var qrCodeGenerator: QRCodeGenerator
    
    @Before
    fun setup() {
        // Mock Android dependencies
        mockkStatic(Bitmap::class)
        every { Bitmap.createBitmap(any<Int>(), any<Int>(), any<Bitmap.Config>()) } returns mockk<Bitmap>(relaxed = true)
        
        qrCodeGenerator = QRCodeGenerator()
    }
    
    @Test
    fun `generateQRCode should create bitmap with default parameters`() {
        // Given
        val testData = "chain://verify?user=test&name=Test&key=abc123&ts=1234567890"
        
        // When
        val result = qrCodeGenerator.generateQRCode(testData)
        
        // Then
        assertNotNull(result)
        // Verify that Bitmap.createBitmap was called
        verify { Bitmap.createBitmap(any<Int>(), any<Int>(), any<Bitmap.Config>()) }
    }
    
    @Test
    fun `generateQRCode should create bitmap with custom size`() {
        // Given
        val testData = "chain://verify?user=test&name=Test&key=abc123&ts=1234567890"
        val customSize = 256
        
        // When
        val result = qrCodeGenerator.generateQRCode(testData, customSize)
        
        // Then
        assertNotNull(result)
        verify { Bitmap.createBitmap(customSize, customSize, any<Bitmap.Config>()) }
    }
    
    @Test
    fun `generateQRCode should create bitmap with custom colors`() {
        // Given
        val testData = "chain://verify?user=test&name=Test&key=abc123&ts=1234567890"
        val foregroundColor = Color.BLUE
        val backgroundColor = Color.YELLOW
        
        // When
        val result = qrCodeGenerator.generateQRCode(
            data = testData,
            foregroundColor = foregroundColor,
            backgroundColor = backgroundColor
        )
        
        // Then
        assertNotNull(result)
        verify { Bitmap.createBitmap(any<Int>(), any<Int>(), any<Bitmap.Config>()) }
    }
    
    @Test
    fun `validateQRData should return true for valid Chain verification URL`() {
        // Given
        val validData = "chain://verify?user=test-user&name=Test%20User&key=abc123&ts=1234567890"
        
        // When
        val isValid = qrCodeGenerator.validateQRData(validData)
        
        // Then
        assertTrue(isValid)
    }
    
    @Test
    fun `validateQRData should return false for invalid scheme`() {
        // Given
        val invalidData = "http://verify?user=test-user&name=Test%20User&key=abc123&ts=1234567890"
        
        // When
        val isValid = qrCodeGenerator.validateQRData(invalidData)
        
        // Then
        assertFalse(isValid)
    }
    
    @Test
    fun `validateQRData should return false for invalid host`() {
        // Given
        val invalidData = "chain://invalid?user=test-user&name=Test%20User&key=abc123&ts=1234567890"
        
        // When
        val isValid = qrCodeGenerator.validateQRData(invalidData)
        
        // Then
        assertFalse(isValid)
    }
    
    @Test
    fun `validateQRData should return false for missing user parameter`() {
        // Given
        val invalidData = "chain://verify?name=Test%20User&key=abc123&ts=1234567890"
        
        // When
        val isValid = qrCodeGenerator.validateQRData(invalidData)
        
        // Then
        assertFalse(isValid)
    }
    
    @Test
    fun `validateQRData should return false for missing name parameter`() {
        // Given
        val invalidData = "chain://verify?user=test-user&key=abc123&ts=1234567890"
        
        // When
        val isValid = qrCodeGenerator.validateQRData(invalidData)
        
        // Then
        assertFalse(isValid)
    }
    
    @Test
    fun `validateQRData should return false for missing key parameter`() {
        // Given
        val invalidData = "chain://verify?user=test-user&name=Test%20User&ts=1234567890"
        
        // When
        val isValid = qrCodeGenerator.validateQRData(invalidData)
        
        // Then
        assertFalse(isValid)
    }
    
    @Test
    fun `validateQRData should return false for missing timestamp parameter`() {
        // Given
        val invalidData = "chain://verify?user=test-user&name=Test%20User&key=abc123"
        
        // When
        val isValid = qrCodeGenerator.validateQRData(invalidData)
        
        // Then
        assertFalse(isValid)
    }
    
    @Test
    fun `validateQRData should return false for malformed URL`() {
        // Given
        val invalidData = "not-a-url"
        
        // When
        val isValid = qrCodeGenerator.validateQRData(invalidData)
        
        // Then
        assertFalse(isValid)
    }
    
    @Test
    fun `getOptimalSize should return appropriate size for data length`() {
        // Test different data lengths
        assertEquals(256, qrCodeGenerator.getOptimalSize(50))
        assertEquals(384, qrCodeGenerator.getOptimalSize(150))
        assertEquals(512, qrCodeGenerator.getOptimalSize(250))
        assertEquals(768, qrCodeGenerator.getOptimalSize(350))
    }
    
    @Test
    fun `generateQRCode should throw QRCodeException for invalid data`() {
        // Given
        val invalidData = "" // Empty string should cause encoding to fail
        
        // When & Then
        try {
            qrCodeGenerator.generateQRCode(invalidData)
            // If we reach here, the test should fail
            assertTrue(false, "Expected QRCodeException to be thrown")
        } catch (e: QRCodeException) {
            // Expected exception
            assertNotNull(e.message)
        }
    }
    
    @Test
    fun `generateQRCode should handle special characters in data`() {
        // Given
        val dataWithSpecialChars = "chain://verify?user=test-user&name=Test%20User%20%26%20Co&key=abc123&ts=1234567890"
        
        // When
        val result = qrCodeGenerator.generateQRCode(dataWithSpecialChars)
        
        // Then
        assertNotNull(result)
    }
    
    @Test
    fun `generateQRCode should handle unicode characters in data`() {
        // Given
        val dataWithUnicode = "chain://verify?user=test-user&name=测试用户&key=abc123&ts=1234567890"
        
        // When
        val result = qrCodeGenerator.generateQRCode(dataWithUnicode)
        
        // Then
        assertNotNull(result)
    }
}