package com.chain.messaging.core.group

import com.chain.messaging.core.crypto.EncryptedGroupMessage
import org.signal.libsignal.protocol.groups.SenderKeyName
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing group encryption, key distribution, and forward secrecy.
 * Handles Signal Protocol sender keys for secure group messaging.
 */
interface GroupEncryptionManager {
    
    /**
     * Initialize group encryption for a new group.
     * Creates initial sender keys for all members.
     */
    suspend fun initializeGroupEncryption(
        groupId: String,
        memberIds: List<String>,
        creatorId: String
    ): Result<GroupEncryptionInfo>
    
    /**
     * Add new members to group encryption.
     * Distributes current sender keys to new members and rotates keys for forward secrecy.
     */
    suspend fun addMembersToGroupEncryption(
        groupId: String,
        newMemberIds: List<String>,
        existingMemberIds: List<String>
    ): Result<Unit>
    
    /**
     * Remove members from group encryption.
     * Rotates all sender keys to ensure removed members cannot decrypt future messages.
     */
    suspend fun removeMembersFromGroupEncryption(
        groupId: String,
        removedMemberIds: List<String>,
        remainingMemberIds: List<String>
    ): Result<Unit>
    
    /**
     * Encrypt a message for the group using sender keys.
     */
    suspend fun encryptGroupMessage(
        groupId: String,
        senderId: String,
        deviceId: Int,
        message: ByteArray
    ): Result<EncryptedGroupMessage>
    
    /**
     * Decrypt a group message using sender keys.
     */
    suspend fun decryptGroupMessage(
        groupId: String,
        senderId: String,
        deviceId: Int,
        encryptedMessage: EncryptedGroupMessage
    ): Result<ByteArray>
    
    /**
     * Rotate sender keys for forward secrecy.
     * Should be called periodically or when security is compromised.
     */
    suspend fun rotateSenderKeys(
        groupId: String,
        memberIds: List<String>
    ): Result<Unit>
    
    /**
     * Get sender key distribution message for a specific member.
     * Used to share sender keys with group members.
     */
    suspend fun getSenderKeyDistribution(
        groupId: String,
        senderId: String,
        deviceId: Int,
        recipientId: String
    ): Result<SenderKeyDistributionMessage>
    
    /**
     * Process received sender key distribution message.
     * Updates local sender key store with received keys.
     */
    suspend fun processSenderKeyDistribution(
        groupId: String,
        senderId: String,
        deviceId: Int,
        distributionMessage: SenderKeyDistributionMessage
    ): Result<Unit>
    
    /**
     * Check if group encryption is properly initialized.
     */
    suspend fun isGroupEncryptionInitialized(groupId: String): Boolean
    
    /**
     * Get encryption info for a group.
     */
    suspend fun getGroupEncryptionInfo(groupId: String): Result<GroupEncryptionInfo?>
    
    /**
     * Observe encryption status changes for a group.
     */
    fun observeGroupEncryptionStatus(groupId: String): Flow<GroupEncryptionStatus>
    
    /**
     * Clean up encryption data for a group (when group is deleted).
     */
    suspend fun cleanupGroupEncryption(groupId: String): Result<Unit>
    
    /**
     * Verify sender key integrity for a group member.
     */
    suspend fun verifySenderKeyIntegrity(
        groupId: String,
        senderId: String,
        deviceId: Int
    ): Result<Boolean>
}

/**
 * Information about group encryption setup
 */
data class GroupEncryptionInfo(
    val groupId: String,
    val memberCount: Int,
    val keyRotationCount: Int,
    val lastKeyRotation: Long,
    val encryptionVersion: Int,
    val isInitialized: Boolean
)

/**
 * Status of group encryption
 */
data class GroupEncryptionStatus(
    val groupId: String,
    val isHealthy: Boolean,
    val membersSynced: Int,
    val membersTotal: Int,
    val lastActivity: Long,
    val issues: List<EncryptionIssue> = emptyList()
)

/**
 * Encryption issues that may occur
 */
sealed class EncryptionIssue {
    data class MissingSenderKey(val senderId: String, val deviceId: Int) : EncryptionIssue()
    data class KeyRotationNeeded(val reason: String) : EncryptionIssue()
    data class MemberOutOfSync(val memberId: String) : EncryptionIssue()
    data class CorruptedKey(val senderId: String, val deviceId: Int) : EncryptionIssue()
}

/**
 * Sender key distribution message for sharing keys between group members
 */
data class SenderKeyDistributionMessage(
    val groupId: String,
    val senderId: String,
    val deviceId: Int,
    val distributionData: ByteArray,
    val timestamp: Long,
    val version: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SenderKeyDistributionMessage

        if (groupId != other.groupId) return false
        if (senderId != other.senderId) return false
        if (deviceId != other.deviceId) return false
        if (!distributionData.contentEquals(other.distributionData)) return false
        if (timestamp != other.timestamp) return false
        if (version != other.version) return false

        return true
    }

    override fun hashCode(): Int {
        var result = groupId.hashCode()
        result = 31 * result + senderId.hashCode()
        result = 31 * result + deviceId
        result = 31 * result + distributionData.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + version
        return result
    }
}