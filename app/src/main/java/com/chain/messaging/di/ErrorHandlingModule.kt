package com.chain.messaging.di

import com.chain.messaging.core.error.*
import com.chain.messaging.core.network.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for error handling components
 */
@Module
@InstallIn(SingletonComponent::class)
object ErrorHandlingModule {
    
    @Provides
    @Singleton
    fun provideErrorHandler(): ErrorHandler {
        return ErrorHandler()
    }
    
    @Provides
    @Singleton
    fun provideErrorRecoveryManager(
        errorHandler: ErrorHandler,
        networkMonitor: NetworkMonitor
    ): ErrorRecoveryManager {
        return ErrorRecoveryManager(errorHandler, networkMonitor).apply {
            startNetworkRecoveryMonitoring()
        }
    }
    
    @Provides
    @Singleton
    fun provideBlockchainErrorHandler(
        errorHandler: ErrorHandler,
        errorRecoveryManager: ErrorRecoveryManager
    ): BlockchainErrorHandler {
        return BlockchainErrorHandler(errorHandler, errorRecoveryManager)
    }
    
    @Provides
    @Singleton
    fun provideEncryptionErrorHandler(
        errorHandler: ErrorHandler,
        errorRecoveryManager: ErrorRecoveryManager
    ): EncryptionErrorHandler {
        return EncryptionErrorHandler(errorHandler, errorRecoveryManager)
    }
    
    @Provides
    @Singleton
    fun provideNetworkErrorHandler(
        errorHandler: ErrorHandler,
        errorRecoveryManager: ErrorRecoveryManager,
        networkMonitor: NetworkMonitor
    ): NetworkErrorHandler {
        return NetworkErrorHandler(errorHandler, errorRecoveryManager, networkMonitor)
    }
    
    @Provides
    @Singleton
    fun provideP2PErrorHandler(
        errorHandler: ErrorHandler,
        errorRecoveryManager: ErrorRecoveryManager
    ): P2PErrorHandler {
        return P2PErrorHandler(errorHandler, errorRecoveryManager)
    }
    
    @Provides
    @Singleton
    fun provideWebRTCErrorHandler(
        errorHandler: ErrorHandler,
        errorRecoveryManager: ErrorRecoveryManager
    ): WebRTCErrorHandler {
        return WebRTCErrorHandler(errorHandler, errorRecoveryManager)
    }
    
    @Provides
    @Singleton
    fun provideStorageErrorHandler(
        errorHandler: ErrorHandler,
        errorRecoveryManager: ErrorRecoveryManager
    ): StorageErrorHandler {
        return StorageErrorHandler(errorHandler, errorRecoveryManager)
    }
    
    @Provides
    @Singleton
    fun provideMessagingErrorHandler(
        errorHandler: ErrorHandler,
        errorRecoveryManager: ErrorRecoveryManager
    ): MessagingErrorHandler {
        return MessagingErrorHandler(errorHandler, errorRecoveryManager)
    }
}