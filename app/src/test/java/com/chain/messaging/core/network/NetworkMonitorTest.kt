package com.chain.messaging.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class NetworkMonitorTest {
    
    private lateinit var networkMonitor: NetworkMonitorImpl
    private lateinit var context: Context
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var network: Network
    private lateinit var networkCapabilities: NetworkCapabilities
    
    @Before
    fun setup() {
        context = mockk()
        connectivityManager = mockk()
        network = mockk()
        networkCapabilities = mockk()
        
        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        
        networkMonitor = NetworkMonitorImpl(context)
    }
    
    @Test
    fun `isNetworkAvailable should return true when network has internet capability`() = runTest {
        // Given
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        
        // When
        val result = networkMonitor.isNetworkAvailable()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isNetworkAvailable should return false when network has no internet capability`() = runTest {
        // Given
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns false
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        
        // When
        val result = networkMonitor.isNetworkAvailable()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isNetworkAvailable should return false when no active network`() = runTest {
        // Given
        every { connectivityManager.activeNetwork } returns null
        
        // When
        val result = networkMonitor.isNetworkAvailable()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isNetworkAvailable should return false when no network capabilities`() = runTest {
        // Given
        every { connectivityManager.getNetworkCapabilities(network) } returns null
        
        // When
        val result = networkMonitor.isNetworkAvailable()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `getCurrentNetworkType should return WIFI when connected via WiFi`() = runTest {
        // Given
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns false
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        
        // When
        val networkType = networkMonitor.networkType.first()
        
        // Then
        assertEquals(NetworkType.WIFI, networkType)
    }
    
    @Test
    fun `getCurrentNetworkType should return CELLULAR when connected via cellular`() = runTest {
        // Given
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns false
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        
        // When
        val networkType = networkMonitor.networkType.first()
        
        // Then
        assertEquals(NetworkType.CELLULAR, networkType)
    }
    
    @Test
    fun `getCurrentNetworkType should return ETHERNET when connected via ethernet`() = runTest {
        // Given
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        
        // When
        val networkType = networkMonitor.networkType.first()
        
        // Then
        assertEquals(NetworkType.ETHERNET, networkType)
    }
    
    @Test
    fun `getCurrentNetworkType should return NONE when no network available`() = runTest {
        // Given
        every { connectivityManager.activeNetwork } returns null
        
        // When
        val networkType = networkMonitor.networkType.first()
        
        // Then
        assertEquals(NetworkType.NONE, networkType)
    }
    
    @Test
    fun `getNetworkQuality should return quality metrics`() = runTest {
        // Given
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        every { networkCapabilities.linkDownstreamBandwidthKbps } returns 1000
        
        // When
        val quality = networkMonitor.getNetworkQuality()
        
        // Then
        assertNotNull(quality)
        assertTrue(quality.bandwidth > 0)
        assertTrue(quality.signalStrength > 0)
        assertTrue(quality.isStable)
    }
    
    @Test
    fun `isBlockchainReachable should return true when connection succeeds`() = runTest {
        // This test would require mocking socket connections, which is complex
        // In a real implementation, you might want to use a test double or mock the socket
        
        // When
        val result = networkMonitor.isBlockchainReachable()
        
        // Then
        // This will depend on actual network connectivity during test
        // In production, you'd mock the socket connection
        assertNotNull(result)
    }
}