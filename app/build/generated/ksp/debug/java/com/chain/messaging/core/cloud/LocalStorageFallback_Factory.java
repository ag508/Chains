package com.chain.messaging.core.cloud;

import android.content.Context;
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
public final class LocalStorageFallback_Factory implements Factory<LocalStorageFallback> {
  private final Provider<Context> contextProvider;

  private final Provider<FileEncryption> fileEncryptionProvider;

  public LocalStorageFallback_Factory(Provider<Context> contextProvider,
      Provider<FileEncryption> fileEncryptionProvider) {
    this.contextProvider = contextProvider;
    this.fileEncryptionProvider = fileEncryptionProvider;
  }

  @Override
  public LocalStorageFallback get() {
    return newInstance(contextProvider.get(), fileEncryptionProvider.get());
  }

  public static LocalStorageFallback_Factory create(Provider<Context> contextProvider,
      Provider<FileEncryption> fileEncryptionProvider) {
    return new LocalStorageFallback_Factory(contextProvider, fileEncryptionProvider);
  }

  public static LocalStorageFallback newInstance(Context context, FileEncryption fileEncryption) {
    return new LocalStorageFallback(context, fileEncryption);
  }
}
