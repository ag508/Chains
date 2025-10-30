package com.chain.messaging.presentation.sync;

import com.chain.messaging.core.sync.CrossDeviceSyncService;
import com.chain.messaging.core.sync.DeviceManager;
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
public final class DeviceManagementViewModel_Factory implements Factory<DeviceManagementViewModel> {
  private final Provider<DeviceManager> deviceManagerProvider;

  private final Provider<CrossDeviceSyncService> crossDeviceSyncServiceProvider;

  public DeviceManagementViewModel_Factory(Provider<DeviceManager> deviceManagerProvider,
      Provider<CrossDeviceSyncService> crossDeviceSyncServiceProvider) {
    this.deviceManagerProvider = deviceManagerProvider;
    this.crossDeviceSyncServiceProvider = crossDeviceSyncServiceProvider;
  }

  @Override
  public DeviceManagementViewModel get() {
    return newInstance(deviceManagerProvider.get(), crossDeviceSyncServiceProvider.get());
  }

  public static DeviceManagementViewModel_Factory create(
      Provider<DeviceManager> deviceManagerProvider,
      Provider<CrossDeviceSyncService> crossDeviceSyncServiceProvider) {
    return new DeviceManagementViewModel_Factory(deviceManagerProvider, crossDeviceSyncServiceProvider);
  }

  public static DeviceManagementViewModel newInstance(DeviceManager deviceManager,
      CrossDeviceSyncService crossDeviceSyncService) {
    return new DeviceManagementViewModel(deviceManager, crossDeviceSyncService);
  }
}
