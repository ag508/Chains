package com.chain.messaging.core.crypto

// Import Signal Protocol types directly
import org.signal.libsignal.protocol.groups.SenderKeyName
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord

/**
 * Interface for sender key storage operations (group messaging)
 * This interface matches the Signal Protocol SenderKeyStore interface
 * but uses our internal naming conventions
 */
interface SenderKeyStore {
    fun storeSenderKey(senderKeyName: SenderKeyName, record: SenderKeyRecord)
    fun loadSenderKey(senderKeyName: SenderKeyName): SenderKeyRecord?
}