package com.chain.messaging.core.crypto

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.InvalidKeyException
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.ecc.Curve
import org.signal.libsignal.protocol.util.KeyHelper
import org.signal.libsignal.protocol.state.IdentityKeyStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of IdentityStorage using encrypted shared preferences
 */
@Singleton
class IdentityStorageImpl @Inject constructor(
    private val context: Context
) : IdentityStorage {

    companion object {
        private const val PREFS_NAME = "chain_identities"
        private const val KEYSTORE_ALIAS = "ChainIdentityKeys"
        private const val TRUSTED_PREFIX = "trusted_"
        private const val IDENTITY_PREFIX = "identity_"
        private const val LOCAL_IDENTITY_KEY = "local_identity_key"
        private const val LOCAL_REGISTRATION_ID = "local_registration_id"
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

    override fun getIdentityKeyPair(): IdentityKeyPair {
        val storedKey = encryptedPrefs.getString(LOCAL_IDENTITY_KEY, null)
        
        return if (storedKey != null) {
            try {
                IdentityKeyPair(storedKey.toByteArray())
            } catch (e: InvalidKeyException) {
                // If stored key is corrupted, generate new one
                generateAndStoreIdentityKeyPair()
            }
        } else {
            // Generate new identity key pair if none exists
            generateAndStoreIdentityKeyPair()
        }
    }

    override fun getLocalRegistrationId(): Int {
        val storedId = encryptedPrefs.getInt(LOCAL_REGISTRATION_ID, -1)
        
        return if (storedId != -1) {
            storedId
        } else {
            // Generate new registration ID if none exists
            val registrationId = KeyHelper.generateRegistrationId(false)
            encryptedPrefs.edit()
                .putInt(LOCAL_REGISTRATION_ID, registrationId)
                .apply()
            registrationId
        }
    }

    override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean {
        val identityKeyString = getIdentityKey(address)
        val existingIdentity = identityKeyString?.let { 
            try {
                IdentityKey(it.toByteArray(), 0)
            } catch (e: InvalidKeyException) {
                null
            }
        }

        val hasChanged = existingIdentity == null || !existingIdentity.equals(identityKey)

        // Store the new identity key
        val key = getIdentityKeyKey(address)
        val serializedKey = String(identityKey.serialize())
        
        encryptedPrefs.edit()
            .putString(key, serializedKey)
            .apply()

        // If this is a new identity or the identity has changed, mark as untrusted initially
        if (hasChanged) {
            setTrustedIdentity(address, false)
        }

        return hasChanged
    }

    override fun isTrustedIdentity(
        address: SignalProtocolAddress,
        identityKey: IdentityKey,
        direction: IdentityKeyStore.Direction
    ): Boolean {
        // For sending messages, we need explicit trust
        // For receiving messages, we can be more lenient on first contact
        
        val storedIdentity = getIdentity(address)
        
        // If no stored identity, this is first contact
        if (storedIdentity == null) {
            return when (direction) {
                IdentityKeyStore.Direction.SENDING -> false // Require explicit verification for sending
                IdentityKeyStore.Direction.RECEIVING -> true // Allow receiving from new contacts
            }
        }

        // If identity matches stored identity, check trust status
        if (storedIdentity.equals(identityKey)) {
            return getTrustedIdentity(address)
        }

        // Identity has changed - this is a security concern
        return false
    }

    override fun getIdentity(address: SignalProtocolAddress): IdentityKey? {
        val identityKeyString = getIdentityKey(address) ?: return null
        
        return try {
            IdentityKey(identityKeyString.toByteArray(), 0)
        } catch (e: InvalidKeyException) {
            null
        }
    }

    /**
     * Mark an identity as trusted (after user verification)
     */
    fun setTrustedIdentity(address: SignalProtocolAddress, trusted: Boolean) {
        val key = getTrustedKey(address)
        encryptedPrefs.edit()
            .putBoolean(key, trusted)
            .apply()
    }

    /**
     * Get all stored identities for verification purposes
     */
    fun getAllIdentities(): Map<SignalProtocolAddress, IdentityKey> {
        val identities = mutableMapOf<SignalProtocolAddress, IdentityKey>()
        
        encryptedPrefs.all.forEach { (key, value) ->
            if (key.startsWith(IDENTITY_PREFIX) && value is String) {
                try {
                    val addressString = key.removePrefix(IDENTITY_PREFIX)
                    val parts = addressString.split("_")
                    if (parts.size == 2) {
                        val address = SignalProtocolAddress(parts[0], parts[1].toInt())
                        val identityKey = IdentityKey(value.toByteArray(), 0)
                        identities[address] = identityKey
                    }
                } catch (e: Exception) {
                    // Skip corrupted entries
                }
            }
        }
        
        return identities
    }

    /**
     * Remove an identity (for cleanup)
     */
    fun removeIdentity(address: SignalProtocolAddress) {
        val identityKey = getIdentityKeyKey(address)
        val trustedKey = getTrustedKey(address)
        
        encryptedPrefs.edit()
            .remove(identityKey)
            .remove(trustedKey)
            .apply()
    }

    private fun getIdentityKey(address: SignalProtocolAddress): String? {
        val key = getIdentityKeyKey(address)
        return encryptedPrefs.getString(key, null)
    }

    private fun getTrustedIdentity(address: SignalProtocolAddress): Boolean {
        val key = getTrustedKey(address)
        return encryptedPrefs.getBoolean(key, false)
    }

    private fun getIdentityKeyKey(address: SignalProtocolAddress): String {
        return "$IDENTITY_PREFIX${address.name}_${address.deviceId}"
    }

    private fun getTrustedKey(address: SignalProtocolAddress): String {
        return "$TRUSTED_PREFIX${address.name}_${address.deviceId}"
    }

    private fun generateAndStoreIdentityKeyPair(): IdentityKeyPair {
        val identityKeyPair = KeyHelper.generateIdentityKeyPair()
        val serializedKey = String(identityKeyPair.serialize())
        
        encryptedPrefs.edit()
            .putString(LOCAL_IDENTITY_KEY, serializedKey)
            .apply()
            
        return identityKeyPair
    }
}