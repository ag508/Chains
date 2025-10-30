package com.chain.messaging.core.privacy

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.*
import com.chain.messaging.core.privacy.DisappearingMessageManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Background service for cleaning up expired disappearing messages.
 */
@AndroidEntryPoint
class DisappearingMessageCleanupService : Service() {
    
    @Inject
    lateinit var disappearingMessageManager: DisappearingMessageManager
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            disappearingMessageManager.startCleanupService()
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.launch {
            disappearingMessageManager.stopCleanupService()
        }
    }
}

/**
 * WorkManager worker for periodic cleanup of expired messages.
 */
class DisappearingMessageCleanupWorker(
    context: android.content.Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // Get the DisappearingMessageManager through dependency injection
            // In a real implementation, you'd use Hilt's WorkManager integration
            val deletedCount = 0 // disappearingMessageManager.cleanupExpiredMessages()
            
            Result.success(
                workDataOf("deleted_count" to deletedCount)
            )
        } catch (e: Exception) {
            Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "disappearing_message_cleanup"
        
        fun schedulePeriodicCleanup(workManager: WorkManager) {
            val cleanupRequest = PeriodicWorkRequestBuilder<DisappearingMessageCleanupWorker>(
                15, TimeUnit.MINUTES // Run every 15 minutes
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest
            )
        }
        
        fun cancelPeriodicCleanup(workManager: WorkManager) {
            workManager.cancelUniqueWork(WORK_NAME)
        }
    }
}