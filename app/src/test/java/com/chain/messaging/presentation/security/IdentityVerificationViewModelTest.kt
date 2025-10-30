package com.chain.messaging.presentation.security

import android.graphics.Bitmap
import com.chain.messaging.core.security.IdentityVerificationManager
import com.chain.messaging.core.security.QRCodeScanner
import com.chain.messaging.domain.model.SecurityAlert
import com.chain.messaging.domain.model.VerificationResult
import com.chain.messaging.domain.model.VerificationState
import com.chain.messaging.domain.model.User
import com.chain.messaging.domain.model.UserStatus
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.signal.libsignal.protocol.IdentityKey
import org.signal.libsignal.protocol.ecc.Curve
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class IdentityVerificationViewModelTest {
    
    private lateinit var identityVerificationManager: IdentityVerificationManager
    private lateinit var qrCodeScanner: QRCodeScanner
    private lateinit var viewModel: IdentityVerificationViewModel
    
    private val testDispatcher = StandardTestDispatcher()
    private val testUser = User(
        id = "test-user-id",
        publicKey = "test-public-key",
        displayName = "Test User",
        status = UserStatus.ONLINE
    )
    private val testIdentityKey = IdentityKey(Curve.generateKeyPair().publicKey)
    private val testBitmap = mockk<Bitmap>()
    
    // Mock flows
    private val verificationStateFlow = MutableStateFlow<Map<String, VerificationState>>(emptyMap())
    private val securityAlertsFlow = MutableStateFlow<List<SecurityAlert>>(emptyList())
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        identityVerificationManager = mockk()
        qrCodeScanner = mockk()
        
        // Setup mock flows
        every { identityVerificationManager.verificationState } returns verificationStateFlow
        every { identityVerificationManager.securityAlerts } returns securityAlertsFlow
        every { identityVerificationManager.getSecurityRecommendations() } returns emptyList()
        
        viewModel = IdentityVerificationViewModel(
            identityVerificationManager = identityVerificationManager,
            qrCodeScanner = qrCodeScanner
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `generateQRCode should update qrCodeBitmap on success`() = runTest {
        // Given
        coEvery { identityVerificationManager.generateVerificationQRCode(testUser) } returns Result.success(testBitmap)
        
        // When
        viewModel.generateQRCode(testUser)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val qrCodeBitmap = viewModel.qrCodeBitmap.first()
        assertEquals(testBitmap, qrCodeBitmap)
        
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
    }
    
    @Test
    fun `generateQRCode should update error state on failure`() = runTest {
        // Given
        val errorMessage = "Failed to generate QR code"
        coEvery { identityVerificationManager.generateVerificationQRCode(testUser) } returns Result.failure(Exception(errorMessage))
        
        // When
        viewModel.generateQRCode(testUser)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val qrCodeBitmap = viewModel.qrCodeBitmap.first()
        assertNull(qrCodeBitmap)
        
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoading)
        assertEquals(errorMessage, uiState.error)
    }
    
    @Test
    fun `scanQRCode should update scanResult on successful scan and verification`() = runTest {
        // Given
        val qrData = "chain://verify?user=test&name=Test&key=abc123&ts=1234567890"
        val verificationResult = VerificationResult.Success("test", "Test")
        
        coEvery { qrCodeScanner.scanQRCode(testBitmap) } returns Result.success(qrData)
        coEvery { identityVerificationManager.verifyQRCode(qrData) } returns Result.success(verificationResult)
        
        // When
        viewModel.scanQRCode(testBitmap)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val scanResult = viewModel.scanResult.first()
        assertTrue(scanResult is ScanResult.Success)
        assertEquals("test", (scanResult as ScanResult.Success).userId)
        assertEquals("Test", scanResult.displayName)
        
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
    }
    
    @Test
    fun `scanQRCode should update scanResult with key mismatch`() = runTest {
        // Given
        val qrData = "chain://verify?user=test&name=Test&key=abc123&ts=1234567890"
        val verificationResult = VerificationResult.KeyMismatch("test", "Test")
        
        coEvery { qrCodeScanner.scanQRCode(testBitmap) } returns Result.success(qrData)
        coEvery { identityVerificationManager.verifyQRCode(qrData) } returns Result.success(verificationResult)
        
        // When
        viewModel.scanQRCode(testBitmap)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val scanResult = viewModel.scanResult.first()
        assertTrue(scanResult is ScanResult.KeyMismatch)
        assertEquals("test", (scanResult as ScanResult.KeyMismatch).userId)
        assertEquals("Test", scanResult.displayName)
    }
    
    @Test
    fun `scanQRCode should update error state on scan failure`() = runTest {
        // Given
        val errorMessage = "Failed to scan QR code"
        coEvery { qrCodeScanner.scanQRCode(testBitmap) } returns Result.failure(Exception(errorMessage))
        
        // When
        viewModel.scanQRCode(testBitmap)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val scanResult = viewModel.scanResult.first()
        assertNull(scanResult)
        
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoading)
        assertEquals(errorMessage, uiState.error)
    }
    
    @Test
    fun `generateSafetyNumber should update safetyNumber on success`() = runTest {
        // Given
        val expectedSafetyNumber = "12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345 67890"
        coEvery { identityVerificationManager.generateSafetyNumber("test-user", testIdentityKey) } returns Result.success(expectedSafetyNumber)
        
        // When
        viewModel.generateSafetyNumber("test-user", testIdentityKey)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val safetyNumber = viewModel.safetyNumber.first()
        assertEquals(expectedSafetyNumber, safetyNumber)
        
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
    }
    
    @Test
    fun `generateSafetyNumber should update error state on failure`() = runTest {
        // Given
        val errorMessage = "Failed to generate safety number"
        coEvery { identityVerificationManager.generateSafetyNumber("test-user", testIdentityKey) } returns Result.failure(Exception(errorMessage))
        
        // When
        viewModel.generateSafetyNumber("test-user", testIdentityKey)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val safetyNumber = viewModel.safetyNumber.first()
        assertNull(safetyNumber)
        
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoading)
        assertEquals(errorMessage, uiState.error)
    }
    
    @Test
    fun `verifySafetyNumber should update verificationSuccess on success`() = runTest {
        // Given
        val enteredSafetyNumber = "12345 67890 12345 67890 12345 67890 12345 67890 12345 67890 12345 67890"
        coEvery { identityVerificationManager.verifySafetyNumber("test-user", enteredSafetyNumber, testIdentityKey) } returns Result.success(true)
        
        // When
        viewModel.verifySafetyNumber("test-user", enteredSafetyNumber, testIdentityKey)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoading)
        assertEquals(true, uiState.verificationSuccess)
        assertNull(uiState.error)
    }
    
    @Test
    fun `verifySafetyNumber should update verificationSuccess false for mismatch`() = runTest {
        // Given
        val enteredSafetyNumber = "54321 09876 54321 09876 54321 09876 54321 09876 54321 09876 54321 09876"
        coEvery { identityVerificationManager.verifySafetyNumber("test-user", enteredSafetyNumber, testIdentityKey) } returns Result.success(false)
        
        // When
        viewModel.verifySafetyNumber("test-user", enteredSafetyNumber, testIdentityKey)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.first()
        assertFalse(uiState.isLoading)
        assertEquals(false, uiState.verificationSuccess)
        assertNull(uiState.error)
    }
    
    @Test
    fun `dismissSecurityAlert should call manager`() {
        // Given
        val alertId = "test-alert-id"
        every { identityVerificationManager.dismissSecurityAlert(alertId) } just Runs
        
        // When
        viewModel.dismissSecurityAlert(alertId)
        
        // Then
        verify { identityVerificationManager.dismissSecurityAlert(alertId) }
    }
    
    @Test
    fun `clearAllSecurityAlerts should call manager`() {
        // Given
        every { identityVerificationManager.clearAllSecurityAlerts() } just Runs
        
        // When
        viewModel.clearAllSecurityAlerts()
        
        // Then
        verify { identityVerificationManager.clearAllSecurityAlerts() }
    }
    
    @Test
    fun `clearScanResult should reset scan result`() = runTest {
        // Given - Set a scan result first
        val qrData = "chain://verify?user=test&name=Test&key=abc123&ts=1234567890"
        val verificationResult = VerificationResult.Success("test", "Test")
        coEvery { qrCodeScanner.scanQRCode(testBitmap) } returns Result.success(qrData)
        coEvery { identityVerificationManager.verifyQRCode(qrData) } returns Result.success(verificationResult)
        
        viewModel.scanQRCode(testBitmap)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearScanResult()
        
        // Then
        val scanResult = viewModel.scanResult.first()
        assertNull(scanResult)
    }
    
    @Test
    fun `clearError should reset error state`() = runTest {
        // Given - Set an error first
        coEvery { identityVerificationManager.generateVerificationQRCode(testUser) } returns Result.failure(Exception("Test error"))
        viewModel.generateQRCode(testUser)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearError()
        
        // Then
        val uiState = viewModel.uiState.first()
        assertNull(uiState.error)
    }
    
    @Test
    fun `clearVerificationSuccess should reset verification success state`() = runTest {
        // Given - Set verification success first
        coEvery { identityVerificationManager.verifySafetyNumber("test-user", any(), testIdentityKey) } returns Result.success(true)
        viewModel.verifySafetyNumber("test-user", "test-number", testIdentityKey)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.clearVerificationSuccess()
        
        // Then
        val uiState = viewModel.uiState.first()
        assertNull(uiState.verificationSuccess)
    }
    
    @Test
    fun `viewModel should observe verification state changes`() = runTest {
        // Given
        val verificationState = VerificationState(
            userId = "test-user",
            identityKey = testIdentityKey,
            isVerified = true,
            verifiedAt = System.currentTimeMillis()
        )
        
        // When
        verificationStateFlow.value = mapOf("test-user" to verificationState)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.first()
        assertEquals(1, uiState.verificationStates.size)
        assertEquals(verificationState, uiState.verificationStates["test-user"])
    }
    
    @Test
    fun `viewModel should observe security alerts changes`() = runTest {
        // Given
        val alert = SecurityAlert.SuspiciousActivity("user1", "login", "Failed attempts")
        val recommendation = SecurityRecommendation.VerifyContacts(1)
        
        every { identityVerificationManager.getSecurityRecommendations() } returns listOf(recommendation)
        
        // When
        securityAlertsFlow.value = listOf(alert)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val uiState = viewModel.uiState.first()
        assertEquals(1, uiState.securityAlerts.size)
        assertEquals(alert, uiState.securityAlerts.first())
        assertEquals(1, uiState.securityRecommendations.size)
        assertEquals(recommendation, uiState.securityRecommendations.first())
    }
    
    @Test
    fun `uiState should have correct initial values`() = runTest {
        // When
        val uiState = viewModel.uiState.first()
        
        // Then
        assertFalse(uiState.isLoading)
        assertNull(uiState.error)
        assertNull(uiState.verificationSuccess)
        assertTrue(uiState.verificationStates.isEmpty())
        assertTrue(uiState.securityAlerts.isEmpty())
        assertTrue(uiState.securityRecommendations.isEmpty())
    }
}