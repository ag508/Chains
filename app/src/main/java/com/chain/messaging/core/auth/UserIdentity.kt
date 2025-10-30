package com.chain.messaging.core.auth

import org.signal.libsignal.protocol.IdentityKey

/**
 * Represents a user's identity in the Chain messaging system
 */
data class UserIdentity(
    val userId: String,
    val username: String,
    val email: String,
    val displayName: String,
    val profilePictureUrl: String? = null,
    val blockchainPublicKey: ByteArray,
    val signalIdentityKey: IdentityKey,
    val createdAt: Long,
    val lastLoginAt: Long,
    val isVerified: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserIdentity

        if (userId != other.userId) return false
        if (username != other.username) return false
        if (email != other.email) return false
        if (displayName != other.displayName) return false
        if (profilePictureUrl != other.profilePictureUrl) return false
        if (!blockchainPublicKey.contentEquals(other.blockchainPublicKey)) return false
        if (signalIdentityKey != other.signalIdentityKey) return false
        if (createdAt != other.createdAt) return false
        if (lastLoginAt != other.lastLoginAt) return false
        if (isVerified != other.isVerified) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + (profilePictureUrl?.hashCode() ?: 0)
        result = 31 * result + blockchainPublicKey.contentHashCode()
        result = 31 * result + signalIdentityKey.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + lastLoginAt.hashCode()
        result = 31 * result + isVerified.hashCode()
        return result
    }
}

/**
 * OAuth data from external providers
 */
data class OAuthData(
    val providerId: String,
    val providerUserId: String,
    val email: String,
    val name: String,
    val profilePictureUrl: String? = null,
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAt: Long
)

/**
 * Passkey authentication data
 */
data class PasskeyData(
    val credentialId: String,
    val publicKey: ByteArray,
    val signature: ByteArray,
    val authenticatorData: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PasskeyData

        if (credentialId != other.credentialId) return false
        if (!publicKey.contentEquals(other.publicKey)) return false
        if (!signature.contentEquals(other.signature)) return false
        if (!authenticatorData.contentEquals(other.authenticatorData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = credentialId.hashCode()
        result = 31 * result + publicKey.contentHashCode()
        result = 31 * result + signature.contentHashCode()
        result = 31 * result + authenticatorData.contentHashCode()
        return result
    }
}