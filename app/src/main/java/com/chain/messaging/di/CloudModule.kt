package com.chain.messaging.di

import android.content.Context
import com.chain.messaging.core.cloud.CloudAuthManager
import com.chain.messaging.core.cloud.CloudAuthManagerImpl
import com.chain.messaging.core.cloud.CloudStorageManager
import com.chain.messaging.core.cloud.CloudStorageManagerImpl
import com.chain.messaging.core.cloud.OAuthCallbackHandler
import com.chain.messaging.core.security.SecureStorage
import com.chain.messaging.core.security.SecureStorageImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CloudModule {
    
    @Binds
    @Singleton
    abstract fun bindCloudAuthManager(
        cloudAuthManagerImpl: CloudAuthManagerImpl
    ): CloudAuthManager
    
    @Binds
    @Singleton
    abstract fun bindSecureStorage(
        secureStorageImpl: SecureStorageImpl
    ): SecureStorage
    
    @Binds
    @Singleton
    abstract fun bindCloudStorageManager(
        cloudStorageManagerImpl: CloudStorageManagerImpl
    ): CloudStorageManager
    
    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }
        
        @Provides
        @Singleton
        fun provideJson(): Json {
            return Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }
        }
        
        @Provides
        @Singleton
        fun provideOAuthCallbackHandler(): OAuthCallbackHandler {
            return OAuthCallbackHandler()
        }
    }
}