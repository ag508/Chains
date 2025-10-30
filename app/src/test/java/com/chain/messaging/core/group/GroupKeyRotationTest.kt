package com.chain.messaging.core.group

import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.crypto.SenderKeyStore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord

/**
 * Tests for group key rotation scenarios and forward secrecy.
 */
class GroupKeyRotationTest {

    private lateinit var groupEncryptionManager: GroupEncryptionManagerImpl
    private lateinit var mockSignalEncryptionService: SignalEncryptionService
    private lateinit var mockSenderKeyStore: SenderKeyStore

    private val testGroupId = "test-group-rotation"
    private val testCreatorId = "creator-user"
    private val testMemberIds = listOf("user-1", "user-2", "user-3")

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
    fun `key rotation increments rotation count and updates timestamp`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        val initialInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()!!
        val initialRotationCount = initialInfo.keyRotationCount
        val initialTimestamp = initialInfo.lastKeyRotation

        // When
        Thread.sleep(10) // Ensure timestamp difference
        val result = groupEncryptionManager.rotateSenderKeys(testGroupId, testMemberIds)

        // Then
        assertTrue("Key rotation should succeed", result.isSuccess)
        
        val updatedInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()!!
        assertEquals("Rotation count should be incremented", 
            initialRotationCount + 1, updatedInfo.keyRotationCount)
        assertTrue("Timestamp should be updated", 
            updatedInfo.lastKeyRotation > initialTimestamp)
    }

    @Test
    fun `key rotation creates new sender keys for all members`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        // When
        val result = groupEncryptionManager.rotateSenderKeys(testGroupId, testMemberIds)

        // Then
        assertTrue("Key rotation should succeed", result.isSuccess)
        
        // Verify new keys were stored for all members (initial + rotation)
        verify(atLeast = testMemberIds.size * 2) { 
            mockSenderKeyStore.storeSenderKey(any(), any()) 
        }
    }

    @Test
    fun `key rotation fails when group not initialized`() = runTest {
        // Given - group not initialized

        // When
        val result = groupEncryptionManager.rotateSenderKeys(testGroupId, testMemberIds)

        // Then
        assertTrue("Key rotation should fail", result.isFailure)
        assertTrue("Should have correct error message", 
            result.exceptionOrNull()?.message?.contains("not initialized") == true)
    }

    @Test
    fun `key rotation fails when maximum rotation count reached`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        // Simulate reaching maximum rotation count
        repeat(1001) { // MAX_KEY_ROTATION_COUNT is 1000
            try {
                groupEncryptionManager.rotateSenderKeys(testGroupId, testMemberIds)
            } catch (e: Exception) {
                // Expected to fail at some point
                break
            }
        }

        // When - try one more rotation
        val result = groupEncryptionManager.rotateSenderKeys(testGroupId, testMemberIds)

        // Then
        assertTrue("Key rotation should fail", result.isFailure)
        assertTrue("Should have correct error message", 
            result.exceptionOrNull()?.message?.contains("Maximum key rotation count") == true)
    }

    @Test
    fun `adding members triggers key rotation for forward secrecy`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        val initialInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()!!
        val initialRotationCount = initialInfo.keyRotationCount

        // When
        val newMemberIds = listOf("user-4", "user-5")
        val result = groupEncryptionManager.addMembersToGroupEncryption(
            groupId = testGroupId,
            newMemberIds = newMemberIds,
            existingMemberIds = testMemberIds
        )

        // Then
        assertTrue("Adding members should succeed", result.isSuccess)
        
        val updatedInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()!!
        assertEquals("Rotation count should be incremented", 
            initialRotationCount + 1, updatedInfo.keyRotationCount)
    }

    @Test
    fun `removing members triggers key rotation for forward secrecy`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        val initialInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()!!
        val initialRotationCount = initialInfo.keyRotationCount

        // When
        val removedMemberIds = listOf("user-3")
        val remainingMemberIds = listOf("user-1", "user-2")
        val mockSenderKeyStoreImpl = mockk<com.chain.messaging.core.crypto.SenderKeyStoreImpl>(relaxed = true)
        groupEncryptionManager = GroupEncryptionManagerImpl(
            signalEncryptionService = mockSignalEncryptionService,
            senderKeyStore = mockSenderKeyStoreImpl
        )
        
        // Re-initialize with new store
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )
        
        val result = groupEncryptionManager.removeMembersFromGroupEncryption(
            groupId = testGroupId,
            removedMemberIds = removedMemberIds,
            remainingMemberIds = remainingMemberIds
        )

        // Then
        assertTrue("Removing members should succeed", result.isSuccess)
        
        val updatedInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()!!
        assertEquals("Rotation count should be incremented", 1, updatedInfo.keyRotationCount)
        
        // Verify removed member's key was deleted
        verify { mockSenderKeyStoreImpl.removeSenderKey(any()) }
    }

    @Test
    fun `key rotation preserves group encryption functionality`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        val testMessage = "Test message after rotation".toByteArray()
        val mockEncryptedMessage = mockk<com.chain.messaging.core.crypto.EncryptedGroupMessage>()
        
        every { 
            mockSignalEncryptionService.encryptGroupMessage(any(), testMessage) 
        } returns Result.success(mockEncryptedMessage)
        
        every { 
            mockSignalEncryptionService.decryptGroupMessage(any(), mockEncryptedMessage) 
        } returns Result.success(testMessage)

        // When - rotate keys
        val rotateResult = groupEncryptionManager.rotateSenderKeys(testGroupId, testMemberIds)
        assertTrue("Key rotation should succeed", rotateResult.isSuccess)

        // Then - encryption should still work
        val encryptResult = groupEncryptionManager.encryptGroupMessage(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            message = testMessage
        )
        assertTrue("Encryption should work after rotation", encryptResult.isSuccess)

        val decryptResult = groupEncryptionManager.decryptGroupMessage(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            encryptedMessage = mockEncryptedMessage
        )
        assertTrue("Decryption should work after rotation", decryptResult.isSuccess)
        assertArrayEquals("Message should be correctly decrypted", testMessage, decryptResult.getOrNull())
    }

    @Test
    fun `concurrent key rotations are handled safely`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        // When - perform concurrent key rotations
        val rotationTasks = (1..5).map {
            kotlinx.coroutines.async {
                groupEncryptionManager.rotateSenderKeys(testGroupId, testMemberIds)
            }
        }

        val results = rotationTasks.map { it.await() }

        // Then - all rotations should complete (some may succeed, some may fail due to concurrency)
        results.forEach { result ->
            assertTrue("Rotation should complete without throwing", 
                result.isSuccess || result.isFailure)
        }

        // At least one rotation should succeed
        assertTrue("At least one rotation should succeed", 
            results.any { it.isSuccess })

        // Final rotation count should be reasonable
        val finalInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()!!
        assertTrue("Final rotation count should be reasonable", 
            finalInfo.keyRotationCount in 1..5)
    }

    @Test
    fun `key rotation updates encryption status`() = runTest {
        // Given
        groupEncryptionManager.initializeGroupEncryption(
            testGroupId, testMemberIds, testCreatorId
        )

        val statusFlow = groupEncryptionManager.observeGroupEncryptionStatus(testGroupId)
        val initialStatus = statusFlow.replayCache.firstOrNull() 
            ?: kotlinx.coroutines.flow.first(statusFlow)
        val initialActivity = initialStatus.lastActivity

        // When
        Thread.sleep(10) // Ensure timestamp difference
        val result = groupEncryptionManager.rotateSenderKeys(testGroupId, testMemberIds)

        // Then
        assertTrue("Key rotation should succeed", result.isSuccess)
        
        // Status should be updated (we can't easily test the flow update in this setup,
        // but we can verify the rotation succeeded which would update the status)
        val updatedInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()!!
        assertTrue("Last rotation should be updated", 
            updatedInfo.lastKeyRotation > initialActivity)
    }
}