package com.chain.messaging.core.crypto

import android.content.Context
import com.chain.messaging.domain.model.CryptoException
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.InvalidKeyException
import org.signal.libsignal.protocol.ecc.Curve
import org.signal.libsignal.protocol.ecc.ECKeyPair
import org.signal.libsignal.protocol.state.PreKeyBundle
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.state.SignedPreKeyRecord
import org.signal.libsignal.protocol.util.KeyHelper
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Signal Protocol keys including identity keys, signed pre-keys, and one-time pre-keys.
 * Uses Android Keystore for secure key storage.
 */
@Singleton
class KeyManager @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val KEYSTORE_ALIAS = "ChainMessagingKeys"
        private const val IDENTITY_KEY_ALIAS = "identity_key"
        private const val SIGNED_PREKEY_ALIAS = "signed_prekey"
        private const val PREKEY_ALIAS_PREFIX = "prekey_"
        private const val REGISTRATION_ID_KEY = "registration_id"
        private const val DEVICE_ID_KEY = "device_id"
        private const val PREFS_NAME = "chain_crypto_prefs"
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
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

    private var _identityKeyPair: IdentityKeyPair? = null
    private var _registrationId: Int? = null
    private var _deviceId: Int? = null

    /**
     * Initialize the key manager and generate initial keys if they don't exist
     */
    suspend fun initialize(): Result<Unit> {
        return try {
            // Generate or load registration ID
            _registrationId = getOrGenerateRegistrationId()
            
            // Generate or load device ID
            _deviceId = getOrGenerateDeviceId()
            
            // Generate or load identity key pair
            _identityKeyPair = getOrGenerateIdentityKeyPair()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(CryptoException("Failed to initialize KeyManager", e))
        }
    }

    /**
     * Get the identity key pair for this device
     */
    fun getIdentityKeyPair(): IdentityKeyPair {
        return _identityKeyPair ?: throw CryptoException("KeyManager not initialized")
    }

    /**
     * Get the public identity key
     */
    fun getIdentityKey(): IdentityKey {
        return getIdentityKeyPair().publicKey
    }

    /**
     * Get the registration ID for this device
     */
    fun getRegistrationId(): Int {
        return _registrationId ?: throw CryptoException("KeyManager not initialized")
    }

    /**
     * Get the device ID
     */
    fun getDeviceId(): Int {
        return _deviceId ?: throw CryptoException("KeyManager not initialized")
    }

    /**
     * Generate a batch of one-time pre-keys
     */
    fun generatePreKeys(startId: Int, count: Int): List<PreKeyRecord> {
        val preKeys = mutableListOf<PreKeyRecord>()
        
        for (i in 0 until count) {
            val keyId = startId + i
            val keyPair = Curve.generateKeyPair()
            val preKey = PreKeyRecord(keyId, keyPair)
            
            // Store the pre-key securely
            storePreKey(preKey)
            preKeys.add(preKey)
        }
        
        return preKeys
    }

    /**
     * Generate a signed pre-key
     */
    fun generateSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord {
        val keyPair = Curve.generateKeyPair()
        val signature = Curve.calculateSignature(
            getIdentityKeyPair().privateKey,
            keyPair.publicKey.serialize()
        )
        val timestamp = System.currentTimeMillis()
        
        val signedPreKey = SignedPreKeyRecord(
            signedPreKeyId,
            timestamp,
            keyPair,
            signature
        )
        
        // Store the signed pre-key securely
        storeSignedPreKey(signedPreKey)
        
        return signedPreKey
    }

    /**
     * Create a pre-key bundle for key exchange
     */
    fun createPreKeyBundle(
        preKeyId: Int,
        signedPreKeyId: Int
    ): PreKeyBundle? {
        val preKey = loadPreKey(preKeyId) ?: return null
        val signedPreKey = loadSignedPreKey(signedPreKeyId) ?: return null
        
        return PreKeyBundle(
            getRegistrationId(),
            getDeviceId(),
            preKeyId,
            preKey.keyPair.publicKey,
            signedPreKeyId,
            signedPreKey.keyPair.publicKey,
            signedPreKey.signature,
            getIdentityKey()
        )
    }

    /**
     * Load a pre-key by ID
     */
    fun loadPreKey(preKeyId: Int): PreKeyRecord? {
        return try {
            val keyData = encryptedPrefs.getString("$PREKEY_ALIAS_PREFIX$preKeyId", null)
            keyData?.let { PreKeyRecord(it.toByteArray()) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Load a signed pre-key by ID
     */
    fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord? {
        return try {
            val keyData = encryptedPrefs.getString("$SIGNED_PREKEY_ALIAS$signedPreKeyId", null)
            keyData?.let { SignedPreKeyRecord(it.toByteArray()) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Remove a pre-key
     */
    fun removePreKey(preKeyId: Int) {
        encryptedPrefs.edit()
            .remove("$PREKEY_ALIAS_PREFIX$preKeyId")
            .apply()
    }

    /**
     * Get all pre-key IDs
     */
    fun getPreKeyIds(): List<Int> {
        return encryptedPrefs.all.keys
            .filter { it.startsWith(PREKEY_ALIAS_PREFIX) }
            .mapNotNull { 
                it.removePrefix(PREKEY_ALIAS_PREFIX).toIntOrNull() 
            }
    }

    /**
     * Get all signed pre-key IDs
     */
    fun getSignedPreKeyIds(): List<Int> {
        return encryptedPrefs.all.keys
            .filter { it.startsWith(SIGNED_PREKEY_ALIAS) && it != SIGNED_PREKEY_ALIAS }
            .mapNotNull { 
                it.removePrefix(SIGNED_PREKEY_ALIAS).toIntOrNull() 
            }
    }

    /**
     * Load all signed pre-keys (used for key rotation)
     */
    fun loadAllSignedPreKeys(): List<SignedPreKeyRecord> {
        return getSignedPreKeyIds().mapNotNull { signedPreKeyId ->
            loadSignedPreKey(signedPreKeyId)
        }
    }

    /**
     * Store a pre-key (required by SignalProtocolStore interface)
     */
    fun storePreKey(preKeyId: Int, record: PreKeyRecord) {
        val serialized = String(record.serialize())
        encryptedPrefs.edit()
            .putString("$PREKEY_ALIAS_PREFIX$preKeyId", serialized)
            .apply()
    }

    /**
     * Check if a pre-key exists (required by SignalProtocolStore interface)
     */
    fun containsPreKey(preKeyId: Int): Boolean {
        return encryptedPrefs.contains("$PREKEY_ALIAS_PREFIX$preKeyId")
    }

    /**
     * Store a signed pre-key (required by SignalProtocolStore interface)
     */
    fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) {
        val serialized = String(record.serialize())
        encryptedPrefs.edit()
            .putString("$SIGNED_PREKEY_ALIAS$signedPreKeyId", serialized)
            .apply()
    }

    /**
     * Load all signed pre-keys (required by SignalProtocolStore interface)
     */
    fun loadSignedPreKeys(): MutableList<SignedPreKeyRecord> {
        return getSignedPreKeyIds().mapNotNull { signedPreKeyId ->
            loadSignedPreKey(signedPreKeyId)
        }.toMutableList()
    }

    /**
     * Check if a signed pre-key exists (required by SignalProtocolStore interface)
     */
    fun containsSignedPreKey(signedPreKeyId: Int): Boolean {
        return encryptedPrefs.contains("$SIGNED_PREKEY_ALIAS$signedPreKeyId")
    }

    /**
     * Remove a signed pre-key
     */
    fun removeSignedPreKey(signedPreKeyId: Int) {
        encryptedPrefs.edit()
            .remove("$SIGNED_PREKEY_ALIAS$signedPreKeyId")
            .apply()
    }

    /**
     * Rotate identity keys (should be done carefully)
     */
    suspend fun rotateIdentityKeys(): Result<IdentityKeyPair> {
        return try {
            val newKeyPair = generateIdentityKeyPair()
            storeIdentityKeyPair(newKeyPair)
            _identityKeyPair = newKeyPair
            Result.success(newKeyPair)
        } catch (e: Exception) {
            Result.failure(CryptoException("Failed to rotate identity keys", e))
        }
    }

    /**
     * Clear all stored keys (for logout/reset)
     */
    fun clearAllKeys() {
        encryptedPrefs.edit().clear().apply()
        _identityKeyPair = null
        _registrationId = null
        _deviceId = null
    }

    // Private helper methods

    private fun getOrGenerateRegistrationId(): Int {
        return encryptedPrefs.getInt(REGISTRATION_ID_KEY, -1).takeIf { it != -1 }
            ?: KeyHelper.generateRegistrationId(false).also { regId ->
                encryptedPrefs.edit().putInt(REGISTRATION_ID_KEY, regId).apply()
            }
    }

    private fun getOrGenerateDeviceId(): Int {
        return encryptedPrefs.getInt(DEVICE_ID_KEY, -1).takeIf { it != -1 }
            ?: SecureRandom().nextInt(Int.MAX_VALUE).also { deviceId ->
                encryptedPrefs.edit().putInt(DEVICE_ID_KEY, deviceId).apply()
            }
    }

    private fun getOrGenerateIdentityKeyPair(): IdentityKeyPair {
        val storedKeyData = encryptedPrefs.getString(IDENTITY_KEY_ALIAS, null)
        
        return if (storedKeyData != null) {
            try {
                IdentityKeyPair(storedKeyData.toByteArray())
            } catch (e: InvalidKeyException) {
                // If stored key is corrupted, generate new one
                generateAndStoreIdentityKeyPair()
            }
        } else {
            generateAndStoreIdentityKeyPair()
        }
    }

    private fun generateAndStoreIdentityKeyPair(): IdentityKeyPair {
        val keyPair = generateIdentityKeyPair()
        storeIdentityKeyPair(keyPair)
        return keyPair
    }

    private fun generateIdentityKeyPair(): IdentityKeyPair {
        val keyPair = Curve.generateKeyPair()
        return IdentityKeyPair(IdentityKey(keyPair.publicKey), keyPair.privateKey)
    }

    private fun storeIdentityKeyPair(keyPair: IdentityKeyPair) {
        val serialized = String(keyPair.serialize())
        encryptedPrefs.edit()
            .putString(IDENTITY_KEY_ALIAS, serialized)
            .apply()
    }

    private fun storePreKey(preKey: PreKeyRecord) {
        val serialized = String(preKey.serialize())
        encryptedPrefs.edit()
            .putString("$PREKEY_ALIAS_PREFIX${preKey.id}", serialized)
            .apply()
    }

    private fun storeSignedPreKey(signedPreKey: SignedPreKeyRecord) {
        val serialized = String(signedPreKey.serialize())
        encryptedPrefs.edit()
            .putString("$SIGNED_PREKEY_ALIAS${signedPreKey.id}", serialized)
            .apply()
    }

    /**
     * Generate a symmetric key for additional encryption needs
     */
    fun generateSymmetricKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "symmetric_key_${System.currentTimeMillis()}",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Export keys for synchronization (simplified implementation)
     */
    fun exportKeys(): Map<String, String> {
        val keys = mutableMapOf<String, String>()
        
        // Export identity key
        _identityKeyPair?.let { keyPair ->
            keys["identity_public"] = String(keyPair.publicKey.serialize())
        }
        
        // Export pre-keys
        getPreKeyIds().forEach { preKeyId ->
            loadPreKey(preKeyId)?.let { preKey ->
                keys["prekey_$preKeyId"] = String(preKey.serialize())
            }
        }
        
        return keys
    }

    /**
     * Export recent keys for incremental sync
     */
    fun exportRecentKeys(since: java.time.LocalDateTime): Map<String, String> {
        // For simplicity, return all keys
        // In a real implementation, you'd track key creation timestamps
        return exportKeys()
    }

    /**
     * Import a key from synchronization
     */
    fun importKey(keyId: String, keyData: String) {
        when {
            keyId.startsWith("prekey_") -> {
                val preKeyId = keyId.removePrefix("prekey_").toIntOrNull()
                if (preKeyId != null) {
                    try {
                        val preKey = PreKeyRecord(keyData.toByteArray())
                        storePreKey(preKey)
                    } catch (e: Exception) {
                        // Log error but continue
                    }
                }
            }
            keyId == "identity_public" -> {
                // Handle identity key import if needed
                // This is more complex and would require careful consideration
            }
        }
    }
}

