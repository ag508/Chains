package com.chain.messaging.core.crypto

/**
 * Represents an encrypted group message using Signal Protocol sender keys
 */
data class EncryptedGroupMessage(
    val groupId: String,
    val senderId: String,
    val deviceId: Int,
    val ciphertext: ByteArray,
    val timestamp: Long,
    val messageVersion: Int = 1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedGroupMessage

        if (groupId != other.groupId) return false
        if (senderId != other.senderId) return false
        if (deviceId != other.deviceId) return false
        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (timestamp != other.timestamp) return false
        if (messageVersion != other.messageVersion) return false

        return true
    }

    override fun hashCode(): Int {
        var result = groupId.hashCode()
        result = 31 * result + senderId.hashCode()
        result = 31 * result + deviceId
        result = 31 * result + ciphertext.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + messageVersion
        return result
    }
}