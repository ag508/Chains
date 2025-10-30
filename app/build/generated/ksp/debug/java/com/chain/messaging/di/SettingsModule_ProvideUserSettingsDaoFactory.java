package com.chain.messaging.di;

import com.chain.messaging.data.local.ChainDatabase;
import com.chain.messaging.data.local.dao.UserSettingsDao;
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
public final class SettingsModule_ProvideUserSettingsDaoFactory implements Factory<UserSettingsDao> {
  private final Provider<ChainDatabase> databaseProvider;

  public SettingsModule_ProvideUserSettingsDaoFactory(Provider<ChainDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public UserSettingsDao get() {
    return provideUserSettingsDao(databaseProvider.get());
  }

  public static SettingsModule_ProvideUserSettingsDaoFactory create(
      Provider<ChainDatabase> databaseProvider) {
    return new SettingsModule_ProvideUserSettingsDaoFactory(databaseProvider);
  }

  public static UserSettingsDao provideUserSettingsDao(ChainDatabase database) {
    return Preconditions.checkNotNullFromProvides(SettingsModule.INSTANCE.provideUserSettingsDao(database));
  }
}
