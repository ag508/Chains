package com.chain.messaging.core.crypto

import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.IdentityKeyPair
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.state.IdentityKeyStore
import org.signal.libsignal.protocol.state.PreKeyStore
import org.signal.libsignal.protocol.state.SessionStore
import org.signal.libsignal.protocol.state.SignalProtocolStore
import org.signal.libsignal.protocol.state.SignedPreKeyStore
import org.signal.libsignal.protocol.state.KyberPreKeyStore
import org.signal.libsignal.protocol.state.KyberPreKeyRecord
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adapter that implements Signal Protocol's SignalProtocolStore interface
 * by delegating to our internal store implementations
 *
 * Note: SenderKeyStore is not implemented as it's not available in libsignal-android 0.42.0
 */
@Singleton
class SignalProtocolStoreAdapter @Inject constructor(
    private val chainIdentityStore: IdentityStorageImpl,
    private val chainSessionStore: SessionStorageImpl,
    private val chainSenderKeyStore: SenderKeyStoreImpl,
    private val keyManager: KeyManager
) : SignalProtocolStore {

    // SignalProtocolStore implementation
    override fun getIdentityKeyPair(): IdentityKeyPair {
        return chainIdentityStore.getIdentityKeyPair()
    }

    override fun getLocalRegistrationId(): Int {
        return chainIdentityStore.getLocalRegistrationId()
    }

    override fun saveIdentity(address: SignalProtocolAddress, identityKey: IdentityKey): Boolean {
        return chainIdentityStore.saveIdentity(address, identityKey)
    }

    override fun isTrustedIdentity(
        address: SignalProtocolAddress,
        identityKey: IdentityKey,
        direction: IdentityKeyStore.Direction
    ): Boolean {
        return chainIdentityStore.isTrustedIdentity(address, identityKey, direction)
    }

    override fun getIdentity(address: SignalProtocolAddress): IdentityKey? {
        return chainIdentityStore.getIdentity(address)
    }

    override fun loadSession(address: SignalProtocolAddress): org.signal.libsignal.protocol.state.SessionRecord {
        return chainSessionStore.loadSession(address)
    }

    override fun getSubDeviceSessions(name: String): MutableList<Int> {
        return chainSessionStore.getSubDeviceSessions(name).toMutableList()
    }

    override fun storeSession(
        address: SignalProtocolAddress,
        record: org.signal.libsignal.protocol.state.SessionRecord
    ) {
        chainSessionStore.storeSession(address, record)
    }

    override fun containsSession(address: SignalProtocolAddress): Boolean {
        return chainSessionStore.containsSession(address)
    }

    override fun deleteSession(address: SignalProtocolAddress) {
        chainSessionStore.deleteSession(address)
    }

    override fun deleteAllSessions(name: String) {
        chainSessionStore.deleteAllSessions(name)
    }

    // Additional method that might be required by SessionStore interface
    fun loadExistingSessions(addresses: List<SignalProtocolAddress>): List<org.signal.libsignal.protocol.state.SessionRecord> {
        return addresses.map { address -> loadSession(address) }
    }

    override fun loadPreKey(preKeyId: Int): org.signal.libsignal.protocol.state.PreKeyRecord? {
        return keyManager.loadPreKey(preKeyId)
    }

    override fun storePreKey(preKeyId: Int, record: org.signal.libsignal.protocol.state.PreKeyRecord) {
        keyManager.storePreKey(preKeyId, record)
    }

    override fun containsPreKey(preKeyId: Int): Boolean {
        return keyManager.containsPreKey(preKeyId)
    }

    override fun removePreKey(preKeyId: Int) {
        keyManager.removePreKey(preKeyId)
    }

    override fun loadSignedPreKey(signedPreKeyId: Int): org.signal.libsignal.protocol.state.SignedPreKeyRecord? {
        return keyManager.loadSignedPreKey(signedPreKeyId)
    }

    override fun loadSignedPreKeys(): MutableList<org.signal.libsignal.protocol.state.SignedPreKeyRecord> {
        return keyManager.loadSignedPreKeys()
    }

    override fun storeSignedPreKey(
        signedPreKeyId: Int,
        record: org.signal.libsignal.protocol.state.SignedPreKeyRecord
    ) {
        keyManager.storeSignedPreKey(signedPreKeyId, record)
    }

    override fun containsSignedPreKey(signedPreKeyId: Int): Boolean {
        return keyManager.containsSignedPreKey(signedPreKeyId)
    }

    override fun removeSignedPreKey(signedPreKeyId: Int) {
        keyManager.removeSignedPreKey(signedPreKeyId)
    }

    // KyberPreKeyStore implementation
    override fun loadKyberPreKey(kyberPreKeyId: Int): KyberPreKeyRecord {
        // For now, generate a dummy Kyber prekey
        // TODO: Implement proper Kyber prekey storage when needed
        throw UnsupportedOperationException("Kyber prekeys are not yet supported")
    }

    override fun storeKyberPreKey(kyberPreKeyId: Int, record: KyberPreKeyRecord) {
        // TODO: Implement proper Kyber prekey storage when needed
        throw UnsupportedOperationException("Kyber prekeys are not yet supported")
    }

    override fun containsKyberPreKey(kyberPreKeyId: Int): Boolean {
        // TODO: Implement proper Kyber prekey storage when needed
        return false
    }

    override fun markKyberPreKeyUsed(kyberPreKeyId: Int) {
        // TODO: Implement proper Kyber prekey storage when needed
    }

    /**
     * Get access to the Chain-specific identity store
     */
    fun getChainIdentityStore(): IdentityStorageImpl = chainIdentityStore

    /**
     * Get access to the Chain-specific session store
     */
    fun getChainSessionStore(): SessionStorageImpl = chainSessionStore

    /**
     * Get access to the Chain-specific sender key store
     */
    fun getChainSenderKeyStore(): SenderKeyStoreImpl = chainSenderKeyStore

    /**
     * Get access to the Chain-specific key manager
     */
    fun getChainKeyManager(): KeyManager = keyManager
}