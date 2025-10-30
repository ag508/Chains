package com.chain.messaging.core.crypto

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.groups.SenderKeyName
import org.signal.libsignal.protocol.state.PreKeyBundle

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SignalEncryptionServiceTest {

    private lateinit var context: Context
    private lateinit var keyManager: KeyManager
    private lateinit var identityStorage: IdentityStorageImpl
    private lateinit var sessionStorage: SessionStorageImpl
    private lateinit var senderKeyStore: SenderKeyStoreImpl
    private lateinit var protocolStore: SignalProtocolStore
    private lateinit var encryptionService: SignalEncryptionService

    // Test addresses
    private val aliceAddress = SignalProtocolAddress("alice", 1)
    private val bobAddress = SignalProtocolAddress("bob", 1)
    private val groupId = "test_group_123"

    @Before
    fun setup() = runTest {
        context = ApplicationProvider.getApplicationContext()
        
        // Initialize components
        keyManager = KeyManager(context)
        identityStorage = IdentityStorageImpl(context)
        sessionStorage = SessionStorageImpl(context)
        senderKeyStore = SenderKeyStoreImpl(context)
        
        protocolStore = SignalProtocolStore(keyManager, sessionStorage, identityStorage)
        encryptionService = SignalEncryptionService(protocolStore, senderKeyStore)
        
        // Initialize key manager
        keyManager.initialize()
    }

    @After
    fun tearDown() {
        // Clean up test data
        keyManager.clearAllKeys()
        senderKeyStore.clearAllSenderKeys()
        sessionStorage.deleteAllSessions("alice")
        sessionStorage.deleteAllSessions("bob")
        identityStorage.removeIdentity(aliceAddress)
        identityStorage.removeIdentity(bobAddress)
    }

    @Test
    fun `establishSession should create session successfully`() = runTest {
        // Given
        val preKeys = keyManager.generatePreKeys(1, 1)
        val signedPreKey = keyManager.generateSignedPreKey(1)
        val preKeyBundle = keyManager.createPreKeyBundle(preKeys[0].id, signedPreKey.id)
        
        assertNotNull("Pre-key bundle should be created", preKeyBundle)

        // When
        val result = encryptionService.establishSession(bobAddress, preKeyBundle!!)

        // Then
        assertTrue("Session establishment should succeed", result.isSuccess)
        assertTrue("Session should exist", encryptionService.hasSession(bobAddress))
    }

    @Test
    fun `encryptMessage and decryptMessage should work for established session`() = runTest {
        // Given - establish session first
        val preKeys = keyManager.generatePreKeys(1, 1)
        val signedPreKey = keyManager.generateSignedPreKey(1)
        val preKeyBundle = keyManager.createPreKeyBundle(preKeys[0].id, signedPreKey.id)
        encryptionService.establishSession(bobAddress, preKeyBundle!!)
        
        val originalMessage = "Hello, Bob! This is a test message.".toByteArray()

        // When - encrypt message
        val encryptResult = encryptionService.encryptMessage(bobAddress, originalMessage)

        // Then - encryption should succeed
        assertTrue("Encryption should succeed", encryptResult.isSuccess)
        val encryptedMessage = encryptResult.getOrThrow()
        
        assertNotNull("Encrypted message should not be null", encryptedMessage)
        assertEquals("Recipient should match", bobAddress, encryptedMessage.recipientAddress)
        assertTrue("Ciphertext should not be empty", encryptedMessage.ciphertext.isNotEmpty())
        
        // When - decrypt message
        val decryptResult = encryptionService.decryptMessage(bobAddress, encryptedMessage)

        // Then - decryption should succeed
        assertTrue("Decryption should succeed", decryptResult.isSuccess)
        val decryptedMessage = decryptResult.getOrThrow()
        
        assertArrayEquals("Decrypted message should match original", 
            originalMessage, decryptedMessage)
    }

    @Test
    fun `encryptMessage should fail without established session`() = runTest {
        // Given - no session established
        val message = "This should fail".toByteArray()

        // When
        val result = encryptionService.encryptMessage(bobAddress, message)

        // Then
        assertTrue("Encryption should fail without session", result.isFailure)
        assertTrue("Should be CryptoException", 
            result.exceptionOrNull() is CryptoException)
    }

    @Test
    fun `decryptMessage should fail with invalid ciphertext`() = runTest {
        // Given - establish session first
        val preKeys = keyManager.generatePreKeys(1, 1)
        val signedPreKey = keyManager.generateSignedPreKey(1)
        val preKeyBundle = keyManager.createPreKeyBundle(preKeys[0].id, signedPreKey.id)
        encryptionService.establishSession(bobAddress, preKeyBundle!!)
        
        // Create invalid encrypted message
        val invalidEncryptedMessage = EncryptedMessage(
            recipientAddress = bobAddress,
            ciphertext = "invalid_ciphertext".toByteArray(),
            type = EncryptedMessage.Type.SIGNAL
        )

        // When
        val result = encryptionService.decryptMessage(bobAddress, invalidEncryptedMessage)

        // Then
        assertTrue("Decryption should fail with invalid ciphertext", result.isFailure)
        assertTrue("Should be CryptoException", 
            result.exceptionOrNull() is CryptoException)
    }

    @Test
    fun `createGroupSession should succeed`() = runTest {
        // Given
        val senderKeyName = SenderKeyName(groupId, "alice", 1)

        // When
        val result = encryptionService.createGroupSession(groupId, senderKeyName)

        // Then
        assertTrue("Group session creation should succeed", result.isSuccess)
        val senderKeyRecord = result.getOrThrow()
        assertNotNull("Sender key record should not be null", senderKeyRecord)
    }

    @Test
    fun `encryptGroupMessage and decryptGroupMessage should work`() = runTest {
        // Given - create group session
        val senderKeyName = SenderKeyName(groupId, "alice", 1)
        val sessionResult = encryptionService.createGroupSession(groupId, senderKeyName)
        assertTrue("Group session should be created", sessionResult.isSuccess)
        
        val originalMessage = "Hello, group! This is a test group message.".toByteArray()

        // When - encrypt group message
        val encryptResult = encryptionService.encryptGroupMessage(senderKeyName, originalMessage)

        // Then - encryption should succeed
        assertTrue("Group encryption should succeed", encryptResult.isSuccess)
        val encryptedMessage = encryptResult.getOrThrow()
        
        assertNotNull("Encrypted group message should not be null", encryptedMessage)
        assertEquals("Group ID should match", groupId, encryptedMessage.groupId)
        assertEquals("Sender key name should match", senderKeyName, encryptedMessage.senderKeyName)
        assertTrue("Ciphertext should not be empty", encryptedMessage.ciphertext.isNotEmpty())

        // When - decrypt group message
        val decryptResult = encryptionService.decryptGroupMessage(senderKeyName, encryptedMessage)

        // Then - decryption should succeed
        assertTrue("Group decryption should succeed", decryptResult.isSuccess)
        val decryptedMessage = decryptResult.getOrThrow()
        
        assertArrayEquals("Decrypted group message should match original", 
            originalMessage, decryptedMessage)
    }

    @Test
    fun `encryptGroupMessage should fail without group session`() = runTest {
        // Given - no group session created
        val senderKeyName = SenderKeyName(groupId, "alice", 1)
        val message = "This should fail".toByteArray()

        // When
        val result = encryptionService.encryptGroupMessage(senderKeyName, message)

        // Then
        assertTrue("Group encryption should fail without session", result.isFailure)
        assertTrue("Should be CryptoException", 
            result.exceptionOrNull() is CryptoException)
    }

    @Test
    fun `hasSession should return correct status`() = runTest {
        // Given - no session initially
        assertFalse("Should not have session initially", 
            encryptionService.hasSession(bobAddress))

        // When - establish session
        val preKeys = keyManager.generatePreKeys(1, 1)
        val signedPreKey = keyManager.generateSignedPreKey(1)
        val preKeyBundle = keyManager.createPreKeyBundle(preKeys[0].id, signedPreKey.id)
        encryptionService.establishSession(bobAddress, preKeyBundle!!)

        // Then
        assertTrue("Should have session after establishment", 
            encryptionService.hasSession(bobAddress))
    }

    @Test
    fun `deleteSession should remove session`() = runTest {
        // Given - establish session first
        val preKeys = keyManager.generatePreKeys(1, 1)
        val signedPreKey = keyManager.generateSignedPreKey(1)
        val preKeyBundle = keyManager.createPreKeyBundle(preKeys[0].id, signedPreKey.id)
        encryptionService.establishSession(bobAddress, preKeyBundle!!)
        
        assertTrue("Session should exist before deletion", 
            encryptionService.hasSession(bobAddress))

        // When
        val result = encryptionService.deleteSession(bobAddress)

        // Then
        assertTrue("Session deletion should succeed", result.isSuccess)
        assertFalse("Session should not exist after deletion", 
            encryptionService.hasSession(bobAddress))
    }

    @Test
    fun `deleteAllSessions should remove all user sessions`() = runTest {
        // Given - establish sessions with multiple devices
        val bobDevice1 = SignalProtocolAddress("bob", 1)
        val bobDevice2 = SignalProtocolAddress("bob", 2)
        
        // Establish sessions
        val preKeys1 = keyManager.generatePreKeys(1, 1)
        val signedPreKey1 = keyManager.generateSignedPreKey(1)
        val preKeyBundle1 = keyManager.createPreKeyBundle(preKeys1[0].id, signedPreKey1.id)
        encryptionService.establishSession(bobDevice1, preKeyBundle1!!)
        
        val preKeys2 = keyManager.generatePreKeys(2, 1)
        val signedPreKey2 = keyManager.generateSignedPreKey(2)
        val preKeyBundle2 = keyManager.createPreKeyBundle(preKeys2[0].id, signedPreKey2.id)
        encryptionService.establishSession(bobDevice2, preKeyBundle2!!)
        
        assertTrue("Should have session with device 1", 
            encryptionService.hasSession(bobDevice1))
        assertTrue("Should have session with device 2", 
            encryptionService.hasSession(bobDevice2))

        // When
        val result = encryptionService.deleteAllSessions("bob")

        // Then
        assertTrue("All sessions deletion should succeed", result.isSuccess)
        assertFalse("Should not have session with device 1 after deletion", 
            encryptionService.hasSession(bobDevice1))
        assertFalse("Should not have session with device 2 after deletion", 
            encryptionService.hasSession(bobDevice2))
    }

    @Test
    fun `multiple message encryption should work with ratcheting`() = runTest {
        // Given - establish session
        val preKeys = keyManager.generatePreKeys(1, 1)
        val signedPreKey = keyManager.generateSignedPreKey(1)
        val preKeyBundle = keyManager.createPreKeyBundle(preKeys[0].id, signedPreKey.id)
        encryptionService.establishSession(bobAddress, preKeyBundle!!)
        
        val messages = listOf(
            "First message".toByteArray(),
            "Second message".toByteArray(),
            "Third message".toByteArray()
        )

        // When - encrypt and decrypt multiple messages
        val results = mutableListOf<ByteArray>()
        
        for (message in messages) {
            val encryptResult = encryptionService.encryptMessage(bobAddress, message)
            assertTrue("Encryption should succeed for all messages", encryptResult.isSuccess)
            
            val encryptedMessage = encryptResult.getOrThrow()
            val decryptResult = encryptionService.decryptMessage(bobAddress, encryptedMessage)
            assertTrue("Decryption should succeed for all messages", decryptResult.isSuccess)
            
            results.add(decryptResult.getOrThrow())
        }

        // Then - all messages should be correctly decrypted
        messages.zip(results).forEach { (original, decrypted) ->
            assertArrayEquals("Each message should be correctly decrypted", 
                original, decrypted)
        }
    }

    @Test
    fun `EncryptedMessage equals and hashCode should work correctly`() {
        // Given
        val address = SignalProtocolAddress("test", 1)
        val ciphertext = "test_ciphertext".toByteArray()
        
        val message1 = EncryptedMessage(address, ciphertext, EncryptedMessage.Type.SIGNAL)
        val message2 = EncryptedMessage(address, ciphertext, EncryptedMessage.Type.SIGNAL)
        val message3 = EncryptedMessage(address, "different".toByteArray(), EncryptedMessage.Type.SIGNAL)

        // Then
        assertEquals("Same messages should be equal", message1, message2)
        assertNotEquals("Different messages should not be equal", message1, message3)
        assertEquals("Same messages should have same hash code", 
            message1.hashCode(), message2.hashCode())
    }

    @Test
    fun `EncryptedGroupMessage equals and hashCode should work correctly`() {
        // Given
        val senderKeyName = SenderKeyName("group1", "sender", 1)
        val ciphertext = "test_ciphertext".toByteArray()
        
        val message1 = EncryptedGroupMessage("group1", senderKeyName, ciphertext)
        val message2 = EncryptedGroupMessage("group1", senderKeyName, ciphertext)
        val message3 = EncryptedGroupMessage("group2", senderKeyName, ciphertext)

        // Then
        assertEquals("Same group messages should be equal", message1, message2)
        assertNotEquals("Different group messages should not be equal", message1, message3)
        assertEquals("Same group messages should have same hash code", 
            message1.hashCode(), message2.hashCode())
    }
}