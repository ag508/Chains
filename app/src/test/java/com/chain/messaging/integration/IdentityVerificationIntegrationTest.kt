package com.chain.messaging.integration

import android.graphics.Bitmap
import com.chain.messaging.core.crypto.KeyManager
import com.chain.messaging.core.security.*
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

/**
 * Integration test for the complete identity verification system
 */
class IdentityVerificationIntegrationTest {
    
    private lateinit var keyManager: KeyManager
    private lateinit var qrCodeGenerator: QRCodeGenerator
    private lateinit var qrCodeScanner: QRCodeScanner
    private lateinit var safetyNumberGenerator: SafetyNumberGenerator
    private lateinit var securityAlertManager: SecurityAlertManager
    private lateinit var identityVerificationManager: IdentityVerificationManager
    
    private val testUser1 = User(
        id = "user1",
        publicKey = "public-key-1",
        displayName = "Alice",
        status = UserStatus.ONLINE
    )
    
    private val testUser2 = User(
        id = "user2",
        publicKey = "public-key-2",
        displayName = "Bob",
        status = UserStatus.ONLINE
    )
    
    private val identityKey1 = IdentityKey(Curve.generateKeyPair().publicKey)
    private val identityKey2 = IdentityKey(Curve.generateKeyPair().publicKey)
    private val mockBitmap = mockk<Bitmap>()
    
    @Before
    fun setup() {
        // Create real instances for integration testing
        qrCodeGenerator = QRCodeGenerator()
        qrCodeScanner = QRCodeScanner()
        safetyNumberGenerator = SafetyNumberGenerator()
        securityAlertManager = SecurityAlertManager()
        
        // Mock KeyManager since it depends on Android components
        keyManager = mockk()
        every { keyManager.getIdentityKey() } returns identityKey1
        every { keyManager.getRegistrationId() } returns 12345
        
        // Mock QR code generation since it depends on ZXing and Android graphics
        mockkStatic(Bitmap::class)
        every { Bitmap.createBitmap(any<Int>(), any<Int>(), any<Bitmap.Config>()) } returns mockBitmap
        
        identityVerificationManager = IdentityVerificationManager(
            keyManager = keyManager,
            qrCodeGenerator = qrCodeGenerator,
            safetyNumberGenerator = safetyNumberGenerator,
            securityAlertManager = securityAlertManager
        )
    }
    
    @Test
    fun `complete QR code verification flow should work end-to-end`() = runTest {
        // Step 1: Generate QR code for user1
        val qrResult = identityVerificationManager.generateVerificationQRCode(testUser1)
        assertTrue(qrResult.isSuccess)
        assertNotNull(qrResult.getOrNull())
        
        // Step 2: Simulate scanning the QR code (we'll construct the QR data manually)
        val qrData = "chain://verify?user=${testUser1.id}&name=${testUser1.displayName}&key=abcd1234&ts=${System.currentTimeMillis()}"
        
        // Step 3: Verify the QR code
        val verifyResult = identityVerificationManager.verifyQRCode(qrData)
        assertTrue(verifyResult.isSuccess)
        
        val verificationResult = verifyResult.getOrNull()
        assertTrue(verificationResult is VerificationResult.Success)
        assertEquals(testUser1.id, (verificationResult as VerificationResult.Success).userId)
        assertEquals(testUser1.displayName, verificationResult.displayName)
        
        // Step 4: Check that user is now verified
        assertTrue(identityVerificationManager.isUserVerified(testUser1.id))
        
        // Step 5: Check verification state
        val verificationStates = identityVerificationManager.verificationState.first()
        assertTrue(verificationStates.containsKey(testUser1.id))
        assertTrue(verificationStates[testUser1.id]?.isVerified == true)
    }
    
    @Test
    fun `complete safety number verification flow should work end-to-end`() = runTest {
        // Step 1: Generate safety number for two users
        val safetyNumberResult = identityVerificationManager.generateSafetyNumber(testUser2.id, identityKey2)
        assertTrue(safetyNumberResult.isSuccess)
        
        val safetyNumber = safetyNumberResult.getOrNull()!!
        assertNotNull(safetyNumber)
        assertTrue(safetyNumberGenerator.isValidSafetyNumberFormat(safetyNumber))
        
        // Step 2: Verify the safety number (simulate user entering the same number)
        val verifyResult = identityVerificationManager.verifySafetyNumber(testUser2.id, safetyNumber, identityKey2)
        assertTrue(verifyResult.isSuccess)
        assertTrue(verifyResult.getOrNull() == true)
        
        // Step 3: Check that user is now verified
        assertTrue(identityVerificationManager.isUserVerified(testUser2.id))
        
        // Step 4: Check verification state
        val verificationStates = identityVerificationManager.verificationState.first()
        assertTrue(verificationStates.containsKey(testUser2.id))
        assertTrue(verificationStates[testUser2.id]?.isVerified == true)
    }
    
    @Test
    fun `safety number should be symmetric between users`() = runTest {
        // Generate safety number from user1's perspective
        val safetyNumber1 = identityVerificationManager.generateSafetyNumber(testUser2.id, identityKey2)
        assertTrue(safetyNumber1.isSuccess)
        
        // Generate safety number from user2's perspective (simulate by swapping keys)
        every { keyManager.getIdentityKey() } returns identityKey2
        every { keyManager.getRegistrationId() } returns 67890
        
        val safetyNumber2 = identityVerificationManager.generateSafetyNumber(testUser1.id, identityKey1)
        assertTrue(safetyNumber2.isSuccess)
        
        // Safety numbers should be identical
        assertEquals(safetyNumber1.getOrNull(), safetyNumber2.getOrNull())
    }
    
    @Test
    fun `identity key change should trigger security alert`() = runTest {
        // Step 1: Verify user initially
        val safetyNumberResult = identityVerificationManager.generateSafetyNumber(testUser1.id, identityKey1)
        val safetyNumber = safetyNumberResult.getOrNull()!!
        identityVerificationManager.verifySafetyNumber(testUser1.id, safetyNumber, identityKey1)
        
        // Step 2: Simulate identity key change
        val newIdentityKey = IdentityKey(Curve.generateKeyPair().publicKey)
        identityVerificationManager.handleIdentityKeyChange(
            userId = testUser1.id,
            displayName = testUser1.displayName,
            oldKey = identityKey1,
            newKey = newIdentityKey
        )
        
        // Step 3: Check that security alert was created
        val alerts = identityVerificationManager.securityAlerts.first()
        assertEquals(1, alerts.size)
        assertTrue(alerts.first() is SecurityAlert.IdentityKeyChanged)
        
        val alert = alerts.first() as SecurityAlert.IdentityKeyChanged
        assertEquals(testUser1.id, alert.userId)
        assertEquals(testUser1.displayName, alert.displayName)
        assertEquals(identityKey1, alert.oldKey)
        assertEquals(newIdentityKey, alert.newKey)
        
        // Step 4: Check that user is no longer verified
        assertFalse(identityVerificationManager.isUserVerified(testUser1.id))
    }
    
    @Test
    fun `security recommendations should be generated based on state`() = runTest {
        // Step 1: Create some unverified users and alerts
        identityVerificationManager.handleIdentityKeyChange(
            userId = testUser1.id,
            displayName = testUser1.displayName,
            oldKey = identityKey1,
            newKey = identityKey2
        )
        
        // Step 2: Get security recommendations
        val recommendations = identityVerificationManager.getSecurityRecommendations()
        
        // Step 3: Verify recommendations are appropriate
        assertTrue(recommendations.isNotEmpty())
        assertTrue(recommendations.any { it is SecurityRecommendation.ReviewSecurityAlerts })
    }
    
    @Test
    fun `security alert manager should track threat levels correctly`() = runTest {
        // Step 1: Initially should be low threat
        assertEquals(ThreatLevel.LOW, securityAlertManager.threatLevel.first())
        
        // Step 2: Add a key change alert (should be medium threat)
        val keyChangeAlert = securityAlertManager.createKeyChangeAlert(
            testUser1.id, testUser1.displayName, identityKey1, identityKey2
        )
        securityAlertManager.addAlert(keyChangeAlert)
        assertEquals(ThreatLevel.MEDIUM, securityAlertManager.threatLevel.first())
        
        // Step 3: Add a key mismatch alert (should be high threat)
        val keyMismatchAlert = securityAlertManager.createKeyMismatchAlert(
            testUser2.id, testUser2.displayName, identityKey1, identityKey2
        )
        securityAlertManager.addAlert(keyMismatchAlert)
        assertEquals(ThreatLevel.HIGH, securityAlertManager.threatLevel.first())
        
        // Step 4: Clear alerts (should return to low threat)
        securityAlertManager.clearAllAlerts()
        assertEquals(ThreatLevel.LOW, securityAlertManager.threatLevel.first())
    }
    
    @Test
    fun `security analysis should provide comprehensive threat assessment`() = runTest {
        // Step 1: Create various types of alerts
        val keyChangeAlert = securityAlertManager.createKeyChangeAlert(
            testUser1.id, testUser1.displayName, identityKey1, identityKey2
        )
        val suspiciousAlert = securityAlertManager.createSuspiciousActivityAlert(
            testUser2.id, "login", "Multiple failed attempts"
        )
        
        securityAlertManager.addAlert(keyChangeAlert)
        securityAlertManager.addAlert(suspiciousAlert)
        
        // Step 2: Analyze security threats
        val analysis = securityAlertManager.analyzeSecurityThreats()
        
        // Step 3: Verify analysis results
        assertEquals(2, analysis.totalAlerts)
        assertEquals(2, analysis.recentAlerts) // Both alerts are recent
        assertEquals(ThreatLevel.MEDIUM, analysis.threatLevel) // Key change alert sets medium threat
        assertTrue(analysis.recommendations.isNotEmpty())
        
        // Check specific recommendations
        val recommendationTypes = analysis.recommendations.map { it::class }
        assertTrue(recommendationTypes.contains(SecurityRecommendation.ReviewKeyChanges::class))
        assertTrue(recommendationTypes.contains(SecurityRecommendation.ReviewSuspiciousActivity::class))
    }
    
    @Test
    fun `security score should reflect verification status and alerts`() = runTest {
        // Step 1: Calculate initial security score (no verified contacts, no alerts)
        val initialScore = securityAlertManager.getSecurityScore(0, 0)
        assertEquals(100, initialScore.score) // Perfect score initially
        
        // Step 2: Add some alerts and calculate score
        val alert = securityAlertManager.createKeyChangeAlert(
            testUser1.id, testUser1.displayName, identityKey1, identityKey2
        )
        securityAlertManager.addAlert(alert)
        
        val scoreWithAlert = securityAlertManager.getSecurityScore(0, 1)
        assertTrue(scoreWithAlert.score < 100) // Score should be reduced due to alert
        
        // Step 3: Add verified contacts and calculate score
        val scoreWithVerification = securityAlertManager.getSecurityScore(1, 1)
        assertTrue(scoreWithVerification.score > scoreWithAlert.score) // Score should improve with verification
    }
    
    @Test
    fun `QR code validation should work correctly`() {
        // Test valid QR codes
        val validQR1 = "chain://verify?user=test&name=Test&key=abc123&ts=1234567890"
        assertTrue(qrCodeGenerator.validateQRData(validQR1))
        assertTrue(qrCodeScanner.isValidVerificationQR(validQR1))
        
        val validQR2 = "chain://verify?user=user123&name=John%20Doe&key=def456&ts=9876543210"
        assertTrue(qrCodeGenerator.validateQRData(validQR2))
        assertTrue(qrCodeScanner.isValidVerificationQR(validQR2))
        
        // Test invalid QR codes
        val invalidQR1 = "http://verify?user=test&name=Test&key=abc123&ts=1234567890" // Wrong scheme
        assertFalse(qrCodeGenerator.validateQRData(invalidQR1))
        assertFalse(qrCodeScanner.isValidVerificationQR(invalidQR1))
        
        val invalidQR2 = "chain://verify?user=test&name=Test&key=abc123" // Missing timestamp
        assertFalse(qrCodeGenerator.validateQRData(invalidQR2))
        assertFalse(qrCodeScanner.isValidVerificationQR(invalidQR2))
    }
    
    @Test
    fun `safety number format validation should work correctly`() {
        // Test valid formats
        val validSafetyNumber1 = "12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345 67890"
        assertTrue(safetyNumberGenerator.isValidSafetyNumberFormat(validSafetyNumber1))
        
        val validSafetyNumber2 = "123456789012345678901234567890123456789012345678901234567890"
        assertTrue(safetyNumberGenerator.isValidSafetyNumberFormat(validSafetyNumber2))
        
        // Test invalid formats
        val invalidSafetyNumber1 = "12345 67890 12345" // Too short
        assertFalse(safetyNumberGenerator.isValidSafetyNumberFormat(invalidSafetyNumber1))
        
        val invalidSafetyNumber2 = "12345 67890 abcde 67890 12345 67890 12345 67890 12345 67890 12345 67890" // Contains letters
        assertFalse(safetyNumberGenerator.isValidSafetyNumberFormat(invalidSafetyNumber2))
    }
}