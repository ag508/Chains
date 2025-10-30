package com.chain.messaging.presentation.security;

import com.chain.messaging.core.security.IdentityVerificationManager;
import com.chain.messaging.core.security.QRCodeScanner;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class IdentityVerificationViewModel_Factory implements Factory<IdentityVerificationViewModel> {
  private final Provider<IdentityVerificationManager> identityVerificationManagerProvider;

  private final Provider<QRCodeScanner> qrCodeScannerProvider;

  public IdentityVerificationViewModel_Factory(
      Provider<IdentityVerificationManager> identityVerificationManagerProvider,
      Provider<QRCodeScanner> qrCodeScannerProvider) {
    this.identityVerificationManagerProvider = identityVerificationManagerProvider;
    this.qrCodeScannerProvider = qrCodeScannerProvider;
  }

  @Override
  public IdentityVerificationViewModel get() {
    return newInstance(identityVerificationManagerProvider.get(), qrCodeScannerProvider.get());
  }

  public static IdentityVerificationViewModel_Factory create(
      Provider<IdentityVerificationManager> identityVerificationManagerProvider,
      Provider<QRCodeScanner> qrCodeScannerProvider) {
    return new IdentityVerificationViewModel_Factory(identityVerificationManagerProvider, qrCodeScannerProvider);
  }

  public static IdentityVerificationViewModel newInstance(
      IdentityVerificationManager identityVerificationManager, QRCodeScanner qrCodeScanner) {
    return new IdentityVerificationViewModel(identityVerificationManager, qrCodeScanner);
  }
}
