package com.chain.messaging.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class SecureStorageTest {
    
    private lateinit var secureStorage: SecureStorageImpl
    private val mockContext = mockk<Context>()
    private val mockSharedPrefs = mockk<SharedPreferences>()
    private val mockEditor = mockk<SharedPreferences.Editor>()
    
    @Before
    fun setup() {
        clearAllMocks()
        
        // Mock EncryptedSharedPreferences creation
        mockkStatic(EncryptedSharedPreferences::class)
        every { 
            EncryptedSharedPreferences.create(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns mockSharedPrefs
        
        every { mockSharedPrefs.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.remove(any()) } returns mockEditor
        every { mockEditor.clear() } returns mockEditor
        every { mockEditor.apply() } just Runs
        
        secureStorage = SecureStorageImpl(mockContext)
    }
    
    @Test
    fun `store saves value to encrypted preferences`() = runTest {
        // Given
        val key = "test_key"
        val value = "test_value"
        
        // When
        secureStorage.store(key, value)
        
        // Then
        verify { mockEditor.putString(key, value) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `get retrieves value from encrypted preferences`() = runTest {
        // Given
        val key = "test_key"
        val expectedValue = "test_value"
        every { mockSharedPrefs.getString(key, null) } returns expectedValue
        
        // When
        val result = secureStorage.get(key)
        
        // Then
        assertEquals(expectedValue, result)
        verify { mockSharedPrefs.getString(key, null) }
    }
    
    @Test
    fun `get returns null when key not found`() = runTest {
        // Given
        val key = "nonexistent_key"
        every { mockSharedPrefs.getString(key, null) } returns null
        
        // When
        val result = secureStorage.get(key)
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `remove deletes value from encrypted preferences`() = runTest {
        // Given
        val key = "test_key"
        
        // When
        secureStorage.remove(key)
        
        // Then
        verify { mockEditor.remove(key) }
        verify { mockEditor.apply() }
    }
    
    @Test
    fun `contains checks if key exists in encrypted preferences`() = runTest {
        // Given
        val key = "test_key"
        every { mockSharedPrefs.contains(key) } returns true
        
        // When
        val result = secureStorage.contains(key)
        
        // Then
        assertTrue(result)
        verify { mockSharedPrefs.contains(key) }
    }
    
    @Test
    fun `contains returns false when key does not exist`() = runTest {
        // Given
        val key = "nonexistent_key"
        every { mockSharedPrefs.contains(key) } returns false
        
        // When
        val result = secureStorage.contains(key)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `clear removes all values from encrypted preferences`() = runTest {
        // When
        secureStorage.clear()
        
        // Then
        verify { mockEditor.clear() }
        verify { mockEditor.apply() }
    }
}