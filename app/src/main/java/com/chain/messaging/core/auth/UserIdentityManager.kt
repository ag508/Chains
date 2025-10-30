package com.chain.messaging.core.auth

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.InvalidKeyException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user identity storage and retrieval
 */
@Singleton
class UserIdentityManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "UserIdentityManager"
        private const val PREFS_NAME = "chain_user_identities"
        private const val KEYSTORE_ALIAS = "ChainUserIdentityKeys"
        private const val CURRENT_USER_KEY = "current_user"
        private const val USER_PREFIX = "user_"
        private const val BLOCKCHAIN_KEY_PREFIX = "blockchain_key_"
    }

    private val gson = Gson()

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context, KEYSTORE_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Store user identity securely
     */
    suspend fun storeUserIdentity(userIdentity: UserIdentity, blockchainPrivateKey: ByteArray): Result<Unit> {
        return try {
            // Convert UserIdentity to storable format
            val storableIdentity = StorableUserIdentity(
                userId = userIdentity.userId,
                username = userIdentity.username,
                email = userIdentity.email,
                displayName = userIdentity.displayName,
                profilePictureUrl = userIdentity.profilePictureUrl,
                blockchainPublicKey = userIdentity.blockchainPublicKey,
                signalIdentityKey = userIdentity.signalIdentityKey.serialize(),
                createdAt = userIdentity.createdAt,
                lastLoginAt = userIdentity.lastLoginAt,
                isVerified = userIdentity.isVerified
            )

            val userJson = gson.toJson(storableIdentity)
            val blockchainKeyString = String(blockchainPrivateKey)

            encryptedPrefs.edit()
                .putString("$USER_PREFIX${userIdentity.userId}", userJson)
                .putString("$BLOCKCHAIN_KEY_PREFIX${userIdentity.userId}", blockchainKeyString)
                .putString(CURRENT_USER_KEY, userIdentity.userId)
                .apply()

            Log.d(TAG, "User identity stored successfully: ${userIdentity.userId}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store user identity", e)
            Result.failure(AuthenticationException("Failed to store user identity", e))
        }
    }

    /**
     * Get user identity by user ID
     */
    suspend fun getUserIdentity(userId: String): UserIdentity? {
        return try {
            val userJson = encryptedPrefs.getString("$USER_PREFIX$userId", null) ?: return null
            val storableIdentity = gson.fromJson(userJson, StorableUserIdentity::class.java)
            
            storableIdentity.toUserIdentity()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user identity: $userId", e)
            null
        }
    }

    /**
     * Get user identity by email
     */
    suspend fun getUserByEmail(email: String): UserIdentity? {
        return try {
            val allUsers = getAllUsers()
            allUsers.find { it.email == email }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user by email: $email", e)
            null
        }
    }

    /**
     * Get user identity by username
     */
    suspend fun getUserByUsername(username: String): UserIdentity? {
        return try {
            val allUsers = getAllUsers()
            allUsers.find { it.username == username }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user by username: $username", e)
            null
        }
    }

    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUserIdentity(): UserIdentity? {
        return try {
            val currentUserId = encryptedPrefs.getString(CURRENT_USER_KEY, null) ?: return null
            getUserIdentity(currentUserId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current user identity", e)
            null
        }
    }

    /**
     * Update user identity
     */
    suspend fun updateUserIdentity(userIdentity: UserIdentity): Result<Unit> {
        return try {
            // Get existing blockchain private key
            val blockchainKeyString = encryptedPrefs.getString(
                "$BLOCKCHAIN_KEY_PREFIX${userIdentity.userId}", null)
            
            if (blockchainKeyString != null) {
                storeUserIdentity(userIdentity, blockchainKeyString.toByteArray())
            } else {
                Log.w(TAG, "Blockchain private key not found for user: ${userIdentity.userId}")
                Result.failure(AuthenticationException("Blockchain private key not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user identity", e)
            Result.failure(AuthenticationException("Failed to update user identity", e))
        }
    }

    /**
     * Get blockchain private key for a user
     */
    suspend fun getBlockchainPrivateKey(userId: String): ByteArray? {
        return try {
            val keyString = encryptedPrefs.getString("$BLOCKCHAIN_KEY_PREFIX$userId", null)
            keyString?.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get blockchain private key for user: $userId", e)
            null
        }
    }

    /**
     * Set current user
     */
    suspend fun setCurrentUser(userId: String): Result<Unit> {
        return try {
            encryptedPrefs.edit()
                .putString(CURRENT_USER_KEY, userId)
                .apply()
            
            Log.d(TAG, "Current user set to: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set current user", e)
            Result.failure(AuthenticationException("Failed to set current user", e))
        }
    }

    /**
     * Clear current user (logout)
     */
    suspend fun clearCurrentUser(): Result<Unit> {
        return try {
            encryptedPrefs.edit()
                .remove(CURRENT_USER_KEY)
                .apply()
            
            Log.d(TAG, "Current user cleared")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear current user", e)
            Result.failure(AuthenticationException("Failed to clear current user", e))
        }
    }

    /**
     * Delete user identity
     */
    suspend fun deleteUserIdentity(userId: String): Result<Unit> {
        return try {
            encryptedPrefs.edit()
                .remove("$USER_PREFIX$userId")
                .remove("$BLOCKCHAIN_KEY_PREFIX$userId")
                .apply()
            
            // Clear current user if it's the deleted user
            val currentUserId = encryptedPrefs.getString(CURRENT_USER_KEY, null)
            if (currentUserId == userId) {
                clearCurrentUser()
            }
            
            Log.d(TAG, "User identity deleted: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete user identity", e)
            Result.failure(AuthenticationException("Failed to delete user identity", e))
        }
    }

    /**
     * Get all stored users
     */
    suspend fun getAllUsers(): List<UserIdentity> {
        return try {
            val users = mutableListOf<UserIdentity>()
            val allPrefs = encryptedPrefs.all
            
            allPrefs.keys.filter { it.startsWith(USER_PREFIX) }.forEach { key ->
                try {
                    val userJson = allPrefs[key] as? String
                    if (userJson != null) {
                        val storableIdentity = gson.fromJson(userJson, StorableUserIdentity::class.java)
                        users.add(storableIdentity.toUserIdentity())
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse user from key: $key", e)
                }
            }
            
            users
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get all users", e)
            emptyList()
        }
    }

    /**
     * Clear all user data (for app reset)
     */
    suspend fun clearAllUsers(): Result<Unit> {
        return try {
            encryptedPrefs.edit().clear().apply()
            Log.d(TAG, "All user data cleared")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all users", e)
            Result.failure(AuthenticationException("Failed to clear all users", e))
        }
    }

    /**
     * Storable version of UserIdentity (for JSON serialization)
     */
    private data class StorableUserIdentity(
        val userId: String,
        val username: String,
        val email: String,
        val displayName: String,
        val profilePictureUrl: String?,
        val blockchainPublicKey: ByteArray,
        val signalIdentityKey: ByteArray,
        val createdAt: Long,
        val lastLoginAt: Long,
        val isVerified: Boolean
    ) {
        fun toUserIdentity(): UserIdentity {
            return UserIdentity(
                userId = userId,
                username = username,
                email = email,
                displayName = displayName,
                profilePictureUrl = profilePictureUrl,
                blockchainPublicKey = blockchainPublicKey,
                signalIdentityKey = IdentityKey(signalIdentityKey, 0),
                createdAt = createdAt,
                lastLoginAt = lastLoginAt,
                isVerified = isVerified
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StorableUserIdentity

            if (userId != other.userId) return false
            if (username != other.username) return false
            if (email != other.email) return false
            if (displayName != other.displayName) return false
            if (profilePictureUrl != other.profilePictureUrl) return false
            if (!blockchainPublicKey.contentEquals(other.blockchainPublicKey)) return false
            if (!signalIdentityKey.contentEquals(other.signalIdentityKey)) return false
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
            result = 31 * result + signalIdentityKey.contentHashCode()
            result = 31 * result + createdAt.hashCode()
            result = 31 * result + lastLoginAt.hashCode()
            result = 31 * result + isVerified.hashCode()
            return result
        }
    }
}