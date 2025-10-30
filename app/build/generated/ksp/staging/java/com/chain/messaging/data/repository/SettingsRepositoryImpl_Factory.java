package com.chain.messaging.data.repository;

import com.chain.messaging.data.local.dao.UserSettingsDao;
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
public final class SettingsRepositoryImpl_Factory implements Factory<SettingsRepositoryImpl> {
  private final Provider<UserSettingsDao> settingsDaoProvider;

  public SettingsRepositoryImpl_Factory(Provider<UserSettingsDao> settingsDaoProvider) {
    this.settingsDaoProvider = settingsDaoProvider;
  }

  @Override
  public SettingsRepositoryImpl get() {
    return newInstance(settingsDaoProvider.get());
  }

  public static SettingsRepositoryImpl_Factory create(
      Provider<UserSettingsDao> settingsDaoProvider) {
    return new SettingsRepositoryImpl_Factory(settingsDaoProvider);
  }

  public static SettingsRepositoryImpl newInstance(UserSettingsDao settingsDao) {
    return new SettingsRepositoryImpl(settingsDao);
  }
}
