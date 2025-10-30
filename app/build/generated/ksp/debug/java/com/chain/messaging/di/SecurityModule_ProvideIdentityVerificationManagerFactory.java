package com.chain.messaging.di;

import com.chain.messaging.core.crypto.KeyManager;
import com.chain.messaging.core.security.IdentityVerificationManager;
import com.chain.messaging.core.security.QRCodeGenerator;
import com.chain.messaging.core.security.SafetyNumberGenerator;
import com.chain.messaging.core.security.SecurityAlertManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class SecurityModule_ProvideIdentityVerificationManagerFactory implements Factory<IdentityVerificationManager> {
  private final Provider<KeyManager> keyManagerProvider;

  private final Provider<QRCodeGenerator> qrCodeGeneratorProvider;

  private final Provider<SafetyNumberGenerator> safetyNumberGeneratorProvider;

  private final Provider<SecurityAlertManager> securityAlertManagerProvider;

  public SecurityModule_ProvideIdentityVerificationManagerFactory(
      Provider<KeyManager> keyManagerProvider, Provider<QRCodeGenerator> qrCodeGeneratorProvider,
      Provider<SafetyNumberGenerator> safetyNumberGeneratorProvider,
      Provider<SecurityAlertManager> securityAlertManagerProvider) {
    this.keyManagerProvider = keyManagerProvider;
    this.qrCodeGeneratorProvider = qrCodeGeneratorProvider;
    this.safetyNumberGeneratorProvider = safetyNumberGeneratorProvider;
    this.securityAlertManagerProvider = securityAlertManagerProvider;
  }

  @Override
  public IdentityVerificationManager get() {
    return provideIdentityVerificationManager(keyManagerProvider.get(), qrCodeGeneratorProvider.get(), safetyNumberGeneratorProvider.get(), securityAlertManagerProvider.get());
  }

  public static SecurityModule_ProvideIdentityVerificationManagerFactory create(
      Provider<KeyManager> keyManagerProvider, Provider<QRCodeGenerator> qrCodeGeneratorProvider,
      Provider<SafetyNumberGenerator> safetyNumberGeneratorProvider,
      Provider<SecurityAlertManager> securityAlertManagerProvider) {
    return new SecurityModule_ProvideIdentityVerificationManagerFactory(keyManagerProvider, qrCodeGeneratorProvider, safetyNumberGeneratorProvider, securityAlertManagerProvider);
  }

  public static IdentityVerificationManager provideIdentityVerificationManager(
      KeyManager keyManager, QRCodeGenerator qrCodeGenerator,
      SafetyNumberGenerator safetyNumberGenerator, SecurityAlertManager securityAlertManager) {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideIdentityVerificationManager(keyManager, qrCodeGenerator, safetyNumberGenerator, securityAlertManager));
  }
}
