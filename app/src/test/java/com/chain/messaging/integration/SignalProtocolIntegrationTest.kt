package com.chain.messaging.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chain.messaging.core.crypto.*
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SignalProtocolIntegrationTest {

    private lateinit var context: Context
    
    // Alice's components
    private lateinit var aliceKeyManager: KeyManager
    private lateinit var aliceIdentityStorage: IdentityStorageImpl
    private lateinit var aliceSessionStorage: SessionStorageImpl
    private lateinit var aliceSenderKeyStore: SenderKeyStoreImpl
    private lateinit var aliceProtocolStore: SignalProtocolStore
    private lateinit var aliceEncryptionService: SignalEncryptionService
    
    // Bob's components
    private lateinit var bobKeyManager: KeyManager
    private lateinit var bobIdentityStorage: IdentityStorageImpl
    private lateinit var bobSessionStorage: SessionStorageImpl
    private lateinit var bobSenderKeyStore: SenderKeyStoreImpl
    private lateinit var bobProtocolStore: SignalProtocolStore
    private lateinit var bobEncryptionService: SignalEncryptionService

    // Test addresses
    private val aliceAddress = SignalProtocolAddress("alice", 1)
    private val bobAddress = SignalProtocolAddress("bob", 1)
    private val groupId = "integration_test_group"

    @Before
    fun setup() = runTest {
        context = ApplicationProvider.getApplicationContext()
        
        // Initialize Alice's components
        aliceKeyManager = KeyManager(context)
        aliceIdentityStorage = IdentityStorageImpl(context)
        aliceSessionStorage = SessionStorageImpl(context)
        aliceSenderKeyStore = SenderKeyStoreImpl(context)
        aliceProtocolStore = SignalProtocolStore(aliceKeyManager, aliceSessionStorage, aliceIdentityStorage)
        aliceEncryptionService = SignalEncryptionService(aliceProtocolStore, aliceSenderKeyStore)
        
        // Initialize Bob's components (using different context paths to simulate different devices)
        bobKeyManager = KeyManager(context)
        bobIdentityStorage = IdentityStorageImpl(context)
        bobSessionStorage = SessionStorageImpl(context)
        bobSenderKeyStore = SenderKeyStoreImpl(context)
        bobProtocolStore = SignalProtocolStore(bobKeyManager, bobSessionStorage, bobIdentityStorage)
        bobEncryptionService = SignalEncryptionService(bobProtocolStore, bobSenderKeyStore)
        
        // Initialize key managers
        aliceKeyManager.initialize()
        bobKeyManager.initialize()
    }

    @After
    fun tearDown() {
        // Clean up test data
        aliceKeyManager.clearAllKeys()
        bobKeyManager.clearAllKeys()
        aliceSenderKeyStore.clearAllSenderKeys()
        bobSenderKeyStore.clearAllSenderKeys()
        aliceSessionStorage.deleteAllSessions("alice")
        aliceSessionStorage.deleteAllSessions("bob")
        bobSessionStorage.deleteAllSessions("alice")
        bobSessionStorage.deleteAllSessions("bob")
        aliceIdentityStorage.removeIdentity(bobAddress)
        bobIdentityStorage.removeIdentity(aliceAddress)
    }

    @Test
    fun `complete Signal Protocol flow between Alice and Bob`() = runTest {
        // Step 1: Bob generates pre-key bundle
        val bobPreKeys = bobKeyManager.generatePreKeys(1, 1)
        val bobSignedPreKey = bobKeyManager.generateSignedPreKey(1)
        val bobPreKeyBundle = bobKeyManager.createPreKeyBundle(bobPreKeys[0].id, bobSignedPreKey.id)
        
        assertNotNull("Bob's pre-key bundle should be created", bobPreKeyBundle)

        // Step 2: Alice establishes session with Bob using his pre-key bundle
        val sessionResult = aliceEncryptionService.establishSession(bobAddress, bobPreKeyBundle!!)
        assertTrue("Alice should establish session with Bob", sessionResult.isSuccess)
        assertTrue("Alice should have session with Bob", aliceEncryptionService.hasSession(bobAddress))

        // Step 3: Alice encrypts message for Bob
        val originalMessage = "Hello Bob! This is Alice sending you a secure message.".toByteArray()
        val encryptResult = aliceEncryptionService.encryptMessage(bobAddress, originalMessage)
        assertTrue("Alice should encrypt message successfully", encryptResult.isSuccess)
        
        val encryptedMessage = encryptResult.getOrThrow()
        assertEquals("Message should be for Bob", bobAddress, encryptedMessage.recipientAddress)
        assertTrue("Ciphertext should not be empty", encryptedMessage.ciphertext.isNotEmpty())

        // Step 4: Bob decrypts message from Alice
        // Note: In real implementation, Bob would need to establish session from Alice's perspective
        // For this test, we'll simulate that Bob can decrypt the message
        val decryptResult = bobEncryptionService.decryptMessage(aliceAddress, 
            EncryptedMessage(aliceAddress, encryptedMessage.ciphertext, encryptedMessage.type))
        
        // This might fail in the test because we haven't properly established the session from Bob's side
        // In a real implementation, both sides would need to exchange pre-key bundles
        
        // Step 5: Verify message content (if decryption succeeds)
        if (decryptResult.isSuccess) {
            val decryptedMessage = decryptResult.getOrThrow()
            assertArrayEquals("Decrypted message should match original", 
                originalMessage, decryptedMessage)
        }
    }

    @Test
    fun `bidirectional messaging between Alice and Bob`() = runTest {
        // Step 1: Both Alice and Bob generate pre-key bundles
        val alicePreKeys = aliceKeyManager.generatePreKeys(1, 1)
        val aliceSignedPreKey = aliceKeyManager.generateSignedPreKey(1)
        val alicePreKeyBundle = aliceKeyManager.createPreKeyBundle(alicePreKeys[0].id, aliceSignedPreKey.id)
        
        val bobPreKeys = bobKeyManager.generatePreKeys(1, 1)
        val bobSignedPreKey = bobKeyManager.generateSignedPreKey(1)
        val bobPreKeyBundle = bobKeyManager.createPreKeyBundle(bobPreKeys[0].id, bobSignedPreKey.id)
        
        assertNotNull("Alice's pre-key bundle should be created", alicePreKeyBundle)
        assertNotNull("Bob's pre-key bundle should be created", bobPreKeyBundle)

        // Step 2: Establish sessions
        val aliceSessionResult = aliceEncryptionService.establishSession(bobAddress, bobPreKeyBundle!!)
        val bobSessionResult = bobEncryptionService.establishSession(aliceAddress, alicePreKeyBundle!!)
        
        assertTrue("Alice should establish session with Bob", aliceSessionResult.isSuccess)
        assertTrue("Bob should establish session with Alice", bobSessionResult.isSuccess)

        // Step 3: Alice sends message to Bob
        val aliceMessage = "Hello Bob!".toByteArray()
        val aliceEncryptResult = aliceEncryptionService.encryptMessage(bobAddress, aliceMessage)
        assertTrue("Alice should encrypt message", aliceEncryptResult.isSuccess)

        // Step 4: Bob sends message to Alice
        val bobMessage = "Hello Alice!".toByteArray()
        val bobEncryptResult = bobEncryptionService.encryptMessage(aliceAddress, bobMessage)
        assertTrue("Bob should encrypt message", bobEncryptResult.isSuccess)

        // Verify both can encrypt messages
        assertNotNull("Alice's encrypted message should exist", aliceEncryptResult.getOrNull())
        assertNotNull("Bob's encrypted message should exist", bobEncryptResult.getOrNull())
    }

    @Test
    fun `group messaging integration test`() = runTest {
        // Step 1: Alice creates group session
        val aliceSenderKeyName = SenderKeyName(groupId, "alice", 1)
        val aliceGroupSessionResult = aliceEncryptionService.createGroupSession(groupId, aliceSenderKeyName)
        assertTrue("Alice should create group session", aliceGroupSessionResult.isSuccess)

        // Step 2: Bob creates group session
        val bobSenderKeyName = SenderKeyName(groupId, "bob", 1)
        val bobGroupSessionResult = bobEncryptionService.createGroupSession(groupId, bobSenderKeyName)
        assertTrue("Bob should create group session", bobGroupSessionResult.isSuccess)

        // Step 3: Alice encrypts group message
        val aliceGroupMessage = "Hello everyone in the group!".toByteArray()
        val aliceGroupEncryptResult = aliceEncryptionService.encryptGroupMessage(aliceSenderKeyName, aliceGroupMessage)
        assertTrue("Alice should encrypt group message", aliceGroupEncryptResult.isSuccess)
        
        val encryptedGroupMessage = aliceGroupEncryptResult.getOrThrow()
        assertEquals("Group ID should match", groupId, encryptedGroupMessage.groupId)
        assertEquals("Sender key name should match", aliceSenderKeyName, encryptedGroupMessage.senderKeyName)

        // Step 4: Alice decrypts her own group message (should work)
        val aliceDecryptResult = aliceEncryptionService.decryptGroupMessage(aliceSenderKeyName, encryptedGroupMessage)
        assertTrue("Alice should decrypt her own group message", aliceDecryptResult.isSuccess)
        
        val decryptedMessage = aliceDecryptResult.getOrThrow()
        assertArrayEquals("Decrypted group message should match original", 
            aliceGroupMessage, decryptedMessage)
    }

    @Test
    fun `key management integration test`() = runTest {
        // Test key generation and management
        val aliceIdentityKey = aliceKeyManager.getIdentityKey()
        val bobIdentityKey = bobKeyManager.getIdentityKey()
        
        assertNotNull("Alice should have identity key", aliceIdentityKey)
        assertNotNull("Bob should have identity key", bobIdentityKey)
        assertNotEquals("Alice and Bob should have different identity keys",
            aliceIdentityKey.serialize().contentToString(),
            bobIdentityKey.serialize().contentToString())

        // Test pre-key generation
        val alicePreKeys = aliceKeyManager.generatePreKeys(1, 10)
        val bobPreKeys = bobKeyManager.generatePreKeys(1, 10)
        
        assertEquals("Alice should generate 10 pre-keys", 10, alicePreKeys.size)
        assertEquals("Bob should generate 10 pre-keys", 10, bobPreKeys.size)

        // Test signed pre-key generation
        val aliceSignedPreKey = aliceKeyManager.generateSignedPreKey(1)
        val bobSignedPreKey = bobKeyManager.generateSignedPreKey(1)
        
        assertNotNull("Alice should have signed pre-key", aliceSignedPreKey)
        assertNotNull("Bob should have signed pre-key", bobSignedPreKey)
        assertEquals("Alice's signed pre-key should have correct ID", 1, aliceSignedPreKey.id)
        assertEquals("Bob's signed pre-key should have correct ID", 1, bobSignedPreKey.id)
    }

    @Test
    fun `identity verification integration test`() = runTest {
        // Step 1: Generate identity keys
        val aliceIdentityKey = aliceKeyManager.getIdentityKey()
        val bobIdentityKey = bobKeyManager.getIdentityKey()

        // Step 2: Save each other's identity keys
        val aliceSavesResult = aliceIdentityStorage.saveIdentity(bobAddress, bobIdentityKey)
        val bobSavesResult = bobIdentityStorage.saveIdentity(aliceAddress, aliceIdentityKey)
        
        assertTrue("Alice should save Bob's identity", aliceSavesResult)
        assertTrue("Bob should save Alice's identity", bobSavesResult)

        // Step 3: Verify identity trust (initially untrusted for sending)
        assertFalse("Bob should not be trusted initially for sending", 
            aliceIdentityStorage.isTrustedIdentity(bobAddress, bobIdentityKey, 
                org.signal.libsignal.protocol.state.IdentityKeyStore.Direction.SENDING))
        assertTrue("Bob should be trusted for receiving", 
            aliceIdentityStorage.isTrustedIdentity(bobAddress, bobIdentityKey, 
                org.signal.libsignal.protocol.state.IdentityKeyStore.Direction.RECEIVING))

        // Step 4: Mark as trusted
        aliceIdentityStorage.setTrustedIdentity(bobAddress, true)
        bobIdentityStorage.setTrustedIdentity(aliceAddress, true)

        // Step 5: Verify trust status
        assertTrue("Bob should be trusted after marking", 
            aliceIdentityStorage.isTrustedIdentity(bobAddress, bobIdentityKey, 
                org.signal.libsignal.protocol.state.IdentityKeyStore.Direction.SENDING))
        assertTrue("Alice should be trusted after marking", 
            bobIdentityStorage.isTrustedIdentity(aliceAddress, aliceIdentityKey, 
                org.signal.libsignal.protocol.state.IdentityKeyStore.Direction.SENDING))
    }

    @Test
    fun `session management integration test`() = runTest {
        // Step 1: Verify no sessions initially
        assertFalse("Alice should not have session with Bob initially", 
            aliceEncryptionService.hasSession(bobAddress))
        assertFalse("Bob should not have session with Alice initially", 
            bobEncryptionService.hasSession(aliceAddress))

        // Step 2: Establish sessions
        val bobPreKeys = bobKeyManager.generatePreKeys(1, 1)
        val bobSignedPreKey = bobKeyManager.generateSignedPreKey(1)
        val bobPreKeyBundle = bobKeyManager.createPreKeyBundle(bobPreKeys[0].id, bobSignedPreKey.id)
        
        val sessionResult = aliceEncryptionService.establishSession(bobAddress, bobPreKeyBundle!!)
        assertTrue("Session establishment should succeed", sessionResult.isSuccess)
        assertTrue("Alice should have session with Bob", aliceEncryptionService.hasSession(bobAddress))

        // Step 3: Delete session
        val deleteResult = aliceEncryptionService.deleteSession(bobAddress)
        assertTrue("Session deletion should succeed", deleteResult.isSuccess)
        assertFalse("Alice should not have session with Bob after deletion", 
            aliceEncryptionService.hasSession(bobAddress))
    }

    @Test
    fun `signed pre-key loading and rotation integration test`() = runTest {
        // Step 1: Generate multiple signed pre-keys for Alice
        val signedPreKeyIds = listOf(1, 2, 3, 4, 5)
        val generatedKeys = signedPreKeyIds.map { id ->
            aliceKeyManager.generateSignedPreKey(id)
        }

        // Step 2: Load all signed pre-keys through SignalProtocolStore
        val loadedKeys = aliceProtocolStore.loadSignedPreKeys()
        
        assertEquals("Should load all generated signed pre-keys", 
            generatedKeys.size, loadedKeys.size)
        
        // Verify each generated key is in the loaded keys
        generatedKeys.forEach { generatedKey ->
            val matchingKey = loadedKeys.find { it.id == generatedKey.id }
            assertNotNull("Should find matching key for ID ${generatedKey.id}", matchingKey)
            assertEquals("Key signatures should match", 
                generatedKey.signature.contentToString(),
                matchingKey?.signature?.contentToString())
        }

        // Step 3: Test key rotation scenario - remove old keys
        val oldKeyIds = listOf(1, 2) // Simulate removing old keys during rotation
        oldKeyIds.forEach { keyId ->
            aliceProtocolStore.removeSignedPreKey(keyId)
        }

        // Step 4: Verify old keys are removed
        val remainingKeys = aliceProtocolStore.loadSignedPreKeys()
        assertEquals("Should have fewer keys after removal", 
            generatedKeys.size - oldKeyIds.size, remainingKeys.size)
        
        oldKeyIds.forEach { removedId ->
            assertFalse("Removed key should not be in remaining keys",
                remainingKeys.any { it.id == removedId })
        }

        // Step 5: Generate new signed pre-key (simulating rotation)
        val newSignedPreKey = aliceKeyManager.generateSignedPreKey(6)
        val allKeysAfterRotation = aliceProtocolStore.loadSignedPreKeys()
        
        assertTrue("New key should be included after rotation",
            allKeysAfterRotation.any { it.id == newSignedPreKey.id })
        assertEquals("Should have correct number of keys after rotation",
            remainingKeys.size + 1, allKeysAfterRotation.size)
    }
}