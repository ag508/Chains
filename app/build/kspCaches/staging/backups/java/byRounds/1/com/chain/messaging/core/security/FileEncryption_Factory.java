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
public final class FileEncryption_Factory implements Factory<FileEncryption> {
  @Override
  public FileEncryption get() {
    return newInstance();
  }

  public static FileEncryption_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FileEncryption newInstance() {
    return new FileEncryption();
  }

  private static final class InstanceHolder {
    private static final FileEncryption_Factory INSTANCE = new FileEncryption_Factory();
  }
}
