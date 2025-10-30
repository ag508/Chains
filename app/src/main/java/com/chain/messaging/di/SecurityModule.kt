package com.chain.messaging.di

import com.chain.messaging.core.crypto.KeyManager
import com.chain.messaging.core.security.IdentityVerificationManager
import com.chain.messaging.core.security.QRCodeGenerator
import com.chain.messaging.core.security.QRCodeScanner
import com.chain.messaging.core.security.SafetyNumberGenerator
import com.chain.messaging.core.security.SecurityAlertManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideQRCodeGenerator(): QRCodeGenerator {
        return QRCodeGenerator()
    }
    
    @Provides
    @Singleton
    fun provideQRCodeScanner(): QRCodeScanner {
        return QRCodeScanner()
    }
    
    @Provides
    @Singleton
    fun provideSafetyNumberGenerator(): SafetyNumberGenerator {
        return SafetyNumberGenerator()
    }
    
    @Provides
    @Singleton
    fun provideSecurityAlertManager(): SecurityAlertManager {
        return SecurityAlertManager()
    }
    
    @Provides
    @Singleton
    fun provideIdentityVerificationManager(
        keyManager: KeyManager,
        qrCodeGenerator: QRCodeGenerator,
        safetyNumberGenerator: SafetyNumberGenerator,
        securityAlertManager: SecurityAlertManager
    ): IdentityVerificationManager {
        return IdentityVerificationManager(
            keyManager = keyManager,
            qrCodeGenerator = qrCodeGenerator,
            safetyNumberGenerator = safetyNumberGenerator,
            securityAlertManager = securityAlertManager
        )
    }
}