package com.chain.messaging.core.group

import android.util.Log
import com.chain.messaging.core.crypto.EncryptedGroupMessage
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.crypto.SignalProtocolStoreAdapter
// Use type aliases for Signal Protocol types
import com.chain.messaging.core.crypto.SignalSenderKeyName
import com.chain.messaging.core.crypto.SignalSenderKeyRecord
import com.chain.messaging.core.crypto.SignalGroupSessionBuilder
import com.chain.messaging.core.crypto.SignalProtocolAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.signal.libsignal.protocol.util.KeyHelper
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of GroupEncryptionManager that handles Signal Protocol sender keys
 * for secure group messaging with forward secrecy and key rotation.
 */
@Singleton
class GroupEncryptionManagerImpl @Inject constructor(
    private val signalEncryptionService: SignalEncryptionService,
    private val signalProtocolStore: SignalProtocolStoreAdapter
) : GroupEncryptionManager {

    companion object {
        private const val TAG = "GroupEncryptionManager"
        private const val ENCRYPTION_VERSION = 1
        private const val KEY_ROTATION_THRESHOLD_HOURS = 24
        private const val MAX_KEY_ROTATION_COUNT = 1000
    }

    // Thread-safe storage for group encryption info
    private val groupEncryptionInfo = ConcurrentHashMap<String, GroupEncryptionInfo>()
    private val groupEncryptionStatus = ConcurrentHashMap<String, MutableStateFlow<GroupEncryptionStatus>>()
    private val encryptionMutex = ConcurrentHashMap<String, Mutex>()
    private val secureRandom = SecureRandom()

    override suspend fun initializeGroupEncryption(
        groupId: String,
        memberIds: List<String>,
        creatorId: String
    ): Result<GroupEncryptionInfo> {
        return try {
            val mutex = encryptionMutex.getOrPut(groupId) { Mutex() }
            mutex.withLock {
                Log.d(TAG, "Initializing group encryption for group $groupId with ${memberIds.size} members")

                // Check if already initialized
                if (isGroupEncryptionInitialized(groupId)) {
                    Log.w(TAG, "Group encryption already initialized for group $groupId")
                    return@withLock Result.success(groupEncryptionInfo[groupId]!!)
                }

                // Create sender keys for all members
                val createdKeys = mutableListOf<SignalSenderKeyName>()
                
                for (memberId in memberIds) {
                    val deviceId = 1 // For simplicity, using device ID 1
                    val senderKeyName = SignalSenderKeyName(
                        groupId, 
                        SignalProtocolAddress(memberId, deviceId)
                    )
                    
                    try {
                        val groupSessionBuilder = SignalGroupSessionBuilder(signalProtocolStore)
                        val senderKeyRecord = groupSessionBuilder.create(senderKeyName)
                        signalProtocolStore.storeSenderKey(senderKeyName, senderKeyRecord)
                        createdKeys.add(senderKeyName)
                        
                        Log.d(TAG, "Created sender key for member $memberId in group $groupId")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to create sender key for member $memberId", e)
                        // Clean up any created keys on failure
                        createdKeys.forEach { keyName: SignalSenderKeyName ->
                            try {
                                signalProtocolStore.getChainSenderKeyStore().removeSenderKey(keyName)
                            } catch (cleanupException: Exception) {
                                Log.w(TAG, "Failed to cleanup sender key during rollback", cleanupException)
                            }
                        }
                        return@withLock Result.failure(Exception("Failed to initialize group encryption", e))
                    }
                }

                // Create group encryption info
                val encryptionInfo = GroupEncryptionInfo(
                    groupId = groupId,
                    memberCount = memberIds.size,
                    keyRotationCount = 0,
                    lastKeyRotation = System.currentTimeMillis(),
                    encryptionVersion = ENCRYPTION_VERSION,
                    isInitialized = true
                )

                // Store encryption info
                groupEncryptionInfo[groupId] = encryptionInfo

                // Initialize encryption status
                val status = GroupEncryptionStatus(
                    groupId = groupId,
                    isHealthy = true,
                    membersSynced = memberIds.size,
                    membersTotal = memberIds.size,
                    lastActivity = System.currentTimeMillis(),
                    issues = emptyList()
                )
                groupEncryptionStatus[groupId] = MutableStateFlow(status)

                Log.i(TAG, "Group encryption initialized successfully for group $groupId")
                Result.success(encryptionInfo)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize group encryption for group $groupId", e)
            Result.failure(e)
        }
    }

    override suspend fun addMembersToGroupEncryption(
        groupId: String,
        newMemberIds: List<String>,
        existingMemberIds: List<String>
    ): Result<Unit> {
        return try {
            val mutex = encryptionMutex.getOrPut(groupId) { Mutex() }
            mutex.withLock {
                Log.d(TAG, "Adding ${newMemberIds.size} members to group encryption for group $groupId")

                if (!isGroupEncryptionInitialized(groupId)) {
                    return@withLock Result.failure(Exception("Group encryption not initialized"))
                }

                // Create sender keys for new members
                for (memberId in newMemberIds) {
                    val deviceId = 1
                    val senderKeyName = SignalSenderKeyName(
                        groupId, 
                        SignalProtocolAddress(memberId, deviceId)
                    )
                    
                    val groupSessionBuilder = SignalGroupSessionBuilder(signalProtocolStore)
                    val senderKeyRecord = groupSessionBuilder.create(senderKeyName)
                    signalProtocolStore.storeSenderKey(senderKeyName, senderKeyRecord)
                    
                    Log.d(TAG, "Created sender key for new member $memberId in group $groupId")
                }

                // Rotate keys for forward secrecy (new members shouldn't see old messages)
                val allMemberIds = existingMemberIds + newMemberIds
                rotateSenderKeys(groupId, allMemberIds).getOrThrow()

                // Update encryption info
                val currentInfo = groupEncryptionInfo[groupId]!!
                val updatedInfo = currentInfo.copy(
                    memberCount = allMemberIds.size,
                    keyRotationCount = currentInfo.keyRotationCount + 1,
                    lastKeyRotation = System.currentTimeMillis()
                )
                groupEncryptionInfo[groupId] = updatedInfo

                // Update status
                updateGroupEncryptionStatus(groupId) { status ->
                    status.copy(
                        membersSynced = allMemberIds.size,
                        membersTotal = allMemberIds.size,
                        lastActivity = System.currentTimeMillis()
                    )
                }

                Log.i(TAG, "Successfully added ${newMemberIds.size} members to group encryption")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add members to group encryption", e)
            Result.failure(e)
        }
    }

    override suspend fun removeMembersFromGroupEncryption(
        groupId: String,
        removedMemberIds: List<String>,
        remainingMemberIds: List<String>
    ): Result<Unit> {
        return try {
            val mutex = encryptionMutex.getOrPut(groupId) { Mutex() }
            mutex.withLock {
                Log.d(TAG, "Removing ${removedMemberIds.size} members from group encryption for group $groupId")

                if (!isGroupEncryptionInitialized(groupId)) {
                    return@withLock Result.failure(Exception("Group encryption not initialized"))
                }

                // Remove sender keys for removed members
                for (memberId in removedMemberIds) {
                    val deviceId = 1
                    val senderKeyName = SignalSenderKeyName(
                        groupId, 
                        SignalProtocolAddress(memberId, deviceId)
                    )
                    signalProtocolStore.getChainSenderKeyStore().removeSenderKey(senderKeyName)
                    
                    Log.d(TAG, "Removed sender key for member $memberId from group $groupId")
                }

                // Rotate keys for forward secrecy (removed members shouldn't see new messages)
                rotateSenderKeys(groupId, remainingMemberIds).getOrThrow()

                // Update encryption info
                val currentInfo = groupEncryptionInfo[groupId]!!
                val updatedInfo = currentInfo.copy(
                    memberCount = remainingMemberIds.size,
                    keyRotationCount = currentInfo.keyRotationCount + 1,
                    lastKeyRotation = System.currentTimeMillis()
                )
                groupEncryptionInfo[groupId] = updatedInfo

                // Update status
                updateGroupEncryptionStatus(groupId) { status ->
                    status.copy(
                        membersSynced = remainingMemberIds.size,
                        membersTotal = remainingMemberIds.size,
                        lastActivity = System.currentTimeMillis()
                    )
                }

                Log.i(TAG, "Successfully removed ${removedMemberIds.size} members from group encryption")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove members from group encryption", e)
            Result.failure(e)
        }
    }

    override suspend fun encryptGroupMessage(
        groupId: String,
        senderId: String,
        deviceId: Int,
        message: ByteArray
    ): Result<EncryptedGroupMessage> {
        return try {
            Log.d(TAG, "Encrypting group message for group $groupId from sender $senderId")

            if (!isGroupEncryptionInitialized(groupId)) {
                return Result.failure(Exception("Group encryption not initialized"))
            }

            val senderKeyName = SignalSenderKeyName(groupId, SignalProtocolAddress(senderId, deviceId))
            val result = signalEncryptionService.encryptGroupMessage(senderKeyName, message)

            if (result.isSuccess) {
                // Update last activity
                updateGroupEncryptionStatus(groupId) { status ->
                    status.copy(lastActivity = System.currentTimeMillis())
                }
                Log.d(TAG, "Successfully encrypted group message")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt group message", e)
            Result.failure(e)
        }
    }

    override suspend fun decryptGroupMessage(
        groupId: String,
        senderId: String,
        deviceId: Int,
        encryptedMessage: EncryptedGroupMessage
    ): Result<ByteArray> {
        return try {
            Log.d(TAG, "Decrypting group message for group $groupId from sender $senderId")

            if (!isGroupEncryptionInitialized(groupId)) {
                return Result.failure(Exception("Group encryption not initialized"))
            }

            val senderKeyName = SignalSenderKeyName(groupId, SignalProtocolAddress(senderId, deviceId))
            val result = signalEncryptionService.decryptGroupMessage(senderKeyName, encryptedMessage)

            if (result.isSuccess) {
                // Update last activity
                updateGroupEncryptionStatus(groupId) { status ->
                    status.copy(lastActivity = System.currentTimeMillis())
                }
                Log.d(TAG, "Successfully decrypted group message")
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt group message", e)
            Result.failure(e)
        }
    }

    override suspend fun rotateSenderKeys(
        groupId: String,
        memberIds: List<String>
    ): Result<Unit> {
        return try {
            val mutex = encryptionMutex.getOrPut(groupId) { Mutex() }
            mutex.withLock {
                Log.d(TAG, "Rotating sender keys for group $groupId with ${memberIds.size} members")

                if (!isGroupEncryptionInitialized(groupId)) {
                    return@withLock Result.failure(Exception("Group encryption not initialized"))
                }

                val currentInfo = groupEncryptionInfo[groupId]!!
                if (currentInfo.keyRotationCount >= MAX_KEY_ROTATION_COUNT) {
                    Log.w(TAG, "Maximum key rotation count reached for group $groupId")
                    return@withLock Result.failure(Exception("Maximum key rotation count reached"))
                }

                // Create new sender keys for all members
                for (memberId in memberIds) {
                    val deviceId = 1
                    val senderKeyName = SignalSenderKeyName(groupId, SignalProtocolAddress(memberId, deviceId))
                    
                    val groupSessionBuilder = SignalGroupSessionBuilder(signalProtocolStore)
                    val newSenderKeyRecord = groupSessionBuilder.create(senderKeyName)
                    signalProtocolStore.storeSenderKey(senderKeyName, newSenderKeyRecord)
                    
                    Log.d(TAG, "Rotated sender key for member $memberId in group $groupId")
                }

                // Update encryption info
                val updatedInfo = currentInfo.copy(
                    keyRotationCount = currentInfo.keyRotationCount + 1,
                    lastKeyRotation = System.currentTimeMillis()
                )
                groupEncryptionInfo[groupId] = updatedInfo

                Log.i(TAG, "Successfully rotated sender keys for group $groupId")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rotate sender keys for group $groupId", e)
            Result.failure(e)
        }
    }

    override suspend fun getSenderKeyDistribution(
        groupId: String,
        senderId: String,
        deviceId: Int,
        recipientId: String
    ): Result<SenderKeyDistributionMessage> {
        return try {
            Log.d(TAG, "Getting sender key distribution for group $groupId, sender $senderId to recipient $recipientId")

            if (!isGroupEncryptionInitialized(groupId)) {
                return Result.failure(Exception("Group encryption not initialized"))
            }

            val senderKeyName = SignalSenderKeyName(groupId, SignalProtocolAddress(senderId, deviceId))
            val senderKeyRecord = signalProtocolStore.loadSenderKey(senderKeyName)
                ?: return Result.failure(Exception("Sender key not found"))

            // Create distribution message (simplified - in real implementation would use Signal's SenderKeyDistributionMessage)
            val distributionMessage = SenderKeyDistributionMessage(
                groupId = groupId,
                senderId = senderId,
                deviceId = deviceId,
                distributionData = senderKeyRecord.serialize(),
                timestamp = System.currentTimeMillis(),
                version = ENCRYPTION_VERSION
            )

            Log.d(TAG, "Created sender key distribution message")
            Result.success(distributionMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get sender key distribution", e)
            Result.failure(e)
        }
    }

    override suspend fun processSenderKeyDistribution(
        groupId: String,
        senderId: String,
        deviceId: Int,
        distributionMessage: SenderKeyDistributionMessage
    ): Result<Unit> {
        return try {
            Log.d(TAG, "Processing sender key distribution for group $groupId from sender $senderId")

            if (!isGroupEncryptionInitialized(groupId)) {
                return Result.failure(Exception("Group encryption not initialized"))
            }

            // Validate distribution message
            if (distributionMessage.groupId != groupId ||
                distributionMessage.senderId != senderId ||
                distributionMessage.deviceId != deviceId) {
                return Result.failure(Exception("Invalid distribution message"))
            }

            val senderKeyName = SignalSenderKeyName(groupId, SignalProtocolAddress(senderId, deviceId))
            val senderKeyRecord = SignalSenderKeyRecord(distributionMessage.distributionData)
            signalProtocolStore.storeSenderKey(senderKeyName, senderKeyRecord)

            Log.d(TAG, "Successfully processed sender key distribution")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to process sender key distribution", e)
            Result.failure(e)
        }
    }

    override suspend fun isGroupEncryptionInitialized(groupId: String): Boolean {
        return groupEncryptionInfo[groupId]?.isInitialized == true
    }

    override suspend fun getGroupEncryptionInfo(groupId: String): Result<GroupEncryptionInfo?> {
        return try {
            val info = groupEncryptionInfo[groupId]
            Result.success(info)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get group encryption info", e)
            Result.failure(e)
        }
    }

    override fun observeGroupEncryptionStatus(groupId: String): Flow<GroupEncryptionStatus> {
        return groupEncryptionStatus.getOrPut(groupId) {
            MutableStateFlow(
                GroupEncryptionStatus(
                    groupId = groupId,
                    isHealthy = false,
                    membersSynced = 0,
                    membersTotal = 0,
                    lastActivity = 0,
                    issues = listOf(EncryptionIssue.KeyRotationNeeded("Group encryption not initialized"))
                )
            )
        }.asStateFlow()
    }

    override suspend fun cleanupGroupEncryption(groupId: String): Result<Unit> {
        return try {
            val mutex = encryptionMutex.getOrPut(groupId) { Mutex() }
            mutex.withLock {
                Log.d(TAG, "Cleaning up group encryption for group $groupId")

                // Remove all sender keys for the group
                signalProtocolStore.getChainSenderKeyStore().removeAllSenderKeysForGroup(groupId)

                // Remove encryption info and status
                groupEncryptionInfo.remove(groupId)
                groupEncryptionStatus.remove(groupId)
                encryptionMutex.remove(groupId)

                Log.i(TAG, "Successfully cleaned up group encryption for group $groupId")
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup group encryption for group $groupId", e)
            Result.failure(e)
        }
    }

    override suspend fun verifySenderKeyIntegrity(
        groupId: String,
        senderId: String,
        deviceId: Int
    ): Result<Boolean> {
        return try {
            Log.d(TAG, "Verifying sender key integrity for group $groupId, sender $senderId")

            if (!isGroupEncryptionInitialized(groupId)) {
                return Result.failure(Exception("Group encryption not initialized"))
            }

            val senderKeyName = SignalSenderKeyName(groupId, SignalProtocolAddress(senderId, deviceId))
            val senderKeyRecord = signalProtocolStore.loadSenderKey(senderKeyName)

            val isValid = senderKeyRecord != null
            Log.d(TAG, "Sender key integrity verification result: $isValid")
            
            Result.success(isValid)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify sender key integrity", e)
            Result.failure(e)
        }
    }

    /**
     * Helper function to update group encryption status
     */
    private fun updateGroupEncryptionStatus(
        groupId: String,
        update: (GroupEncryptionStatus) -> GroupEncryptionStatus
    ) {
        val statusFlow = groupEncryptionStatus[groupId]
        if (statusFlow != null) {
            val currentStatus = statusFlow.value
            val updatedStatus = update(currentStatus)
            statusFlow.value = updatedStatus
        }
    }

    /**
     * Check if key rotation is needed based on time threshold
     */
    private fun isKeyRotationNeeded(groupId: String): Boolean {
        val info = groupEncryptionInfo[groupId] ?: return false
        val timeSinceLastRotation = System.currentTimeMillis() - info.lastKeyRotation
        val rotationThresholdMs = KEY_ROTATION_THRESHOLD_HOURS * 60 * 60 * 1000
        return timeSinceLastRotation > rotationThresholdMs
    }
}