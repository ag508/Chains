package com.chain.messaging.di;

import com.chain.messaging.data.local.dao.UserSettingsDao;
import com.chain.messaging.domain.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class SettingsModule_ProvideSettingsRepositoryFactory implements Factory<SettingsRepository> {
  private final Provider<UserSettingsDao> userSettingsDaoProvider;

  public SettingsModule_ProvideSettingsRepositoryFactory(
      Provider<UserSettingsDao> userSettingsDaoProvider) {
    this.userSettingsDaoProvider = userSettingsDaoProvider;
  }

  @Override
  public SettingsRepository get() {
    return provideSettingsRepository(userSettingsDaoProvider.get());
  }

  public static SettingsModule_ProvideSettingsRepositoryFactory create(
      Provider<UserSettingsDao> userSettingsDaoProvider) {
    return new SettingsModule_ProvideSettingsRepositoryFactory(userSettingsDaoProvider);
  }

  public static SettingsRepository provideSettingsRepository(UserSettingsDao userSettingsDao) {
    return Preconditions.checkNotNullFromProvides(SettingsModule.INSTANCE.provideSettingsRepository(userSettingsDao));
  }
}
