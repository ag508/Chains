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
public final class GetUserSettingsUseCase_Factory implements Factory<GetUserSettingsUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public GetUserSettingsUseCase_Factory(Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public GetUserSettingsUseCase get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static GetUserSettingsUseCase_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new GetUserSettingsUseCase_Factory(settingsRepositoryProvider);
  }

  public static GetUserSettingsUseCase newInstance(SettingsRepository settingsRepository) {
    return new GetUserSettingsUseCase(settingsRepository);
  }
}
