package com.chain.messaging.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitorImpl @Inject constructor(
    private val context: Context
) : NetworkMonitor {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var isMonitoring = false
    
    override val isConnected: Flow<Boolean> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }
            
            override fun onLost(network: Network) {
                trySend(false)
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                trySend(hasInternet)
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        // Send initial state
        trySend(isNetworkCurrentlyAvailable())
        
        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()
    
    override val networkType: Flow<NetworkType> = isConnected.map {
        getCurrentNetworkType()
    }.distinctUntilChanged()
    
    override suspend fun isNetworkAvailable(): Boolean {
        return isNetworkCurrentlyAvailable()
    }
    
    override suspend fun isBlockchainReachable(): Boolean {
        return try {
            // Test connection to a known blockchain node
            // This is a simplified check - in production, you'd test actual blockchain endpoints
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53), 3000) // DNS test
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getNetworkQuality(): NetworkQualityMetrics {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        return NetworkQualityMetrics(
            latency = measureLatency(),
            bandwidth = estimateBandwidth(networkCapabilities),
            isStable = isConnectionStable(),
            signalStrength = getSignalStrength(networkCapabilities)
        )
    }
    
    override suspend fun startMonitoring() {
        if (!isMonitoring) {
            isMonitoring = true
            // Monitoring is handled by the Flow-based approach
            // This method can be used for any additional setup if needed
        }
    }
    
    override fun isConnected(): Boolean {
        return isNetworkCurrentlyAvailable()
    }
    
    private fun isNetworkCurrentlyAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    private fun getCurrentNetworkType(): NetworkType {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkType.NONE
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.NONE
        }
    }
    
    private suspend fun measureLatency(): Long {
        return try {
            val startTime = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53), 3000)
            val latency = System.currentTimeMillis() - startTime
            socket.close()
            latency
        } catch (e: Exception) {
            -1L // Indicates measurement failed
        }
    }
    
    private fun estimateBandwidth(networkCapabilities: NetworkCapabilities?): Long {
        return networkCapabilities?.linkDownstreamBandwidthKbps?.toLong()?.times(1024) ?: 0L
    }
    
    private fun isConnectionStable(): Boolean {
        // Simple stability check - in production, this would be more sophisticated
        return isNetworkCurrentlyAvailable()
    }
    
    private fun getSignalStrength(networkCapabilities: NetworkCapabilities?): Int {
        // This is a simplified implementation
        // In production, you'd get actual signal strength from network capabilities
        return if (networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true) {
            75 // Assume good signal if validated
        } else {
            25 // Assume poor signal otherwise
        }
    }
}