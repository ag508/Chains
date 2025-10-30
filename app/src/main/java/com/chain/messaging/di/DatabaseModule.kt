package com.chain.messaging.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.chain.messaging.core.config.AppConfig
import com.chain.messaging.data.local.ChainDatabase
import com.chain.messaging.data.local.dao.ChatDao
import com.chain.messaging.data.local.dao.DeviceDao
import com.chain.messaging.data.local.dao.MessageDao
import com.chain.messaging.data.local.dao.PerformanceDao
import com.chain.messaging.data.local.dao.QueuedMessageDao
import com.chain.messaging.data.local.dao.ReactionDao
import com.chain.messaging.data.local.dao.SecurityEventDao
import com.chain.messaging.data.local.dao.SyncLogDao
import com.chain.messaging.data.local.dao.UserDao
import com.chain.messaging.data.local.dao.UserSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideMasterKey(@ApplicationContext context: Context): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context,
        masterKey: MasterKey
    ): EncryptedSharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            AppConfig.ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }
    
    @Provides
    @Singleton
    fun provideDatabasePassphrase(
        encryptedSharedPreferences: EncryptedSharedPreferences
    ): String {
        val existingPassphrase = encryptedSharedPreferences.getString(AppConfig.DB_PASSPHRASE_KEY, null)
        return if (existingPassphrase != null) {
            existingPassphrase
        } else {
            // Generate a new passphrase
            val newPassphrase = java.util.UUID.randomUUID().toString()
            encryptedSharedPreferences.edit()
                .putString(AppConfig.DB_PASSPHRASE_KEY, newPassphrase)
                .apply()
            newPassphrase
        }
    }
    
    @Provides
    @Singleton
    fun provideChainDatabase(
        @ApplicationContext context: Context,
        passphrase: String
    ): ChainDatabase {
        return ChainDatabase.getDatabase(context, passphrase)
    }
    
    @Provides
    fun provideUserDao(database: ChainDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideMessageDao(database: ChainDatabase): MessageDao {
        return database.messageDao()
    }
    
    @Provides
    fun provideChatDao(database: ChainDatabase): ChatDao {
        return database.chatDao()
    }
    
    @Provides
    fun provideDeviceDao(database: ChainDatabase): DeviceDao {
        return database.deviceDao()
    }
    
    @Provides
    fun provideSyncLogDao(database: ChainDatabase): SyncLogDao {
        return database.syncLogDao()
    }
    
    @Provides
    fun provideQueuedMessageDao(database: ChainDatabase): QueuedMessageDao {
        return database.queuedMessageDao()
    }
    
    @Provides
    fun provideReactionDao(database: ChainDatabase): ReactionDao {
        return database.reactionDao()
    }
    
    @Provides
    fun provideUserSettingsDao(database: ChainDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }
    
    @Provides
    fun providePerformanceDao(database: ChainDatabase): PerformanceDao {
        return database.performanceDao()
    }
    
    @Provides
    fun provideSecurityEventDao(database: ChainDatabase): SecurityEventDao {
        return database.securityEventDao()
    }
}