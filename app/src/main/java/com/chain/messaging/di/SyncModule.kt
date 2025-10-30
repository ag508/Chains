package com.chain.messaging.di

import com.chain.messaging.core.sync.CrossDeviceSyncService
import com.chain.messaging.core.sync.CrossDeviceSyncServiceImpl
import com.chain.messaging.core.sync.DeviceManager
import com.chain.messaging.core.sync.DeviceManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for synchronization components
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {
    
    @Binds
    @Singleton
    abstract fun bindDeviceManager(
        deviceManagerImpl: DeviceManagerImpl
    ): DeviceManager
    
    @Binds
    @Singleton
    abstract fun bindCrossDeviceSyncService(
        crossDeviceSyncServiceImpl: CrossDeviceSyncServiceImpl
    ): CrossDeviceSyncService
}