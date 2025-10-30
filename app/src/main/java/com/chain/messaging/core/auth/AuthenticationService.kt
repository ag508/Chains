package com.chain.messaging.core.auth

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.chain.messaging.core.crypto.KeyManager
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Service for handling user authentication including OAuth, passkeys, and biometrics
 */
@Singleton
class AuthenticationService @Inject constructor(
    private val context: Context,
    private val keyManager: KeyManager,
    private val userIdentityManager: UserIdentityManager,
    private val oAuthManager: OAuthManager,
    private val passkeyManager: PasskeyManager
) {
    companion object {
        private const val TAG = "AuthenticationService"
    }

    /**
     * Authenticate user with Google OAuth
     */
    suspend fun authenticateWithGoogle(activity: FragmentActivity): Result<AuthenticationResult> {
        return try {
            Log.d(TAG, "Starting Google OAuth authentication")
            val oAuthResult = oAuthManager.authenticateWithGoogle(activity)
            
            if (oAuthResult.isSuccess) {
                val oAuthData = oAuthResult.getOrThrow()
                val userIdentity = createUserIdentity(oAuthData)
                
                Log.d(TAG, "Google OAuth authentication successful")
                Result.success(AuthenticationResult(
                    isSuccess = true,
                    userIdentity = userIdentity,
                    authMethod = AuthMethod.GOOGLE_OAUTH
                ))
            } else {
                Log.w(TAG, "Google OAuth authentication failed", oAuthResult.exceptionOrNull())
                Result.failure(AuthenticationException("Google OAuth authentication failed", 
                    oAuthResult.exceptionOrNull()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during Google OAuth authentication", e)
            Result.failure(AuthenticationException("Google OAuth authentication error", e))
        }
    }

    /**
     * Authenticate user with Microsoft OAuth
     */
    suspend fun authenticateWithMicrosoft(activity: FragmentActivity): Result<AuthenticationResult> {
        return try {
            Log.d(TAG, "Starting Microsoft OAuth authentication")
            val oAuthResult = oAuthManager.authenticateWithMicrosoft(activity)
            
            if (oAuthResult.isSuccess) {
                val oAuthData = oAuthResult.getOrThrow()
                val userIdentity = createUserIdentity(oAuthData)
                
                Log.d(TAG, "Microsoft OAuth authentication successful")
                Result.success(AuthenticationResult(
                    isSuccess = true,
                    userIdentity = userIdentity,
                    authMethod = AuthMethod.MICROSOFT_OAUTH
                ))
            } else {
                Log.w(TAG, "Microsoft OAuth authentication failed", oAuthResult.exceptionOrNull())
                Result.failure(AuthenticationException("Microsoft OAuth authentication failed", 
                    oAuthResult.exceptionOrNull()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during Microsoft OAuth authentication", e)
            Result.failure(AuthenticationException("Microsoft OAuth authentication error", e))
        }
    }

    /**
     * Authenticate user with passkey
     */
    suspend fun authenticateWithPasskey(activity: FragmentActivity, username: String): Result<AuthenticationResult> {
        return try {
            Log.d(TAG, "Starting passkey authentication for user: $username")
            val passkeyResult = passkeyManager.authenticate(activity, username)
            
            if (passkeyResult.isSuccess) {
                val passkeyData = passkeyResult.getOrThrow()
                val userIdentity = getUserIdentityByUsername(username)
                
                if (userIdentity != null) {
                    Log.d(TAG, "Passkey authentication successful")
                    Result.success(AuthenticationResult(
                        isSuccess = true,
                        userIdentity = userIdentity,
                        authMethod = AuthMethod.PASSKEY
                    ))
                } else {
                    Log.w(TAG, "User identity not found for username: $username")
                    Result.failure(AuthenticationException("User identity not found"))
                }
            } else {
                Log.w(TAG, "Passkey authentication failed", passkeyResult.exceptionOrNull())
                Result.failure(AuthenticationException("Passkey authentication failed", 
                    passkeyResult.exceptionOrNull()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during passkey authentication", e)
            Result.failure(AuthenticationException("Passkey authentication error", e))
        }
    }

    /**
     * Authenticate user with biometrics
     */
    suspend fun authenticateWithBiometrics(activity: FragmentActivity): Result<AuthenticationResult> {
        return try {
            Log.d(TAG, "Starting biometric authentication")
            
            // Check if biometrics are available
            val biometricManager = BiometricManager.from(context)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    // Biometrics are available
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    return Result.failure(AuthenticationException("No biometric hardware available"))
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    return Result.failure(AuthenticationException("Biometric hardware unavailable"))
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    return Result.failure(AuthenticationException("No biometrics enrolled"))
                }
                else -> {
                    return Result.failure(AuthenticationException("Biometric authentication not available"))
                }
            }

            val biometricResult = performBiometricAuthentication(activity)
            
            if (biometricResult.isSuccess) {
                // Get the current user identity (assuming user is already registered)
                val userIdentity = getCurrentUserIdentity()
                
                if (userIdentity != null) {
                    Log.d(TAG, "Biometric authentication successful")
                    Result.success(AuthenticationResult(
                        isSuccess = true,
                        userIdentity = userIdentity,
                        authMethod = AuthMethod.BIOMETRIC
                    ))
                } else {
                    Log.w(TAG, "No current user identity found for biometric authentication")
                    Result.failure(AuthenticationException("No user identity found"))
                }
            } else {
                Log.w(TAG, "Biometric authentication failed", biometricResult.exceptionOrNull())
                Result.failure(AuthenticationException("Biometric authentication failed", 
                    biometricResult.exceptionOrNull()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during biometric authentication", e)
            Result.failure(AuthenticationException("Biometric authentication error", e))
        }
    }

    /**
     * Register a new user with OAuth data
     */
    suspend fun registerUser(oAuthData: OAuthData): Result<UserIdentity> {
        return try {
            Log.d(TAG, "Registering new user: ${oAuthData.email}")
            
            // Generate blockchain identity keys
            val blockchainKeys = generateBlockchainIdentityKeys()
            
            // Create user identity
            val userIdentity = UserIdentity(
                userId = generateUserId(),
                username = oAuthData.email,
                email = oAuthData.email,
                displayName = oAuthData.name,
                profilePictureUrl = oAuthData.profilePictureUrl,
                blockchainPublicKey = blockchainKeys.publicKey,
                signalIdentityKey = keyManager.getIdentityKey(),
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )
            
            // Store user identity
            val storeResult = userIdentityManager.storeUserIdentity(userIdentity, blockchainKeys.privateKey)
            
            if (storeResult.isSuccess) {
                Log.d(TAG, "User registration successful: ${userIdentity.userId}")
                Result.success(userIdentity)
            } else {
                Log.e(TAG, "Failed to store user identity", storeResult.exceptionOrNull())
                Result.failure(AuthenticationException("Failed to store user identity", 
                    storeResult.exceptionOrNull()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during user registration", e)
            Result.failure(AuthenticationException("User registration error", e))
        }
    }

    /**
     * Setup passkey for a user
     */
    suspend fun setupPasskey(activity: FragmentActivity, userIdentity: UserIdentity): Result<Unit> {
        return try {
            Log.d(TAG, "Setting up passkey for user: ${userIdentity.username}")
            val result = passkeyManager.createPasskey(activity, userIdentity.username, userIdentity.userId)
            
            if (result.isSuccess) {
                Log.d(TAG, "Passkey setup successful")
                Result.success(Unit)
            } else {
                Log.w(TAG, "Passkey setup failed", result.exceptionOrNull())
                Result.failure(AuthenticationException("Passkey setup failed", result.exceptionOrNull()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during passkey setup", e)
            Result.failure(AuthenticationException("Passkey setup error", e))
        }
    }

    /**
     * Check if biometric authentication is available
     */
    fun isBiometricAuthenticationAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == 
                BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Check if passkey authentication is available
     */
    suspend fun isPasskeyAuthenticationAvailable(): Boolean {
        return passkeyManager.isPasskeySupported()
    }

    /**
     * Logout current user
     */
    suspend fun logout(): Result<Unit> {
        return try {
            Log.d(TAG, "Logging out current user")
            
            // Clear user session
            userIdentityManager.clearCurrentUser()
            
            // Clear OAuth tokens
            oAuthManager.clearTokens()
            
            Log.d(TAG, "Logout successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            Result.failure(AuthenticationException("Logout error", e))
        }
    }

    /**
     * Get current authenticated user
     */
    suspend fun getCurrentUser(): UserIdentity? {
        return userIdentityManager.getCurrentUserIdentity()
    }

    /**
     * Check if user is currently authenticated
     */
    suspend fun isAuthenticated(): Boolean {
        return getCurrentUser() != null
    }
    
    /**
     * Authenticate user with specified method
     */
    suspend fun authenticate(authMethod: com.chain.messaging.core.integration.AuthMethod): Result<com.chain.messaging.domain.model.User> {
        return try {
            when (authMethod) {
                com.chain.messaging.core.integration.AuthMethod.GOOGLE_OAUTH -> {
                    // For now, return a mock user since we need FragmentActivity
                    val mockUser = com.chain.messaging.domain.model.User(
                        id = "mock_user_id",
                        publicKey = "mock_public_key_google",
                        displayName = "Mock User"
                    )
                    Result.success(mockUser)
                }
                com.chain.messaging.core.integration.AuthMethod.MICROSOFT_OAUTH -> {
                    val mockUser = com.chain.messaging.domain.model.User(
                        id = "mock_user_id",
                        publicKey = "mock_public_key_microsoft",
                        displayName = "Mock User"
                    )
                    Result.success(mockUser)
                }
                com.chain.messaging.core.integration.AuthMethod.PASSKEY -> {
                    val mockUser = com.chain.messaging.domain.model.User(
                        id = "mock_user_id",
                        publicKey = "mock_public_key_passkey",
                        displayName = "Mock User"
                    )
                    Result.success(mockUser)
                }
                com.chain.messaging.core.integration.AuthMethod.BIOMETRIC -> {
                    val mockUser = com.chain.messaging.domain.model.User(
                        id = "mock_user_id",
                        publicKey = "mock_public_key_biometric",
                        displayName = "Mock User"
                    )
                    Result.success(mockUser)
                }
            }
        } catch (e: Exception) {
            Result.failure(AuthenticationException("Authentication failed", e))
        }
    }

    // Private helper methods

    private suspend fun createUserIdentity(oAuthData: OAuthData): UserIdentity {
        // Check if user already exists
        val existingUser = userIdentityManager.getUserByEmail(oAuthData.email)
        
        return if (existingUser != null) {
            // Update last login time
            val updatedUser = existingUser.copy(lastLoginAt = System.currentTimeMillis())
            userIdentityManager.updateUserIdentity(updatedUser)
            updatedUser
        } else {
            // Register new user
            registerUser(oAuthData).getOrThrow()
        }
    }

    private suspend fun getUserIdentityByUsername(username: String): UserIdentity? {
        return userIdentityManager.getUserByUsername(username)
    }

    private suspend fun getCurrentUserIdentity(): UserIdentity? {
        return userIdentityManager.getCurrentUserIdentity()
    }

    private suspend fun performBiometricAuthentication(activity: FragmentActivity): Result<Unit> {
        return suspendCancellableCoroutine { continuation ->
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(activity, executor, 
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        continuation.resume(Result.failure(
                            AuthenticationException("Biometric authentication error: $errString")))
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        continuation.resume(Result.success(Unit))
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        continuation.resume(Result.failure(
                            AuthenticationException("Biometric authentication failed")))
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Use your fingerprint or face to authenticate")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun generateBlockchainIdentityKeys(): BlockchainKeyPair {
        // Generate blockchain identity keys (simplified implementation)
        // In a real implementation, this would use proper blockchain key generation
        val keyPair = org.signal.libsignal.protocol.ecc.Curve.generateKeyPair()
        return BlockchainKeyPair(
            publicKey = keyPair.publicKey.serialize(),
            privateKey = keyPair.privateKey.serialize()
        )
    }

    private fun generateUserId(): String {
        return "user_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * Authentication result data class
 */
data class AuthenticationResult(
    val isSuccess: Boolean,
    val userIdentity: UserIdentity?,
    val authMethod: AuthMethod,
    val error: String? = null
)

/**
 * Authentication methods enum
 */
enum class AuthMethod {
    GOOGLE_OAUTH,
    MICROSOFT_OAUTH,
    PASSKEY,
    BIOMETRIC
}

/**
 * Blockchain key pair data class
 */
data class BlockchainKeyPair(
    val publicKey: ByteArray,
    val privateKey: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BlockchainKeyPair

        if (!publicKey.contentEquals(other.publicKey)) return false
        if (!privateKey.contentEquals(other.privateKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.contentHashCode()
        result = 31 * result + privateKey.contentHashCode()
        return result
    }
}

/**
 * Custom exception for authentication errors
 */
class AuthenticationException(message: String, cause: Throwable? = null) : Exception(message, cause)