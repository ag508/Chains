package com.chain.messaging.core.deployment

import android.content.Context
import com.chain.messaging.core.util.Logger
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BuildConfigManagerTest {

    private lateinit var context: Context
    private lateinit var logger: Logger
    private lateinit var buildConfigManager: BuildConfigManager

    @BeforeEach
    fun setup() {
        context = mockk(relaxed = true)
        logger = mockk(relaxed = true)
        buildConfigManager = BuildConfigManager(context, logger)
    }

    @Test
    fun `getDeploymentInfo should return correct build information`() {
        // When
        val deploymentInfo = buildConfigManager.getDeploymentInfo()

        // Then
        assertTrue(deploymentInfo.versionName.isNotEmpty())
        assertTrue(deploymentInfo.versionCode > 0)
        assertTrue(deploymentInfo.applicationId.isNotEmpty())
        assertTrue(deploymentInfo.buildType.isNotEmpty())
    }

    @Test
    fun `configureForDeployment should configure release settings for release build`() {
        // Given
        mockkObject(buildConfigManager)
        every { buildConfigManager.isReleaseBuild() } returns true

        // When
        buildConfigManager.configureForDeployment()

        // Then
        verify { logger.i("BuildConfigManager", "Configuring release build settings") }
    }

    @Test
    fun `configureForDeployment should configure debug settings for debug build`() {
        // Given
        mockkObject(buildConfigManager)
        every { buildConfigManager.isDebugBuild() } returns true
        every { buildConfigManager.isReleaseBuild() } returns false

        // When
        buildConfigManager.configureForDeployment()

        // Then
        verify { logger.i("BuildConfigManager", "Configuring debug build settings") }
    }

    @Test
    fun `isDebugBuild should return correct debug status`() {
        // When
        val isDebug = buildConfigManager.isDebugBuild()

        // Then
        // This will depend on the actual BuildConfig.DEBUG value
        assertEquals(com.chain.messaging.BuildConfig.DEBUG, isDebug)
    }

    @Test
    fun `isReleaseBuild should return opposite of debug status`() {
        // When
        val isRelease = buildConfigManager.isReleaseBuild()

        // Then
        assertEquals(!com.chain.messaging.BuildConfig.DEBUG, isRelease)
    }

    @Test
    fun `getVersionName should return correct version name`() {
        // When
        val versionName = buildConfigManager.getVersionName()

        // Then
        assertEquals(com.chain.messaging.BuildConfig.VERSION_NAME, versionName)
    }

    @Test
    fun `getVersionCode should return correct version code`() {
        // When
        val versionCode = buildConfigManager.getVersionCode()

        // Then
        assertEquals(com.chain.messaging.BuildConfig.VERSION_CODE, versionCode)
    }

    @Test
    fun `getApplicationId should return correct application ID`() {
        // When
        val applicationId = buildConfigManager.getApplicationId()

        // Then
        assertEquals(com.chain.messaging.BuildConfig.APPLICATION_ID, applicationId)
    }

    @Test
    fun `getBuildType should return correct build type`() {
        // When
        val buildType = buildConfigManager.getBuildType()

        // Then
        assertEquals(com.chain.messaging.BuildConfig.BUILD_TYPE, buildType)
    }
}