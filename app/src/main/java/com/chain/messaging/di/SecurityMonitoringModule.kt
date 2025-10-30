package com.chain.messaging.di

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.app.NotificationManagerCompat
import com.chain.messaging.core.security.*
import com.chain.messaging.data.local.dao.SecurityEventDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityMonitoringModule {
    
    @Provides
    @Singleton
    fun provideConnectivityManager(
        @ApplicationContext context: Context
    ): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    
    @Provides
    @Singleton
    fun provideNotificationManagerCompat(
        @ApplicationContext context: Context
    ): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }
    
    @Provides
    @Singleton
    fun provideSecurityEventStorage(
        securityEventDao: SecurityEventDao
    ): SecurityEventStorage {
        return SecurityEventStorageImpl(securityEventDao)
    }
    
    @Provides
    @Singleton
    fun provideThreatDetector(
        @ApplicationContext context: Context,
        connectivityManager: ConnectivityManager
    ): ThreatDetector {
        return ThreatDetectorImpl(context, connectivityManager)
    }
    
    @Provides
    @Singleton
    fun provideAlertNotificationService(
        @ApplicationContext context: Context,
        notificationManager: NotificationManagerCompat
    ): AlertNotificationService {
        return AlertNotificationServiceImpl(context, notificationManager)
    }
    
    @Provides
    @Singleton
    fun provideSecurityMonitoringManager(
        @ApplicationContext context: Context,
        securityEventStorage: SecurityEventStorage,
        threatDetector: ThreatDetector,
        alertNotificationService: AlertNotificationService
    ): SecurityMonitoringManager {
        return SecurityMonitoringManagerImpl(
            context,
            securityEventStorage,
            threatDetector,
            alertNotificationService
        )
    }
}