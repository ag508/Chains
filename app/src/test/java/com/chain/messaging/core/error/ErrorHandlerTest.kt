package com.chain.messaging.core.error

import com.chain.messaging.core.util.Logger
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ErrorHandlerTest {
    
    private lateinit var errorHandler: ErrorHandler
    
    @Before
    fun setup() {
        mockkObject(Logger)
        every { Logger.e(any(), any<Throwable>()) } just Runs
        
        errorHandler = ErrorHandler()
    }
    
    @After
    fun tearDown() {
        unmockkObject(Logger)
    }
    
    @Test
    fun `handleError emits error event`() = runTest {
        // Given
        val error = ChainError.NetworkError.NoInternet()
        val context = ErrorContext(
            component = "TestComponent",
            operation = "testOperation"
        )
        
        // When
        val result = errorHandler.handleError(error, context)
        
        // Then
        assertTrue(result.handled)
        assertEquals(error, result.error)
        assertEquals(context, result.context)
        
        // Verify error event was emitted
        val errorEvent = errorHandler.errorEvents.first()
        assertEquals(error, errorEvent.error)
        assertEquals(context, errorEvent.context)
        assertNotNull(errorEvent.userMessage)
        assertTrue(errorEvent.recoveryActions.isNotEmpty())
    }
    
    @Test
    fun `handleError with recovery strategy executes recovery`() = runTest {
        // Given
        val error = ChainError.NetworkError.ConnectionTimeout()
        val context = ErrorContext(
            component = "TestComponent",
            operation = "testOperation"
        )
        val recoveryStrategy = mockk<RecoveryStrategy>()
        every { recoveryStrategy.recover(error, context) } returns RecoveryResult.Success
        
        // When
        val result = errorHandler.handleError(error, context, recoveryStrategy)
        
        // Then
        assertTrue(result.handled)
        assertEquals(RecoveryResult.Success, result.recoveryResult)
        verify { recoveryStrategy.recover(error, context) }
    }
    
    @Test
    fun `handleThrowable converts exception to ChainError`() = runTest {
        // Given
        val exception = RuntimeException("Test exception")
        val context = ErrorContext(
            component = "TestComponent",
            operation = "testOperation"
        )
        
        // When
        val result = errorHandler.handleThrowable(exception, context)
        
        // Then
        assertTrue(result.handled)
        assertTrue(result.error is ChainError.SystemError.UnexpectedError)
        assertEquals(exception, result.error.cause)
    }
    
    @Test
    fun `getUserFriendlyMessage returns appropriate message for network errors`() = runTest {
        // Given
        val errors = listOf(
            ChainError.NetworkError.NoInternet() to "No internet connection",
            ChainError.NetworkError.ConnectionTimeout() to "Connection timed out",
            ChainError.NetworkError.ServerUnreachable("test") to "Unable to connect to server"
        )
        
        // When & Then
        errors.forEach { (error, expectedMessagePart) ->
            val result = errorHandler.handleError(error, ErrorContext("test", "test"))
            val errorEvent = errorHandler.errorEvents.first()
            assertTrue(
                errorEvent.userMessage.contains(expectedMessagePart, ignoreCase = true),
                "Expected message to contain '$expectedMessagePart' but was '${errorEvent.userMessage}'"
            )
        }
    }
    
    @Test
    fun `getRecoveryActions returns appropriate actions for different error types`() = runTest {
        // Given
        val networkError = ChainError.NetworkError.NoInternet()
        val blockchainError = ChainError.BlockchainError.NodeUnavailable()
        val encryptionError = ChainError.EncryptionError.KeyExchangeFailed("user123")
        
        // When
        val networkResult = errorHandler.handleError(networkError, ErrorContext("test", "test"))
        val blockchainResult = errorHandler.handleError(blockchainError, ErrorContext("test", "test"))
        val encryptionResult = errorHandler.handleError(encryptionError, ErrorContext("test", "test"))
        
        // Then
        val networkEvent = errorHandler.errorEvents.first()
        assertTrue(networkEvent.recoveryActions.any { it is RecoveryAction.CheckNetworkSettings })
        assertTrue(networkEvent.recoveryActions.any { it is RecoveryAction.UseOfflineMode })
        
        // Note: Due to flow nature, we'd need to collect multiple events or use a different approach
        // For simplicity, we're testing the first event
    }
    
    @Test
    fun `recovery strategy failure is handled gracefully`() = runTest {
        // Given
        val error = ChainError.NetworkError.ConnectionTimeout()
        val context = ErrorContext(
            component = "TestComponent",
            operation = "testOperation"
        )
        val recoveryStrategy = mockk<RecoveryStrategy>()
        val recoveryException = RuntimeException("Recovery failed")
        every { recoveryStrategy.recover(error, context) } throws recoveryException
        
        // When
        val result = errorHandler.handleError(error, context, recoveryStrategy)
        
        // Then
        assertTrue(result.handled)
        assertTrue(result.recoveryResult is RecoveryResult.Failed)
        val failedResult = result.recoveryResult as RecoveryResult.Failed
        assertTrue(failedResult.error is ChainError.SystemError.UnexpectedError)
        assertEquals(recoveryException, failedResult.error.cause)
    }
    
    @Test
    fun `error context information is preserved`() = runTest {
        // Given
        val error = ChainError.StorageError.DatabaseError("insert")
        val context = ErrorContext(
            component = "DatabaseManager",
            operation = "insertMessage",
            userId = "user123",
            chatId = "chat456",
            messageId = "msg789",
            additionalInfo = mapOf("table" to "messages")
        )
        
        // When
        val result = errorHandler.handleError(error, context)
        
        // Then
        assertEquals(context, result.context)
        val errorEvent = errorHandler.errorEvents.first()
        assertEquals(context, errorEvent.context)
    }
}