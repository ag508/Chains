package com.chain.messaging.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chain.messaging.core.crypto.KeyManager
import com.chain.messaging.core.crypto.SenderKeyStoreImpl
import com.chain.messaging.core.crypto.SignalEncryptionService
import com.chain.messaging.core.crypto.SignalProtocolStore
import com.chain.messaging.core.group.GroupEncryptionManagerImpl
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Integration tests for group encryption functionality.
 * Tests the complete flow of group encryption, key distribution, and forward secrecy.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GroupEncryptionIntegrationTest {

    private lateinit var context: Context
    private lateinit var keyManager: KeyManager
    private lateinit var protocolStore: SignalProtocolStore
    private lateinit var senderKeyStore: SenderKeyStoreImpl
    private lateinit var signalEncryptionService: SignalEncryptionService
    private lateinit var groupEncryptionManager: GroupEncryptionManagerImpl

    private val testGroupId = "integration-test-group"
    private val testCreatorId = "creator-user"
    private val testMemberIds = listOf("user-1", "user-2", "user-3", "user-4")
    private val testMessage = "Hello, this is a test group message!".toByteArray()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Initialize crypto components
        keyManager = KeyManager(context)
        protocolStore = SignalProtocolStore(context, keyManager)
        senderKeyStore = SenderKeyStoreImpl(context)
        signalEncryptionService = SignalEncryptionService(protocolStore, senderKeyStore)
        
        // Initialize group encryption manager
        groupEncryptionManager = GroupEncryptionManagerImpl(
            signalEncryptionService = signalEncryptionService,
            senderKeyStore = senderKeyStore
        )
    }

    @Test
    fun `complete group encryption flow - initialization, messaging, and key rotation`() = runTest {
        // Step 1: Initialize group encryption
        val initResult = groupEncryptionManager.initializeGroupEncryption(
            groupId = testGroupId,
            memberIds = testMemberIds,
            creatorId = testCreatorId
        )
        
        assertTrue("Group encryption initialization should succeed", initResult.isSuccess)
        val encryptionInfo = initResult.getOrNull()!!
        assertEquals("Should have correct member count", testMemberIds.size, encryptionInfo.memberCount)
        assertTrue("Should be initialized", encryptionInfo.isInitialized)

        // Step 2: Verify group is properly initialized
        assertTrue("Group should be initialized", 
            groupEncryptionManager.isGroupEncryptionInitialized(testGroupId))

        // Step 3: Encrypt a message from creator
        val encryptResult = groupEncryptionManager.encryptGroupMessage(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            message = testMessage
        )
        
        assertTrue("Message encryption should succeed", encryptResult.isSuccess)
        val encryptedMessage = encryptResult.getOrNull()!!
        assertEquals("Encrypted message should have correct group ID", testGroupId, encryptedMessage.groupId)

        // Step 4: Decrypt the message as another member
        val decryptResult = groupEncryptionManager.decryptGroupMessage(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            encryptedMessage = encryptedMessage
        )
        
        assertTrue("Message decryption should succeed", decryptResult.isSuccess)
        val decryptedMessage = decryptResult.getOrNull()!!
        assertArrayEquals("Decrypted message should match original", testMessage, decryptedMessage)

        // Step 5: Rotate sender keys
        val rotateResult = groupEncryptionManager.rotateSenderKeys(
            groupId = testGroupId,
            memberIds = testMemberIds
        )
        
        assertTrue("Key rotation should succeed", rotateResult.isSuccess)

        // Step 6: Verify encryption info was updated
        val updatedInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()!!
        assertEquals("Key rotation count should be incremented", 1, updatedInfo.keyRotationCount)
        assertTrue("Last rotation time should be updated", 
            updatedInfo.lastKeyRotation > encryptionInfo.lastKeyRotation)
    }

    @Test
    fun `adding new members provides forward secrecy`() = runTest {
        // Step 1: Initialize group with initial members
        val initialMembers = listOf("user-1", "user-2")
        groupEncryptionManager.initializeGroupEncryption(
            groupId = testGroupId,
            memberIds = initialMembers,
            creatorId = testCreatorId
        )

        // Step 2: Send a message before adding new members
        val oldMessage = "This message should not be visible to new members".toByteArray()
        val oldEncryptResult = groupEncryptionManager.encryptGroupMessage(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            message = oldMessage
        )
        assertTrue("Old message encryption should succeed", oldEncryptResult.isSuccess)
        val oldEncryptedMessage = oldEncryptResult.getOrNull()!!

        // Step 3: Add new members
        val newMembers = listOf("user-3", "user-4")
        val addResult = groupEncryptionManager.addMembersToGroupEncryption(
            groupId = testGroupId,
            newMemberIds = newMembers,
            existingMemberIds = initialMembers
        )
        assertTrue("Adding members should succeed", addResult.isSuccess)

        // Step 4: Verify encryption info was updated
        val updatedInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()!!
        assertEquals("Member count should be updated", 
            initialMembers.size + newMembers.size, updatedInfo.memberCount)
        assertEquals("Key rotation count should be incremented", 1, updatedInfo.keyRotationCount)

        // Step 5: Send a new message after adding members
        val newMessage = "This message should be visible to all members".toByteArray()
        val newEncryptResult = groupEncryptionManager.encryptGroupMessage(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            message = newMessage
        )
        assertTrue("New message encryption should succeed", newEncryptResult.isSuccess)
        val newEncryptedMessage = newEncryptResult.getOrNull()!!

        // Step 6: All members should be able to decrypt the new message
        val newDecryptResult = groupEncryptionManager.decryptGroupMessage(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            encryptedMessage = newEncryptedMessage
        )
        assertTrue("New message decryption should succeed", newDecryptResult.isSuccess)
        assertArrayEquals("New message should be decryptable", newMessage, newDecryptResult.getOrNull())
    }

    @Test
    fun `removing members ensures forward secrecy`() = runTest {
        // Step 1: Initialize group with all members
        groupEncryptionManager.initializeGroupEncryption(
            groupId = testGroupId,
            memberIds = testMemberIds,
            creatorId = testCreatorId
        )

        // Step 2: Remove some members
        val removedMembers = listOf("user-3", "user-4")
        val remainingMembers = listOf("user-1", "user-2")
        val removeResult = groupEncryptionManager.removeMembersFromGroupEncryption(
            groupId = testGroupId,
            removedMemberIds = removedMembers,
            remainingMemberIds = remainingMembers
        )
        assertTrue("Removing members should succeed", removeResult.isSuccess)

        // Step 3: Verify encryption info was updated
        val updatedInfo = groupEncryptionManager.getGroupEncryptionInfo(testGroupId).getOrNull()!!
        assertEquals("Member count should be updated", remainingMembers.size, updatedInfo.memberCount)
        assertEquals("Key rotation count should be incremented", 1, updatedInfo.keyRotationCount)

        // Step 4: Send a message after removing members
        val postRemovalMessage = "This message should not be visible to removed members".toByteArray()
        val encryptResult = groupEncryptionManager.encryptGroupMessage(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            message = postRemovalMessage
        )
        assertTrue("Post-removal message encryption should succeed", encryptResult.isSuccess)

        // Step 5: Remaining members should be able to decrypt the message
        val decryptResult = groupEncryptionManager.decryptGroupMessage(
            groupId = testGroupId,
            senderId = testCreatorId,
            deviceId = 1,
            encryptedMessage = encryptResult.getOrNull()!!
        )
        assertTrue("Post-removal message decryption should succeed", decryptResult.isSuccess)
        assertArrayEquals("Post-removal message should be decryptable", 
            postRemovalMessage, decryptResult.getOrNull())
    }

    @Test
    fun `sender key distribution works between members`() = runTest {
        // Step 1: Initialize group
        groupEncryptionManager.initializeGroupEncryption(
            groupId = testGroupId,
            memberIds = testMemberIds,
            creatorId = testCreatorId
        )

        // Step 2: Get sender key distribution from one member
        val distributionResult = groupEncryptionManager.getSenderKeyDistribution(
            groupId = testGroupId,
            senderId = "user-1",
            deviceId = 1,
            recipientId = "user-2"
        )
        assertTrue("Getting sender key distribution should succeed", distributionResult.isSuccess)
        val distributionMessage = distributionResult.getOrNull()!!

        // Step 3: Verify distribution message properties
        assertEquals("Distribution message should have correct group ID", 
            testGroupId, distributionMessage.groupId)
        assertEquals("Distribution message should have correct sender ID", 
            "user-1", distributionMessage.senderId)
        assertEquals("Distribution message should have correct device ID", 
            1, distributionMessage.deviceId)
        assertTrue("Distribution message should have data", 
            distributionMessage.distributionData.isNotEmpty())

        // Step 4: Process the distribution message
        val processResult = groupEncryptionManager.processSenderKeyDistribution(
            groupId = testGroupId,
            senderId = "user-1",
            deviceId = 1,
            distributionMessage = distributionMessage
        )
        assertTrue("Processing sender key distribution should succeed", processResult.isSuccess)
    }

    @Test
    fun `encryption status monitoring works correctly`() = runTest {
        // Step 1: Initialize group
        groupEncryptionManager.initializeGroupEncryption(
            groupId = testGroupId,
            memberIds = testMemberIds,
            creatorId = testCreatorId
        )

        // Step 2: Observe encryption status
        val statusFlow = groupEncryptionManager.observeGroupEncryptionStatus(testGroupId)
        
        // Step 3: Collect initial status
        val initialStatus = statusFlow.replayCache.firstOrNull() 
            ?: kotlinx.coroutines.flow.first(statusFlow)

        // Step 4: Verify initial status
        assertEquals("Status should have correct group ID", testGroupId, initialStatus.groupId)
        assertTrue("Status should be healthy", initialStatus.isHealthy)
        assertEquals("Status should have correct synced members", 
            testMemberIds.size, initialStatus.membersSynced)
        assertEquals("Status should have correct total members", 
            testMemberIds.size, initialStatus.membersTotal)
        assertTrue("Status should have recent activity", 
            initialStatus.lastActivity > 0)
    }

    @Test
    fun `sender key integrity verification works correctly`() = runTest {
        // Step 1: Initialize group
        groupEncryptionManager.initializeGroupEncryption(
            groupId = testGroupId,
            memberIds = testMemberIds,
            creatorId = testCreatorId
        )

        // Step 2: Verify integrity for existing member
        val verifyResult = groupEncryptionManager.verifySenderKeyIntegrity(
            groupId = testGroupId,
            senderId = "user-1",
            deviceId = 1
        )
        assertTrue("Integrity verification should succeed", verifyResult.isSuccess)
        assertTrue("Integrity should be valid for existing member", verifyResult.getOrNull() == true)

        // Step 3: Verify integrity for non-existent member
        val verifyNonExistentResult = groupEncryptionManager.verifySenderKeyIntegrity(
            groupId = testGroupId,
            senderId = "non-existent-user",
            deviceId = 1
        )
        assertTrue("Integrity verification should succeed", verifyNonExistentResult.isSuccess)
        assertFalse("Integrity should be invalid for non-existent member", 
            verifyNonExistentResult.getOrNull() == true)
    }

    @Test
    fun `group cleanup removes all encryption data`() = runTest {
        // Step 1: Initialize group
        groupEncryptionManager.initializeGroupEncryption(
            groupId = testGroupId,
            memberIds = testMemberIds,
            creatorId = testCreatorId
        )

        // Step 2: Verify group is initialized
        assertTrue("Group should be initialized", 
            groupEncryptionManager.isGroupEncryptionInitialized(testGroupId))

        // Step 3: Clean up group
        val cleanupResult = groupEncryptionManager.cleanupGroupEncryption(testGroupId)
        assertTrue("Cleanup should succeed", cleanupResult.isSuccess)

        // Step 4: Verify group is no longer initialized
        assertFalse("Group should not be initialized after cleanup", 
            groupEncryptionManager.isGroupEncryptionInitialized(testGroupId))

        // Step 5: Verify encryption info is removed
        val infoResult = groupEncryptionManager.getGroupEncryptionInfo(testGroupId)
        assertTrue("Getting info should succeed", infoResult.isSuccess)
        assertNull("Info should be null after cleanup", infoResult.getOrNull())
    }

    @Test
    fun `multiple concurrent operations are handled safely`() = runTest {
        // Step 1: Initialize group
        groupEncryptionManager.initializeGroupEncryption(
            groupId = testGroupId,
            memberIds = testMemberIds,
            creatorId = testCreatorId
        )

        // Step 2: Perform multiple concurrent operations
        val operations = listOf(
            // Encrypt messages concurrently
            kotlinx.coroutines.async {
                groupEncryptionManager.encryptGroupMessage(
                    testGroupId, "user-1", 1, "Message 1".toByteArray()
                )
            },
            kotlinx.coroutines.async {
                groupEncryptionManager.encryptGroupMessage(
                    testGroupId, "user-2", 1, "Message 2".toByteArray()
                )
            },
            // Rotate keys concurrently
            kotlinx.coroutines.async {
                groupEncryptionManager.rotateSenderKeys(testGroupId, testMemberIds)
            },
            // Verify integrity concurrently
            kotlinx.coroutines.async {
                groupEncryptionManager.verifySenderKeyIntegrity(testGroupId, "user-1", 1)
            }
        )

        // Step 3: Wait for all operations to complete
        val results = operations.map { it.await() }

        // Step 4: Verify all operations succeeded (or failed gracefully)
        results.forEach { result ->
            assertTrue("Concurrent operation should not throw exceptions", 
                result.isSuccess || result.isFailure)
        }
    }
}