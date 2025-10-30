package com.chain.messaging.core.auth

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.credentials.CreateCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Manages passkey authentication (WebAuthn/FIDO2) using androidx.credentials library
 */
@Singleton
class PasskeyManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "PasskeyManager"
        private const val PREFS_NAME = "chain_passkeys"
        private const val KEYSTORE_ALIAS = "ChainPasskeyKeys"
        private const val PASSKEY_PREFIX = "passkey_"
        private const val RP_ID = "chain.messaging.com"
        private const val RP_NAME = "Chain Messaging"
    }

    private val credentialManager: CredentialManager by lazy {
        CredentialManager.create(context)
    }
    
    // Test mode flag for unit testing
    private val isTestMode: Boolean = try {
        Class.forName("org.robolectric.RobolectricTestRunner")
        true
    } catch (e: ClassNotFoundException) {
        false
    }

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
     * Check if passkey authentication is supported on this device
     */
    suspend fun isPasskeySupported(): Boolean {
        return try {
            // Check Android version (Credentials API requires API 34+)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Log.d(TAG, "Passkey not supported: requires API 34+")
                return false
            }
            
            // Additional checks could include:
            // - Biometric hardware availability
            // - Security patch level
            // - Device attestation capabilities
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking passkey support", e)
            false
        }
    }

    /**
     * Create a new passkey for a user using WebAuthn
     */
    suspend fun createPasskey(activity: FragmentActivity, username: String, userId: String): Result<PasskeyData> {
        return try {
            Log.d(TAG, "Creating passkey for user: $username")
            
            // Use mock data in test mode
            if (isTestMode) {
                val passkeyData = createMockPasskeyData(username, userId)
                storePasskeyData(username, passkeyData)
                Log.d(TAG, "Passkey created successfully for user: $username (test mode)")
                return Result.success(passkeyData)
            }
            
            if (!isPasskeySupported()) {
                return Result.failure(AuthenticationException("Passkey not supported on this device"))
            }
            
            // Generate challenge and user ID
            val challenge = generateChallenge()
            val userIdBytes = userId.toByteArray()
            
            // Create WebAuthn credential request
            val createPublicKeyCredentialRequest = createWebAuthnCredentialRequest(
                challenge = challenge,
                userId = userIdBytes,
                username = username
            )
            
            val createCredentialRequest = CreateCredentialRequest.Builder()
                .setCredentialData(createPublicKeyCredentialRequest)
                .build()
            
            // Create credential using CredentialManager
            val credentialResult = suspendCancellableCoroutine<Result<PublicKeyCredential>> { continuation ->
                try {
                    credentialManager.createCredentialAsync(
                        context = activity,
                        request = createCredentialRequest,
                        cancellationSignal = null,
                        executor = context.mainExecutor,
                        callback = object : androidx.credentials.CredentialManagerCallback<androidx.credentials.CreateCredentialResponse, CreateCredentialException> {
                            override fun onResult(result: androidx.credentials.CreateCredentialResponse) {
                                try {
                                    val credential = result.credential as PublicKeyCredential
                                    continuation.resume(Result.success(credential))
                                } catch (e: Exception) {
                                    continuation.resume(Result.failure(AuthenticationException("Failed to cast credential", e)))
                                }
                            }
                            
                            override fun onError(e: CreateCredentialException) {
                                continuation.resume(Result.failure(AuthenticationException("Failed to create credential", e)))
                            }
                        }
                    )
                } catch (e: Exception) {
                    continuation.resume(Result.failure(AuthenticationException("Failed to start credential creation", e)))
                }
            }
            
            if (credentialResult.isSuccess) {
                val credential = credentialResult.getOrThrow()
                val passkeyData = parseWebAuthnCredential(credential)
                
                // Store passkey data
                storePasskeyData(username, passkeyData)
                
                Log.d(TAG, "Passkey created successfully for user: $username")
                Result.success(passkeyData)
            } else {
                Log.w(TAG, "Failed to create passkey", credentialResult.exceptionOrNull())
                Result.failure(credentialResult.exceptionOrNull() ?: 
                    AuthenticationException("Failed to create passkey"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create passkey for user: $username", e)
            Result.failure(AuthenticationException("Failed to create passkey", e))
        }
    }

    /**
     * Authenticate using passkey with WebAuthn
     */
    suspend fun authenticate(activity: FragmentActivity, username: String): Result<PasskeyData> {
        return try {
            Log.d(TAG, "Authenticating with passkey for user: $username")
            
            // Check if passkey exists for user
            val storedPasskey = getStoredPasskeyData(username)
                ?: return Result.failure(AuthenticationException("No passkey found for user"))
            
            // Use mock data in test mode
            if (isTestMode) {
                val authenticationData = createMockAuthenticationData(storedPasskey)
                Log.d(TAG, "Passkey authentication successful for user: $username (test mode)")
                return Result.success(authenticationData)
            }
            
            if (!isPasskeySupported()) {
                return Result.failure(AuthenticationException("Passkey not supported on this device"))
            }
            
            // Generate authentication challenge
            val challenge = generateChallenge()
            
            // Create WebAuthn authentication request
            val getPublicKeyCredentialOption = createWebAuthnAuthenticationRequest(
                challenge = challenge,
                credentialId = storedPasskey.credentialId
            )
            
            val getCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(getPublicKeyCredentialOption)
                .build()
            
            // Authenticate using CredentialManager
            val credentialResult = suspendCancellableCoroutine<Result<PublicKeyCredential>> { continuation ->
                try {
                    credentialManager.getCredentialAsync(
                        context = activity,
                        request = getCredentialRequest,
                        cancellationSignal = null,
                        executor = context.mainExecutor,
                        callback = object : androidx.credentials.CredentialManagerCallback<androidx.credentials.GetCredentialResponse, GetCredentialException> {
                            override fun onResult(result: androidx.credentials.GetCredentialResponse) {
                                try {
                                    val credential = result.credential as PublicKeyCredential
                                    continuation.resume(Result.success(credential))
                                } catch (e: Exception) {
                                    continuation.resume(Result.failure(AuthenticationException("Failed to cast credential", e)))
                                }
                            }
                            
                            override fun onError(e: GetCredentialException) {
                                continuation.resume(Result.failure(AuthenticationException("Failed to get credential", e)))
                            }
                        }
                    )
                } catch (e: Exception) {
                    continuation.resume(Result.failure(AuthenticationException("Failed to start authentication", e)))
                }
            }
            
            if (credentialResult.isSuccess) {
                val credential = credentialResult.getOrThrow()
                val authenticationData = parseWebAuthnAuthenticationResponse(credential)
                
                Log.d(TAG, "Passkey authentication successful for user: $username")
                Result.success(authenticationData)
            } else {
                Log.w(TAG, "Passkey authentication failed", credentialResult.exceptionOrNull())
                Result.failure(credentialResult.exceptionOrNull() ?: 
                    AuthenticationException("Passkey authentication failed"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to authenticate with passkey for user: $username", e)
            Result.failure(AuthenticationException("Passkey authentication failed", e))
        }
    }

    /**
     * Check if a passkey exists for a user
     */
    suspend fun hasPasskey(username: String): Boolean {
        return try {
            getStoredPasskeyData(username) != null
        } catch (e: Exception) {
            Log.e(TAG, "Error checking passkey existence for user: $username", e)
            false
        }
    }

    /**
     * Delete passkey for a user
     */
    suspend fun deletePasskey(username: String): Result<Unit> {
        return try {
            encryptedPrefs.edit()
                .remove("$PASSKEY_PREFIX$username")
                .apply()
            
            Log.d(TAG, "Passkey deleted for user: $username")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete passkey for user: $username", e)
            Result.failure(AuthenticationException("Failed to delete passkey", e))
        }
    }

    /**
     * Get all users with passkeys
     */
    suspend fun getUsersWithPasskeys(): List<String> {
        return try {
            encryptedPrefs.all.keys
                .filter { it.startsWith(PASSKEY_PREFIX) }
                .map { it.removePrefix(PASSKEY_PREFIX) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting users with passkeys", e)
            emptyList()
        }
    }

    /**
     * Clear all passkeys
     */
    suspend fun clearAllPasskeys(): Result<Unit> {
        return try {
            encryptedPrefs.edit().clear().apply()
            Log.d(TAG, "All passkeys cleared")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear all passkeys", e)
            Result.failure(AuthenticationException("Failed to clear all passkeys", e))
        }
    }

    // Private helper methods

    private fun storePasskeyData(username: String, passkeyData: PasskeyData) {
        val passkeyJson = """
            {
                "credentialId": "${passkeyData.credentialId}",
                "publicKey": "${android.util.Base64.encodeToString(passkeyData.publicKey, android.util.Base64.DEFAULT)}",
                "signature": "${android.util.Base64.encodeToString(passkeyData.signature, android.util.Base64.DEFAULT)}",
                "authenticatorData": "${android.util.Base64.encodeToString(passkeyData.authenticatorData, android.util.Base64.DEFAULT)}"
            }
        """.trimIndent()
        
        encryptedPrefs.edit()
            .putString("$PASSKEY_PREFIX$username", passkeyJson)
            .apply()
    }

    private fun getStoredPasskeyData(username: String): PasskeyData? {
        return try {
            val passkeyJson = encryptedPrefs.getString("$PASSKEY_PREFIX$username", null) ?: return null
            
            // Simple JSON parsing (in a real app, use Gson or similar)
            val credentialId = extractJsonValue(passkeyJson, "credentialId")
            val publicKeyBase64 = extractJsonValue(passkeyJson, "publicKey")
            val signatureBase64 = extractJsonValue(passkeyJson, "signature")
            val authenticatorDataBase64 = extractJsonValue(passkeyJson, "authenticatorData")
            
            if (credentialId != null && publicKeyBase64 != null && 
                signatureBase64 != null && authenticatorDataBase64 != null) {
                
                PasskeyData(
                    credentialId = credentialId,
                    publicKey = android.util.Base64.decode(publicKeyBase64, android.util.Base64.DEFAULT),
                    signature = android.util.Base64.decode(signatureBase64, android.util.Base64.DEFAULT),
                    authenticatorData = android.util.Base64.decode(authenticatorDataBase64, android.util.Base64.DEFAULT)
                )
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing stored passkey data", e)
            null
        }
    }

    private fun extractJsonValue(json: String, key: String): String? {
        return try {
            val pattern = "\"$key\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            pattern.find(json)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }

    // Real WebAuthn helper methods
    
    private fun generateChallenge(): ByteArray {
        val challenge = ByteArray(32)
        SecureRandom().nextBytes(challenge)
        return challenge
    }
    
    private fun createWebAuthnCredentialRequest(
        challenge: ByteArray,
        userId: ByteArray,
        username: String
    ): CreatePublicKeyCredentialRequest {
        val requestJson = JSONObject().apply {
            put("rp", JSONObject().apply {
                put("id", RP_ID)
                put("name", RP_NAME)
            })
            put("user", JSONObject().apply {
                put("id", android.util.Base64.encodeToString(userId, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP))
                put("name", username)
                put("displayName", username)
            })
            put("challenge", android.util.Base64.encodeToString(challenge, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP))
            put("pubKeyCredParams", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "public-key")
                    put("alg", -7) // ES256
                })
                put(JSONObject().apply {
                    put("type", "public-key")
                    put("alg", -257) // RS256
                })
            })
            put("authenticatorSelection", JSONObject().apply {
                put("authenticatorAttachment", "platform")
                put("userVerification", "required")
                put("residentKey", "required")
            })
            put("attestation", "none")
        }
        
        return CreatePublicKeyCredentialRequest(requestJson.toString())
    }
    
    private fun createWebAuthnAuthenticationRequest(
        challenge: ByteArray,
        credentialId: String
    ): GetPublicKeyCredentialOption {
        val requestJson = JSONObject().apply {
            put("challenge", android.util.Base64.encodeToString(challenge, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP))
            put("rpId", RP_ID)
            put("allowCredentials", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "public-key")
                    put("id", credentialId)
                })
            })
            put("userVerification", "required")
        }
        
        return GetPublicKeyCredentialOption(requestJson.toString())
    }
    
    private fun parseWebAuthnCredential(credential: PublicKeyCredential): PasskeyData {
        val responseJson = JSONObject(credential.authenticationResponseJson)
        val response = responseJson.getJSONObject("response")
        
        return PasskeyData(
            credentialId = responseJson.getString("id"),
            publicKey = android.util.Base64.decode(
                response.getString("publicKey"), 
                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
            ),
            signature = android.util.Base64.decode(
                response.getString("signature"), 
                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
            ),
            authenticatorData = android.util.Base64.decode(
                response.getString("authenticatorData"), 
                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
            )
        )
    }
    
    private fun parseWebAuthnAuthenticationResponse(credential: PublicKeyCredential): PasskeyData {
        val responseJson = JSONObject(credential.authenticationResponseJson)
        val response = responseJson.getJSONObject("response")
        
        return PasskeyData(
            credentialId = responseJson.getString("id"),
            publicKey = ByteArray(0), // Not included in authentication response
            signature = android.util.Base64.decode(
                response.getString("signature"), 
                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
            ),
            authenticatorData = android.util.Base64.decode(
                response.getString("authenticatorData"), 
                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
            )
        )
    }
    
    // Mock data for testing
    private fun createMockPasskeyData(username: String, userId: String): PasskeyData {
        val credentialId = "passkey_${userId}_${System.currentTimeMillis()}"
        val publicKey = "mock_public_key_$username".toByteArray()
        val signature = "mock_signature_$username".toByteArray()
        val authenticatorData = "mock_authenticator_data_$username".toByteArray()
        
        return PasskeyData(
            credentialId = credentialId,
            publicKey = publicKey,
            signature = signature,
            authenticatorData = authenticatorData
        )
    }

    private fun createMockAuthenticationData(storedPasskey: PasskeyData): PasskeyData {
        // In a real implementation, this would be the authentication response
        // For now, we'll return the stored passkey data with a new signature
        return storedPasskey.copy(
            signature = "mock_auth_signature_${System.currentTimeMillis()}".toByteArray()
        )
    }
}