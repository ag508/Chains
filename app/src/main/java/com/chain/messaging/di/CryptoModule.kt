package com.chain.messaging.di

import android.content.Context
import com.chain.messaging.core.crypto.IdentityStorage
import com.chain.messaging.core.crypto.IdentityStorageImpl
import com.chain.messaging.core.crypto.KeyManager
import com.chain.messaging.core.crypto.SenderKeyStore
import com.chain.messaging.core.crypto.SenderKeyStoreImpl
import com.chain.messaging.core.crypto.SessionStorage
import com.chain.messaging.core.crypto.SessionStorageImpl
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.crypto.SignalProtocolStore
import com.chain.messaging.core.crypto.SignalProtocolStoreAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for cryptographic components
 */
@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    @Provides
    @Singleton
    fun provideKeyManager(
        @ApplicationContext context: Context
    ): KeyManager {
        return KeyManager(context)
    }

    @Provides
    @Singleton
    fun provideSessionStorage(
        @ApplicationContext context: Context
    ): SessionStorage {
        return SessionStorageImpl(context)
    }

    @Provides
    @Singleton
    fun provideIdentityStorage(
        @ApplicationContext context: Context
    ): IdentityStorage {
        return IdentityStorageImpl(context)
    }

    @Provides
    @Singleton
    fun provideSignalProtocolStore(
        keyManager: KeyManager,
        sessionStorage: SessionStorage,
        identityStorage: IdentityStorage
    ): SignalProtocolStore {
        return SignalProtocolStore(keyManager, sessionStorage, identityStorage)
    }

    @Provides
    @Singleton
    fun provideSenderKeyStore(
        @ApplicationContext context: Context
    ): SenderKeyStore {
        return SenderKeyStoreImpl(context)
    }

    @Provides
    @Singleton
    fun provideSignalProtocolStoreAdapter(
        identityStorage: IdentityStorageImpl,
        sessionStorage: SessionStorageImpl,
        senderKeyStore: SenderKeyStoreImpl,
        keyManager: KeyManager
    ): SignalProtocolStoreAdapter {
        return SignalProtocolStoreAdapter(identityStorage, sessionStorage, senderKeyStore, keyManager)
    }

    @Provides
    @Singleton
    fun provideSignalEncryptionService(
        protocolStoreAdapter: SignalProtocolStoreAdapter
    ): SignalEncryptionService {
        return SignalEncryptionService(protocolStoreAdapter)
    }
}