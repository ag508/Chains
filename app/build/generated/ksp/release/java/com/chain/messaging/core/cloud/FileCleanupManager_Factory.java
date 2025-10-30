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
public final class FileCleanupManager_Factory implements Factory<FileCleanupManager> {
  private final Provider<Context> contextProvider;

  private final Provider<CloudStorageManager> cloudStorageManagerProvider;

  public FileCleanupManager_Factory(Provider<Context> contextProvider,
      Provider<CloudStorageManager> cloudStorageManagerProvider) {
    this.contextProvider = contextProvider;
    this.cloudStorageManagerProvider = cloudStorageManagerProvider;
  }

  @Override
  public FileCleanupManager get() {
    return newInstance(contextProvider.get(), cloudStorageManagerProvider.get());
  }

  public static FileCleanupManager_Factory create(Provider<Context> contextProvider,
      Provider<CloudStorageManager> cloudStorageManagerProvider) {
    return new FileCleanupManager_Factory(contextProvider, cloudStorageManagerProvider);
  }

  public static FileCleanupManager newInstance(Context context,
      CloudStorageManager cloudStorageManager) {
    return new FileCleanupManager(context, cloudStorageManager);
  }
}
