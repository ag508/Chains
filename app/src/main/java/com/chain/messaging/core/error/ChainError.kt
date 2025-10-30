package com.chain.messaging.core.error

import java.io.IOException

/**
 * Base sealed class for all Chain messaging errors
 */
sealed class ChainError(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * Network-related errors
     */
    sealed class NetworkError(message: String, cause: Throwable? = null) : ChainError(message, cause) {
        class ConnectionTimeout(cause: Throwable? = null) : NetworkError("Network connection timeout", cause)
        class NoInternet(cause: Throwable? = null) : NetworkError("No internet connection available", cause)
        class ServerUnreachable(serverType: String, cause: Throwable? = null) : NetworkError("$serverType server unreachable", cause)
        class RateLimited(retryAfter: Long? = null) : NetworkError("Rate limit exceeded${retryAfter?.let { ", retry after ${it}ms" } ?: ""}")
        class BadResponse(statusCode: Int, cause: Throwable? = null) : NetworkError("Bad response: $statusCode", cause)
        class NetworkUnavailable(cause: Throwable? = null) : NetworkError("Network temporarily unavailable", cause)
    }
    
    /**
     * Blockchain-related errors
     */
    sealed class BlockchainError(message: String, cause: Throwable? = null) : ChainError(message, cause) {
        class NodeUnavailable(cause: Throwable? = null) : BlockchainError("Blockchain node unavailable", cause)
        class TransactionFailed(txHash: String?, cause: Throwable? = null) : BlockchainError("Transaction failed${txHash?.let { ": $it" } ?: ""}", cause)
        class InsufficientFunds(cause: Throwable? = null) : BlockchainError("Insufficient funds for transaction", cause)
        class InvalidTransaction(reason: String, cause: Throwable? = null) : BlockchainError("Invalid transaction: $reason", cause)
        class ConsensusFailure(cause: Throwable? = null) : BlockchainError("Blockchain consensus failure", cause)
        class SyncFailure(cause: Throwable? = null) : BlockchainError("Blockchain synchronization failed", cause)
    }
    
    /**
     * Encryption-related errors
     */
    sealed class EncryptionError(message: String, cause: Throwable? = null) : ChainError(message, cause) {
        class KeyGenerationFailed(cause: Throwable? = null) : EncryptionError("Failed to generate encryption keys", cause)
        class KeyExchangeFailed(userId: String, cause: Throwable? = null) : EncryptionError("Key exchange failed with user: $userId", cause)
        class EncryptionFailed(cause: Throwable? = null) : EncryptionError("Message encryption failed", cause)
        class DecryptionFailed(cause: Throwable? = null) : EncryptionError("Message decryption failed", cause)
        class InvalidSignature(cause: Throwable? = null) : EncryptionError("Invalid message signature", cause)
        class KeyStorageError(cause: Throwable? = null) : EncryptionError("Key storage operation failed", cause)
        class SessionNotFound(userId: String) : EncryptionError("Encryption session not found for user: $userId")
    }
    
    /**
     * Storage-related errors
     */
    sealed class StorageError(message: String, cause: Throwable? = null) : ChainError(message, cause) {
        class DatabaseError(operation: String, cause: Throwable? = null) : StorageError("Database $operation failed", cause)
        class FileSystemError(operation: String, cause: Throwable? = null) : StorageError("File system $operation failed", cause)
        class CloudStorageError(service: String, operation: String, cause: Throwable? = null) : StorageError("$service $operation failed", cause)
        class StorageQuotaExceeded(service: String) : StorageError("Storage quota exceeded for $service")
        class CorruptedData(dataType: String, cause: Throwable? = null) : StorageError("Corrupted $dataType data", cause)
        class AccessDenied(resource: String, cause: Throwable? = null) : StorageError("Access denied to $resource", cause)
    }
    
    /**
     * Authentication-related errors
     */
    sealed class AuthError(message: String, cause: Throwable? = null) : ChainError(message, cause) {
        class InvalidCredentials(cause: Throwable? = null) : AuthError("Invalid credentials", cause)
        class AuthenticationFailed(provider: String, cause: Throwable? = null) : AuthError("Authentication failed with $provider", cause)
        class TokenExpired(cause: Throwable? = null) : AuthError("Authentication token expired", cause)
        class BiometricUnavailable(cause: Throwable? = null) : AuthError("Biometric authentication unavailable", cause)
        class PasskeyError(cause: Throwable? = null) : AuthError("Passkey authentication failed", cause)
        class PermissionDenied(permission: String) : AuthError("Permission denied: $permission")
    }
    
    /**
     * WebRTC call-related errors
     */
    sealed class CallError(message: String, cause: Throwable? = null) : ChainError(message, cause) {
        class ConnectionFailed(cause: Throwable? = null) : CallError("Call connection failed", cause)
        class MediaDeviceError(device: String, cause: Throwable? = null) : CallError("$device device error", cause)
        class SignalingError(cause: Throwable? = null) : CallError("Call signaling error", cause)
        class IceConnectionFailed(cause: Throwable? = null) : CallError("ICE connection failed", cause)
        class CallRejected(reason: String? = null) : CallError("Call rejected${reason?.let { ": $it" } ?: ""}")
        class CallTimeout(cause: Throwable? = null) : CallError("Call connection timeout", cause)
    }
    
    /**
     * P2P networking errors
     */
    sealed class P2PError(message: String, cause: Throwable? = null) : ChainError(message, cause) {
        class PeerDiscoveryFailed(cause: Throwable? = null) : P2PError("Peer discovery failed", cause)
        class PeerConnectionFailed(peerId: String, cause: Throwable? = null) : P2PError("Connection to peer $peerId failed", cause)
        class MessageRoutingFailed(cause: Throwable? = null) : P2PError("Message routing failed", cause)
        class DHTError(operation: String, cause: Throwable? = null) : P2PError("DHT $operation failed", cause)
        class NATTraversalFailed(cause: Throwable? = null) : P2PError("NAT traversal failed", cause)
    }
    
    /**
     * User interface errors
     */
    sealed class UIError(message: String, cause: Throwable? = null) : ChainError(message, cause) {
        class InvalidInput(field: String, reason: String) : UIError("Invalid $field: $reason")
        class MediaAccessError(mediaType: String, cause: Throwable? = null) : UIError("Cannot access $mediaType", cause)
        class RenderingError(component: String, cause: Throwable? = null) : UIError("Error rendering $component", cause)
        class NavigationError(destination: String, cause: Throwable? = null) : UIError("Navigation to $destination failed", cause)
    }
    
    /**
     * Generic system errors
     */
    sealed class SystemError(message: String, cause: Throwable? = null) : ChainError(message, cause) {
        class OutOfMemory(cause: Throwable? = null) : SystemError("Out of memory", cause)
        class ConfigurationError(setting: String, cause: Throwable? = null) : SystemError("Configuration error: $setting", cause)
        class ServiceUnavailable(service: String, cause: Throwable? = null) : SystemError("$service service unavailable", cause)
        class UnexpectedError(cause: Throwable? = null) : SystemError("Unexpected error occurred", cause)
        class FeatureNotSupported(feature: String) : SystemError("Feature not supported: $feature")
    }
}

/**
 * Extension function to convert common exceptions to ChainError
 */
fun Throwable.toChainError(): ChainError {
    return when (this) {
        is ChainError -> this
        is IOException -> ChainError.NetworkError.NetworkUnavailable(this)
        is SecurityException -> ChainError.AuthError.PermissionDenied(message ?: "Unknown permission")
        is OutOfMemoryError -> ChainError.SystemError.OutOfMemory(this)
        else -> ChainError.SystemError.UnexpectedError(this)
    }
}