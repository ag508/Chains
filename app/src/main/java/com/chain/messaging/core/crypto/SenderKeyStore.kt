package com.chain.messaging.core.crypto

/**
 * Interface for sender key storage operations (group messaging)
 * This interface provides sender key storage functionality
 *
 * Note: Uses stub types since libsignal-android 0.42.0 doesn't support sender keys
 */
interface SenderKeyStore {
    fun storeSenderKey(senderKeyName: SignalSenderKeyName, record: SignalSenderKeyRecord)
    fun loadSenderKey(senderKeyName: SignalSenderKeyName): SignalSenderKeyRecord?
}