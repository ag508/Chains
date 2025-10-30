package com.chain.messaging.core.crypto

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.IdentityKeyPair

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class KeyManagerTest {

    private lateinit var context: Context
    private lateinit var keyManager: KeyManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        keyManager = KeyManager(context)
    }

    @After
    fun tearDown() {
        // Clean up test data
        keyManager.clearAllKeys()
    }

    @Test
    fun `initialize should generate keys successfully`() = runTest {
        // When
        val result = keyManager.initialize()

        // Then
        assertTrue("Initialization should succeed", result.isSuccess)
        
        // Verify keys are generated
        val identityKeyPair = keyManager.getIdentityKeyPair()
        assertNotNull("Identity key pair should be generated", identityKeyPair)
        
        val registrationId = keyManager.getRegistrationId()
        assertTrue("Registration ID should be positive", registrationId > 0)
        
        val deviceId = keyManager.getDeviceId()
        assertTrue("Device ID should be positive", deviceId > 0)
    }

    @Test
    fun `getIdentityKeyPair should return consistent keys across calls`() = runTest {
        // Given
        keyManager.initialize()

        // When
        val keyPair1 = keyManager.getIdentityKeyPair()
        val keyPair2 = keyManager.getIdentityKeyPair()

        // Then
        assertEquals("Identity key pairs should be identical", 
            keyPair1.publicKey.serialize().contentToString(),
            keyPair2.publicKey.serialize().contentToString())
        assertEquals("Private keys should be identical",
            keyPair1.privateKey.serialize().contentToString(),
            keyPair2.privateKey.serialize().contentToString())
    }

    @Test
    fun `generatePreKeys should create specified number of keys`() = runTest {
        // Given
        keyManager.initialize()
        val startId = 1
        val count = 10

        // When
        val preKeys = keyManager.generatePreKeys(startId, count)

        // Then
        assertEquals("Should generate correct number of pre-keys", count, preKeys.size)
        
        // Verify each pre-key has correct ID
        preKeys.forEachIndexed { index, preKey ->
            assertEquals("Pre-key should have correct ID", startId + index, preKey.id)
        }
        
        // Verify keys can be loaded
        preKeys.forEach { preKey ->
            val loadedKey = keyManager.loadPreKey(preKey.id)
            assertNotNull("Pre-key should be loadable", loadedKey)
            assertEquals("Loaded key should match generated key", 
                preKey.id, loadedKey?.id)
        }
    }

    @Test
    fun `generateSignedPreKey should create valid signed key`() = runTest {
        // Given
        keyManager.initialize()
        val signedPreKeyId = 1

        // When
        val signedPreKey = keyManager.generateSignedPreKey(signedPreKeyId)

        // Then
        assertEquals("Signed pre-key should have correct ID", signedPreKeyId, signedPreKey.id)
        assertNotNull("Signature should not be null", signedPreKey.signature)
        assertTrue("Timestamp should be recent", 
            System.currentTimeMillis() - signedPreKey.timestamp < 5000)
        
        // Verify key can be loaded
        val loadedKey = keyManager.loadSignedPreKey(signedPreKeyId)
        assertNotNull("Signed pre-key should be loadable", loadedKey)
        assertEquals("Loaded key should match generated key", 
            signedPreKey.id, loadedKey?.id)
    }

    @Test
    fun `createPreKeyBundle should return valid bundle`() = runTest {
        // Given
        keyManager.initialize()
        val preKeys = keyManager.generatePreKeys(1, 1)
        val signedPreKey = keyManager.generateSignedPreKey(1)

        // When
        val bundle = keyManager.createPreKeyBundle(preKeys[0].id, signedPreKey.id)

        // Then
        assertNotNull("Pre-key bundle should be created", bundle)
        assertEquals("Bundle should have correct registration ID", 
            keyManager.getRegistrationId(), bundle?.registrationId)
        assertEquals("Bundle should have correct device ID", 
            keyManager.getDeviceId(), bundle?.deviceId)
        assertEquals("Bundle should have correct pre-key ID", 
            preKeys[0].id, bundle?.preKeyId)
        assertEquals("Bundle should have correct signed pre-key ID", 
            signedPreKey.id, bundle?.signedPreKeyId)
    }

    @Test
    fun `removePreKey should delete key successfully`() = runTest {
        // Given
        keyManager.initialize()
        val preKeys = keyManager.generatePreKeys(1, 1)
        val preKeyId = preKeys[0].id

        // Verify key exists
        assertNotNull("Pre-key should exist before removal", 
            keyManager.loadPreKey(preKeyId))

        // When
        keyManager.removePreKey(preKeyId)

        // Then
        assertNull("Pre-key should not exist after removal", 
            keyManager.loadPreKey(preKeyId))
    }

    @Test
    fun `getPreKeyIds should return all stored pre-key IDs`() = runTest {
        // Given
        keyManager.initialize()
        val startId = 1
        val count = 5
        keyManager.generatePreKeys(startId, count)

        // When
        val preKeyIds = keyManager.getPreKeyIds()

        // Then
        assertEquals("Should return correct number of IDs", count, preKeyIds.size)
        
        // Verify all expected IDs are present
        for (i in 0 until count) {
            assertTrue("Should contain pre-key ID ${startId + i}", 
                preKeyIds.contains(startId + i))
        }
    }

    @Test
    fun `rotateIdentityKeys should generate new keys`() = runTest {
        // Given
        keyManager.initialize()
        val originalKeyPair = keyManager.getIdentityKeyPair()

        // When
        val result = keyManager.rotateIdentityKeys()

        // Then
        assertTrue("Key rotation should succeed", result.isSuccess)
        
        val newKeyPair = keyManager.getIdentityKeyPair()
        assertNotEquals("New key pair should be different from original",
            originalKeyPair.publicKey.serialize().contentToString(),
            newKeyPair.publicKey.serialize().contentToString())
    }

    @Test
    fun `clearAllKeys should remove all stored data`() = runTest {
        // Given
        keyManager.initialize()
        keyManager.generatePreKeys(1, 5)
        keyManager.generateSignedPreKey(1)

        // Verify data exists
        assertTrue("Pre-keys should exist", keyManager.getPreKeyIds().isNotEmpty())

        // When
        keyManager.clearAllKeys()

        // Then
        assertThrows("Should throw exception when accessing cleared keys",
            CryptoException::class.java) {
            keyManager.getIdentityKeyPair()
        }
        
        assertThrows("Should throw exception when accessing cleared registration ID",
            CryptoException::class.java) {
            keyManager.getRegistrationId()
        }
    }

    @Test
    fun `keys should persist across KeyManager instances`() = runTest {
        // Given
        keyManager.initialize()
        val originalRegistrationId = keyManager.getRegistrationId()
        val originalIdentityKey = keyManager.getIdentityKey()

        // When - create new KeyManager instance
        val newKeyManager = KeyManager(context)
        newKeyManager.initialize()

        // Then
        assertEquals("Registration ID should persist", 
            originalRegistrationId, newKeyManager.getRegistrationId())
        assertEquals("Identity key should persist",
            originalIdentityKey.serialize().contentToString(),
            newKeyManager.getIdentityKey().serialize().contentToString())
    }

    @Test
    fun `generateSymmetricKey should create valid AES key`() = runTest {
        // Given
        keyManager.initialize()

        // When
        val symmetricKey = keyManager.generateSymmetricKey()

        // Then
        assertNotNull("Symmetric key should be generated", symmetricKey)
        assertEquals("Key should be AES", "AES", symmetricKey.algorithm)
    }

    @Test
    fun `getSignedPreKeyIds should return all stored signed pre-key IDs`() = runTest {
        // Given
        keyManager.initialize()
        val signedPreKeyIds = listOf(1, 2, 3)
        signedPreKeyIds.forEach { id ->
            keyManager.generateSignedPreKey(id)
        }

        // When
        val retrievedIds = keyManager.getSignedPreKeyIds()

        // Then
        assertEquals("Should return correct number of signed pre-key IDs", 
            signedPreKeyIds.size, retrievedIds.size)
        
        signedPreKeyIds.forEach { expectedId ->
            assertTrue("Should contain signed pre-key ID $expectedId", 
                retrievedIds.contains(expectedId))
        }
    }

    @Test
    fun `loadAllSignedPreKeys should return all stored signed pre-keys`() = runTest {
        // Given
        keyManager.initialize()
        val signedPreKeyIds = listOf(1, 2, 3)
        val generatedKeys = signedPreKeyIds.map { id ->
            keyManager.generateSignedPreKey(id)
        }

        // When
        val loadedKeys = keyManager.loadAllSignedPreKeys()

        // Then
        assertEquals("Should return correct number of signed pre-keys", 
            generatedKeys.size, loadedKeys.size)
        
        generatedKeys.forEach { generatedKey ->
            val matchingKey = loadedKeys.find { it.id == generatedKey.id }
            assertNotNull("Should find matching key for ID ${generatedKey.id}", matchingKey)
            assertEquals("Key signatures should match", 
                generatedKey.signature.contentToString(),
                matchingKey?.signature?.contentToString())
        }
    }

    @Test
    fun `removeSignedPreKey should delete signed pre-key successfully`() = runTest {
        // Given
        keyManager.initialize()
        val signedPreKeyId = 1
        keyManager.generateSignedPreKey(signedPreKeyId)

        // Verify key exists
        assertNotNull("Signed pre-key should exist before removal", 
            keyManager.loadSignedPreKey(signedPreKeyId))
        assertTrue("Signed pre-key ID should be in list", 
            keyManager.getSignedPreKeyIds().contains(signedPreKeyId))

        // When
        keyManager.removeSignedPreKey(signedPreKeyId)

        // Then
        assertNull("Signed pre-key should not exist after removal", 
            keyManager.loadSignedPreKey(signedPreKeyId))
        assertFalse("Signed pre-key ID should not be in list", 
            keyManager.getSignedPreKeyIds().contains(signedPreKeyId))
    }

    @Test
    fun `loadAllSignedPreKeys should return empty list when no keys exist`() = runTest {
        // Given
        keyManager.initialize()

        // When
        val loadedKeys = keyManager.loadAllSignedPreKeys()

        // Then
        assertTrue("Should return empty list when no signed pre-keys exist", 
            loadedKeys.isEmpty())
    }
}