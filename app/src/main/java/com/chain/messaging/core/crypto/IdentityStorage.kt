package com.chain.messaging.core.crypto

import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.state.IdentityKeyStore

/**
 * Interface for storing and retrieving identity keys
 */
interface IdentityStorage {
    /**
     * Get the local identity key pair
     */
    fun getIdentityKeyPair(): IdentityKeyPair
    
    /**
     * Get the local registration ID
     */
    fun getLocalRegistrationId(): Int
    
    /**
     * Save an identity key for a remote address
     */
    fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean
    
    /**
     * Check if an identity key is trusted
     */
    fun isTrustedIdentity(address: SignalProtocolAddress, identityKey: IdentityKey, direction: IdentityKeyStore.Direction): Boolean
    
    /**
     * Get the identity key for a remote address
     */
    fun getIdentity(address: SignalProtocolAddress): IdentityKey?
}