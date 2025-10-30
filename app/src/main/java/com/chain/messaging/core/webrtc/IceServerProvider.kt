package com.chain.messaging.core.webrtc

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provider for ICE servers (STUN/TURN) configuration
 * Handles NAT traversal server configuration and management
 */
@Singleton
class IceServerProvider @Inject constructor() {
    
    /**
     * Get default STUN servers for basic NAT traversal
     */
    fun getDefaultStunServers(): List<IceServer> {
        return listOf(
            IceServer("stun:stun.l.google.com:19302"),
            IceServer("stun:stun1.l.google.com:19302"),
            IceServer("stun:stun2.l.google.com:19302"),
            IceServer("stun:stun3.l.google.com:19302"),
            IceServer("stun:stun4.l.google.com:19302")
        )
    }
    
    /**
     * Get TURN servers for relay when direct connection fails
     * In production, these would be configured with actual TURN server credentials
     */
    fun getTurnServers(): List<IceServer> {
        return listOf(
            // Example TURN server configuration
            // In production, replace with actual TURN server credentials
            IceServer(
                url = "turn:turnserver.example.com:3478",
                username = "username",
                credential = "password"
            ),
            IceServer(
                url = "turns:turnserver.example.com:5349",
                username = "username", 
                credential = "password"
            )
        )
    }
    
    /**
     * Get all available ICE servers (STUN + TURN)
     */
    fun getAllIceServers(): List<IceServer> {
        return getDefaultStunServers() + getTurnServers()
    }
    
    /**
     * Get ICE servers based on network conditions
     * Returns STUN servers for good connections, adds TURN for poor connections
     */
    fun getIceServersForNetworkCondition(isNetworkRestricted: Boolean): List<IceServer> {
        return if (isNetworkRestricted) {
            getAllIceServers()
        } else {
            getDefaultStunServers()
        }
    }
    
    /**
     * Validate ICE server configuration
     */
    fun validateIceServer(iceServer: IceServer): Boolean {
        return when {
            iceServer.url.isBlank() -> false
            iceServer.url.startsWith("stun:") -> true
            iceServer.url.startsWith("turn:") || iceServer.url.startsWith("turns:") -> {
                !iceServer.username.isNullOrBlank() && !iceServer.credential.isNullOrBlank()
            }
            else -> false
        }
    }
}