package com.chain.messaging

import android.app.Application
import com.chain.messaging.core.config.AppConfig
import com.chain.messaging.core.integration.ChainApplicationManager
import com.chain.messaging.core.notification.NotificationChannelManager
import com.chain.messaging.core.performance.PerformanceMonitor
import com.chain.messaging.core.performance.BatteryOptimizer
import com.chain.messaging.core.performance.MemoryManager
import com.chain.messaging.core.util.Logger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Application class for Chain messaging app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class ChainApplication : Application() {
    
    @Inject
    lateinit var applicationManager: ChainApplicationManager
    
    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager
    
    @Inject
    lateinit var performanceMonitor: PerformanceMonitor
    
    @Inject
    lateinit var batteryOptimizer: BatteryOptimizer
    
    @Inject
    lateinit var memoryManager: MemoryManager
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        Logger.i("Initializing Chain Application v${AppConfig.APP_VERSION}")
        
        // Initialize SQLCipher
        try {
            net.sqlcipher.database.SQLiteDatabase.loadLibs(this)
            Logger.i("SQLCipher initialized successfully")
        } catch (e: Exception) {
            Logger.e("Failed to initialize SQLCipher", e)
        }
        
        // Initialize notification channels
        try {
            notificationChannelManager.createNotificationChannels()
            Logger.i("Notification channels initialized successfully")
        } catch (e: Exception) {
            Logger.e("Failed to initialize notification channels", e)
        }
        
        // Initialize the integrated Chain application manager
        applicationScope.launch {
            try {
                val initResult = applicationManager.initialize()
                if (initResult.isSuccess) {
                    Logger.i("Chain Application Manager initialized successfully")
                } else {
                    Logger.e("Failed to initialize Chain Application Manager", initResult.exceptionOrNull())
                }
            } catch (e: Exception) {
                Logger.e("Unexpected error during Chain Application Manager initialization", e)
            }
        }
        
        Logger.i("Chain Application initialization complete")
    }
    
    override fun onTerminate() {
        super.onTerminate()
        
        Logger.i("Shutting down Chain Application")
        
        // Gracefully shutdown the application manager
        applicationScope.launch {
            try {
                applicationManager.shutdown()
                Logger.i("Chain Application Manager shutdown complete")
            } catch (e: Exception) {
                Logger.e("Error during Chain Application Manager shutdown", e)
            }
        }
    }
}