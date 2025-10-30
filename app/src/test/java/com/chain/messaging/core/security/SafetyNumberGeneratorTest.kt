package com.chain.messaging.core.security

import org.junit.Before
import org.junit.Test
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.ecc.Curve
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SafetyNumberGeneratorTest {
    
    private lateinit var safetyNumberGenerator: SafetyNumberGenerator
    private lateinit var localIdentityKey: IdentityKey
    private lateinit var remoteIdentityKey: IdentityKey
    private val localUserId = "local-user-123"
    private val remoteUserId = "remote-user-456"
    
    @Before
    fun setup() {
        safetyNumberGenerator = SafetyNumberGenerator()
        localIdentityKey = IdentityKey(Curve.generateKeyPair().publicKey)
        remoteIdentityKey = IdentityKey(Curve.generateKeyPair().publicKey)
    }
    
    @Test
    fun `generateSafetyNumber should return 60-digit number with spaces`() {
        // When
        val safetyNumber = safetyNumberGenerator.generateSafetyNumber(
            localIdentityKey, remoteIdentityKey, localUserId, remoteUserId
        )
        
        // Then
        // Should be 60 digits + 11 spaces = 71 characters total
        assertEquals(71, safetyNumber.length)
        
        // Should have spaces every 5 digits
        val parts = safetyNumber.split(" ")
        assertEquals(12, parts.size)
        parts.forEach { part ->
            assertEquals(5, part.length)
            assertTrue(part.all { it.isDigit() })
        }
    }
    
    @Test
    fun `generateSafetyNumber should be deterministic for same inputs`() {
        // When
        val safetyNumber1 = safetyNumberGenerator.generateSafetyNumber(
            localIdentityKey, remoteIdentityKey, localUserId, remoteUserId
        )
        val safetyNumber2 = safetyNumberGenerator.generateSafetyNumber(
            localIdentityKey, remoteIdentityKey, localUserId, remoteUserId
        )
        
        // Then
        assertEquals(safetyNumber1, safetyNumber2)
    }
    
    @Test
    fun `generateSafetyNumber should be symmetric for swapped users`() {
        // When
        val safetyNumber1 = safetyNumberGenerator.generateSafetyNumber(
            localIdentityKey, remoteIdentityKey, localUserId, remoteUserId
        )
        val safetyNumber2 = safetyNumberGenerator.generateSafetyNumber(
            remoteIdentityKey, localIdentityKey, remoteUserId, localUserId
        )
        
        // Then
        assertEquals(safetyNumber1, safetyNumber2)
    }
    
    @Test
    fun `generateSafetyNumber should be different for different keys`() {
        // Given
        val differentRemoteKey = IdentityKey(Curve.generateKeyPair().publicKey)
        
        // When
        val safetyNumber1 = safetyNumberGenerator.generateSafetyNumber(
            localIdentityKey, remoteIdentityKey, localUserId, remoteUserId
        )
        val safetyNumber2 = safetyNumberGenerator.generateSafetyNumber(
            localIdentityKey, differentRemoteKey, localUserId, remoteUserId
        )
        
        // Then
        assertNotEquals(safetyNumber1, safetyNumber2)
    }
    
    @Test
    fun `generateSafetyNumber should be different for different user IDs`() {
        // Given
        val differentRemoteUserId = "different-remote-user"
        
        // When
        val safetyNumber1 = safetyNumberGenerator.generateSafetyNumber(
            localIdentityKey, remoteIdentityKey, localUserId, remoteUserId
        )
        val safetyNumber2 = safetyNumberGenerator.generateSafetyNumber(
            localIdentityKey, remoteIdentityKey, localUserId, differentRemoteUserId
        )
        
        // Then
        assertNotEquals(safetyNumber1, safetyNumber2)
    }
    
    @Test
    fun `isValidSafetyNumberFormat should return true for valid format`() {
        // Given
        val validSafetyNumber = "12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345 67890"
        
        // When
        val isValid = safetyNumberGenerator.isValidSafetyNumberFormat(validSafetyNumber)
        
        // Then
        assertTrue(isValid)
    }
    
    @Test
    fun `isValidSafetyNumberFormat should return true for valid format without spaces`() {
        // Given
        val validSafetyNumber = "123456789012345678901234567890123456789012345678901234567890"
        
        // When
        val isValid = safetyNumberGenerator.isValidSafetyNumberFormat(validSafetyNumber)
        
        // Then
        assertTrue(isValid)
    }
    
    @Test
    fun `isValidSafetyNumberFormat should return false for too short number`() {
        // Given
        val shortSafetyNumber = "12345 67890 12345"
        
        // When
        val isValid = safetyNumberGenerator.isValidSafetyNumberFormat(shortSafetyNumber)
        
        // Then
        assertFalse(isValid)
    }
    
    @Test
    fun `isValidSafetyNumberFormat should return false for too long number`() {
        // Given
        val longSafetyNumber = "12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345"
        
        // When
        val isValid = safetyNumberGenerator.isValidSafetyNumberFormat(longSafetyNumber)
        
        // Then
        assertFalse(isValid)
    }
    
    @Test
    fun `isValidSafetyNumberFormat should return false for non-numeric characters`() {
        // Given
        val invalidSafetyNumber = "12345 67890 abcde 67890 12345 67890 12345 67890 12345 67890 12345 67890"
        
        // When
        val isValid = safetyNumberGenerator.isValidSafetyNumberFormat(invalidSafetyNumber)
        
        // Then
        assertFalse(isValid)
    }
    
    @Test
    fun `normalizeSafetyNumber should remove spaces`() {
        // Given
        val safetyNumberWithSpaces = "12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345 67890"
        val expectedNormalized = "123456789012345678901234567890123456789012345678901234567890"
        
        // When
        val normalized = safetyNumberGenerator.normalizeSafetyNumber(safetyNumberWithSpaces)
        
        // Then
        assertEquals(expectedNormalized, normalized)
    }
    
    @Test
    fun `compareSafetyNumbers should return true for same numbers with different spacing`() {
        // Given
        val safetyNumber1 = "12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345 67890"
        val safetyNumber2 = "123456789012345678901234567890123456789012345678901234567890"
        
        // When
        val areEqual = safetyNumberGenerator.compareSafetyNumbers(safetyNumber1, safetyNumber2)
        
        // Then
        assertTrue(areEqual)
    }
    
    @Test
    fun `compareSafetyNumbers should return false for different numbers`() {
        // Given
        val safetyNumber1 = "12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345 67890"
        val safetyNumber2 = "54321 09876 54321 09876 54321 09876 54321 09876 54321 09876 54321 09876"
        
        // When
        val areEqual = safetyNumberGenerator.compareSafetyNumbers(safetyNumber1, safetyNumber2)
        
        // Then
        assertFalse(areEqual)
    }
    
    @Test
    fun `generateShortSafetyNumber should return abbreviated version`() {
        // When
        val shortSafetyNumber = safetyNumberGenerator.generateShortSafetyNumber(
            localIdentityKey, remoteIdentityKey, localUserId, remoteUserId
        )
        
        // Then
        // Should be in format "1234567890...1234567890" (23 characters)
        assertEquals(23, shortSafetyNumber.length)
        assertTrue(shortSafetyNumber.contains("..."))
        
        val parts = shortSafetyNumber.split("...")
        assertEquals(2, parts.size)
        assertEquals(10, parts[0].length)
        assertEquals(10, parts[1].length)
        assertTrue(parts[0].all { it.isDigit() })
        assertTrue(parts[1].all { it.isDigit() })
    }
    
    @Test
    fun `generateQRSafetyNumber should return normalized safety number`() {
        // When
        val qrSafetyNumber = safetyNumberGenerator.generateQRSafetyNumber(
            localIdentityKey, remoteIdentityKey, localUserId, remoteUserId
        )
        
        // Then
        assertEquals(60, qrSafetyNumber.length)
        assertTrue(qrSafetyNumber.all { it.isDigit() })
        assertFalse(qrSafetyNumber.contains(" "))
    }
    
    @Test
    fun `generateSafetyNumber should handle empty user IDs`() {
        // When
        val safetyNumber = safetyNumberGenerator.generateSafetyNumber(
            localIdentityKey, remoteIdentityKey, "", ""
        )
        
        // Then
        // Should still generate a valid safety number
        assertEquals(71, safetyNumber.length)
        assertTrue(safetyNumberGenerator.isValidSafetyNumberFormat(safetyNumber))
    }
    
    @Test
    fun `generateSafetyNumber should handle unicode user IDs`() {
        // Given
        val unicodeLocalUserId = "用户123"
        val unicodeRemoteUserId = "пользователь456"
        
        // When
        val safetyNumber = safetyNumberGenerator.generateSafetyNumber(
            localIdentityKey, remoteIdentityKey, unicodeLocalUserId, unicodeRemoteUserId
        )
        
        // Then
        assertEquals(71, safetyNumber.length)
        assertTrue(safetyNumberGenerator.isValidSafetyNumberFormat(safetyNumber))
    }
}