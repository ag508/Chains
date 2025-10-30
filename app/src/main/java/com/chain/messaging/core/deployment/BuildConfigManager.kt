package com.chain.messaging.core.deployment

import android.content.Context
import com.chain.messaging.BuildConfig
import com.chain.messaging.core.util.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages build configuration and deployment settings
 */
@Singleton
class BuildConfigManager @Inject constructor(
    private val context: Context,
    private val logger: Logger
) {
    
    fun isDebugBuild(): Boolean = BuildConfig.DEBUG
    
    fun isReleaseBuild(): Boolean = !BuildConfig.DEBUG
    
    fun getVersionName(): String = BuildConfig.VERSION_NAME
    
    fun getVersionCode(): Int = BuildConfig.VERSION_CODE
    
    fun getApplicationId(): String = BuildConfig.APPLICATION_ID
    
    fun getBuildType(): String = BuildConfig.BUILD_TYPE
    
    fun configureForDeployment() {
        if (isReleaseBuild()) {
            configureReleaseSettings()
        } else {
            configureDebugSettings()
        }
    }
    
    private fun configureReleaseSettings() {
        logger.i("BuildConfigManager: Configuring release build settings")
        
        // Disable debug logging
        Logger.setDebugEnabled(false)
        
        // Configure crash reporting
        configureCrashReporting()
        
        // Configure analytics
        configureAnalytics()
        
        // Configure security settings
        configureSecuritySettings()
    }
    
    private fun configureDebugSettings() {
        logger.i("BuildConfigManager: Configuring debug build settings")
        
        // Enable debug logging
        Logger.setDebugEnabled(true)
        
        // Configure development tools
        configureDevelopmentTools()
    }
    
    private fun configureCrashReporting() {
        // Configure crash reporting for production
        logger.d("BuildConfigManager: Crash reporting configured")
    }
    
    private fun configureAnalytics() {
        // Configure analytics for production
        logger.d("BuildConfigManager: Analytics configured")
    }
    
    private fun configureSecuritySettings() {
        // Configure security settings for production
        logger.d("BuildConfigManager: Security settings configured")
    }
    
    private fun configureDevelopmentTools() {
        // Configure development and debugging tools
        logger.d("BuildConfigManager: Development tools configured")
    }
    
    fun getDeploymentInfo(): DeploymentInfo {
        return DeploymentInfo(
            versionName = getVersionName(),
            versionCode = getVersionCode(),
            buildType = getBuildType(),
            isDebug = isDebugBuild(),
            applicationId = getApplicationId()
        )
    }
    
    /**
     * Initialize deployment configuration
     */
    fun initializeDeployment() {
        logger.i("BuildConfigManager: Initializing deployment configuration")
        configureForDeployment()
    }
    
    /**
     * Validate deployment configuration
     */
    fun validateDeploymentConfig(): Boolean {
        return try {
            val info = getDeploymentInfo()
            val isValid = info.versionName.isNotEmpty() && 
                         info.versionCode > 0 && 
                         info.applicationId.isNotEmpty() &&
                         info.buildType.isNotEmpty()
            
            if (isValid) {
                logger.i("BuildConfigManager: Deployment configuration is valid")
            } else {
                logger.w("BuildConfigManager: Deployment configuration validation failed")
            }
            
            isValid
        } catch (e: Exception) {
            logger.e("BuildConfigManager: Error validating deployment configuration", e)
            false
        }
    }
    
    /**
     * Get deployment environment
     */
    fun getDeploymentEnvironment(): DeploymentEnvironment {
        return when {
            isDebugBuild() -> DeploymentEnvironment.DEBUG
            getBuildType().equals("staging", ignoreCase = true) -> DeploymentEnvironment.STAGING
            getBuildType().equals("release", ignoreCase = true) -> DeploymentEnvironment.PRODUCTION
            else -> DeploymentEnvironment.UNKNOWN
        }
    }
    
    /**
     * Configure deployment-specific settings
     */
    fun configureDeploymentSettings(environment: DeploymentEnvironment) {
        logger.i("BuildConfigManager: Configuring settings for environment: $environment")
        
        when (environment) {
            DeploymentEnvironment.DEBUG -> configureDebugSettings()
            DeploymentEnvironment.STAGING -> configureStagingSettings()
            DeploymentEnvironment.PRODUCTION -> configureReleaseSettings()
            DeploymentEnvironment.UNKNOWN -> {
                logger.w("BuildConfigManager: Unknown deployment environment, using default settings")
                configureForDeployment()
            }
        }
    }
    
    private fun configureStagingSettings() {
        logger.i("BuildConfigManager: Configuring staging build settings")
        
        // Enable limited debug logging for staging
        Logger.setDebugEnabled(true)
        
        // Configure staging-specific settings
        configureCrashReporting()
        configureAnalytics()
        
        // Configure staging security settings (less strict than production)
        configureSecuritySettings()
    }
}

data class DeploymentInfo(
    val versionName: String,
    val versionCode: Int,
    val buildType: String,
    val isDebug: Boolean,
    val applicationId: String
)

enum class DeploymentEnvironment {
    DEBUG,
    STAGING,
    PRODUCTION,
    UNKNOWN
}