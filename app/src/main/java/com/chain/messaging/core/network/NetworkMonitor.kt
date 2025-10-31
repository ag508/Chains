package com.chain.messaging.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Interface for monitoring network connectivity
 */
interface NetworkMonitor {
    /**
     * Flow that emits true when network is available, false otherwise
     */
    val isConnected: Flow<Boolean>
    
    /**
     * Flow that emits the current network type
     */
    val networkType: Flow<NetworkType>
    
    /**
     * Check if network is currently available
     */
    suspend fun isNetworkAvailable(): Boolean
    
    /**
     * Check if blockchain network is reachable
     */
    suspend fun isBlockchainReachable(): Boolean
    
    /**
     * Get current network quality metrics
     */
    suspend fun getNetworkQuality(): NetworkQualityMetrics
    
    /**
     * Start network monitoring
     */
    suspend fun startMonitoring()
    
    /**
     * Check if connected (synchronous)
     */
    fun isConnected(): Boolean
}

/**
 * Types of network connections
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    NONE
}

/**
 * Network quality metrics
 */
data class NetworkQualityMetrics(
    val latency: Long, // in milliseconds
    val bandwidth: Long, // in bytes per second
    val isStable: Boolean,
    val signalStrength: Int // 0-100
)