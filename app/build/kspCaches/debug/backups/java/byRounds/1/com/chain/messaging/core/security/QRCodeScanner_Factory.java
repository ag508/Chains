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
public final class QRCodeScanner_Factory implements Factory<QRCodeScanner> {
  @Override
  public QRCodeScanner get() {
    return newInstance();
  }

  public static QRCodeScanner_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static QRCodeScanner newInstance() {
    return new QRCodeScanner();
  }

  private static final class InstanceHolder {
    private static final QRCodeScanner_Factory INSTANCE = new QRCodeScanner_Factory();
  }
}
