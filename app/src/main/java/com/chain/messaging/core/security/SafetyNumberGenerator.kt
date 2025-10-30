package com.chain.messaging.core.security

import org.signal.libsignal.protocol.IdentityKey
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates safety numbers for manual identity verification
 * Based on Signal Protocol's safety number generation
 */
@Singleton
class SafetyNumberGenerator @Inject constructor() {
    
    companion object {
        private const val SAFETY_NUMBER_LENGTH = 60
        private const val CHUNK_SIZE = 5
        private const val ITERATIONS = 5200
    }
    
    /**
     * Generate a safety number for two users
     */
    fun generateSafetyNumber(
        localIdentityKey: IdentityKey,
        remoteIdentityKey: IdentityKey,
        localUserId: String,
        remoteUserId: String
    ): String {
        val localFingerprint = generateFingerprint(localIdentityKey, localUserId)
        val remoteFingerprint = generateFingerprint(remoteIdentityKey, remoteUserId)
        
        // Combine fingerprints in a deterministic order
        val combinedFingerprint = if (localFingerprint.contentCompareTo(remoteFingerprint) < 0) {
            localFingerprint + remoteFingerprint
        } else {
            remoteFingerprint + localFingerprint
        }
        
        return formatSafetyNumber(combinedFingerprint)
    }
    
    /**
     * Generate fingerprint for a single identity
     */
    private fun generateFingerprint(identityKey: IdentityKey, userId: String): ByteArray {
        val publicKeyBytes = identityKey.serialize()
        val userIdBytes = userId.toByteArray(Charsets.UTF_8)
        
        // Combine identity key and user ID
        val combined = publicKeyBytes + userIdBytes
        
        // Use PBKDF2-like iteration to strengthen the fingerprint
        var hash = combined
        repeat(ITERATIONS) {
            hash = MessageDigest.getInstance("SHA-256").digest(hash)
        }
        
        return hash
    }
    
    /**
     * Format the safety number for display
     */
    private fun formatSafetyNumber(fingerprint: ByteArray): String {
        // Convert to numeric string
        val numericString = fingerprintToNumericString(fingerprint)
        
        // Format with spaces every 5 digits
        return numericString.chunked(CHUNK_SIZE).joinToString(" ")
    }
    
    /**
     * Convert fingerprint bytes to numeric string
     */
    private fun fingerprintToNumericString(fingerprint: ByteArray): String {
        val builder = StringBuilder()
        
        // Take first 30 bytes and convert to 60-digit number
        for (i in 0 until minOf(30, fingerprint.size)) {
            val byte = fingerprint[i].toInt() and 0xFF
            builder.append(String.format("%02d", byte % 100))
        }
        
        // Ensure we have exactly 60 digits
        val result = builder.toString()
        return if (result.length >= SAFETY_NUMBER_LENGTH) {
            result.substring(0, SAFETY_NUMBER_LENGTH)
        } else {
            result.padEnd(SAFETY_NUMBER_LENGTH, '0')
        }
    }
    
    /**
     * Validate a safety number format
     */
    fun isValidSafetyNumberFormat(safetyNumber: String): Boolean {
        // Remove spaces and check if it's 60 digits
        val cleaned = safetyNumber.replace(" ", "")
        return cleaned.length == SAFETY_NUMBER_LENGTH && cleaned.all { it.isDigit() }
    }
    
    /**
     * Normalize safety number by removing spaces
     */
    fun normalizeSafetyNumber(safetyNumber: String): String {
        return safetyNumber.replace(" ", "")
    }
    
    /**
     * Compare two safety numbers
     */
    fun compareSafetyNumbers(safetyNumber1: String, safetyNumber2: String): Boolean {
        val normalized1 = normalizeSafetyNumber(safetyNumber1)
        val normalized2 = normalizeSafetyNumber(safetyNumber2)
        return normalized1 == normalized2
    }
    
    /**
     * Generate a shortened safety number for display in limited space
     */
    fun generateShortSafetyNumber(
        localIdentityKey: IdentityKey,
        remoteIdentityKey: IdentityKey,
        localUserId: String,
        remoteUserId: String
    ): String {
        val fullSafetyNumber = generateSafetyNumber(localIdentityKey, remoteIdentityKey, localUserId, remoteUserId)
        val normalized = normalizeSafetyNumber(fullSafetyNumber)
        
        // Return first and last 10 digits with ellipsis
        return "${normalized.substring(0, 10)}...${normalized.substring(50, 60)}"
    }
    
    /**
     * Generate QR-friendly safety number (without spaces)
     */
    fun generateQRSafetyNumber(
        localIdentityKey: IdentityKey,
        remoteIdentityKey: IdentityKey,
        localUserId: String,
        remoteUserId: String
    ): String {
        return normalizeSafetyNumber(
            generateSafetyNumber(localIdentityKey, remoteIdentityKey, localUserId, remoteUserId)
        )
    }
}

/**
 * Extension function for byte array comparison
 */
private fun ByteArray.contentCompareTo(other: ByteArray): Int {
    val minLength = minOf(this.size, other.size)
    for (i in 0 until minLength) {
        val comparison = this[i].compareTo(other[i])
        if (comparison != 0) return comparison
    }
    return this.size.compareTo(other.size)
}