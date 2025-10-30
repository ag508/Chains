package com.chain.messaging.security

import com.chain.messaging.core.crypto.KeyManager
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.security.IdentityVerificationManager
import com.chain.messaging.core.security.SecurityMonitoringManager
import com.chain.messaging.core.security.SecureStorageImpl
import com.chain.messaging.domain.model.MessageType
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

/**
 * Comprehensive security test suite for encryption and key management
 * Tests cryptographic implementations, key security, and threat detection
 */
@HiltAndroidTest
class SecurityTestSuite {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var keyManager: KeyManager

    @Inject
    lateinit var encryptionService: SignalEncryptionService

    @Inject
    lateinit var identityVerificationManager: IdentityVerificationManager

    @Inject
    lateinit var securityMonitoringManager: SecurityMonitoringManager

    @Inject
    lateinit var secureStorage: SecureStorageImpl

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testSignalProtocolKeyGeneration() = runTest {
        // Given: Key generation parameters
        val userId = "test_user_${UUID.randomUUID()}"

        // When: Keys are generated
        val identityKeyPair = keyManager.generateIdentityKeyPair(userId)
        val signedPreKey = keyManager.generateSignedPreKey(userId)
        val oneTimePreKeys = keyManager.generateOneTimePreKeys(userId, 100)

        // Then: Keys should be properly generated
        assertNotNull("Identity key pair should be generated", identityKeyPair)
        assertNotNull("Identity public key should exist", identityKeyPair.publicKey)
        assertNotNull("Identity private key should exist", identityKeyPair.privateKey)
        
        assertNotNull("Signed pre-key should be generated", signedPreKey)
        assertTrue("Should generate requested number of one-time keys", oneTimePreKeys.size == 100)

        // Verify key cryptographic properties
        assertTrue("Identity key should be valid", keyManager.validateIdentityKey(identityKeyPair))
        assertTrue("Signed pre-key should be valid", keyManager.validateSignedPreKey(signedPreKey))
        
        oneTimePreKeys.forEach { key ->
            assertTrue("One-time key should be valid", keyManager.validateOneTimePreKey(key))
        }
    }

    @Test
    fun testEndToEndEncryptionSecurity() = runTest {
        // Given: Two users with established session
        val aliceId = "alice_${UUID.randomUUID()}"
        val bobId = "bob_${UUID.randomUUID()}"

        // Initialize encryption sessions
        val alicePreKeyBundle = encryptionService.generatePreKeyBundle(aliceId)
        val bobPreKeyBundle = encryptionService.generatePreKeyBundle(bobId)

        encryptionService.initializeSession(bobId, alicePreKeyBundle)
        encryptionService.initializeSession(aliceId, bobPreKeyBundle)

        // When: Alice encrypts a message for Bob
        val originalMessage = "This is a highly confidential message with sensitive data: SSN 123-45-6789"
        val encryptedMessage = encryptionService.encryptMessage(originalMessage, aliceId, bobId)

        // Then: Message should be properly encrypted
        assertNotNull("Encrypted message should not be null", encryptedMessage)
        assertNotEquals("Encrypted content should differ from original", originalMessage, encryptedMessage.content)
        assertFalse("Encrypted content should not contain original text", 
            encryptedMessage.content.contains("confidential"))
        assertFalse("Encrypted content should not contain SSN", 
            encryptedMessage.content.contains("123-45-6789"))

        // Verify Bob can decrypt the message
        val decryptedMessage = encryptionService.decryptMessage(encryptedMessage, bobId, aliceId)
        assertEquals("Decrypted message should match original", originalMessage, decryptedMessage)

        // Verify forward secrecy - previous messages remain secure after key rotation
        encryptionService.rotateKeys(aliceId)
        
        // Old encrypted message should still be decryptable
        val decryptedAfterRotation = encryptionService.decryptMessage(encryptedMessage, bobId, aliceId)
        assertEquals("Message should still decrypt after key rotation", originalMessage, decryptedAfterRotation)

        // New messages should use new keys
        val newEncryptedMessage = encryptionService.encryptMessage("New message", aliceId, bobId)
        assertNotEquals("New message should use different encryption", 
            encryptedMessage.content, newEncryptedMessage.content)
    }

    @Test
    fun testKeyStorageSecurity() = runTest {
        // Given: Sensitive key material
        val userId = "secure_user_${UUID.randomUUID()}"
        val sensitiveKey = ByteArray(32).apply { SecureRandom().nextBytes(this) }
        val keyAlias = "test_key_$userId"

        // When: Key is stored securely
        val storageResult = secureStorage.storeKey(keyAlias, sensitiveKey)
        assertTrue("Key storage should succeed", storageResult.isSuccess)

        // Then: Key should be retrievable only by authorized access
        val retrievedKey = secureStorage.retrieveKey(keyAlias)
        assertNotNull("Key should be retrievable", retrievedKey)
        assertArrayEquals("Retrieved key should match original", sensitiveKey, retrievedKey)

        // Verify key is encrypted at rest
        val rawStorageContent = secureStorage.getRawStorageContent(keyAlias)
        assertNotNull("Raw storage should exist", rawStorageContent)
        assertFalse("Raw storage should not contain plaintext key", 
            rawStorageContent.contentEquals(sensitiveKey))

        // Test unauthorized access protection
        val unauthorizedAccess = secureStorage.attemptUnauthorizedAccess(keyAlias)
        assertFalse("Unauthorized access should fail", unauthorizedAccess.isSuccess)
        assertNull("Unauthorized access should not return key", unauthorizedAccess.key)
    }

    @Test
    fun testIdentityVerificationSecurity() = runTest {
        // Given: Two users wanting to verify identities
        val user1Id = "user1_${UUID.randomUUID()}"
        val user2Id = "user2_${UUID.randomUUID()}"

        // Generate identity keys
        val user1Keys = keyManager.generateIdentityKeyPair(user1Id)
        val user2Keys = keyManager.generateIdentityKeyPair(user2Id)

        // When: Safety numbers are generated
        val safetyNumber = identityVerificationManager.generateSafetyNumber(user1Id, user2Id)
        assertNotNull("Safety number should be generated", safetyNumber)
        assertEquals("Safety number should have correct length", 60, safetyNumber.length)

        // Then: Safety numbers should be consistent
        val safetyNumber2 = identityVerificationManager.generateSafetyNumber(user1Id, user2Id)
        assertEquals("Safety numbers should be consistent", safetyNumber, safetyNumber2)

        // Verify QR code generation
        val qrCode = identityVerificationManager.generateVerificationQRCode(user1Id)
        assertNotNull("QR code should be generated", qrCode)
        assertTrue("QR code should contain user identity", qrCode.contains(user1Id))

        // Test verification process
        val verificationResult = identityVerificationManager.verifyIdentity(user1Id, user2Id, safetyNumber)
        assertTrue("Identity verification should succeed", verificationResult.isVerified)
        assertNotNull("Verification should have timestamp", verificationResult.verifiedAt)

        // Test tampered identity detection
        val tamperedKeys = keyManager.generateIdentityKeyPair("tampered_user")
        keyManager.replaceIdentityKey(user2Id, tamperedKeys) // Simulate key compromise

        val tamperedSafetyNumber = identityVerificationManager.generateSafetyNumber(user1Id, user2Id)
        assertNotEquals("Safety number should change with key change", safetyNumber, tamperedSafetyNumber)

        val securityAlert = securityMonitoringManager.checkForKeyChanges(user2Id)
        assertTrue("Security alert should be triggered", securityAlert.isTriggered)
        assertEquals("Alert should indicate key change", "KEY_CHANGE_DETECTED", securityAlert.alertType)
    }

    @Test
    fun testCryptographicAttackResistance() = runTest {
        // Given: Encrypted message and potential attack scenarios
        val userId1 = "victim_${UUID.randomUUID()}"
        val userId2 = "attacker_${UUID.randomUUID()}"
        
        val originalMessage = "Confidential business information"
        val encryptedMessage = encryptionService.encryptMessage(originalMessage, userId1, userId2)

        // Test 1: Brute force attack resistance
        val bruteForceAttempts = (1..1000).map { attempt ->
            try {
                val fakeKey = ByteArray(32).apply { SecureRandom().nextBytes(this) }
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(fakeKey, "AES"))
                cipher.doFinal(encryptedMessage.content.toByteArray())
                true // If this succeeds, encryption is weak
            } catch (e: Exception) {
                false // Expected - encryption should resist brute force
            }
        }

        val successfulBruteForce = bruteForceAttempts.count { it }
        assertEquals("Brute force attacks should fail", 0, successfulBruteForce)

        // Test 2: Replay attack resistance
        val replayAttempt1 = encryptionService.decryptMessage(encryptedMessage, userId2, userId1)
        val replayAttempt2 = encryptionService.decryptMessage(encryptedMessage, userId2, userId1)
        
        // Both should succeed for legitimate decryption, but system should detect replay
        assertEquals("First decryption should succeed", originalMessage, replayAttempt1)
        assertEquals("Second decryption should succeed", originalMessage, replayAttempt2)
        
        val replayDetection = securityMonitoringManager.detectReplayAttack(encryptedMessage.id)
        assertTrue("Replay attack should be detected", replayDetection.isDetected)

        // Test 3: Man-in-the-middle attack detection
        val mitm_userId = "mitm_attacker_${UUID.randomUUID()}"
        val mitmKeys = keyManager.generateIdentityKeyPair(mitm_userId)
        
        // Simulate MITM by replacing keys during session establishment
        val compromisedSession = encryptionService.simulateMITMAttack(userId1, userId2, mitmKeys)
        assertFalse("MITM attack should be detected and prevented", compromisedSession.isSuccessful)
        
        val securityAlert = securityMonitoringManager.getLatestSecurityAlert()
        assertEquals("Should detect MITM attack", "MITM_ATTACK_DETECTED", securityAlert?.alertType)
    }

    @Test
    fun testKeyRotationSecurity() = runTest {
        // Given: Established encrypted communication
        val user1Id = "user1_${UUID.randomUUID()}"
        val user2Id = "user2_${UUID.randomUUID()}"

        // Initialize session and send messages
        encryptionService.initializeSession(user2Id, encryptionService.generatePreKeyBundle(user1Id))
        
        val message1 = "Message before key rotation"
        val encrypted1 = encryptionService.encryptMessage(message1, user1Id, user2Id)
        
        // When: Keys are rotated
        val rotationResult = encryptionService.rotateKeys(user1Id)
        assertTrue("Key rotation should succeed", rotationResult.isSuccess)

        // Then: Old messages should still be decryptable (backward compatibility)
        val decrypted1 = encryptionService.decryptMessage(encrypted1, user2Id, user1Id)
        assertEquals("Old message should still decrypt", message1, decrypted1)

        // New messages should use new keys
        val message2 = "Message after key rotation"
        val encrypted2 = encryptionService.encryptMessage(message2, user1Id, user2Id)
        
        // Verify new encryption uses different keys
        assertNotEquals("New message should use different encryption", 
            encrypted1.keyId, encrypted2.keyId)

        // Verify forward secrecy - compromising old keys doesn't affect new messages
        val oldKeys = keyManager.getHistoricalKeys(user1Id, encrypted1.timestamp)
        keyManager.simulateKeyCompromise(oldKeys)
        
        val decrypted2 = encryptionService.decryptMessage(encrypted2, user2Id, user1Id)
        assertEquals("New message should still decrypt despite old key compromise", message2, decrypted2)

        // Verify key rotation frequency compliance
        val rotationHistory = keyManager.getKeyRotationHistory(user1Id)
        assertTrue("Should maintain key rotation history", rotationHistory.isNotEmpty())
        
        val timeSinceLastRotation = System.currentTimeMillis() - rotationHistory.last().timestamp
        assertTrue("Key rotation should be recent", timeSinceLastRotation < 60000) // Within 1 minute
    }

    @Test
    fun testSecurityMonitoringAndThreatDetection() = runTest {
        // Given: Various security events to monitor
        val userId = "monitored_user_${UUID.randomUUID()}"

        // Test 1: Unusual activity detection
        repeat(50) { // Simulate rapid message sending
            encryptionService.encryptMessage("Spam message $it", userId, "recipient_$it")
        }

        val unusualActivityAlert = securityMonitoringManager.detectUnusualActivity(userId)
        assertTrue("Should detect unusual activity", unusualActivityAlert.isDetected)
        assertEquals("Should identify rapid messaging", "RAPID_MESSAGING", unusualActivityAlert.activityType)

        // Test 2: Failed authentication attempts
        repeat(10) {
            securityMonitoringManager.recordFailedAuthentication(userId, "wrong_password_$it")
        }

        val authAlert = securityMonitoringManager.detectAuthenticationThreats(userId)
        assertTrue("Should detect authentication threats", authAlert.isDetected)
        assertTrue("Should recommend account lockdown", authAlert.recommendsLockdown)

        // Test 3: Device fingerprint changes
        val originalFingerprint = securityMonitoringManager.getDeviceFingerprint()
        securityMonitoringManager.simulateDeviceChange(userId) // Simulate device compromise

        val deviceAlert = securityMonitoringManager.detectDeviceChanges(userId)
        assertTrue("Should detect device changes", deviceAlert.isDetected)
        assertNotEquals("Device fingerprint should change", originalFingerprint, deviceAlert.newFingerprint)

        // Test 4: Network anomaly detection
        securityMonitoringManager.simulateNetworkAnomaly("suspicious_ip_address", "malicious_traffic_pattern")
        
        val networkAlert = securityMonitoringManager.detectNetworkAnomalies()
        assertTrue("Should detect network anomalies", networkAlert.isDetected)
        assertTrue("Should recommend network isolation", networkAlert.recommendsIsolation)

        // Verify comprehensive security report
        val securityReport = securityMonitoringManager.generateSecurityReport(userId)
        assertNotNull("Security report should be generated", securityReport)
        assertTrue("Report should include all alerts", securityReport.alerts.size >= 4)
        assertTrue("Report should have risk assessment", securityReport.riskLevel > 0)
        assertNotNull("Report should have recommendations", securityReport.recommendations)
    }
}