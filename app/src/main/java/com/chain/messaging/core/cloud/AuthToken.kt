package com.chain.messaging.core.cloud

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

/**
 * Represents an OAuth authentication token for cloud services
 */
@Serializable
data class AuthToken(
    val accessToken: String,
    val refreshToken: String?,
    @Serializable(with = InstantSerializer::class)
    val expiresAt: Instant,
    val tokenType: String = "Bearer",
    val scope: String? = null
) {
    /**
     * Check if the token is expired or will expire within the next 5 minutes
     */
    fun isExpired(): Boolean {
        return Instant.now().plusSeconds(300).isAfter(expiresAt)
    }
    
    /**
     * Check if the token can be refreshed
     */
    fun canRefresh(): Boolean {
        return refreshToken != null
    }
}

/**
 * Represents the result of an authentication attempt
 */
sealed class AuthResult {
    data class Success(val token: AuthToken) : AuthResult()
    data class Error(val message: String, val cause: Throwable? = null) : AuthResult()
    object Cancelled : AuthResult()
}

/**
 * Represents cloud service account information
 */
@Serializable
data class CloudAccount(
    val service: CloudService,
    val userId: String,
    val email: String,
    val displayName: String,
    val token: AuthToken
)

/**
 * Serializer for java.time.Instant
 */
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.LONG)
    
    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.epochSecond)
    }
    
    override fun deserialize(decoder: Decoder): Instant {
        return Instant.ofEpochSecond(decoder.decodeLong())
    }
}