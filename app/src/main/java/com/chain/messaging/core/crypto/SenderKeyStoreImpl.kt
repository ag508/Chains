package com.chain.messaging.core.crypto

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
// Import Signal Protocol types directly
import org.signal.libsignal.protocol.groups.SenderKeyName
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord
import org.signal.libsignal.protocol.SignalProtocolAddress
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SenderKeyStore using encrypted shared preferences for group messaging
 */
@Singleton
class SenderKeyStoreImpl @Inject constructor(
    private val context: Context
) : SenderKeyStore {

    companion object {
        private const val PREFS_NAME = "chain_sender_keys"
        private const val KEYSTORE_ALIAS = "ChainSenderKeys"
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

    override fun storeSenderKey(senderKeyName: SenderKeyName, record: SenderKeyRecord) {
        val key = getSenderKeyKey(senderKeyName)
        val serializedRecord = String(record.serialize())
        
        encryptedPrefs.edit()
            .putString(key, serializedRecord)
            .apply()
    }

    override fun loadSenderKey(senderKeyName: SenderKeyName): SenderKeyRecord? {
        val key = getSenderKeyKey(senderKeyName)
        val serializedRecord = encryptedPrefs.getString(key, null) ?: return null
        
        return try {
            SenderKeyRecord(serializedRecord.toByteArray())
        } catch (e: Exception) {
            // If record is corrupted, return null
            null
        }
    }

    /**
     * Remove a sender key record
     */
    fun removeSenderKey(senderKeyName: SenderKeyName) {
        val key = getSenderKeyKey(senderKeyName)
        encryptedPrefs.edit()
            .remove(key)
            .apply()
    }

    /**
     * Remove all sender keys for a specific group
     */
    fun removeAllSenderKeysForGroup(groupId: String) {
        val prefix = "group_${groupId}_"
        val keysToRemove = encryptedPrefs.all.keys.filter { it.startsWith(prefix) }
        
        val editor = encryptedPrefs.edit()
        keysToRemove.forEach { key ->
            editor.remove(key)
        }
        editor.apply()
    }

    /**
     * Get all sender key names for a specific group
     */
    fun getSenderKeyNamesForGroup(groupId: String): List<SenderKeyName> {
        val prefix = "group_${groupId}_"
        return encryptedPrefs.all.keys
            .filter { it.startsWith(prefix) }
            .mapNotNull { key ->
                try {
                    val parts = key.removePrefix(prefix).split("_device_")
                    if (parts.size == 2) {
                        val senderName = parts[0]
                        val deviceId = parts[1].toInt()
                        SenderKeyName(
                            groupId, 
                            SignalProtocolAddress(senderName, deviceId)
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
    }

    /**
     * Clear all sender keys (for logout/reset)
     */
    fun clearAllSenderKeys() {
        encryptedPrefs.edit().clear().apply()
    }

    private fun getSenderKeyKey(senderKeyName: SenderKeyName): String {
        return "group_${senderKeyName.groupId}_${senderKeyName.sender.name}_device_${senderKeyName.sender.deviceId}"
    }
}