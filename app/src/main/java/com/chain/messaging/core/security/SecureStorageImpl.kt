package com.chain.messaging.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorageImpl @Inject constructor(
    private val context: Context
) : SecureStorage {
    
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "chain_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    override suspend fun store(key: String, value: String) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit()
            .putString(key, value)
            .apply()
    }
    
    override suspend fun get(key: String): String? = withContext(Dispatchers.IO) {
        encryptedPrefs.getString(key, null)
    }
    
    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit()
            .remove(key)
            .apply()
    }
    
    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.IO) {
        encryptedPrefs.contains(key)
    }
    
    override suspend fun clear() = withContext(Dispatchers.IO) {
        encryptedPrefs.edit()
            .clear()
            .apply()
    }
}