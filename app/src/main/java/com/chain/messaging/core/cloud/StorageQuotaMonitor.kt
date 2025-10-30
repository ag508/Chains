package com.chain.messaging.core.cloud

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monitors storage quota across all cloud services and provides alerts
 */
@Singleton
class StorageQuotaMonitor @Inject constructor(
    private val cloudStorageManager: CloudStorageManager
) {
    
    private val _quotaAlerts = MutableStateFlow<List<QuotaAlert>>(emptyList())
    val quotaAlerts: Flow<List<QuotaAlert>> = _quotaAlerts.asStateFlow()
    
    companion object {
        private const val WARNING_THRESHOLD = 80f // 80% usage
        private const val CRITICAL_THRESHOLD = 95f // 95% usage
    }
    
    /**
     * Check storage quota for all authenticated services
     */
    suspend fun checkAllQuotas(): List<StorageInfo> {
        val storageInfos = mutableListOf<StorageInfo>()
        val alerts = mutableListOf<QuotaAlert>()
        
        CloudService.values().forEach { service ->
            try {
                val storageInfo = cloudStorageManager.getStorageInfo(service)
                if (storageInfo != null) {
                    storageInfos.add(storageInfo)
                    
                    // Check for quota alerts
                    val alert = checkQuotaAlert(storageInfo)
                    if (alert != null) {
                        alerts.add(alert)
                    }
                }
            } catch (e: Exception) {
                // Continue with other services
            }
        }
        
        _quotaAlerts.value = alerts
        return storageInfos
    }
    
    /**
     * Check quota for a specific service
     */
    suspend fun checkServiceQuota(service: CloudService): StorageInfo? {
        return try {
            val storageInfo = cloudStorageManager.getStorageInfo(service)
            if (storageInfo != null) {
                val alert = checkQuotaAlert(storageInfo)
                if (alert != null) {
                    val currentAlerts = _quotaAlerts.value.toMutableList()
                    currentAlerts.removeAll { it.service == service }
                    currentAlerts.add(alert)
                    _quotaAlerts.value = currentAlerts
                }
            }
            storageInfo
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get storage recommendations based on usage
     */
    suspend fun getStorageRecommendations(): List<StorageRecommendation> {
        val recommendations = mutableListOf<StorageRecommendation>()
        val storageInfos = checkAllQuotas()
        
        storageInfos.forEach { info ->
            when {
                info.usagePercentage >= CRITICAL_THRESHOLD -> {
                    recommendations.add(
                        StorageRecommendation(
                            service = info.service,
                            type = RecommendationType.CRITICAL_CLEANUP,
                            message = "Storage is ${info.usagePercentage.toInt()}% full. Immediate cleanup required.",
                            priority = Priority.HIGH
                        )
                    )
                }
                info.usagePercentage >= WARNING_THRESHOLD -> {
                    recommendations.add(
                        StorageRecommendation(
                            service = info.service,
                            type = RecommendationType.CLEANUP_SUGGESTED,
                            message = "Storage is ${info.usagePercentage.toInt()}% full. Consider cleaning up old files.",
                            priority = Priority.MEDIUM
                        )
                    )
                }
                info.availableSpace < 1024 * 1024 * 100 -> { // Less than 100MB
                    recommendations.add(
                        StorageRecommendation(
                            service = info.service,
                            type = RecommendationType.LOW_SPACE,
                            message = "Less than 100MB available space remaining.",
                            priority = Priority.MEDIUM
                        )
                    )
                }
            }
        }
        
        return recommendations
    }
    
    /**
     * Clear alerts for a specific service
     */
    fun clearAlertsForService(service: CloudService) {
        val currentAlerts = _quotaAlerts.value.toMutableList()
        currentAlerts.removeAll { it.service == service }
        _quotaAlerts.value = currentAlerts
    }
    
    /**
     * Clear all alerts
     */
    fun clearAllAlerts() {
        _quotaAlerts.value = emptyList()
    }
    
    private fun checkQuotaAlert(storageInfo: StorageInfo): QuotaAlert? {
        return when {
            storageInfo.usagePercentage >= CRITICAL_THRESHOLD -> {
                QuotaAlert(
                    service = storageInfo.service,
                    level = AlertLevel.CRITICAL,
                    message = "Storage is ${storageInfo.usagePercentage.toInt()}% full",
                    usagePercentage = storageInfo.usagePercentage
                )
            }
            storageInfo.usagePercentage >= WARNING_THRESHOLD -> {
                QuotaAlert(
                    service = storageInfo.service,
                    level = AlertLevel.WARNING,
                    message = "Storage is ${storageInfo.usagePercentage.toInt()}% full",
                    usagePercentage = storageInfo.usagePercentage
                )
            }
            else -> null
        }
    }
}

/**
 * Represents a storage quota alert
 */
data class QuotaAlert(
    val service: CloudService,
    val level: AlertLevel,
    val message: String,
    val usagePercentage: Float
)

/**
 * Alert levels for storage quota
 */
enum class AlertLevel {
    WARNING,
    CRITICAL
}

/**
 * Storage recommendations for users
 */
data class StorageRecommendation(
    val service: CloudService,
    val type: RecommendationType,
    val message: String,
    val priority: Priority
)

/**
 * Types of storage recommendations
 */
enum class RecommendationType {
    CLEANUP_SUGGESTED,
    CRITICAL_CLEANUP,
    LOW_SPACE,
    OPTIMIZE_STORAGE
}

/**
 * Priority levels for recommendations
 */
enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}