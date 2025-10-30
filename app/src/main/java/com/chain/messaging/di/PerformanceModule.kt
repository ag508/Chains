package com.chain.messaging.di

import com.chain.messaging.core.performance.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for performance monitoring dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PerformanceModule {
    
    @Binds
    @Singleton
    abstract fun bindPerformanceMonitor(
        performanceMonitorImpl: PerformanceMonitorImpl
    ): PerformanceMonitor
    
    @Binds
    @Singleton
    abstract fun bindPerformanceStorage(
        performanceStorageImpl: PerformanceStorageImpl
    ): PerformanceStorage
    
    @Binds
    @Singleton
    abstract fun bindBatteryOptimizer(
        batteryOptimizerImpl: BatteryOptimizerImpl
    ): BatteryOptimizer
    
    @Binds
    @Singleton
    abstract fun bindMemoryManager(
        memoryManagerImpl: MemoryManagerImpl
    ): MemoryManager
    
    @Binds
    @Singleton
    abstract fun bindPerformanceTester(
        performanceTesterImpl: PerformanceTesterImpl
    ): PerformanceTester
}