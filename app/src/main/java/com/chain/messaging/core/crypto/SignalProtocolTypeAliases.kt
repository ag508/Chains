package com.chain.messaging.core.crypto

/**
 * Type aliases to resolve naming conflicts between internal Chain classes
 * and Signal Protocol library classes
 *
 * Note: SenderKey functionality is not available in libsignal-android 0.42.0
 * Group messaging will need alternative implementation
 */

// Import Signal Protocol types
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.SessionBuilder
import org.signal.libsignal.protocol.SessionCipher
import org.signal.libsignal.protocol.state.PreKeyBundle
import org.signal.libsignal.protocol.message.CiphertextMessage
import org.signal.libsignal.protocol.message.PreKeySignalMessage
import org.signal.libsignal.protocol.message.SignalMessage as SignalMessageType
import org.signal.libsignal.protocol.state.IdentityKeyStore

// Signal Protocol Library Types (prefixed with Signal)
typealias SignalProtocolAddress = SignalProtocolAddress
typealias SignalSessionBuilder = SessionBuilder
typealias SignalSessionCipher = SessionCipher
typealias SignalPreKeyBundle = PreKeyBundle
typealias SignalCiphertextMessage = CiphertextMessage
typealias SignalPreKeySignalMessage = PreKeySignalMessage
typealias SignalMessage = SignalMessageType
typealias SignalIdentityDirection = IdentityKeyStore.Direction

// Chain Internal Types (prefixed with Chain)
typealias ChainSenderKeyStore = com.chain.messaging.core.crypto.SenderKeyStore
typealias ChainEncryptedGroupMessage = com.chain.messaging.core.crypto.EncryptedGroupMessage

// Stub types for SenderKey functionality (not supported in this version of libsignal)
// These are placeholders to maintain compilation compatibility
data class SignalSenderKeyName(val groupId: String, val sender: SignalProtocolAddress)

data class SignalSenderKeyRecord(val data: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SignalSenderKeyRecord
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

data class SignalEncryptedGroupMessage(val data: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SignalEncryptedGroupMessage
        return data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}