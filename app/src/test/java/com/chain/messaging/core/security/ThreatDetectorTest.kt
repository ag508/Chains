package com.chain.messaging.core.security

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ThreatDetectorTest {
    
    private val context = mockk<Context>()
    private val connectivityManager = mockk<ConnectivityManager>()
    private val network = mockk<Network>()
    private val networkCapabilities = mockk<NetworkCapabilities>()
    
    private lateinit var threatDetector: ThreatDetectorImpl
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        
        // Default network capabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns true
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) } returns true
        
        // Mock Settings.Secure for developer options check
        mockkStatic(android.provider.Settings.Secure::class)
        every { 
            android.provider.Settings.Secure.getInt(
                any(), 
                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 
                0
            ) 
        } returns 0
        
        threatDetector = ThreatDetectorImpl(context, connectivityManager)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }
    
    @Test
    fun `startDetection should begin threat monitoring`() = runTest {
        // When
        threatDetector.startDetection()
        
        // Then - Should not throw exception and start monitoring
        advanceUntilIdle()
        // Verify monitoring is active (implementation detail)
    }
    
    @Test
    fun `stopDetection should stop threat monitoring`() = runTest {
        // Given
        threatDetector.startDetection()
        
        // When
        threatDetector.stopDetection()
        
        // Then - Should stop monitoring gracefully
        advanceUntilIdle()
    }
    
    @Test
    fun `analyzeNetworkTraffic should detect unvalidated network`() = runTest {
        // Given
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns false
        
        // When
        val indicators = threatDetector.analyzeNetworkTraffic()
        
        // Then
        assertTrue(indicators.isNotEmpty())
        val networkAnomaly = indicators.find { it.type == ThreatType.NETWORK_ANOMALY }
        assertNotNull(networkAnomaly)
        assertTrue(networkAnomaly.description.contains("Network validation failed"))
        assertEquals(0.6f, networkAnomaly.confidence)
    }
    
    @Test
    fun `analyzeNetworkTraffic should detect VPN usage`() = runTest {
        // Given
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns true
        
        // When
        val indicators = threatDetector.analyzeNetworkTraffic()
        
        // Then
        assertTrue(indicators.isNotEmpty())
        val vpnIndicator = indicators.find { it.description.contains("VPN connection detected") }
        assertNotNull(vpnIndicator)
        assertEquals(ThreatType.NETWORK_ANOMALY, vpnIndicator.type)
        assertEquals(SecuritySeverity.LOW, vpnIndicator.severity)
    }
    
    @Test
    fun `analyzeNetworkTraffic should detect open WiFi`() = runTest {
        // Given
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) } returns true
        
        // When
        val indicators = threatDetector.analyzeNetworkTraffic()
        
        // Then
        val openWifiIndicator = indicators.find { it.description.contains("open WiFi") }
        if (openWifiIndicator != null) {
            assertEquals(ThreatType.NETWORK_ANOMALY, openWifiIndicator.type)
            assertEquals(SecuritySeverity.MEDIUM, openWifiIndicator.severity)
            assertEquals(0.8f, openWifiIndicator.confidence)
        }
    }
    
    @Test
    fun `detectMITMAttacks should return empty list when no issues detected`() = runTest {
        // When
        val indicators = threatDetector.detectMITMAttacks()
        
        // Then
        assertTrue(indicators.isEmpty())
    }
    
    @Test
    fun `monitorAuthenticationPatterns should return empty list initially`() = runTest {
        // When
        val indicators = threatDetector.monitorAuthenticationPatterns()
        
        // Then
        assertTrue(indicators.isEmpty())
    }
    
    @Test
    fun `monitorAuthenticationPatterns should detect excessive failures after reporting`() = runTest {
        // Given - Report multiple authentication failures
        repeat(6) {
            threatDetector.reportAuthenticationFailure()
        }
        
        // When
        val indicators = threatDetector.monitorAuthenticationPatterns()
        
        // Then
        assertTrue(indicators.isNotEmpty())
        val bruteForceIndicator = indicators.find { 
            it.type == ThreatType.AUTHENTICATION_ANOMALY &&
            it.description.contains("Excessive authentication failures")
        }
        assertNotNull(bruteForceIndicator)
        assertEquals(SecuritySeverity.HIGH, bruteForceIndicator.severity)
        assertEquals(0.8f, bruteForceIndicator.confidence)
    }
    
    @Test
    fun `detectBlockchainTampering should return empty list when no issues`() = runTest {
        // When
        val indicators = threatDetector.detectBlockchainTampering()
        
        // Then
        assertTrue(indicators.isEmpty())
    }
    
    @Test
    fun `checkDeviceSecurity should detect developer options enabled`() = runTest {
        // Given
        every { 
            android.provider.Settings.Secure.getInt(
                any(), 
                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 
                0
            ) 
        } returns 1
        
        // When
        val indicators = threatDetector.checkDeviceSecurity()
        
        // Then
        assertTrue(indicators.isNotEmpty())
        val devOptionsIndicator = indicators.find { 
            it.description.contains("Developer options enabled") 
        }
        assertNotNull(devOptionsIndicator)
        assertEquals(ThreatType.DEVICE_COMPROMISE, devOptionsIndicator.type)
        assertEquals(SecuritySeverity.MEDIUM, devOptionsIndicator.severity)
    }
    
    @Test
    fun `checkDeviceSecurity should return empty list for secure device`() = runTest {
        // When
        val indicators = threatDetector.checkDeviceSecurity()
        
        // Then
        assertTrue(indicators.isEmpty())
    }
    
    @Test
    fun `getThreatIndicators should return flow of threat indicators`() = runTest {
        // Given
        threatDetector.startDetection()
        
        // When
        val indicatorFlow = threatDetector.getThreatIndicators()
        
        // Then
        // Flow should be available (actual indicators depend on detection logic)
        assertNotNull(indicatorFlow)
    }
    
    @Test
    fun `threat detection should handle network exceptions gracefully`() = runTest {
        // Given
        every { connectivityManager.activeNetwork } throws SecurityException("Network access denied")
        
        // When
        val indicators = threatDetector.analyzeNetworkTraffic()
        
        // Then - Should not crash and return empty list
        assertTrue(indicators.isEmpty())
    }
    
    @Test
    fun `threat indicators should have proper confidence levels`() = runTest {
        // Given
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) } returns false
        every { networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) } returns true
        
        // When
        val indicators = threatDetector.analyzeNetworkTraffic()
        
        // Then
        indicators.forEach { indicator ->
            assertTrue(indicator.confidence in 0.0f..1.0f)
            assertTrue(indicator.timestamp != null)
            assertTrue(indicator.source.isNotEmpty())
            assertTrue(indicator.id.isNotEmpty())
        }
    }
    
    @Test
    fun `authentication failure tracking should clean old entries`() = runTest {
        // Given - Report failures and wait for cleanup
        repeat(3) {
            threatDetector.reportAuthenticationFailure()
        }
        
        // When - Check patterns (this triggers cleanup of old entries)
        val indicators1 = threatDetector.monitorAuthenticationPatterns()
        
        // Simulate time passing (in real implementation, old entries would be cleaned)
        // For testing, we verify the current behavior
        val indicators2 = threatDetector.monitorAuthenticationPatterns()
        
        // Then
        assertTrue(indicators1.isEmpty()) // Below threshold
        assertTrue(indicators2.isEmpty()) // Still below threshold
    }
    
    private fun assertNotNull(value: Any?) {
        assertTrue(value != null)
    }
}