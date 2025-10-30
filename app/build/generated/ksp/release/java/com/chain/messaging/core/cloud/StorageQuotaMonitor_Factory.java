package com.chain.messaging.core.cloud;

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
public final class StorageQuotaMonitor_Factory implements Factory<StorageQuotaMonitor> {
  private final Provider<CloudStorageManager> cloudStorageManagerProvider;

  public StorageQuotaMonitor_Factory(Provider<CloudStorageManager> cloudStorageManagerProvider) {
    this.cloudStorageManagerProvider = cloudStorageManagerProvider;
  }

  @Override
  public StorageQuotaMonitor get() {
    return newInstance(cloudStorageManagerProvider.get());
  }

  public static StorageQuotaMonitor_Factory create(
      Provider<CloudStorageManager> cloudStorageManagerProvider) {
    return new StorageQuotaMonitor_Factory(cloudStorageManagerProvider);
  }

  public static StorageQuotaMonitor newInstance(CloudStorageManager cloudStorageManager) {
    return new StorageQuotaMonitor(cloudStorageManager);
  }
}
