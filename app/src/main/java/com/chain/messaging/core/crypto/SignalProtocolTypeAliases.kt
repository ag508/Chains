package com.chain.messaging.core.crypto

/**
 * Type aliases to resolve naming conflicts between internal Chain classes 
 * and Signal Protocol library classes
 */

// Import Signal Protocol types first
import org.signal.libsignal.protocol.groups.SenderKeyName
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord
import org.signal.libsignal.protocol.groups.state.SenderKeyStore
import org.signal.libsignal.protocol.message.SenderKeyMessage
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.groups.GroupSessionBuilder
import org.signal.libsignal.protocol.groups.GroupCipher
import org.signal.libsignal.protocol.SessionBuilder
import org.signal.libsignal.protocol.SessionCipher
import org.signal.libsignal.protocol.state.PreKeyBundle
import org.signal.libsignal.protocol.message.CiphertextMessage
import org.signal.libsignal.protocol.message.PreKeySignalMessage
import org.signal.libsignal.protocol.message.SignalMessage
import org.signal.libsignal.protocol.state.IdentityKeyStore

// Signal Protocol Library Types (prefixed with Signal)
typealias SignalSenderKeyName = SenderKeyName
typealias SignalSenderKeyRecord = SenderKeyRecord
typealias SignalSenderKeyStore = SenderKeyStore
typealias SignalEncryptedGroupMessage = SenderKeyMessage
typealias SignalProtocolAddress = SignalProtocolAddress
typealias SignalGroupSessionBuilder = GroupSessionBuilder
typealias SignalGroupCipher = GroupCipher
typealias SignalSessionBuilder = SessionBuilder
typealias SignalSessionCipher = SessionCipher
typealias SignalPreKeyBundle = PreKeyBundle
typealias SignalCiphertextMessage = CiphertextMessage
typealias SignalPreKeySignalMessage = PreKeySignalMessage
typealias SignalMessage = SignalMessage
typealias SignalIdentityDirection = IdentityKeyStore.Direction

// Chain Internal Types (prefixed with Chain)
typealias ChainSenderKeyStore = com.chain.messaging.core.crypto.SenderKeyStore
typealias ChainEncryptedGroupMessage = com.chain.messaging.core.crypto.EncryptedGroupMessage