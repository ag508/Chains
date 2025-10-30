package com.chain.messaging.presentation.cloud;

import com.chain.messaging.core.cloud.FileCleanupManager;
import com.chain.messaging.core.cloud.LocalStorageFallback;
import com.chain.messaging.core.cloud.StorageQuotaMonitor;
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
public final class StorageManagementViewModel_Factory implements Factory<StorageManagementViewModel> {
  private final Provider<StorageQuotaMonitor> storageQuotaMonitorProvider;

  private final Provider<FileCleanupManager> fileCleanupManagerProvider;

  private final Provider<LocalStorageFallback> localStorageFallbackProvider;

  public StorageManagementViewModel_Factory(
      Provider<StorageQuotaMonitor> storageQuotaMonitorProvider,
      Provider<FileCleanupManager> fileCleanupManagerProvider,
      Provider<LocalStorageFallback> localStorageFallbackProvider) {
    this.storageQuotaMonitorProvider = storageQuotaMonitorProvider;
    this.fileCleanupManagerProvider = fileCleanupManagerProvider;
    this.localStorageFallbackProvider = localStorageFallbackProvider;
  }

  @Override
  public StorageManagementViewModel get() {
    return newInstance(storageQuotaMonitorProvider.get(), fileCleanupManagerProvider.get(), localStorageFallbackProvider.get());
  }

  public static StorageManagementViewModel_Factory create(
      Provider<StorageQuotaMonitor> storageQuotaMonitorProvider,
      Provider<FileCleanupManager> fileCleanupManagerProvider,
      Provider<LocalStorageFallback> localStorageFallbackProvider) {
    return new StorageManagementViewModel_Factory(storageQuotaMonitorProvider, fileCleanupManagerProvider, localStorageFallbackProvider);
  }

  public static StorageManagementViewModel newInstance(StorageQuotaMonitor storageQuotaMonitor,
      FileCleanupManager fileCleanupManager, LocalStorageFallback localStorageFallback) {
    return new StorageManagementViewModel(storageQuotaMonitor, fileCleanupManager, localStorageFallback);
  }
}
