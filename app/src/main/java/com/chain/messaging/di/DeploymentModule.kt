package com.chain.messaging.di

import android.content.Context
import com.chain.messaging.core.deployment.*
import com.chain.messaging.core.util.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeploymentModule {

    @Provides
    @Singleton
    fun providePerformanceOptimizer(
        @ApplicationContext context: Context,
        logger: Logger
    ): PerformanceOptimizer {
        return PerformanceOptimizer(context, logger)
    }

    @Provides
    @Singleton
    fun provideBuildConfigManager(
        @ApplicationContext context: Context,
        logger: Logger
    ): BuildConfigManager {
        return BuildConfigManager(context, logger)
    }

    @Provides
    @Singleton
    fun provideCodeCleanupManager(
        logger: Logger
    ): CodeCleanupManager {
        return CodeCleanupManager(logger)
    }

    @Provides
    @Singleton
    fun provideDocumentationGenerator(
        buildConfigManager: BuildConfigManager,
        logger: Logger
    ): DocumentationGenerator {
        return DocumentationGenerator(buildConfigManager, logger)
    }

    @Provides
    @Singleton
    fun provideAppStoreManager(
        buildConfigManager: BuildConfigManager,
        logger: Logger
    ): AppStoreManager {
        return AppStoreManager(buildConfigManager, logger)
    }

    @Provides
    @Singleton
    fun provideDeploymentManager(
        performanceOptimizer: PerformanceOptimizer,
        buildConfigManager: BuildConfigManager,
        codeCleanupManager: CodeCleanupManager,
        documentationGenerator: DocumentationGenerator,
        appStoreManager: AppStoreManager,
        logger: Logger
    ): DeploymentManager {
        return DeploymentManager(
            performanceOptimizer,
            buildConfigManager,
            codeCleanupManager,
            documentationGenerator,
            appStoreManager,
            logger
        )
    }
}