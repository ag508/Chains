package com.chain.messaging.di;

import com.chain.messaging.core.security.QRCodeScanner;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class SecurityModule_ProvideQRCodeScannerFactory implements Factory<QRCodeScanner> {
  @Override
  public QRCodeScanner get() {
    return provideQRCodeScanner();
  }

  public static SecurityModule_ProvideQRCodeScannerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static QRCodeScanner provideQRCodeScanner() {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideQRCodeScanner());
  }

  private static final class InstanceHolder {
    private static final SecurityModule_ProvideQRCodeScannerFactory INSTANCE = new SecurityModule_ProvideQRCodeScannerFactory();
  }
}
