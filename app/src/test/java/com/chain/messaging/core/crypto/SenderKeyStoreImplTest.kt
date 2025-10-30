package com.chain.messaging.core.crypto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.signal.libsignal.protocol.groups.SenderKeyName
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SenderKeyStoreImplTest {

    private lateinit var context: Context
    private lateinit var senderKeyStore: SenderKeyStoreImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        senderKeyStore = SenderKeyStoreImpl(context)
    }

    @After
    fun tearDown() {
        // Clean up test data
        senderKeyStore.clearAllSenderKeys()
    }

    @Test
    fun `storeSenderKey and loadSenderKey should work correctly`() {
        // Given
        val senderKeyName = SenderKeyName("test_group", "alice", 1)
        val senderKeyRecord = SenderKeyRecord()

        // When
        senderKeyStore.storeSenderKey(senderKeyName, senderKeyRecord)
        val loadedRecord = senderKeyStore.loadSenderKey(senderKeyName)

        // Then
        assertNotNull("Loaded sender key record should not be null", loadedRecord)
        // Note: We can't directly compare SenderKeyRecord objects, but we can verify
        // that the record was stored and retrieved without error
    }

    @Test
    fun `loadSenderKey should return null for non-existent key`() {
        // Given
        val senderKeyName = SenderKeyName("non_existent_group", "alice", 1)

        // When
        val loadedRecord = senderKeyStore.loadSenderKey(senderKeyName)

        // Then
        assertNull("Should return null for non-existent sender key", loadedRecord)
    }

    @Test
    fun `removeSenderKey should delete sender key`() {
        // Given
        val senderKeyName = SenderKeyName("test_group", "alice", 1)
        val senderKeyRecord = SenderKeyRecord()
        
        senderKeyStore.storeSenderKey(senderKeyName, senderKeyRecord)
        assertNotNull("Sender key should exist before removal", 
            senderKeyStore.loadSenderKey(senderKeyName))

        // When
        senderKeyStore.removeSenderKey(senderKeyName)

        // Then
        assertNull("Sender key should not exist after removal", 
            senderKeyStore.loadSenderKey(senderKeyName))
    }

    @Test
    fun `removeAllSenderKeysForGroup should remove all keys for specific group`() {
        // Given
        val groupId = "test_group"
        val senderKeyName1 = SenderKeyName(groupId, "alice", 1)
        val senderKeyName2 = SenderKeyName(groupId, "bob", 1)
        val senderKeyName3 = SenderKeyName("other_group", "charlie", 1)
        
        val senderKeyRecord = SenderKeyRecord()
        
        // Store sender keys
        senderKeyStore.storeSenderKey(senderKeyName1, senderKeyRecord)
        senderKeyStore.storeSenderKey(senderKeyName2, senderKeyRecord)
        senderKeyStore.storeSenderKey(senderKeyName3, senderKeyRecord)

        // Verify all keys exist
        assertNotNull("Alice's key should exist", senderKeyStore.loadSenderKey(senderKeyName1))
        assertNotNull("Bob's key should exist", senderKeyStore.loadSenderKey(senderKeyName2))
        assertNotNull("Charlie's key should exist", senderKeyStore.loadSenderKey(senderKeyName3))

        // When
        senderKeyStore.removeAllSenderKeysForGroup(groupId)

        // Then
        assertNull("Alice's key should be removed", senderKeyStore.loadSenderKey(senderKeyName1))
        assertNull("Bob's key should be removed", senderKeyStore.loadSenderKey(senderKeyName2))
        assertNotNull("Charlie's key should still exist", senderKeyStore.loadSenderKey(senderKeyName3))
    }

    @Test
    fun `getSenderKeyNamesForGroup should return correct sender key names`() {
        // Given
        val groupId = "test_group"
        val senderKeyName1 = SenderKeyName(groupId, "alice", 1)
        val senderKeyName2 = SenderKeyName(groupId, "bob", 2)
        val senderKeyName3 = SenderKeyName("other_group", "charlie", 1)
        
        val senderKeyRecord = SenderKeyRecord()
        
        // Store sender keys
        senderKeyStore.storeSenderKey(senderKeyName1, senderKeyRecord)
        senderKeyStore.storeSenderKey(senderKeyName2, senderKeyRecord)
        senderKeyStore.storeSenderKey(senderKeyName3, senderKeyRecord)

        // When
        val senderKeyNames = senderKeyStore.getSenderKeyNamesForGroup(groupId)

        // Then
        assertEquals("Should return 2 sender key names for the group", 2, senderKeyNames.size)
        assertTrue("Should contain Alice's sender key name", senderKeyNames.contains(senderKeyName1))
        assertTrue("Should contain Bob's sender key name", senderKeyNames.contains(senderKeyName2))
        assertFalse("Should not contain Charlie's sender key name", senderKeyNames.contains(senderKeyName3))
    }

    @Test
    fun `getSenderKeyNamesForGroup should return empty list for non-existent group`() {
        // Given
        val nonExistentGroupId = "non_existent_group"

        // When
        val senderKeyNames = senderKeyStore.getSenderKeyNamesForGroup(nonExistentGroupId)

        // Then
        assertTrue("Should return empty list for non-existent group", senderKeyNames.isEmpty())
    }

    @Test
    fun `clearAllSenderKeys should remove all sender keys`() {
        // Given
        val senderKeyName1 = SenderKeyName("group1", "alice", 1)
        val senderKeyName2 = SenderKeyName("group2", "bob", 1)
        val senderKeyRecord = SenderKeyRecord()
        
        senderKeyStore.storeSenderKey(senderKeyName1, senderKeyRecord)
        senderKeyStore.storeSenderKey(senderKeyName2, senderKeyRecord)

        // Verify keys exist
        assertNotNull("Alice's key should exist", senderKeyStore.loadSenderKey(senderKeyName1))
        assertNotNull("Bob's key should exist", senderKeyStore.loadSenderKey(senderKeyName2))

        // When
        senderKeyStore.clearAllSenderKeys()

        // Then
        assertNull("Alice's key should be removed", senderKeyStore.loadSenderKey(senderKeyName1))
        assertNull("Bob's key should be removed", senderKeyStore.loadSenderKey(senderKeyName2))
    }

    @Test
    fun `sender keys should be isolated between different groups`() {
        // Given
        val group1 = "group1"
        val group2 = "group2"
        val senderKeyName1 = SenderKeyName(group1, "alice", 1)
        val senderKeyName2 = SenderKeyName(group2, "alice", 1)
        val senderKeyRecord = SenderKeyRecord()

        // When
        senderKeyStore.storeSenderKey(senderKeyName1, senderKeyRecord)
        senderKeyStore.storeSenderKey(senderKeyName2, senderKeyRecord)

        // Then
        assertNotNull("Group1 key should exist", senderKeyStore.loadSenderKey(senderKeyName1))
        assertNotNull("Group2 key should exist", senderKeyStore.loadSenderKey(senderKeyName2))

        // When removing keys for group1
        senderKeyStore.removeAllSenderKeysForGroup(group1)

        // Then
        assertNull("Group1 key should be removed", senderKeyStore.loadSenderKey(senderKeyName1))
        assertNotNull("Group2 key should still exist", senderKeyStore.loadSenderKey(senderKeyName2))
    }

    @Test
    fun `sender keys should be isolated between different devices`() {
        // Given
        val groupId = "test_group"
        val senderKeyName1 = SenderKeyName(groupId, "alice", 1)
        val senderKeyName2 = SenderKeyName(groupId, "alice", 2)
        val senderKeyRecord = SenderKeyRecord()

        // When
        senderKeyStore.storeSenderKey(senderKeyName1, senderKeyRecord)
        senderKeyStore.storeSenderKey(senderKeyName2, senderKeyRecord)

        // Then
        assertNotNull("Device 1 key should exist", senderKeyStore.loadSenderKey(senderKeyName1))
        assertNotNull("Device 2 key should exist", senderKeyStore.loadSenderKey(senderKeyName2))

        // When removing device 1 key
        senderKeyStore.removeSenderKey(senderKeyName1)

        // Then
        assertNull("Device 1 key should be removed", senderKeyStore.loadSenderKey(senderKeyName1))
        assertNotNull("Device 2 key should still exist", senderKeyStore.loadSenderKey(senderKeyName2))
    }

    @Test
    fun `sender keys should persist across SenderKeyStore instances`() {
        // Given
        val senderKeyName = SenderKeyName("test_group", "alice", 1)
        val senderKeyRecord = SenderKeyRecord()
        
        senderKeyStore.storeSenderKey(senderKeyName, senderKeyRecord)

        // When - create new SenderKeyStore instance
        val newSenderKeyStore = SenderKeyStoreImpl(context)
        val loadedRecord = newSenderKeyStore.loadSenderKey(senderKeyName)

        // Then
        assertNotNull("Sender key should persist across instances", loadedRecord)
    }
}