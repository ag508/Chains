package com.chain.messaging.core.webrtc

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class BandwidthMonitorTest {
    
    private lateinit var bandwidthMonitor: BandwidthMonitorImpl
    
    @Before
    fun setup() {
        bandwidthMonitor = BandwidthMonitorImpl()
    }
    
    @Test
    fun `startMonitoring should create active session`() = runTest {
        // Given
        val callId = "call_123"
        
        // When
        bandwidthMonitor.startMonitoring(callId)
        
        // Then
        val bandwidthInfo = bandwidthMonitor.getCurrentBandwidth(callId)
        assertEquals(callId, bandwidthInfo.callId)
        assertTrue(bandwidthInfo.availableBandwidth > 0)
    }
    
    @Test
    fun `stopMonitoring should remove active session`() = runTest {
        // Given
        val callId = "call_123"
        bandwidthMonitor.startMonitoring(callId)
        
        // When
        bandwidthMonitor.stopMonitoring(callId)
        
        // Then
        val bandwidthInfo = bandwidthMonitor.getCurrentBandwidth(callId)
        assertEquals(0, bandwidthInfo.availableBandwidth)
        assertEquals(0, bandwidthInfo.usedBandwidth)
    }
    
    @Test
    fun `getCurrentBandwidth should return zero for inactive session`() = runTest {
        // Given
        val callId = "inactive_call"
        
        // When
        val bandwidthInfo = bandwidthMonitor.getCurrentBandwidth(callId)
        
        // Then
        assertEquals(callId, bandwidthInfo.callId)
        assertEquals(0, bandwidthInfo.availableBandwidth)
        assertEquals(0, bandwidthInfo.usedBandwidth)
    }
    
    @Test
    fun `estimateAvailableBandwidth should return positive value`() = runTest {
        // When
        val bandwidth = bandwidthMonitor.estimateAvailableBandwidth()
        
        // Then
        assertTrue(bandwidth > 0)
    }
    
    @Test
    fun `observeBandwidthChanges should return flow`() = runTest {
        // When
        val bandwidthFlow = bandwidthMonitor.observeBandwidthChanges()
        
        // Then
        assertNotNull(bandwidthFlow)
    }
    
    @Test
    fun `multiple calls can be monitored simultaneously`() = runTest {
        // Given
        val callId1 = "call_1"
        val callId2 = "call_2"
        
        // When
        bandwidthMonitor.startMonitoring(callId1)
        bandwidthMonitor.startMonitoring(callId2)
        
        // Then
        val bandwidth1 = bandwidthMonitor.getCurrentBandwidth(callId1)
        val bandwidth2 = bandwidthMonitor.getCurrentBandwidth(callId2)
        
        assertEquals(callId1, bandwidth1.callId)
        assertEquals(callId2, bandwidth2.callId)
        assertTrue(bandwidth1.availableBandwidth > 0)
        assertTrue(bandwidth2.availableBandwidth > 0)
    }
}