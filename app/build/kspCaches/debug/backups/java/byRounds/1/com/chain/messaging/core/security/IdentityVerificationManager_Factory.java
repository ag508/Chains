package com.chain.messaging.core.security;

import com.chain.messaging.core.crypto.KeyManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class IdentityVerificationManager_Factory implements Factory<IdentityVerificationManager> {
  private final Provider<KeyManager> keyManagerProvider;

  private final Provider<QRCodeGenerator> qrCodeGeneratorProvider;

  private final Provider<SafetyNumberGenerator> safetyNumberGeneratorProvider;

  private final Provider<SecurityAlertManager> securityAlertManagerProvider;

  public IdentityVerificationManager_Factory(Provider<KeyManager> keyManagerProvider,
      Provider<QRCodeGenerator> qrCodeGeneratorProvider,
      Provider<SafetyNumberGenerator> safetyNumberGeneratorProvider,
      Provider<SecurityAlertManager> securityAlertManagerProvider) {
    this.keyManagerProvider = keyManagerProvider;
    this.qrCodeGeneratorProvider = qrCodeGeneratorProvider;
    this.safetyNumberGeneratorProvider = safetyNumberGeneratorProvider;
    this.securityAlertManagerProvider = securityAlertManagerProvider;
  }

  @Override
  public IdentityVerificationManager get() {
    return newInstance(keyManagerProvider.get(), qrCodeGeneratorProvider.get(), safetyNumberGeneratorProvider.get(), securityAlertManagerProvider.get());
  }

  public static IdentityVerificationManager_Factory create(Provider<KeyManager> keyManagerProvider,
      Provider<QRCodeGenerator> qrCodeGeneratorProvider,
      Provider<SafetyNumberGenerator> safetyNumberGeneratorProvider,
      Provider<SecurityAlertManager> securityAlertManagerProvider) {
    return new IdentityVerificationManager_Factory(keyManagerProvider, qrCodeGeneratorProvider, safetyNumberGeneratorProvider, securityAlertManagerProvider);
  }

  public static IdentityVerificationManager newInstance(KeyManager keyManager,
      QRCodeGenerator qrCodeGenerator, SafetyNumberGenerator safetyNumberGenerator,
      SecurityAlertManager securityAlertManager) {
    return new IdentityVerificationManager(keyManager, qrCodeGenerator, safetyNumberGenerator, securityAlertManager);
  }
}
