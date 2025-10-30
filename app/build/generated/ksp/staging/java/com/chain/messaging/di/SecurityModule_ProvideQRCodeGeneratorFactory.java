package com.chain.messaging.di;

import com.chain.messaging.core.security.QRCodeGenerator;
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
public final class SecurityModule_ProvideQRCodeGeneratorFactory implements Factory<QRCodeGenerator> {
  @Override
  public QRCodeGenerator get() {
    return provideQRCodeGenerator();
  }

  public static SecurityModule_ProvideQRCodeGeneratorFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static QRCodeGenerator provideQRCodeGenerator() {
    return Preconditions.checkNotNullFromProvides(SecurityModule.INSTANCE.provideQRCodeGenerator());
  }

  private static final class InstanceHolder {
    private static final SecurityModule_ProvideQRCodeGeneratorFactory INSTANCE = new SecurityModule_ProvideQRCodeGeneratorFactory();
  }
}
