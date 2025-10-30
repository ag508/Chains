package com.chain.messaging.core.deployment

import com.chain.messaging.core.util.Logger
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeploymentManagerTest {

    private lateinit var performanceOptimizer: PerformanceOptimizer
    private lateinit var buildConfigManager: BuildConfigManager
    private lateinit var codeCleanupManager: CodeCleanupManager
    private lateinit var documentationGenerator: DocumentationGenerator
    private lateinit var appStoreManager: AppStoreManager
    private lateinit var logger: Logger
    private lateinit var deploymentManager: DeploymentManager

    @BeforeEach
    fun setup() {
        performanceOptimizer = mockk(relaxed = true)
        buildConfigManager = mockk(relaxed = true)
        codeCleanupManager = mockk(relaxed = true)
        documentationGenerator = mockk(relaxed = true)
        appStoreManager = mockk(relaxed = true)
        logger = mockk(relaxed = true)

        deploymentManager = DeploymentManager(
            performanceOptimizer,
            buildConfigManager,
            codeCleanupManager,
            documentationGenerator,
            appStoreManager,
            logger
        )
    }

    @Test
    fun `prepareForDeployment should execute all deployment tasks`() = runTest {
        // Given
        every { buildConfigManager.getDeploymentInfo() } returns DeploymentInfo(
            versionName = "1.0.0",
            versionCode = 1,
            buildType = "release",
            isDebug = false,
            applicationId = "com.chain.messaging"
        )

        every { codeCleanupManager.generateCleanupReport() } returns CleanupReport(
            debugCodeRemoved = true,
            importsOptimized = true,
            codeQualityPassed = true,
            securityCompliancePassed = true,
            timestamp = System.currentTimeMillis()
        )

        every { documentationGenerator.generateDocumentationSummary() } returns DocumentationSummary(
            userGuideGenerated = true,
            apiDocumentationGenerated = true,
            deploymentGuideGenerated = true,
            troubleshootingGuideGenerated = true,
            privacyPolicyGenerated = true,
            termsOfServiceGenerated = true,
            timestamp = System.currentTimeMillis()
        )

        every { appStoreManager.generateAppStoreAssets() } returns AppStoreAssets(
            shortDescription = "Test description",
            fullDescription = "Test full description",
            screenshots = 8,
            featureGraphics = 5,
            keywords = 25,
            releaseNotesGenerated = true,
            timestamp = System.currentTimeMillis()
        )

        // When
        deploymentManager.prepareForDeployment()

        // Then
        verify { buildConfigManager.configureForDeployment() }
        verify { performanceOptimizer.optimizeForDeployment() }
        verify { codeCleanupManager.performCodeCleanup() }
        verify { documentationGenerator.generateDocumentation() }
        verify { appStoreManager.prepareAppStoreListing() }
        verify { logger.i("DeploymentManager", "Deployment preparation completed successfully") }
    }

    @Test
    fun `prepareForDeployment should handle errors gracefully`() = runTest {
        // Given
        every { performanceOptimizer.optimizeForDeployment() } throws RuntimeException("Optimization failed")

        // When & Then
        assertThrows<DeploymentException> {
            deploymentManager.prepareForDeployment()
        }

        verify { logger.e("DeploymentManager", "Error during deployment preparation", any()) }
    }

    @Test
    fun `validateDeploymentReadiness should return passed status when all validations pass`() {
        // Given
        every { buildConfigManager.getDeploymentInfo() } returns DeploymentInfo(
            versionName = "1.0.0",
            versionCode = 1,
            buildType = "release",
            isDebug = false,
            applicationId = "com.chain.messaging"
        )

        every { codeCleanupManager.generateCleanupReport() } returns CleanupReport(
            debugCodeRemoved = true,
            importsOptimized = true,
            codeQualityPassed = true,
            securityCompliancePassed = true,
            timestamp = System.currentTimeMillis()
        )

        every { documentationGenerator.generateDocumentationSummary() } returns DocumentationSummary(
            userGuideGenerated = true,
            apiDocumentationGenerated = true,
            deploymentGuideGenerated = true,
            troubleshootingGuideGenerated = true,
            privacyPolicyGenerated = true,
            termsOfServiceGenerated = true,
            timestamp = System.currentTimeMillis()
        )

        every { appStoreManager.generateAppStoreAssets() } returns AppStoreAssets(
            shortDescription = "Test description",
            fullDescription = "Test full description",
            screenshots = 8,
            featureGraphics = 5,
            keywords = 25,
            releaseNotesGenerated = true,
            timestamp = System.currentTimeMillis()
        )

        // When
        val validation = deploymentManager.validateDeploymentReadiness()

        // Then
        assertEquals(ValidationStatus.PASSED, validation.status)
        assertTrue(validation.validations.all { it.passed })
    }

    @Test
    fun `validateDeploymentReadiness should return failed status when validations fail`() {
        // Given
        every { buildConfigManager.getDeploymentInfo() } returns DeploymentInfo(
            versionName = "",
            versionCode = 0,
            buildType = "release",
            isDebug = false,
            applicationId = ""
        )

        every { codeCleanupManager.generateCleanupReport() } returns CleanupReport(
            debugCodeRemoved = false,
            importsOptimized = false,
            codeQualityPassed = false,
            securityCompliancePassed = false,
            timestamp = System.currentTimeMillis()
        )

        every { documentationGenerator.generateDocumentationSummary() } returns DocumentationSummary(
            userGuideGenerated = false,
            apiDocumentationGenerated = false,
            deploymentGuideGenerated = false,
            troubleshootingGuideGenerated = false,
            privacyPolicyGenerated = false,
            termsOfServiceGenerated = false,
            timestamp = System.currentTimeMillis()
        )

        every { appStoreManager.generateAppStoreAssets() } returns AppStoreAssets(
            shortDescription = "",
            fullDescription = "",
            screenshots = 0,
            featureGraphics = 0,
            keywords = 0,
            releaseNotesGenerated = false,
            timestamp = System.currentTimeMillis()
        )

        // When
        val validation = deploymentManager.validateDeploymentReadiness()

        // Then
        assertEquals(ValidationStatus.FAILED, validation.status)
        assertTrue(validation.validations.any { !it.passed })
    }

    @Test
    fun `validateDeploymentReadiness should handle validation errors`() {
        // Given
        every { buildConfigManager.getDeploymentInfo() } throws RuntimeException("Config error")

        // When
        val validation = deploymentManager.validateDeploymentReadiness()

        // Then
        assertEquals(ValidationStatus.ERROR, validation.status)
        assertTrue(validation.validations.any { it.name == "Validation Error" && !it.passed })
    }
}