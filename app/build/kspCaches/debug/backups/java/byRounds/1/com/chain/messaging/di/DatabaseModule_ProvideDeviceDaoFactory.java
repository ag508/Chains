package com.chain.messaging.di;

import com.chain.messaging.data.local.ChainDatabase;
import com.chain.messaging.data.local.dao.DeviceDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideDeviceDaoFactory implements Factory<DeviceDao> {
  private final Provider<ChainDatabase> databaseProvider;

  public DatabaseModule_ProvideDeviceDaoFactory(Provider<ChainDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public DeviceDao get() {
    return provideDeviceDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideDeviceDaoFactory create(
      Provider<ChainDatabase> databaseProvider) {
    return new DatabaseModule_ProvideDeviceDaoFactory(databaseProvider);
  }

  public static DeviceDao provideDeviceDao(ChainDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDeviceDao(database));
  }
}
