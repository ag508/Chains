package com.chain.messaging.core.webrtc

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class IceServerProviderTest {
    
    private lateinit var iceServerProvider: IceServerProvider
    
    @Before
    fun setup() {
        iceServerProvider = IceServerProvider()
    }
    
    @Test
    fun `getDefaultStunServers should return Google STUN servers`() {
        // When
        val stunServers = iceServerProvider.getDefaultStunServers()
        
        // Then
        assertFalse(stunServers.isEmpty())
        assertTrue(stunServers.all { it.url.startsWith("stun:") })
        assertTrue(stunServers.any { it.url.contains("stun.l.google.com") })
    }
    
    @Test
    fun `getTurnServers should return TURN server configuration`() {
        // When
        val turnServers = iceServerProvider.getTurnServers()
        
        // Then
        assertFalse(turnServers.isEmpty())
        assertTrue(turnServers.all { 
            it.url.startsWith("turn:") || it.url.startsWith("turns:") 
        })
        assertTrue(turnServers.all { 
            !it.username.isNullOrBlank() && !it.credential.isNullOrBlank() 
        })
    }
    
    @Test
    fun `getAllIceServers should return both STUN and TURN servers`() {
        // When
        val allServers = iceServerProvider.getAllIceServers()
        val stunServers = iceServerProvider.getDefaultStunServers()
        val turnServers = iceServerProvider.getTurnServers()
        
        // Then
        assertEquals(stunServers.size + turnServers.size, allServers.size)
        assertTrue(allServers.containsAll(stunServers))
        assertTrue(allServers.containsAll(turnServers))
    }
    
    @Test
    fun `getIceServersForNetworkCondition should return STUN only for good network`() {
        // When
        val servers = iceServerProvider.getIceServersForNetworkCondition(isNetworkRestricted = false)
        
        // Then
        assertTrue(servers.all { it.url.startsWith("stun:") })
        assertEquals(iceServerProvider.getDefaultStunServers().size, servers.size)
    }
    
    @Test
    fun `getIceServersForNetworkCondition should return all servers for restricted network`() {
        // When
        val servers = iceServerProvider.getIceServersForNetworkCondition(isNetworkRestricted = true)
        
        // Then
        assertEquals(iceServerProvider.getAllIceServers().size, servers.size)
        assertTrue(servers.any { it.url.startsWith("stun:") })
        assertTrue(servers.any { it.url.startsWith("turn:") || it.url.startsWith("turns:") })
    }
    
    @Test
    fun `validateIceServer should validate STUN server correctly`() {
        // Given
        val validStunServer = IceServer("stun:stun.l.google.com:19302")
        val invalidStunServer = IceServer("")
        
        // When & Then
        assertTrue(iceServerProvider.validateIceServer(validStunServer))
        assertFalse(iceServerProvider.validateIceServer(invalidStunServer))
    }
    
    @Test
    fun `validateIceServer should validate TURN server correctly`() {
        // Given
        val validTurnServer = IceServer(
            url = "turn:turnserver.example.com:3478",
            username = "user",
            credential = "pass"
        )
        val invalidTurnServer = IceServer(
            url = "turn:turnserver.example.com:3478",
            username = null,
            credential = null
        )
        
        // When & Then
        assertTrue(iceServerProvider.validateIceServer(validTurnServer))
        assertFalse(iceServerProvider.validateIceServer(invalidTurnServer))
    }
    
    @Test
    fun `validateIceServer should validate TURNS server correctly`() {
        // Given
        val validTurnsServer = IceServer(
            url = "turns:turnserver.example.com:5349",
            username = "user",
            credential = "pass"
        )
        val invalidTurnsServer = IceServer(
            url = "turns:turnserver.example.com:5349",
            username = "",
            credential = ""
        )
        
        // When & Then
        assertTrue(iceServerProvider.validateIceServer(validTurnsServer))
        assertFalse(iceServerProvider.validateIceServer(invalidTurnsServer))
    }
    
    @Test
    fun `validateIceServer should reject invalid URL schemes`() {
        // Given
        val invalidServer = IceServer("http://example.com")
        
        // When & Then
        assertFalse(iceServerProvider.validateIceServer(invalidServer))
    }
}