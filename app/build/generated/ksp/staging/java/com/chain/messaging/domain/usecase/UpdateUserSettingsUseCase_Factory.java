package com.chain.messaging.domain.usecase;

import com.chain.messaging.domain.repository.SettingsRepository;
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
public final class UpdateUserSettingsUseCase_Factory implements Factory<UpdateUserSettingsUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public UpdateUserSettingsUseCase_Factory(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public UpdateUserSettingsUseCase get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static UpdateUserSettingsUseCase_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new UpdateUserSettingsUseCase_Factory(settingsRepositoryProvider);
  }

  public static UpdateUserSettingsUseCase newInstance(SettingsRepository settingsRepository) {
    return new UpdateUserSettingsUseCase(settingsRepository);
  }
}
