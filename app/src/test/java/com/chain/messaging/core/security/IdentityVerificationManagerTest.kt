package com.chain.messaging.core.security

import android.graphics.Bitmap
import com.chain.messaging.core.crypto.KeyManager
import com.chain.messaging.domain.model.User
import com.chain.messaging.domain.model.UserStatus
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.ecc.Curve
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IdentityVerificationManagerTest {
    
    private lateinit var keyManager: KeyManager
    private lateinit var qrCodeGenerator: QRCodeGenerator
    private lateinit var safetyNumberGenerator: SafetyNumberGenerator
    private lateinit var securityAlertManager: SecurityAlertManager
    private lateinit var identityVerificationManager: IdentityVerificationManager
    
    private val testUser = User(
        id = "test-user-id",
        publicKey = "test-public-key",
        displayName = "Test User",
        status = UserStatus.ONLINE
    )
    
    private val testIdentityKey = IdentityKey(Curve.generateKeyPair().publicKey)
    private val testBitmap = mockk<Bitmap>()
    
    @Before
    fun setup() {
        keyManager = mockk()
        qrCodeGenerator = mockk()
        safetyNumberGenerator = mockk()
        securityAlertManager = mockk()
        
        identityVerificationManager = IdentityVerificationManager(
            keyManager = keyManager,
            qrCodeGenerator = qrCodeGenerator,
            safetyNumberGenerator = safetyNumberGenerator,
            securityAlertManager = securityAlertManager
        )
        
        every { keyManager.getIdentityKey() } returns testIdentityKey
        every { keyManager.getRegistrationId() } returns 12345
    }
    
    @Test
    fun `generateVerificationQRCode should return success with valid bitmap`() = runTest {
        // Given
        every { qrCodeGenerator.generateQRCode(any<String>()) } returns testBitmap
        
        // When
        val result = identityVerificationManager.generateVerificationQRCode(testUser)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(testBitmap, result.getOrNull())
        verify { qrCodeGenerator.generateQRCode(any<String>()) }
    }
    
    @Test
    fun `generateVerificationQRCode should return failure when QR generation fails`() = runTest {
        // Given
        every { qrCodeGenerator.generateQRCode(any<String>()) } throws RuntimeException("QR generation failed")
        
        // When
        val result = identityVerificationManager.generateVerificationQRCode(testUser)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
    
    @Test
    fun `verifyQRCode should return success for valid QR data with matching keys`() = runTest {
        // Given
        val qrData = "chain://verify?user=test-user&name=Test%20User&key=abcd1234&ts=1234567890"
        
        // When
        val result = identityVerificationManager.verifyQRCode(qrData)
        
        // Then
        assertTrue(result.isSuccess)
        val verificationResult = result.getOrNull()
        assertTrue(verificationResult is VerificationResult.Success)
        assertEquals("test-user", (verificationResult as VerificationResult.Success).userId)
        assertEquals("Test User", verificationResult.displayName)
    }
    
    @Test
    fun `verifyQRCode should return failure for invalid QR data format`() = runTest {
        // Given
        val invalidQrData = "invalid-qr-data"
        
        // When
        val result = identityVerificationManager.verifyQRCode(invalidQrData)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
    
    @Test
    fun `generateSafetyNumber should return success with valid safety number`() = runTest {
        // Given
        val expectedSafetyNumber = "12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345 67890"
        every { 
            safetyNumberGenerator.generateSafetyNumber(
                any(), any(), any(), any()
            ) 
        } returns expectedSafetyNumber
        
        // When
        val result = identityVerificationManager.generateSafetyNumber("test-user", testIdentityKey)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedSafetyNumber, result.getOrNull())
        verify { 
            safetyNumberGenerator.generateSafetyNumber(
                testIdentityKey, testIdentityKey, "12345", "test-user"
            ) 
        }
    }
    
    @Test
    fun `generateSafetyNumber should return failure when generation fails`() = runTest {
        // Given
        every { 
            safetyNumberGenerator.generateSafetyNumber(
                any(), any(), any(), any()
            ) 
        } throws RuntimeException("Safety number generation failed")
        
        // When
        val result = identityVerificationManager.generateSafetyNumber("test-user", testIdentityKey)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
    
    @Test
    fun `verifySafetyNumber should return success true for matching safety numbers`() = runTest {
        // Given
        val safetyNumber = "12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345 67890"
        every { 
            safetyNumberGenerator.generateSafetyNumber(
                any(), any(), any(), any()
            ) 
        } returns safetyNumber
        
        // When
        val result = identityVerificationManager.verifySafetyNumber("test-user", safetyNumber, testIdentityKey)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }
    
    @Test
    fun `verifySafetyNumber should return success false for non-matching safety numbers`() = runTest {
        // Given
        val expectedSafetyNumber = "12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345 67890"
        val enteredSafetyNumber = "54321 09876 54321 09876 54321 09876 54321 09876 54321 09876 54321 09876"
        every { 
            safetyNumberGenerator.generateSafetyNumber(
                any(), any(), any(), any()
            ) 
        } returns expectedSafetyNumber
        
        // When
        val result = identityVerificationManager.verifySafetyNumber("test-user", enteredSafetyNumber, testIdentityKey)
        
        // Then
        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull() == true)
    }
    
    @Test
    fun `isUserVerified should return false for unverified user`() {
        // When
        val isVerified = identityVerificationManager.isUserVerified("test-user")
        
        // Then
        assertFalse(isVerified)
    }
    
    @Test
    fun `handleIdentityKeyChange should create security alert and mark user as unverified`() = runTest {
        // Given
        val oldKey = testIdentityKey
        val newKey = IdentityKey(Curve.generateKeyPair().publicKey)
        
        // When
        identityVerificationManager.handleIdentityKeyChange(
            userId = "test-user",
            displayName = "Test User",
            oldKey = oldKey,
            newKey = newKey
        )
        
        // Then
        val alerts = identityVerificationManager.securityAlerts.first()
        assertEquals(1, alerts.size)
        assertTrue(alerts.first() is SecurityAlert.IdentityKeyChanged)
        
        val alert = alerts.first() as SecurityAlert.IdentityKeyChanged
        assertEquals("test-user", alert.userId)
        assertEquals("Test User", alert.displayName)
        assertEquals(oldKey, alert.oldKey)
        assertEquals(newKey, alert.newKey)
    }
    
    @Test
    fun `dismissSecurityAlert should remove alert from list`() = runTest {
        // Given
        val alert = SecurityAlert.SuspiciousActivity("test-user", "test-activity", "test-details")
        identityVerificationManager.handleIdentityKeyChange(
            "test-user", "Test User", testIdentityKey, testIdentityKey
        )
        
        // When
        val alertsBefore = identityVerificationManager.securityAlerts.first()
        identityVerificationManager.dismissSecurityAlert(alertsBefore.first().id)
        
        // Then
        val alertsAfter = identityVerificationManager.securityAlerts.first()
        assertTrue(alertsAfter.isEmpty())
    }
    
    @Test
    fun `clearAllSecurityAlerts should remove all alerts`() = runTest {
        // Given
        identityVerificationManager.handleIdentityKeyChange(
            "user1", "User 1", testIdentityKey, testIdentityKey
        )
        identityVerificationManager.handleIdentityKeyChange(
            "user2", "User 2", testIdentityKey, testIdentityKey
        )
        
        // When
        identityVerificationManager.clearAllSecurityAlerts()
        
        // Then
        val alerts = identityVerificationManager.securityAlerts.first()
        assertTrue(alerts.isEmpty())
    }
    
    @Test
    fun `getSecurityRecommendations should return appropriate recommendations`() {
        // When
        val recommendations = identityVerificationManager.getSecurityRecommendations()
        
        // Then
        assertNotNull(recommendations)
        // Should contain recommendations based on current state
    }
    
    @Test
    fun `verification state should be updated when user is verified`() = runTest {
        // Given
        val safetyNumber = "12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345 67890"
        every { 
            safetyNumberGenerator.generateSafetyNumber(
                any(), any(), any(), any()
            ) 
        } returns safetyNumber
        
        // When
        identityVerificationManager.verifySafetyNumber("test-user", safetyNumber, testIdentityKey)
        
        // Then
        val verificationStates = identityVerificationManager.verificationState.first()
        assertTrue(verificationStates.containsKey("test-user"))
        assertTrue(verificationStates["test-user"]?.isVerified == true)
    }
}