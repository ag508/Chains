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
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.SignalProtocolAddress
import org.signal.libsignal.protocol.ecc.Curve
import org.signal.libsignal.protocol.state.IdentityKeyStore

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class IdentityStorageImplTest {

    private lateinit var context: Context
    private lateinit var identityStorage: IdentityStorageImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        identityStorage = IdentityStorageImpl(context)
    }

    @After
    fun tearDown() {
        // Clean up test data
        val testAddress = SignalProtocolAddress("testuser", 1)
        identityStorage.removeIdentity(testAddress)
    }

    @Test
    fun `saveIdentity should store new identity and return true`() {
        // Given
        val address = SignalProtocolAddress("testuser", 1)
        val identityKey = IdentityKey(Curve.generateKeyPair().publicKey)

        // When
        val hasChanged = identityStorage.saveIdentity(address, identityKey)

        // Then
        assertTrue("Should return true for new identity", hasChanged)
        
        val storedIdentity = identityStorage.getIdentity(address)
        assertNotNull("Identity should be stored", storedIdentity)
        assertEquals("Stored identity should match", 
            identityKey.serialize().contentToString(),
            storedIdentity?.serialize()?.contentToString())
    }

    @Test
    fun `saveIdentity should return false for same identity`() {
        // Given
        val address = SignalProtocolAddress("testuser", 1)
        val identityKey = IdentityKey(Curve.generateKeyPair().publicKey)
        
        // Store identity first time
        identityStorage.saveIdentity(address, identityKey)

        // When - store same identity again
        val hasChanged = identityStorage.saveIdentity(address, identityKey)

        // Then
        assertFalse("Should return false for same identity", hasChanged)
    }

    @Test
    fun `saveIdentity should return true for changed identity`() {
        // Given
        val address = SignalProtocolAddress("testuser", 1)
        val originalIdentity = IdentityKey(Curve.generateKeyPair().publicKey)
        val newIdentity = IdentityKey(Curve.generateKeyPair().publicKey)
        
        // Store original identity
        identityStorage.saveIdentity(address, originalIdentity)

        // When - store different identity
        val hasChanged = identityStorage.saveIdentity(address, newIdentity)

        // Then
        assertTrue("Should return true for changed identity", hasChanged)
        
        val storedIdentity = identityStorage.getIdentity(address)
        assertEquals("Should store new identity", 
            newIdentity.serialize().contentToString(),
            storedIdentity?.serialize()?.contentToString())
    }

    @Test
    fun `isTrustedIdentity should handle first contact correctly`() {
        // Given
        val address = SignalProtocolAddress("newuser", 1)
        val identityKey = IdentityKey(Curve.generateKeyPair().publicKey)

        // When/Then - for receiving (should allow first contact)
        assertTrue("Should trust new identity for receiving", 
            identityStorage.isTrustedIdentity(
                address, identityKey, IdentityKeyStore.Direction.RECEIVING))

        // When/Then - for sending (should require explicit trust)
        assertFalse("Should not trust new identity for sending", 
            identityStorage.isTrustedIdentity(
                address, identityKey, IdentityKeyStore.Direction.SENDING))
    }

    @Test
    fun `isTrustedIdentity should respect trust settings`() {
        // Given
        val address = SignalProtocolAddress("testuser", 1)
        val identityKey = IdentityKey(Curve.generateKeyPair().publicKey)
        
        // Store and mark as trusted
        identityStorage.saveIdentity(address, identityKey)
        identityStorage.setTrustedIdentity(address, true)

        // When/Then
        assertTrue("Should trust marked identity for sending", 
            identityStorage.isTrustedIdentity(
                address, identityKey, IdentityKeyStore.Direction.SENDING))
        assertTrue("Should trust marked identity for receiving", 
            identityStorage.isTrustedIdentity(
                address, identityKey, IdentityKeyStore.Direction.RECEIVING))
    }

    @Test
    fun `isTrustedIdentity should reject changed identity`() {
        // Given
        val address = SignalProtocolAddress("testuser", 1)
        val originalIdentity = IdentityKey(Curve.generateKeyPair().publicKey)
        val changedIdentity = IdentityKey(Curve.generateKeyPair().publicKey)
        
        // Store original identity and mark as trusted
        identityStorage.saveIdentity(address, originalIdentity)
        identityStorage.setTrustedIdentity(address, true)

        // When/Then - should reject changed identity
        assertFalse("Should not trust changed identity for sending", 
            identityStorage.isTrustedIdentity(
                address, changedIdentity, IdentityKeyStore.Direction.SENDING))
        assertFalse("Should not trust changed identity for receiving", 
            identityStorage.isTrustedIdentity(
                address, changedIdentity, IdentityKeyStore.Direction.RECEIVING))
    }

    @Test
    fun `getIdentity should return null for non-existent identity`() {
        // Given
        val address = SignalProtocolAddress("nonexistent", 1)

        // When
        val identity = identityStorage.getIdentity(address)

        // Then
        assertNull("Should return null for non-existent identity", identity)
    }

    @Test
    fun `setTrustedIdentity should update trust status`() {
        // Given
        val address = SignalProtocolAddress("testuser", 1)
        val identityKey = IdentityKey(Curve.generateKeyPair().publicKey)
        
        identityStorage.saveIdentity(address, identityKey)

        // Initially should not be trusted for sending
        assertFalse("Should not be trusted initially for sending", 
            identityStorage.isTrustedIdentity(
                address, identityKey, IdentityKeyStore.Direction.SENDING))

        // When
        identityStorage.setTrustedIdentity(address, true)

        // Then
        assertTrue("Should be trusted after setting", 
            identityStorage.isTrustedIdentity(
                address, identityKey, IdentityKeyStore.Direction.SENDING))

        // When - set to untrusted
        identityStorage.setTrustedIdentity(address, false)

        // Then
        assertFalse("Should not be trusted after unsetting", 
            identityStorage.isTrustedIdentity(
                address, identityKey, IdentityKeyStore.Direction.SENDING))
    }

    @Test
    fun `getAllIdentities should return all stored identities`() {
        // Given
        val addresses = listOf(
            SignalProtocolAddress("user1", 1),
            SignalProtocolAddress("user2", 1),
            SignalProtocolAddress("user1", 2)
        )
        val identities = addresses.map { IdentityKey(Curve.generateKeyPair().publicKey) }
        
        // Store identities
        addresses.zip(identities).forEach { (address, identity) ->
            identityStorage.saveIdentity(address, identity)
        }

        // When
        val allIdentities = identityStorage.getAllIdentities()

        // Then
        assertEquals("Should return correct number of identities", 
            addresses.size, allIdentities.size)
        
        addresses.zip(identities).forEach { (address, expectedIdentity) ->
            val storedIdentity = allIdentities[address]
            assertNotNull("Should contain identity for $address", storedIdentity)
            assertEquals("Identity should match for $address",
                expectedIdentity.serialize().contentToString(),
                storedIdentity?.serialize()?.contentToString())
        }
    }

    @Test
    fun `removeIdentity should delete identity and trust status`() {
        // Given
        val address = SignalProtocolAddress("testuser", 1)
        val identityKey = IdentityKey(Curve.generateKeyPair().publicKey)
        
        identityStorage.saveIdentity(address, identityKey)
        identityStorage.setTrustedIdentity(address, true)

        // Verify identity exists
        assertNotNull("Identity should exist before removal", 
            identityStorage.getIdentity(address))

        // When
        identityStorage.removeIdentity(address)

        // Then
        assertNull("Identity should not exist after removal", 
            identityStorage.getIdentity(address))
        
        // Trust status should also be reset
        assertFalse("Should not be trusted after removal", 
            identityStorage.isTrustedIdentity(
                address, identityKey, IdentityKeyStore.Direction.SENDING))
    }
}