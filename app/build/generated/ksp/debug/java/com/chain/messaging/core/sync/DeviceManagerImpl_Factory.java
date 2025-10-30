package com.chain.messaging.core.sync;

import android.content.Context;
import com.chain.messaging.core.crypto.KeyManager;
import com.chain.messaging.data.local.dao.DeviceDao;
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
public final class DeviceManagerImpl_Factory implements Factory<DeviceManagerImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<DeviceDao> deviceDaoProvider;

  private final Provider<KeyManager> keyManagerProvider;

  public DeviceManagerImpl_Factory(Provider<Context> contextProvider,
      Provider<DeviceDao> deviceDaoProvider, Provider<KeyManager> keyManagerProvider) {
    this.contextProvider = contextProvider;
    this.deviceDaoProvider = deviceDaoProvider;
    this.keyManagerProvider = keyManagerProvider;
  }

  @Override
  public DeviceManagerImpl get() {
    return newInstance(contextProvider.get(), deviceDaoProvider.get(), keyManagerProvider.get());
  }

  public static DeviceManagerImpl_Factory create(Provider<Context> contextProvider,
      Provider<DeviceDao> deviceDaoProvider, Provider<KeyManager> keyManagerProvider) {
    return new DeviceManagerImpl_Factory(contextProvider, deviceDaoProvider, keyManagerProvider);
  }

  public static DeviceManagerImpl newInstance(Context context, DeviceDao deviceDao,
      KeyManager keyManager) {
    return new DeviceManagerImpl(context, deviceDao, keyManager);
  }
}
