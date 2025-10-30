package com.chain.messaging.di

import android.content.Context
import com.chain.messaging.core.auth.AuthenticationService
import com.chain.messaging.core.auth.OAuthManager
import com.chain.messaging.core.auth.PasskeyManager
import com.chain.messaging.core.auth.UserIdentityManager
import com.chain.messaging.core.crypto.KeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for authentication components
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideUserIdentityManager(
        @ApplicationContext context: Context
    ): UserIdentityManager {
        return UserIdentityManager(context)
    }

    @Provides
    @Singleton
    fun provideOAuthManager(
        @ApplicationContext context: Context
    ): OAuthManager {
        return OAuthManager(context)
    }

    @Provides
    @Singleton
    fun providePasskeyManager(
        @ApplicationContext context: Context
    ): PasskeyManager {
        return PasskeyManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthenticationService(
        @ApplicationContext context: Context,
        keyManager: KeyManager,
        userIdentityManager: UserIdentityManager,
        oAuthManager: OAuthManager,
        passkeyManager: PasskeyManager
    ): AuthenticationService {
        return AuthenticationService(
            context = context,
            keyManager = keyManager,
            userIdentityManager = userIdentityManager,
            oAuthManager = oAuthManager,
            passkeyManager = passkeyManager
        )
    }
}