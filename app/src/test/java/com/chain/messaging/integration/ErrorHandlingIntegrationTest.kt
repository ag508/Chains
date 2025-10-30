package com.chain.messaging.integration

import com.chain.messaging.core.error.*
import com.chain.messaging.core.network.NetworkMonitor
import com.chain.messaging.core.network.NetworkType
import com.chain.messaging.presentation.error.ErrorViewModel
import io.mockk.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test for the complete error handling system
 */
class ErrorHandlingIntegrationTest {
    
    private lateinit var errorHandler: ErrorHandler
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var errorRecoveryManager: ErrorRecoveryManager
    private lateinit var errorViewModel: ErrorViewModel
    private lateinit var blockchainErrorHandler: BlockchainErrorHandler
    private lateinit var networkErrorHandler: NetworkErrorHandler
    
    @Before
    fun setup() {
        // Create real instances for integration testing
        errorHandler = ErrorHandler()
        
        // Mock network monitor
        networkMonitor = mockk()
        every { networkMonitor.isConnected } returns flowOf(true)
        every { networkMonitor.networkType } returns flowOf(NetworkType.WIFI)
        coEvery { networkMonitor.isNetworkAvailable() } returns true
        
        // Create real error recovery manager
        errorRecoveryManager = ErrorRecoveryManager(errorHandler, networkMonitor)
        
        // Create error view model
        errorViewModel = ErrorViewModel(errorHandler, errorRecoveryManager)
        
        // Create component error handlers
        blockchainErrorHandler = BlockchainErrorHandler(errorHandler, errorRecoveryManager)
        networkErrorHandler = NetworkErrorHandler(errorHandler, errorRecoveryManager, networkMonitor)
    }
    
    @After
    fun tearDown() {
        errorRecoveryManager.cleanup()
        clearAllMocks()
    }
    
    @Test
    fun `end-to-end error handling flow with blockchain error`() = runTest {
        // Given
        val errorEvents = mutableListOf<ErrorEvent>()
        val uiStates = mutableListOf<com.chain.messaging.presentation.error.ErrorUiState>()
        
        // Collect error events
        val errorJob = kotlinx.coroutines.launch {
            errorHandler.errorEvents.collect { event ->
                errorEvents.add(event)
            }
        }
        
        // Collect UI states
        val uiJob = kotlinx.coroutines.launch {
            errorViewModel.uiState.collect { state ->
                uiStates.add(state)
            }
        }
        
        // When - simulate a blockchain operation failure
        val result = blockchainErrorHandler.handleBlockchainOperation("sendTransaction") {
            throw RuntimeException("Connection timeout occurred")
        }
        
        // Give some time for events to propagate
        kotlinx.coroutines.delay(200)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ChainError.BlockchainError.NodeUnavailable)
        
        // Verify error event was emitted
        assertTrue(errorEvents.isNotEmpty())
        val errorEvent = errorEvents.first()
        assertTrue(errorEvent.error is ChainError.BlockchainError.NodeUnavailable)
        assertEquals("BlockchainManager", errorEvent.context.component)
        assertEquals("sendTransaction", errorEvent.context.operation)
        assertTrue(errorEvent.userMessage.contains("blockchain", ignoreCase = true))
        assertTrue(errorEvent.recoveryActions.isNotEmpty())
        
        // Verify UI state was updated
        assertTrue(uiStates.size > 1) // Initial state + updated state
        val latestUiState = uiStates.last()
        // Blockchain errors are typically high priority
        assertNotNull(latestUiState.highPriorityError)
        assertTrue(latestUiState.showHighPriorityDialog)
        
        errorJob.cancel()
        uiJob.cancel()
    }
    
    @Test
    fun `network connectivity recovery triggers retry`() = runTest {
        // Given
        val networkConnectivity = MutableSharedFlow<Boolean>()
        every { networkMonitor.isConnected } returns networkConnectivity.asSharedFlow()
        
        val retrySignals = mutableListOf<RetrySignal>()
        val retryJob = kotlinx.coroutines.launch {
            errorRecoveryManager.retrySignals.collect { signal ->
                retrySignals.add(signal)
            }
        }
        
        // Start network recovery monitoring
        errorRecoveryManager.startNetworkRecoveryMonitoring()
        
        // When - simulate network connectivity loss and restoration
        networkConnectivity.emit(false) // Network lost
        kotlinx.coroutines.delay(50)
        networkConnectivity.emit(true)  // Network restored
        kotlinx.coroutines.delay(100)
        
        // Then
        assertTrue(retrySignals.isNotEmpty())
        val networkRecoverySignal = retrySignals.find { 
            it.context.component == "NetworkRecovery" && 
            it.context.operation == "ConnectivityRestored" 
        }
        assertNotNull(networkRecoverySignal)
        
        retryJob.cancel()
    }
    
    @Test
    fun `network error when offline shows appropriate error`() = runTest {
        // Given
        coEvery { networkMonitor.isNetworkAvailable() } returns false
        
        val errorEvents = mutableListOf<ErrorEvent>()
        val errorJob = kotlinx.coroutines.launch {
            errorHandler.errorEvents.collect { event ->
                errorEvents.add(event)
            }
        }
        
        // When
        val result = networkErrorHandler.handleNetworkOperation("fetchData") {
            "success"
        }
        
        kotlinx.coroutines.delay(100)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ChainError.NetworkError.NoInternet)
        
        // Verify error event
        assertTrue(errorEvents.isNotEmpty())
        val errorEvent = errorEvents.first()
        assertTrue(errorEvent.error is ChainError.NetworkError.NoInternet)
        assertTrue(errorEvent.userMessage.contains("internet", ignoreCase = true))
        assertTrue(errorEvent.recoveryActions.any { it is RecoveryAction.CheckNetworkSettings })
        
        errorJob.cancel()
    }
    
    @Test
    fun `retry operation succeeds after initial failure`() = runTest {
        // Given
        var attemptCount = 0
        val expectedResult = "success"
        
        // When
        val result = errorRecoveryManager.retryOperation(
            context = ErrorContext("TestComponent", "testOperation"),
            maxRetries = 3
        ) {
            attemptCount++
            if (attemptCount < 2) {
                throw RuntimeException("Temporary failure")
            }
            expectedResult
        }
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedResult, result.getOrNull())
        assertEquals(2, attemptCount)
    }
    
    @Test
    fun `error recovery action execution updates UI state`() = runTest {
        // Given
        val error = ChainError.AuthError.TokenExpired()
        val errorEvent = ErrorEvent(
            error = error,
            context = ErrorContext("Auth", "login"),
            userMessage = "Session expired",
            recoveryActions = listOf(RecoveryAction.ReAuthenticate),
            timestamp = System.currentTimeMillis()
        )
        
        val uiStates = mutableListOf<com.chain.messaging.presentation.error.ErrorUiState>()
        val uiJob = kotlinx.coroutines.launch {
            errorViewModel.uiState.collect { state ->
                uiStates.add(state)
            }
        }
        
        // When
        errorViewModel.executeRecoveryAction(RecoveryAction.ReAuthenticate, errorEvent)
        kotlinx.coroutines.delay(100)
        
        // Then
        val latestState = uiStates.last()
        assertEquals(
            com.chain.messaging.presentation.error.NavigationEvent.OpenLogin, 
            latestState.navigationEvent
        )
        
        uiJob.cancel()
    }
    
    @Test
    fun `multiple concurrent errors are handled correctly`() = runTest {
        // Given
        val errorEvents = mutableListOf<ErrorEvent>()
        val errorJob = kotlinx.coroutines.launch {
            errorHandler.errorEvents.collect { event ->
                errorEvents.add(event)
            }
        }
        
        // When - trigger multiple errors concurrently
        val jobs = listOf(
            kotlinx.coroutines.launch {
                blockchainErrorHandler.handleBlockchainOperation("op1") {
                    throw RuntimeException("timeout")
                }
            },
            kotlinx.coroutines.launch {
                blockchainErrorHandler.handleBlockchainOperation("op2") {
                    throw RuntimeException("transaction failed")
                }
            },
            kotlinx.coroutines.launch {
                networkErrorHandler.handleNetworkOperation("op3") {
                    throw RuntimeException("unreachable")
                }
            }
        )
        
        jobs.forEach { it.join() }
        kotlinx.coroutines.delay(200)
        
        // Then
        assertTrue(errorEvents.size >= 3)
        
        // Verify different error types were handled
        val blockchainErrors = errorEvents.filter { it.error is ChainError.BlockchainError }
        val networkErrors = errorEvents.filter { it.error is ChainError.NetworkError }
        
        assertTrue(blockchainErrors.size >= 2)
        assertTrue(networkErrors.size >= 1)
        
        errorJob.cancel()
    }
    
    @Test
    fun `error severity determines UI presentation`() = runTest {
        // Given
        val criticalError = ChainError.SystemError.OutOfMemory()
        val highPriorityError = ChainError.NetworkError.NoInternet()
        val mediumPriorityError = ChainError.StorageError.CloudStorageError("service", "op")
        val lowPriorityError = ChainError.UIError.InvalidInput("field", "reason")
        
        val uiStates = mutableListOf<com.chain.messaging.presentation.error.ErrorUiState>()
        val uiJob = kotlinx.coroutines.launch {
            errorViewModel.uiState.collect { state ->
                uiStates.add(state)
            }
        }
        
        // When - trigger errors of different severities
        errorHandler.handleError(criticalError, ErrorContext("System", "op"))
        kotlinx.coroutines.delay(50)
        
        errorHandler.handleError(highPriorityError, ErrorContext("Network", "op"))
        kotlinx.coroutines.delay(50)
        
        errorHandler.handleError(mediumPriorityError, ErrorContext("Storage", "op"))
        kotlinx.coroutines.delay(50)
        
        errorHandler.handleError(lowPriorityError, ErrorContext("UI", "op"))
        kotlinx.coroutines.delay(100)
        
        // Then
        val finalState = uiStates.last()
        
        // Critical error should show dialog
        assertNotNull(finalState.criticalError)
        assertTrue(finalState.showCriticalDialog)
        
        // Should have inline errors (medium priority)
        assertTrue(finalState.inlineErrors.isNotEmpty())
        
        // Should have snackbar error (low priority)
        assertNotNull(finalState.snackbarError)
        
        uiJob.cancel()
    }
}