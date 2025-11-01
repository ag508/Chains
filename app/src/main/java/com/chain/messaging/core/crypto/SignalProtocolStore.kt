package com.chain.messaging.core.crypto

import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord
import org.signal.libsignal.protocol.state.IdentityKeyStore
import org.signal.libsignal.protocol.state.PreKeyRecord
import org.signal.libsignal.protocol.state.PreKeyStore
import org.signal.libsignal.protocol.state.SessionRecord
import org.signal.libsignal.protocol.state.SessionStore
import org.signal.libsignal.protocol.state.SignedPreKeyRecord
import org.signal.libsignal.protocol.state.SignedPreKeyStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of Signal Protocol stores for managing cryptographic state
 */
@Singleton
class SignalProtocolStore @Inject constructor(
    private val keyManager: KeyManager,
    private val sessionStorage: SessionStorage,
    private val identityStorage: IdentityStorage
) : IdentityKeyStore, PreKeyStore, SignedPreKeyStore, SessionStore {

    // IdentityKeyStore implementation
    override fun getIdentityKeyPair(): IdentityKeyPair {
        return keyManager.getIdentityKeyPair()
    }

    override fun getLocalRegistrationId(): Int {
        return keyManager.getRegistrationId()
    }

    override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean {
        return identityStorage.saveIdentity(address, identityKey)
    }

    override fun isTrustedIdentity(
        address: SignalProtocolAddress,
        identityKey: IdentityKey,
        direction: IdentityKeyStore.Direction
    ): Boolean {
        return identityStorage.isTrustedIdentity(address, identityKey, direction)
    }

    override fun getIdentity(address: SignalProtocolAddress): IdentityKey? {
        return identityStorage.getIdentity(address)
    }

    // PreKeyStore implementation
    override fun loadPreKey(preKeyId: Int): PreKeyRecord {
        return keyManager.loadPreKey(preKeyId)
            ?: throw IllegalStateException("PreKey with ID $preKeyId not found")
    }

    override fun storePreKey(preKeyId: Int, record: PreKeyRecord) {
        // PreKeys are stored by KeyManager during generation
        // This method is called by Signal Protocol during key exchange
    }

    override fun containsPreKey(preKeyId: Int): Boolean {
        return keyManager.loadPreKey(preKeyId) != null
    }

    override fun removePreKey(preKeyId: Int) {
        keyManager.removePreKey(preKeyId)
    }

    // SignedPreKeyStore implementation
    override fun loadSignedPreKey(signedPreKeyId: Int): SignedPreKeyRecord {
        return keyManager.loadSignedPreKey(signedPreKeyId)
            ?: throw IllegalStateException("SignedPreKey with ID $signedPreKeyId not found")
    }

    override fun loadSignedPreKeys(): List<SignedPreKeyRecord> {
        // Return all signed pre-keys for key rotation and management
        return keyManager.loadAllSignedPreKeys()
    }

    override fun storeSignedPreKey(signedPreKeyId: Int, record: SignedPreKeyRecord) {
        // SignedPreKeys are stored by KeyManager during generation
        // This method is called by Signal Protocol during key exchange
    }

    override fun containsSignedPreKey(signedPreKeyId: Int): Boolean {
        return keyManager.loadSignedPreKey(signedPreKeyId) != null
    }

    override fun removeSignedPreKey(signedPreKeyId: Int) {
        // Remove old signed pre-keys during rotation
        keyManager.removeSignedPreKey(signedPreKeyId)
    }

    // SessionStore implementation
    override fun loadSession(address: SignalProtocolAddress): SessionRecord {
        return sessionStorage.loadSession(address)
    }

    override fun getSubDeviceSessions(name: String): List<Int> {
        return sessionStorage.getSubDeviceSessions(name)
    }

    override fun storeSession(address: SignalProtocolAddress, record: SessionRecord) {
        sessionStorage.storeSession(address, record)
    }

    override fun containsSession(address: SignalProtocolAddress): Boolean {
        return sessionStorage.containsSession(address)
    }

    override fun deleteSession(address: SignalProtocolAddress) {
        sessionStorage.deleteSession(address)
    }

    override fun deleteAllSessions(name: String) {
        sessionStorage.deleteAllSessions(name)
    }

    // Additional method that might be required by newer Signal Protocol versions
    override fun loadExistingSessions(addresses: MutableList<SignalProtocolAddress>): MutableList<SessionRecord> {
        val sessions = mutableListOf<SessionRecord>()
        addresses.forEach { address ->
            if (containsSession(address)) {
                sessions.add(loadSession(address))
            }
        }
        return sessions
    }
}

