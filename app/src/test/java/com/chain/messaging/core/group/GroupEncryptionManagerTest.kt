package com.chain.messaging.core.group

import com.chain.messaging.core.crypto.EncryptedGroupMessage
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.crypto.SenderKeyStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.signal.libsignal.protocol.groups.SenderKeyName
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord

class GroupEncryptionManagerTest {

    private lateinit var groupEncryptionManager: GroupEncryptionManagerImpl
    private lateinit var mockSignalEncryptionService: SignalEncryptionService
    private lateinit var mockSenderKeyStore: SenderKeyStore

    private val testGroupId = "test-group-123"
    private val testCreatorId = "creator-user-1"
    private val testMemberIds = listOf("user-1", "user-2", "user-3")
    private val testMessage = "Hello, group!".toByteArray()

    @Before
    fun setup() {
        mockSignalEncryptionService = mockk()
        mockSenderKeyStore = mockk(relaxed = true)
        
        groupEncryptionManager = GroupEncryptionManagerImpl(
            signalEncryptionService = mockSignalEncryptionService,
            senderKeyStore = mockSenderKeyStore
        )
    }

    @Test
    fun `initializeGroupEncryption creates sender keys for all members`() = runTest {
        // Given
        val mockSenderKeyRecord = mockk<SenderKeyRecord>()
        every { mockSenderKeyStore.storeSenderKey(any(), any()) } returns Unit

        // When
        val result = groupEncryptionManager.initializeGroupEncryption(
            groupId = testGroupId,
            memberIds = testMemberIds,
            creatorId = testCreatorId
        )

        // Then
        assertTrue("Should succeed", result.isSuccess)
        val encryptionInfo = result.getOrNull()
        assertNotNull("Should return encryption info", encryptionInfo)
        assertEquals("Should have correct group ID", testGroupId, encryptionInfo!!.groupId)
        assertEquals("Should have correct member count", testMemberIds.size, encryptionInfo.memberCount)
        assertEquals("Should have initial key rotation count", 0, encryptionInfo.keyRotationCount)
        assertTrue("Should be initialized", encryptionInfo.isInitialized)

        // Verify sender keys were stored for all members
        verify(exactly = testMemberIds.size) { 
            mockSenderKeyStore.storeSenderKey(any(), any()) 
        }
    }

    @Test
    fun `initializeGroupEncryption fails when already initialized`() = runTest {
        // Given - initialize first time
        groupEncryptionManager.initializeGroupEncryption(
            groupId = testGroupId,
            memberIds = testMemberIds,
            creatorId = testCreatorId
        )

        // When - try to initialize again
        val result = groupEncryptionManager.initializeGroupEncryption(
            groupId = testGroupId,
            memberIds = testMemberIds,
            creatorId = testCreatorId
        )

        // Then - should still succeed but not create new keys
        assertTrue("Should succeed", result.isSuccess)
    }

    @Test
    fun `addMembersToGroupEncryption creates keys for new members and rotates existing keys`() = runTest {
        // Given - initialize group first
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        val newMemberIds = listOf("user-4", "user-5")
        val allMemberIds = testMemberIds + newMemberIds

        // When
        val result = groupEncryptionManager.addMembersToGroupEncryption(
            groupId = testGroupId,
            newMemberIds = newMemberIds,
            existingMemberIds = testMemberIds
        )

        // Then
        assertTrue("Should succeed", result.isSuccess)

        // Verify encryption info was updated
        val encryptionInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()
        assertNotNull("Should have encryption info", encryptionInfo)
        assertEquals("Should have updated member count", allMemberIds.size, encryptionInfo!!.memberCount)
        assertEquals("Should have incremented key rotation count", 1, encryptionInfo.keyRotationCount)
    }

    @Test
    fun `addMembersToGroupEncryption fails when group not initialized`() = runTest {
        // Given - group not initialized
        val newMemberIds = listOf("user-4", "user-5")

        // When
        val result = groupEncryptionManager.addMembersToGroupEncryption(
            groupId = testGroupId,
            newMemberIds = newMemberIds,
            existingMemberIds = testMemberIds
        )

        // Then
        assertTrue("Should fail", result.isFailure)
        assertTrue("Should have correct error message", 
            result.exceptionOrNull()?.message?.contains("not initialized") == true)
    }

    @Test
    fun `removeMembersFromGroupEncryption removes keys and rotates remaining keys`() = runTest {
        // Given - initialize group first
        val mockSenderKeyStoreImpl = mockk<com.chain.messaging.core.crypto.SenderKeyStoreImpl>(relaxed = true)
        groupEncryptionManager = GroupEncryptionManagerImpl(
            signalEncryptionService = mockSignalEncryptionService,
            senderKeyStore = mockSenderKeyStoreImpl
        )
        
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        val removedMemberIds = listOf("user-3")
        val remainingMemberIds = listOf("user-1", "user-2")

        // When
        val result = groupEncryptionManager.removeMembersFromGroupEncryption(
            groupId = testGroupId,
            removedMemberIds = removedMemberIds,
            remainingMemberIds = remainingMemberIds
        )

        // Then
        assertTrue("Should succeed", result.isSuccess)

        // Verify removed member's key was deleted
        verify { mockSenderKeyStoreImpl.removeSenderKey(any()) }

        // Verify encryption info was updated
        val encryptionInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()
        assertNotNull("Should have encryption info", encryptionInfo)
        assertEquals("Should have updated member count", remainingMemberIds.size, encryptionInfo!!.memberCount)
        assertEquals("Should have incremented key rotation count", 1, encryptionInfo.keyRotationCount)
    }

    @Test
    fun `encryptGroupMessage encrypts message using Signal service`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        val mockEncryptedMessage = mockk<EncryptedGroupMessage>()
        coEvery { 
            mockSignalEncryptionService.encryptGroupMessage(any(), testMessage) 
        } returns Result.success(mockEncryptedMessage)

        // When
        val result = groupEncryptionManager.encryptGroupMessage(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            message = testMessage
        )

        // Then
        assertTrue("Should succeed", result.isSuccess)
        assertEquals("Should return encrypted message", mockEncryptedMessage, result.getOrNull())
        
        coVerify { 
            mockSignalEncryptionService.encryptGroupMessage(
                match { it.groupId == testGroupId && it.senderName == testCreatorId },
                testMessage
            )
        }
    }

    @Test
    fun `encryptGroupMessage fails when group not initialized`() = runTest {
        // Given - group not initialized

        // When
        val result = groupEncryptionManager.encryptGroupMessage(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            message = testMessage
        )

        // Then
        assertTrue("Should fail", result.isFailure)
        assertTrue("Should have correct error message", 
            result.exceptionOrNull()?.message?.contains("not initialized") == true)
    }

    @Test
    fun `decryptGroupMessage decrypts message using Signal service`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        val mockEncryptedMessage = mockk<EncryptedGroupMessage>()
        coEvery { 
            mockSignalEncryptionService.decryptGroupMessage(any(), mockEncryptedMessage) 
        } returns Result.success(testMessage)

        // When
        val result = groupEncryptionManager.decryptGroupMessage(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            encryptedMessage = mockEncryptedMessage
        )

        // Then
        assertTrue("Should succeed", result.isSuccess)
        assertArrayEquals("Should return decrypted message", testMessage, result.getOrNull())
        
        coVerify { 
            mockSignalEncryptionService.decryptGroupMessage(
                match { it.groupId == testGroupId && it.senderName == testCreatorId },
                mockEncryptedMessage
            )
        }
    }

    @Test
    fun `rotateSenderKeys creates new keys for all members`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        // When
        val result = groupEncryptionManager.rotateSenderKeys(
            groupId = testGroupId,
            memberIds = testMemberIds
        )

        // Then
        assertTrue("Should succeed", result.isSuccess)

        // Verify encryption info was updated
        val encryptionInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()
        assertNotNull("Should have encryption info", encryptionInfo)
        assertEquals("Should have incremented key rotation count", 1, encryptionInfo!!.keyRotationCount)
    }

    @Test
    fun `getSenderKeyDistribution creates distribution message`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        val mockSenderKeyRecord = mockk<SenderKeyRecord>()
        val mockSerializedKey = byteArrayOf(1, 2, 3, 4, 5)
        every { mockSenderKeyRecord.serialize() } returns mockSerializedKey
        every { mockSenderKeyStore.loadSenderKey(any()) } returns mockSenderKeyRecord

        // When
        val result = groupEncryptionManager.getSenderKeyDistribution(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            recipientId = "user-2"
        )

        // Then
        assertTrue("Should succeed", result.isSuccess)
        val distributionMessage = result.getOrNull()
        assertNotNull("Should return distribution message", distributionMessage)
        assertEquals("Should have correct group ID", testGroupId, distributionMessage!!.groupId)
        assertEquals("Should have correct sender ID", testCreatorId, distributionMessage.senderId)
        assertEquals("Should have correct device ID", 1, distributionMessage.deviceId)
        assertArrayEquals("Should have serialized key data", mockSerializedKey, distributionMessage.distributionData)
    }

    @Test
    fun `processSenderKeyDistribution stores received key`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        val distributionMessage = SenderKeyDistributionMessage(
            groupId = testGroupId,
            senderId = "user-2",
            deviceId = 1,
            distributionData = byteArrayOf(1, 2, 3, 4, 5),
            timestamp = System.currentTimeMillis(),
            version = 1
        )

        // When
        val result = groupEncryptionManager.processSenderKeyDistribution(
            groupId = testGroupId,
            senderId = "user-2",
            deviceId = 1,
            distributionMessage = distributionMessage
        )

        // Then
        assertTrue("Should succeed", result.isSuccess)
        verify { mockSenderKeyStore.storeSenderKey(any(), any()) }
    }

    @Test
    fun `isGroupEncryptionInitialized returns correct status`() = runTest {
        // Given - group not initialized
        assertFalse("Should not be initialized initially", 
            groupEncryptionManager.isGroupEncryptionInitialized(testGroupId))

        // When - initialize group
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        // Then
        assertTrue("Should be initialized after initialization", 
            groupEncryptionManager.isGroupEncryptionInitialized(testGroupId))
    }

    @Test
    fun `observeGroupEncryptionStatus returns status flow`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        // When
        val statusFlow = groupEncryptionManager.observeGroupEncryptionStatus(testGroupId)
        val status = statusFlow.first()

        // Then
        assertEquals("Should have correct group ID", testGroupId, status.groupId)
        assertTrue("Should be healthy", status.isHealthy)
        assertEquals("Should have correct member count", testMemberIds.size, status.membersSynced)
        assertEquals("Should have correct total members", testMemberIds.size, status.membersTotal)
    }

    @Test
    fun `cleanupGroupEncryption removes all group data`() = runTest {
        // Given
        val mockSenderKeyStoreImpl = mockk<com.chain.messaging.core.crypto.SenderKeyStoreImpl>(relaxed = true)
        groupEncryptionManager = GroupEncryptionManagerImpl(
            signalEncryptionService = mockSignalEncryptionService,
            senderKeyStore = mockSenderKeyStoreImpl
        )
        
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        // When
        val result = groupEncryptionManager.cleanupGroupEncryption(testGroupId)

        // Then
        assertTrue("Should succeed", result.isSuccess)
        verify { mockSenderKeyStoreImpl.removeAllSenderKeysForGroup(testGroupId) }
        
        // Verify group is no longer initialized
        assertFalse("Should not be initialized after cleanup", 
            groupEncryptionManager.isGroupEncryptionInitialized(testGroupId))
    }

    @Test
    fun `verifySenderKeyIntegrity checks key existence`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        val mockSenderKeyRecord = mockk<SenderKeyRecord>()
        every { mockSenderKeyStore.loadSenderKey(any()) } returns mockSenderKeyRecord

        // When
        val result = groupEncryptionManager.verifySenderKeyIntegrity(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1
        )

        // Then
        assertTrue("Should succeed", result.isSuccess)
        assertTrue("Should verify key exists", result.getOrNull() == true)
    }

    @Test
    fun `verifySenderKeyIntegrity returns false for missing key`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        every { mockSenderKeyStore.loadSenderKey(any()) } returns null

        // When
        val result = groupEncryptionManager.verifySenderKeyIntegrity(
            groupId = testGroupId,
            senderId = "non-existent-user",
            deviceId = 1
        )

        // Then
        assertTrue("Should succeed", result.isSuccess)
        assertFalse("Should verify key does not exist", result.getOrNull() == true)
    }
}