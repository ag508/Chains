package com.chain.messaging.core.error

import com.chain.messaging.core.network.NetworkMonitor
import com.chain.messaging.core.network.NetworkQuality
import com.chain.messaging.core.network.NetworkType
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecoveryStrategyTest {
    
    private lateinit var networkMonitor: NetworkMonitor
    
    @Before
    fun setup() {
        networkMonitor = mockk()
        every { networkMonitor.isConnected } returns flowOf(true)
        every { networkMonitor.networkType } returns flowOf(NetworkType.WIFI)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `RetryStrategy returns retry result within max retries`() = runTest {
        // Given
        val strategy = RetryStrategy(maxRetries = 3, baseDelayMs = 10)
        val error = ChainError.NetworkError.ConnectionTimeout()
        val context = ErrorContext("test", "test")
        
        // When
        val result = strategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.Retry, result)
    }
    
    @Test
    fun `RetryStrategy respects retry condition`() = runTest {
        // Given
        val strategy = RetryStrategy(
            maxRetries = 3,
            baseDelayMs = 10,
            retryCondition = { false } // Never retry
        )
        val error = ChainError.NetworkError.ConnectionTimeout()
        val context = ErrorContext("test", "test")
        
        // When
        val result = strategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.NoRecovery, result)
    }
    
    @Test
    fun `NetworkRecoveryStrategy handles no internet error`() = runTest {
        // Given
        coEvery { networkMonitor.isNetworkAvailable() } returns true
        val strategy = NetworkRecoveryStrategy(networkMonitor)
        val error = ChainError.NetworkError.NoInternet()
        val context = ErrorContext("test", "test")
        
        // When
        val result = strategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.Success, result)
        coVerify { networkMonitor.isNetworkAvailable() }
    }
    
    @Test
    fun `NetworkRecoveryStrategy handles connection timeout`() = runTest {
        // Given
        val strategy = NetworkRecoveryStrategy(networkMonitor)
        val error = ChainError.NetworkError.ConnectionTimeout()
        val context = ErrorContext("test", "test")
        
        // When
        val result = strategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.Retry, result)
    }
    
    @Test
    fun `BlockchainRecoveryStrategy handles node unavailable`() = runTest {
        // Given
        val strategy = BlockchainRecoveryStrategy()
        val error = ChainError.BlockchainError.NodeUnavailable()
        val context = ErrorContext("test", "test")
        
        // When
        val result = strategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.Success, result)
    }
    
    @Test
    fun `BlockchainRecoveryStrategy handles transaction failed`() = runTest {
        // Given
        val strategy = BlockchainRecoveryStrategy()
        val error = ChainError.BlockchainError.TransactionFailed("tx123")
        val context = ErrorContext("test", "test")
        
        // When
        val result = strategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.Retry, result)
    }
    
    @Test
    fun `EncryptionRecoveryStrategy handles key exchange failure`() = runTest {
        // Given
        val strategy = EncryptionRecoveryStrategy()
        val error = ChainError.EncryptionError.KeyExchangeFailed("user123")
        val context = ErrorContext("test", "test")
        
        // When
        val result = strategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.Retry, result)
    }
    
    @Test
    fun `EncryptionRecoveryStrategy handles session not found`() = runTest {
        // Given
        val strategy = EncryptionRecoveryStrategy()
        val error = ChainError.EncryptionError.SessionNotFound("user123")
        val context = ErrorContext("test", "test")
        
        // When
        val result = strategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.Retry, result)
    }
    
    @Test
    fun `EncryptionRecoveryStrategy handles decryption failure`() = runTest {
        // Given
        val strategy = EncryptionRecoveryStrategy()
        val error = ChainError.EncryptionError.DecryptionFailed()
        val context = ErrorContext("test", "test")
        
        // When
        val result = strategy.recover(error, context)
        
        // Then
        assertTrue(result is RecoveryResult.Failed)
        assertEquals(error, (result as RecoveryResult.Failed).error)
    }
    
    @Test
    fun `StorageRecoveryStrategy handles database error`() = runTest {
        // Given
        val strategy = StorageRecoveryStrategy()
        val error = ChainError.StorageError.DatabaseError("insert")
        val context = ErrorContext("test", "test")
        
        // When
        val result = strategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.Retry, result)
    }
    
    @Test
    fun `StorageRecoveryStrategy handles cloud storage error`() = runTest {
        // Given
        val strategy = StorageRecoveryStrategy()
        val error = ChainError.StorageError.CloudStorageError("GoogleDrive", "upload")
        val context = ErrorContext("test", "test")
        
        // When
        val result = strategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.Success, result)
    }
    
    @Test
    fun `CallRecoveryStrategy handles connection failed`() = runTest {
        // Given
        val strategy = CallRecoveryStrategy()
        val error = ChainError.CallError.ConnectionFailed()
        val context = ErrorContext("test", "test")
        
        // When
        val result = strategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.Retry, result)
    }
    
    @Test
    fun `CompositeRecoveryStrategy tries multiple strategies`() = runTest {
        // Given
        val strategy1 = mockk<RecoveryStrategy>()
        val strategy2 = mockk<RecoveryStrategy>()
        every { strategy1.recover(any(), any()) } returns RecoveryResult.NoRecovery
        every { strategy2.recover(any(), any()) } returns RecoveryResult.Success
        
        val compositeStrategy = CompositeRecoveryStrategy(listOf(strategy1, strategy2))
        val error = ChainError.NetworkError.ConnectionTimeout()
        val context = ErrorContext("test", "test")
        
        // When
        val result = compositeStrategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.Success, result)
        verify { strategy1.recover(error, context) }
        verify { strategy2.recover(error, context) }
    }
    
    @Test
    fun `CompositeRecoveryStrategy returns NoRecovery when all strategies fail`() = runTest {
        // Given
        val strategy1 = mockk<RecoveryStrategy>()
        val strategy2 = mockk<RecoveryStrategy>()
        every { strategy1.recover(any(), any()) } returns RecoveryResult.NoRecovery
        every { strategy2.recover(any(), any()) } returns RecoveryResult.NoRecovery
        
        val compositeStrategy = CompositeRecoveryStrategy(listOf(strategy1, strategy2))
        val error = ChainError.NetworkError.ConnectionTimeout()
        val context = ErrorContext("test", "test")
        
        // When
        val result = compositeStrategy.recover(error, context)
        
        // Then
        assertEquals(RecoveryResult.NoRecovery, result)
    }
    
    @Test
    fun `RecoveryStrategyFactory creates appropriate strategy for network error`() = runTest {
        // Given
        val error = ChainError.NetworkError.NoInternet()
        
        // When
        val strategy = RecoveryStrategyFactory.createForError(error, networkMonitor)
        
        // Then
        assertTrue(strategy is CompositeRecoveryStrategy)
    }
    
    @Test
    fun `RecoveryStrategyFactory creates appropriate strategy for blockchain error`() = runTest {
        // Given
        val error = ChainError.BlockchainError.NodeUnavailable()
        
        // When
        val strategy = RecoveryStrategyFactory.createForError(error)
        
        // Then
        assertTrue(strategy is CompositeRecoveryStrategy)
    }
    
    @Test
    fun `RecoveryStrategyFactory creates retry strategy for unknown error`() = runTest {
        // Given
        val error = ChainError.SystemError.UnexpectedError()
        
        // When
        val strategy = RecoveryStrategyFactory.createForError(error)
        
        // Then
        assertTrue(strategy is RetryStrategy)
    }
}