package com.chain.messaging.di

import android.content.Context
import com.chain.messaging.core.network.NetworkMonitor
import com.chain.messaging.core.network.NetworkMonitorImpl
import com.chain.messaging.core.offline.*
import com.chain.messaging.data.local.ChainDatabase
import com.chain.messaging.data.local.dao.QueuedMessageDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OfflineModule {
    
    @Binds
    @Singleton
    abstract fun bindOfflineMessageQueue(
        offlineMessageQueueImpl: OfflineMessageQueueImpl
    ): OfflineMessageQueue
    
    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        networkMonitorImpl: NetworkMonitorImpl
    ): NetworkMonitor
    
    @Binds
    @Singleton
    abstract fun bindConflictResolver(
        conflictResolverImpl: ConflictResolverImpl
    ): ConflictResolver
    
    @Binds
    @Singleton
    abstract fun bindOfflineSyncService(
        offlineSyncServiceImpl: OfflineSyncServiceImpl
    ): OfflineSyncService
    
    companion object {
        
        @Provides
        @Singleton
        fun provideQueuedMessageDao(database: ChainDatabase): QueuedMessageDao {
            return database.queuedMessageDao()
        }
        
        @Provides
        @Singleton
        fun provideBackoffStrategy(): BackoffStrategy {
            return ExponentialBackoffStrategy(
                baseDelaySeconds = 2,
                maxDelaySeconds = 300,
                jitterFactor = 0.1
            )
        }
        
        @Provides
        @Singleton
        fun provideOfflineCoroutineScope(): CoroutineScope {
            return CoroutineScope(SupervisorJob())
        }
    }
}