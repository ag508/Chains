package com.chain.messaging.core.crypto

import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.state.SessionRecord

/**
 * Interface for storing and retrieving session records
 */
interface SessionStorage {
    /**
     * Load a session record for the given address
     */
    fun loadSession(address: SignalProtocolAddress): SessionRecord
    
    /**
     * Get all sub-device sessions for a given name
     */
    fun getSubDeviceSessions(name: String): List<Int>
    
    /**
     * Store a session record for the given address
     */
    fun storeSession(address: SignalProtocolAddress, record: SessionRecord)
    
    /**
     * Check if a session exists for the given address
     */
    fun containsSession(address: SignalProtocolAddress): Boolean
    
    /**
     * Delete a session for the given address
     */
    fun deleteSession(address: SignalProtocolAddress)
    
    /**
     * Delete all sessions for the given name
     */
    fun deleteAllSessions(name: String)
}