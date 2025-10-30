package com.chain.messaging.core.error

import com.chain.messaging.core.network.NetworkMonitor
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComponentErrorHandlersTest {
    
    private lateinit var errorHandler: ErrorHandler
    private lateinit var errorRecoveryManager: ErrorRecoveryManager
    private lateinit var networkMonitor: NetworkMonitor
    
    @Before
    fun setup() {
        errorHandler = mockk(relaxed = true)
        errorRecoveryManager = mockk(relaxed = true)
        networkMonitor = mockk()
        
        coEvery { networkMonitor.isNetworkAvailable() } returns true
        coEvery { errorRecoveryManager.withErrorHandling(any(), any()) } answers {
            val operation = secondArg<suspend () -> Any>()
            try {
                Result.success(operation())
            } catch (e: Exception) {
                Result.failure(e.toChainError())
            }
        }
        coEvery { errorRecoveryManager.retryOperation(any(), any(), any()) } answers {
            val operation = thirdArg<suspend () -> Any>()
            try {
                Result.success(operation())
            } catch (e: Exception) {
                Result.failure(e.toChainError())
            }
        }
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `BlockchainErrorHandler handles successful operation`() = runTest {
        // Given
        val handler = BlockchainErrorHandler(errorHandler, errorRecoveryManager)
        val expectedResult = "success"
        
        // When
        val result = handler.handleBlockchainOperation("sendTransaction") {
            expectedResult
        }
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }
    
    @Test
    fun `BlockchainErrorHandler converts timeout exception to NodeUnavailable`() = runTest {
        // Given
        val handler = BlockchainErrorHandler(errorHandler, errorRecoveryManager)
        val timeoutException = RuntimeException("Connection timeout occurred")
        
        coEvery { errorRecoveryManager.withErrorHandling(any(), any()) } answers {
            val operation = secondArg<suspend () -> Any>()
            try {
                operation()
                Result.success("success")
            } catch (e: Exception) {
                val chainError = when {
                    e.message?.contains("timeout") == true -> 
                        ChainError.BlockchainError.NodeUnavailable(e)
                    else -> e.toChainError()
                }
                Result.failure(chainError)
            }
        }
        
        // When
        val result = handler.handleBlockchainOperation("sendTransaction") {
            throw timeoutException
        }
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is ChainError.BlockchainError.NodeUnavailable)
        assertEquals(timeoutException, error?.cause)
    }
    
    @Test
    fun `BlockchainErrorHandler converts transaction failed exception`() = runTest {
        // Given
        val handler = BlockchainErrorHandler(errorHandler, errorRecoveryManager)
        val transactionException = RuntimeException("transaction failed")
        
        coEvery { errorRecoveryManager.withErrorHandling(any(), any()) } answers {
            val operation = secondArg<suspend () -> Any>()
            try {
                operation()
                Result.success("success")
            } catch (e: Exception) {
                val chainError = when {
                    e.message?.contains("transaction failed") == true -> 
                        ChainError.BlockchainError.TransactionFailed(null, e)
                    else -> e.toChainError()
                }
                Result.failure(chainError)
            }
        }
        
        // When
        val result = handler.handleBlockchainOperation("sendTransaction") {
            throw transactionException
        }
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is ChainError.BlockchainError.TransactionFailed)
    }
    
    @Test
    fun `EncryptionErrorHandler handles key exchange failure`() = runTest {
        // Given
        val handler = EncryptionErrorHandler(errorHandler, errorRecoveryManager)
        val keyExchangeException = RuntimeException("key exchange failed")
        val userId = "user123"
        
        coEvery { errorRecoveryManager.withErrorHandling(any(), any()) } answers {
            val operation = secondArg<suspend () -> Any>()
            try {
                operation()
                Result.success("success")
            } catch (e: Exception) {
                val chainError = when {
                    e.message?.contains("key exchange") == true -> 
                        ChainError.EncryptionError.KeyExchangeFailed(userId, e)
                    else -> e.toChainError()
                }
                Result.failure(chainError)
            }
        }
        
        // When
        val result = handler.handleEncryptionOperation("establishSession", userId) {
            throw keyExchangeException
        }
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is ChainError.EncryptionError.KeyExchangeFailed)
        assertEquals(userId, (error as ChainError.EncryptionError.KeyExchangeFailed).message.contains(userId))
    }
    
    @Test
    fun `NetworkErrorHandler checks network availability first`() = runTest {
        // Given
        val handler = NetworkErrorHandler(errorHandler, errorRecoveryManager, networkMonitor)
        coEvery { networkMonitor.isNetworkAvailable() } returns false
        
        // When
        val result = handler.handleNetworkOperation("fetchData") {
            "success"
        }
        
        // Then
        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is ChainError.NetworkError.NoInternet)
        coVerify { networkMonitor.isNetworkAvailable() }
        coVerify { errorHandler.handleError(any(), any()) }
    }
    
    @Test
    fun `NetworkErrorHandler proceeds when network is available`() = runTest {
        // Given
        val handler = NetworkErrorHandler(errorHandler, errorRecoveryManager, networkMonitor)
        coEvery { networkMonitor.isNetworkAvailable() } returns true
        val expectedResult = "success"
        
        // When
        val result = handler.handleNetworkOperation("fetchData") {
            expectedResult
        }
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
        coVerify { networkMonitor.isNetworkAvailable() }
    }
    
    @Test
    fun `P2PErrorHandler includes peer ID in context`() = runTest {
        // Given
        val handler = P2PErrorHandler(errorHandler, errorRecoveryManager)
        val peerId = "peer123"
        val expectedResult = "success"
        
        // When
        val result = handler.handleP2POperation("connectToPeer", peerId) {
            expectedResult
        }
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
        coVerify { 
            errorRecoveryManager.withErrorHandling(
                match { context -> 
                    context.component == "P2PManager" && 
                    context.operation == "connectToPeer" &&
                    context.additionalInfo["peerId"] == peerId
                },
                any()
            )
        }
    }
    
    @Test
    fun `WebRTCErrorHandler includes call ID in context`() = runTest {
        // Given
        val handler = WebRTCErrorHandler(errorHandler, errorRecoveryManager)
        val callId = "call123"
        val expectedResult = "success"
        
        // When
        val result = handler.handleWebRTCOperation("initiateCall", callId) {
            expectedResult
        }
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
        coVerify { 
            errorRecoveryManager.withErrorHandling(
                match { context -> 
                    context.component == "WebRTCManager" && 
                    context.operation == "initiateCall" &&
                    context.additionalInfo["callId"] == callId
                },
                any()
            )
        }
    }
    
    @Test
    fun `StorageErrorHandler includes storage type in context`() = runTest {
        // Given
        val handler = StorageErrorHandler(errorHandler, errorRecoveryManager)
        val storageType = "cloud"
        val expectedResult = "success"
        
        // When
        val result = handler.handleStorageOperation("uploadFile", storageType) {
            expectedResult
        }
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
        coVerify { 
            errorRecoveryManager.withErrorHandling(
                match { context -> 
                    context.component == "StorageManager" && 
                    context.operation == "uploadFile" &&
                    context.additionalInfo["storageType"] == storageType
                },
                any()
            )
        }
    }
    
    @Test
    fun `MessagingErrorHandler includes message context`() = runTest {
        // Given
        val handler = MessagingErrorHandler(errorHandler, errorRecoveryManager)
        val chatId = "chat123"
        val messageId = "msg456"
        val expectedResult = "success"
        
        // When
        val result = handler.handleMessagingOperation("sendMessage", chatId, messageId) {
            expectedResult
        }
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
        coVerify { 
            errorRecoveryManager.withErrorHandling(
                match { context -> 
                    context.component == "MessagingService" && 
                    context.operation == "sendMessage" &&
                    context.chatId == chatId &&
                    context.messageId == messageId
                },
                any()
            )
        }
    }
}