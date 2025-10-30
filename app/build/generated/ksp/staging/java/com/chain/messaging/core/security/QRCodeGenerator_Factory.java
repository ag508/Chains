package com.chain.messaging.core.security;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class QRCodeGenerator_Factory implements Factory<QRCodeGenerator> {
  @Override
  public QRCodeGenerator get() {
    return newInstance();
  }

  public static QRCodeGenerator_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static QRCodeGenerator newInstance() {
    return new QRCodeGenerator();
  }

  private static final class InstanceHolder {
    private static final QRCodeGenerator_Factory INSTANCE = new QRCodeGenerator_Factory();
  }
}
