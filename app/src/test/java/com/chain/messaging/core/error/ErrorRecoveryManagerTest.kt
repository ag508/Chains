package com.chain.messaging.core.error

import com.chain.messaging.core.network.NetworkMonitor
import com.chain.messaging.core.network.NetworkType
import io.mockk.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ErrorRecoveryManagerTest {
    
    private lateinit var errorHandler: ErrorHandler
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var errorRecoveryManager: ErrorRecoveryManager
    
    @Before
    fun setup() {
        errorHandler = mockk(relaxed = true)
        networkMonitor = mockk()
        
        every { networkMonitor.isConnected } returns flowOf(true)
        every { networkMonitor.networkType } returns flowOf(NetworkType.WIFI)
        coEvery { networkMonitor.isNetworkAvailable() } returns true
        
        every { errorHandler.errorEvents } returns flowOf()
        
        errorRecoveryManager = ErrorRecoveryManager(errorHandler, networkMonitor)
    }
    
    @After
    fun tearDown() {
        errorRecoveryManager.cleanup()
        clearAllMocks()
    }
    
    @Test
    fun `withErrorHandling returns success for successful operation`() = runTest {
        // Given
        val context = ErrorContext("TestComponent", "testOperation")
        val expectedResult = "success"
        
        // When
        val result = errorRecoveryManager.withErrorHandling(context) {
            expectedResult
        }
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }
    
    @Test
    fun `withErrorHandling handles exceptions and triggers recovery`() = runTest {
        // Given
        val context = ErrorContext("TestComponent", "testOperation")
        val exception = RuntimeException("Test exception")
        
        // When
        val result = errorRecoveryManager.withErrorHandling(context) {
            throw exception
        }
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ChainError.SystemError.UnexpectedError)
    }
    
    @Test
    fun `retryOperation succeeds on first attempt`() = runTest {
        // Given
        val context = ErrorContext("TestComponent", "testOperation")
        val expectedResult = "success"
        
        // When
        val result = errorRecoveryManager.retryOperation(context, maxRetries = 3) {
            expectedResult
        }
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
    }
    
    @Test
    fun `retryOperation succeeds after retries`() = runTest {
        // Given
        val context = ErrorContext("TestComponent", "testOperation")
        val expectedResult = "success"
        var attemptCount = 0
        
        // When
        val result = errorRecoveryManager.retryOperation(context, maxRetries = 3) {
            attemptCount++
            if (attemptCount < 3) {
                throw RuntimeException("Attempt $attemptCount failed")
            }
            expectedResult
        }
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
        assertEquals(3, attemptCount)
    }
    
    @Test
    fun `retryOperation fails after max retries`() = runTest {
        // Given
        val context = ErrorContext("TestComponent", "testOperation")
        val exception = RuntimeException("Always fails")
        
        // When
        val result = errorRecoveryManager.retryOperation(context, maxRetries = 2) {
            throw exception
        }
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ChainError.SystemError.UnexpectedError)
    }
    
    @Test
    fun `triggerRecovery emits retry signal`() = runTest {
        // Given
        val context = ErrorContext("TestComponent", "testOperation")
        val retrySignals = mutableListOf<RetrySignal>()
        
        // Collect retry signals
        val job = kotlinx.coroutines.launch {
            errorRecoveryManager.retrySignals.collect { signal ->
                retrySignals.add(signal)
            }
        }
        
        // When
        errorRecoveryManager.triggerRecovery(context)
        
        // Give some time for the signal to be emitted
        kotlinx.coroutines.delay(100)
        job.cancel()
        
        // Then
        assertEquals(1, retrySignals.size)
        assertEquals(context, retrySignals.first().context)
    }
    
    @Test
    fun `startNetworkRecoveryMonitoring triggers recovery on connectivity restore`() = runTest {
        // Given
        val networkConnectivity = MutableSharedFlow<Boolean>()
        every { networkMonitor.isConnected } returns networkConnectivity.asSharedFlow()
        
        val retrySignals = mutableListOf<RetrySignal>()
        val job = kotlinx.coroutines.launch {
            errorRecoveryManager.retrySignals.collect { signal ->
                retrySignals.add(signal)
            }
        }
        
        // When
        errorRecoveryManager.startNetworkRecoveryMonitoring()
        networkConnectivity.emit(false) // Network lost
        networkConnectivity.emit(true)  // Network restored
        
        // Give some time for the signal to be emitted
        kotlinx.coroutines.delay(100)
        job.cancel()
        
        // Then
        assertTrue(retrySignals.isNotEmpty())
        val networkRecoverySignal = retrySignals.find { 
            it.context.component == "NetworkRecovery" && 
            it.context.operation == "ConnectivityRestored" 
        }
        assertNotNull(networkRecoverySignal)
    }
    
    @Test
    fun `cancelAllRecoveries cancels active recovery operations`() = runTest {
        // Given
        val context = ErrorContext("TestComponent", "testOperation")
        
        // Start a recovery operation that would normally take time
        val job = kotlinx.coroutines.launch {
            errorRecoveryManager.retryOperation(context, maxRetries = 5) {
                kotlinx.coroutines.delay(1000) // Long operation
                "success"
            }
        }
        
        // Give some time for the operation to start
        kotlinx.coroutines.delay(50)
        
        // When
        errorRecoveryManager.cancelAllRecoveries()
        
        // Then
        val activeRecoveries = errorRecoveryManager.getActiveRecoveries()
        assertTrue(activeRecoveries.isEmpty() || activeRecoveries.values.all { !it })
        
        job.cancel()
    }
    
    @Test
    fun `getActiveRecoveries returns current recovery status`() = runTest {
        // Given
        val context = ErrorContext("TestComponent", "testOperation")
        
        // When - no active recoveries initially
        val initialStatus = errorRecoveryManager.getActiveRecoveries()
        
        // Then
        assertTrue(initialStatus.isEmpty())
        
        // Note: Testing active recoveries would require more complex setup
        // as the recovery operations are internal to the manager
    }
    
    @Test
    fun `cleanup cancels all operations`() = runTest {
        // Given
        val context = ErrorContext("TestComponent", "testOperation")
        
        // Start some operations
        val job1 = kotlinx.coroutines.launch {
            errorRecoveryManager.retryOperation(context) {
                kotlinx.coroutines.delay(1000)
                "success"
            }
        }
        
        val job2 = kotlinx.coroutines.launch {
            errorRecoveryManager.withErrorHandling(context) {
                kotlinx.coroutines.delay(1000)
                "success"
            }
        }
        
        // Give some time for operations to start
        kotlinx.coroutines.delay(50)
        
        // When
        errorRecoveryManager.cleanup()
        
        // Then
        val activeRecoveries = errorRecoveryManager.getActiveRecoveries()
        assertTrue(activeRecoveries.isEmpty())
        
        job1.cancel()
        job2.cancel()
    }
}