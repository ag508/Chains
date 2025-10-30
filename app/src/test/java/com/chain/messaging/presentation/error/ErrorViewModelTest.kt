package com.chain.messaging.presentation.error

import com.chain.messaging.core.error.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ErrorViewModelTest {
    
    private lateinit var errorHandler: ErrorHandler
    private lateinit var errorRecoveryManager: ErrorRecoveryManager
    private lateinit var viewModel: ErrorViewModel
    
    @Before
    fun setup() {
        errorHandler = mockk()
        errorRecoveryManager = mockk(relaxed = true)
        
        every { errorHandler.errorEvents } returns flowOf()
        every { errorRecoveryManager.retrySignals } returns flowOf()
        
        viewModel = ErrorViewModel(errorHandler, errorRecoveryManager)
    }
    
    @After
    fun tearDown() {
        clearAllMocks()
    }
    
    @Test
    fun `initial state is empty`() = runTest {
        // Given & When
        val initialState = viewModel.uiState.value
        
        // Then
        assertEquals(null, initialState.criticalError)
        assertFalse(initialState.showCriticalDialog)
        assertEquals(null, initialState.highPriorityError)
        assertFalse(initialState.showHighPriorityDialog)
        assertTrue(initialState.inlineErrors.isEmpty())
        assertEquals(null, initialState.snackbarError)
        assertEquals(null, initialState.navigationEvent)
    }
    
    @Test
    fun `critical error shows dialog`() = runTest {
        // Given
        val criticalError = ChainError.AuthError.TokenExpired()
        val errorEvent = ErrorEvent(
            error = criticalError,
            context = ErrorContext("Auth", "login"),
            userMessage = "Session expired",
            recoveryActions = listOf(RecoveryAction.ReAuthenticate),
            timestamp = System.currentTimeMillis()
        )
        
        every { errorHandler.errorEvents } returns flowOf(errorEvent)
        
        // When
        val newViewModel = ErrorViewModel(errorHandler, errorRecoveryManager)
        
        // Give some time for the flow to be processed
        kotlinx.coroutines.delay(100)
        
        // Then
        val state = newViewModel.uiState.value
        assertNotNull(state.criticalError)
        assertTrue(state.showCriticalDialog)
        assertEquals(criticalError, state.criticalError?.error)
    }
    
    @Test
    fun `high priority error shows dialog`() = runTest {
        // Given
        val highPriorityError = ChainError.NetworkError.NoInternet()
        val errorEvent = ErrorEvent(
            error = highPriorityError,
            context = ErrorContext("Network", "connect"),
            userMessage = "No internet connection",
            recoveryActions = listOf(RecoveryAction.CheckNetworkSettings),
            timestamp = System.currentTimeMillis()
        )
        
        every { errorHandler.errorEvents } returns flowOf(errorEvent)
        
        // When
        val newViewModel = ErrorViewModel(errorHandler, errorRecoveryManager)
        
        // Give some time for the flow to be processed
        kotlinx.coroutines.delay(100)
        
        // Then
        val state = newViewModel.uiState.value
        assertNotNull(state.highPriorityError)
        assertTrue(state.showHighPriorityDialog)
        assertEquals(highPriorityError, state.highPriorityError?.error)
    }
    
    @Test
    fun `medium priority error shows inline`() = runTest {
        // Given
        val mediumPriorityError = ChainError.StorageError.CloudStorageError("GoogleDrive", "upload")
        val errorEvent = ErrorEvent(
            error = mediumPriorityError,
            context = ErrorContext("Storage", "upload"),
            userMessage = "Cloud storage error",
            recoveryActions = listOf(RecoveryAction.Retry),
            timestamp = System.currentTimeMillis()
        )
        
        every { errorHandler.errorEvents } returns flowOf(errorEvent)
        
        // When
        val newViewModel = ErrorViewModel(errorHandler, errorRecoveryManager)
        
        // Give some time for the flow to be processed
        kotlinx.coroutines.delay(100)
        
        // Then
        val state = newViewModel.uiState.value
        assertTrue(state.inlineErrors.isNotEmpty())
        assertEquals(mediumPriorityError, state.inlineErrors.first().error)
    }
    
    @Test
    fun `low priority error shows snackbar`() = runTest {
        // Given
        val lowPriorityError = ChainError.UIError.InvalidInput("email", "invalid format")
        val errorEvent = ErrorEvent(
            error = lowPriorityError,
            context = ErrorContext("UI", "validation"),
            userMessage = "Invalid email format",
            recoveryActions = emptyList(),
            timestamp = System.currentTimeMillis()
        )
        
        every { errorHandler.errorEvents } returns flowOf(errorEvent)
        
        // When
        val newViewModel = ErrorViewModel(errorHandler, errorRecoveryManager)
        
        // Give some time for the flow to be processed
        kotlinx.coroutines.delay(100)
        
        // Then
        val state = newViewModel.uiState.value
        assertNotNull(state.snackbarError)
        assertEquals(lowPriorityError, state.snackbarError?.error)
    }
    
    @Test
    fun `dismissCriticalError clears critical error state`() = runTest {
        // Given
        val criticalError = ChainError.SystemError.OutOfMemory()
        val errorEvent = ErrorEvent(
            error = criticalError,
            context = ErrorContext("System", "operation"),
            userMessage = "Out of memory",
            recoveryActions = emptyList(),
            timestamp = System.currentTimeMillis()
        )
        
        every { errorHandler.errorEvents } returns flowOf(errorEvent)
        val newViewModel = ErrorViewModel(errorHandler, errorRecoveryManager)
        
        // Give some time for the error to be processed
        kotlinx.coroutines.delay(100)
        
        // When
        newViewModel.dismissCriticalError()
        
        // Then
        val state = newViewModel.uiState.value
        assertEquals(null, state.criticalError)
        assertFalse(state.showCriticalDialog)
    }
    
    @Test
    fun `executeRecoveryAction triggers retry for retry action`() = runTest {
        // Given
        val error = ChainError.NetworkError.ConnectionTimeout()
        val context = ErrorContext("Network", "connect")
        val errorEvent = ErrorEvent(
            error = error,
            context = context,
            userMessage = "Connection timeout",
            recoveryActions = listOf(RecoveryAction.Retry),
            timestamp = System.currentTimeMillis()
        )
        
        // When
        viewModel.executeRecoveryAction(RecoveryAction.Retry, errorEvent)
        
        // Then
        coVerify { errorRecoveryManager.triggerRecovery(context) }
    }
    
    @Test
    fun `executeRecoveryAction sets navigation event for network settings`() = runTest {
        // Given
        val error = ChainError.NetworkError.NoInternet()
        val errorEvent = ErrorEvent(
            error = error,
            context = ErrorContext("Network", "connect"),
            userMessage = "No internet",
            recoveryActions = listOf(RecoveryAction.CheckNetworkSettings),
            timestamp = System.currentTimeMillis()
        )
        
        // When
        viewModel.executeRecoveryAction(RecoveryAction.CheckNetworkSettings, errorEvent)
        
        // Give some time for the action to be processed
        kotlinx.coroutines.delay(100)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(NavigationEvent.OpenNetworkSettings, state.navigationEvent)
    }
    
    @Test
    fun `executeRecoveryAction sets navigation event for re-authentication`() = runTest {
        // Given
        val error = ChainError.AuthError.TokenExpired()
        val errorEvent = ErrorEvent(
            error = error,
            context = ErrorContext("Auth", "login"),
            userMessage = "Token expired",
            recoveryActions = listOf(RecoveryAction.ReAuthenticate),
            timestamp = System.currentTimeMillis()
        )
        
        // When
        viewModel.executeRecoveryAction(RecoveryAction.ReAuthenticate, errorEvent)
        
        // Give some time for the action to be processed
        kotlinx.coroutines.delay(100)
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(NavigationEvent.OpenLogin, state.navigationEvent)
    }
    
    @Test
    fun `clearNavigationEvent clears navigation event`() = runTest {
        // Given
        val error = ChainError.AuthError.TokenExpired()
        val errorEvent = ErrorEvent(
            error = error,
            context = ErrorContext("Auth", "login"),
            userMessage = "Token expired",
            recoveryActions = listOf(RecoveryAction.ReAuthenticate),
            timestamp = System.currentTimeMillis()
        )
        
        viewModel.executeRecoveryAction(RecoveryAction.ReAuthenticate, errorEvent)
        kotlinx.coroutines.delay(100)
        
        // When
        viewModel.clearNavigationEvent()
        
        // Then
        val state = viewModel.uiState.value
        assertEquals(null, state.navigationEvent)
    }
    
    @Test
    fun `dismissInlineError removes error from inline errors`() = runTest {
        // Given
        val error = ChainError.StorageError.CloudStorageError("GoogleDrive", "upload")
        val errorEvent = ErrorEvent(
            error = error,
            context = ErrorContext("Storage", "upload"),
            userMessage = "Cloud storage error",
            recoveryActions = emptyList(),
            timestamp = System.currentTimeMillis()
        )
        
        every { errorHandler.errorEvents } returns flowOf(errorEvent)
        val newViewModel = ErrorViewModel(errorHandler, errorRecoveryManager)
        
        // Give some time for the error to be processed
        kotlinx.coroutines.delay(100)
        
        // When
        newViewModel.dismissInlineError(errorEvent)
        
        // Then
        val state = newViewModel.uiState.value
        assertTrue(state.inlineErrors.isEmpty())
    }
}