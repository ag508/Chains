package com.chain.messaging.core.crypto

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.state.SessionRecord
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SessionStorage using encrypted shared preferences
 */
@Singleton
class SessionStorageImpl @Inject constructor(
    private val context: Context
) : SessionStorage {

    companion object {
        private const val PREFS_NAME = "chain_sessions"
        private const val KEYSTORE_ALIAS = "ChainSessionKeys"
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

    override fun loadSession(address: SignalProtocolAddress): SessionRecord {
        val key = getSessionKey(address)
        val sessionData = encryptedPrefs.getString(key, null)
        
        return if (sessionData != null) {
            try {
                SessionRecord(sessionData.toByteArray())
            } catch (e: Exception) {
                // If session data is corrupted, return fresh session
                SessionRecord()
            }
        } else {
            SessionRecord()
        }
    }

    override fun getSubDeviceSessions(name: String): List<Int> {
        val prefix = "${name}_"
        return encryptedPrefs.all.keys
            .filter { it.startsWith(prefix) }
            .mapNotNull { key ->
                key.removePrefix(prefix).toIntOrNull()
            }
    }

    override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) {
        val key = getSessionKey(address)
        val sessionData = String(record.serialize())
        
        encryptedPrefs.edit()
            .putString(key, sessionData)
            .apply()
    }

    override fun containsSession(address: SignalProtocolAddress): Boolean {
        val key = getSessionKey(address)
        return encryptedPrefs.contains(key)
    }

    override fun deleteSession(address: SignalProtocolAddress) {
        val key = getSessionKey(address)
        encryptedPrefs.edit()
            .remove(key)
            .apply()
    }

    override fun deleteAllSessions(name: String) {
        val prefix = "${name}_"
        val keysToRemove = encryptedPrefs.all.keys.filter { it.startsWith(prefix) }
        
        val editor = encryptedPrefs.edit()
        keysToRemove.forEach { key ->
            editor.remove(key)
        }
        editor.apply()
    }

    private fun getSessionKey(address: SignalProtocolAddress): String {
        return "${address.name}_${address.deviceId}"
    }
}